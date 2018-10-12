/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.apps;

import com.github.fastxml.exception.ParseException;
import com.liuqi.ui.common.entity.Entity;
import com.liuqi.util.DateUtils;
import com.liuqi.util.FastXmlUtil;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 应用配置
 *
 * @author LiuQI 2018/7/26 13:23
 * @version V1.0
 **/
public class AppConfig {
    private List<PanelConfig> panelConfigList = new ArrayList<>(10);

    private static final String APP_CONFIG_FILE = "conf/config.xml";

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    public AppConfig() {
        loadConfigs();
    }

    private static int compare(ComparatorOrder o1, ComparatorOrder o2) {
        return Integer.compare(o2.getIndex(), o1.getIndex());
    }

    /**
     * 从配置文件中加载面板配置
     */
    private void loadConfigs() {
        try {
            List<Map<String, Object>> itemList = FastXmlUtil.parse(APP_CONFIG_FILE);
            Map<String, Object> appMap = itemList.get(0);
            List<Map<String, Object>> subItemList = (List<Map<String, Object>>) appMap.get("subItems");

            if (null != subItemList) {
                subItemList.forEach(panelItem -> {
                    PanelConfig panelConfig = new PanelConfig();
                    panelConfigList.add(panelConfig);

                    Map<String, Object> attributes = (Map<String, Object>) panelItem.get("attributes");
                    if (null != attributes) {
                        attributes.forEach((key, value) -> {
                            switch (key) {
                                case "title": panelConfig.setTitle(value.toString()); break;
                                case "configFile":
                                    panelConfig.setEntityConfigFile(value.toString());
                                    panelConfig.setEntityDataFile(value.toString());
                                    break;
                                case "display":
                                    panelConfig.setDisplay(Boolean.valueOf(value.toString()));
                                    break;
                                case "type":
                                    panelConfig.setType(value.toString());
                                    break;
                                default:
                                    break;
                            }
                        });
                    }

                    // 处理子元素
                    List<Map<String, Object>> subItems = (List<Map<String, Object>>) panelItem.get("subItems");
                    if (null != subItems) {
                        subItems.forEach(subItem -> {
                            String itemName = subItem.get("itemName").toString();
                            List<Map<String, Object>> secondSubItems = (List<Map<String, Object>>) subItem.get("subItems");
                            switch (itemName) {
                                case "rowFill":
                                    panelConfig.setRowFillFunction(getRowFillFunction(secondSubItems));
                                    break;
                                case "comparator":
                                    panelConfig.setComparator(getComparator(secondSubItems));
                                    break;
                                case "beforeSave":
                                    panelConfig.setBeforeSaveConsumer(getBeforeSaveConsumer(secondSubItems));
                                    break;
                                case "nodeExpand":
                                    panelConfig.setTreeNodeExpandFunction(getTreeNodeExpandFunction(secondSubItems));
                                    break;
                                case "search":
                                    panelConfig.setToolbarComboBoxList(getAdditionalSearchFunction(secondSubItems));
                                    break;
                                default: break;
                            }
                        });
                    }
                });
            }
        } catch (IOException | ParseException e) {
            logger.error("加载应用配置文件失败", e);
        }
    }

    /**
     * 处理附加的筛选控制及其事件
     */
    private List<ComboBox<String>> getAdditionalSearchFunction(List<Map<String, Object>> items) {
        // 目前仅支持下拉的方式
        if (null == items || 0 == items.size()) {
            return null;
        }

        List<ComboBox<String>> comboBoxList = new ArrayList<>(4);
        items.forEach(item -> {
            String itemName = item.get("itemName").toString();
            switch (itemName) {
                case "select":
                    ComboBox<String> comboBox = getAdditionalSearchSelectFunction(item);
                    if (null != comboBox) {
                        comboBoxList.add(comboBox);
                    }
                    break;
                default: return;
            }
        });

        return comboBoxList;
    }

