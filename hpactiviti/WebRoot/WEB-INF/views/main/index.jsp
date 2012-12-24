<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/taglib.jsp"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>

<title>My JSP 'index.jsp' starting page</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<%@ include file="/WEB-INF/views/common/resource.jsp"%>
<script type="text/javascript">
	$(function() {

		$('#').button({
			icons : {
				primary : 'ui-icon-document'
			}
		}).click(function() {

		});
	});
</script>
</head>

<body>
	<ul>
		<li><a id='processDefinitionList' href='${ctx }/workflow/process-list'>部署流程/流程定义列表</a></li>
		<li><a id='' href='${ctx }/workflow/processinstance/task/todo/list'>待办列表</a></li>
		<li><a id='' href='${ctx }/workflow/processinstance/process-instance/running/list'>正在运行实例</a></li>
		<li><a id='' href='${ctx }/workflow/processinstance/process-instance/finished/list'>已结束实例</a></li>
	</ul>
</body>
</html>