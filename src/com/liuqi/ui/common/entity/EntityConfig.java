/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.entity;

import com.github.fastxml.FastXmlFactory;
import com.github.fastxml.FastXmlParser;
import com.github.fastxml.exception.ParseException;
import com.liuqi.ui.common.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体对象配置
 *
 * @author LiuQI 2018/7/5 11:00
 * @version V1.0
 **/
public class EntityConfig {
    private Map<String, EntityAttributeConfig> attributeConfigs;
    private List<EntityAttributeConfig> attributeList;
    private EntityService entityService;
    private String quickAdd;

    private static final Logger logger = LoggerFactory.getLogger(EntityConfig.class);

    public EntityConfig(String file) {
        this.attributeConfigs = new HashMap<>(16);
        this.attributeList = new ArrayList<>(16);

        init(file);
    }

    private void init(String file) {
        this.attributeConfigs.clear();

        Path path;
        path = Paths.get(new File(file).toURI());
        byte[] bytes;
        try {
            List<String> lines = Files.readAllLines(path);
            bytes = lines.stream().reduce(String::concat).get().getBytes("utf-8");
        } catch (IOException e) {
            logger.error("读取配置文件失败!", e);
            return;
        }

        try {
            FastXmlParser parser = FastXmlFactory.newInstance(bytes);
            while (parser.next() != FastXmlParser.END_DOCUMENT) {
                String name = parser.getString();
                if ("entity".equals(name)) {
                    int next = parser.next();

                    while (FastXmlParser.ATTRIBUTE_NAME == next) {
                        String attributeName = parser.getStringWithDecoding();
                        parser.next();
                        String attributeValue = parser.getStringWithDecoding();
                        switch (attributeName) {
                            case "entityService": {
                                try {
                                    Class<EntityService> entityClass = (Class<EntityService>) Class.forName(attributeValue);
                                    entityService = entityClass.newInstance();
                                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                                    e.printStackTrace();
                                }

                                break;
                            }

                            case "quickAdd": this.quickAdd = attributeValue;  break;

                            default: break;
                        }

                        next = parser.next();
                    }

                    processEntity(parser);
                }
            }

            System.out.println(this.attributeConfigs);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void processEntity(FastXmlParser parser) throws ParseException {
        do {
            EntityAttributeConfig config = processEntityAttribute(parser);
            this.attributeConfigs.put(config.getKey(), config);
            this.attributeList.add(config);
        } while (parser.next() != FastXmlParser.END_TAG);
    }

    private EntityAttributeConfig processEntityAttribute(FastXmlParser parser) throws ParseException {
        EntityAttributeConfig attributeConfig = new EntityAttributeConfig();
        attributeConfig.setKey(parser.getString());

        int next;
        while ((next = parser.next()) != FastXmlParser.END_TAG && next != FastXmlParser.END_TAG_WITHOUT_TEXT) {
            switch (next) {
                case FastXmlParser.ATTRIBUTE_NAME: {
                    String attrName = parser.getString();
                    parser.next();
                    String attrValue = parser.getStringWithDecoding();

                    switch (attrName) {
                        case "type": attributeConfig.setType(EntityAttributeType.parse(attrValue)); break;
                        case "title": attributeConfig.setTitle(attrValue); break;
                        case "default":  attributeConfig.setDefaultValue(attrValue); break;
                        case "width": attributeConfig.setWidth(Integer.parseInt(attrValue));
                        default: break;
                    }
                    break;
                }
                case FastXmlParser.START_TAG: {
                    // 下一级，如select类型时的选项值
                    String selectKey = parser.getString();
                    // 跳过title，直接到title的值
                    parser.next();
                    parser.next();

                    String selectValue = parser.getStringWithDecoding();

                    attributeConfig.addSelectItem(selectKey, selectValue);

                    // 跳转到该选项的结束
                    int selectNext;
                    do {
                        selectNext = parser.next();
                    } while (FastXmlParser.END_TAG_WITHOUT_TEXT != selectNext);
                }
                default:
            }
        }

        return attributeConfig;
    }

    public String getQuickAdd() {
        return quickAdd;
    }

    public EntityConfig setQuickAdd(String quickAdd) {
        this.quickAdd = quickAdd;
        return this;
    }

    public Map<String, EntityAttributeConfig> getAttributeConfigs() {
        return attributeConfigs;
    }

    public List<EntityAttributeConfig> getAttributeList() {
        return attributeList;
    }

    public EntityService getEntityService() {
        return entityService;
    }

    public static void main(String[] args) {
        EntityConfig config = new EntityConfig("todo.xml");
    }
}
