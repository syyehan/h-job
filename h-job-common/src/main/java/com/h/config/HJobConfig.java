package com.h.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.h.logic.scheduler.JobServerScheduler;
import com.h.mapper.HJobInfoMapper;
import com.h.mapper.HJobLogMapper;
import com.h.mapper.HJobServerAddressMapper;
import com.h.utils.NacosUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Component
public class HJobConfig implements InitializingBean, DisposableBean {

    private static HJobConfig hJobConfig = null;

    public static HJobConfig getHJobConfig() {
        return hJobConfig;
    }
    private JobServerScheduler jobServerScheduler;

    private NacosUtils nacosUtils;
    @Override
    public void afterPropertiesSet() throws Exception {
        hJobConfig = this;
        jobServerScheduler = new JobServerScheduler();
        jobServerScheduler.init();
    }

    @Override
    public void destroy() throws Exception {
        jobServerScheduler.destroy();
    }

    @Resource
    private HJobServerAddressMapper serverAddressMapper;

    @Resource
    private DataSource dataSource;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Resource
    private HJobInfoMapper jobInfoMapper;

    @Resource
    private HJobLogMapper jobLogMapper;
    @Value("${server.port:9009}")
    private Integer serverPort;

    @Value("${h.job.young.pool.core:10}")
    private int youngPoolCore;
    @Value("${h.job.young.pool.capacity:2000}")
    private int youngPoolCapacity;
    @Value("${h.job.young.pool.max:200}")
    private int youngPoolMax;
    @Value("${h.job.middle.pool.core:10}")
    private int middlePoolCore;
    @Value("${h.job.middle.pool.capacity:2000}")
    private int middlePoolCapacity;
    @Value("${h.job.middle.pool.max:200}")
    private int middlePoolMax;
    @Value("${h.job.old.pool.core:10}")
    private int oldPoolCore;
    @Value("${h.job.old.pool.capacity:2000}")
    private int oldPoolCapacity;
    @Value("${h.job.old.pool.max:200}")
    private int oldPoolMax;

    public int getYoungPoolCore() {
        return youngPoolCore;
    }

    public int getYoungPoolCapacity() {
        return youngPoolCapacity;
    }

    public int getYoungPoolMax() {
        return youngPoolMax;
    }

    public int getMiddlePoolCore() {
        return middlePoolCore;
    }

    public int getMiddlePoolCapacity() {
        return middlePoolCapacity;
    }

    public int getMiddlePoolMax() {
        return middlePoolMax;
    }

    public int getOldPoolCore() {
        return oldPoolCore;
    }

    public int getOldPoolCapacity() {
        return oldPoolCapacity;
    }

    public int getOldPoolMax() {
        return oldPoolMax;
    }

    public HJobServerAddressMapper getServerAddressMapper() {
        return serverAddressMapper;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
    public NacosDiscoveryProperties getNacosDiscoveryProperties() {
        return nacosDiscoveryProperties;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public HJobInfoMapper getJobInfoMapper() {
        return jobInfoMapper;
    }

    public HJobLogMapper getJobLogMapper() {
        return jobLogMapper;
    }

}
