// 声明全局函数（先声明，后调用）
// 初始化公共分页组件
CommonPagination.init({
    callback: function(page) {
        window.currentPage = page;
        window.loadTable();
    },
    containerId: 'pagination'
});

window.loadTable = function() {
    var params = {
        serviceName: $('#q_serviceName').val(),
        title: $('#q_title').val(),
        page: window.currentPage,
        size: window.pageSize
    };
    $.ajax({
        url: '/h-job/serverAddress/list',
        type: 'GET',
        data: params,
        success: function (res) {
            if (res.code === 200) {
                var list = res.data.list || [];
                var total = res.data.total || 0;
                window.renderTable(list);
                CommonPagination.render(total, window.pageSize, window.currentPage);
            }
        }
    });
}

window.renderTable = function(list) {
    var tbody = $('#dataTable tbody').empty();
    $.each(list, function (i, item) {
        var typeText = item.addressType === 0 ? '自动注册' : '手动录入';
        var tr = $('<tr>' +
            '<td>' + item.id + '</td>' +
            '<td>' + item.serviceName + '</td>' +
            '<td>' + item.title + '</td>' +
            '<td>' + typeText + '</td>' +
            '<td>' + item.nacosNamespace + '</td>' +
            '<td>' + item.nacosGroup + '</td>' +
            '<td>' + item.addressList + '</td>' +
            '<td>' + item.createTime + '</td>' +
            '<td>' +
            '<button class="btn btn-xs btn-primary" onclick="editServerAddress(' + item.id + ')">修改</button> ' +
            '<button class="btn btn-xs btn-danger" onclick="delServerAddress(' + item.id + ')">删除</button>' +
            '</td>' +
            '</tr>'
        );
        tbody.append(tr);
    });
}


// 切换地址列表编辑状态（全局函数，供外部调用）
window.toggleAddressListEdit = function() {
    var addressType = $('select[name=addressType]').val();
    var addressListTextarea = $('textarea[name=addressList]');
    var addressListLabel = $('label[for=""]').filter(function() {
        return $(this).text().indexOf('地址列表') >= 0;
    }).parent().parent();
    
    if (addressType === '0') { // 自动注册
        addressListTextarea.prop('readonly', true);
        addressListTextarea.css({'background-color': '#f5f5f5', 'cursor': 'not-allowed'});
        addressListTextarea.attr('placeholder', '自动注册模式下地址列表由Nacos自动获取，无需手动填写');
    } else { // 手动录入
        addressListTextarea.prop('readonly', false);
        addressListTextarea.css({'background-color': '', 'cursor': ''});
        addressListTextarea.attr('placeholder', '多个地址用逗号分隔,手动录入(必填)');
    }
}

$(function () {
    window.currentPage = 1;
    window.pageSize = 10;
    
    // 设置导航标题
    setNavTitleFromUrl();

    // 查询
    $('#btn_query').click(function () {
        window.currentPage = 1;
        window.loadTable();
    });

    // 重置
    $('#btn_reset').click(function () {
        $('#q_serviceName,#q_title').val('');
        window.currentPage = 1;
        window.loadTable();
    });

    // 新增
    $('#btn_add').click(function () {
        $('#form1')[0].reset();
        $('select[name=addressType]').val('1');
        window.toggleAddressListEdit();
        layer.open({
            type: 1,
            title: '新增服务地址',
            area: ['600px', '400px'],
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
            url = '/h-job/serverAddress';
            type = 'POST';
        } else {
            url = '/h-job/serverAddress';
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
                    window.loadTable();
                } else {
                    layer.msg(res.msg);
                }
            }
        });
    });

    // 地址类型切换事件
    $('select[name=addressType]').change(function () {
        window.toggleAddressListEdit();
    });

    // 首次加载 - 改为在DOM ready完成后调用全局函数
    $(document).ready(function() {
        setTimeout(function() {
            window.loadTable();
        }, 0);
    });

    // 加载表格（全局函数）
    window.loadTable = function() {
        var params = {
            serviceName: $('#q_serviceName').val(),
            title: $('#q_title').val(),
            page: currentPage,
            size: pageSize
        };
        $.ajax({
            url: '/h-job/serverAddress/list',
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
    }

    // 渲染表格（全局函数）
    window.renderTable = function(list) {
        var tbody = $('#dataTable tbody').empty();
        $.each(list, function (i, item) {
            var typeText = item.addressType === 0 ? '自动注册' : '手动录入';
            var tr = $('<tr>' +
                '<td>' + item.id + '</td>' +
                '<td>' + item.serviceName + '</td>' +
                '<td>' + item.title + '</td>' +
                '<td>' + typeText + '</td>' +
                '<td>' + item.nacosNamespace + '</td>' +
                '<td>' + item.nacosGroup + '</td>' +
                '<td>' + item.addressList + '</td>' +
                '<td>' + item.createTime + '</td>' +
                '<td>' +
                '<button class="btn btn-xs btn-primary" onclick="editServerAddress(' + item.id + ')">修改</button> ' +
                '<button class="btn btn-xs btn-danger" onclick="delServerAddress(' + item.id + ')">删除</button>' +
                '</td>' +
                '</tr>'
            );
            tbody.append(tr);
        });
    }

});


// 全局修改
window.editServerAddress = function (id) {
    $.ajax({
        url: '/h-job/serverAddress/' + id,
        type: 'GET',
        success: function (res) {
            if (res.code === 200) {
                var d = res.data;
                $('#form1 input[name=serviceName]').val(d.serviceName);
                $('#form1 input[name=title]').val(d.title);
                $('#form1 select[name=addressType]').val(d.addressType);
                $('#form1 input[name=nacosNamespace]').val(d.nacosNamespace);
                $('#form1 input[name=nacosGroup]').val(d.nacosGroup);
                $('#form1 textarea[name=addressList]').val(d.addressList);
                layer.open({
                    type: 1,
                    title: '修改服务地址',
                    area: ['600px', '400px'],
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
                // 立即设置地址列表编辑状态，不依赖setTimeout
                window.toggleAddressListEdit();
            } else {
                layer.msg(res.msg);
            }
        }
    });
};

// 全局删除
window.delServerAddress = function (id) {
    layer.confirm('确定删除？', function () {
        $.ajax({
            url: '/h-job/serverAddress/' + id,
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
}