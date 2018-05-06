package com.example.demo.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 一个轻量的 Http 请求工具,要求JDK8
 *
 * @since 1.8
 * @author wanna
 * @createTime 2018-05-06
 */
public class HttpRequest {

    private static final String GET = "GET";

    private static final String POST = "POST";

    private static final int DEFAULT_TIME_OUT = 6000;

    private String url;

    private Map<String, Object> param = new HashMap<>();

    public HttpRequest(String url) {
        this.url = url;
    }

    public HttpRequest addParameter(String key, Object val) {
        this.param.put(key, val);
        return this;
    }

    // 发起 GET 请求
    public String get() throws IOException {
        return request(GET);
    }

    // 发起 POST 请求
    public String post() throws IOException {
        return request(POST);
    }

    // 具体请求
    private String request(String method) throws IOException {
        String requestURL = buildRequestURL(method);
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setConnectTimeout(DEFAULT_TIME_OUT);

        return GET.equals(method) ? requestGet(connection) : requestPost(connection);
    }

    // POST 请求
    private String requestPost(HttpURLConnection connection) throws IOException {
        // post 请求必须设置成 true
        connection.setDoOutput(true);
        connection.connect();

        // post 请求的参数
        PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
        printWriter.write(getPostParamChar());
        printWriter.flush();
        close(printWriter);

        String response = getResponse(connection);
        connection.disconnect();
        return response;
    }

    // GET 请求
    private String requestGet(HttpURLConnection connection) throws IOException {
        connection.connect();
        String response = getResponse(connection);
        connection.disconnect();
        return response;
    }

    // 获取请求响应结果
    private String getResponse(HttpURLConnection connection) throws IOException {
        InputStream is = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader buffer = new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();

        String line;
        while ((line = buffer.readLine()) != null) {
            builder.append(line);
        }

        close(buffer, reader, is);
        return builder.toString();
    }

    // 获取 post 请求的参数 char 数组
    private char[] getPostParamChar() {
        if (param.size() == 0) {
            return new char[]{};
        }

        return buildRequestParam().toCharArray();
    }

    // 构建请求相关的参数  key = val
    private String buildRequestParam() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Object> entry : param.entrySet()) {
            builder.append(entry.getKey()).append("=");
            Object value = entry.getValue();

            if (value instanceof Collection) {
                builder.append(String.join(",", (Collection) value)).append("&");
            } else if (value instanceof Object[]) {
                Stream.of((Object[]) value).forEach(s -> builder.append(s + ","));
                builder.deleteCharAt(builder.lastIndexOf(",")).append("&");
            } else {
                builder.append(value).append("&");
            }
        }

        return builder.substring(0, builder.length() - 1);
    }

    // 根据 请求类型 构建请求 URL
    private String buildRequestURL(String method) {
        switch (method) {
            case GET:
                return buildRequestURL();
            case POST:
                return this.url;
            default:
                return this.url;
        }
    }

    // 构建URL
    private String buildRequestURL() {
        if (param.size() == 0) {
            return this.url;
        }

        String builderParam = buildRequestParam();
        return this.url + "?" + builderParam;
    }

    // 关闭流
    private void close(Closeable... closeables) throws IOException {
        for (Closeable closeObj : closeables) {
            if (closeObj != null) {
                closeObj.close();
            }
        }
    }
}
