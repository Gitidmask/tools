/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components.table;

import com.liuqi.ui.common.components.BeanObservableValue;
import com.liuqi.ui.common.components.ColumnUserObject;
import com.liuqi.util.ReflectUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import java.util.List;

/**
 * 树形表格抽象类
 * @param <T>
 * @author qi.liu
 */
public abstract class AbstractLTreeTableView<T> extends TreeTableView<T> {
    private TreeItem<T> rootItem;

    public AbstractLTreeTableView() {
        rootItem = new TreeItem<>();
        this.setRoot(rootItem);
        this.setShowRoot(false);

        this.setEditable(true);

    }

    /**
     * 根据行实体对象获取行的颜色
     */
    public abstract Paint getRowFill(T t);

    /**
     * 保存某一行对象
     */
    public abstract void saveRow(T t);

    /**
     * 添加CheckBox类型的列
     */
    public AbstractLTreeTableView<T> addCheckBoxColumn(String title, String property) {
        TreeTableColumn<T, CheckBox> column = new TreeTableColumn<>(title);
        column.setCellValueFactory(param -> {
            CheckBox checkBox = new CheckBox();
            ObservableValue<Boolean> value = new BeanObservableValue<>(property, param.getValue().getValue());
            if (null != value.getValue()) {
                checkBox.setSelected(value.getValue());
            } else {
                checkBox.setSelected(false);
            }

            checkBox.setOnAction(event -> {
                ReflectUtil.setBeanValue(param.getValue().getValue(), property, checkBox.isSelected());

                saveRow(param.getValue().getValue());

                List<TreeItem<T>> list = param.getValue().getChildren();
                if (null != list && checkBox.isSelected()) {
                    //父节点设置成完成时，将所有子节点设置成完成
                    list.forEach(item -> {
                        T t = item.getValue();
                        if (null != t) {
                            ReflectUtil.setBeanValue(t, property, true);
                            saveRow(t);
                        }
                    });
                }

            });

            checkBox.setTextFill(getRowFill(param.getValue().getValue()));

            return new ReadOnlyObjectWrapper<>(checkBox);
        });

        //设置column宽度
        column.setMaxWidth(60d);

        this.getColumns().add(column);

        column.setUserData(new ColumnUserObject(title, property, 60d));

        return this;
    }

    /**
     * 添加下拉的列
     */
    public <R> AbstractLTreeTableView<T> addComboBoxColumn(String title, String property,
                                                           List<R> itemList) {
        TreeTableColumn<T, R> column = new TreeTableColumn<>(title);

        column.setCellValueFactory(param -> new BeanObservableValue<>(property, param.getValue().getValue()));

        column.setCellFactory(param -> new ComboBoxTreeTableCell<T, R>(FXCollections.observableArrayList(itemList)) {
            @Override
            public void updateItem(R item, boolean empty) {
                super.updateItem(item, empty);

                if (null != getTreeTableRow() && null != getTreeTableRow().getItem()) {
                    setTextFill(getRowFill((T) getTreeTableRow().getItem()));
                }
            }
        });

        column.setOnEditCommit(event -> {
            T t = event.getRowValue().getValue();
            ReflectUtil.setBeanValue(t, property, event.getNewValue());
            saveRow(t);
        });

        this.getColumns().add(column);

        column.setUserData(new ColumnUserObject(title, property, column.getWidth()));

        return this;
    }

    /**
     * 添加普通列
     */
    public AbstractLTreeTableView<T> addColumn(String title, String property, double...width) {
        TreeTableColumn<T, String> column = new TreeTableColumn<>(title);
        //设置数据处理器
        column.setCellValueFactory(param -> new BeanObservableValue<>(property, param.getValue().getValue()));

        column.setOnEditCommit(event -> {
            T t = event.getRowValue().getValue();
            ReflectUtil.setBeanValue(t, property, event.getNewValue());
            saveRow(t);
        });

        column.setCellFactory(new Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>>() {
            @Override
            public TreeTableCell<T, String> call(TreeTableColumn<T, String> param) {
                return new TextFieldTreeTableCell<T, String>(new DefaultStringConverter()) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (null != getTreeTableRow() && null != getTreeTableRow().getItem()) {
                            setTextFill(getRowFill((T) getTreeTableRow().getItem()));
                        }
                    }
                };
            }
        });

        column.setOnEditCommit(event -> {
            T t = event.getRowValue().getValue();
            ReflectUtil.setBeanValue(t, property, event.getNewValue());
            saveRow(t);
        });

        //设置列宽
        if (1 == width.length) {
            column.setMaxWidth(width[0]);
        } else if (2 == width.length) {
            column.setMinWidth(width[0]);
            column.setPrefWidth(width[0]);
            column.setMaxWidth(width[1]);
        }

        this.getColumns().add(column);

        column.setUserData(new ColumnUserObject(title, property, column.getWidth()));

        return this;
    }
}
