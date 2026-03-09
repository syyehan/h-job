package com.h.core.enums;


import com.h.core.common.CommonConstants;

public enum CommonErrorCodeEnums {
    SYSTEM_UNKNOWN_ERROR(CommonConstants.FAIL, CommonConstants.FAIL_MSG),

    LOGIN_ERROR(CommonConstants.LOGIN_FAIL, CommonConstants.LOGIN_FAIL_MSG),
    CLIENT_HTTP_METHOD_ERROR(1001, "客户端HTTP请求方法错误"),
    CLIENT_REQUEST_BODY_CHECK_ERROR(1002, "客户端请求体参数校验不通过"),
    CLIENT_REQUEST_BODY_FORMAT_ERROR(1003, "客户端请求体JSON格式错误或字段类型不匹配"),
    CLIENT_PATH_VARIABLE_ERROR(1004, "客户端URL中的参数类型错误"),
    CLIENT_REQUEST_PARAM_CHECK_ERROR(1005, "客户端请求参数校验不通过"),
    CLIENT_REQUEST_PARAM_REQUIRED_ERROR(1006, "客户端请求缺少必填的参数"),
    SERVER_REQUEST_ERROR(2000, "业务方法参数检查不通过");

    private Integer errorCode;
    private String errorMsg;

    private CommonErrorCodeEnums(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
