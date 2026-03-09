$(function () {
    var currentPage = 1;
    var pageSize = 10;
    window.serverAddressList = []; // 缓存服务地址，暴露到全局供renderServerOptions使用
    var serverAddressList = window.serverAddressList; // 本地引用
    
    // 设置导航标题
    setNavTitleFromUrl();

    // 加载服务地址下拉
    loadServerAddressList(function() {
        // 服务地址加载完成后才初始化表格
        window.loadTable();
    });

    // 查询
    $('#btn_query').click(function () {
        currentPage = 1;
        loadTable();
    });

    // 重置
    $('#btn_reset').click(function () {
        $('#q_jobDesc,#q_jobStatus').val('');
        currentPage = 1;
        loadTable();
    });

    // 新增
    $('#btn_add').click(function () {
        $('#form1')[0].reset();
        $('#form1 select[name=jobServerId]').html('<option value="">请选择</option>' + renderServerOptions());
        layer.open({
            type: 1,
            title: '新增任务配置',
            area: ['600px', '520px'],
            content: $('#modalDiv'),
            btn: ['确定', '取消'],
            yes: function(index, layero) {
                $('#form1').submit();
                return false;
            },
            cancel: function(index) {
                layer.close(index);
            }
        });
        $('#modalDiv').data('isAdd', true);
    });

    // 保存
    $('#form1').submit(function (e) {
        e.preventDefault();
        var data = {};
        $('#form1').serializeArray().forEach(function (item) {
            data[item.name] = item.value;
        });
        var url, type;
        if ($('#modalDiv').data('isAdd')) {
            url = '/h-job/jobInfo';
            type = 'POST';
        } else {
            url = '/h-job/jobInfo';
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

    // 声明全局函数（先声明，后调用）
    window.loadTable = function() {
        var params = {
            jobDesc: $('#q_jobDesc').val(),
            jobServerId: $('#q_serviceName').val(),
            jobStatus: $('#q_jobStatus').val(),
            page: currentPage,
            size: pageSize
        };
        $.ajax({
            url: '/h-job/jobInfo/list',
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


    
    // 初始加载已在loadServerAddressList的回调中处理，这里不再重复调用

    // 根据ID获取服务地址名称
    function getServerAddressNameById(id) {
        var found = serverAddressList.find(function(item) {
            return item.id == id;
        });
        return found ? found.serviceName : ('ID:' + id);
    }

    // 渲染表格（全局函数）
    window.renderTable = function(list) {
        var tbody = $('#dataTable tbody').empty();
        $.each(list, function (i, item) {
            var statusText = item.jobStatus === 0 ? '停止' : '运行中';
            var serverAddressName = getServerAddressNameById(item.jobServerId);
            var toggleText = item.jobStatus === 0 ? '启动' : '停止';
            var toggleClass = item.jobStatus === 0 ? 'btn-success' : 'btn-warning';
            var methodText = item.method || '-';
            var tr = $('<tr>' +
                '<td>' + item.id + '</td>' +
                '<td title="' + serverAddressName + '">' + serverAddressName + '</td>' +
                '<td title="' + item.jobDesc + '">' + item.jobDesc + '</td>' +
                '<td>' + methodText + '</td>' +
                '<td>' + item.cron + '</td>' +
                '<td>' + (item.routeStrategy === 'first' ? '第一个' : item.routeStrategy === 'last' ? '最后一个' : item.routeStrategy === 'round' ? '轮询' : item.routeStrategy === 'random' ? '随机' : item.routeStrategy) + '</td>' +
                '<td>' + item.path + '</td>' +
                '<td style="text-align:center; font-weight:bold;"><span style="background-color:' + (item.jobStatus === 0 ? '#dcdcdc' : '#5cb85c') + '; padding:2px 8px; border-radius:4px; display:inline-block;">' + statusText + '</span></td>' +
                '<td>' + item.createTime + '</td>' +
                '<td>' +
                (item.jobStatus !== 1 ? '<button class="btn btn-xs btn-primary" onclick="editJobInfo(' + item.id + ')">修改</button> ' : '') +
                '<button class="btn btn-xs ' + toggleClass + '" onclick="toggleJobStatus(' + item.id + ',' + item.jobStatus + ')">' + toggleText + '</button> ' +
                '<button class="btn btn-xs btn-danger" onclick="delJobInfo(' + item.id + ')">删除</button> ' +
                '<button class="btn btn-xs btn-warning" onclick="loadJobLog(' + item.id + ')">日志</button> ' + '<br>'+
                '<button class="btn btn-xs btn-info" onclick="showNextTime(' + item.id + ')">下次执行时间</button> ' +
                '<button class="btn btn-xs btn-info" onclick="executeJobInfo(' + item.id + ')">立即执行</button> ' +

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

    // 加载服务地址
    function loadServerAddressList(callback) {
        $.ajax({
            url: '/h-job/serverAddress/list',
            type: 'GET',
            data: {page: 1, size: 9999},
            success: function (res) {
                if (res.code === 200) {
                    serverAddressList = res.data.list || [];
                    window.serverAddressList = serverAddressList; // 更新全局变量
                    // 填充服务名称下拉框
                    var $serviceSelect = $('#q_serviceName');
                    $serviceSelect.empty().append('<option value="">全部服务</option>');
                    $.each(serverAddressList, function(i, item) {
                        $serviceSelect.append('<option value="' + item.id + '">' + item.serviceName + '</option>');
                    });
                    if (callback) callback();
                }
            }
        });
    }
});

// 渲染服务地址下拉选项（全局函数）
function renderServerOptions() {
    var html = '';
    $.each(window.serverAddressList || [], function (i, item) {
        html += '<option value="' + item.id + '">' + item.serviceName + '</option>';
    });
    return html;
}

// 全局修改
window.editJobInfo = function (id) {
    // 如果服务地址列表为空，先加载
    if (!window.serverAddressList || window.serverAddressList.length === 0) {
        loadServerAddressList(function() {
            loadJobInfoAndShowModal(id);
        });
    } else {
        loadJobInfoAndShowModal(id);
    }
};

// 加载任务信息并显示模态框
function loadJobInfoAndShowModal(id) {
    $.ajax({
        url: '/h-job/jobInfo/' + id,
        type: 'GET',
        success: function (res) {
            if (res.code === 200) {
                var d = res.data;
                $('#form1')[0].reset();
                $('#form1 select[name=jobServerId]').html('<option value="">请选择</option>' + renderServerOptions());
                // 确保DOM更新后设置选中值
                setTimeout(function() {
                    $('#form1 select[name=jobServerId]').val(d.jobServerId);
                }, 10);
                $('#form1 input[name=jobDesc]').val(d.jobDesc);
                $('#form1 input[name=createUser]').val(d.createUser);
                $('#form1 input[name=cron]').val(d.cron);
                $('#form1 select[name=routeStrategy]').val(d.routeStrategy);
                $('#form1 select[name=method]').val(d.method || '');
                $('#form1 input[name=path]').val(d.path);
                $('#form1 textarea[name=param]').val(d.param);
                $('#form1 input[name=executorTimeout]').val(d.executorTimeout);
                $('#form1 input[name=executorFailRetryCount]').val(d.executorFailRetryCount);
                layer.open({
                    type: 1,
                    title: '修改任务配置',
                    area: ['600px', '520px'],
                    content: $('#modalDiv'),
                    btn: ['确定', '取消'],
                    yes: function (index, layero) {
                        $('#form1').submit();
                        return false;
                    },
                    cancel: function (index) {
                        layer.close(index);
                    }
                });
                $('#modalDiv').data('isAdd', false).data('id', id);
            } else {
                layer.msg(res.msg);
            }
        }
    });
}

// 全局切换任务状态（启动/停止）
window.toggleJobStatus = function (id, currentStatus) {
    var actionText = currentStatus === 0 ? '启动' : '停止';
    var newStatus = currentStatus === 0 ? 1 : 0;
    
    layer.confirm('确定要' + actionText + '该任务吗？', function () {
        $.ajax({
            url: '/h-job/jobInfo/changeStatus',
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({id: id, jobStatus: newStatus}),
            success: function (res) {
                if (res.code === 200) {
                    layer.msg(actionText + '成功');
                    loadTable();
                } else {
                    layer.msg(res.msg || actionText + '失败');
                }
            },
            error: function () {
                layer.msg(actionText + '失败，请稍后重试');
            }
        });
    });
};

// 全局删除
window.delJobInfo = function (id) {
    layer.confirm('确定删除？', function () {
        $.ajax({
            url: '/h-job/jobInfo/' + id,
            type: 'DELETE',
            success: function (res) {
                if (res.code === 200) {
                    layer.msg('删除成功');
                    loadTable();
                } else {
                    layer.msg(res.msg);
                }
            }
        });
    });
};

// 全局立即执行任务
window.executeJobInfo = function (id) {
        var promptIndex;
        var contentHtml = '<textarea id="execParamInput" style="width:99%;height:100px;padding:10px;box-sizing:border-box;"></textarea>';
        promptIndex = layer.open({
            type: 1,
            title: '请输入执行参数',
            area: ['400px', '200px'],
            content: contentHtml,
            btn: ['确定', '取消'],
            yes: function(index, layero) {
                var text = $('#execParamInput').val();
                layer.close(index);
                $.ajax({
                    url: '/h-job/jobInfo/execute/' + id + '?param=' + encodeURIComponent((text || '').trim()),
                    type: 'GET',
                    success: function (res) {
                        layer.msg('任务执行成功');
                    },
                    error: function () {
                        layer.msg('任务执行成功');
                    }
                });
            },
            btn2: function(index, layero) {
                layer.close(index);
                return false;
            }
        });
    };

// 全局查看下次执行时间
window.showNextTime = function (id) {
    $.ajax({
        url: '/h-job/jobInfo/getNextTime/' + id,
        type: 'GET',
        success: function (res) {
            if (res.code === 200 && res.data) {
                var times = res.data.join('</br>');
                layer.open({
                    type: 1,
                    title: '下次执行时间',
                    area: ['300px', 'auto'],
                    content: '<div style="padding: 20px 25px; text-align: center;">' + times + '</div>'
                });
            } else {
                layer.msg(res.msg || '获取失败');
            }
        },
        error: function () {
            layer.msg('请求失败');
        }
    });
};

// 查看任务日志（旧方法，会改变地址栏）
window.viewJobLogs = function(jobId) {
    // 直接跳转并改变地址栏，同时传递 jobId 参数
    window.parent.location.hash = 'job_log.html?jobId=' + jobId;
};

// 加载任务日志（新方法，使用iframe嵌入，无刷新且地址栏不变）
window.loadJobLog = function(jobId) {
    // 检查是否已存在日志iframe
    var $existingFrame = $('#jobLogFrame');
    if ($existingFrame.length > 0) {
        // 如果已存在，只需更新src
        $existingFrame.attr('src', 'job_log.html?jobId=' + jobId);
        return;
    }
    
    // 创建遮罩层
    var maskHtml = '<div id="jobLogMask" style="position:fixed;top:0;left:0;width:100%;height:100%;background:#fff;z-index:9998;"></div>';
    $('body').append(maskHtml);
    
    // 创建iframe容器
    var frameHtml = '<div id="jobLogContainer" style="position:fixed;top:0;left:0;width:100%;height:100%;z-index:9999;background:#fff;">' +
        '<div style="padding:10px;background:#f5f5f5;border-bottom:1px solid #ddd;" class="frame-header" style="display:none;">' +
        '<button onclick="closeJobLogFrame()" style="padding:5px 10px;cursor:pointer;">← 返回任务列表</button>' +
        '</div>' +
        '<iframe id="jobLogFrame" src="job_log.html?jobId=' + jobId + '" style="width:100%;height:calc(100vh - 50px);border:none;" frameborder="0"></iframe>' +
        '</div>';
    
    $('body').append(frameHtml);
    
    // 隐藏原有的数据表格区域，保留导航等
    $('#dataTable').closest('.row').hide();
    $('.pagination').closest('.row').hide();
    
    // 添加返回功能到父窗口
    window.closeJobLogFrame = function() {
        $('#jobLogContainer').remove();
        $('#jobLogMask').remove();
        $('#dataTable').closest('.row').show();
        $('.pagination').closest('.row').show();
        delete window.closeJobLogFrame;
    };
};