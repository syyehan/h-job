package com.h.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.h.config.HJobConfig;
import com.h.domain.dto.NamespaceInfo;
import com.h.domain.entity.HJobServerAddress;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Nacos 工具类 - 用于获取 namespace 和 group 信息
 * @author yehan
 */
@Component
public class NacosUtils {

    private static final Logger log = LoggerFactory.getLogger(NacosUtils.class);
    
    /**
     * 注册节点leader标识 - 原子布尔类型确保线程安全
     */
    private static final AtomicBoolean registerLeader = new AtomicBoolean(false);
    
    /**
     * 计算节点leader标识 - 原子布尔类型确保线程安全
     */
    private static final AtomicBoolean calculateLeader = new AtomicBoolean(false);

    /**
     * 检查当前是否为注册节点leader
     * @return true-是leader，false-不是leader
     */
    public static boolean isRegisterLeader() {
        return registerLeader.get();
    }
    
    /**
     * 强制设置注册节点leader状态（主要用于初始化或故障恢复）
     * @param isLeader 目标状态
     */
    public static void setRegisterLeader(boolean isLeader) {
        registerLeader.set(isLeader);
    }


    /**
     * 检查当前是否为计算节点leader
     * @return true-是leader，false-不是leader
     */
    public static boolean isCalculateLeader() {
        return calculateLeader.get();
    }
    
    /**
     * 强制设置计算节点leader状态（主要用于初始化或故障恢复）
     * @param isLeader 目标状态
     */
    public static void setCalculateLeader(boolean isLeader) {
        calculateLeader.set(isLeader);
    }

    private static final Map<String,NamingService> namingServiceMap = new ConcurrentHashMap<>();

    private static final Set<String> namespaceGroupServiceNameSet = new ConcurrentHashSet<>();

    /**
     * nacos配置信息
     */
    public static NacosDiscoveryProperties getNacosInfo(){
        return HJobConfig.getHJobConfig().getNacosDiscoveryProperties();
    }

    /**
     * 获取 Nacos 所有命名空间
     * 通过 Nacos HTTP API 获取：GET /nacos/v1/console/namespaces
     *
     * @return 命名空间列表
     */
    public static Pair<List<NamespaceInfo>,String> getAllNamespaces() {
        List<NamespaceInfo> namespaces = new ArrayList<>();

        try {
            // 调用 NacosHttpUtils 获取命名空间
            cn.hutool.json.JSONArray namespacesArray = NacosHttpUtils.getAllNamespaces(HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getServerAddr(), null);

            if (namespacesArray != null && !namespacesArray.isEmpty()) {
                for (int i = 0; i < namespacesArray.size(); i++) {
                    cn.hutool.json.JSONObject namespaceJson = namespacesArray.getJSONObject(i);
                    NamespaceInfo info = new NamespaceInfo();
                    info.setNamespace(namespaceJson.getStr("namespace"));
                    info.setNamespaceShowName(namespaceJson.getStr("namespaceShowName"));
                    info.setNamespaceDesc(namespaceJson.getStr("namespaceDesc"));
                    info.setQuota(namespaceJson.getInt("quota"));
                    info.setConfigCount(namespaceJson.getInt("configCount"));
                    info.setType(namespaceJson.getInt("type"));
                    namespaces.add(info);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("获取命名空间列表失败: " + e.getMessage(), e);
        }

        return Pair.of(namespaces,HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getServerAddr());
    }

    /**
     * 获取指定命名空间下的所有服务组（通过遍历服务列表获取）
     */
    public static List<String> getAllGroups(String namespace) {
        List<String> groups = new ArrayList<>();
        
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getServerAddr());
            properties.put("namespace", namespace);
            properties.put("username", HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getUsername());
            properties.put("password", HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getPassword());
            // 添加超时配置
            properties.put("connectTimeout", "5000");
            properties.put("namingLoadCacheAtStart", "true");
            
            NamingService namingService = NacosFactory.createNamingService(properties);
            
            // 等待连接就绪
            Thread.sleep(500);
            
            // 获取所有服务
            List<String> serviceNames = namingService.getServicesOfServer(1, Integer.MAX_VALUE).getData();
            
            // 从服务名中提取 group 信息
            // Nacos 服务名格式通常是: groupName@@serviceName
            for (String serviceName : serviceNames) {
                if (serviceName.contains("@@")) {
                    String[] parts = serviceName.split("@@");
                    if (parts.length > 0 && !groups.contains(parts[0])) {
                        groups.add(parts[0]);
                    }
                } else {
                    // 默认组
                    if (!groups.contains("DEFAULT_GROUP")) {
                        groups.add("DEFAULT_GROUP");
                    }
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("获取服务组列表失败: " + e.getMessage(), e);
        }
        
        return groups;
    }



    public static NamingService getNamingService(String namespace) {

        if(namingServiceMap.containsKey(namespace)){
            return namingServiceMap.get(namespace);
        }
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getServerAddr());
            properties.put("namespace", namespace);
            properties.put("username", HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getUsername());
            properties.put("password", HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getPassword());
            // 添加超时配置
            properties.put("connectTimeout", "5000");
            properties.put("namingLoadCacheAtStart", "true");
            NamingService namingService = NacosFactory.createNamingService(properties);
            namingServiceMap.put(namespace,namingService);
            return namingService;
        }catch (NacosException e) {
            throw new RuntimeException("getNamingService: ", e);
        }
    }




