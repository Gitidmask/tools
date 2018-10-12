/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

/**
 * 文件名称：DefaultExcelServiceImpl.java
 * 作　　者：刘奇
 * 创建日期： 下午7:27:25
 * 版　　本：1.0
 */
package com.liuqi.ui.common.service.def;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.liuqi.ui.common.entity.Entity;
import com.liuqi.ui.common.entity.SelectItem;
import com.liuqi.ui.common.service.ExcelService;
import com.liuqi.ui.common.components.ColumnUserObject;
import com.liuqi.util.ReflectUtil;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的Excel服务类
 * <br>
 * 作　　者：刘奇<br>
 * 创建时间：2017年6月25日<br>
 * 对象版本：V1.0<br>
 *－－－－－－－－－－修改记录－－－－－－－－－－<br>
 * 版　　本：V1.1<br>
 * 修改内容：修改导出时未判断对象值是否为空的BUG<br>
 * 修改　人：刘奇<br>
 * 修改时间：20170625<br>
 *－－－－－－－－－－－－－－－－－－－－－－－－<br>
 */
public class DefaultExcelServiceImpl implements ExcelService {
	private static Logger logger = LoggerFactory.getLogger(DefaultExcelServiceImpl.class);

	@Override
	public <T> void export(List<ColumnUserObject> columns, List<T> items, String fileName) {
		File file = new File(fileName);
		 try {
			WritableWorkbook wb = Workbook.createWorkbook(file);
			WritableSheet sheet = wb.createSheet("结果", 0);
			
			//设置是否显示边框 
			sheet.getSettings().setShowGridLines(true);
			
			int pColumn = 0;  
			int pRow = 0;  
			
			//标题行样式  
			WritableCellFormat titleFormat = new WritableCellFormat();
			titleFormat.setBackground(Colour.GRAY_25);
			titleFormat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
			
			WritableCellFormat contentFormat = new WritableCellFormat();
			contentFormat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);

			 for (ColumnUserObject cObj : columns) {
				 String title = cObj.getTitle();
				 String property = cObj.getProperty();

				//设置列宽
				sheet.setColumnView(pColumn, (int)cObj.getWidth() / 5);

				//处理每一列的标题
				Label label = new Label(pColumn, pRow++, title, titleFormat);
				sheet.addCell(label);

				//处理每一列的数据；
				for (T t: items) {
					//V1.1 对应的值可能为空
					Object objValue = ReflectUtil.getBeanValue(t, property);
					String value = "";
					if (null != objValue) {
						value = objValue.toString();
					}

					sheet.addCell(new Label(pColumn, pRow++, value, contentFormat));
				}

				pRow = 0;
				pColumn++;
			}

			wb.write();
			wb.close();
		} catch (IOException e) {
			logger.error("读取文件失败", e);
		} catch (RowsExceededException e) {
			logger.error("行数超限制", e);
		} catch (WriteException e) {
			logger.error("保存文件失败", e);
		} 
	}

    @Override
    public  List<Entity> importFromFile(String fileName) {
	    File file = new File(fileName);
	    if (!file.exists()) {
	        logger.error("文件不存在，路径：" + fileName);
	        return new ArrayList<>(0);
        }

        try {
            Workbook workbook = Workbook.getWorkbook(file);
            Sheet sheet = workbook.getSheet(0);
            int rows = sheet.getRows();
            List<Entity> entityList = new ArrayList<>(16);
            Cell[] titles = sheet.getRow(0);

            for (int row = 1; row < rows; row++) {
                Cell[] cells = sheet.getRow(row);
                Entity entity = new Entity();
                entityList.add(entity);

                for (int col = 0; col < cells.length; col++) {
                    String title = titles[col].getContents();
                    String value = cells[col].getContents();

                    if (title.equals("step")) {
                        SelectItem item;

                        // 下拉的需要特殊处理
//                        <initial title="待电面"/>
//                        <site title="待现场面试"/>
//                        <pass title="通过"/>
//                        <email title="邮件沟通"/>
//                        <end title="结束"/>
                        switch (value) {
                            case "待电面": item = new SelectItem("initial", "待电面"); break;
                            case "待现场面试": item = new SelectItem("site", "待现场面试"); break;
                            case "通过": item = new SelectItem("site", "pass"); break;
                            case "邮件沟通": item = new SelectItem("site", "email"); break;
                            case "结束": item = new SelectItem("site", "end"); break;
                            default: item = new SelectItem("site", "end"); break;
                        }

                        entity.put(title, item);
                    } else {
                        entity.put(title, value);
                    }
                }
            }

            return entityList;
        } catch (IOException | BiffException e) {
            logger.error("读取Excel文件失败!", e);
        }

        return null;
    }
}
