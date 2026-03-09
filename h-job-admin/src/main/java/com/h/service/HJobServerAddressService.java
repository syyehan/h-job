package com.h.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.api.naming.NamingService;
import com.h.config.ReqUtils;
import com.h.core.common.CommonConstants;
import com.h.core.exception.BaseBizException;
import com.h.domain.entity.HJobServerAddress;
import com.h.mapper.HJobServerAddressMapper;
import com.h.utils.NacosUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * h_job_server_address Service 实现类
 */
@Service
public class HJobServerAddressService {

    @Resource
    private HJobServerAddressMapper hJobServerAddressMapper;

    public HJobServerAddress getById(Long id) {
        return hJobServerAddressMapper.selectById(id);
    }

    public Map<String, Object> listByPage(String serviceName, String title, Integer page, Integer size) {

        List<Long> ids = new ArrayList<>();
        if (CommonConstants.ROLE_USER == ReqUtils.getRole()){
            String[] permissionArr = ReqUtils.getPermission().split(",");
            for (String idStr : permissionArr) {
                if (!idStr.trim().isEmpty()) {
                    ids.add(Long.valueOf(idStr.trim()));
                }
            }
        }
        int offset = (page - 1) * size;
        List<HJobServerAddress> list = hJobServerAddressMapper.selectByPage(serviceName, title, offset, size,ids);
        Integer total = hJobServerAddressMapper.selectCount(serviceName, title,ids);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    public void save(HJobServerAddress serverAddress) {
        check(serverAddress);
        HJobServerAddress old = hJobServerAddressMapper.selectByServiceName(serverAddress.getServiceName());
        if (ObjectUtil.isNotEmpty(old)){
            throw new BaseBizException("服务名已存在");
        }
        hJobServerAddressMapper.insert(serverAddress);
        if (1 == serverAddress.getAddressType()){
            return;
        }
        String namespace = serverAddress.getNacosNamespace();
        NamingService namingService = NacosUtils.getNamingService(namespace);
        NacosUtils.subscribeService(namespace,namingService,serverAddress.getServiceName(),serverAddress.getServiceName(),serverAddress.getId());
    }

    public void update(HJobServerAddress serverAddress) {
        check(serverAddress);
        HJobServerAddress old = this.getById(serverAddress.getId());
        if (ObjectUtil.isEmpty(old)){
            throw new BaseBizException("服务地址不存在");
        }

        HJobServerAddress jobServerAddress = hJobServerAddressMapper.selectByServiceNameNotId(serverAddress.getServiceName(),serverAddress.getId());
        if (ObjectUtil.isNotEmpty(jobServerAddress)){
            throw new BaseBizException("服务名已存在");
        }
        hJobServerAddressMapper.update(serverAddress);
        if (1 == serverAddress.getAddressType()){
            return;
        }
        String namespace = old.getNacosNamespace();
        NamingService namingService = NacosUtils.getNamingService(namespace);
        NacosUtils.subscribeService(namespace,namingService,old.getServiceName(),old.getServiceName(),old.getId());
    }

    public int deleteById(Integer id) {
        return hJobServerAddressMapper.deleteById(id);
    }


    private void check(HJobServerAddress serverAddress){
        if (ObjectUtil.isEmpty(serverAddress.getServiceName())){
            throw new BaseBizException("服务名不能为空");
        }
        if (ObjectUtil.isEmpty(serverAddress.getAddressType())){
            throw new BaseBizException("服务地址类型不能为空");
        }
        if(ObjectUtil.isEmpty(serverAddress.getTitle())){
            throw new BaseBizException("业务名称不能为空");
        }
        if (0 == serverAddress.getAddressType()){
            if (ObjectUtil.isEmpty(serverAddress.getNacosNamespace())){
                throw new BaseBizException("nacos-namespace不能为空");
            }
            if (ObjectUtil.isEmpty(serverAddress.getNacosGroup())){
                throw new BaseBizException("nacos-group不能为空");
            }
            serverAddress.setAddressList(null);
        } else if (1 == serverAddress.getAddressType()) {
            if (ObjectUtil.isEmpty(serverAddress.getAddressList())){
                throw new BaseBizException("服务地址列表不能为空");
            }
        }
    }
}