    /**
     * 调用指定 namespace 和 group 下的服务接口
     * @param namespace 命名空间
     * @param group 组名
     * @param serviceName 服务名
     * @param path 接口路径，如：/api/user/list
     * @param httpMethod HTTP 方法：GET、POST、PUT、DELETE
     * @param params 请求参数（JSON 字符串）
     * @return 响应结果
     */
    public static JSONObject callService(String namespace, String group, String serviceName,
                                     String path, String httpMethod, String params) {
        try {
            // 构造完整的服务名
            String fullServiceName = serviceName;
            // 获取服务的所有实例
            List<Instance> instances = getInstances(HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getServerAddr(), namespace, fullServiceName, HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getUsername(), HJobConfig.getHJobConfig().getNacosDiscoveryProperties().getPassword());

//            if (CollUtil.isEmpty(instances)) {
//                //构造完整的服务名（包含 group）
//                fullServiceName = String.format("%s@@%s",group,serviceName);
//                instances = getInstances(serverAddr, namespace, fullServiceName, username, password);
//            }

            if (CollUtil.isEmpty(instances)){
                throw new RuntimeException("服务 " + fullServiceName + " 没有可用实例");
            }

            // 选择一个健康的实例
            Instance instance = instances.stream()
                    .filter(Instance::isHealthy)
                    .findFirst()
                    .orElse(instances.get(0));

            // 默认使用 http，如果需要 https 可以根据实例元数据判断
            String protocol = "true".equals(instance.getMetadata().get("secure")) ? "https" : "http";
            String url = String.format("%s://%s:%d%s", protocol, instance.getIp(), instance.getPort(), path);

            // 使用 hutool 的 HttpRequest 发送请求
            cn.hutool.http.HttpRequest request;

            switch (httpMethod.toUpperCase()) {
                case "GET":
                    request = cn.hutool.http.HttpRequest.get(url);
                    break;
                case "POST":
                    request = cn.hutool.http.HttpRequest.post(url);
                    if (params != null && !params.isEmpty()) {
                        request.body(params);
                    }
                    break;
                case "PUT":
                    request = cn.hutool.http.HttpRequest.put(url);
                    if (params != null && !params.isEmpty()) {
                        request.body(params);
                    }
                    break;
                case "DELETE":
                    request = cn.hutool.http.HttpRequest.delete(url);
                    break;
                default:
                    request = cn.hutool.http.HttpRequest.get(url);
            }

            // 设置超时时间
            request.timeout(10000);


            // 发送请求并返回响应
            cn.hutool.http.HttpResponse response = request.execute();

            if (!response.isOk()) {
                throw new RuntimeException("调用服务失败，HTTP状态码: " + response.getStatus());
            }
            String resStr = response.body();
            return JSONUtil.parseObj(resStr);
        } catch (Exception e) {
            throw new RuntimeException("调用服务失败: ", e);
        }
    }

