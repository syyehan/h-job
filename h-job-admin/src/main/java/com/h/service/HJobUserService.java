package com.h.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.h.core.common.CommonConstants;
import com.h.core.exception.BaseBizException;
import com.h.domain.dto.UserLoginDTO;
import com.h.domain.entity.HJobUser;
import com.h.mapper.HJobUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * h_job_user Service 实现类
 */
@Service
public class HJobUserService {

    @Resource
    private HJobUserMapper hJobUserMapper;

    public HJobUser getById(Long id) {
        return hJobUserMapper.selectById(id);
    }

    public HJobUser getByUsername(String username) {
        return hJobUserMapper.selectByUsername(username);
    }

    public HJobUser getByJobToken(String jobToken) {
        return hJobUserMapper.selectByJobToken(jobToken);
    }

    public boolean checkLogin(UserLoginDTO userLoginDTO) {
        boolean b = false;
        HJobUser user = hJobUserMapper.selectByUsername(userLoginDTO.getUserName());
        if (ObjectUtil.isEmpty(user)){
            return b;
        }
        if (user.getPassword().equals(userLoginDTO.getPassword())){
            b = true;
            userLoginDTO.setUserId(user.getId());
            userLoginDTO.setRole(user.getRole());
            userLoginDTO.setPermission(user.getPermission());
        }
        return b;
    }
    public List<HJobUser> listAll() {
        return hJobUserMapper.selectAll();
    }

    public Map<String, Object> listByPage(String userName,Integer role,int page, int size) {
        int offset = (page - 1) * size;
        List<HJobUser> list = hJobUserMapper.selectByPage(userName,role,offset, size);
        int total = hJobUserMapper.selectCount();
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    public int save(HJobUser user) {
        checkUser(user);
        user.setJobToken("0");
        user.setCreateTime(DateUtil.date());
        user.setUpdateTime(DateUtil.date());
        HJobUser old = this.getByUsername(user.getUserName());
        if (ObjectUtil.isNotEmpty(old)){
            throw new BaseBizException("用户名已存在");
        }
        return hJobUserMapper.insert(user);
    }

    public int update(HJobUser user) {
        checkUser(user);
        HJobUser old = this.getById(user.getId());
        if (ObjectUtil.isEmpty(old)){
            throw new BaseBizException("用户不存在");
        }
        HJobUser nameUser = hJobUserMapper.selectByUsernameNotId(user.getUserName(),user.getId());
        if (ObjectUtil.isNotEmpty(nameUser)){
            throw new BaseBizException("用户名已存在");
        }
        user.setUpdateTime(DateUtil.date());
        return hJobUserMapper.update(user);
    }

    public int updateToken(Long id ,String token) {
        return hJobUserMapper.updateToken(id,token);
    }

    public int deleteById(Long id) {
        return hJobUserMapper.deleteById(id);
    }



    private void checkUser(HJobUser user) {
        if (ObjectUtil.isEmpty(user.getUserName())){
            throw new BaseBizException("用户名不能为空");
        }
        if (ObjectUtil.isEmpty(user.getPassword())){
            throw new BaseBizException("密码不能为空");
        }

        if (ObjectUtil.isEmpty(user.getRole())){
            throw new BaseBizException("角色不能为空");
        }
        if (CommonConstants.ROLE_USER == user.getRole() && ObjectUtil.isEmpty(user.getPermission())){
            throw new BaseBizException("权限不能为空");
        }
    }
}
