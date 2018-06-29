package com.mikyou.plugins.image.slimming.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.mikyou.plugins.image.slimming.extension.showDialog
import com.mikyou.plugins.image.slimming.helper.FileIOHelper
import com.mikyou.plugins.image.slimming.helper.ImageSlimmingHelper
import com.mikyou.plugins.image.slimming.ui.ImageSlimmingDialog
import com.mikyou.plugins.image.slimming.ui.InputKeyDialog
import com.mikyou.plugins.image.slimming.ui.model.ImageSlimmingModel


class ImageSlimmingAction : AnAction() {
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
            popupCompressDialog(event)
        })
    }

    //弹出输入apiKey dialog
    private fun popupInputKeyDialog(labelTitle: String, event: AnActionEvent?) {
        InputKeyDialog(labelTitle, object : InputKeyDialog.DialogCallback {
            override fun onOkBtnClicked(tinyPngKey: String) = mSlimmingHelper.checkApiKeyValid(project = getEventProject(event), apiKey = tinyPngKey, validAction = {
                mIOHelper.updateExpireKey(apiKey = tinyPngKey)
                popupCompressDialog(event)
            }, invalidAction = {
                popupInputKeyDialog(labelTitle = it, event = event)
            })

            override fun onCancelBtnClicked() {

            }
        }).showDialog(width = 530, height = 150, isInCenter = true, isResizable = false)
    }

    //弹出压缩目录选择 dialog
    private fun popupCompressDialog(event: AnActionEvent?) {
        ImageSlimmingDialog(mIOHelper.readUsedDirs(), mIOHelper.readUsedFilePrefix(), object : ImageSlimmingDialog.DialogCallback {
            override fun onOkClicked(imageSlimmingModel: ImageSlimmingModel) {
                mIOHelper.saveUsedDirs(imageSlimmingModel)
                mIOHelper.saveUsedFilePrefix(imageSlimmingModel.filePrefix)
                val inputFiles = mIOHelper.readInputDirFiles(imageSlimmingModel.inputDir)
                val startTime = System.currentTimeMillis()
                mSlimmingHelper.slimImage(project = getEventProject(event), inputFiles = inputFiles, model = imageSlimmingModel, successAction = {
                    Messages.showWarningDialog("压缩完成, 已压缩: ${inputFiles.size}张图片, 压缩总时长共计: ${(System.currentTimeMillis() - startTime) / 1000}s", "来自ImageSlimming提示")
                }, failAction = {
                    popupInputKeyDialog(labelTitle = it, event = event)
                })
            }

            override fun onCancelClicked() {

            }

        }).showDialog(width = 530, height = 200, isInCenter = true, isResizable = false)
    }
}