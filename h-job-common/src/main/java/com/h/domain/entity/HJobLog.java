package com.h.domain.entity;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * h_job_log 实体类
 */
public class HJobLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * h_job_server_address-主键ID
     */
    private Long jobServerId;

    /**
     * h_job_info-主键ID
     */
    private Long jobId;

    /**
     * 本次执行-地址
     */
    private String address;

    /**
     * 本次执行-参数
     */
    private String param;

    /**
     * 本次执行-分片参数，格式如 1-2-3
     */
    private String sharding;

    /**
     * 失败重试次数
     */
    private Integer failRetryCount;

    /**
     * 返回-code
     */
    private Integer resultCode;

    /**
     * 返回-信息
     */
    private String resultMsg;

    /**
     * 执行-时间
     */
    private Date executorDate;

    /**
     * 执行-状态，0-成功、1-进行中、2-失败
     */
    private Integer executorCode;

    /**
     * 重试-状态，0-未重试、1-已经重试
     */
    private Integer failRetryFlag;

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

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getSharding() {
        return sharding;
    }

    public void setSharding(String sharding) {
        this.sharding = sharding;
    }

    public Integer getFailRetryCount() {
        return failRetryCount;
    }

    public void setFailRetryCount(Integer failRetryCount) {
        this.failRetryCount = failRetryCount;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getExecutorDate() {
        return DateUtil.format(executorDate, DatePattern.NORM_DATETIME_PATTERN);
    }

    public void setExecutorDate(Date executorDate) {
        this.executorDate = executorDate;
    }

    public Integer getExecutorCode() {
        return executorCode;
    }

    public void setExecutorCode(Integer executorCode) {
        this.executorCode = executorCode;
    }

    public Integer getFailRetryFlag() {
        return failRetryFlag;
    }

    public void setFailRetryFlag(Integer failRetryFlag) {
        this.failRetryFlag = failRetryFlag;
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
