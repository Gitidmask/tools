package com.liuqi.tool.httptool;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.liuqi.tool.Constants.HTTP_HISTORY_DATA_FILE;

/**
 * 历史查询缓存
 *
 * @author LiuQI 2018/6/1 12:28
 * @version V1.0
 **/
class HistoryQueryCache implements Serializable {
    private Set<CacheListener> cacheListeners = new HashSet<>();
    private Set<String> urlSet = new HashSet<>(10);
    private Set<HistoryQuery> querySet;

    HistoryQueryCache() {
        Path path = Paths.get(HTTP_HISTORY_DATA_FILE);
        try {
            querySet = Files.readAllLines(path).stream().map(line -> JSONObject.parseObject(line, HistoryQuery.class))
                .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null == querySet) {
            querySet = new HashSet<>();
        } else {
            querySet.forEach(item -> urlSet.add(item.getUrl()));
        }
    }

    /**
     * 获取所有缓存的查询
     */
    Set<HistoryQuery> getQuerySet() {
        return querySet;
    }

    /**
     * 缓存请求
     */
    public boolean add(HistoryQuery historyQuery) {
        for (HistoryQuery item : querySet) {
            if (item.getUrl().equals(historyQuery.getUrl())) {
                item.addUseTimes();
                item.setParameters(historyQuery.getParameters());
                item.setHeads(historyQuery.getHeads());
                item.setAddTime(new Date());
                break;
            }
        }

        if (urlSet.contains(historyQuery.getUrl())) {
            if (null != cacheListeners) {
                cacheListeners.forEach(CacheListener::cacheChanged);
            }

            saveToFile();
            return false;
        }

        urlSet.add(historyQuery.getUrl());

        historyQuery.addUseTimes();
        historyQuery.setAddTime(new Date());
        boolean b = querySet.add(historyQuery);
        if (b && null != cacheListeners) {
            cacheListeners.forEach(CacheListener::cacheChanged);
        }

        saveToFile();

        return b;
    }

    /**
     * 将缓存保存到文件
     */
    private void saveToFile() {
        Path path = Paths.get(HTTP_HISTORY_DATA_FILE);
        List<String> lineList = querySet.stream().map(JSONObject::toJSONString).collect(Collectors.toList());

        try {
            Files.write(path, lineList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加缓存监听
     */
    void addListener(CacheListener cacheListener) {
        this.cacheListeners.add(cacheListener);
    }
}
