package com.liuqi.ui.common.components.table;

import com.liuqi.ui.common.apps.PanelConfig;
import com.liuqi.ui.common.entity.Entity;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 通过PanelConfig来配置的表单面板
 *
 * @author LiuQI 2018/9/7 9:25
 * @version V1.0
 **/
public class ConfigEntityTablePanel extends AbstractEntityTablePanel {
    private PanelConfig panelConfig;

    public static ConfigEntityTablePanel getInstance(PanelConfig panelConfig) {
        return new ConfigEntityTablePanel(panelConfig);
    }

    private ConfigEntityTablePanel(PanelConfig panelConfig) {
        this.panelConfig = panelConfig;
        this.setConfigFile(panelConfig.getEntityConfigFile());
    }

    @Override
    public void initToolBar() {
        super.initToolBar();
        List<ComboBox<String>> comboBoxList = panelConfig.getToolbarComboBoxList();

        if (null == comboBoxList) {
            return;
        }

        comboBoxList.forEach(comboBox -> {
            @SuppressWarnings("unchecked")
            Predicate<Entity> filter = (Predicate<Entity>) comboBox.getUserData();
            if (null == filter) {
                return;
            }

            ConfigEntityTablePanel.this.toolBar.getItems().add(comboBox);
            comboBox.setOnAction(event -> {
                List<Entity> dataList = ConfigEntityTablePanel.this.getItems();
                List<Entity> filterList = dataList.stream().filter(filter).collect(Collectors.toList());
                ConfigEntityTablePanel.this.refreshData(filterList);
            });
        });
    }

    @Override
    public void saveRow(Entity comparable) {
        Consumer<Entity> consumer = panelConfig.getBeforeSaveConsumer();
        if (null != consumer) {
            consumer.accept(comparable);
        }

        super.saveRow(comparable);
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
