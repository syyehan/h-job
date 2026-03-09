package com.h.domain.entity;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * h_job_user 实体类
 */
public class HJobUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 账号
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色：0-普通用户、1-管理员
     */
    private Integer role;

    /**
     * 权限：执行器ID列表，多个逗号分割
     */
    private String permission;

    /**
     * 登录 token
     */
    private String jobToken;

    private Date createTime;

    private Date updateTime;

    public String getJobToken() {
        return jobToken;
    }

    public void setJobToken(String jobToken) {
        this.jobToken = jobToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getCreateTime() {
        return DateUtil.format(createTime, DatePattern.NORM_DATETIME_PATTERN);
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return DateUtil.format(updateTime, DatePattern.NORM_DATETIME_PATTERN);
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
