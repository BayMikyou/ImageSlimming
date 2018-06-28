package com.mikyou.plugins.image.slimming.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.mikyou.plugins.image.slimming.extension.showDialog
import com.mikyou.plugins.image.slimming.ui.ImageSlimmingDialog
import com.mikyou.plugins.image.slimming.ui.InputKeyDialog
import com.mikyou.plugins.image.slimming.ui.model.ImageSlimmingModel
import com.tinify.Tinify
import java.io.File
import java.nio.charset.Charset


class ImageSlimmingAction : AnAction() {
    //插件入口函数
    override fun actionPerformed(event: AnActionEvent?) = with(File(FILE_PATH_API_KEY)) {
        if (!exists()) {//保存API_KEY的文件不存在，提示用户输入
            popupInputKeyDialog(labelTitle = "请输入TinyPng Key, 请往TinyPng官网申请", event = event)
        } else {//文件存在，读取相应的KEY，检查有效性
            checkKeyValid(event, readText(Charset.defaultCharset()))
        }
    }

    //检查Key的合法性
    private fun checkKeyValid(event: AnActionEvent?, apiKey: String) {
        if (apiKey.isBlank()) {
            popupInputKeyDialog(labelTitle = "TinyPng key验证失败，请重新输入", event = event)
            return
        }
        getEventProject(event)?.asyncTask(hintText = "正在检查key是否合法", runAction = {
            try {
                Tinify.setKey(apiKey)
                Tinify.validate()
            } catch (exception: Exception) {
                throw exception
            }
        }, successAction = {
            updateExpireKey(apiKey)
            popupCompressDialog(event)
        }, failAction = {
            println("验证Key失败!!${it.printStackTrace()}")
            popupInputKeyDialog(labelTitle = "TinyPng key验证失败，请重新输入", event = event)
        })
    }

    //更新本地缓存中过期或者失效的API_KEY
    private fun updateExpireKey(apiKey: String) = File(FILE_PATH_API_KEY).createFile {
        if (it.readText(Charset.defaultCharset()) != apiKey) {
            it.writeText(apiKey, Charset.defaultCharset())
        }
    }

    //弹出输入apiKey dialog
    private fun popupInputKeyDialog(labelTitle: String, event: AnActionEvent?) = InputKeyDialog(labelTitle, object : InputKeyDialog.DialogCallback {
        override fun onOkBtnClicked(tinyPngKey: String) {
            checkKeyValid(event, tinyPngKey ?: "")
        }

        override fun onCancelBtnClicked() {

        }
    }).showDialog(width = 530, height = 150, isInCenter = true, isResizable = false)

    //弹出压缩目录选择 dialog
    private fun popupCompressDialog(e: AnActionEvent?) = ImageSlimmingDialog(readUsedDirs(), object : ImageSlimmingDialog.DialogCallback {
        override fun onOkClicked(imageSlimmingModel: ImageSlimmingModel) {
            saveUsedDirs(imageSlimmingModel)
            val inputFiles: List<File> = readInputDirFiles(imageSlimmingModel.inputDir)
            val startTime = System.currentTimeMillis()
            getEventProject(e)?.asyncTask(hintText = "正在压缩", runAction = {
                executeCompressPic(inputFiles, imageSlimmingModel)
            }, successAction = {
                Messages.showWarningDialog("压缩完成, 已压缩: ${inputFiles.size}张图片, 压缩总时长共计: ${(System.currentTimeMillis() - startTime) / 1000}s", "来自FastCompress提示")
            }, failAction = {
                println(it.message)
            })
        }

        override fun onCancelClicked() {

        }

    }).showDialog(width = 530, height = 180, isInCenter = true, isResizable = false)

    //读取本地缓存了用户使用过的输入，输出目录的文件
    private fun readUsedDirs(): Pair<List<String>, List<String>> {
        val inputDirStrs = mutableListOf<String>()
        val outputDirStrs = mutableListOf<String>()
        val inputDirsFile = File(FILE_PATH_INPUT_DIRS)
        val outputDirsFile = File(FILE_PATH_OUTPUT_DIRS)

        if (inputDirsFile.exists()) {
            inputDirStrs.addAll(inputDirsFile.readLines(Charset.defaultCharset()))
        }

        if (outputDirsFile.exists()) {
            outputDirStrs.addAll(outputDirsFile.readLines(Charset.defaultCharset()))
        }

        return inputDirStrs to outputDirStrs
    }

    //写入用户当前使用的输入和输出路径到缓存文件中
    private fun saveUsedDirs(imageSlimmingModel: ImageSlimmingModel) {

        File(FILE_PATH_INPUT_DIRS).createFile {
            if (!it.readLines(Charset.defaultCharset()).contains(imageSlimmingModel.inputDir)) {
                it.appendText("${imageSlimmingModel.inputDir}\n", Charset.defaultCharset())
            }
        }

        File(FILE_PATH_OUTPUT_DIRS).createFile {
            if (!it.readLines(Charset.defaultCharset()).contains(imageSlimmingModel.outputDir)) {
                it.appendText("${imageSlimmingModel.outputDir}\n", Charset.defaultCharset())
            }
        }
    }

    //执行图片压缩操作
    private fun executeCompressPic(inputFiles: List<File>, imageSlimmingModel: ImageSlimmingModel) = inputFiles.forEach {
        Tinify.fromFile(it.absolutePath).toFile("${imageSlimmingModel.outputDir}/${it.name}")
    }

    //读取用户输入目录下的所有图片文件
    private fun readInputDirFiles(inputDir: String): List<File> {
        val inputFiles: List<String> = inputDir.split(",")
        if (inputFiles.isEmpty()) {
            return listOf()
        }

        if (inputFiles.size == 1) {
            val inputFile: File = File(inputFiles[0])
            if (inputFile.isFile) {
                return listOf(inputFile).filterPic()
            }

            if (inputFile.isDirectory) {
                return inputFile.listFiles().toList().filterPic()
            }
        }

        return inputFiles.map { File(it) }.filterPic()
    }

    //筛选图片后缀的文件List<File>的扩展函数filterPic
    private fun List<File>.filterPic(): List<File> = this.filter { it.name.endsWith(".png") || it.name.endsWith(".jpg") || it.name.endsWith(".jpeg") }

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

    //创建文件扩展函数
    private fun File.createFile(existedAction: ((File) -> Unit)? = null) {
        if (!exists()) {//文件不存在,创建一个新文件
            File(parent).mkdir()
            createNewFile()
        }
        //文件存在
        existedAction?.invoke(this)
    }

    companion object {
        private var FILE_PATH_API_KEY = "${PathManager.getPreInstalledPluginsPath()}/FastCompress/apiKey.csv"
        private var FILE_PATH_INPUT_DIRS = "${PathManager.getPreInstalledPluginsPath()}/FastCompress/inputDirs.csv"
        private var FILE_PATH_OUTPUT_DIRS = "${PathManager.getPreInstalledPluginsPath()}/FastCompress/outputDirs.csv"
    }
}