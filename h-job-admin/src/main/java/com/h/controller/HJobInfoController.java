package com.h.controller;

import com.h.core.common.CommonConstants;
import com.h.core.common.R;
import com.h.domain.entity.HJobInfo;
import com.h.service.HJobInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * h_job_info Controller
 */
@RestController
@RequestMapping("/jobInfo")
@Api(tags = "任务信息管理")
public class HJobInfoController {

    @Resource
    private HJobInfoService hJobInfoService;

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询任务")
    public R<HJobInfo> getById(@PathVariable Long id) {
        HJobInfo jobInfo = hJobInfoService.getById(id);
        if (jobInfo == null) {
            return R.failed("任务不存在");
        }
        return R.ok(jobInfo);
    }

    @GetMapping("/list")
    @ApiOperation("分页查询任务")
    public R<Map<String, Object>> list(@RequestParam(required = false) String jobDesc,
                                       @RequestParam(required = false) Long jobServerId,
                                       @RequestParam(required = false) Integer jobStatus,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = hJobInfoService.listByPage(jobDesc, jobServerId, jobStatus, page, size);
        return R.ok(result);
    }

    @PostMapping
    @ApiOperation("新增任务")
    public R<String> save(@RequestBody HJobInfo jobInfo) {
        hJobInfoService.save(jobInfo);
        return R.ok();
    }

    @PutMapping
    @ApiOperation("更新任务")
    public R<Void> update(@RequestBody HJobInfo jobInfo) {
        hJobInfoService.update(jobInfo);
        return R.ok();
    }

    @PutMapping("/changeStatus")
    @ApiOperation("启动/停止")
    public R<Void> changeStatus(@RequestBody HJobInfo jobInfo) {
        hJobInfoService.changeStatus(jobInfo);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("根据ID删除任务")
    public R<Void> deleteById(@PathVariable Long id) {
        int result = hJobInfoService.deleteById(id);
        return result > 0 ? R.ok() : R.failed(CommonConstants.FAIL_MSG);
    }

    @GetMapping("/execute/{id}")
    @ApiOperation("立即执行")
    public R<Void> execute(@PathVariable Long id, @RequestParam(required = false) String param) {
        hJobInfoService.execute(id, param);
        return R.ok();
    }

    @GetMapping("/getNextTime/{id}")
    @ApiOperation("获取下次执行时间")
    public R<List<String>> getNextTime(@PathVariable Long id) {
        return R.ok(hJobInfoService.getNextTime(id));
    }

}