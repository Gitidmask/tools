/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.tool;

import com.liuqi.tool.cmd.LCmdPanel;
import com.liuqi.tool.httptool.HttpToolMainPane;
import com.liuqi.ui.common.apps.AppConfig;
import com.liuqi.ui.common.apps.PanelConfig;
import com.liuqi.ui.common.components.table.ConfigEntityTablePanel;
import com.liuqi.ui.common.components.table.ConfigEntityTreeTablePanel;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 工具应用入口
 *
 * @author qi.liu
 */
public class MainApplication extends Application {
    private static final String TREE_TABLE = "treeTable";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        TabPane tabPane = new TabPane();

        // 加载应用配置文件(conf/config.xml文件所配置内容)并添加所配置的面板
        AppConfig appConfig = new AppConfig();
        List<PanelConfig> panelConfigList = appConfig.getPanelConfigList();
        if (null != panelConfigList) {
            panelConfigList.forEach(panelConfig -> {
                if (!panelConfig.isDisplay()) {
                    return;
                }

                String title = panelConfig.getTitle();
                String type = panelConfig.getType();
                Pane pane;

                if (!TREE_TABLE.equals(type)) {
                    pane = ConfigEntityTablePanel.getInstance(panelConfig);
                } else {
                    pane = ConfigEntityTreeTablePanel.getInstance(panelConfig);
                }

                tabPane.getTabs().add(createTab(title, pane));
            });
        }

        // 添加Http调用工具Tab页
        HttpToolMainPane borderPane = new HttpToolMainPane();
        tabPane.getTabs().add(createTab("Http调用", borderPane));

        // 添加命令工具Tab页
        LCmdPanel lCmdPanel = new LCmdPanel();
        tabPane.getTabs().add(createTab("CMD", lCmdPanel));

        // 面板关闭时退出后台线程
        primaryStage.setOnCloseRequest(event -> {
            borderPane.handle(event);
            lCmdPanel.handle(event);
            System.exit(0);
        });

        primaryStage.setScene(new Scene(tabPane));
        primaryStage.setTitle("应用工具");
        primaryStage.setMaximized(true);
        Path path = Paths.get("icons/mouse.png");
        primaryStage.getIcons().add(new Image(new FileInputStream(path.toFile())));

        primaryStage.show();
    }

    /**
     * 创建Tab页
     */
    private Tab createTab(String title, Node node) {
        Tab tab = new Tab(title, node);
        tab.setClosable(false);
        return tab;
    }
}
