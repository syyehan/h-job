package com.h.logic.scheduler;

import com.h.logic.thread.FailThread;
import com.h.logic.thread.NacosListenerThread;
import com.h.logic.thread.HJobCoreThread;
import com.h.logic.thread.TaskExecuteThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobServerScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobServerScheduler.class);

    public void init() throws Exception {
        TaskExecuteThread.toStart();
        NacosListenerThread.getInstance().toStart();
        HJobCoreThread.getInstance().start();
        FailThread.getInstance().start();
        logger.info("========== init h-job start success.");
    }

    public void destroy() throws Exception {
        TaskExecuteThread.toStop();
        NacosListenerThread.getInstance().toStop();
        HJobCoreThread.getInstance().stop();
        FailThread.getInstance().stop();
        logger.info("========== init h-job stop success.");
    }

}
