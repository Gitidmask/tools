/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components.table;/**
 * Created by icaru on 2017/8/17.
 */

import com.liuqi.ui.common.components.LButton;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;

import java.util.Comparator;
import java.util.List;

/**
 * 工具栏表格面板抽象类
 *
 * @author qi.liu
 * @param <T>
 */
public abstract class AbstractLToolBarTablePane<T extends Comparable<T>> extends BorderPane implements ToolbarTablePaneInterface<T> {
    ToolBar toolBar;
    AbstractLTableView<T> tableView;
    private ToolBar statusBar;

    @Override
    public void initView() {
        this.toolBar = new ToolBar();
        this.statusBar = new ToolBar();
        this.tableView = new AbstractLTableView<T>(){

            @Override
            public Paint getRowFill(T t) {
                return AbstractLToolBarTablePane.this.getRowFill(t);
            }

            @Override
            public void saveRow(T t) {
                AbstractLToolBarTablePane.this.saveRow(t);
            }
        };

        //工具栏默认添加刷新按钮
        this.toolBar.getItems().add(new LButton("刷新", e -> refreshData()));

        initStatusBar();

        this.setTop(toolBar);
        this.setCenter(tableView);
        this.setBottom(statusBar);
    }

    /**
     * 初始化状态栏
     */
    private void initStatusBar() {
        Label totalCountLabel = new Label();
        Label selCountLabel = new Label();
        this.statusBar.getItems().addAll(new Label("总记录："), totalCountLabel,
                new Label("，选择记录："), selCountLabel);

        this.tableView.getItems().addListener((ListChangeListener<? super T>) event -> {
            totalCountLabel.setText(String.valueOf(event.getList().size()));
        });

        this.tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super T>) event -> {
            selCountLabel.setText(String.valueOf(event.getList().size()));
        });
    }

    @Override
    public void refreshData(List<T> list) {
        if (null != list) {
            this.tableView.getItems().clear();
            Comparator<T> comparator = getComparator();
            if (null == comparator) {
                list.sort(T::compareTo);
            } else {
                list.sort(comparator);
            }
            this.tableView.getItems().addAll(list);
        }
    }
}
