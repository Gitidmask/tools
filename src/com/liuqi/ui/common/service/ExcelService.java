/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

/**
 * 文件名称：ExcelService.java
 * 作　　者：刘奇
 * 创建日期： 下午7:23:01
 * 版　　本：1.0
 */
package com.liuqi.ui.common.service;

import com.liuqi.ui.common.components.ColumnUserObject;
import com.liuqi.ui.common.entity.Entity;

import java.util.List;
import java.util.Map;

/**
 * Excel导出工具服务接口
 * 
 * @author ctx334
 *
 */
public interface ExcelService {
	/**
	 * 将表格中的数据导出成Excel
	 * 
	 * @param fileName 导出的文件名称
     * @param items 导出项
     * @param columns 导出列
	 */
	<T> void export(List<ColumnUserObject> columns, List<T> items, String fileName);

    /**
     * 从文件中加载数据
     *
     * @param fileName 文件名
     * @return 加载的数据
     */
    List<Entity> importFromFile(String fileName);
}
