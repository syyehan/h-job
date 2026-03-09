package com.h.logic.thread;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.h.config.HJobConfig;
import com.h.domain.entity.HJobServerAddress;
import com.h.utils.NacosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class NacosListenerThread {

    private static final Logger log = LoggerFactory.getLogger(NacosListenerThread.class);

    private static Thread leaderElectionThread;
    private static volatile boolean running = true;

    private static Thread registerThread;

    private static NacosListenerThread listener = new NacosListenerThread();

    public static NacosListenerThread getInstance() {
        return listener;
    }

    public void toStart() {
        listener.start();
    }

    public void toStop() {
        running = false;
        if (leaderElectionThread != null) {
            leaderElectionThread.interrupt();
        }
        if (registerThread != null) {
            registerThread.interrupt();
        }
    }

    public void start() {
        subscribeHJob();
        leaderElectionThread = new Thread(this::leaderElectionLoop);
        leaderElectionThread.setDaemon(true);
        leaderElectionThread.setName("h-job, NacosListener#leaderElectionThread");
        leaderElectionThread.start();

        registerThread = new Thread(this::registerNacosThread);
        registerThread.setDaemon(true);
        registerThread.setName("h-job, NacosListener#registerThread");
        registerThread.start();
    }

    private void leaderElectionLoop() {
        try {
            TimeUnit.MILLISECONDS.sleep(2000 - System.currentTimeMillis()%1000 );
        } catch (Throwable e) {
            if (!running) {
                log.error("error:", e);
            }
        }
        while (running) {
            try {
                performLeaderElection();
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                log.error("leaderElectionLoop error:", e);
            }
        }
    }


    public void registerNacosThread() {
        try {
            TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis()%1000 );
        } catch (Throwable e) {
            if (!running) {
                log.error("error:", e);
            }
        }
        while (running) {
            try {
                naocsRegister();
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error("registerThread error:", e);
            }
        }
    }


    private void performLeaderElection() {
        try {
            NamingService namingService = NacosUtils.getNamingService(NacosUtils.getNacosInfo().getNamespace());
            List<Instance> instances = namingService.getAllInstances(
                    NacosUtils.getNacosInfo().getService(),
                    NacosUtils.getNacosInfo().getGroup()
            );

            if (instances.isEmpty()) {
                NacosUtils.setRegisterLeader(true);
                log.debug("========== h-job register is Leader ==========");
                NacosUtils.setCalculateLeader(true);
                log.debug("========== h-job calculateLeader is Leader ==========");
                return;
            }
            List<Instance> sortInstances = getSortInstances(instances);
            String localIp = getLocalIp();
            int localPort = HJobConfig.getHJobConfig().getServerPort();
            calculateLeader(sortInstances,localIp,localPort);
            registerLeader(sortInstances,localIp,localPort);
        } catch (Exception e) {
            log.error("Leader error", e);
        }
    }

    private static void registerLeader(List<Instance> sortedInstances,String localIp,int localPort) {
        Instance leaderInstance = sortedInstances.get(0);
        boolean currentlyLeader = leaderInstance.getIp().equals(localIp) &&
                leaderInstance.getPort() == localPort;
        if (currentlyLeader) {
            NacosUtils.setRegisterLeader(true);
            log.debug("========== h-job register is Leader ==========");
        }else {
            NacosUtils.setRegisterLeader(false);
            log.debug("========== h-job register is Follower ==========");
        }
    }
    public static void calculateLeader(List<Instance> sortedInstances,String localIp,int localPort) {
        Instance finallyInstance = sortedInstances.get(sortedInstances.size() - 1);
        boolean calculateLeader = finallyInstance.getIp().equals(localIp) &&
                finallyInstance.getPort() == localPort;
        if (calculateLeader){
            NacosUtils.setCalculateLeader(true);
            log.debug("========== h-job calculateLeader is Leader ==========");
        }else {
            NacosUtils.setCalculateLeader(false);
            log.debug("========== h-job calculateLeader is Follower ==========");
        }
    }

    private static List<Instance> getSortInstances(List<Instance> instances) {
        return instances.stream()
                .sorted((a, b) -> {
                    long ipCompare = NetUtil.ipv4ToLong(a.getIp()) - NetUtil.ipv4ToLong(b.getIp());
                    if (ipCompare != 0) {
                        return ipCompare < 0 ? -1 : 1;
                    }
                    return Integer.compare(a.getPort(), b.getPort());
                })
                .collect(Collectors.toList());
    }


    private String getLocalIp() {
        try {
            return NetUtil.getLocalhost().getHostAddress();
        } catch (Exception e) {
            log.error("get local ip error:", e);
            return "127.0.0.1";
        }
    }

    public void naocsRegister() {
        try {
            if (!NacosUtils.isRegisterLeader()) {
                return;
            }
            List<HJobServerAddress> jobServerAddresses = HJobConfig.getHJobConfig().getServerAddressMapper().selectAll();
            if (CollUtil.isEmpty(jobServerAddresses)) {
                return;
            }
            for (HJobServerAddress jobServerAddress : jobServerAddresses) {
                if (0 != jobServerAddress.getAddressType()) {
                    continue;
                }
                String namespace = jobServerAddress.getNacosNamespace();
                String group = jobServerAddress.getNacosGroup();
                String serviceName = jobServerAddress.getServiceName();
                NamingService namingService = NacosUtils.getNamingService(namespace);
                NacosUtils.subscribeService(namespace, namingService, serviceName, group, jobServerAddress.getId());
            }
        } catch (Exception e) {
            log.error("naocsRegister error:", e);
        }
    }

    public void subscribeHJob(){
        String namespace = NacosUtils.getNacosInfo().getNamespace();
        String groupName = NacosUtils.getNacosInfo().getGroup();
        String serviceName = NacosUtils.getNacosInfo().getService();
        NamingService namingService = NacosUtils.getNamingService(namespace);
        try {
            EventListener listener = event -> {
                if (event instanceof NamingEvent) {
                    performLeaderElection();
                }
            };
            namingService.subscribe(serviceName, groupName, listener);
        } catch (NacosException e) {
            log.error("subscribeHJob error:", e);
        }
    }
}
