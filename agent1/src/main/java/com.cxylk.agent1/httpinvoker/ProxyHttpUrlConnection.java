package com.cxylk.agent1.httpinvoker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author likui
 * @date 2022/4/24 下午9:23
 * 静态代理
 **/
public class ProxyHttpUrlConnection extends HttpURLConnection {
    HttpURLConnection target;
    HttpInputStream httpInputStream;
    HttpOutputStream httpOutputStream;

    /**
     * Constructor for the HttpURLConnection.
     *
     * @param u the URL
     */
    public ProxyHttpUrlConnection(HttpURLConnection target, URL u) {
        super(u);
        this.target = target;

    }


    @Override
    public void disconnect() {
        target.disconnect();
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void connect() throws IOException {
        long begin = System.currentTimeMillis();
        target.connect();
        System.out.println(String.format("url：%s 连接用时：%s 毫秒", getURL().toString(),System.currentTimeMillis() - begin) );
    }


    @Override
    public InputStream getInputStream() throws IOException {
        return target.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return target.getOutputStream();
    }

    @Override
    public String toString() {
        return target.toString();
    }





    class HttpInputStream extends BufferedInputStream {
        ByteArrayOutputStream copy = new ByteArrayOutputStream();

        public HttpInputStream(InputStream in) {
            super(in);
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            int i = super.read(b, off, len);
            if (i > 0)
                copy.write(b, off, i);
            return i;
        }

        @Override
        public synchronized int read() throws IOException {
            int i = super.read();
            if (i != -1)
                copy.write(i);
            return i;
        }
    }

    public byte[] getInputBytes() {
        if (httpInputStream != null) {
            return httpInputStream.copy.toByteArray();
        }
        return new byte[]{};
    }

    public byte[] getOutputBytes() {
        if (httpOutputStream != null) {
            return httpOutputStream.copy.toByteArray();
        }
        return new byte[]{};
    }

    class HttpOutputStream extends BufferedOutputStream {
        ByteArrayOutputStream copy = new ByteArrayOutputStream();

        public HttpOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public synchronized void write(int b) throws IOException {
            super.write(b);
            copy.write(b);
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
            copy.write(b, off, len);
        }

        @Override
        public synchronized void flush() throws IOException {
            super.flush();
        }

    }
}
