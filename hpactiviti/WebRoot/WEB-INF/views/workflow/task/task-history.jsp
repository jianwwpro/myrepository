<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/taglib.jsp"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>

<title>待办任务</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<%@ include file="/WEB-INF/views/common/resource.jsp"%>
<script src="${ctx }/static/js/plugins/qtip/jquery.qtip.pack.js" type="text/javascript"></script>
<script type="text/javascript" src="${ctx }/static/js/workflow.js"></script>
<script type="text/javascript">
	$(function() {
		$('.task-history').click(taskHistory);
	});
	function taskHistory() {

	}
</script>


</head>

<body>
	<c:if test="${not empty message}">
		<div id="message" class="alert alert-success">${message}</div>
		<!-- 自动隐藏提示信息 -->
		<script type="text/javascript">
			setTimeout(function() {
				$('#message').hide('slow');
			}, 5000);
		</script>
	</c:if>

	<table>
		<tr>
			<th>任务名称</th>
			<th>执行者</th>
			<th>开始时间</th>
			<th>结束时间</th>
			<th>意见</th>
		</tr>

		<c:forEach items="${list }" var="object">
			<tr>
				<td>${object.tname }</td>
				<td>${object.tassignee}</td>
				<td>${object.tstarttime }</td>
				<td>${object.tendtime }</td>
				<td>${object.tcomment }</td>
			</tr>
		</c:forEach>

	</table>

	<!-- 办理任务对话框 -->
	<div id="handleTemplate" class="template"></div>

</body>

</html>
