/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.util;

import com.github.fastxml.FastXmlFactory;
import com.github.fastxml.FastXmlParser;
import com.github.fastxml.exception.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.fastxml.FastXmlParser.*;

/**
 * 使用fastxml解析XML的辅助类
 *
 * @author LiuQI 2018/7/30 11:20
 * @version V1.0
 **/
public class FastXmlUtil {
    private static final Logger logger = LoggerFactory.getLogger(FastXmlUtil.class);

    /**
     * 解析指定的文件，将其解析成List并返回
     */
    public static List<Map<String, Object>> parse(String fileName) throws IOException, ParseException {
        Path path = Paths.get(fileName);
        String lines = Files.readAllLines(path).stream().filter(line -> !line.startsWith("<?"))
                .reduce(String::concat).get();

        logger.info("配置文件内容：" + lines);

        byte[] bytes = lines.getBytes();

        FastXmlParser parser = FastXmlFactory.newInstance(bytes);
        List<Map<String, Object>> resultList = new ArrayList<>(10);

        parser.next();

        while (parser.next() != END_DOCUMENT) {
            resultList.add(parseItem(parser));
        }

        return resultList;
    }

    /**
     * 解析XML中某个元素
     */
    private static Map<String, Object> parseItem(FastXmlParser parser) throws ParseException {
        Map<String, Object> resultMap = new HashMap<>(16);

        String itemName = parser.getStringWithDecoding();
        resultMap.put("itemName", itemName);

        // 先解析当前项的属性
        int next = parser.next();
        Map<String, Object> attributeMap = new HashMap<>(6);
        resultMap.put("attributes", attributeMap);
        while (ATTRIBUTE_NAME == next) {
            String attributeName = parser.getStringWithDecoding();
            parser.next();
            String attributeValue = parser.getStringWithDecoding();

            attributeMap.put(attributeName, attributeValue);

            next = parser.next();
        }

        // 属性解析完，解析下一层元素
        List<Map<String, Object>> subItemList = new ArrayList<>(10);
        resultMap.put("subItems", subItemList);
        while (START_TAG == next) {
            subItemList.add(parseItem(parser));

            next = parser.next();
        }

        return resultMap;
    }

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println(parse("todo.xml"));
    }
}
