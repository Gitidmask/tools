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
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.util.List;


/**
 * 表格抽象类
 * @author qi.liu
 * @param <T> 表格中存储的对象类型
 */
public abstract class AbstractTableView<T> extends TableView<T> {
    public AbstractTableView() {
        this.setEditable(true);
    }

    /**
     * 根据该行的数据动态获取该行颜色
     */
    public abstract Paint getRowFill(T t);

    /**
     * 获取选择的单元格的背景色
     */
    public Color getSelectionBackground() {
        return Color.web("#F9CC76");
    }

    /**
     * 保存对象
     * @param t 需要保存的对象
     */
    public abstract void saveRow(T t);

    /**
     * 隐藏标题
     */
    public void hideHeader() {
        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            Pane header = (Pane) lookup("TableHeaderRow");
            if (null != header) {
                header.setPrefHeight(0);
                header.setMaxHeight(0);
                header.setMinHeight(0);
                header.setVisible(false);
                header.setManaged(false);
            }
        });
    }

    /**
     * 添加CheckBox类型的列
     */
    public AbstractTableView<T> addCheckBoxColumn(String title, String property) {
        //添加列属性名称
        TableColumn<T, CheckBox> column = new TableColumn<>(title);
        column.setCellValueFactory(param -> {
            CheckBox checkBox = new CheckBox();
            ObservableValue<Boolean> value = new BeanObservableValue<>(property, param.getValue());
            if (null != value.getValue()) {
                checkBox.setSelected(value.getValue());
            } else {
                checkBox.setSelected(false);
            }

            checkBox.setOnAction(event -> {
                ReflectUtil.setBeanValue(param.getValue(), property, checkBox.isSelected());

                saveRow(param.getValue());
            });

            checkBox.setTextFill(getRowFill(param.getValue()));

            return new ReadOnlyObjectWrapper<>(checkBox);
        });

        //设置column宽度
        column.setMaxWidth(30d);

        this.getColumns().add(column);

        column.setUserData(new ColumnUserObject(title, property, column.getWidth()));

        return this;
    }

    /**
     * 添加下拉的列
     */
    public <R> AbstractTableView<T> addComboBoxColumn(String title, String property,
                                                      List<R> itemList) {
        TableColumn<T, R> column = new TableColumn<>(title);

        column.setCellValueFactory(param -> {
            ObservableValue<R> value = new BeanObservableValue<>(property, param.getValue());

            return value;
        });

        column.setCellFactory(param -> {
            TableCell<T, R> cell = new ComboBoxTableCell<T, R>(FXCollections.observableArrayList(itemList)) {
                @Override
                public void updateItem(R item, boolean empty) {
                    super.updateItem(item, empty);

                    if (null != getTableRow() && null != getTableRow().getItem()) {
                        setTextFill(getRowFill((T) getTableRow().getItem()));
                    }
                }
            };

            cell.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    cell.setBackground(new Background(new BackgroundFill(getSelectionBackground(), null, null)));
                } else {
                    cell.setBackground(null);
                }
            });

            return cell;
        });

        column.setOnEditCommit(event -> {
            T t = event.getRowValue();
            ReflectUtil.setBeanValue(t, property, event.getNewValue());
            saveRow(t);
        });

        this.getColumns().add(column);

        column.setUserData(new ColumnUserObject(title, property, column.getWidth()));

        return this;
    }

    /**
     * 增加表格列
     */
    public <S> AbstractTableView<T> addTableColumn(String title, String property,
                                                   Callback<Void, AbstractTableView<S>> callback,
                                                   double...width) {
        TableColumn<T, AbstractTableView<S>> column = new TableColumn<>(title);
        column.setCellValueFactory(param -> {
            List<S> list = ReflectUtil.getBeanValue(param.getValue(), property);
            AbstractTableView<S> tableView = callback.call(null);

            if (null != list && null != tableView) {
                tableView.getItems().clear();
                tableView.getItems().addAll(list);

                tableView.setPrefHeight(28 * (list.size() + 1));
            }

            return new ReadOnlyObjectWrapper<>(tableView);
        });

        this.getColumns().add(column);

        if (1 == width.length) {
            column.setMaxWidth(width[0]);
        } else if (2 == width.length) {
            column.setMinWidth(width[0]);
            column.setPrefWidth(width[0]);
            column.setMaxWidth(width[1]);
        }

        return this;
    }

    /**
     * 添加文本列
     */
    public AbstractTableView<T> addColumn(String title, String property, double...width) {
        TableColumn<T, String> column = new TableColumn<>(title);
        //设置数据处理器
        column.setCellValueFactory(param -> new BeanObservableValue<>(property, param.getValue()));

        column.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
            @Override
            public TableCell<T, String> call(TableColumn<T, String> param) {

                TableCell<T, String> cell = new TextFieldTableCell<T, String>(new DefaultStringConverter()) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(null == item ? "" : item, empty);

                        if (null != getTableRow() && null != getTableRow().getItem()) {
                            setTextFill(getRowFill((T) getTableRow().getItem()));
                        }

                        this.setTooltip(new Tooltip(item));
                    }
                };

                cell.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        cell.setBackground(new Background(new BackgroundFill(getSelectionBackground(), null, null)));
                    } else {
                        cell.setBackground(null);
                    }
                });

                return cell;
            }
        });

        column.setOnEditCommit(event -> {
            T t = event.getRowValue();
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

    /**
     * 添加对象列
     */
    public <I> AbstractTableView<T> addObjectColumn(String title, String property, StringConverter<I> convert, double...width) {
        TableColumn<T, I> column = new TableColumn<>(title);
        //设置数据处理器
        column.setCellValueFactory(param -> new BeanObservableValue<>(property, param.getValue()));

        column.setCellFactory(new Callback<TableColumn<T, I>, TableCell<T, I>>() {
            @Override
            public TableCell<T, I> call(TableColumn<T, I> param) {
                return new TextFieldTableCell<T, I>(convert) {
                    @Override
                    public void updateItem(I item, boolean empty) {
                        super.updateItem(item, empty);

                        if (null != getTableRow() && null != getTableRow().getItem()) {
                            setTextFill(getRowFill((T) getTableRow().getItem()));
                        }
                    }
                };
            }
        });

        column.setOnEditCommit(event -> {
            T t = event.getRowValue();
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
