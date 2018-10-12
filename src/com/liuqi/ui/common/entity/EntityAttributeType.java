/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.entity;

/**
 * .
 *
 * @author LiuQI 2018/7/10 9:22
 * @version V1.0
 **/
public enum EntityAttributeType {
    ID,
    TEXT,
    CHECKBOX,
    SELECT,
    DATE,
    DATETIME;

    public static EntityAttributeType parse(String text) {
        switch (text) {
            case "id": return ID;
            case "text": return TEXT;
            case "checkbox": return CHECKBOX;
            case "select": return SELECT;
            case "date": return DATE;
            case "datetime": return DATETIME;
            default: return ID;
        }
    }

    @Override
    public String toString() {
        return this.name();
    }
}
