/*****************************
 * Copyright (c) 2012 by Artron Co. Ltd.  All rights reserved.
 ****************************/
package com.wang.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ProcessStart implements ExecutionListener {

	public void notify(DelegateExecution execution) throws Exception {
		System.out.println("=============start process");
	}
}
