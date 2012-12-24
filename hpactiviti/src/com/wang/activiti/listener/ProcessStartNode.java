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
public class ProcessStartNode implements TaskListener {

	public void notify(DelegateTask delegateTask) {
		System.out.println("=============start task node");		
	}

}
