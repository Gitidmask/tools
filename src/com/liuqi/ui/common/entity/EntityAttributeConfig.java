/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.entity;

import com.liuqi.util.DateUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 实体属性配置
 *
 * @author LiuQI 2018/7/5 11:02
 * @version V1.0
 **/
public class EntityAttributeConfig {
    private String key;
    private EntityAttributeType type;
    private String title;
    private String defaultValue;
    private int width;
    private List<SelectItem> selectItems = new ArrayList<>(10);

    public EntityAttributeConfig() {

    }

    public EntityAttributeType getType() {
        return type;
    }

    public EntityAttributeConfig setType(EntityAttributeType type) {
        this.type = type;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public EntityAttributeConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public Object getDefaultValue() {
        switch (type) {
            case DATE: {
                if (null == defaultValue) {
                    return null;
                }

                switch (defaultValue) {
                    case "nowDate":
                        return DateUtils.getNowDateStr();
                    default:
                        break;
                }
                break;
            }
            case DATETIME: {
                if (null == defaultValue) {
                    return null;
                }

                switch (defaultValue) {
                    case "now": return DateUtils.getNowTimeStr();
                    default:  break;
                }

                break;
            }
            case SELECT: {
                if (null == defaultValue || null == this.selectItems) {
                    return null;
                }

                for (SelectItem item: this.selectItems) {
                    if (item.getKey().equals(defaultValue)) {
                        return item;
                    }
                }

                break;
            }
            default: break;
        }

        return defaultValue;
    }

    public EntityAttributeConfig setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getKey() {
        return key;
    }

    public EntityAttributeConfig setKey(String key) {
        this.key = key;
        return this;
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    public EntityAttributeConfig addSelectItem(String key, String value) {
        this.selectItems.add(new SelectItem(key, value));
        return this;
    }

    public int getWidth() {
        return width;
    }

    public EntityAttributeConfig setWidth(int width) {
        this.width = width;
        return this;
    }

    @Override
    public String toString() {
        return "EntityAttributeConfig{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", width=" + width +
                ", selectItems=" + selectItems +
                '}';
    }
}
