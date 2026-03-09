package com.h.controller;

import com.h.aop.PermissionNeed;
import com.h.core.common.R;
import com.h.domain.entity.HJobUser;
import com.h.service.HJobUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * h_job_user Controller
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class HJobUserController {

    @Resource
    private HJobUserService hJobUserService;

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询用户")
    @PermissionNeed
    public R<HJobUser> getById(@PathVariable Long id) {
        HJobUser user = hJobUserService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }
        return R.ok(user);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有用户")
    @PermissionNeed
    public R<Map<String, Object>> list(@RequestParam(required = false)String userName,
                                       @RequestParam(required = false)Integer role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> data = hJobUserService.listByPage(userName,role,page, size);
        return R.ok(data);
    }

    @PostMapping
    @ApiOperation("新增用户")
    @PermissionNeed
    public R<Void> save(@RequestBody HJobUser user) {
        int result = hJobUserService.save(user);
        return result > 0 ? R.ok() : R.failed("新增失败");
    }

    @PutMapping
    @ApiOperation("更新用户")
    @PermissionNeed
    public R<Void> update(@RequestBody HJobUser user) {
        int result = hJobUserService.update(user);
        return result > 0 ? R.ok() : R.failed("更新失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("根据ID删除用户")
    @PermissionNeed
    public R<Void> deleteById(@PathVariable Long id) {
        int result = hJobUserService.deleteById(id);
        return result > 0 ? R.ok() : R.failed("删除失败");
    }

}