    /**
     * 获取指定服务的所有实例
     */
    public static List<Instance> getInstances(String serverAddr, String namespace, String serviceName,
                                              String username, String password) throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("namespace", namespace);
        properties.put("username", username);
        properties.put("password", password);
        // 添加超时配置
        properties.put("connectTimeout", "5000");
        properties.put("namingLoadCacheAtStart", "true");

        NamingService namingService = NacosFactory.createNamingService(properties);

        // 等待连接就绪
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return namingService.getAllInstances(serviceName);
    }

    public static void subscribeService(String namespace, NamingService namingService, String serviceName, String groupName, Long id) {
        try {
            String serviceKey = getServiceKey(namespace, groupName, serviceName);

            if (namespaceGroupServiceNameSet.contains(serviceKey)) {
                log.debug("========== 命名空间[{}]分组[{}]服务[{}]已订阅，跳过", namespace, groupName, serviceName);
                return;
            }
            EventListener listener = event -> {
                if (event instanceof NamingEvent) {
                    NamingEvent namingEvent = (NamingEvent) event;
                    String service = namingEvent.getServiceName();
                    List<Instance> instances = namingEvent.getInstances();
                    logServiceInstances(namespace, groupName, service, instances);
                    updateJobServerAddress(id, instances);
                    namespaceGroupServiceNameSet.add(serviceKey);
                }
            };
            namingService.subscribe(serviceName, groupName, listener);

            List<Instance> instances = namingService.getAllInstances(serviceName, groupName);
            logServiceInstances(namespace, groupName, serviceName, instances);
            updateJobServerAddress(id, instances);
            namespaceGroupServiceNameSet.add(serviceKey);
        } catch (NacosException e) {
            log.error("命名空间[{}]监听服务[{}]失败", namespace, serviceName, e);
        }
    }

    private static String getServiceKey(String namespace, String group, String serviceName) {
        return namespace + ":" + group + ":" + serviceName;
    }

    private static void logServiceInstances(String namespace, String groupName, String serviceName, List<Instance> instances) {
        if (instances == null || instances.isEmpty()) {
            log.info("========== 命名空间[{}]分组[{}]服务[{}]暂无实例", namespace, groupName, serviceName);
            return;
        }

        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            log.info("========== 命名空间[{}]分组[{}]服务[{}]实例[{}] - IP: {}, Port: {}, Healthy: {}, Weight: {}, Metadata: {}",
                    namespace,
                    groupName,
                    serviceName,
                    i + 1,
                    instance.getIp(),
                    instance.getPort(),
                    instance.isHealthy(),
                    instance.getWeight(),
                    formatMetadata(instance.getMetadata()));
        }
    }

    /**
     * 将元数据Map格式化为字符串表示形式
     *
     * @param metadata 需要格式化的元数据Map，键值对形式
     * @return 格式化后的字符串，格式为"{key1=value1, key2=value2}"，空Map返回"{}"
     */
    private static String formatMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        return metadata.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }


    /**
     * 将实例列表转换为字符串
     */
    private static String instancesToString(List<Instance> instances) {
        if (CollUtil.isEmpty(instances)) {
            return "";
        }
        return instances.stream()
                .map(instance -> instance.getIp() + ":" + instance.getPort())
                .collect(Collectors.joining(","));
    }

    private static void updateJobServerAddress(Long id, List<Instance> instances) {
        HJobServerAddress jobServerAddress = new HJobServerAddress();
        jobServerAddress.setId(id);
        jobServerAddress.setAddressList(instancesToString(instances));
        HJobConfig.getHJobConfig().getServerAddressMapper().update(jobServerAddress);
    }

}
