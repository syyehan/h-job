package com.h.controller;

import cn.hutool.core.lang.UUID;
import com.h.core.common.CommonConstants;
import com.h.core.common.R;
import com.h.domain.dto.UserLoginDTO;
import com.h.component.TokenComponent;
import com.h.service.HJobUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yehan
 */

@RestController
@Api(tags = "用户登录")
public class LoginController {

    @Value("${h.job.user.name}")
    private String userName;
    @Value("${h.job.user.password}")
    private String password;

    @Resource
    private TokenComponent tokenComponent;

    @Resource
    private HJobUserService hJobUserService;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public R<Map<String,Object>> userLogin(@RequestBody UserLoginDTO userLoginDTO) {

        if (hJobUserService.checkLogin(userLoginDTO)){
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String token = TokenComponent.createToken(userLoginDTO,uuid);
            hJobUserService.updateToken(userLoginDTO.getUserId(),uuid);
            Map<String,Object> map = new HashMap<>();
            map.put("token",token);
            map.put("role",userLoginDTO.getRole());
            map.put("userName",userLoginDTO.getUserName());
            return R.ok(map);
        }else {
            return R.failed("用户名或密码错误");
        }
    }

    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public R<String> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(CommonConstants.ATTR_USER_ID);
        hJobUserService.updateToken(userId,"1");
        return R.ok();
    }

}
