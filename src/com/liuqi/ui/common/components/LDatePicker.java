/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

import java.time.format.DateTimeFormatter;

/**
 * 日期选择器
 *
 * @author qi.liu
 */
public class LDatePicker extends DatePicker {
    public LDatePicker(String format) {
        setFormat(format);
    }

    public LDatePicker() {
        setFormat("yyyyMMdd");
    }

    public void setFormat(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        StringConverter converter = new LocalDateStringConverter(formatter, formatter) ;
        this.setConverter(converter);
    }

    public String getDateStr() {
        return this.getEditor().getText().trim();
    }
}
