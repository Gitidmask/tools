package com.liuqi.tool.cmd;

import com.liuqi.tool.Constants;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Dialog;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebView;
import javafx.stage.WindowEvent;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * 单个命令工具面板
 *
 * @author LiuQI 2018/9/29 14:55
 * @version V1.0
 **/
public class SingleCmdPanel extends SplitPane implements EventHandler<WindowEvent> {
    private static final String CMD_TITLE_NEW_WIN = "新建窗口";
    private TextArea inputArea = new TextArea();
    private WebView logArea = new WebView();
    private final StringBuilder logString = new StringBuilder();
    private Process process;
    private StringProperty titleProperty = new SimpleStringProperty(CMD_TITLE_NEW_WIN);
    private Dialog<String> dialog = new TextInputDialog();

    private String lastLine = null;

    private HistoryCmdCache historyCmdCache = HistoryCmdCache.getInstance();
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * 输入是否变化；
     * 初始化会加载缓存的命令，此时标题后不应该加(*)表示已修改；
     */
    private boolean isInputChange = false;

    SingleCmdPanel() {
        try {
            this.process = Runtime.getRuntime().exec("cmd");
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setOrientation(Orientation.HORIZONTAL);
        this.getItems().addAll(inputArea, logArea);
        this.setDividerPositions(0.4);

        // 先CD到d:/projects目录
        if (null != process) {
            OutputStream outputStream = process.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println("d:");
            writer.println("cd d:/projects/");
            writer.flush();
        }

        inputArea.setText("d:\rcd d:/projects/\r");

        inputArea.setOnKeyPressed(event -> {
            if (event.isControlDown() && KeyCode.ENTER == event.getCode()) {
                // CTRL+ENTER执行命令
                processCmds();
            } else if (event.isControlDown() && KeyCode.S == event.getCode()) {
                // CTRL+S时保存当前界面
                String title = titleProperty.getValue();
                if (titleProperty.getValue().equals(CMD_TITLE_NEW_WIN)) {
                    Optional<String> optional = dialog.showAndWait();

                    if (optional.isPresent()) {
                        title = optional.get();
                    }
                } else if (title.endsWith(Constants.CMD_UNSAVED_TITLE_SUFFIX)) {
                    title = title.substring(0, title.length() - 3);
                }

                titleProperty.setValue(title);

                HistoryCmd historyCmd = new HistoryCmd();
                historyCmd.setName(title);
                historyCmd.setCmds(inputArea.getText().trim());
                historyCmdCache.add(historyCmd);
            }
        });

        // 添加CMD进程的内容读取线程，将其输出到输出内容域中
        if (null != process) {
            msgToOutput(process.getInputStream(), false);
            msgToOutput(process.getErrorStream(), true);
        }

        // 当内容变化时，标题后面添加(*)表示未保存
        inputArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isInputChange) {
                isInputChange = true;
                return;
            }

            String title = titleProperty.getValue();
            if (!CMD_TITLE_NEW_WIN.equals(title) && !title.endsWith(Constants.CMD_UNSAVED_TITLE_SUFFIX)) {
                titleProperty.setValue(titleProperty.getValue() + Constants.CMD_UNSAVED_TITLE_SUFFIX);
            }
        });
    }

    StringProperty getTitleProperty() {
        return this.titleProperty;
    }

    void setCmds(String cmds) {
        this.inputArea.setText(cmds);
    }

    private void msgToOutput(InputStream inputStream2, boolean isError) {
        // 使用线程接收cmd输出的数据并将其输出在相应控件中
        executorService.submit(() -> {
            BufferedReader normalReader = new BufferedReader(new InputStreamReader(inputStream2, Charset.forName("gbk")));
            BufferedReader gitReader = new BufferedReader(new InputStreamReader(inputStream2, Charset.forName("utf-8")));

            BufferedReader reader = normalReader;

            String line;
            try {
                while (null != (line = reader.readLine())) {
                    if ("".equals(line.trim())) {
                        continue;
                    }

                    // 替换文本中的<DIR>
                    String color = "black";

                    // 在更新logString时需要加锁，因为有两个线程在更新这个值，如果不加锁会导致修改的值混乱；
                    synchronized (logString) {
                        if (":\\".equals(line.substring(1, 3))) {
                            // 如果当前行是打印的命令，那么在前面增加空行，并且使用蓝色显示
                            logString.append("<br/>");
                            color = "#117ED1";

                            // 如果当前行是命令行，并且是通过GITREADER读取的，那么需要使用GBK方式来解析读取的行
                            if (reader.equals(gitReader)) {
                                line = new String(line.getBytes("gbk"), "gbk");
                            }
                        } else {
                            if (isError) {
                                // 如果是异常信息，则标红
                                color = "#AC0606";
                            }
                        }

                        logString.append("<span style='color: ")
                                .append(color)
                                .append("; font-size: 13px; font-family: Consolas; '>").append(line).append("</span><br/>");

                        StringBuilder scrollHtml = scrollWebView();
                        Platform.runLater(() -> logArea.getEngine().loadContent(scrollHtml + logString.toString()));
                    }

                    // 根据上一行是否是GIT命令来决定接下来的读取编码方式 ；
                    // 不能直接使用line是因为命令后面可能会跟正常的或者是异常的消息；如果直接使用line，那么异常信息将无法进入这个逻辑进行处理
                    if (null != lastLine && ":\\".equals(lastLine.substring(1, 3))) {
                        // git命令返回的编码是utf-8的，需要特殊处理
                        if (lastLine.contains(">git ")) {
                            reader = gitReader;
                        } else {
                            reader = normalReader;
                        }
                    }

                    this.lastLine = line;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * 获取内容增加时滚动到底部的HTML内容
     */
    private static StringBuilder scrollWebView() {
        StringBuilder script = new StringBuilder().append("<html>");
        script.append("<head>");
        script.append("   <script language=\"javascript\" type=\"text/javascript\">");
        script.append("       function toBottom(){");
        script.append("           var height = document.body.scrollHeight; ");
        script.append("           window.scrollTo(0, height);");
        script.append("       }");
        script.append("   </script>");
        script.append("</head>");
        script.append("<body onload='toBottom()'>");
        return script;
    }

    /**
     * 批量执行命令
     */
    private void processCmds() {
        if (null == process) {
            System.err.println("创建CMD进程失败！");
            return;
        }

        // 如果有选择文本，则执行选择的文本；否则 ，执行所有文本
        String cmds = inputArea.getSelectedText();
        if (null == cmds || "".equals(cmds.trim())) {
            cmds = inputArea.getText();
        }
        if ("".equals(cmds.trim())) {
            return;
        }

        String[] lines = cmds.split("\n");

        OutputStream outputStream = process.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        for (String line : lines) {
            writer.println(line);
        }

        writer.flush();
    }

    @Override
    public void handle(WindowEvent event) {
        if (null != process) {
            process.destroy();
        }
    }
}
