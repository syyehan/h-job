$(function () {
    var currentPage = 1;
    var pageSize = 10;
    
    // 设置导航标题
    setNavTitleFromUrl();

    // 解析 URL 参数
    function getUrlParam(name) {
        var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)');
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]);
        return null;
    }
    var jobIdParam = getUrlParam('jobId');

    // 缓存任务描述信息
    var jobDescMap = {};
    // 加载任务下拉列表
    function loadJobSelect() {
        $.ajax({
            url: '/h-job/jobInfo/list',
            type: 'GET',
            data: {page: 1, size: 999},
            success: function (res) {
                if (res.code === 200) {
                    var jobSelect = $('#q_jobId');
                    jobSelect.empty();
                    jobSelect.append('<option value="">全部</option>');
                    $.each(res.data.list || [], function (i, item) {
                        jobDescMap[item.id] = item.jobDesc;
                        jobSelect.append('<option value="' + item.id + '">' + item.jobDesc + '</option>');
                    });
                    // 如果 URL 中有 jobId 参数，设置并触发查询
                    if (jobIdParam) {
                        jobSelect.val(jobIdParam);
                        currentPage = 1;
                        loadTable();
                    }
                }
            }
        });
    }

    // 初始化：先加载任务描述，再绑定事件和加载数据
    $.when(loadJobSelect()).done(function() {
        // 查询
        $('#btn_query').click(function () {
            currentPage = 1;
            loadTable();
        });

        // 重置
        $('#btn_reset').click(function () {
            $('#q_jobId').val('');
            $('#q_executorCode').val('');
            currentPage = 1;
            loadTable();
        });

        // 清空日志
        $('#btn_clear').click(function () {
            layer.confirm('确定清空所有日志？', function () {
                $.ajax({
                    url: '/h-job/jobLog/clear',
                    type: 'DELETE',
                    success: function (res) {
                        if (res.code === 200) {
                            layer.msg('清空成功');
                            loadTable();
                        } else {
                            layer.msg(res.msg);
                        }
                    }
                });
            });
        });
        // 初始加载表格（如果没有 jobId 参数才加载全部）
        if (!jobIdParam) {
            loadTable();
        }
    });

    // 加载表格
    function loadTable() {
        var params = {
            jobId: $('#q_jobId').val(),
            executorCode: $('#q_executorCode').val(),
            page: currentPage,
            size: pageSize
        };
        $.ajax({
            url: '/h-job/jobLog/list',
            type: 'GET',
            data: params,
            success: function (res) {
                if (res.code === 200) {
                    var list = res.data.list || [];
                    var total = res.data.total || 0;
                    renderTable(list);
                    CommonPagination.render(total, pageSize, currentPage);
                }
            }
        });
    }

    // 渲染表格
    function renderTable(list) {
        var tbody = $('#dataTable tbody').empty();
        $.each(list, function (i, item) {
            var executorCode = parseInt(item.executorCode);
            var statusText = '';
            var statusColor = '';
            switch(executorCode) {
                case 0: statusText = '成功'; statusColor = '#5cb85c'; break;
                case 1: statusText = '进行中'; statusColor = '#5bc0de'; break;
                case 2: statusText = '失败'; statusColor = '#d9534f'; break;
                default: statusText = item.executorCode; statusColor = '#cccccc';
            }
            var jobDesc = jobDescMap[item.jobId] || '';
            var tr = $('<tr>' +
                '<td>' + item.id + '</td>' +
                '<td>' + jobDesc + '</td>' +
                '<td>' + item.jobId + '</td>' +
                '<td>' + item.address + '</td>' +
                '<td>' + item.sharding + '</td>' +
                '<td>' + item.failRetryCount + '</td>' +
                '<td>' + item.executorDate + '</td>' +
                '<td>' + (item.resultCode || '') + '</td>' +
                '<td style="text-align:center;"><span style="background-color:' + statusColor + '; padding:2px 8px; border-radius:4px; display:inline-block;">' + statusText + '</span></td>' +
                '<td><button class="btn btn-xs btn-info view-btn" data-id="' + item.id + '">查看</button></td>' +
                '</tr>'
            );
            tbody.append(tr);
        });

        // 绑定查看按钮事件
        $('.view-btn').click(function () {
            var id = $(this).data('id');
            $.ajax({
                url: '/h-job/jobLog/' + id,
                type: 'GET',
                success: function (res) {
                    if (res.code === 200) {
                        var d = res.data;
                        $('#detail_id').text(d.id);
                        $('#detail_jobDesc').text(jobDescMap[d.jobId] || '');
                        $('#detail_jobId').text(d.jobId);
                        $('#detail_address').text(d.address);
                        $('#detail_sharding').text(d.sharding);
                        $('#detail_failRetryCount').text(d.failRetryCount);
                        $('#detail_param').text(d.param);
                        $('#detail_executorDate').text(d.executorDate);
                        var executorStatus = '';
                        var executorColor = '';
                        switch(parseInt(d.executorCode)) {
                            case 0: executorStatus = '成功'; executorColor = '#5cb85c'; break;
                            case 1: executorStatus = '进行中'; executorColor = '#5bc0de'; break;
                            case 2: executorStatus = '失败'; executorColor = '#d9534f'; break;
                            default: executorStatus = d.executorCode; executorColor = '#cccccc';
                        }
                        $('#detail_executorCode').text(executorStatus);
                        $('#detail_resultCode').text(d.resultCode);
                        $('#detail_resultMsg').text(d.resultMsg);
                        layer.open({
                            type: 1,
                            title: '日志详情',
                            area: ['700px', '500px'],
                            content: $('#detailModalDiv')
                        });
                    } else {
                        layer.msg(res.msg);
                    }
                }
            });
        });
    }

    // 初始化公共分页组件
    CommonPagination.init({
        callback: function(page) {
            currentPage = page;
            loadTable();
        },
        containerId: 'pagination'
    });
});