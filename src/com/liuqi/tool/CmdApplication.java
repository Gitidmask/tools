package com.liuqi.tool;

import com.liuqi.tool.cmd.LCmdPanel;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * .
 *
 * @author LiuQI 2018/10/19 16:28
 * @version V1.0
 **/
public class CmdApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        LCmdPanel lCmdPanel = new LCmdPanel();

        // 面板关闭时退出后台线程
        primaryStage.setOnCloseRequest(event -> {
            lCmdPanel.handle(event);
            System.exit(0);
        });

        lCmdPanel.setTabSide(Side.TOP);
        lCmdPanel.setTabClosable(false);
        lCmdPanel.hideToolBar();

        primaryStage.setScene(new Scene(lCmdPanel));
        primaryStage.setTitle("命令行工具");
        primaryStage.setMaximized(true);
        Path path = Paths.get("icons/mouse1.png");
        primaryStage.getIcons().add(new Image(new FileInputStream(path.toFile())));

        primaryStage.show();
    }
}
