package com.h.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nacos HTTP API 工具类 - 通过 URL 请求获取 Nacos 信息
 * @author yehan
 */
public class NacosHttpUtils {

    private static final Logger log = LoggerFactory.getLogger(NacosHttpUtils.class);

    /**
     * 获取所有命名空间
     * API: GET /nacos/v1/console/namespaces
     */
    public static JSONArray getAllNamespaces(String serverAddr, String accessToken) {
        try {
            String url = buildUrl(serverAddr, "/nacos/v1/console/namespaces");
            
            HttpResponse response = HttpRequest.get(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(5000)
                    .execute();

            if (!response.isOk()) {
                log.error("获取命名空间失败，HTTP状态码: {}", response.getStatus());
                return null;
            }

            String body = response.body();
            JSONObject json = JSONUtil.parseObj(body);

            // Nacos 返回格式: { "code": 200, "data": [...] }
            if (json.getInt("code") == 200) {
                return json.getJSONArray("data");
            }

            return null;
            
        } catch (Exception e) {
            log.error("获取命名空间异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取指定命名空间下的所有配置列表
     * API: GET /nacos/v1/cs/configs?dataId=&group=&tenant=&pageNo=1&pageSize=100
     */
    public static Map<String, Object> getConfigList(String serverAddr, String namespace, String accessToken) {
        try {
            String url = buildUrl(serverAddr, "/nacos/v1/cs/configs");
            
            HttpResponse response = HttpRequest.get(url)
                    .form("dataId", "")
                    .form("group", "")
                    .form("tenant", namespace)
                    .form("pageNo", 1)
                    .form("pageSize", 100)
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(5000)
                    .execute();

            if (!response.isOk()) {
                log.error("获取配置列表失败，HTTP状态码: {}", response.getStatus());
                return new HashMap<>();
            }

            String body = response.body();
            JSONObject json = JSONUtil.parseObj(body);

            return json;

        } catch (Exception e) {
            log.error("获取配置列表异常: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取指定命名空间下的所有服务
     * API: GET /nacos/v1/ns/service/list?pageNo=1&pageSize=100&namespaceId=
     */
    public static Map<String, Object> getServiceList(String serverAddr, String namespace, String accessToken) {
        try {
            String url = buildUrl(serverAddr, "/nacos/v1/ns/service/list");
            
            HttpResponse response = HttpRequest.get(url)
                    .form("pageNo", 1)
                    .form("pageSize", 100)
                    .form("namespaceId", namespace)
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(5000)
                    .execute();

            if (!response.isOk()) {
                log.error("获取服务列表失败，HTTP状态码: {}", response.getStatus());
                return new HashMap<>();
            }

            String body = response.body();
            JSONObject json = JSONUtil.parseObj(body);

            return json;

        } catch (Exception e) {
            log.error("获取服务列表异常: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取指定服务的所有实例
     * API: GET /nacos/v1/ns/instance/list?serviceName=&namespaceId=
     */
    public static Map<String, Object> getInstanceList(String serverAddr, String serviceName, String namespace, String accessToken) {
        try {
            String url = buildUrl(serverAddr, "/nacos/v1/ns/instance/list");
            
            HttpResponse response = HttpRequest.get(url)
                    .form("serviceName", serviceName)
                    .form("namespaceId", namespace)
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(5000)
                    .execute();

            if (!response.isOk()) {
                log.error("获取实例列表失败，HTTP状态码: {}", response.getStatus());
                return new HashMap<>();
            }

            String body = response.body();
            JSONObject json = JSONUtil.parseObj(body);

            return json;

        } catch (Exception e) {
            log.error("获取实例列表异常: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取指定配置的内容
     * API: GET /nacos/v1/cs/configs?dataId=&group=&tenant=
     */
    public static String getConfig(String serverAddr, String dataId, String group, String namespace, String accessToken) {
        try {
            String url = buildUrl(serverAddr, "/nacos/v1/cs/configs");
            
            HttpResponse response = HttpRequest.get(url)
                    .form("dataId", dataId)
                    .form("group", group)
                    .form("tenant", namespace)
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(5000)
                    .execute();

            if (response.isOk()) {
                return response.body();
            }

            return null;

        } catch (Exception e) {
            log.error("获取配置内容异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从服务列表中提取所有唯一的组名
     */
    public static List<String> extractGroupsFromServices(Map<String, Object> serviceList) {
        List<String> groups = new ArrayList<>();
        
        if (serviceList == null || !serviceList.containsKey("doms")) {
            return groups;
        }

        JSONArray services = JSONUtil.parseArray(serviceList.get("doms"));
        
        for (Object serviceObj : services) {
            String serviceName = (String) serviceObj;
            if (serviceName.contains("@@")) {
                String[] parts = serviceName.split("@@", 2);
                if (parts.length > 0 && !groups.contains(parts[0])) {
                    groups.add(parts[0]);
                }
            } else {
                if (!groups.contains("DEFAULT_GROUP")) {
                    groups.add("DEFAULT_GROUP");
                }
            }
        }
        
        return groups;
    }

    /**
     * 过滤指定组的服务
     */
    public static List<String> filterServicesByGroup(Map<String, Object> serviceList, String group) {
        List<String> services = new ArrayList<>();
        
        if (serviceList == null || !serviceList.containsKey("doms")) {
            return services;
        }

        JSONArray serviceArray = JSONUtil.parseArray(serviceList.get("doms"));
        
        for (Object serviceObj : serviceArray) {
            String serviceName = (String) serviceObj;
            if (serviceName.contains("@@")) {
                String[] parts = serviceName.split("@@", 2);
                if (parts.length == 2 && parts[0].equals(group)) {
                    services.add(parts[1]);
                }
            } else if (group.equals("DEFAULT_GROUP")) {
                services.add(serviceName);
            }
        }
        
        return services;
    }

    /**
     * 获取 Nacos 健康状态
     * API: GET /nacos/v1/console/health/readiness
     */
    public static boolean checkHealth(String serverAddr) {
        try {
            String url = buildUrl(serverAddr, "/nacos/v1/console/health/readiness");
            
            HttpResponse response = HttpRequest.get(url)
                    .timeout(3000)
                    .execute();

            return response.isOk() && "UP".equals(response.body());

        } catch (Exception e) {
            log.error("检查 Nacos 健康状态异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建 Nacos API URL
     */
    private static String buildUrl(String serverAddr, String path) {
        String addr = serverAddr.startsWith("http") ? serverAddr : "http://" + serverAddr;
        return addr + path;
    }
}
