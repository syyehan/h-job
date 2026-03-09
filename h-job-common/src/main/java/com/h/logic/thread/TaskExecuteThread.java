package com.h.logic.thread;

import com.h.config.HJobConfig;
import com.h.logic.manager.MiddleThreadPoolManager;
import com.h.logic.manager.OldThreadPoolManager;
import com.h.logic.manager.YoungThreadPoolManager;
import com.h.logic.trigger.HJobTriggerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskExecuteThread {
    private static final Logger log = LoggerFactory.getLogger(TaskExecuteThread.class);

    private static final TaskExecuteThread taskExecuteThread = new TaskExecuteThread();

    public static void toStart() {
        taskExecuteThread.start();
    }

    public static void toStop() {
        taskExecuteThread.stop();
    }

    private ThreadPoolExecutor youngThreadPoolManager = null;
    private ThreadPoolExecutor oldThreadPoolManager = null;

    private ThreadPoolExecutor middleThreadPoolManager = null;

    // job timeout count
    private volatile long FIVE_MIN_TIME = System.currentTimeMillis() / 300000;
    private volatile ConcurrentMap<Long, AtomicInteger> timeoutCountMap = new ConcurrentHashMap<>();


    public void start() {
        youngThreadPoolManager = YoungThreadPoolManager.pool;
        middleThreadPoolManager = MiddleThreadPoolManager.pool;
        oldThreadPoolManager = OldThreadPoolManager.pool;
    }

    public void stop() {
        YoungThreadPoolManager.pool.shutdown();
        MiddleThreadPoolManager.pool.shutdown();
        OldThreadPoolManager.pool.shutdown();
    }

    /**
     * addJob
     *
     * @param jobId
     * @param failRetryCount
     * @param param
     */
    public void addJob(final long jobId,
                       final int failRetryCount,
                       final String param) {

        ThreadPoolExecutor threadPoolExecutor;
        boolean b = timeoutCountMap.containsKey(jobId);
        if (b) {
            threadPoolExecutor = oldThreadPoolManager;
        } else {
            int youngActiveCount = youngThreadPoolManager.getActiveCount();
            int middleActiveCount = middleThreadPoolManager.getActiveCount();
            int youngQueueSize = youngThreadPoolManager.getQueue().size();
            int middleQueueSize = middleThreadPoolManager.getQueue().size();
            int youngPoolMax = HJobConfig.getHJobConfig().getYoungPoolMax();
            int youngPoolCore = HJobConfig.getHJobConfig().getYoungPoolCore();
            int youngPoolCapacity = HJobConfig.getHJobConfig().getYoungPoolCapacity();
            int middlePoolCore = HJobConfig.getHJobConfig().getMiddlePoolCore();
            int middlePoolCapacity = HJobConfig.getHJobConfig().getMiddlePoolCapacity();

            if (youngActiveCount >= youngPoolMax){
                threadPoolExecutor = middleThreadPoolManager;
            }else if (youngActiveCount >= youngPoolCore
                   && middleActiveCount < middlePoolCore){
                    threadPoolExecutor = middleThreadPoolManager;
            }else if(youngActiveCount >= youngPoolCore
                    && youngQueueSize >= youngPoolCapacity
                    && middleQueueSize >= middlePoolCapacity){
                threadPoolExecutor = youngThreadPoolManager;
            } else if (youngActiveCount >= youngPoolCore
                    && youngQueueSize >= youngPoolCapacity) {
                threadPoolExecutor = middleThreadPoolManager;
            } else {
                threadPoolExecutor = youngThreadPoolManager;
            }

        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();

                try {
                    HJobTriggerHandler.handle(jobId, failRetryCount, param);
                } catch (Throwable e) {
                    log.error("========== TaskExecuteThread executeJob addJob error:", e);
                } finally {
                    long five_min_time_now = System.currentTimeMillis() / 300000;
                    if (FIVE_MIN_TIME != five_min_time_now) {
                        FIVE_MIN_TIME = five_min_time_now;
                        timeoutCountMap.clear();
                    }
                    long cost = System.currentTimeMillis() - start;
                    if (cost > 5000) {
                        AtomicInteger timeoutCount = timeoutCountMap.putIfAbsent(jobId, new AtomicInteger(1));
                        if (timeoutCount != null) {
                            int count = timeoutCount.incrementAndGet();
                            log.info("========== TaskExecuteThread executeJob addJob jobId={} timeoutCount:{}", jobId, count);
                        }
                    }

                }
            }
        });
    }

    /**
     * executeJob
     *
     * @param jobId
     * @param failRetryCount
     * @param param
     */
    public static void executeJob(long jobId, int failRetryCount, String param) {
        taskExecuteThread.addJob(jobId, failRetryCount, param);
    }
}
