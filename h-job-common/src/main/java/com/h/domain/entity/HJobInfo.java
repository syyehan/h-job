package com.h.domain.entity;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * h_job_info 实体类
 */
public class HJobInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * h_job_server_address主键ID
     */
    private Long jobServerId;

    /**
     * 任务描述
     */
    private String jobDesc;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 路由策略
     */
    private String routeStrategy;

    /**
     * 调用路径
     */
    private String path;

    /**
     * 调用参数
     */
    private String param;

    /**
     * 调用方式 POST/GET
     */
    private String method;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    /**
     * 失败重试次数
     */
    private Integer executorFailRetryCount;

    /**
     * 调度状态：0-停止，1-运行
     */
    private Integer jobStatus;

    /**
     * 上次调度时间
     */
    private Long jobLastTime;

    /**
     * 下次调度时间
     */
    private Long jobNextTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobServerId() {
        return jobServerId;
    }

    public void setJobServerId(Long jobServerId) {
        this.jobServerId = jobServerId;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getRouteStrategy() {
        return routeStrategy;
    }

    public void setRouteStrategy(String routeStrategy) {
        this.routeStrategy = routeStrategy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public Integer getExecutorFailRetryCount() {
        return executorFailRetryCount;
    }

    public void setExecutorFailRetryCount(Integer executorFailRetryCount) {
        this.executorFailRetryCount = executorFailRetryCount;
    }

    public Integer getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Integer jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Long getJobLastTime() {
        return jobLastTime;
    }

    public void setJobLastTime(Long jobLastTime) {
        this.jobLastTime = jobLastTime;
    }

    public Long getJobNextTime() {
        return jobNextTime;
    }

    public void setJobNextTime(Long jobNextTime) {
        this.jobNextTime = jobNextTime;
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
