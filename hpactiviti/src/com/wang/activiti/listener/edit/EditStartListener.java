/*****************************
 * Copyright (c) 2012 by Artron Co. Ltd.  All rights reserved.
 ****************************/
package com.wang.activiti.listener.edit;

import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EditStartListener implements ExecutionListener {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public void notify(DelegateExecution execution) throws Exception {
		String businessKey = execution.getBusinessKey();
		Set<String> variableNames = execution.getVariableNames();
		for (String name : variableNames) {
			Object variable = execution.getVariable(name);
			System.out.println(variable);
		}
		
		logger.debug("新建编辑任务:{}", variableNames+"businessKey:"+businessKey);
	}

}
