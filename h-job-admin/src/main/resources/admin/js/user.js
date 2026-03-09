$(function () {
    var currentPage = 1;
    var pageSize = 10;
    window.executors = []; // 存储执行器列表
    
    // 设置导航标题
    setNavTitleFromUrl();

    // 获取执行器列表 - 改为全局函数
    window.loadExecutors = function() {
        $.ajax({
            url: '/h-job/serverAddress/list',
            type: 'GET',
            data: {
                page: 1,
                size: 9999
            },
            success: function (res) {
                if (res.code === 200) {
                    window.executors = res.data.list || [];
                    renderPermissionCheckboxes();
                } else {
                    layer.msg('加载执行器列表失败');
                }
            },
            error: function () {
                layer.msg('加载执行器列表失败');
            }
        });
    }

    // 渲染权限复选框 - 改为全局函数
    window.renderPermissionCheckboxes = function() {
        var container = $('.permission-section').empty();
        if (window.executors.length === 0) {
            container.append('<div class="checkbox-item"><label><input type="checkbox" disabled> 暂无可用执行器</label></div>');
            return;
        }
        
        // 创建表格容器实现2列布局
        var tableHtml = '<table style="width: 100%; border-collapse: collapse;"><tr>';
        $.each(window.executors, function (i, executor) {
            // 每2个复选框换一行
            if (i > 0 && i % 2 === 0) {
                tableHtml += '</tr><tr>';
            }
            
            tableHtml += '<td style="width: 50%; padding: 2px 0;">' +
                '<div class="checkbox-item">' +
                '<label>' +
                '<input type="checkbox" name="permission" value="' + executor.id + '" style="margin-right: 5px;"> ' +
                executor.serviceName +
                '</label>' +
                '</div>' +
                '</td>';
        });
        tableHtml += '</tr></table>';
        container.append(tableHtml);
    }

    // 收集选中的权限 - 改为全局函数
    window.collectPermissions = function() {
        var selected = [];
        $('.permission-section input[name="permission"]:checked').each(function () {
            selected.push($(this).val());
        });
        return selected.join(',');
    }

    // 设置选中的权限 - 改为全局函数
    window.setPermissions = function(permissionStr) {
        var permissions = permissionStr ? permissionStr.split(',') : [];
        $('.permission-section input[name="permission"]').each(function () {
            $(this).prop('checked', permissions.indexOf($(this).val()) !== -1);
        });
    }

    // 查询
    $('#btn_query').click(function () {
        currentPage = 1;
        loadTable();
    });

    // 重置
    $('#btn_reset').click(function () {
        $('#q_username,#q_role').val('');
        currentPage = 1;
        loadTable();
    });

        // 新增
    $('#btn_add').click(function () {
        $('#form1')[0].reset();
        // 不默认选择角色，让用户必须手动选择
        setPermissions('');
        $('#permissionGroup').hide();
        $('#permissionStar').hide();
        if (window.executors.length === 0) {
            window.loadExecutors();
        }
        
        // 默认隐藏权限区域
        $('#permissionGroup').hide();
        
        // 监听角色选择变化
        $('#form1 select[name=role]').off('change.roleChange').on('change.roleChange', function() {
            var role = $(this).val();
            if (role == 0) { // 普通用户显示权限
                $('#permissionGroup').show();
                $('#permissionStar').show();
            } else { // 管理员隐藏权限
                $('#permissionGroup').hide();
                $('#permissionStar').hide();
            }
        });
        
        var index = layer.open({
            type: 1,
            title: '新增用户',
            area: ['520px', 'auto'],
            minWidth: 480,
            minHeight: 350,
            content: $('#modalDiv'),
            btn: ['确定', '取消'],
            yes: function (index, layero) {
                $('#form1').submit();
                return false;
            },
            cancel: function (index) {
                layer.close(index);
            },
            success: function(layerElem, index) {
                // 确保内容左对齐
                $(layerElem).css({'text-align': 'left'});
                $(layerElem).find('.layui-layer-content').css({'text-align': 'left'});
                $(layerElem).find('form').css({'text-align': 'left'});
            }
        });
        $('#modalDiv').data('isAdd', true);
    });

    // 保存
    $('#form1').submit(function (e) {
        e.preventDefault();
        
        // 清除之前的错误提示
        $('.help-text').hide();
        $('input, select').removeClass('error');
        
        var isValid = true;
        
        // 校验账号
        var userName = $('input[name=userName]').val().trim();
        if (!userName) {
            $('#userNameError').show();
            $('input[name=userName]').addClass('error');
            isValid = false;
        }
        
        // 校验密码
        var password = $('input[name=password]').val().trim();
        if (!password) {
            $('#passwordError').show();
            $('input[name=password]').addClass('error');
            isValid = false;
        }
        
        // 校验角色
        var role = $('select[name=role]').val();
        if (!role) {
            $('#roleError').show();
            $('select[name=role]').addClass('error');
            isValid = false;
        }
        
        // 如果是普通用户，校验权限
        if (role === '0') {
            var permissions = collectPermissions();
            if (!permissions) {
                $('#permissionError').show();
                $('#permissionStar').show();
                isValid = false;
            }
        }
        
        if (!isValid) {
            layer.msg('请填写必填项');
            return false;
        }
        
        var data = {};
        $('#form1').serializeArray().forEach(function (item) {
            if (item.name !== 'permission') {
                data[item.name] = item.value;
            }
        });
        // 处理权限复选框
        data.permission = collectPermissions();
        
        var url, type;
        if ($('#modalDiv').data('isAdd')) {
            url = '/h-job/user';
            type = 'POST';
        } else {
            url = '/h-job/user';
            type = 'PUT';
            data.id = $('#modalDiv').data('id');
        }
        $.ajax({
            url: url,
            type: type,
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function (res) {
                if (res.code === 200) {
                    layer.closeAll();
                    layer.msg('操作成功');
                    loadTable();
                } else {
                    layer.msg(res.msg);
                }
            }
        });
    });
    
    // 实时清除错误提示（当用户开始输入时）
    $('input[name=userName], input[name=password]').on('input', function() {
        $(this).removeClass('error');
        var errorId = $(this).attr('name') + 'Error';
        $('#' + errorId).hide();
    });
    
    $('select[name=role]').on('change', function() {
        $(this).removeClass('error');
        $('#roleError').hide();
        $('#permissionError').hide(); // 切换角色时总是隐藏权限错误
    });
    
    // 权限复选框变化时清除权限错误
    $('input[name=permission]').on('change', function() {
        $('#permissionError').hide();
    });



    // 声明全局函数（先声明，后调用）
    window.loadTable = function() {
        var params = {
            userName: $('#q_username').val(),
            role: $('#q_role').val(),
            page: currentPage,
            size: pageSize
        };
        $.ajax({
            url: '/h-job/user/list',
            type: 'GET',
            data: params,
            success: function (res) {
                if (res.code === 200) {
                    var list = res.data.list || [];
                    var total = res.data.total || 0;
                    window.renderTable(list);
                    CommonPagination.render(total, pageSize, currentPage);
                }
            }
        });
    };
    
    // 加载执行器列表
    loadExecutors();
    
    // 初始加载（函数已声明，可以安全调用）
    window.loadTable();

    // 渲染表格（全局函数）
    window.renderTable = function(list) {
        var tbody = $('#dataTable tbody').empty();
        $.each(list, function (i, item) {
            var roleText = item.role === 0 ? '普通用户' : '管理员';
            var tr = $('<tr>' +
                '<td>' + item.id + '</td>' +
                '<td>' + item.userName + '</td>' +
                '<td>' + roleText + '</td>' +
                '<td>' + item.createTime + '</td>' +
                '<td>' +
                '<a href="javascript:;" onclick="editUser(' + item.id + ')" style="display:inline-block;padding:2px 8px;margin-right:5px;background:#3498db;color:#fff;border-radius:3px;text-decoration:none;font-size:12px;">修改</a> ' +
                '<a href="javascript:;" onclick="delUser(' + item.id + ')" style="display:inline-block;padding:2px 8px;background:#e74c3c;color:#fff;border-radius:3px;text-decoration:none;font-size:12px;">删除</a>' +
                '</td>' +
                '</tr>'
            );
            tbody.append(tr);
        });
    }

    // 初始化公共分页组件
    CommonPagination.init({
        callback: function(page) {
            currentPage = page;
            window.loadTable();
        },
        containerId: 'pagination'
    });

});

