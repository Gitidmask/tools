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
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @Author icaru
 * @Date 2017/8/17 18:07
 * @Version V1.0
 * --------------Modify Logs------------------
 * @Version V1.*
 * @Comments <p></p>
 * @Author icaru
 * @Date 2017/8/17 18:07
 **/
public abstract class LToolBarTreeTablePane<T extends Comparable<T>> extends BorderPane implements ToolbarTablePaneInterface<T> {
    protected ToolBar toolBar;
    protected AbstractLTreeTableView<T> tableView;

    public LToolBarTreeTablePane() {
        this.beforeConstruct();

        this.toolBar = new ToolBar();
        this.tableView = new AbstractLTreeTableView<T>(){

            @Override
            public Paint getRowFill(T t) {
                return LToolBarTreeTablePane.this.getRowFill(t);
            }

            @Override
            public void saveRow(T t) {
                LToolBarTreeTablePane.this.saveRow(t);
            }
        };

        initColumns();

        //工具栏默认添加刷新按钮
        this.toolBar.getItems().add(new LButton("刷新", e -> {
            refreshData();
            tableView.getSelectionModel().clearSelection();
        }));

        initToolBar();

        this.setTop(toolBar);
        this.setCenter(tableView);

        afterConstruct();
    }

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

    public abstract Function<T, Boolean> getNodeExpandFunction();

    public abstract List<T> getChildren(T t);
}
