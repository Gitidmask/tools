/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.apps;

import com.liuqi.ui.common.entity.Entity;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 面板配置
 *
 * @author LiuQI 2018/7/26 13:31
 * @version V1.0
 **/
public class PanelConfig {
    /**
     * 面板标题
     */
    private String title;

    /**
     * 面板表格类型
     */
    private String type;

    /**
     * 面板实体对象配置文件
     */
    private String entityConfigFile;

    /**
     * 面板是否展示
     */
    private boolean display = true;

    /**
     * 面板实体对象存储文件
     */
    private String entityDataFile;

    /**
     * 行背景色生成函数
     */
    private Function<Entity, Color> rowFillFunction;

    /**
     * 保存前进行的一些处理，如设置完成时间等
     */
    private Consumer<Entity> beforeSaveConsumer;

    /**
     * 排序
     */
    private Comparator<Entity> comparator;

    /**
     * 当类型是树型表格时，节点是否展开
     */
    private Function<Entity, Boolean> treeNodeExpandFunction;

    /**
     * 附加的搜索函数
     */
    private List<ComboBox<String>> toolbarComboBoxList;

    public String getTitle() {
        return title;
    }

    public PanelConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getEntityConfigFile() {
        return entityConfigFile;
    }

    public PanelConfig setEntityConfigFile(String entityConfigFile) {
        this.entityConfigFile = entityConfigFile;
        return this;
    }

    public String getEntityDataFile() {
        return entityDataFile;
    }

    public PanelConfig setEntityDataFile(String entityDataFile) {
        this.entityDataFile = entityDataFile;
        return this;
    }

    public Function<Entity, Color> getRowFillFunction() {
        return rowFillFunction;
    }

    public PanelConfig setRowFillFunction(Function<Entity, Color> rowFillFunction) {
        this.rowFillFunction = rowFillFunction;
        return this;
    }

    public Consumer<Entity> getBeforeSaveConsumer() {
        return beforeSaveConsumer;
    }

    public PanelConfig setBeforeSaveConsumer(Consumer<Entity> beforeSaveConsumer) {
        this.beforeSaveConsumer = beforeSaveConsumer;
        return this;
    }

    public Comparator<Entity> getComparator() {
        return comparator;
    }

    public PanelConfig setComparator(Comparator<Entity> comparator) {
        this.comparator = comparator;
        return this;
    }

    public boolean isDisplay() {
        return display;
    }

    public PanelConfig setDisplay(boolean display) {
        this.display = display;
        return this;
    }

    public String getType() {
        return type;
    }

    public PanelConfig setType(String type) {
        this.type = type;
        return this;
    }

    public Function<Entity, Boolean> getTreeNodeExpandFunction() {
        return treeNodeExpandFunction;
    }

    public PanelConfig setTreeNodeExpandFunction(Function<Entity, Boolean> treeNodeExpandFunction) {
        this.treeNodeExpandFunction = treeNodeExpandFunction;
        return this;
    }

    public List<ComboBox<String>> getToolbarComboBoxList() {
        return toolbarComboBoxList;
    }

    public void setToolbarComboBoxList(List<ComboBox<String>> toolbarComboBoxList) {
        this.toolbarComboBoxList = toolbarComboBoxList;
    }

    @Override
    public String toString() {
        return "PanelConfig{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", entityConfigFile='" + entityConfigFile + '\'' +
                ", display=" + display +
                ", entityDataFile='" + entityDataFile + '\'' +
                ", rowFillFunction=" + rowFillFunction +
                ", beforeSaveConsumer=" + beforeSaveConsumer +
                ", comparator=" + comparator +
                ", treeNodeExpandFunction=" + treeNodeExpandFunction +
                '}';
    }
}
