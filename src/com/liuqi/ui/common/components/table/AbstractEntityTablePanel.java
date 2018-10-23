/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components.table;

import com.liuqi.ui.common.components.ExcelExportDialogProxy;
import com.liuqi.ui.common.entity.Entity;
import com.liuqi.ui.common.entity.EntityAttributeConfig;
import com.liuqi.ui.common.entity.EntityAttributeType;
import com.liuqi.ui.common.entity.EntityConfig;
import com.liuqi.ui.common.service.EntityService;
import com.liuqi.ui.common.service.ExcelService;
import com.liuqi.ui.common.service.def.FileEntityService;
import com.liuqi.ui.common.service.ServiceFactory;
import com.liuqi.util.DateUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 实体管理表格面板
 *
 * @author LiuQI 2018/7/10 11:24
 * @version V1.0
 **/
public abstract class AbstractEntityTablePanel extends AbstractTablePane<Entity> {
    private EntityConfig entityConfig;
    private EntityService entityService;

    private List<Entity> dataList;
    private String configFileName;

    public void initWithConfigFile(String configFile) {
        this.configFileName = configFile;
        this.entityConfig = new EntityConfig("conf/" + configFile + ".xml");
        this.entityService = entityConfig.getEntityService();

        if (null == entityService) {
            try {
                this.entityService = new FileEntityService("data/" + configFile + ".txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        init();
    }

    @Override
    public void afterConstruct() {
        // 可以选择单元格
        this.getTableView().getSelectionModel().cellSelectionEnabledProperty().setValue(true);

        // 可以多选
        this.getTableView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 按ctrl + c时复制当前单元格
        setCopyAction();
    }

    /**
     * 按ctrl+C时将所选择的内容复制到系统粘贴板
     */
    private void setCopyAction() {
        this.getTableView().setOnKeyPressed(event -> {
           if (event.getCode() == KeyCode.C && event.isControlDown()) {
               ObservableList<TablePosition> selectedCells = this.getTableView().getSelectionModel().getSelectedCells();
               if (null != selectedCells && 0 != selectedCells.size()) {
                   TablePosition position = selectedCells.get(0);
                   int row = position.getRow();

                   Object value = position.getTableColumn().getCellObservableValue(row).getValue();

                    if (null != value) {
                        Clipboard.getSystemClipboard().clear();
                        Map<DataFormat, Object> map = new HashMap<>(2);
                        map.put(DataFormat.PLAIN_TEXT, value.toString());
                        Clipboard.getSystemClipboard().setContent(map);
                    }
               }
           }
        });
    }

    /**
     * 根据配置文件加载表格配置并为表格添加列
     */
    @Override
    public void initColumns() {
        Collection<EntityAttributeConfig> attributeConfigs = entityConfig.getAttributeList();
        if (0 != attributeConfigs.size()) {
            attributeConfigs.forEach(attributeConfig -> {
                EntityAttributeType type = attributeConfig.getType();
                switch (type) {
                    case SELECT: {
                        if (null != attributeConfig.getSelectItems()) {
                            getTableView().addComboBoxColumn(attributeConfig.getTitle(), attributeConfig.getKey(),
                                    attributeConfig.getSelectItems());
                        } else {
                            getTableView().addColumn(attributeConfig.getTitle(), attributeConfig.getKey());
                        }

                        break;
                    }
                    case CHECKBOX: {
                        getTableView().addCheckBoxColumn(attributeConfig.getTitle(), attributeConfig.getKey());
                        break;
                    }
                    default: {
                        if (0 != attributeConfig.getWidth()) {
                            getTableView().addColumn(attributeConfig.getTitle(), attributeConfig.getKey(), attributeConfig.getWidth());
                        } else {
                            getTableView().addColumn(attributeConfig.getTitle(), attributeConfig.getKey());
                        }
                    }
                }
            });
        }
    }

    @Override
    public void initToolBar(ToolBar toolBar) {
        initToolbarQuickAddFunction();

        initToolbarSearchField();

        // 添加导出按钮
        Button exportButton = new Button("导出");
        exportButton.setOnAction(e -> ExcelExportDialogProxy.INSTANCE.saveFile(getTableView(), configFileName + ".xls"));
        toolBar.getItems().add(exportButton);

        // 添加导入按钮
        initImportButton();
    }

    /**
     * 初始化通过工具样输入框快速添加控制及其事件
     */
    private void initToolbarQuickAddFunction() {
        TextField textField = new TextField();
        textField.setPromptText("添加新记录...");
        this.getToolBar().getItems().add(textField);

        textField.setOnKeyReleased(event -> {
            if (KeyCode.ENTER == event.getCode()) {
                Entity entity = new Entity();

                // 设置新增时，textField中的值保存到哪个属性中
                String quickAddKey = entityConfig.getQuickAdd();
                EntityAttributeConfig attributeConfig = entityConfig.getAttributeConfigs().get(quickAddKey);
                if (null == attributeConfig) {
                    System.out.println("QuickAdd属性无效，属性：" + quickAddKey);
                } else {
                    entity.put(quickAddKey, textField.getText().trim());
                }

                // 设置默认值
                entityConfig.getAttributeConfigs().values().forEach(pConfig -> {
                    String key = pConfig.getKey();
                    Object defaultValue = pConfig.getDefaultValue();
                    if (null != defaultValue) {
                        entity.put(key, defaultValue);
                    }
                });

                // 保存对象
                saveRow(entity);

                // 清空快捷添加框
                textField.clear();
            }
        });
        textField.setPrefWidth(600);
    }

    /**
     * 初始化工具栏搜索框及其事件
     */
    private void initToolbarSearchField() {
        TextField searchField = new TextField();
        searchField.setPromptText("输入关键字并回车进行查询...");
        this.getToolBar().getItems().add(searchField);
        searchField.setPrefWidth(200);

        searchField.setOnKeyReleased(event -> {
            if (event.getCode() != KeyCode.ENTER) {
                return;
            }

            String key = searchField.getText().trim();

            if (null == dataList) {
                // 存储原始数据
                dataList = new ArrayList<>(getTableView().getItems().size());
                dataList.addAll(getTableView().getItems());
            }

            // 根据关键字进行筛选
            List<Entity> leftList = new ArrayList<>(dataList.size());
            dataList.forEach(entity -> {
                boolean find = false;

                for (Object o : entity.values()) {
                    if (null == o) {
                        continue;
                    }

                    if (o.toString().contains(key)) {
                        find = true;
                        break;
                    }
                }

                if (find) {
                    leftList.add(entity);
                }
            });

            getTableView().getItems().clear();
            getTableView().getItems().addAll(leftList);
        });
    }

    /**
     * 初始化导入按钮事件
     */
    private void initImportButton() {
        Button importButton = new Button("导入");
        this.getToolBar().getItems().add(importButton);
        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            ExcelService excelService = ServiceFactory.getExcelService();
            List<Entity> list = excelService.importFromFile(file.getPath());

            list.forEach(entity -> entityService.save(entity));
        });
    }

    @Override
    public void saveRow(Entity comparable) {
        if (null != this.entityService) {
            this.entityService.save(comparable);

            this.refreshData();
        }
    }

    @Override
    public Paint getRowFill(Entity comparable) {
        if (comparable.getOrDefault("status", false).equals(true)) {
            return Color.GRAY;
        } else {
            String planDate = comparable.getOrDefault("planDate", "").toString();
            if (!"".equals(planDate)) {
                String nowDate = DateUtils.getNowDateStr();
                int result = planDate.compareTo(nowDate);


                if (0 > result) {
                    return Color.RED;
                } else if (0 == result) {
                    return Color.DARKBLUE;
                }
            }
        }

        return Color.BLACK;
    }

    @Override
    public List<Entity> getItems() {
        if (null != this.entityService) {
            return this.entityService.list();
        }

        return new ArrayList<>(0);
    }
}
