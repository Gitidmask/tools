package com.liuqi.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * .
 *
 * @author LiuQI 2018/5/21 16:38
 * @version V1.0
 **/
public class RestHttpClient {
    private Map<String, Object> dataMap = new HashMap<>();
    private String url;
    private CloseableHttpClient httpClient;
    private Map<String, String> headerMap = new HashMap<>();

    private RestHttpClient(String url) {
        this.url = url;
        CookieStore cookieStore = new BasicCookieStore();
        this.httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }

    public static RestHttpClient client(String url) {
        return new RestHttpClient(url);
    }

    /**
     * 设置请求参数
     *
     */
    public RestHttpClient setParameter(String key, Object value) {
        this.dataMap.put(key, value);
        return this;
    }

    public RestHttpClient addHeader(String key, String value) {
        headerMap.put(key, value);
        return this;
    }

    public CloseableHttpResponse post() throws IOException {
        return post(JSONObject.toJSONString(dataMap));
    }

    public CloseableHttpResponse post(String dataStr) throws IOException {
        assert dataStr != null;

        String pUrl = url;
        if (20 >= dataStr.length()) {
            JSONObject object = JSONObject.parseObject(dataStr);
            Optional<String> paramStr = object.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((s1, s2) -> s1.concat("&").concat(s2));
            pUrl += "?" + paramStr.orElse("");
        }

        HttpPost httpPost = new HttpPost(pUrl);
        StringEntity entity = new StringEntity(dataStr, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        headerMap.forEach(httpPost::setHeader);

        return httpClient.execute(httpPost);
    }

    public CloseableHttpResponse get() throws IOException {
        HttpGet httpPost = new HttpGet(url);
        headerMap.forEach(httpPost::setHeader);

        return httpClient.execute(httpPost);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(RestHttpClient.client("http://saas-uop.bdn_saas-dev.irootechapp.com/auth/login")
                .setParameter("username", "admin_test022")
                .setParameter("password", "admin")
                .post());
    }
}