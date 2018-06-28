package com.mikyou.plugins.image.slimming.extension

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.UIUtil
import java.awt.Dialog
import java.awt.Toolkit
import javax.swing.Icon

fun Dialog.showDialog(width: Int = 550, height: Int = 400, isInCenter: Boolean = true, isResizable: Boolean = false) {
    pack()
    this.isResizable = isResizable
    setSize(width, height)
    if (isInCenter) {
        setLocation(Toolkit.getDefaultToolkit().screenSize.width / 2 - width / 2, Toolkit.getDefaultToolkit().screenSize.height / 2 - height / 2)
    }
    isVisible = true
}

fun Project.showWarnDialog(icon: Icon = UIUtil.getWarningIcon(), title: String, msg: String, positiveText: String = "确定", negativeText: String = "取消", positiveAction: (() -> Unit)? = null, negativeAction: (() -> Unit)? = null) {
    Messages.showDialog(this, msg, title, arrayOf(positiveText, negativeText), 0, icon, object : DialogWrapper.DoNotAskOption.Adapter() {
        override fun rememberChoice(p0: Boolean, p1: Int) {
            if (p1 == 0) {
                positiveAction?.invoke()
            } else if (p1 == 1) {
                negativeAction?.invoke()
            }
        }
    })
}