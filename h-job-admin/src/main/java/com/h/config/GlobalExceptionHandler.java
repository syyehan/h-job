package com.h.config;


import com.h.core.common.R;
import com.h.core.enums.CommonErrorCodeEnums;
import com.h.core.exception.BaseBizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 全局异常拦截
 * </p>
 *
 * @author: hy
 **/

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(value = Exception.class)
    public R exceptionHandler(HttpServletRequest request, Exception ex){

        // BizException
        if(ex instanceof BaseBizException){
            BaseBizException bizException = (BaseBizException) ex;
            log.warn("code:{},message:{}", bizException.getErrorCode(), bizException.getMessage());
            return R.failed(bizException.getErrorMsg()).setCode(bizException.getErrorCode());
        }

        if (ex instanceof MethodArgumentNotValidException){
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) ex;
            List<ObjectError> allErrors = exception.getBindingResult().getAllErrors();
            String message = allErrors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";"));
            log.warn("message:{}", message);
            return R.failed(message).setCode(CommonErrorCodeEnums.CLIENT_HTTP_METHOD_ERROR.getErrorCode());
        }

        // 其余异常简单返回为服务器异常
        log.error(ex.getMessage(),ex);
        return R.failed();

    }

}
