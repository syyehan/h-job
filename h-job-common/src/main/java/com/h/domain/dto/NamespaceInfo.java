package com.h.domain.dto;

public class NamespaceInfo {
    private String namespace;
    private String namespaceShowName;
    private String namespaceDesc;
    private Integer quota;
    private Integer configCount;
    private Integer type;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespaceShowName() {
        return namespaceShowName;
    }

    public void setNamespaceShowName(String namespaceShowName) {
        this.namespaceShowName = namespaceShowName;
    }

    public String getNamespaceDesc() {
        return namespaceDesc;
    }

    public void setNamespaceDesc(String namespaceDesc) {
        this.namespaceDesc = namespaceDesc;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Integer getConfigCount() {
        return configCount;
    }

    public void setConfigCount(Integer configCount) {
        this.configCount = configCount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "NamespaceInfo{" +
                "namespace='" + namespace + '\'' +
                ", namespaceShowName='" + namespaceShowName + '\'' +
                ", namespaceDesc='" + namespaceDesc + '\'' +
                ", quota=" + quota +
                ", configCount=" + configCount +
                ", type=" + type +
                '}';
    }
}
