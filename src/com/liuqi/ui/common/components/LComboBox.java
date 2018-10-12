/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components;

import javafx.scene.control.ComboBox;

import java.util.List;

/**
 * 下拉框
 *
 * @author qi.liu
 * @param <T>
 */
public class LComboBox<T> extends ComboBox<T>{
    public LComboBox(List<T> list) {
        this.getItems().add(null);
        this.getItems().addAll(list);

        this.getSelectionModel().select(1);
    }

    public LComboBox(T...args) {
        this.getItems().add(null);
        this.getItems().addAll(args);

        this.getSelectionModel().select(1);
    }

    /**
     * 设置选项值
     */
    public void setItems(List<T> list) {
        this.getItems().addAll(list);
    }

    /**
     * 设置被 选中的对象
     * @param t
     */
    public void select(T t) {
        this.getSelectionModel().select(t);
    }

    /**
     * 设置被 选中的对象
     * @param idx
     */
    public void select(int idx) {
        this.getSelectionModel().select(idx);
    }

    /**
     * 返回选择的对象
     * @return
     */
    public T getSelectedItem() {
        return this.getSelectionModel().getSelectedItem();
    }

    /**
     * 返回选择的对象转换成字符串
     * 如果选择对象为空，返回""
     * @return
     */
    public String getSelectedItemStr() {
        if (getSelectedItem() == null) {
            return "";
        } else {
            return getSelectedItem().toString();
        }
    }
}
