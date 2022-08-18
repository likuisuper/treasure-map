package com.cxylk.agent2.model;

/**
 * @author likui
 * @date 2022/8/17 上午10:21
 * service信息采集
 **/
public class ServiceInfo extends BaseDataNode{
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务简称
     */
    private String simpleName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 异常信息
     */
    private String errorMsg;

    /**
     * 异常类型
     */
    private String errorType;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "serviceName='" + serviceName + '\'' +
                ", simpleName='" + simpleName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", errorType='" + errorType + '\'' +
                '}';
    }
}
