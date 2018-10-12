package com.liuqi.ui.common.components.table;

import com.liuqi.ui.common.apps.PanelConfig;
import com.liuqi.ui.common.entity.Entity;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 通过PanelConfig来配置的树型表单面板
 *
 * @author LiuQI 2018/9/7 9:25
 * @version V1.0
 **/
public class ConfigEntityTreeTablePanel extends EntityTreeTablePanel {
    private static PanelConfig panelConfig;

    public static ConfigEntityTreeTablePanel getInstance(PanelConfig panelConfig) {
        ConfigEntityTreeTablePanel.panelConfig = panelConfig;
        ConfigEntityTreeTablePanel tablePanel = new ConfigEntityTreeTablePanel(panelConfig);

        tablePanel.initToolbarComboBoxes();
        return tablePanel;
    }

    private ConfigEntityTreeTablePanel(PanelConfig panelConfig) {
        super(panelConfig.getEntityConfigFile());
    }

    private void initToolbarComboBoxes() {
        List<ComboBox<String>> comboBoxList = panelConfig.getToolbarComboBoxList();
        if (null == comboBoxList) {
            return;
        }

        comboBoxList.forEach(comboBox -> {
            Predicate<Entity> filter = (Predicate<Entity>) comboBox.getUserData();
            if (null == filter) {
                return;
            }

            ConfigEntityTreeTablePanel.this.toolBar.getItems().add(comboBox);
            EventHandler<ActionEvent> eventEventHandler = event -> {
                List<Entity> dataList = ConfigEntityTreeTablePanel.this.getItems();
                List<Entity> filterList = dataList.stream().filter(filter).collect(Collectors.toList());

                // 筛选子元素
                filterList.forEach(entity -> {
                    List<Entity> childrenList = (List<Entity>) entity.get("children");
                    if (null == childrenList) {
                        return;
                    }

                    entity.put("children", childrenList.stream().filter(filter).collect(Collectors.toList()));
                });

                ConfigEntityTreeTablePanel.this.refreshData(filterList);
            };

            comboBox.setOnAction(eventEventHandler);

            String defaultValue = comboBox.getProperties().getOrDefault("defaultValue", "").toString();
            if (!"".equals(defaultValue)) {
                comboBox.setValue(defaultValue);
                eventEventHandler.handle(new ActionEvent());
            }
        });
    }

    @Override
    public void refreshData() {
        List<ComboBox<String>> comboBoxList = panelConfig.getToolbarComboBoxList();
        if (null == comboBoxList) {
            super.refreshData();
            return;
        }

        comboBoxList.forEach(comboBox -> {
            comboBox.getOnAction().handle(new ActionEvent());
        });
    }

    @Override
    public void refreshData(List<Entity> list) {
        super.refreshData(list);
    }

    @Override
    public Function<Entity, Boolean> getNodeExpandFunction() {
        return panelConfig.getTreeNodeExpandFunction();
    }

    @Override
    public List<Entity> getChildren(Entity entity) {
        return (List<Entity>) entity.getOrDefault("children", new ArrayList<>(0));
    }

    @Override
    public void saveRow(Entity comparable) {
        Consumer<Entity> consumer = panelConfig.getBeforeSaveConsumer();
        if (null != consumer) {
            consumer.accept(comparable);
        }

        super.saveRow(comparable);

        // 保存后进行筛选
        List<ComboBox<String>> comboBoxList = panelConfig.getToolbarComboBoxList();
        if (null == comboBoxList) {
            return;
        }

        comboBoxList.forEach(comboBox -> {
            comboBox.getOnAction().handle(new ActionEvent());
        });
    }

    @Override
    public Paint getRowFill(Entity comparable) {
        Function<Entity, Color> function = panelConfig.getRowFillFunction();
        if (null != function) {
            return function.apply(comparable);
        }

        return super.getRowFill(comparable);
    }

    @Override
    public Comparator<Entity> getComparator() {
        Comparator<Entity> comparator = panelConfig.getComparator();
        if (null != comparator) {
            return comparator;
        }

        return null;
    }
}
