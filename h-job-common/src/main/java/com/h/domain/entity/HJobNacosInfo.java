package com.h.domain.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * h_job_nacos_info 实体类
 */
public class HJobNacosInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * nacos-service-name
     */
    private String nacosServiceName;

    /**
     * nacos-namespace
     */
    private String nacosNamespace;

    /**
     * nacos-group
     */
    private String nacosGroup;

    /**
     * IP地址列表，多地址逗号分隔
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNacosServiceName() {
        return nacosServiceName;
    }

    public void setNacosServiceName(String nacosServiceName) {
        this.nacosServiceName = nacosServiceName;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
