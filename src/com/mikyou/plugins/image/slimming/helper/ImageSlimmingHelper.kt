package com.mikyou.plugins.image.slimming.helper

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.mikyou.plugins.image.slimming.ui.model.ImageSlimmingModel
import com.tinify.Tinify
import java.io.File

fun setTinyPngApiKey(apiKey: String) {
    Tinify.setKey(apiKey)
}

fun checkApiKeyValid(
        project: Project?,
        apiKey: String,
        validAction: (() -> Unit)? = null,
        invalidAction: ((String) -> Unit)? = null
) {
    if (apiKey.isBlank()) {
        invalidAction?.invoke("TinyPng key为空，请重新输入")
    }
    project?.asyncTask(hintText = "正在检查key是否合法", runAction = {
        try {
            Tinify.setKey(apiKey)
            Tinify.validate()
        } catch (exception: Exception) {
            throw exception
        }
    }, successAction = {
        validAction?.invoke()
    }, failAction = {
        println("验证Key失败!!${it.message}")
        invalidAction?.invoke("TinyPng key验证失败，请重新输入")
    })
}

fun slimImage(
        project: Project?,
        inputFiles: List<File>,
        model: ImageSlimmingModel = ImageSlimmingModel("", "", ""),
        successAction: (() -> Unit)? = null,
        outputSameFile: Boolean = false,
        failAction: ((String) -> Unit)? = null
) {
    project?.asyncTask(hintText = "正在压缩", runAction = {
        //执行图片压缩操作
        if (outputSameFile) {//针对右键选定图片情况，直接压缩当前目录选中图片，输出目录包括文件也是原来的
            inputFiles.forEach { Tinify.fromFile(it.absolutePath).toFile(it.absolutePath) }
        } else {
            inputFiles.forEach {
                if (model.filePrefix.isBlank()) {
                    Tinify.fromFile(it.absolutePath).toFile("${model.outputDir}/${it.name}")
                } else {
                    Tinify.fromFile(it.absolutePath).toFile("${model.outputDir}/${model.filePrefix}_${it.name}")
                }
            }
        }
    }, successAction = {
        successAction?.invoke()
    }, failAction = {
        //由于之前并没有对文件中的API_KEY提前做有效性检验，所以此处需要捕获API_KEY认证的异常:
        if (it.message?.contains("Bad authorization") == true || it.message?.contains("HTTP 400/Bad request") == true) {
            failAction?.invoke("TinyPng key存在异常，请重新输入")
        }
    })
}

//创建后台异步任务的Project的扩展函数asyncTask
private fun Project.asyncTask(hintText: String, runAction: (ProgressIndicator) -> Unit, successAction: (() -> Unit)? = null, failAction: ((Throwable) -> Unit)? = null, finishAction: (() -> Unit)? = null) {
    object : Task.Backgroundable(this, hintText) {
        override fun run(p0: ProgressIndicator) {
            runAction.invoke(p0)
        }

        override fun onSuccess() {
            successAction?.invoke()
        }

        override fun onThrowable(error: Throwable) {
            failAction?.invoke(error)
        }

        override fun onFinished() {
            finishAction?.invoke()
        }
    }.queue()
}