package com.cxylk.agent2.model;

/**
 * @author likui
 * @date 2022/8/12 下午5:43
 **/
public class HttpInfo extends BaseDataNode{
    private String url;
    private String clientIp;
    private String error;

    private String httpParams;
    private String code;
    private String codeStack;

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

    public String getHttpParams() {
        return httpParams;
    }

    public void setHttpParams(String httpParams) {
        this.httpParams = httpParams;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeStack() {
        return codeStack;
    }

    public void setCodeStack(String codeStack) {
        this.codeStack = codeStack;
    }

    @Override
    public String toString() {
        return "HttpInfo{" +
                "url='" + url + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", error='" + error + '\'' +
                ", httpParams='" + httpParams + '\'' +
                ", code='" + code + '\'' +
                ", codeStack='" + codeStack + '\'' +
                '}';
    }
}
