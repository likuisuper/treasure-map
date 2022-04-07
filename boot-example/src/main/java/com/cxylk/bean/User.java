package com.cxylk.bean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

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
