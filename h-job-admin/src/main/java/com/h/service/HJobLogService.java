package com.h.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.h.config.ReqUtils;
import com.h.core.common.CommonConstants;
import com.h.core.exception.BaseBizException;
import com.h.domain.entity.HJobInfo;
import com.h.domain.entity.HJobLog;
import com.h.mapper.HJobInfoMapper;
import com.h.mapper.HJobLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * h_job_log Service 实现类
 */
@Service
public class HJobLogService {

    @Resource
    private HJobLogMapper hJobLogMapper;

    @Resource
    private HJobInfoMapper hJobInfoMapper;

    /**
     * 根据ID查询日志
     */
    public HJobLog getById(Long id) {
        return hJobLogMapper.selectById(id);
    }

    /**
     * 根据jobId查询日志列表
     */
    public List<HJobLog> listByJobId(Integer jobId) {
        return hJobLogMapper.selectByJobId(jobId);
    }

    /**
     * 根据执行状态查询日志列表
     */
    public List<HJobLog> listByExecutorCode(Integer executorCode) {
        return hJobLogMapper.selectByExecutorCode(executorCode);
    }

    /**
     * 分页查询日志列表
     */
    public Map<String, Object> listByPage(Long jobId, Integer executorCode, Integer page, Integer size) {

        List<Long> jobIds = new ArrayList<>();

        List<Long> jobServerIds = new ArrayList<>();
        if (CommonConstants.ROLE_USER == ReqUtils.getRole()){
            String[] permissionArr = ReqUtils.getPermission().split(",");
            for (String idStr : permissionArr) {
                if (!idStr.trim().isEmpty()) {
                    jobServerIds.add(Long.valueOf(idStr.trim()));
                }
            }
            List<HJobInfo> jobInfos = hJobInfoMapper.selectByJobServerIds(jobServerIds);
            if (CollUtil.isNotEmpty(jobInfos)){
                for (HJobInfo jobInfo : jobInfos){
                    jobIds.add(jobInfo.getId());
                }
            }
            if (ObjectUtil.isNotEmpty(jobId)){
                if (jobIds.contains(jobId)) {
                    jobIds.clear();
                    jobIds.add(jobId);
                }else {
                    throw new BaseBizException("无权限访问");
                }
            }
        }else if (CommonConstants.ROLE_ADMIN == ReqUtils.getRole()){
            if (ObjectUtil.isNotEmpty(jobId)) {
                jobIds.add(jobId);
            }
        }


        int offset = (page - 1) * size;
        
        // 暂时只实现基础的分页查询所有日志
        List<HJobLog> list = hJobLogMapper.selectPage(jobIds,executorCode,offset, size);
        int total = hJobLogMapper.selectPageCount(jobIds,executorCode);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 新增日志
     */
    public void save(HJobLog jobLog) {
        hJobLogMapper.insert(jobLog);
    }

    /**
     * 更新日志
     */
    public void update(HJobLog jobLog) {
        hJobLogMapper.update(jobLog);
    }

    /**
     * 根据ID删除日志
     */
    public int deleteById(Long id) {
        return hJobLogMapper.deleteById(id);
    }

    /**
     * 根据jobId删除日志
     */
    public int deleteByJobId(Integer jobId) {
        return hJobLogMapper.deleteByJobId(jobId);
    }

    /**
     * 清空日志
     */
    public int clear() {
        return hJobLogMapper.clear();
    }
}