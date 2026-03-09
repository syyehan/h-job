package com.h.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.h.core.common.CommonConstants;
import com.h.core.common.R;
import com.h.component.TokenComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Token验证拦截器
 * @author hy
 */
@Component
public class TokenInterceptor implements HandlerInterceptor, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(TokenInterceptor.class);

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext( ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        
        String requestUri = request.getRequestURI();
        
        // get token
        String token = request.getHeader("hjob_token");
        log.debug(" requestUri: {}, token: {}", requestUri, token);
        //  token
        if (StrUtil.isBlank(token)) {
            log.warn("token is null, URI: {}", requestUri);
            response.getWriter().print(JSONUtil.toJsonStr(R.failed(CommonConstants.LOGIN_FAIL,"token is null")));
            return false;
        }
        TokenComponent tokenComponent = applicationContext.getBean(TokenComponent.class);

        // 验证token有效性
        if (!tokenComponent.validateToken(request,token)) {
            log.warn("validateToken failed, token: {}, URI: {}", token, requestUri);
            response.getWriter().print(JSONUtil.toJsonStr(R.failed(CommonConstants.LOGIN_FAIL,"validateToken failed")));
            return false;
        }
        //
        if (!TokenComponent.hasPermission(handler)){
            log.warn("check permission failed, token: {}, URI: {}", token, requestUri);
            response.getWriter().print(JSONUtil.toJsonStr(R.failed(CommonConstants.FAIL,"permission failed")));
            return false;
        }
        return true;
    }
}