package com.cxylk.bean;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author likui
 * @date 2022/3/31 下午10:29
 **/
public class User implements Serializable {
    private static final long serialVersionUID=1L;

    private Long id;

    private LocalDateTime createTime;


    /**
     * 创建者
     */
    private String createUser;

    /**
     * 创建人
     */
    private Long createUserId;

    /**
     * 备注
     */
    private String description;

    /**
     * 是否是首次登录
     */
    private String firstLogin;

    /**
     * 是否删除
     */
    private String isDelete;


    /**
     * 最后登入时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后修改人
     */
    private String lastModifyUser;

    /**
     * 最后修改人ID
     */
    private Long lastModifyUserId;

    /**
     * 手机号
     */
    private String mobile;

    private LocalDateTime modifyTime;

    /**
     * MD5密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 商户Code
     */
    private Long mrchCode;

    /**
     *  NO 禁用 YES 启用
     */
    private String status;

    /**
     * 用户名称
     */
    private String userName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(String firstLogin) {
        this.firstLogin = firstLogin;
    }

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastModifyUser() {
        return lastModifyUser;
    }

    public void setLastModifyUser(String lastModifyUser) {
        this.lastModifyUser = lastModifyUser;
    }

    public Long getLastModifyUserId() {
        return lastModifyUserId;
    }

    public void setLastModifyUserId(Long lastModifyUserId) {
        this.lastModifyUserId = lastModifyUserId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getMrchCode() {
        return mrchCode;
    }

    public void setMrchCode(Long mrchCode) {
        this.mrchCode = mrchCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id=").append(id);
        sb.append(", createTime=").append(createTime);
        sb.append(", createUser='").append(createUser).append('\'');
        sb.append(", createUserId=").append(createUserId);
        sb.append(", description='").append(description).append('\'');
        sb.append(", firstLogin='").append(firstLogin).append('\'');
        sb.append(", isDelete='").append(isDelete).append('\'');
        sb.append(", lastLoginTime=").append(lastLoginTime);
        sb.append(", lastModifyUser='").append(lastModifyUser).append('\'');
        sb.append(", lastModifyUserId=").append(lastModifyUserId);
        sb.append(", mobile='").append(mobile).append('\'');
        sb.append(", modifyTime=").append(modifyTime);
        sb.append(", password='").append(password).append('\'');
        sb.append(", realName='").append(realName).append('\'');
        sb.append(", mrchCode=").append(mrchCode);
        sb.append(", status='").append(status).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
