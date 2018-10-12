package com.liuqi.tool.httptool;

import com.alibaba.fastjson.JSONObject;
import com.liuqi.util.JsonFormater;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;

/**
 * 状态栏
 *
 * @author LiuQI 2018/6/1 15:05
 * @version V1.0
 **/
class StatusBar extends ToolBar {
    private Label spentTimeLabel = new Label();
    private Label dataSizeLabel = new Label();
    private ProgressBar progressBar = new ProgressBar();
    private Button copyButton = new Button("复制结果");
    private Button copyOriginButton = new Button("复制原始结果");
    private Label messageLabel = new Label();

    private String result;

    StatusBar() {
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getItems().addAll(new Label("请求耗时："), spentTimeLabel,
                new Label("      返回数据大小："), dataSizeLabel, spacer, messageLabel, progressBar, copyButton, copyOriginButton);

        String infoStyle = "-fx-text-fill: blue; ";
        spentTimeLabel.setStyle(infoStyle);
        dataSizeLabel.setStyle(infoStyle);

        progressBar.setVisible(false);
        copyButton.setVisible(false);
        copyOriginButton.setVisible(false);

        messageLabel.setStyle("-fx-text-fill: red;");

        copyButton.setOnAction(e -> {
            if (null == this.result) {
                return;
            }

            ClipboardContent content = new ClipboardContent();
            content.putString(JsonFormater.format(JSONObject.parseObject(this.result)));
            Clipboard.getSystemClipboard().setContent(content);
        });

        copyOriginButton.setOnAction(e -> {
            if (null == this.result) {
                return;
            }

            ClipboardContent content = new ClipboardContent();
            content.putString(this.result);
            Clipboard.getSystemClipboard().setContent(content);
        });
    }

    void showStatusBar() {
        this.progressBar.setVisible(true);
    }

    void hideStatusBar() {
        this.progressBar.setVisible(false);
    }

    void showErrorMessage(String message) {
        this.messageLabel.setStyle("-fx-text-fill: red;");
        this.messageLabel.setText(message);
    }

    void showSuccessMessage(String message) {
        this.messageLabel.setStyle("-fx-text-fill: green;");
        this.messageLabel.setText(message);
    }

    void setInfo(long spentTime, String data) {
        this.result = data;

        spentTimeLabel.setText(String.valueOf(spentTime) + "ms");

        String dataSizeStr = "";
        long dataSize = data.length() * 2;
        if (1000 >= dataSize) {
            dataSizeStr = dataSize + "B";
        } else if (1000000 >= dataSize) {
            dataSizeStr = dataSize / 1000 + "KB";
        } else {
            dataSizeStr = dataSize / 1000000 + "MB";
        }

        dataSizeLabel.setText(dataSizeStr);

        copyButton.setVisible(true);
        copyOriginButton.setVisible(true);
    }
}
