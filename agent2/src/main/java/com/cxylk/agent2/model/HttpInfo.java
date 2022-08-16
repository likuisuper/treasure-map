package com.cxylk.agent2.model;

/**
 * @author likui
 * @date 2022/8/12 下午5:43
 **/
public class HttpInfo extends BaseDataNode{
    public String url;
    public String clientIp;
    public String error;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "HttpInfo{" +
                ", url='" + url + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
