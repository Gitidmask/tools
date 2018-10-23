/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components.treetable;

import com.liuqi.ui.common.components.LButton;
import com.liuqi.ui.common.components.TablePanelInterface;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 抽象树形表格面板
 *
 * @param <T>
 * @author qi.liu
 */
public abstract class AbstractTreeTablePane<T extends Comparable<T>> extends BorderPane implements TablePanelInterface<T> {
    private AbstractTreeTableView<T> tableView;
    private ToolBar toolBar;

    @Override
    public void initView() {
        this.tableView = new AbstractTreeTableView<T>(){

            @Override
            public Paint getRowFill(T t) {
                return AbstractTreeTablePane.this.getRowFill(t);
            }

            @Override
            public void saveRow(T t) {
                AbstractTreeTablePane.this.saveRow(t);
            }
        };

        //工具栏默认添加刷新按钮
        this.toolBar = new ToolBar();
        toolBar.getItems().add(new LButton("刷新", e -> {
            refreshData();
            tableView.getSelectionModel().clearSelection();
        }));

        initToolBar(toolBar);

        this.setTop(toolBar);
        this.setCenter(tableView);
    }

    public AbstractTreeTableView<T> getTableView() {
        return tableView;
    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    /**
     * 初始化工具栏
     * @param toolBar 工具栏
     */
    @SuppressWarnings("WeakerAccess")
    public abstract void initToolBar(ToolBar toolBar);

    @Override
    public void refreshData(List<T> list) {
        if (null != list) {
            // 排序
            this.tableView.getRoot().getChildren().clear();
            Comparator<T> comparator = T::compareTo;

            if (null != getComparator()) {
                comparator = getComparator();
            }

            list.sort(comparator);

            // 对子元素进行排序
            Comparator<T> pComparator = comparator;
            list.forEach(t -> {
                List<T> children = getChildren(t);
                if (null != children && 0 != children.size()) {
                    children.sort(pComparator);
                }
            });

            this.tableView.getRoot().getChildren().addAll(list.stream().map(item -> {
                TreeItem<T> treeItem = new TreeItem(item);
                List<T> children = getChildren(item);
                if (null != children && 0 != children.size()) {
                    children.forEach(child -> {
                        treeItem.getChildren().add(new TreeItem<>(child));
                    });

                    if (null != getNodeExpandFunction()) {
                        treeItem.setExpanded(getNodeExpandFunction().apply(item));
                    } else {
                        treeItem.setExpanded(true);
                    }
                }

                return treeItem;
            }).collect(Collectors.toList()));
        }
    }

    /**
     * 获取默认展开规则
     * 根据条件来决定包含子元素的父级元素是否默认展开
     *
     * @return 展开规则
     */
    public abstract Function<T, Boolean> getNodeExpandFunction();

    /**
     * 获取父级元素的子元素清单
     * @param t 父级元素
     * @return 子元素清单
     */
    public abstract List<T> getChildren(T t);
}
