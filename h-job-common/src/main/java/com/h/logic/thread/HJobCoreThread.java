package com.h.logic.thread;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.h.config.HJobConfig;
import com.h.domain.entity.HJobInfo;
import com.h.utils.NacosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class HJobCoreThread {

    private static final Logger log = LoggerFactory.getLogger(HJobCoreThread.class);

    private static final HJobCoreThread instance = new HJobCoreThread();

    public static HJobCoreThread getInstance() {
        return instance;
    }

    private static volatile boolean calculateThreadStop = false;

    private static volatile boolean addThreadStop = false;
    private static Thread calculateThread;

    private static Thread addThread;
    private final ConcurrentMap<Integer, ConcurrentLinkedQueue<Long>> secondTask = new ConcurrentHashMap<>();


    public void start() {

        calculateThread = new Thread(this::calculateTask);
        calculateThread.setDaemon(true);
        calculateThread.setName("h-job, CalculateTaskThread#calculateThread");
        calculateThread.start();

        addThread = new Thread(this::addTask);
        addThread.setDaemon(true);
        addThread.setName("h-job, CalculateTaskThread#addThread");
        addThread.start();
        log.info("========== start CalculateTaskThread start success.");
    }


    private void calculateTask() {
        try {
            TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis() % 1000);
        } catch (Throwable e) {
            if (!calculateThreadStop) {
                log.error("calculateTask error:", e);
            }
        }
        int size = (HJobConfig.getHJobConfig().getYoungPoolMax() + HJobConfig.getHJobConfig().getOldPoolMax()) * 10;

        while (!calculateThreadStop) {
            if (!NacosUtils.isCalculateLeader()) {
                log.debug("========== calculateTask calculateLeader is false");
                try {
                    TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis() % 1000);
                } catch (Throwable e) {
                    if (!calculateThreadStop) {
                        log.error("calculateTask error:", e);
                    }
                }
                continue;
            }

            long start = System.currentTimeMillis();
            boolean hasTask = true;
            try {
                List<HJobInfo> jobInfoList = HJobConfig.getHJobConfig().getJobInfoMapper().selectListNextTime(System.currentTimeMillis() + 5000, size);
                log.debug("========== calculateTask jobInfoList size:{}", jobInfoList.size());
                if (CollUtil.isNotEmpty(jobInfoList)) {
                    for (HJobInfo jobInfo : jobInfoList) {
                        if (start > jobInfo.getJobNextTime() + 5000){
                            log.debug("========== calculateTask start >= jobInfo.getJobNextTime() + 5000 jobInfo:{}", jobInfo);
                            calculateNextTime(jobInfo, new Date());
                        }else if (start >= jobInfo.getJobNextTime()) {
                            log.debug("========== calculateTask start >= jobInfo.getJobNextTime() jobInfo:{}", jobInfo);
                            TaskExecuteThread.executeJob(jobInfo.getId(), -1, null);
                            calculateNextTime(jobInfo, new Date());
                        } else {
                            log.debug("========== calculateTask start < jobInfo.getJobNextTime() jobInfo:{}", jobInfo);
                            int second = (int) ((jobInfo.getJobNextTime() / 1000) % 60);
                            executeSecondTask(second, jobInfo.getId());
                            calculateNextTime(jobInfo, new Date(jobInfo.getJobNextTime()));
                        }
                        // update time
                        HJobConfig.getHJobConfig().getJobInfoMapper().updateTime(jobInfo);
                    }

                } else {
                    hasTask = false;
                }

            } catch (Throwable e) {
                if (!calculateThreadStop) {
                    log.error("========== calculateTask error:", e);
                }
            }

            long cost = System.currentTimeMillis() - start;

            if (cost < 1000) {
                try {
                    TimeUnit.MILLISECONDS.sleep((hasTask ? 1000 : 5000) - System.currentTimeMillis() % 1000);
                } catch (Throwable e) {
                    if (!calculateThreadStop) {
                        log.error("calculateTask error:", e);
                    }
                }
            }

        }
    }


    private void addTask() {
        while (!addThreadStop) {
            log.debug("========== addTask calculateLeader is leader start");
            try {
                TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
            } catch (Throwable e) {
                if (!addThreadStop) {
                    log.error("addTask error:", e);
                }
            }
            try {
                List<Long> addSecondTask = new ArrayList<>();
                int nowSecond = LocalDateTime.now().getSecond();
                ConcurrentLinkedQueue<Long> tmpTask = secondTask.remove(nowSecond%60);
                if (CollUtil.isNotEmpty(tmpTask)) {
                    addSecondTask.addAll(tmpTask);
                }
                ConcurrentLinkedQueue<Long> preTask = secondTask.remove((nowSecond+60-1)%60);
                if (CollUtil.isNotEmpty(preTask)) {
                    addSecondTask.addAll(preTask);
                }
                log.debug("========== addTask addSecondTask size:{}", addSecondTask.size());
                if (CollUtil.isNotEmpty(addSecondTask)){
                    for (Long jobId : addSecondTask) {
                        TaskExecuteThread.executeJob(jobId, -1, null);
                    }
                    addSecondTask.clear();
                }

            }catch (Throwable e){
                if (!addThreadStop) {
                    log.error("addTask error:", e);
                }
            }
        }
    }

    private void executeSecondTask(int second, Long jobId) {
        secondTask.computeIfAbsent(second, k -> new ConcurrentLinkedQueue<>())
                .offer(jobId);
    }

    private static void calculateNextTime(HJobInfo jobInfo, Date time) {
        try {
            // validate Cron
            if (!CronExpression.isValidExpression(jobInfo.getCron())) {
                log.error("calculateNextTime error, invalid cron expression, jobId={}, cron={}", jobInfo.getId(), jobInfo.getCron());
                jobInfo.setJobNextTime(0L);
                jobInfo.setJobLastTime(0L);
                return;
            }
            // parse Cron
            CronExpression cronExpression = CronExpression.parse(jobInfo.getCron());
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time.getTime()), ZoneId.systemDefault());
            // calculate next time
            LocalDateTime nextLocalDateTime = cronExpression.next(localDateTime);
            if (ObjectUtil.isNotEmpty(nextLocalDateTime)) {
                long nextTimeMillis = nextLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                jobInfo.setJobNextTime(nextTimeMillis);
                jobInfo.setJobLastTime(time.getTime());
            } else {
                jobInfo.setJobNextTime(0L);
                jobInfo.setJobLastTime(0L);
            }

        } catch (Exception e) {
            jobInfo.setJobNextTime(0L);
            jobInfo.setJobLastTime(0L);
            log.error("calculateNextTime error, jobId={}, cron={}", jobInfo.getId(), jobInfo.getCron(), e);
        }

    }

    public void stop() {
        calculateThreadStop = true;
        addThreadStop = true;
        if (calculateThread != null) {
            calculateThread.interrupt();
        }
        if (addThread != null) {
            addThread.interrupt();
        }
    }

    public static void main(String[] args) {
        List<Long> addSecondTask = new ArrayList<>();
        ConcurrentMap<Integer, ConcurrentLinkedQueue<Long>> secondTask = new ConcurrentHashMap<>();
        secondTask.computeIfAbsent(1, k -> new ConcurrentLinkedQueue<>())
                .offer(1L);
        secondTask.computeIfAbsent(1, k -> new ConcurrentLinkedQueue<>())
                .offer(2L);
        secondTask.computeIfAbsent(1, k -> new ConcurrentLinkedQueue<>())
                .offer(3L);

        addSecondTask.addAll(secondTask.remove(1));
        System.out.println(JSONUtil.toJsonStr(addSecondTask));
    }

}
