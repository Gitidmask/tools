package com.liuqi.tool.httptool;

import java.io.Serializable;

/**
 * 缓存监听器
 *
 * @author LiuQI 2018/6/1 12:34
 * @version V1.0
 **/
@FunctionalInterface
interface CacheListener extends Serializable {
    /**
     * 当缓存变化时调用的方法
     */
    void cacheChanged();
}
