package com.h.domain.entity;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * h_job_server_address 实体类
 */
public class HJobServerAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 服务名称(nacos中的服务名),或者IP地址
     */
    private String serviceName;

    /**
     * 业务名称
     */
    private String title;

    /**
     * 服务地址类型：0=自动注册(nacos)、1=手动录入(支持IP)
     */
    private Integer addressType;

    /**
     * nacos-namespace
     */
    private String nacosNamespace;

    /**
     * nacos-group
     */
    private String nacosGroup;

    /**
     * 服务地址列表，多地址逗号分隔
     */
    private String addressList;

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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getAddressType() {
        return addressType;
    }

    public void setAddressType(Integer addressType) {
        this.addressType = addressType;
    }

    public String getNacosNamespace() {
        return nacosNamespace;
    }

    public void setNacosNamespace(String nacosNamespace) {
        this.nacosNamespace = nacosNamespace;
    }

    public String getNacosGroup() {
        return nacosGroup;
    }

    public void setNacosGroup(String nacosGroup) {
        this.nacosGroup = nacosGroup;
    }

    public String getAddressList() {
        return addressList;
    }

    public void setAddressList(String addressList) {
        this.addressList = addressList;
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
