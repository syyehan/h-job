package com.h.logic.thread;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.h.config.HJobConfig;
import com.h.domain.entity.HJobInfo;
import com.h.domain.entity.HJobLog;
import com.h.logic.trigger.HJobTriggerHandler;
import com.h.utils.NacosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FailThread {

    private static final Logger log = LoggerFactory.getLogger(FailThread.class);

    private static final FailThread instance = new FailThread();

    private static volatile boolean failThreadStop = false;

    private static Thread failJobThread;

    public static FailThread getInstance(){
        return instance;
    }

    public void start() {
        failJobThread = new Thread(this::failExecuteRetry);
        failJobThread.setDaemon(true);
        failJobThread.setName("h-job, FailThread#failExecuteRetry");
        failJobThread.start();
    }

    public void failExecuteRetry(){
        while (!failThreadStop){

            if (!NacosUtils.isCalculateLeader()) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (Throwable e) {
                    if (!failThreadStop) {
                        log.error("FailThread,failExecuteRetry sleep :", e);
                    }
                }
                continue;
            }

            try {
                List<HJobLog> hJobLogList = HJobConfig.getHJobConfig().getJobLogMapper().selectAllFail(1000);
                if (CollUtil.isNotEmpty(hJobLogList)) {
                    for (HJobLog jobLog : hJobLogList){
                        if (ObjectUtil.isNotEmpty(jobLog.getFailRetryCount()) && jobLog.getFailRetryCount() > 0){
                            HJobTriggerHandler.handle(jobLog.getJobId(),jobLog.getFailRetryCount()-1,jobLog.getParam());
                            jobLog.setFailRetryFlag(1);
                            HJobConfig.getHJobConfig().getJobLogMapper().updateFailRetryFlag(jobLog);
                        }
                    }
                }
            }catch (Exception e){
                if (!failThreadStop) {
                    log.error("FailThread,failExecuteRetry error :", e);
                }
            }


            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (Throwable e) {
                if (!failThreadStop) {
                    log.error("FailThread,failExecuteRetry sleep :", e);
                }
            }
        }
    }


    public void stop(){
        failThreadStop = true;
        if (failJobThread != null) {
            failJobThread.interrupt();
        }
    }
}
