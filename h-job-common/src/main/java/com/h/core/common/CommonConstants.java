package com.h.core.common;

import java.util.HashMap;
import java.util.Map;

public interface CommonConstants {
    String STATUS_NORMAL = "0";
    String UTF8 = "UTF-8";
    String CONTENT_TYPE = "application/json; charset=utf-8";
    Integer SUCCESS = 200;
    Integer FAIL = 500;

    String FAIL_MSG = "系统未知错误";

    Integer LOGIN_FAIL = 1000;

    String LOGIN_FAIL_MSG = "登录或校验失败";
    String HTTP = "http://";
    String HTTPS = "https://";

    String SECRET_KEY = "29426e6e-1830-45c5-83f3-41071511c7c1";



    /**
     * token过期时间，单位：毫秒, 7天
     */
    long TOKEN_KEY_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;
    /**
     * 用户id
     */
    String ATTR_USER_ID = "userId";
    /**
     * 用户名
     */
    String ATTR_USER_NAME = "userName";
    /**
     * 用户密码
     */
    String ATTR_USER_PASSWORD = "password";

    String ATTR_USER_TOKEN = "token";

    String ATTR_USER_PERMISSION = "permission";
    /**
     * 角色：0-普通用户、1-管理员
     */
    String ATTR_USER_ROLE = "role";

    int ROLE_ADMIN = 1;

    int ROLE_USER = 0;
    /**
     * 正常
     */
    Integer IS_DELETE_NORMAL = 0;
    /**
     * 已删除
     */
    Integer IS_DELETE_DELETE = 1;

    String START = "start";

    String END = "end";


}
