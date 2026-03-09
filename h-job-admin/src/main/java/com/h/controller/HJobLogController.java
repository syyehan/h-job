package com.h.controller;

import com.h.core.common.R;
import com.h.domain.entity.HJobLog;
import com.h.service.HJobLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * h_job_log Controller
 */
@RestController
@RequestMapping("/jobLog")
@Api(tags = "调度日志管理")
public class HJobLogController {

    @Resource
    private HJobLogService hJobLogService;

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询日志")
    public R<HJobLog> getById(@PathVariable Long id) {
        HJobLog jobLog = hJobLogService.getById(id);
        if (jobLog == null) {
            return R.failed("日志不存在");
        }
        return R.ok(jobLog);
    }

    @GetMapping("/list")
    @ApiOperation("分页查询日志列表")
    public R<Map<String, Object>> list(@RequestParam(required = false) Long jobId,
                                       @RequestParam(required = false) Integer executorCode,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = hJobLogService.listByPage(jobId, executorCode, page, size);
        return R.ok(result);
    }
    @DeleteMapping("/clear")
    @ApiOperation("清空日志")
    public R<Void> clear() {
        int result = hJobLogService.clear();
        return result > 0 ? R.ok() : R.failed("清空失败");
    }
}
