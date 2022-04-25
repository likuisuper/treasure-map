package com.cxylk.agent1.httpinvoker;

import sun.net.www.protocol.http.Handler;

import java.io.IOException;
import java.net.*;

/**
 * @author likui
 * @date 2022/4/24 下午8:15
 * 通过代理URLStreamHandlerFactory来根据不同协议产生不同的协议处理器，然后使用这些处理器打开连接
 **/
@SuppressWarnings("error")
public class HttpProxy1 {
    public static void registerProtocol(){
        URL.setURLStreamHandlerFactory(new proxyURLStreamHandlerFactory());
    }

    static class proxyURLStreamHandlerFactory implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if("http".equals(protocol)){
                return new AgentHttpHandler();
            }else if("https".equals(protocol)){
                return new AgentHttpsHandler();
            }
            return null;
        }
    }

    static class AgentHttpHandler extends Handler{
        @Override
        protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
            HttpURLConnection connection= (HttpURLConnection) super.openConnection(url, proxy);
            return new ProxyHttpUrlConnection(connection,url);
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return openConnection(url,null);
        }
    }

    static class AgentHttpsHandler extends sun.net.www.protocol.https.Handler{
        @Override
        protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
            HttpURLConnection connection= (HttpURLConnection) super.openConnection(url, proxy);
            return new ProxyHttpUrlConnection(connection,url);
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return openConnection(url,null);
        }
    }
}
