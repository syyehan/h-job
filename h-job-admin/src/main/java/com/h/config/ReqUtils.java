package com.h.config;

import com.h.core.common.CommonConstants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求工具类
 */
public class ReqUtils {


    /**
     * 获取header参数
     *
     * @param headerKey
     * @return
     */
    public static String getRequestHeader(String headerKey) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null != servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getHeader(headerKey);
        }
        return null;
    }

    /**
     * 获取Attribute参数
     *
     * @param attributeKey
     * @return
     */
    public static Object getRequestAttributes(String attributeKey) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null != servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getAttribute(attributeKey);
        }
        return null;
    }

    /**
     * 获取userId
     *
     * @return
     */
    public static Long getUserId() {
        return Long.valueOf((String) getRequestAttributes(CommonConstants.ATTR_USER_ID));
    }

    /**
     * 获取角色
     * @return
     */
    public static Integer getRole() {
        return (Integer) getRequestAttributes(CommonConstants.ATTR_USER_ROLE);
    }

    /**
     * 获取权限
     * @return
     */
    public static String getPermission() {
        return (String) getRequestAttributes(CommonConstants.ATTR_USER_PERMISSION);
    }
}