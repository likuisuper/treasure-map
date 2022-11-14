package com.cxylk.apm.esDao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author likui
 * @date 2022/11/11 下午12:34
 * es索引
 **/
@Data
@Document(indexName = "apm-2022.11")
public class TraceNode {
    @Id
    private String docId;

    /**
     * 调用链跟踪ID
     * Auto表示自动检测类型
     */
    @Field(type = FieldType.Auto)
    private String traceId;

    /**
     * 事件ID
     */
    @Field(type = FieldType.Auto)
    private String spanId;

    /**
     * 开始时间
     */
    @Field(type = FieldType.Date)
    private Date beginTime;

    /**
     * 耗时
     */
    @Field(type = FieldType.Auto)
    private long useTime;

    /**
     * 结束时间
     */
    @Field(type = FieldType.Auto)
    private long endTime;

    /**
     * 应用名称
     */
    @Field(type = FieldType.Auto)
    private String appName;

    /**
     * 主机
     */
    @Field(type = FieldType.Auto)
    private String host;

    /**
     * 类型：http、sql、service
     */
    @Field(type = FieldType.Auto)
    private String modeType;

    /**
     * 服务名称
     */
    @Field(type = FieldType.Auto)
    private String serviceName;

    /**
     * 服务简称
     */
    @Field(type = FieldType.Auto)
    private String simpleName;

    /**
     * 方法名称
     */
    @Field(type = FieldType.Auto)
    private String methodName;

    /**
     * 异常信息
     */
    @Field(type = FieldType.Auto)
    private String errorMsg;

    /**
     * 异常类型
     */
    @Field(type = FieldType.Auto)
    private String errorType;

    @Field(type = FieldType.Auto)
    private String url;

    @Field(type = FieldType.Auto)
    private String clientIp;

    @Field(type = FieldType.Auto)
    private String error;

    @Field(type = FieldType.Auto)
    private String httpParams;

    @Field(type = FieldType.Auto)
    private String code;

    @Field(type = FieldType.Auto)
    private String codeStack;

    @Field(type = FieldType.Auto)
    public String jdbcUrl;

    @Field(type = FieldType.Auto)
    public String sql;

    @Field(type = FieldType.Auto)
    public String databaseName;

    @Field(type = FieldType.Auto)
    private String remoteIp;

    @Field(type = FieldType.Auto)
    private String remoteUrl;

    @Field(type = FieldType.Auto)
    private String serviceInterface;

    @Field(type = FieldType.Auto)
    private String serviceMethodName;

    /**
     * client|server
     */
    @Field(type = FieldType.Auto)
    private String seat;
}
