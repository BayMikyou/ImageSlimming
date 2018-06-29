package com.mikyou.plugins.image.slimming.helper

import com.intellij.openapi.application.PathManager
import com.mikyou.plugins.image.slimming.ui.model.ImageSlimmingModel
import java.io.File
import java.nio.charset.Charset

class FileIOHelper {
    //检查ApiKey文件是否存在
    fun checkApiKeyFile(inexistAction: ((String) -> Unit)? = null, existAction: ((String) -> Unit)? = null) = with(File(FILE_PATH_API_KEY)) {
        if (!exists() || readText(Charset.defaultCharset()).isBlank()) {//保存API_KEY的文件不存在, 或者读取的文件内容为空，提示用户输入(一般为第一次)
            return@with inexistAction?.invoke("请输入TinyPng Key, 请往TinyPng官网申请")
        } else {//为了减少不必要API KEY检查次数，此处改为只要文件存在，直接读取文件中的API_KEY
            return@with existAction?.invoke(readText(Charset.defaultCharset()))//(注意:此时API_KEY有可能不合法，此处先不做检验，而是去捕获认证异常，提示用户重新键入API_KEY)
        }
    }

    //更新本地缓存中过期或者失效的API_KEY
    fun updateExpireKey(apiKey: String) = File(FILE_PATH_API_KEY).createFile {
        if (it.readText(Charset.defaultCharset()) != apiKey) {
            it.writeText(apiKey, Charset.defaultCharset())
        }
    }


    //读取本地缓存了用户使用过的输入，输出目录的文件
    fun readUsedDirs(): Pair<List<String>, List<String>> {
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

    //读取本地缓存了用户使用过的文件前缀
    fun readUsedFilePrefix(): List<String> = with(File(FILE_PATH_PREFIX)) {
        return if (exists()) {
            readLines(Charset.defaultCharset())
        } else {
            listOf()
        }
    }

    //读取用户输入目录下的所有图片文件
    fun readInputDirFiles(inputDir: String): List<File> {
        val inputFiles: List<String> = inputDir.split(",")
        if (inputFiles.isEmpty()) {
            return listOf()
        }

        if (inputFiles.size == 1) {
            val inputFile = File(inputFiles[0])
            if (inputFile.isFile) {
                return listOf(inputFile).filterPic()
            }

            if (inputFile.isDirectory) {
                return inputFile.listFiles().toList().filterPic()
            }
        }

        return inputFiles.map { File(it) }.filterPic()
    }

    //写入用户当前使用的文件前缀到缓存文件中
    fun saveUsedFilePrefix(filePrefix: String) = File(FILE_PATH_PREFIX).createFile {
        if (!it.readLines(Charset.defaultCharset()).contains(filePrefix)) {
            it.appendText("$filePrefix\n", Charset.defaultCharset())
        }
    }

    //写入用户当前使用的输入、输出路径到缓存文件中
    fun saveUsedDirs(imageSlimmingModel: ImageSlimmingModel) {

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

    //筛选图片后缀的文件List<File>的扩展函数filterPic
    private fun List<File>.filterPic(): List<File> = this.filter { it.name.endsWith(".png") || it.name.endsWith(".jpg") || it.name.endsWith(".jpeg") }

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
        private var FILE_PATH_API_KEY = "${PathManager.getPreInstalledPluginsPath()}/ImageSlimming/apiKey.csv"
        private var FILE_PATH_INPUT_DIRS = "${PathManager.getPreInstalledPluginsPath()}/ImageSlimming/inputDirs.csv"
        private var FILE_PATH_OUTPUT_DIRS = "${PathManager.getPreInstalledPluginsPath()}/ImageSlimming/outputDirs.csv"
        private var FILE_PATH_PREFIX = "${PathManager.getPreInstalledPluginsPath()}/ImageSlimming/prefix.csv"
    }
}



