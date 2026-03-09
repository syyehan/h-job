package com.h.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.h.core.common.R;
import com.h.domain.dto.NamespaceInfo;
import com.h.utils.NacosUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nacos 管理控制器
 *
 * @author yehan
 */
@RestController
@Api(tags = "Nacos管理")
@RequestMapping("/nacos")
public class NacosController {

    private static final Logger log = LoggerFactory.getLogger(NacosController.class);

    @GetMapping("/info")
    @ApiOperation("获取 Nacos 概览信息")
    public R<Map<String, Object>> getNacosInfo() {
        try {
            Map<String, Object> info = new HashMap<>();

            // 获取命名空间
            Pair<List<NamespaceInfo>, String> pair = NacosUtils.getAllNamespaces();
            List<NamespaceInfo> namespaces = pair.getLeft();
            info.put("namespaces", namespaces);
            info.put("serverAddr", pair.getRight());
            // 获取所有组
            for (int i = 0; i < namespaces.size(); i++) {
                Map<String, Object> group = new HashMap<>();
                List<String> groups = NacosUtils.getAllGroups(namespaces.get(i).getNamespace());
                group.put("groups", groups);
                info.put("namespaces-" + namespaces.get(i).getNamespaceShowName(), groups);
            }
            return R.ok(info);
        } catch (Exception e) {
            return R.failed("获取 Nacos 信息失败: " + e.getMessage());
        }
    }


    @GetMapping("/test")
    @ApiOperation("test")
    public R<JSONObject> test(String namespace, String group, String serviceName,
                              String path, String httpMethod, String params) {
        JSONObject result =
                NacosUtils.callService(namespace, group, serviceName,
                        path, httpMethod, params);
        return R.ok(result);
    }

    @GetMapping("/getLeaderInfo")
    @ApiOperation("getLeaderInfo")
    public R<Map<String, Boolean>> leaderInfo() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("registerLeader", NacosUtils.isRegisterLeader());
        map.put("calculateLeader", NacosUtils.isCalculateLeader());
        return R.ok(map);
    }

    @GetMapping("/getClusterList")
    @ApiOperation("getClusterList")
    public R<List> getClusterList() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            NamingService namingService = NacosUtils.getNamingService(NacosUtils.getNacosInfo().getNamespace());
            List<Instance> instances = namingService.getAllInstances(
                    NacosUtils.getNacosInfo().getService(),
                    NacosUtils.getNacosInfo().getGroup()
            );
            for (Instance instance : instances) {
                Map<String, Object> map = new HashMap<>();
                map.put("ip", instance.getIp());
                map.put("port", instance.getPort());
                map.put("registerLeader", false);
                map.put("calculateLeader", false);
                map.put("online", false);
                try {
                    String returnStr = HttpUtil.get(instance.getIp() + ":" + instance.getPort() + "/h-job/nacos/getLeaderInfo");
                    JSONObject jsonObject = JSONUtil.parseObj(returnStr);
                    if (200 != jsonObject.getInt("code")) {
                        continue;
                    }
                    JSONObject dataJson = jsonObject.getJSONObject("data");
                    map.put("registerLeader", dataJson.get("registerLeader"));
                    map.put("calculateLeader", dataJson.get("calculateLeader"));
                    map.put("online", true);
                } catch (Exception e) {
                    log.error("HttpUtil get error:", e);
                }
                list.add(map);
            }
        } catch (Exception e) {
            log.error("getClusterList error:", e);
        }
        return R.ok(list);
    }

}
