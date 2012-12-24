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
		$('.trace').click(graphTrace);
	});

	/**
	 * 动态Form办理功能
	 */
	$(function() {

		$('.handle').click(handle);

	});

	/**
	 * 打开办理对话框
	 */
	function handle() {
		var $ele = $(this);

		// 当前节点的英文名称
		var tkey = $(this).attr('tkey');

		// 当前节点的中文名称
		var tname = $(this).attr('tname');

		// 任务ID
		var taskId = $(this).attr('tid');

		$('#handleTemplate').html('').dialog({
			modal : true,
			width : $.common.window.getClientWidth() * 0.8,
			height : $.common.window.getClientHeight() * 0.9,
			title : '办理任务[' + tname + ']',
			open : function() {
				readForm.call(this, taskId);
			},
			buttons : [ {
				text : '提交',
				click : function() {
					$('.formkey-form').submit();
				}
			}, {
				text : '关闭',
				click : function() {
					$(this).dialog('close');
				}
			} ]
		});
	}

	/**
	 * 读取任务表单
	 */
	function readForm(taskId) {
		var dialog = this;

		// 读取启动时的表单
		$.get(ctx + '/workflow/processinstance/get-form/task/' + taskId,
				function(form) {
					// 获取的form是字符行，html格式直接显示在对话框内就可以了，然后用form包裹起来
					$(dialog).html(form).wrap(
							"<form class='formkey-form' method='post' />");

					var $form = $('.formkey-form');

					// 设置表单action
					$form.attr('action', ctx
							+ '/workflow/processinstance/task/complete/'
							+ taskId);

				});
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
	<table width="100%" class="need-border">
		<thead>
			<tr>
				<th>任务ID</th>
				<th>任务Key</th>
				<th>任务名称</th>
				<th>流程定义ID</th>
				<th>流程实例ID</th>
				<th>优先级</th>
				<th>任务创建日期</th>
				<th>任务逾期日</th>
				<th>任务描述</th>
				<th>任务所属人</th>
				<th>操作</th>
			</tr>
		</thead>
		<tbody>

			<c:forEach items="${todo }" var="task">
				<tr>
					<td>${task.id }</td>
					<td>${task.taskDefinitionKey }</td>
					<td>${task.name }</td>
					<td>${task.processDefinitionId }</td>
					<td>${task.processInstanceId }</td>
					<td>${task.priority }</td>
					<td>${task.createTime }</td>
					<td>${task.dueDate }</td>
					<td>${task.description }</td>
					<td>${task.owner }</td>
					<td><a class="handle" tkey='${task.taskDefinitionKey }' tname='${task.name }' tid='${task.id }' href="#">办理</a> |<a class='trace' href='#'
						pid='${task.processInstanceId }' title='点击查看流程图'>跟踪</a></td>
				</tr>
			</c:forEach>
			<c:forEach items="${claim }" var="task">
				<tr>
					<td>${task.id }</td>
					<td>${task.taskDefinitionKey }</td>
					<td>${task.name }</td>
					<td>${task.processDefinitionId }</td>
					<td>${task.processInstanceId }</td>
					<td>${task.priority }</td>
					<td>${task.createTime }</td>
					<td>${task.dueDate }</td>
					<td>${task.description }</td>
					<td>${task.owner }</td>
					<td><a class="claim" href="${ctx }/workflow/processinstance/task/claim/${task.id}">签收</a> |<a class='trace' href='#' pid='${task.processInstanceId }' title='点击查看流程图'>跟踪</a>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>

	<!-- 办理任务对话框 -->
	<div id="handleTemplate" class="template"></div>

</body>

</html>
