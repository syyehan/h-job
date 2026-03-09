package com.h.mapper;

import com.h.domain.entity.HJobServerAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * h_job_server_address Mapper 接口
 */
@Mapper
public interface HJobServerAddressMapper {

    /**
     * 根据ID查询服务地址
     */
    HJobServerAddress selectById(Long id);


    HJobServerAddress selectByServiceName(@Param("serviceName") String serviceName);

    HJobServerAddress selectByServiceNameNotId(@Param("serviceName") String serviceName,@Param("id") Long id);


    /**
     * 分页查询服务地址
     */
    List<HJobServerAddress> selectByPage(@Param("serviceName") String serviceName, 
                                         @Param("title") String title,
                                         @Param("offset") Integer offset,
                                         @Param("size") Integer size,
                                         @Param("ids")List<Long> ids);
    
    /**
     * 查询总数
     */
    Integer selectCount(@Param("serviceName") String serviceName, 
                       @Param("title") String title,@Param("ids")List<Long> ids);

    /**
     * 查询所有服务地址
     */
    List<HJobServerAddress> selectAll();

    /**
     * 新增服务地址
     */
    int insert(HJobServerAddress serverAddress);

    /**
     * 更新服务地址
     */
    int update(HJobServerAddress serverAddress);

    /**
     * 根据ID删除服务地址
     */
    int deleteById(Integer id);


}
