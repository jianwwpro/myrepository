<%@page import="com.wang.activiti.util.ProcessDefinitionCache,org.activiti.engine.RepositoryService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/taglib.jsp"%>

<!DOCTYPE html>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html lang="en">
<head>
<title>运行中流程列表</title>
<%@ include file="/WEB-INF/views/common/resource.jsp"%>
<script src="${ctx }/static/js/plugins/qtip/jquery.qtip.pack.js" type="text/javascript"></script>
<script type="text/javascript" src="${ctx }/static/js/workflow.js"></script>
<script type="text/javascript">
	$(function() {
		// 跟踪
		$('.trace').click(graphTrace);
		$('.deleteProcessInstance').click(deleteProcessInstance);
	});

	/**
	 * 打开删除页面
	 */
	function deleteProcessInstance() {
		var $ele = $(this);
		$('<div/>', {
			'class' : 'dynamic-form-dialog',
			title : '删除流程实例[' + $ele.attr("pid") + ']',
			html : '<span class="ui-loading">请稍等……</span>'
		}).dialog({
			modal : true,
			width : $.common.window.getClientWidth() * 0.5,
			height : $.common.window.getClientHeight() * 0.5,
			open : function() {
				// 获取json格式的表单数据，就是流程定义中的所有field

				readForm.call(this, $ele.attr("pid"));
			},
			buttons : [ {
				text : '提交',
				click : submitDelete
			} ]
		});
	}
	/**
	 * 读取任务表单
	 */
	function readForm(processInstanceId) {
		var dialog = this;
		var html = "<table><tr><td>删除原因:</td><td><textarea name='reson' rows='5' cols='10'></textarea></td> </tr> </table>";
		$(dialog).html(html)
				.wrap("<form class='formkey-form' method='post' />");

		var $form = $('.formkey-form');
		// 设置表单action
		$form.attr('action', ctx + '/workflow/processinstance/delete/'
				+ processInstanceId);

	}
	/**
	 *提交删除原因
	 */
	function submitDelete() {
		var $form = $('.formkey-form');
		$form.submit();
	}
</script>
</head>

<body>
	<%
		RepositoryService repositoryService = WebApplicationContextUtils
				.getWebApplicationContext(session.getServletContext())
				.getBean(org.activiti.engine.RepositoryService.class);
		ProcessDefinitionCache.setRepositoryService(repositoryService);
	%>

	<table width="100%" class="need-border">
		<thead>
			<tr>
				<th>执行ID</th>
				<th>流程实例ID</th>
				<th>流程定义ID</th>
				<th>当前节点</th>
				<th>是否挂起</th>
				<th>操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${list }" var="p">
				<c:set var="process" value="${p}" />



				<%
					pageContext
								.setAttribute(
										"isSuspended",
										((org.activiti.engine.runtime.ProcessInstance) pageContext
												.getAttribute("process")).isSuspended());
				%>
				<c:set var="pdid" value="${p.processDefinitionId }" />
				<c:set var="activityId" value="${p.activityId }" />
				<tr>
					<td>${p.id }</td>
					<td>${p.processInstanceId }</td>
					<td>${p.processDefinitionId }</td>
					<td><a class="trace" href='#' pid="${p.id }" title="点击查看流程图"><%=ProcessDefinitionCache.getActivityName(pageContext
						.getAttribute("pdid").toString(), pageContext
						.getAttribute("activityId").toString())%></a>
					</td>
					<td>${isSuspended} | <c:if test="${isSuspended }">
							<a href="processdefinition/update/active/${process.id}">激活</a>
						</c:if> <c:if test="${!isSuspended }">
							<a href="processdefinition/update/suspend/${process.id}">挂起</a>
						</c:if></td>
					<td><a href="#" pid="${process.id}" class="deleteProcessInstance">删除</a>
				</tr>
			</c:forEach>
		</tbody>
	</table>

	<!-- 办理任务对话框 -->
	<div id="handleTemplate" class="template"></div>

</body>
</html>
