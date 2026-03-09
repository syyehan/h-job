package com.h.mapper;

import com.h.domain.entity.HJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * h_job_log Mapper 接口
 */
@Mapper
public interface HJobLogMapper {

    /**
     * 根据ID查询日志
     */
    HJobLog selectById(Long id);

    /**
     * 根据jobId查询日志列表
     */
    List<HJobLog> selectByJobId(Integer jobId);

    /**
     * 根据执行状态查询日志列表
     */
    List<HJobLog> selectByExecutorCode(Integer executorCode);

    /**
     * 查询所有日志
     */
    List<HJobLog> selectAll();


    List<HJobLog> selectAllFail(@Param("size") int size);

    /**
     * 分页查询日志列表
     */
    List<HJobLog> selectPage(@Param("jobIds") List<Long> jobIds,@Param("executorCode") Integer executorCode,@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计所有日志数量
     */
    int selectPageCount(@Param("jobIds") List<Long> jobIds,@Param("executorCode") Integer executorCode);

    /**
     * 新增日志（自动设置创建时间和更新时间）
     */
    int insert(HJobLog jobLog);

    /**
     * 更新日志（自动设置更新时间）
     */
    int update(HJobLog jobLog);


    int updateFailRetryFlag(HJobLog jobLog);

    /**
     * 根据ID删除日志
     */
    int deleteById(Long id);

    /**
     * 根据jobId删除日志
     */
    int deleteByJobId(Integer jobId);

    /**
     * 清空日志
     */
    int clear();
}