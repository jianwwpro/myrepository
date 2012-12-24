<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/taglib.jsp"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>

<html>
<head>

<title><decorator:title /></title>

<decorator:head></decorator:head>
<style type="text/css">
#menu {
	list-style: none;
	float: left;
}

#menu li {
	float: left;
	margin-right: 5px;
}

#menu li a {
	text-decoration: none;
	padding: 5 3;
	background: #FF9224;
	color: black;
}

#menu li a:HOVER {
	background: #D9B300;
}
</style>

<script type="text/javascript">
$(function(){
	$("#menu li a").click(function(a){
		$(this).css("background","#D9B300");
	});	
});


</script>
</head>


<body>
	<div style="margin-top: 20px;">
		<ul id="menu">
			<li><a id='processDefinitionList' href='${ctx }/workflow/process-list'>流程定义列表</a></li>
			<li><a id='' href='${ctx }/workflow/processinstance/task/todo/list'>待办列表</a></li>
			<li><a id='' href='${ctx }/workflow/processinstance/process-instance/running/list'>正在运行实例</a></li>
			<li><a id='' href='${ctx }/workflow/processinstance/process-instance/finished/list'>已结束实例</a></li>
		</ul>
	</div>
	<div style="margin-top: 10px;">
		<decorator:body />
	</div>

</body>
</html>