package com.liuqi.tool.httptool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.liuqi.util.JsonFormater;
import com.liuqi.util.RestHttpClient;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.WindowEvent;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Collections.*;


/**
 * Http调用工具主入口
 *
 * @author LiuQI 2018/5/31 17:22
 * @version V1.0
 **/
public class HttpToolMainPane extends BorderPane implements EventHandler<WindowEvent> {
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final int MAX_DISPLAY_LENGTH = 500000;
    private TextField urlField = new TextField();
    private Tooltip urlTooltip = new Tooltip("URL不能为空");
    private TextArea headerArea = new TextArea();
    private TextArea parameterArea = new TextArea();
    private StatusBar statusBar = new StatusBar();
    private ComboBox<String> httpType = new ComboBox<>();

    private TextArea responseArea = new TextArea();
    private Button sendButton = new Button("发送");

    private ListView<HistoryQuery> historyListView = new ListView<>();

    private HistoryQueryCache queryCache = new HistoryQueryCache();

    private RestHttpClient client = RestHttpClient.client("");

    public HttpToolMainPane() {
        initView();

        initEvent();
    }

    /**
     * 初始化按钮事件
     */
    private void initEvent() {
        sendButton.setOnAction(e -> {
            responseArea.setText("");
            statusBar.showSuccessMessage("");

            String url = urlField.getText().trim();
            if ("".equals(url)) {
                statusBar.showErrorMessage("URL不能为空");
                return;
            }

            client.setUrl(url);

            // 处理报文头
            if (processHeaders()) {
                statusBar.showErrorMessage("报文头格式不正确");
                return;
            }

            String parameters = processParamters();
            if (parameters == null) {
                statusBar.showErrorMessage("参数格式不正确");
                return;
            }

            final String pParameters = parameters;

            Task<String> task = new Task<String>() {
                @Override
                protected String call() {
                    long startTime = System.currentTimeMillis();

                    try {
                        statusBar.showStatusBar();
                        CloseableHttpResponse response;
                        String httpTypeStr = httpType.getSelectionModel().getSelectedItem();
                        if (POST.equals(httpTypeStr)) {
                            response = client.post(pParameters);
                        } else {
                            response = client.get();
                        }

                        CloseableHttpResponse pResponse = response;
                        processResponse(startTime, pResponse, url, pParameters);

                        return null;
                    } catch (Exception ex) {
                        StringBuffer errorInfo = new StringBuffer(ex.getMessage() + "\n");
                        Arrays.stream(ex.getStackTrace()).forEach(stackTraceElement ->
                                errorInfo.append(stackTraceElement.toString()).append("\n"));
                        responseArea.setText(errorInfo.toString());
                    }

                    return "";
                }
            };

            executorService.submit(task);
        });

        // 缓存变化时，界面的List同样变化
        queryCache.addListener(new Listener());

        // 点击历史查询时的事件处理
        historyListView.setOnMouseClicked(e -> {
            if (1 == e.getClickCount()) {
                return;
            }

            HistoryQuery historyQuery = historyListView.getSelectionModel().getSelectedItem();
            if (null != historyQuery) {
                headerArea.setText(historyQuery.getHeads());
                parameterArea.setText(historyQuery.getParameters());
                urlField.setText(historyQuery.getUrl());
            }
        });
    }

    /**
     * 处理返回数据
     */
    private void processResponse(long startTime, CloseableHttpResponse pResponse, String url, String pParameters) {
        Platform.runLater(() -> {
            statusBar.setInfo(System.currentTimeMillis() - startTime, pResponse.toString());
            statusBar.showSuccessMessage("请求成功");
            queryCache.add(new HistoryQuery(url, parameterArea.getText(), headerArea.getText()));

            executorService.submit(() -> {
                    StringBuilder formattedStr = formatResponseMessage(pParameters, pResponse);
                    if (formattedStr == null) {
                        return null;
                    }

                    String pFormattedStr = formattedStr.toString();
                    Platform.runLater(() -> {
                        statusBar.hideStatusBar();
                        String result = pFormattedStr;
                        // 最多展示500K数据
                        if (pFormattedStr.length() > MAX_DISPLAY_LENGTH) {
                            result = pFormattedStr.substring(0, MAX_DISPLAY_LENGTH);
                        }
                        responseArea.setText(result);
                    });

                    return null;
            });
        });
    }

