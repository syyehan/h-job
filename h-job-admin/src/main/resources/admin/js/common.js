//工具集合Tools
window.T = {};

// 动态插入 favicon（适配 context-path=/h-job）
(function () {
    var link = document.createElement('link');
    link.rel = 'icon';
    link.href = '/h-job/favicon.ico';
    link.type = 'image/x-icon';
    document.head.appendChild(link);
})();

// 获取请求参数
var url = function(name) {
	var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
	var r = window.location.search.substr(1).match(reg);
	if(r!=null)return  unescape(r[2]); return null;
};
T.p = url;

// //全局配置
// $.ajaxSetup({
// 	dataType: "json",
// 	contentType: "application/json",
// 	cache: false,
// 	beforeSend: function(xhr) {
// 		var token = getToken();
// 		if (token) {
// 			xhr.setRequestHeader('hjob_token', token);
// 		}
// 	},
// 	complete: function(xhr, status) {
// 		if (status === 'success' && xhr.responseJSON) {
// 			var res = xhr.responseJSON;
// 			if (res.code === 1000) {
// 				removeToken();
// 				var loginUrl = window.location.origin + '/h-job/admin/views/login.html';
// 				console.log(loginUrl);
// 				window.open(loginUrl, '_blank'); // 新 tab 打开
// 				window.close(); // 尝试关闭当前页
// 				// window.location.href = loginUrl;
// 			}
// 		}
// 	}
// });

// 确保 jQuery 加载后再执行 ajaxSetup
function initAjaxSetup() {
	if (window.jQuery) {
		$.ajaxSetup({
			dataType: "json",
			contentType: "application/json",
			cache: false,
			beforeSend: function(xhr) {
				var token = getToken();
				if (token) {
					xhr.setRequestHeader('hjob_token', token);
				}
			},
			complete: function(xhr, status) {
				if (status === 'success' && xhr.responseJSON) {
					var res = xhr.responseJSON;
					if (res.code === 1000) {
						removeToken();
						var loginUrl = window.location.origin + '/h-job/admin/views/login.html';
						console.log(loginUrl);
						window.open(loginUrl, '_blank');
						window.close();
					}
				}
			}
		});
	} else {
		// 如果 jQuery 还没加载，等待一下再试
		setTimeout(initAjaxSetup, 100);
	}
}

initAjaxSetup();


//重写alert
window.alert = function(msg, callback){
	parent.layer.alert(msg, function(index){
		parent.layer.close(index);
		if(typeof(callback) === "function"){
			callback("ok");
		}
	});
}

//重写confirm式样框
window.confirm = function(msg, callback){
	parent.layer.confirm(msg, {btn: ['确定','取消']},
	function(){//确定事件
		if(typeof(callback) === "function"){
			callback("ok");
		}
	});
}

// 设置导航标题
function setNavTitleFromUrl() {
    var navTitle = T.p('navTitle');
    if (navTitle) {
        var decodedTitle = decodeURIComponent(navTitle);
        // 方式1: 直接更新DOM元素
        var navTitleElement = document.getElementById('navTitleText');
        if (navTitleElement) {
            navTitleElement.textContent = decodedTitle;
        }
        // 方式2: 同时更新Vue实例（如果存在）
        if (typeof vm !== 'undefined' && vm.navTitle !== undefined) {
            vm.navTitle = decodedTitle;
        }
    }
}

//选择一条记录
function getSelectedRow() {
    var grid = $("#jqGrid");
    var rowKey = grid.getGridParam("selrow");
    if(!rowKey){
    	alert("请选择一条记录");
    	return ;
    }

    var selectedIDs = grid.getGridParam("selarrrow");
    if(selectedIDs.length > 1){
    	alert("只能选择一条记录");
    	return ;
    }

    return selectedIDs[0];
}

//选择多条记录
function getSelectedRows() {
    var grid = $("#jqGrid");
    var rowKey = grid.getGridParam("selrow");
    if(!rowKey){
    	alert("请选择一条记录");
    	return ;
    }

    return grid.getGridParam("selarrrow");
}

// Token 管理方法
function getToken() {
	return localStorage.getItem('hjob_token') || '';
}

function setToken(token) {
	localStorage.setItem('hjob_token', token);
}

function removeToken() {
	localStorage.removeItem('hjob_token');
}

// 全局 Ajax 拦截，自动添加 token
if (window.jQuery) {
	$.ajaxSetup({
		beforeSend: function(xhr) {
			var token = getToken();
			if (token) {
				xhr.setRequestHeader('hjob_token', token);
			}
		}
	});
}
