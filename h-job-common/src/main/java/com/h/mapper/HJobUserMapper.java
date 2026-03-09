package com.h.mapper;

import com.h.domain.entity.HJobUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * h_job_user Mapper 接口
 */
@Mapper
public interface HJobUserMapper {

    /**
     * 根据ID查询用户
     */
    HJobUser selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    HJobUser selectByUsername(@Param("userName") String userName);

    HJobUser selectByUsernameNotId(@Param("userName") String userName,@Param("id") Long id);

    HJobUser selectByJobToken(@Param("jobToken") String jobToken);

    /**
     * 查询所有用户
     */
    List<HJobUser> selectAll();

    /**
     * 分页查询用户
     */
    List<HJobUser> selectByPage(@Param("userName") String userName,@Param("role") Integer role,@Param("offset") int offset, @Param("size") int size);

    /**
     * 查询用户总数
     */
    int selectCount();

    /**
     * 新增用户
     */
    int insert(HJobUser user);

    /**
     * 更新用户
     */
    int update(HJobUser user);

    int updateToken(@Param("id") Long id , @Param("token") String token);

    /**
     * 根据ID删除用户
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户名删除用户
     */
    int deleteByUsername(String username);
}
