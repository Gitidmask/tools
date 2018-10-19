package com.liuqi.tool.cmd;

import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.WindowEvent;

/**
 * 命令工具面板
 *
 * @author LiuQI 2018/9/29 14:55
 * @version V1.0
 **/
public class LCmdPanel extends BorderPane implements EventHandler<WindowEvent> {
    private TabPane tabPane = new TabPane();

    public LCmdPanel() {
        ToolBar toolBar = new ToolBar();
        this.setTop(toolBar);
        this.setCenter(tabPane);

        this.tabPane.setSide(Side.LEFT);

        // 初始化事件处理
        this.setOnKeyPressed(event -> {
             if (event.isControlDown() && KeyCode.N == event.getCode()) {
                 // 按CTRL+N新建命令窗口
                 newTab();
            }
        });

        Button newButton = new Button("新建窗口");
        toolBar.getItems().add(newButton);
        newButton.setOnAction(e -> newTab());

        // 加载缓存的命令
        HistoryCmdCache historyCmdCache = HistoryCmdCache.getInstance();
        historyCmdCache.getQuerySet().forEach(historyCmd -> {
            SingleCmdPanel singleCmdPanel = newTab();
            singleCmdPanel.getTitleProperty().setValue(historyCmd.getName());
            singleCmdPanel.setCmds(historyCmd.getCmds());
        });
    }

    /**
     * 新建CMD窗口
     */
    private SingleCmdPanel newTab() {
        SingleCmdPanel singleCmdPanel = new SingleCmdPanel();

        Tab tab = new Tab();
        tab.setText("新建窗口");
        tab.setContent(singleCmdPanel);
        singleCmdPanel.getTitleProperty().addListener((observable, oldValue, newValue) -> {
            tab.setText(newValue);
            updateTitleStyle(tab);
        });

        // Tab切换时，更新标签显示
        tab.setOnSelectionChanged(event -> updateTitleStyle(tab));

        tabPane.getTabs().add(tab);

        return singleCmdPanel;
    }

    /**
     * 更新标签风格
     */
    private void updateTitleStyle(Tab tab) {
        if (tab.getText().endsWith("(*)")) {
            tab.setStyle("-fx-background-color: #FF7F27; ");
        } else {
            if (tab.isSelected()) {
                tab.setStyle("-fx-background-color: #ffffff; ");
            } else {
                tab.setStyle("-fx-background-color: #efefef; ");
            }
        }
    }

    @Override
    public void handle(WindowEvent event) {
        this.tabPane.getTabs().forEach(tab -> {
            SingleCmdPanel singleCmdPanel = (SingleCmdPanel) tab.getContent();
            singleCmdPanel.handle(event);
        });
    }
}
