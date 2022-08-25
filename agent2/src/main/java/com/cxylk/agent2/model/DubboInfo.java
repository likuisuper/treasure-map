package com.cxylk.agent2.model;

/**
 * @author likui
 * @date 2022/8/23 下午4:46
 **/
public class DubboInfo extends BaseDataNode{
    public static final String SEAT_SERVER="server";
    public static final String SEAT_CLIENT="client";
    private String remoteIp;
    private String remoteUrl;
    private String clientIp;
    private String serviceInterface;
    private String serviceMethodName;
    private String  error;
    /**
     * 所在位：client或者server，区分服务端和客户端
     */
    private String seat;

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getServiceMethodName() {
        return serviceMethodName;
    }

    public void setServiceMethodName(String serviceMethodName) {
        this.serviceMethodName = serviceMethodName;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }
}
