package com.mikyou.plugins.image.slimming.helper

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.mikyou.plugins.image.slimming.extension.otherwise
import com.mikyou.plugins.image.slimming.extension.yes
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
        model: ImageSlimmingModel = ImageSlimmingModel("", "", "", ""),
        successAction: (() -> Unit)? = null,
        outputSameFile: Boolean = false,
        failAction: ((String) -> Unit)? = null
) {
    project?.asyncTask(hintText = "正在压缩", runAction = {
        //执行图片压缩操作
        outputSameFile.yes {
            //针对右键选定图片情况，直接压缩当前目录选中图片，输出目录包括文件也是原来的
            inputFiles.forEach { inputFile -> Tinify.fromFile(inputFile.absolutePath).toFile(inputFile.absolutePath) }
        }.otherwise {
            inputFiles.forEach { inputFile -> Tinify.fromFile(inputFile.absolutePath).toFile(getDestFilePath(model, inputFile.name)) }
        }
    }, successAction = {
        successAction?.invoke()
    }, failAction = {
        failAction?.invoke("TinyPng key存在异常，请重新输入")
    })
}

private fun getDestFilePath(model: ImageSlimmingModel, sourceName: String): String {
    return model.filePrefix.isBlank()
            .yes { "${model.outputDir}/${getFileRename(sourceName = sourceName, rename = model.rename)}" }
            .otherwise { "${model.outputDir}/${model.filePrefix}_${getFileRename(sourceName = sourceName, rename = model.rename)}" }
}

private fun getFileRename(sourceName: String, rename: String): String {
    return rename.isNotBlank()
            .yes { "$rename.${sourceName.split(".").last()}" }
            .otherwise { sourceName }
}


//创建后台异步任务的Project的扩展函数asyncTask
private fun Project.asyncTask(
        hintText: String,
        runAction: (ProgressIndicator) -> Unit,
        successAction: (() -> Unit)? = null,
        failAction: ((Throwable) -> Unit)? = null,
        finishAction: (() -> Unit)? = null
) {
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