// 行内修改
window.editUser = function (id) {
    $.ajax({
        url: '/h-job/user/' + id,
        type: 'GET',
        success: function (res) {
            if (res.code === 200) {
                var d = res.data;
                $('#form1')[0].reset();
                $('#form1 input[name=userName]').val(d.userName);
                $('#form1 input[name=password]').val(d.password);
                $('#form1 select[name=role]').val(d.role);
                
                // 加载执行器列表并设置权限
                if (typeof window.executors === 'undefined' || window.executors.length === 0) {
                    window.loadExecutors();
                    // 等待执行器加载完成后设置权限
                    setTimeout(function() {
                        if (typeof window.executors !== 'undefined') {
                            setPermissions(d.permission || '');
                            // 根据角色显示/隐藏权限区域
                            updatePermissionsDisplay(d.role);
                        }
                    }, 500);
                } else {
                    setPermissions(d.permission || '');
                    // 根据角色显示/隐藏权限区域
                    updatePermissionsDisplay(d.role);
                }
                
                var index = layer.open({
                    type: 1,
                    title: '修改用户',
                    area: ['520px', 'auto'],
                    minWidth: 480,
                    minHeight: 350,
                    content: $('#modalDiv'),
                    btn: ['确定', '取消'],
                    yes: function (index, layero) {
                        $('#form1').submit();
                        return false;
                    },
                    cancel: function (index) {
                        layer.close(index);
                    },
                    success: function(layerElem, index) {
                        // 确保内容左对齐
                        $(layerElem).css({'text-align': 'left'});
                        $(layerElem).find('.layui-layer-content').css({'text-align': 'left'});
                        $(layerElem).find('form').css({'text-align': 'left'});
                    }
                });
                
                // 定义更新权限显示的函数（不再调整弹窗大小）
                function updatePermissionsDisplay(role) {
                    if (role == 0) { // 普通用户显示权限
                        $('#permissionGroup').show();
                        $('#permissionStar').show();
                    } else { // 管理员隐藏权限
                        $('#permissionGroup').hide();
                        $('#permissionStar').hide();
                    }
                }
                
                // 根据当前角色初始化显示
                updatePermissionsDisplay(d.role);
                
                // 监听角色选择变化
                $('#form1 select[name=role]').off('change.editRoleChange').on('change.editRoleChange', function() {
                    var role = $(this).val();
                    updatePermissionsDisplay(role);
                });
                $('#modalDiv').data('isAdd', false).data('id', id);
            } else {
                layer.msg(res.msg);
            }
        }
    });
};

// 行内删除
window.delUser = function (id) {
    layer.confirm('确定删除？', function () {
        $.ajax({
            url: '/h-job/user/' + id,
            type: 'DELETE',
            success: function (res) {
                if (res.code === 200) {
                    layer.msg('删除成功');
                    window.loadTable();
                } else {
                    layer.msg(res.msg);
                }
            }
        });
    });
};