package com.h.logic.trigger;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.h.config.HJobConfig;
import com.h.core.common.RouteStrategyConstants;
import com.h.domain.entity.HJobInfo;
import com.h.domain.entity.HJobLog;
import com.h.domain.entity.HJobServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HJobTriggerHandler {
    private static final Logger logger = LoggerFactory.getLogger(HJobTriggerHandler.class);

    private static final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public static void handle(Long jobId,
                               int failRetryCount,
                               String param) {
        HJobInfo info = HJobConfig.getHJobConfig().getJobInfoMapper().selectById(jobId);
        if (ObjectUtil.isEmpty(info)){
            logger.warn("========== HJobTriggerHandler fail, job info is null，jobId={}", jobId);
            return;
        }
        HJobServerAddress serverAddress = HJobConfig.getHJobConfig().getServerAddressMapper().selectById(info.getJobServerId());
        if (ObjectUtil.isEmpty(serverAddress)){
            logger.error("========== HJobTriggerHandler fail, serverAddress is null，jobId={} jobServerId={}", jobId, info.getJobServerId());
            return;
        }
        String ips = serverAddress.getAddressList();
        if (StrUtil.isBlank(ips)){
            HJobLog jobLog = new HJobLog();
            jobLog.setJobId(info.getId());
            jobLog.setJobServerId(info.getJobServerId());
            jobLog.setAddress("");
            jobLog.setParam(param);
            jobLog.setSharding("0");
            jobLog.setFailRetryCount(failRetryCount > 0 ? failRetryCount:info.getExecutorFailRetryCount());
            jobLog.setExecutorCode(2);
            jobLog.setFailRetryFlag(0);
            jobLog.setResultMsg("serverAddress addressList is null");
            jobLog.setExecutorDate(DateUtil.date());
            HJobConfig.getHJobConfig().getJobLogMapper().insert(jobLog);
            logger.error("========== HJobTriggerHandler fail, addressList is null，jobId={} jobServerId={}", jobId, info.getJobServerId());
            return;
        }
        Map<String,String> result = new HashMap<>();
        String routeStrategy = info.getRouteStrategy();
        //如果路由策略为单机，则只调用一个节点
        String[] ipArray = ips.split(",");
        if (ipArray.length == 1){
            call(info, failRetryCount,param, ipArray[0], 1,result);
        }else {
            switch (routeStrategy){
                case RouteStrategyConstants.FIRST:
                    call(info,failRetryCount,param, ipArray[0], 1,result);
                    break;
                case RouteStrategyConstants.RANDOM:
                    int r = RandomUtil.randomInt(0, ipArray.length-1);
                    call(info,failRetryCount,param, ipArray[r], 1,result);
                    break;
                case RouteStrategyConstants.ROUND_ROBIN:
                    int index = Math.abs(roundRobinIndex.getAndIncrement()) % ipArray.length;
                    call(info,failRetryCount,param, ipArray[index], 1,result);
                    break;
//                case RouteStrategyConstants.WEIGHT:
//                    //权重路由
//                    break;
                case RouteStrategyConstants.LAST:
                    call(info,failRetryCount,param, ipArray[ipArray.length-1], 1,result);
                    break;
                case RouteStrategyConstants.SHARD:
                    for (int i = 0; i < ipArray.length; i++) {
                        call(info,failRetryCount,param, ipArray[i], i+1,result);
                    }
                    break;
                default:
                    call(info, failRetryCount,param, ipArray[0], 1,result);
                    break;
            }
        }


    }

    public static void call(HJobInfo info ,int failRetryCount,String param, String ip, Integer shard,Map<String,String> result) {

        HJobLog jobLog = new HJobLog();
        jobLog.setJobId(info.getId());
        jobLog.setJobServerId(info.getJobServerId());
        jobLog.setAddress(ip);
        jobLog.setParam(param);
        jobLog.setSharding(shard+"");
        jobLog.setFailRetryCount(failRetryCount > 0 ? failRetryCount:info.getExecutorFailRetryCount());
        jobLog.setExecutorCode(1);
        jobLog.setFailRetryFlag(0);
        jobLog.setExecutorDate(DateUtil.date());
        HJobConfig.getHJobConfig().getJobLogMapper().insert(jobLog);

        String url = ip + info.getPath();
        try {
            HttpRequest request = null;
            if (info.getMethod().equalsIgnoreCase("POST")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.putOpt("param", param);
                jsonObject.putOpt("shard", shard);
                request = HttpRequest.post(url);
                request.body(jsonObject.toString());
            }else if (info.getMethod().equalsIgnoreCase("GET")){
                url = url  + "?shard=" + shard + "&param=" + param;
                request = HttpRequest.get(url);
            }
            // 设置超时时间
            request.timeout(info.getExecutorTimeout()*1000);
            HttpResponse response = request.execute();
            String responseBody = response.body();
            logger.info("========== HJobTriggerHandler success, jobId={} ip={} shard={} response={}", info.getId(), ip, shard, responseBody);
            jobLog.setResultCode(response.getStatus());
            jobLog.setResultMsg(responseBody);
            jobLog.setExecutorCode(0);
            result.put("code", response.getStatus() + "");
            result.put("msg", responseBody);
            if (!response.isOk()) {
                logger.error("========== HJobTriggerHandler fail, jobId={} ip={} shard={} httpStatus={}", info.getId(), ip, shard, response.getStatus());
            }
        }catch (Exception e){
            jobLog.setExecutorCode(2);
            logger.error("========== HJobTriggerHandler fail, jobId={} ip={} shard={} error:", info.getId(), ip, shard, e);
        }finally {
            HJobConfig.getHJobConfig().getJobLogMapper().update(jobLog);
        }
    }


}
