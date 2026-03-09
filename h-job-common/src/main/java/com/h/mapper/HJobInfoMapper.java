package com.h.mapper;

import com.h.domain.entity.HJobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * h_job_info Mapper 接口
 */
@Mapper
public interface HJobInfoMapper {

    /**
     * 根据ID查询任务
     */
    HJobInfo selectById(Long id);

    HJobInfo selectByJobDesc(@Param("jobDesc")String jobDesc);

    HJobInfo selectByJobDescNotId(@Param("jobDesc")String jobDesc,@Param("id")Long id);

    /**
     * 根据jobServerId查询任务列表
     */
    List<HJobInfo> selectByJobServerId(Long jobServerId);


    List<HJobInfo> selectByJobServerIds(@Param("jobServerIds") List<Long> jobServerIds);

    /**
     * 根据调度状态查询任务列表
     */
    List<HJobInfo> selectByTriggerStatus(Integer triggerStatus);

    /**
     * 查询所有任务
     */
    List<HJobInfo> selectAll();

    /**
     * 分页查询任务
     */
    List<HJobInfo> selectByPage(@Param("jobDesc") String jobDesc,
                                 @Param("jobServerIds") List<Long> jobServerIds,
                                 @Param("jobStatus") Integer jobStatus,
                                 @Param("offset") Integer offset,
                                 @Param("size") Integer size);
    
    /**
     * 查询任务总数
     */
    Integer selectCount(@Param("jobDesc") String jobDesc,
                       @Param("jobServerIds") List<Long> jobServerIds,
                       @Param("jobStatus") Integer jobStatus);

    List<HJobInfo> selectListNextTime(@Param("nextTime") long nextTime, @Param("size") int size);

    /**
     * 新增任务
     */
    int insert(HJobInfo jobInfo);

    /**
     * 更新任务
     */
    int update(HJobInfo jobInfo);


    int changeStatus(HJobInfo jobInfo);

    /**
     * 根据ID删除任务
     */
    int deleteById(Long id);

    /**
     * 根据jobServerId删除任务
     */
    int deleteByJobServerId(Long jobServerId);

    int updateTime(HJobInfo jobInfo);

}
