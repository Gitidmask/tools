package com.liuqi.tool.httptool;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 历史查询对象
 *
 * @author LiuQI 2018/6/1 12:23
 * @version V1.0
 **/
class HistoryQuery implements Serializable, Comparable {
    private String url;
    private String parameters;
    private String heads;
    private int useTimes;
    private Date addTime;

    HistoryQuery() {

    }

    HistoryQuery(String url, String parameters, String heads) {
        this.url = url;
        this.parameters = parameters;
        this.heads = heads;
    }

    String getUrl() {
        return url;
    }

    String getParameters() {
        return parameters;
    }

    String getHeads() {
        return heads;
    }

    @Override
    public String toString() {
        return "[" + useTimes + "]" + url;
    }

    void addUseTimes() {
        this.useTimes++;
    }

    void setParameters(String parameters) {
        this.parameters = parameters;
    }

    void setHeads(String heads) {
        this.heads = heads;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUseTimes() {
        return useTimes;
    }

    public void setUseTimes(int useTimes) {
        this.useTimes = useTimes;
    }

    private Date getAddTime() {
        return addTime;
    }

    void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    @Override
    public int compareTo(Object o) {
        // 按增加时间排序，最近查询的优先级最高
        if (o instanceof HistoryQuery) {
            HistoryQuery dest = (HistoryQuery) o;

            if (null != this.getAddTime()) {
                if (null == dest.getAddTime()) {
                    return -1;
                } else if (this.getAddTime().equals(dest.getAddTime())) {
                    return 0;
                } else {
                    return this.getAddTime().after(dest.getAddTime()) ? -1 : 1;
                }
            } else if (null != dest.getAddTime()) {
                return 1;
            } else {
                if (dest.useTimes == this.useTimes) {
                    return 0;
                } else {
                    return this.useTimes > dest.useTimes ? -1 : 1;
                }
            }
        }

        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HistoryQuery that = (HistoryQuery) o;
        return useTimes == that.useTimes &&
                Objects.equals(url, that.url) &&
                Objects.equals(parameters, that.parameters) &&
                Objects.equals(heads, that.heads) &&
                Objects.equals(addTime, that.addTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, parameters, heads, useTimes, addTime);
    }
}
