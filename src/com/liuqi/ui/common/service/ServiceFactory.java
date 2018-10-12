/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

/**
 * 文   件  名：ServiceFactory.java
 * 作          者：刘奇
 * 创建日期：2014-7-26
 * 版          本：1.0
 */
package com.liuqi.ui.common.service;

import com.liuqi.ui.common.service.def.DefaultConfigService;
import com.liuqi.ui.common.service.def.DefaultExcelServiceImpl;

/**
 * 
 * @作者： 刘奇
 * @时间：2014-7-26
 *
 */
public class ServiceFactory {
	private static ConfigService configService;
	private static ExcelService excelService;
	
	public static ConfigService getConfigService(DefaultConfigService.ConfigInitCallable callable) {
		if (null == configService) {
			configService = new DefaultConfigService(callable);
		}
		
		return configService; 
	}

	public static ExcelService getExcelService() {
		if (null == excelService) {
			excelService = new DefaultExcelServiceImpl();
		}

		return excelService;
	}
	
}
