//iframe自适应
$(window).on('resize', function() {
	var $content = $('.content');
	$content.height($(this).height() - 120);
	$content.find('iframe').each(function() {
		$(this).height($content.height());
	});
}).resize();

var vm = new Vue({
	el:'#rrapp',
	data:{
		main: 'main.html',
		navTitle:"欢迎页"
	}
});

//路由
var router = new Router();
var menus = ["main.html","server_address.html","node_info.html","job_info.html","job_log.html","user.html"];
routerList(router, menus);
router.start();

function routerList(router, menus){
	for(var index in menus){
		router.add('#'+menus[index], function() {
			var url = window.location.hash;

			//替换iframe的url
			vm.main = url.replace('#', '');

			//导航菜单展开
			$(".treeview-menu li").removeClass("active");
			$("a[href='"+url+"']").parents("li").addClass("active");
			
			// 处理node_info.html的特殊情况
			if(url.includes('node_info.html')) {
				$("a[href='#node_info.html']").parents("li").addClass("active");
				vm.navTitle = "节点管理";
			} else {
				vm.navTitle = $("a[href='"+url+"']").text();
			}
		});
	}
}
