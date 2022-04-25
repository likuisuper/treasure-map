package com.cxylk.agent1.httpinvoker.https;

import com.cxylk.agent1.httpinvoker.ProxyHttpUrlConnection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author likui
 * @date 2022/4/24 下午11:16
 **/
public class Handler extends sun.net.www.protocol.https.Handler {
    @Override
    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) super.openConnection(url, proxy);
        return new ProxyHttpUrlConnection(connection, url);
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }
}
