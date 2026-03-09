package com.h.component;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.h.aop.PermissionNeed;
import com.h.config.ReqUtils;
import com.h.core.common.CommonConstants;
import com.h.core.common.R;
import com.h.domain.dto.UserLoginDTO;
import com.h.domain.entity.HJobUser;
import com.h.service.HJobUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenComponent {

    private static final Logger log = LoggerFactory.getLogger(TokenComponent.class);

    @Resource
    private HJobUserService hJobUserService;
    /**
     * 验证token
     * @param token
     * @return
     */
    public boolean validateToken(HttpServletRequest request,String token) {
        if (StrUtil.isEmpty(token)) {
            log.info("validateToken token is empty");
            return false;
        }
        // token
        boolean b = JWTUtil.verify(token, CommonConstants.SECRET_KEY.getBytes());
        if (!b){
            log.warn("verify token failed");
            return false;
        }
        JWT jwt = JWTUtil.parseToken(token);
        String timeStr = (String)jwt.getPayload("time");

        if (StrUtil.isEmpty(timeStr)){
            log.warn("timeStr time is empty");
            return false;
        }
        long time = Long.parseLong(timeStr);
        if (time < System.currentTimeMillis()){
            log.warn("token is expired");
            return false;
        }
        String uuid = (String)jwt.getPayload("uuid");
        HJobUser user = hJobUserService.getByJobToken(uuid);
        if (ObjectUtil.isEmpty(user)){
            log.warn("token user is failed");
            return false;
        }
        //init user info
        initUserInfo(request,jwt);
        return true;
    }

    /**
     * 初始化用户信息
     * @param request
     * @param jwt
     * @return
     */
    public static void initUserInfo(HttpServletRequest request,JWT jwt) {
        String userName = (String) jwt.getPayload(CommonConstants.ATTR_USER_NAME);
        String password = (String) jwt.getPayload(CommonConstants.ATTR_USER_PASSWORD);
        String role = (String) jwt.getPayload(CommonConstants.ATTR_USER_ROLE);
        String userId = (String) jwt.getPayload(CommonConstants.ATTR_USER_ID);
        String permission = (String) jwt.getPayload(CommonConstants.ATTR_USER_PERMISSION);
        request.setAttribute(CommonConstants.ATTR_USER_NAME,userName);
        request.setAttribute(CommonConstants.ATTR_USER_PASSWORD,password);
        request.setAttribute(CommonConstants.ATTR_USER_ROLE,Integer.valueOf(role));
        request.setAttribute(CommonConstants.ATTR_USER_ID,Long.valueOf(userId));
        request.setAttribute(CommonConstants.ATTR_USER_PERMISSION,permission);
    }

    public static String createToken(UserLoginDTO userLoginDTO,String uuid) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstants.ATTR_USER_NAME, userLoginDTO.getUserName());
        map.put(CommonConstants.ATTR_USER_PASSWORD,userLoginDTO.getPassword());
        map.put(CommonConstants.ATTR_USER_ROLE,userLoginDTO.getRole()+"");
        map.put(CommonConstants.ATTR_USER_ID,userLoginDTO.getUserId()+"");
        map.put(CommonConstants.ATTR_USER_PERMISSION,userLoginDTO.getPermission());
        map.put("uuid", uuid);
        map.put("time", String.valueOf(System.currentTimeMillis()+CommonConstants.TOKEN_KEY_EXPIRE_TIME));
        return JWTUtil.createToken(map, CommonConstants.SECRET_KEY.getBytes());
    }

    public static boolean hasPermission(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        boolean needAdmin = false;
        HandlerMethod method = (HandlerMethod)handler;
        PermissionNeed permission = method.getMethodAnnotation(PermissionNeed.class);
        if (permission!=null) {
            needAdmin = permission.admin();
        }
        if(needAdmin) {
            Integer role = ReqUtils.getRole();
            if (CommonConstants.ROLE_USER == role) {
                return false;
            } else if (CommonConstants.ROLE_ADMIN == role) {
                return true;
            }
        }
        return true;
    }


    public static void main(String[] args) {
        System.out.println(UUID.fastUUID().toString().replace("-", ""));
    }

}
