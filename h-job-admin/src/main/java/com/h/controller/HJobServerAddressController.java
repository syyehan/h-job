package com.h.controller;

import com.h.core.common.R;
import com.h.domain.entity.HJobServerAddress;
import com.h.service.HJobServerAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * h_job_server_address Controller
 */
@RestController
@RequestMapping("/serverAddress")
@Api(tags = "服务地址管理")
public class HJobServerAddressController {

    @Resource
    private HJobServerAddressService hJobServerAddressService;

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询服务地址")
    public R<HJobServerAddress> getById(@PathVariable Long id) {
        HJobServerAddress serverAddress = hJobServerAddressService.getById(id);
        if (serverAddress == null) {
            return R.failed("服务地址不存在");
        }
        return R.ok(serverAddress);
    }
    
    @GetMapping("/list")
    @ApiOperation("分页查询服务地址")
    public R<Map<String, Object>> list(@RequestParam(required = false) String serviceName,
                                      @RequestParam(required = false) String title,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = hJobServerAddressService.listByPage(serviceName, title, page, size);
        return R.ok(result);
    }
    @PostMapping
    @ApiOperation("新增服务地址")
    public R<Void> save(@RequestBody HJobServerAddress serverAddress) {
        hJobServerAddressService.save(serverAddress);
        return R.ok();
    }

    @PutMapping
    @ApiOperation("更新服务地址")
    public R<Void> update(@RequestBody HJobServerAddress serverAddress) {
        hJobServerAddressService.update(serverAddress);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("根据ID删除服务地址")
    public R<Void> deleteById(@PathVariable Long id) {
        int result = hJobServerAddressService.deleteById(id.intValue());
        return result > 0 ? R.ok() : R.failed("删除失败");
    }

}
