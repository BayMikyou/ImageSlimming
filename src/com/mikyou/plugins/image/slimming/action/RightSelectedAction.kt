package com.mikyou.plugins.image.slimming.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.mikyou.plugins.image.slimming.extension.showDialog
import com.mikyou.plugins.image.slimming.helper.FileIOHelper
import com.mikyou.plugins.image.slimming.helper.ImageSlimmingHelper
import com.mikyou.plugins.image.slimming.ui.InputKeyDialog
import java.io.File

class RightSelectedAction : AnAction() {
    private val mSlimmingHelper: ImageSlimmingHelper = ImageSlimmingHelper()
    private val mIOHelper: FileIOHelper = FileIOHelper()
    //插件入口函数
    override fun actionPerformed(event: AnActionEvent?) {
        //检查本地存在ApiKey文件是否存在
        mIOHelper.checkApiKeyFile(inexistAction = { labelTitle ->
            popupInputKeyDialog(labelTitle = labelTitle, event = event)//不存在，提示用户输入
        }, existAction = { apiKey ->
            //存在直接设置ApiKey，并弹出压缩图片对话框
            mSlimmingHelper.setTinyPngApiKey(apiKey)
            executeSlimImage(event)
        })
    }

    private fun popupInputKeyDialog(labelTitle: String, event: AnActionEvent?) {
        InputKeyDialog(labelTitle, object : InputKeyDialog.DialogCallback {
            override fun onOkBtnClicked(tinyPngKey: String) = mSlimmingHelper.checkApiKeyValid(project = getEventProject(event), apiKey = tinyPngKey, validAction = {
                mIOHelper.updateExpireKey(apiKey = tinyPngKey)
                executeSlimImage(event)
            }, invalidAction = {
                popupInputKeyDialog(labelTitle = it, event = event)
            })

            override fun onCancelBtnClicked() {

            }
        }).showDialog(width = 530, height = 150, isInCenter = true, isResizable = false)
    }

    private fun executeSlimImage(event: AnActionEvent?) {
        val startTime = System.currentTimeMillis()
        mSlimmingHelper.slimImage(project = getEventProject(event), inputFiles = getValidImageFiles(event?.dataContext?.getSelectedFiles()), outputSameFile = true, successAction = {
            Messages.showWarningDialog("压缩完成, 已压缩: ${getValidImageFiles(event?.dataContext?.getSelectedFiles()).size}张图片, 压缩总时长共计: ${(System.currentTimeMillis() - startTime) / 1000}s", "来自ImageSlimming提示")
        }, failAction = {
            popupInputKeyDialog(labelTitle = it, event = event)
        })
    }

    private fun getValidImageFiles(selectedFiles: Array<VirtualFile>?): List<File> {
        if (selectedFiles == null || selectedFiles.isEmpty()) return listOf()
        return selectedFiles.asSequence().filter { it.extension == POSTFIX_JPEG || it.extension == POSTFIX_JPG || it.extension == POSTFIX_PNG }.map { File(it.path) }.toList()
    }

    private fun checkSelectedFilesExtension(selectedFiles: Array<VirtualFile>?): Boolean {
        if (selectedFiles == null || selectedFiles.isEmpty()) return false
        return selectedFiles.any { it.extension == POSTFIX_JPEG || it.extension == POSTFIX_JPG || it.extension == POSTFIX_PNG }//只要选中的文件中含有一张或多张图片就会显示
    }


    private fun DataContext.getSelectedFiles(): Array<VirtualFile>? = DataKeys.VIRTUAL_FILE_ARRAY.getData(this)//右键获取选中多个文件,扩展函数

    companion object {
        private const val POSTFIX_PNG = "png"
        private const val POSTFIX_JPG = "jpg"
        private const val POSTFIX_JPEG = "jpeg"
    }
}