    /**
     * 将结果格式化后返回
     */
    private StringBuilder formatResponseMessage(String pParameters, CloseableHttpResponse pResponse) {
        StringBuilder formattedStr;

        formattedStr = new StringBuilder("Send Parameters: \r\n");
        formattedStr.append(pParameters).append("\r\n\r\n");

        try {
            formattedStr = formattedStr.append("Headers: \r\n");
            for (Header header : pResponse.getAllHeaders()) {
                String name = header.getName();
                String value = header.getValue();

                if (null != name && name.endsWith("_msg")) {
                    value = new String(new Base64Encoder().decode(value), "utf-8");
                }

                formattedStr.append("   ").append(name).append(": ").append(value).append("\r\n");
            }

            formattedStr.append("\r\n\r\nBody: \r\n");

            String bodyStr = EntityUtils.toString(pResponse.getEntity());
            if (bodyStr.startsWith("[")) {
                JSONArray array = JSONArray.parseArray(bodyStr);
                formattedStr.append(JsonFormater.format(array));
            } else {
                try {
                    formattedStr.append(JsonFormater.format(JSONObject.parseObject(bodyStr)));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    formattedStr.append(bodyStr);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
        return formattedStr;
    }

    /**
     * 处理参数
     * @return 如果参数格式不正确则返回Null
     */
    private String processParamters() {
        String parameters = parameterArea.getText().trim();

        // 处理参数，如果第一行是[或者是{，则直接发送
        // 否则，按JSON的方式直接发送
        Map<String, Object> parameterMap = new HashMap<>(10);
        String firstLine = parameters.split("\n")[0];
        if (!("[".equals(firstLine) || "{".equals(firstLine))) {
            String[] parameterLines = parameters.split("\n");
            for (String parameterLine: parameterLines) {
                int index = parameterLine.indexOf("=");
                if (-1 == index) {
                    continue;
                }

                parameterMap.put(parameterLine.substring(0, index), parameterLine.substring(index + 1));
            }

            parameters = JSONObject.toJSONString(parameterMap);
        }
        return parameters;
    }

    /**
     * 处理报文头
     * @return 如果报文头格式不符合要求则返回False；否则True
     */
    private boolean processHeaders() {
        String headStr = headerArea.getText().trim();
        if ("".equals(headStr)) {
            return true;
        }

        String[] headStrs = headStr.split("\n");
        for (String pHeadStr: headStrs) {
            int index = pHeadStr.indexOf(STR_EQUALS);
            if (-1 == index) {
                continue;
            }

            client.addHeader(pHeadStr.substring(0, index), pHeadStr.substring(index + 1));
        }
        return false;
    }

    /**
     * 初始化界面
     */
    private void initView() {
        HBox hBox = new HBox();
        VBox vBox = new VBox();

        httpType.getItems().addAll(POST, GET);
        httpType.getSelectionModel().select(0);
        hBox.getChildren().addAll(httpType, urlField, sendButton);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10, 10, 10, 10));
        HBox.setHgrow(urlField, Priority.ALWAYS);
        urlField.setPromptText("URL");
        vBox.getChildren().addAll(hBox);
        this.setTop(vBox);

        VBox leftBox = new VBox();
        leftBox.getChildren().addAll(headerArea, parameterArea);
        headerArea.setPrefHeight(100);
        headerArea.setText("user_id=394\ntenant_id=31\naccess_token=userTicket:TK-12978-vftQQuAqwrdAvtVpgjcKbQE4RTIKH0eCCdv");
        headerArea.setWrapText(true);

        parameterArea.setText("tenantId=31\ndeviceTypeId=5981\n");
        VBox.setVgrow(parameterArea, Priority.ALWAYS);
        this.setLeft(leftBox);

        this.setCenter(responseArea);

        responseArea.setWrapText(true);
        responseArea.setPromptText("返回信息");
        responseArea.setEditable(false);
        Tooltip.install(urlField, urlTooltip);

        // 历史查询区域
        VBox bottomBox = new VBox();
        this.setBottom(bottomBox);

        bottomBox.getChildren().add(historyListView);
        historyListView.setPrefHeight(100);
        historyListView.getItems().addAll(queryCache.getQuerySet());
        sort(historyListView.getItems());

        bottomBox.getChildren().add(statusBar);
        statusBar.setPrefHeight(30);

        // 设置字体
        responseArea.setFont(Font.font(FONT_CONSOLAS));
        headerArea.setFont(Font.font(FONT_CONSOLAS));
        parameterArea.setFont(Font.font(FONT_CONSOLAS));
    }

    @Override
    public void handle(WindowEvent event) {
        // 监听到窗口关闭时退出当前线程
        executorService.shutdownNow();
    }

    private class Listener implements CacheListener, Serializable {

        @Override
        public void cacheChanged() {
            ObservableList<HistoryQuery> itemList = historyListView.getItems();
            itemList.clear();
            itemList.addAll(queryCache.getQuerySet());
            sort(itemList);
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(new String(new Base64Encoder().decode("5pyq5o6I5p2D"), "utf-8"));
    }

    private static final String FONT_CONSOLAS = "Consolas";
    private static final String STR_EQUALS = "=";
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
}
