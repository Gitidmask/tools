/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components.table;

import com.liuqi.ui.common.components.ExcelExportDialogProxy;
import com.liuqi.ui.common.components.table.LToolBarTreeTablePane;
import com.liuqi.ui.common.entity.Entity;
import com.liuqi.ui.common.entity.EntityAttributeConfig;
import com.liuqi.ui.common.entity.EntityAttributeType;
import com.liuqi.ui.common.entity.EntityConfig;
import com.liuqi.ui.common.service.EntityService;
import com.liuqi.ui.common.service.def.FileEntityService;
import com.liuqi.util.DateUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 实体管理表格面板
 *
 * @author LiuQI 2018/7/10 11:24
 * @version V1.0
 **/
public abstract class EntityTreeTablePanel extends LToolBarTreeTablePane<Entity> {
    protected EntityConfig entityConfig;
    protected EntityService entityService;

    private String configFileName;

    @Override
    public void initView() {

    }

    public EntityTreeTablePanel(String configFile) {
        this.configFileName = configFile;
        this.entityConfig = new EntityConfig("conf/" + configFile + ".xml");
        this.entityService = entityConfig.getEntityService();

        // 可以选择单元格
        this.tableView.getSelectionModel().cellSelectionEnabledProperty().setValue(true);

        // 可以多选
        this.tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 按ctrl + c时复制当前单元格
        setCopyAction();

        // 根据配置文件添加表格列
        initColumnsAfterConstruct();

        if (null == entityService) {
            try {
                this.entityService = new FileEntityService("data/" + configFile + ".txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 配置鼠标按住拖动时选择多个单元格
     */
    private void setMouseSelectAction() {
        TableMouseSelection tableMouseSelection = new TableMouseSelection();

        this.tableView.setOnMouseDragEntered(event -> {
            tableMouseSelection.isPressed = true;
        });

        this.tableView.setOnMouseDragged(event -> {
            if (tableMouseSelection.isPressed) {
                System.out.println(event.getSource());
            }
        });

        this.tableView.setOnMouseDragReleased(event -> {
            tableMouseSelection.isPressed = false;
        });
    }

    /**
     * 按ctrl+C时将所选择的内容复制到系统粘贴板
     */
    private void setCopyAction() {
        this.tableView.setOnKeyPressed(event -> {
           if (event.getCode() == KeyCode.C && event.isControlDown()) {
               ObservableList<TreeTablePosition<Entity, ?>> selectedCells = this.tableView.getSelectionModel().getSelectedCells();
               if (null != selectedCells && 0 != selectedCells.size()) {
                   TreeTablePosition position = selectedCells.get(0);
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
    private void initColumnsAfterConstruct() {
        Collection<EntityAttributeConfig> attributeConfigs = entityConfig.getAttributeList();
        if (0 != attributeConfigs.size()) {
            attributeConfigs.forEach(attributeConfig -> {
                EntityAttributeType type = attributeConfig.getType();
                switch (type) {
                    case SELECT: {
                        if (null != attributeConfig.getSelectItems()) {
                            tableView.addComboBoxColumn(attributeConfig.getTitle(), attributeConfig.getKey(),
                                    attributeConfig.getSelectItems());
                        } else {
                            tableView.addColumn(attributeConfig.getTitle(), attributeConfig.getKey());
                        }

                        break;
                    }
                    case CHECKBOX: {
                        tableView.addCheckBoxColumn(attributeConfig.getTitle(), attributeConfig.getKey());
                        break;
                    }
                    default: {
                        if (0 != attributeConfig.getWidth()) {
                            tableView.addColumn(attributeConfig.getTitle(), attributeConfig.getKey(), attributeConfig.getWidth());
                        } else {
                            tableView.addColumn(attributeConfig.getTitle(), attributeConfig.getKey());
                        }
                    }
                }
            });
        }
    }

    @Override
    public void initToolBar() {
        TextField textField = new TextField();
        textField.setPromptText("添加新记录...");
        this.toolBar.getItems().add(textField);

        textField.setOnKeyReleased(event -> {
            if (KeyCode.ENTER == event.getCode()) {
                Entity entity = new Entity();

                TreeItem<Entity> selItem = tableView.getSelectionModel().getSelectedItem();
                Entity parentEntity = null;

                if (null != selItem) {
                    if (null != selItem.getParent() && !selItem.getParent().equals(tableView.getRoot())) {
                        parentEntity = selItem.getParent().getValue();
                    } else {
                        parentEntity = selItem.getValue();
                    }
                    entity.put("parentId", parentEntity.getOrDefault("id", ""));
                }

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
                tableView.getSelectionModel().clearSelection();
            }
        });
        textField.setPrefWidth(600);

        TextField searchField = new TextField();
        searchField.setPromptText("输入关键字并回车进行查询...");
        this.toolBar.getItems().add(searchField);
        searchField.setPrefWidth(200);

        searchField.setOnKeyReleased(event -> {
            if (event.getCode() != KeyCode.ENTER) return;

            String key = searchField.getText().trim();

            // 根据关键字进行筛选
            List<Entity> dataList = entityService.list();
            List<Entity> leftList = new ArrayList<>(dataList.size());
            dataList.forEach(entity -> {
                boolean find = false;

                for (Object o : entity.values()) {
                    if (null == o) continue;

                    if (o.toString().contains(key)) {
                        find = true;
                        break;
                    }
                }

                if (find) {
                    leftList.add(entity);
                }
            });

            refreshData(leftList);
        });

        // 添加导出按钮
        Button exportButton = new Button("导出");
        exportButton.setOnAction(e -> ExcelExportDialogProxy.INSTANCE.saveFile(tableView, configFileName + ".xls"));
        this.toolBar.getItems().add(exportButton);
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

    private static class TableMouseSelection {
        boolean isPressed = false;
    }
}
