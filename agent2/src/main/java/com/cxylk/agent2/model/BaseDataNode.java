package com.cxylk.agent2.model;

import java.time.LocalDateTime;

/**
 * @author likui
 * @date 2022/8/12 下午5:44
 **/
public class BaseDataNode {
    private String traceId;
    private LocalDateTime beginTime;
    private long useTime;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
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

    @Override
    public String toString() {
        return "BaseDataNode{" +
                "traceId='" + traceId + '\'' +
                ", beginTime=" + beginTime +
                ", useTime=" + useTime +
                '}';
    }
}
