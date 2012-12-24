<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/taglib.jsp"%>


<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<html>
<head>

<title>流程部署对象</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<%@ include file="/WEB-INF/views/common/resource.jsp"%>


<script type="text/javascript">
	$(function() {
		$('#redeploy').button({
			icons : {
				primary : 'ui-icon-refresh'
			}
		});
		$('#deploy').button({
			icons : {
				primary : 'ui-icon-document'
			}
		}).click(function() {
			$('#deployFieldset').toggle('normal');
		});

		$('.startup-process').click(showStartupProcessDialog);

	});

	/**
	 * 打开启动流程
	 */

	function showStartupProcessDialog() {
		var $ele = $(this);
		$(
				'<div/>',
				{
					'class' : 'dynamic-form-dialog',
					title : '启动流程['
							+ $ele.parents('tr').find('.process_name').text()
							+ ']',
					html : '<span class="ui-loading">正在读取表单……</span>'
				}).dialog(
				{
					modal : true,
					width : $.common.window.getClientWidth() * 0.8,
					height : $.common.window.getClientHeight() * 0.9,
					open : function() {
						// 获取json格式的表单数据，就是流程定义中的所有field
						var processDefinitionId = $ele.parents('tr').find(
								'.process-id').text();

						readForm.call(this, processDefinitionId);
					},
					buttons : [ {
						text : '启动流程',
						click : sendStartupRequest
					} ]
				});
	}
	/**
	 * 读取流程启动表单
	 */
	function readForm(processDefinitionId) {

		var dialog = this;

		// 读取启动时的表单

		$.get(ctx + '/workflow/processinstance/get-form/start/'
				+ processDefinitionId, function(form) {
			// 获取的form是字符行，html格式直接显示在对话框内就可以了，然后用form包裹起来
			$(dialog).html(form).wrap(
					"<form class='formkey-form' method='post' />");

			var $form = $('.formkey-form');

			// 设置表单action
			$form.attr('action', ctx
					+ '/workflow/processinstance/start-process/'
					+ processDefinitionId);

		});
	}
	/**
	 * 提交表单
	 * @return {[type]} [description]
	 */
	function sendStartupRequest() {
		$('.formkey-form').submit();

	}
</script>
</head>
<c:if test="${not empty message}">
	<div class="ui-widget">
		<div class="ui-state-highlight ui-corner-all" style="margin-top: 20px; padding: 0 .7em;">
			<p>
				<span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span> <strong>提示：</strong>${message}
			</p>
		</div>
	</div>
</c:if>
<div style="text-align: right;padding: 2px 1em 2px">

	<a id='deploy' href='#'>部署流程</a>
	<!-- <a id='redeploy' href='${ctx }/workflow/redeploy/all'>重新部署流程</a> -->
</div>
<fieldset id="deployFieldset" style="display: none">
	<legend>部署新流程</legend>
	<div>
		<b>支持文件格式：</b>zip、bar、bpmn、bpmn20.xml
	</div>
	<form action="${ctx }/workflow/deploy" method="post" enctype="multipart/form-data">
		<input type="file" name="file" /> <input type="submit" value="Submit" />
	</form>
</fieldset>
<table width="100%" class="need-border">
	<thead>
		<tr>
			<th>ProcessDefinitionId</th>
			<th>DeploymentId</th>
			<th>名称</th>
			<th>KEY</th>
			<th>版本号</th>
			<th>XML</th>
			<th>图片</th>
			<th>部署时间</th>
			<th>是否挂起</th>
			<th>操作</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${objects }" var="object">
			<c:set var="process" value="${object[0] }" />
			<c:set var="deployment" value="${object[1] }" />

			<%
				pageContext
							.setAttribute(
									"isSuspended",
									((org.activiti.engine.repository.ProcessDefinition) pageContext
											.getAttribute("process")).isSuspended());
			%>
			<tr>
				<td class="process-id">${process.id }</td>
				<td>${process.deploymentId }</td>
				<td class="process_name">${process.name }</td>
				<td>${process.key }</td>
				<td>${process.version }</td>
				<td><a target="_blank" href='${ctx }/workflow/resource/deployment?deploymentId=${process.deploymentId}&resourceName=${process.resourceName }'>${process.resourceName }</a>
				</td>
				<td><a target="_blank" href='${ctx }/workflow/resource/deployment?deploymentId=${process.deploymentId}&resourceName=${process.diagramResourceName }'>${process.diagramResourceName
						}</a></td>
				<td><fmt:formatDate value="${deployment.deploymentTime }" type="both" /></td>
				<td>${isSuspended} | <c:if test="${isSuspended }">
						<a href="processdefinition/update/active/${process.id}">激活</a>
					</c:if> <c:if test="${!isSuspended }">
						<a href="processdefinition/update/suspend/${process.id}">挂起</a>
					</c:if>
				</td>
				<td><a class="startup-process" href='#'>启动</a>| <a href='${ctx }/workflow/process/delete?deploymentId=${process.deploymentId}'>删除</a></td>
			</tr>
		</c:forEach>
	</tbody>
</table>
</body>
</html>