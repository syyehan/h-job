package com.h.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.h.config.ReqUtils;
import com.h.core.common.CommonConstants;
import com.h.core.exception.BaseBizException;
import com.h.domain.entity.HJobInfo;
import com.h.domain.entity.HJobServerAddress;
import com.h.logic.trigger.HJobTriggerHandler;
import com.h.mapper.HJobInfoMapper;
import com.h.mapper.HJobServerAddressMapper;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * h_job_info Service 实现类
 */
@Service
public class HJobInfoService {

    @Resource
    private HJobInfoMapper hJobInfoMapper;

    @Resource
    private HJobServerAddressMapper hJobServerAddressMapper;

    /**
     * 根据ID查询任务
     */
    public HJobInfo getById(Long id) {
        return hJobInfoMapper.selectById(id);
    }


    /**
     * 分页查询任务
     */
    public Map<String, Object> listByPage(String jobDesc, Long jobServerId, Integer jobStatus, Integer page, Integer size) {

        List<Long> jobServerIds = new ArrayList<>();
        if (CommonConstants.ROLE_USER == ReqUtils.getRole()) {
            String[] permissionArr = ReqUtils.getPermission().split(",");
            for (String idStr : permissionArr) {
                if (!idStr.trim().isEmpty()) {
                    jobServerIds.add(Long.valueOf(idStr.trim()));
                }
            }
            if (ObjectUtil.isNotEmpty(jobServerId)) {
                if (jobServerIds.contains(jobServerId)) {
                    jobServerIds.clear();
                    jobServerIds.add(jobServerId);
                } else {
                    throw new BaseBizException("无权限访问");
                }
            }

        } else if (CommonConstants.ROLE_ADMIN == ReqUtils.getRole()) {
            if (ObjectUtil.isNotEmpty(jobServerId)) {
                jobServerIds.add(jobServerId);
            }
        }

        int offset = (page - 1) * size;
        List<HJobInfo> list = hJobInfoMapper.selectByPage(jobDesc, jobServerIds, jobStatus, offset, size);
        Integer total = hJobInfoMapper.selectCount(jobDesc, jobServerIds, jobStatus);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    /**
     * 新增任务
     */
    public void save(HJobInfo jobInfo) {
        check(jobInfo);
        HJobInfo old = hJobInfoMapper.selectByJobDesc(jobInfo.getJobDesc());
        if (ObjectUtil.isNotEmpty(old)){
            throw new BaseBizException("任务已存在");
        }
        boolean b = CronExpression.isValidExpression(jobInfo.getCron());
        if (!b) {
            throw new BaseBizException("cron表达式不合法");
        }
        // parse Cron
        CronExpression cronExpression = CronExpression.parse(jobInfo.getCron());
        // calculate next time
        LocalDateTime nextLocalDateTime = cronExpression.next(LocalDateTime.now());
        long nextTimeMillis = nextLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        jobInfo.setJobNextTime(nextTimeMillis);
        jobInfo.setJobStatus(0);
        jobInfo.setJobLastTime(0L);
        hJobInfoMapper.insert(jobInfo);
    }

    /**
     * 更新任务
     */
    public void update(HJobInfo jobInfo) {
        check(jobInfo);
        HJobInfo info = hJobInfoMapper.selectById(jobInfo.getId());
        if (ObjectUtil.isEmpty(info)){
            throw new BaseBizException("任务不存在");
        }
        HJobInfo other = hJobInfoMapper.selectByJobDescNotId(jobInfo.getJobDesc(),jobInfo.getId());
        if (ObjectUtil.isNotEmpty(other)){
            throw new BaseBizException("任务已存在");
        }
        hJobInfoMapper.update(jobInfo);
    }

    public void changeStatus(HJobInfo jobInfo) {
        hJobInfoMapper.changeStatus(jobInfo);
    }

    /**
     * 根据ID删除任务
     */
    public int deleteById(Long id) {
        return hJobInfoMapper.deleteById(id);
    }

    /**
     * 立即执行（异步）
     *
     * @param id
     */
    public void execute(Long id, String param) {
        CompletableFuture.runAsync(() -> {
            HJobTriggerHandler.handle(id, -1, param);
        });
    }

    public List<String> getNextTime(Long id) {
        HJobInfo jobInfo = hJobInfoMapper.selectById(id);
        String cron = jobInfo.getCron();
        if (!CronExpression.isValidExpression(cron)) {
            throw new BaseBizException("cron表达式不合法");
        }
        List<String> nextTimeList = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.now();
        for (int i = 0; i < 6; i++) {
            // parse Cron
            CronExpression cronExpression = CronExpression.parse(cron);
            // calculate next time
            LocalDateTime nextLocalDateTime = cronExpression.next(startDate);

            if (ObjectUtil.isNotEmpty(nextLocalDateTime)) {
                nextTimeList.add(nextLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            startDate = nextLocalDateTime;
        }
        return nextTimeList;
    }


    private void check(HJobInfo jobInfo) {
        if (StrUtil.isBlank(jobInfo.getJobDesc())) {
            throw new BaseBizException("任务描述不能为空");
        }
        if (ObjectUtil.isEmpty(jobInfo.getJobServerId())) {
            throw new BaseBizException("任务服务地址不能为空");
        }
        if (StrUtil.isBlank(jobInfo.getCreateUser())) {
            throw new BaseBizException("创建人不能为空");
        }
        if (StrUtil.isBlank(jobInfo.getCron())) {
            throw new BaseBizException("cron表达式不能为空");
        }
        if (StrUtil.isBlank(jobInfo.getRouteStrategy())) {
            throw new BaseBizException("路由策略不能为空");
        }
        if (StrUtil.isBlank(jobInfo.getPath())) {
            throw new BaseBizException("调用路径不能为空");
        }
        if (StrUtil.isBlank(jobInfo.getMethod())) {
            throw new BaseBizException("调用方式不能为空");
        }
        if(ObjectUtil.isEmpty(jobInfo.getExecutorTimeout()) || jobInfo.getExecutorTimeout() < 1){
            jobInfo.setExecutorTimeout(30);
        }
        if (ObjectUtil.isEmpty(jobInfo.getExecutorFailRetryCount()) || jobInfo.getExecutorFailRetryCount() < 0){
            jobInfo.setExecutorFailRetryCount(0);
        }
//        if (StrUtil.isBlank(jobInfo.getParam())) {
//            throw new BaseBizException("调用参数不能为空");
//        }
        HJobServerAddress jobServerAddress = hJobServerAddressMapper.selectById(jobInfo.getJobServerId());
        if (ObjectUtil.isEmpty(jobServerAddress)) {
            throw new BaseBizException("服务地址不存在");
        }
    }
}