    /**
     * 处理附加搜索中的Select
     */
    @SuppressWarnings("uncheck")
    private ComboBox<String> getAdditionalSearchSelectFunction(Map<String, Object> item) {
        Map<String, Object> attributeMap = (Map<String, Object>) item.get("attributes");
        List<Map<String, Object>> subItems = (List<Map<String, Object>>) item.get("subItems");

        if (null == subItems || 0 == subItems.size()) {
            logger.warn("Select下必须有子元素item");
            return null;
        }

        ComboBox<String> comboBox = new ComboBox<>();
        Map<String, Map<String, String>> itemAttributeMap = new HashMap<>(10);

        // 设置下拉框下拉项
        List<String> itemNameList = subItems.stream().map(subItem -> {
            Map<String, String> subAttributeMap = (Map<String, String>) subItem.get("attributes");
            String name = subAttributeMap.get("name");
            if (null == name) {
                logger.warn("item子元素必须包含name属性");
                return null;
            }

            itemAttributeMap.put(name, subAttributeMap);

            return name;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        comboBox.getItems().addAll(itemNameList);

        Predicate<Entity> function = entity -> {
            String selName = comboBox.getSelectionModel().getSelectedItem();
            if (null == selName) {
                return true;
            }

            Map<String, String> selItemAttributeMap = itemAttributeMap.get(selName);
            String key = selItemAttributeMap.get("key");
            String value = selItemAttributeMap.get("value");

            if (null == key || null == value || null == entity.get(key)) {
                // 如果没有Key或者Value，那么返回所有值
                return true;
            }

            // 有Key时，根据Key所对应的值进行筛选
            Object obj = entity.get(key);
            String keyValue = obj.toString();
            if (value.startsWith("!(")) {
                return !value.contains(keyValue);
            } else {
                return value.contains(keyValue);
            }
        };

        comboBox.setUserData(function);

        // 设置下拉框默认的选择项
        String defaultValue = attributeMap.getOrDefault("default", "").toString();
        comboBox.getProperties().put("defaultValue", defaultValue);

        return comboBox;
    }

    private Function<Entity, Boolean> getTreeNodeExpandFunction(List<Map<String, Object>> items) {
        if (null == items || 0 == items.size()) {
            return null;
        }

        Map<String, Function<Entity, Boolean>> functionMap = new HashMap<>(6);

        items.forEach(item -> {
            String itemName = item.get("itemName").toString();
            @SuppressWarnings("unchecked")
            Map<String, String> attributes = (Map<String, String>) item.get("attributes");
            Boolean ret = Boolean.valueOf(attributes.getOrDefault("ret", "true"));

            switch (itemName) {
                case "if": case "elif": functionMap.put(itemName, entity -> {
                        String key = attributes.get("key");
                        String value = attributes.getOrDefault("value", "");

                        String entityValue = entity.getOrDefault(key, "").toString();
                        if (entityValue.equals("")) {
                            return true;
                        }

                        if (-1 != value.indexOf(entityValue)) {
                            return ret;
                        }

                        return null;
                    }); break;
                case "else": functionMap.put(itemName, entity -> ret);  break;
                default: break;
            }
        });

        return entity -> {
            Function<Entity, Boolean> ifFunction = functionMap.get("if");
            Function<Entity, Boolean> elifFunction = functionMap.get("elif");
            Function<Entity, Boolean> elseFunction = functionMap.get("else");

            if (null != ifFunction) {
                Boolean ifResult = ifFunction.apply(entity);
                if (null != ifResult) {
                    return ifResult;
                }

                if (null != elifFunction) {
                    Boolean elifResult = elifFunction.apply(entity);
                    if (null != elifResult) {
                        return elifResult;
                    }
                }

                if (null != elseFunction) {
                    return elseFunction.apply(entity);
                }
            }

            return true;
        };
    }

    /**
     * 获取保存实体对象前执行的函数
     *
     * @param items
     * @return
     */
    private Consumer<Entity> getBeforeSaveConsumer(List<Map<String, Object>> items) {
        if (null == items || 0 == items.size()) {
            return null;
        }

        List<Consumer<Entity>> consumerList = new ArrayList<>(10);
        Map<String, Object> functionMap = new HashMap<>(3);

        items.forEach(item -> {
            String itemName = item.get("itemName").toString();
            Map<String, String> attributes = (Map<String, String>) item.get("attributes");
            List<Map<String, Object>> subItems = (List<Map<String, Object>>) item.get("subItems");

            Consumer<Entity> subConsumer = null;
            if (null != subItems) {
                subConsumer = getBeforeSaveConsumer(subItems);
            }

            String key = attributes.get("key");
            String value = attributes.get("value");
            Consumer<Entity> pSubConsumer = subConsumer;

            if ("prop".equals(itemName)) {
                consumerList.add(entity -> {
                    if (null != value && !"null".equals(value)) {
                        if ("nowDate".equals(value)) {
                            entity.put(key, DateUtils.getNowDateStr());
                        } else if (("nowDateTime").equals(value)) {
                            entity.put(key, DateUtils.getNowTimeStr());
                        } else {
                            entity.put(key, value);
                        }
                    } else {
                        entity.remove(key);
                    }
                });
            } else if ("if".equals(itemName) || "elif".equals(itemName)) {
                if (null == pSubConsumer) {
                    return;
                }

                Function<Entity, Boolean> ifFunction = entity -> {
                    if (entity.getOrDefault(key, "").toString().equals("empty".equals(value) ? "" : value)) {
                        pSubConsumer.accept(entity);
                        return true;
                    }

                    return false;
                };
                functionMap.put(itemName, ifFunction);
            } else if ("else".equals(itemName)) {
                if (null == pSubConsumer) {
                    return;
                }

                functionMap.put(itemName, pSubConsumer);
            }
        });

        return entity -> {
            consumerList.forEach(consumer -> consumer.accept(entity));

            Function<Entity, Boolean> ifFunction = (Function<Entity, Boolean>) functionMap.get("if");
            if (null != ifFunction) {
                Boolean b = ifFunction.apply(entity);
                if (b) {
                    return;
                }

                Function<Entity, Boolean> elifFunction = (Function<Entity, Boolean>) functionMap.get("elif");
                if (null != elifFunction) {
                    b = elifFunction.apply(entity);
                    if (b) {
                        return;
                    }
                }

                Consumer<Entity> elseConsumer = (Consumer<Entity>) functionMap.get("else");
                if (null != elseConsumer) {
                    elseConsumer.accept(entity);
                }
            }

        };
    }

    /**
     * 获取数据排序的Comparator
     *
     * @param items
     * @return
     */
    private Comparator<Entity> getComparator(List<Map<String, Object>> items) {
        if (null == items || 0 == items.size()) {
            return null;
        }

        List<ComparatorOrder> orderList = items.stream().filter(item -> {
            String itemName = item.get("itemName").toString();
            if (!"order".equals(itemName)) {
                logger.error("comparator下元素必须是order");
                return false;
            }

            Map<String, Object> attributes = (Map<String, Object>) item.get("attributes");
            if (null == attributes ||
                    0 == attributes.size()) {
                logger.error("comparator下的order元素必须包含key属性");
                return false;
            }

            if ("".equals(attributes.getOrDefault("key", ""))) {
                logger.error("comparator下的order元素必须包含key属性");
                return false;
            }

            return true;
        }).map(item -> {
            ComparatorOrder order = new ComparatorOrder();
            Map<String, Object> attributes = (Map<String, Object>) item.get("attributes");
            String key = attributes.get("key").toString();
            String value = attributes.getOrDefault("value", "").toString();
            String orderBy = attributes.getOrDefault("orderBy", "asc").toString();
            int index = Integer.valueOf(attributes.getOrDefault("index", "0").toString());

            order.setKey(key)
                    .setIndex(index)
                    .setOrderBy(orderBy)
                    .setValue(value);

            return order;
        }).sorted((o1, o2) -> Integer.compare(o1.getIndex(), o2.getIndex())).collect(Collectors.toList());

        logger.info("ComparatorOrderList: {}", orderList);

        return (o1, o2) -> {
            int result = 0;

            for (ComparatorOrder order : orderList) {
                String key = order.getKey();
                String obj1 = o1.getOrDefault(key, "").toString();
                String obj2 = o2.getOrDefault(key, "").toString();

                String value = order.getValue();
                String orderBy = order.getOrderBy();

                if ("".equals(value)) {
                    result = obj1.compareTo(obj2);
                    if (0 != result) {
                        return "asc".equals(orderBy) ? -result : result;
                    }

                    continue;
                }

                int index1 = value.indexOf(obj1);
                int index2 = value.indexOf(obj2);

                if (index1 == index2) {
                    continue;
                }

                result = index1 > index2 ? 1 : -1;
                return "asc".equals(orderBy) ? -result : result;
            }

            return result;
        };
    }

    /**
     * 获取行前景色生成函数
     *
     * @param items
     * @return
     */
    private Function<Entity, Color> getRowFillFunction(List<Map<String, Object>> items) {
        if (null == items || 0 == items.size()) {
            return null;
        }

        List<Function<Entity, Color>> functionList = new ArrayList<>(10);

        items.forEach(item -> {
            String itemName = item.get("itemName").toString();
            Map<String, String> attributes = (Map<String, String>) item.get("attributes");
            List<Map<String, Object>> subItems = (List<Map<String, Object>>) item.get("subItems");

            Function<Entity, Color> subFunction = null;
            if (null != subItems) {
                subFunction = getRowFillFunction(subItems);
            }

            Function<Entity, Color> function = null;
            Function<Entity, Color> pSubFunction = subFunction;

            String key = attributes.get("key");
            String value = attributes.get("value");
            String retColorStr = attributes.get("ret");

            switch (itemName) {
                case "if":
                case "elif": {
                    function = entity -> {
                        String entityValue = entity.getOrDefault(key, "").toString();
                        if (entityValue.equals(value)) {
                            // 走当前这条路径
                            if (null != pSubFunction) {
                                return pSubFunction.apply(entity);
                            } else {
                                return Color.web(retColorStr);
                            }
                        }

                        return null;
                    };

                    break;
                }
                case "else":
                    function = entity -> Color.web(retColorStr);
                    break;

                default: break;
            }

            if (null != function) {
                functionList.add(function);
            }
        });

        return entity -> {
           for (Function<Entity, Color> function: functionList) {
               Color retColor = function.apply(entity);
               if (null != retColor) {
                   return retColor;
               }
           }

           return Color.BLACK;
        };
    }

    public List<PanelConfig> getPanelConfigList() {
        return panelConfigList;
    }

    public AppConfig setPanelConfigList(List<PanelConfig> panelConfigList) {
        this.panelConfigList = panelConfigList;
        return this;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "panelConfigList=" + panelConfigList +
                '}';
    }

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        System.out.println(config);
    }

    private class ComparatorOrder {
        String key;
        String value;
        String orderBy;
        int index;

        public int getIndex() {
            return index;
        }

        public ComparatorOrder setIndex(int index) {
            this.index = index;
            return this;
        }

        public String getKey() {
            return key;
        }

        public ComparatorOrder setKey(String key) {
            this.key = key;
            return this;
        }

        public String getValue() {
            return value;
        }

        public ComparatorOrder setValue(String value) {
            this.value = value;
            return this;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public ComparatorOrder setOrderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        @Override
        public String toString() {
            return "ComparatorOrder{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    ", orderBy='" + orderBy + '\'' +
                    ", index=" + index +
                    '}';
        }
    }
}
