/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components.table;

import com.liuqi.ui.common.components.LButton;
import javafx.scene.control.ToolBar;
import javafx.scene.paint.Paint;

import java.util.Comparator;
import java.util.List;

/**
 * 工具栏表格面板接口
 *
 * @author qi.liu
 * @param <T>
 */
public interface ToolbarTablePaneInterface<T extends Comparable<T>> {
    /**
     * 初始化
     */
    default void init() {
        this.beforeConstruct();

        this.initView();
        initToolBar();

        afterInstantiation();

        initColumns();

        afterConstruct();

        this.refreshData();
    }

    /**
     * 刷新表格数据
     */
    default void refreshData() {
        refreshData(getItems());
    }

    /**
     * 使用指定的数据刷新表格
     */
    void refreshData(List<T> list);

    /**
     * 初始化界面
     * 属性赋值等操作均需要在此完成
     */
    void initView();

    /**
     * 属性赋值后执行的方法
     */
    default void afterInstantiation() {}

    /**
     * 初始化完后执行的函数
     */
    default void afterConstruct() {}

    /**
     * 初始化之前执行的函数
     */
    default void beforeConstruct() {}

    /**
     * 初始化表格列
     */
    default void initColumns() {}

    /**
     * 初始化工具栏
     */
    void initToolBar();

    /**
     * 保存列
     */
    void saveRow(T t);

    /**
     * 根据某行对象动态获取该行的字体颜色
     */
    Paint getRowFill(T t);

    /**
     * 获取表格展示的数据清单
     */
    List<T> getItems();

    /**
     * 获取表格数据排序器
     */
    default Comparator<T> getComparator() {
        return null;
    }
}
