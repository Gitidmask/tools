/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.entity;

import java.io.Serializable;

/**
 * .
 *
 * @author LiuQI 2018/7/11 10:56
 * @version V1.0
 **/
public class SelectItem implements Serializable {
    private String key;
    private String value;

    public SelectItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public SelectItem setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public SelectItem setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return value;
    }
}
