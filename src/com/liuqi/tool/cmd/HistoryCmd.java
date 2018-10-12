package com.liuqi.tool.cmd;

import java.io.Serializable;
import java.util.Objects;

/**
 * 历史命令对象
 *
 * @author LiuQI 2018/9/29 16:48
 * @version V1.0
 **/
public class HistoryCmd implements Serializable {
    private String cmds;
    private String name;

    public String getCmds() {
        return cmds;
    }

    public void setCmds(String cmds) {
        this.cmds = cmds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryCmd that = (HistoryCmd) o;
        return Objects.equals(cmds, that.cmds) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cmds, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
