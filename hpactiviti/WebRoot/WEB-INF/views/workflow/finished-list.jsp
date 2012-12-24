<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/taglib.jsp"%>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html lang="en">
<head>
<title>已结束的流程列表</title>
<%@ include file="/WEB-INF/views/common/resource.jsp"%>
<script src="${ctx }/static/js/plugins/qtip/jquery.qtip.pack.js" type="text/javascript"></script>
<script type="text/javascript" src="${ctx }/static/js/workflow.js"></script>
</head>

<body>
	<table width="100%" class="need-border">
		<thead>
			<tr>
				<th>流程ID</th>
				<th>流程定义ID</th>
				<th>流程启动时间</th>
				<th>流程结束时间</th>
				<th>流程结束原因</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${list }" var="hpi">
				<tr>
					<td>${hpi.id }</td>
					<td>${hpi.processDefinitionId }</td>
					<td>${hpi.startTime }</td>
					<td>${hpi.endTime }</td>
					<td>${hpi.deleteReason }</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>

	<!-- 办理任务对话框 -->
	<div id="handleTemplate" class="template"></div>

</body>
</html>
