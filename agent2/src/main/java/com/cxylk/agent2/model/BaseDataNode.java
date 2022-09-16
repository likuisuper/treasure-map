package com.cxylk.agent2.model;

import java.time.LocalDateTime;

/**
 * @author likui
 * @date 2022/8/12 下午5:44
 **/
public class BaseDataNode {
    /**
     * 调用链跟踪ID
     */
    private String traceId;

    /**
     * 事件ID
     */
    private String spanId;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 耗时
     */
    private long useTime;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 主机
     */
    private String host;

    /**
     * 类型：http、sql、service
     */
    private String modeType;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(LocalDateTime beginTime) {
        this.beginTime = beginTime;
    }

    public long getUseTime() {
        return useTime;
    }

    public void setUseTime(long useTime) {
        this.useTime = useTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getModeType() {
        return modeType;
    }

    public void setModeType(String modeType) {
        this.modeType = modeType;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "BaseDataNode{" +
                "traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                ", beginTime=" + beginTime +
                ", useTime=" + useTime +
                ", endTime=" + endTime +
                ", appName='" + appName + '\'' +
                ", host='" + host + '\'' +
                ", modeType='" + modeType + '\'' +
                '}';
    }
}
