/*****************************
 * Copyright (c) 2012 by Artron Co. Ltd.  All rights reserved.
 ****************************/
package com.wang.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class TaskAListener implements TaskListener {

	public TaskAListener() {
		System.out.println("初始化监听器");
	}

	public void notify(DelegateTask delegateTask) {
		
		// TODO Auto-generated method stub
		System.out.println("id:" + delegateTask.getId() + "\n"
				+ "vaiableNames:" + delegateTask.getVariableNames() + "\n"
				+ "Assignee:" + delegateTask.getAssignee() + "\n"
				+ "Description:" + delegateTask.getDescription() + "\n"
				+ "EventName:" + delegateTask.getEventName() + "\n"
				+ "ExecutionId:" + delegateTask.getExecutionId() + "\n"
				+ "Name:" + delegateTask.getName() + "\n" + "Owner:"
				+ delegateTask.getOwner() + "\n" + "Priority:"
				+ delegateTask.getPriority() + "\n" + "ProcessDefinitionId:"
				+ delegateTask.getProcessDefinitionId() + "\n"
				+ "TaskDefinitionKey:" + delegateTask.getTaskDefinitionKey()
				+ "\n" + "Candidates:" + delegateTask.getCandidates() + "\n"
				+ "Variables:" + delegateTask.getVariables()

		);
		System.out.println("================================");
	}
}
