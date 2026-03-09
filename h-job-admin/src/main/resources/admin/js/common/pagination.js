// 公共分页组件
// 使用方式：
// 1. 引入此文件
// 2. CommonPagination.init({callback: loadTable, containerId: 'pagination'})
// 3. CommonPagination.render(total, pageSize, currentPage)

window.CommonPagination = {
    // 配置项
    config: {
        callback: null,      // 分页点击回调函数
        containerId: 'pagination', // 分页容器ID
        loadDataFunc: null   // 加载数据的函数名（兼容旧代码）
    },
    
    // 初始化
    init: function(options) {
        this.config = Object.assign({}, this.config, options);
        return this;
    },
    
    // 渲染分页
    render: function(total, size, page) {
        var pages = Math.ceil(total / size);
        var containerId = this.config.containerId;
        var ul = $('#' + containerId).empty();
        
        if (pages <= 0) {
            return;
        }
        
        // 最多显示7个页码，避免页码过多
        var maxPages = 7;
        var startPage = 1;
        var endPage = pages;
        
        if (pages > maxPages) {
            if (page <= 4) {
                endPage = maxPages;
            } else if (page >= pages - 3) {
                startPage = pages - maxPages + 1;
            } else {
                startPage = page - 3;
                endPage = page + 3;
            }
        }
        
        // 上一页
        if (page > 1) {
            var prevLi = $('<li class="page-item"><a href="javascript:;" class="page-link">&laquo;</a></li>');
            prevLi.find('a').click(this._createClickHandler(page - 1));
            ul.append(prevLi);
        }
        
        // 页码
        for (var i = startPage; i <= endPage; i++) {
            var li = $('<li class="page-item"></li>');
            if (i === page) li.addClass('active');
            li.append('<a href="javascript:;" class="page-link">' + i + '</a>');
            li.find('a').click(this._createClickHandler(i));
            ul.append(li);
        }
        
        // 下一页
        if (page < pages) {
            var nextLi = $('<li class="page-item"><a href="javascript:;" class="page-link">&raquo;</a></li>');
            nextLi.find('a').click(this._createClickHandler(page + 1));
            ul.append(nextLi);
        }
        
        // 显示总记录数
        if (total > 0) {
            ul.append('<li class="page-item disabled"><span class="page-link">共 ' + total + ' 条</span></li>');
        }
    },
    
    // 创建点击处理器（绑定this上下文）
    _createClickHandler: function(targetPage) {
        var self = this;
        return function() {
            if (self.config.callback) {
                self.config.callback(targetPage);
            } else if (self.config.loadDataFunc && window[self.config.loadDataFunc]) {
                // 兼容旧的全局函数方式
                window.currentPage = targetPage;
                window[self.config.loadDataFunc]();
            }
        };
    },
    
    // 兼容旧版本的全局函数（可选，用于平滑迁移）
    renderCompat: function(total, size, page, loadDataFuncName) {
        this.config.loadDataFunc = loadDataFuncName;
        this.render(total, size, page);
    }
};