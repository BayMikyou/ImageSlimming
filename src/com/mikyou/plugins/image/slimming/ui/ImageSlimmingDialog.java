package com.mikyou.plugins.image.slimming.ui;

import com.mikyou.plugins.image.slimming.extension.ExtGUIDialogKt;
import com.mikyou.plugins.image.slimming.ui.model.ImageSlimmingModel;
import kotlin.Pair;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageSlimmingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> mCBoxInputPath;
    private JButton mBtnInputDir;
    private JComboBox<String> mCBoxOutputPath;
    private JButton mBtnOutputDir;
    private JCheckBox mCbShowPrefix;
    private JLabel mLabelPrefix;
    private JComboBox<String> mCBoxPrefix;
    private JLabel mLabelRename;
    private JTextField mTvRename;


    public ImageSlimmingDialog(Pair<List<String>, List<String>> usedDirs, List<String> filePrefixList, DialogCallback callback) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        mLabelPrefix.setVisible(false);
        mCBoxPrefix.setVisible(false);
        mLabelRename.setVisible(false);
        mTvRename.setVisible(false);


        mCBoxInputPath.setEditable(true);
        mCBoxOutputPath.setEditable(true);
        mCBoxPrefix.setEditable(true);

        mCbShowPrefix.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (mCbShowPrefix.isSelected()) {
                    mLabelPrefix.setVisible(true);
                    mCBoxPrefix.setVisible(true);
                    mLabelRename.setVisible(true);
                    mTvRename.setVisible(true);
                } else {
                    mLabelPrefix.setVisible(false);
                    mCBoxPrefix.setVisible(false);
                    mLabelRename.setVisible(false);
                    mTvRename.setVisible(false);
                }
            }
        });

        //render cbox
        for (String inputDir : usedDirs.getFirst()) {
            mCBoxInputPath.addItem(inputDir.trim());
        }

        for (String outputDir : usedDirs.getSecond()) {
            mCBoxOutputPath.addItem(outputDir.trim());
        }

        for (String filePrefix : filePrefixList) {
            if (mCBoxPrefix != null) {
                mCBoxPrefix.addItem(filePrefix.trim());
            }
        }

        //选择输入文件
        mBtnInputDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileAndSetPath(mCBoxInputPath, JFileChooser.FILES_AND_DIRECTORIES, true);
            }
        });

        //选择输出文件
        mBtnOutputDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileAndSetPath(mCBoxOutputPath, JFileChooser.DIRECTORIES_ONLY, false);
            }
        });

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

    private void openFileAndSetPath(JComboBox<String> cBoxPath, int selectedMode, Boolean isSupportMultiSelect) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(selectedMode);
        fileChooser.setMultiSelectionEnabled(isSupportMultiSelect);
        //设置文件扩展过滤器
        if (selectedMode != JFileChooser.DIRECTORIES_ONLY) {
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".png", "png"));
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".jpg", "jpg"));
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".jpeg", "jpeg"));
        }

        fileChooser.showOpenDialog(null);


        if (selectedMode == JFileChooser.DIRECTORIES_ONLY) {//仅仅选择目录情况，不存在多文件选中
            File selectedDir = fileChooser.getSelectedFile();
            if (selectedDir != null) {
                cBoxPath.insertItemAt(selectedDir.getAbsolutePath(), 0);
                cBoxPath.setSelectedIndex(0);
            }
        } else {//选择含有文件情况，包括仅仅 选择文件 和 同时选择文件和目录，
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles != null && selectedFiles.length > 0) {
                cBoxPath.insertItemAt(getSelectedFilePath(selectedFiles), 0);
                cBoxPath.setSelectedIndex(0);
            }
        }

    }

    private String getSelectedFilePath(File[] selectedFiles) {
        if (selectedFiles.length == 1) {//单个文件或者目录
            return selectedFiles[0].getAbsolutePath();
        }
        //多个文件选中情况,使用逗号分隔将选中多个文件的路径连接为一个长串
        StringBuilder builder = new StringBuilder();
        builder.append(selectedFiles[0]);
        for (int i = 1; i < selectedFiles.length; i++) {
            builder.append(",");
            builder.append(selectedFiles[i].getAbsolutePath());
        }
        return builder.toString();
    }

    private void onOK(DialogCallback callback) {
        String filePrefix = "";
        if (mCBoxPrefix.getSelectedItem() != null) {
            filePrefix = mCBoxPrefix.getSelectedItem().toString();
        }

        if (callback != null && mCBoxInputPath.getSelectedItem() != null && mCBoxOutputPath.getSelectedItem() != null) {
            callback.onOkClicked(new ImageSlimmingModel(mCBoxInputPath.getSelectedItem().toString(), mCBoxOutputPath.getSelectedItem().toString(), filePrefix, mTvRename.getText()));
        }
        dispose();
    }

    private void onCancel(DialogCallback callback) {
        // add your code here if necessary
        if (callback != null) {
            callback.onCancelClicked();
        }
        dispose();
    }

    public interface DialogCallback {
        void onOkClicked(ImageSlimmingModel imageSlimmingModel);

        void onCancelClicked();
    }

    public static void main(String[] args) {
        ImageSlimmingDialog dialog = new ImageSlimmingDialog(new Pair<>(new ArrayList<>(), new ArrayList<>()), new ArrayList<>(), new DialogCallback() {
            @Override
            public void onOkClicked(ImageSlimmingModel compressModel) {
                System.out.println(compressModel.toString());
            }

            @Override
            public void onCancelClicked() {

            }
        });
        ExtGUIDialogKt.showDialog(dialog, 580, 200, true, false);
    }
}
