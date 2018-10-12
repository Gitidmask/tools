package com.liuqi.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * JSON格式化工具
 *
 * @author LiuQI 2018/6/1 12:15
 * @version V1.0
 **/
public class JsonFormater {
    public static String format(JSONObject jsonObject) {
        Level level = new Level();
        return format(jsonObject, level);
    }

    /**
     * 格式化JSON对象，其中blankLevel表示的是当前对象格式化后行前需要有多少个空格
     */
    public static String format(JSONObject jsonObject, Level blankLevel) {
        if (null == jsonObject) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        result.append("{\n");

        blankLevel.value++;

        jsonObject.entrySet().forEach(item -> {
            String key = item.getKey();
            Object value = item.getValue();

            if (null == value) {
                return;
            }

            result.append(getBlank(blankLevel));

            if (value instanceof JSONObject) {
                result.append(key).append(": ").append(format((JSONObject) value, blankLevel)).append(",\n");
            } else if (value instanceof JSONArray) {
                result.append(key).append(": ").append(format((JSONArray) value, blankLevel)).append(",\n");
            } else {
                result.append(key).append(": ").append(value.toString()).append(",\n");
            }
        });

        blankLevel.value--;
        String resultStr = result.substring(0, result.length() - 2) + "\n" + getBlank(blankLevel) + "}";
        return resultStr;
    }

    /**
     * 格式化JSON对象数组
     */
    public static String format(JSONArray array, Level blankLevel) {
        if (0 == array.size()) {
            return getBlank(blankLevel) + "[]";
        }

        StringBuffer result = new StringBuffer("[\n");
        blankLevel.value++;
        array.forEach(item -> {
            if (item instanceof JSONObject) {
                result.append(getBlank(blankLevel)).append(format((JSONObject) item, blankLevel)).append(",\n");
            } else if (item instanceof JSONArray) {
                result.append(getBlank(blankLevel)).append(format((JSONArray) item, blankLevel)).append(",\n");
            } else {
                result.append(getBlank(blankLevel)).append(item.toString()).append(",\n");
            }
        });

        blankLevel.value--;
        String resultStr = result.substring(0, result.length() - 2) + "\n" + getBlank(blankLevel) + "]";
        return resultStr;
    }

    private static String getBlank(Level blankLevel) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < blankLevel.value; i++) {
            stringBuffer.append("    ");
        }

        return stringBuffer.toString();
    }

    public static String format(JSONArray array) {
        StringBuilder stringBuilder = new StringBuilder("[");

        for (Object obj : array) {
            if (obj instanceof JSONArray) {
                stringBuilder.append(format((JSONArray)obj)).append(",");
            } else {
                stringBuilder.append(format((JSONObject)obj)).append(",");
            }
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.append("]").toString();
    }

    private static class Level {
        private int value = 0;
    }
}
