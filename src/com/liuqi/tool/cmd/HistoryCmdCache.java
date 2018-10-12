package com.liuqi.tool.cmd;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.liuqi.tool.Constants.CMD_HISTORY_DATA_FILE;

/**
 * 历史命令缓存
 *
 * @author LiuQI 2018/6/1 12:28
 * @version V1.0
 **/
public class HistoryCmdCache implements Serializable {
    private static final String SPLIT_BETWEEN_CMDS = "&&&&&&";
    private static final String SPLIT_INTERNAL_CMD_REGEX = "\\|\\|\\|\\|";
    private static final String SPLIT_INTERNAL_CMD = "||||";
    private Set<HistoryCmd> querySet;
    private static HistoryCmdCache instance;

    static HistoryCmdCache getInstance() {
        if (null == instance) {
            instance = new HistoryCmdCache();
        }

        return instance; 
    }

    private HistoryCmdCache() {
        Path path = Paths.get(CMD_HISTORY_DATA_FILE);
        try {
            List<String> lines = Files.readAllLines(path, Charset.forName("utf-8"));
            if (null == lines || 0 == lines.size()) {
                querySet = new HashSet<>(16);
                return;
            }

            String str = lines.stream().reduce((s1, s2) -> s1.concat("\n").concat(s2)).get();
            if (!"".equals(str.trim())) {
                querySet = Arrays.stream(str.split(SPLIT_BETWEEN_CMDS)).map(line -> {
                    String[] pLine = line.split(SPLIT_INTERNAL_CMD_REGEX);
                    String name = pLine[0];
                    String cmds = pLine[1];

                    HistoryCmd historyCmd = new HistoryCmd();
                    historyCmd.setName(name);
                    historyCmd.setCmds(cmds);
                    return historyCmd;
                }).collect(Collectors.toSet());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null == querySet) {
            querySet = new HashSet<>(16);
        }
    }

    /**
     * 获取所有缓存的查询
     */
    Set<HistoryCmd> getQuerySet() {
        return querySet;
    }

    /**
     * 缓存请求
     */
    public boolean add(HistoryCmd historyQuery) {
        String name = historyQuery.getName();
        boolean find = false;

        for (HistoryCmd historyCmd : querySet) {
            if (historyCmd.getName().equals(name)) {
                historyCmd.setCmds(historyQuery.getCmds());
                find = true;
                break;
            }
        }

        if (!find) {
            querySet.add(historyQuery);
        }

        saveToFile();

        return true;
    }

    /**
     * 将缓存保存到文件
     */
    private void saveToFile() {
        Path path = Paths.get(CMD_HISTORY_DATA_FILE);
        if (0 == querySet.size()) {
            return;
        }

        String str = querySet.stream().map(historyCmd -> historyCmd.getName() + SPLIT_INTERNAL_CMD + historyCmd.getCmds())
                .reduce((s1, s2) -> s1.concat(SPLIT_BETWEEN_CMDS).concat(s2)).get();

        try {
            Files.write(path, str.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
