/*****************************
 * Copyright (c) 2012 by Artron Co. Ltd.  All rights reserved.
 ****************************/
package com.wang.activiti.service;

import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkFlowService {
	@Autowired
	private TaskService taskService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private ManagementService managementService;
	@Autowired
	private IdentityService identityService;
	@Autowired
	private FormService formService;

	/**
	 * 激活流程实例
	 * */
	public void activateProcessInstanceById(String processInstanceId) {
		runtimeService.activateProcessInstanceById(processInstanceId);

	}

	/**
	 * 挂起流程实例
	 * */
	public void suspendProcessInstanceById(String processInstanceId) {
		runtimeService.suspendProcessInstanceById(processInstanceId);

	}

	/**
	 * 获取启动表单
	 * */
	public Object getRenderedStartForm(String processDefinitionId) {
		Object startForm = formService
				.getRenderedStartForm(processDefinitionId);
		return startForm;
	}

	/**
	 * 获取任务表单内容
	 * */
	public Object getRenderedTaskForm(String taskId) {
		Object renderedTaskForm = formService.getRenderedTaskForm(taskId);
		return renderedTaskForm;
	}

	/**
	 * 办理任务，并提交表单
	 * */
	public void submitTaskFormData(String taskId,
			Map<String, String> formProperties) {
		formService.submitTaskFormData(taskId, formProperties);
	}

	/**
	 * 启动并提交流程
	 * */
	public ProcessInstance submitStartFormData(String processDefinitionId,
			Map<String, String> formProperties, String businessKey, String user) {
		identityService.setAuthenticatedUserId(user);
		
		ProcessInstance processInstance = formService.submitStartFormData(
				processDefinitionId,businessKey, formProperties);
		
		return processInstance;
	}

	/**
	 * 签收任务
	 * */
	public void claim(String taskId, String user) {
		taskService.claim(taskId, user);
	}

	/**
	 * 运行中的流程实例
	 * */
	public List<ProcessInstance> queryRunningProcessInstance() {
		List<ProcessInstance> list = runtimeService
				.createProcessInstanceQuery().active().list();
		return list;
	}

	/**
	 * 已结束的流程实例
	 * */
	public List<HistoricProcessInstance> queryFinishedProcessInstance() {
		List<HistoricProcessInstance> list = historyService
				.createHistoricProcessInstanceQuery().finished().list();
		return list;
	}

	/**
	 * 已经签收的任务
	 * */
	public List<Task> queryClaimedTask(String username) {

		List<Task> todoList = taskService.createTaskQuery()
				.taskAssignee(username).active().list();
		return todoList;
	}

	/**
	 * 未签收的任务
	 * */
	public List<Task> queryUnClaimTask(String group) {
		List<Task> toClaimList = taskService.createTaskQuery()
				.taskCandidateGroup(group).active().list();
		return toClaimList;
	}

	/**
	 * 删除实例
	 * */
	public void deleteProcessInstance(String processInstanceId, String reson) {
		runtimeService.deleteProcessInstance(processInstanceId, reson);
	}

}
