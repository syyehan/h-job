package com.h.core.exception;


import com.h.core.enums.CommonErrorCodeEnums;

public class BaseBizException extends RuntimeException {
    private Integer errorCode;
    private String errorMsg;

    public BaseBizException(String errorMsg) {
        super(errorMsg);
        this.errorCode = CommonErrorCodeEnums.SYSTEM_UNKNOWN_ERROR.getErrorCode();
        this.errorMsg = errorMsg;
    }

    public BaseBizException(Integer errorCode, String errorMsg) {
        super(errorMsg);
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
