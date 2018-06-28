package com.mikyou.plugins.image.slimming.ui;

import com.mikyou.plugins.image.slimming.extension.ExtGUIDialogKt;

import javax.swing.*;
import java.awt.event.*;

public class InputKeyDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField mTvTinyPngKey;
    private JLabel mLabelTitle;

    public InputKeyDialog(String labelTitle, DialogCallback callback) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        mLabelTitle.setText(labelTitle);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK(callback);
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel(callback);
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel(callback);
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel(callback);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK(DialogCallback callback) {
        if (callback != null) {
            callback.onOkBtnClicked(mTvTinyPngKey.getText().trim());
        }
        dispose();
    }

    private void onCancel(DialogCallback callback) {
        if (callback != null) {
            callback.onCancelBtnClicked();
        }
        dispose();
    }

    public interface DialogCallback {
        void onOkBtnClicked(String tinyPngKey);

        void onCancelBtnClicked();
    }

    public static void main(String[] args) {
        InputKeyDialog dialog = new InputKeyDialog("请输入TinyPng Key, 请往TinyPng官网申请", new DialogCallback() {
            @Override
            public void onOkBtnClicked(String tinyPngKey) {
                System.out.println(tinyPngKey);
            }

            @Override
            public void onCancelBtnClicked() {

            }
        });
        ExtGUIDialogKt.showDialog(dialog, 530, 170, true, false);
        System.exit(0);
    }
}
