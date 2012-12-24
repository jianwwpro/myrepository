package com.wang.activiti.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wang.activiti.service.WorkFlowService;
import com.wang.activiti.service.WorkflowProcessDefinitionService;
import com.wang.activiti.service.WorkflowTraceService;
import com.wang.activiti.util.UserUtil;

@Controller
@RequestMapping(value = "/workflow/processinstance")
public class ProcessInstanceController {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	protected WorkflowProcessDefinitionService workflowProcessDefinitionService;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected TaskService taskService;
	@Autowired
	protected WorkflowTraceService traceService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private WorkFlowService workFlowService;
	protected static Map<String, ProcessDefinition> PROCESS_DEFINITION_CACHE = new HashMap<String, ProcessDefinition>();

	/**
	 * 挂起、激活流程实例
	 */
	@RequestMapping(value = "update/{state}/{processInstanceId}")
	public String updateState(@PathVariable("state") String state,
			@PathVariable("processInstanceId") String processInstanceId,
			RedirectAttributes redirectAttributes) {
		if (state.equals("active")) {
			redirectAttributes.addFlashAttribute("message", "已激活ID为["
					+ processInstanceId + "]的流程实例。");
			workFlowService.activateProcessInstanceById(processInstanceId);
		} else if (state.equals("suspend")) {
			workFlowService.suspendProcessInstanceById(processInstanceId);
			redirectAttributes.addFlashAttribute("message", "已挂起ID为["
					+ processInstanceId + "]的流程实例。");
		}
		return "redirect:/workflow/processinstance/running";
	}

	/**
	 * 读取启动流程的表单内容
	 */
	@RequestMapping(value = "get-form/start/{processDefinitionId}")
	@ResponseBody
	public Object findStartForm(
			@PathVariable("processDefinitionId") String processDefinitionId)
			throws Exception {

		// 根据流程定义ID读取外置表单
		Object startForm = workFlowService
				.getRenderedStartForm(processDefinitionId);
		return startForm;
	}

	/**
	 * 读取Task的表单
	 */
	@RequestMapping(value = "get-form/task/{taskId}")
	@ResponseBody
	public Object findTaskForm(@PathVariable("taskId") String taskId)
			throws Exception {
		Object renderedTaskForm = workFlowService.getRenderedTaskForm(taskId);
		return renderedTaskForm;
	}

	/**
	 * 提交task的并保存form
	 */
	@RequestMapping(value = "task/complete/{taskId}")
	@SuppressWarnings("unchecked")
	public String completeTask(@PathVariable("taskId") String taskId,
			RedirectAttributes redirectAttributes, HttpServletRequest request) {
		Map<String, String> formProperties = new HashMap<String, String>();

		// 从request中读取参数然后转换
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
		for (Entry<String, String[]> entry : entrySet) {
			String key = entry.getKey();

			/*
			 * 参数结构：fq_reason，用_分割 fp的意思是form paremeter 最后一个是属性名称
			 */
			if (StringUtils.defaultString(key).startsWith("pf_")) {
				String[] paramSplit = key.split("_");
				formProperties.put(paramSplit[1], entry.getValue()[0]);
			}
		}

		logger.debug("start form parameters: {}", formProperties);

		String user = UserUtil.getUserFromSession();

		// 用户未登陆不能操作，实际应用使用权限框架实现，例如Spring Security、Shiro等
		if (user == null || StringUtils.isBlank(user)) {
			return "redirect:/login?timeout=true";
		}
		// identityService.setAuthenticatedUserId(user.getId());
		formProperties.put("user", user);
		workFlowService.submitTaskFormData(taskId, formProperties);

		redirectAttributes
				.addFlashAttribute("message", "任务完成：taskId=" + taskId);
		return "redirect:/workflow/processinstance/task/todo/list";
	}

	/**
	 * 读取启动流程的表单字段
	 */
	@RequestMapping(value = "start-process/{processDefinitionId}")
	@SuppressWarnings("unchecked")
	public String submitStartFormAndStartProcessInstance(
			@PathVariable("processDefinitionId") String processDefinitionId,
			RedirectAttributes redirectAttributes, HttpServletRequest request) {
		Map<String, String> formProperties = new HashMap<String, String>();

		// 从request中读取参数然后转换
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
		for (Entry<String, String[]> entry : entrySet) {
			String key = entry.getKey();

			// fp_的意思是form paremeter
			if (StringUtils.defaultString(key).startsWith("pf_")) {
				formProperties.put(key.split("_")[1], entry.getValue()[0]);
			}
		}

		logger.debug("start form parameters: {}", formProperties);

		// User user = UserUtil.getUserFromSession(request.getSession());
		String user = UserUtil.getUserFromSession();
		// 用户未登陆不能操作，实际应用使用权限框架实现，例如Spring Security、Shiro等
		if (user == null || StringUtils.isBlank(user)) {
			return "redirect:/login?timeout=true";
		}
		String businessKey = UUID.randomUUID().toString();
		ProcessInstance processInstance = workFlowService.submitStartFormData(
				processDefinitionId, formProperties, businessKey, user);
		logger.debug("start a processinstance: {}", processInstance);

		redirectAttributes.addFlashAttribute("message", "启动成功，流程ID："
				+ processInstance.getId());
		return "redirect:/workflow/process-list";
	}

	/**
	 * 签收任务
	 */
	@RequestMapping(value = "task/claim/{id}")
	public String claim(@PathVariable("id") String taskId, HttpSession session,
			RedirectAttributes redirectAttributes) {
		// String userId = UserUtil.getUserFromSession(session).getId();
		String user = UserUtil.getUserFromSession();
		workFlowService.claim(taskId, user);
		redirectAttributes.addFlashAttribute("message", "任务已签收");
		return "redirect:/workflow/processinstance/task/todo/list";
	}

	/**
	 * 运行中的流程实例
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process-instance/running/list")
	public ModelAndView running(Model model, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("/workflow/running-list");
		List<ProcessInstance> list = workFlowService
				.queryRunningProcessInstance();

		mav.addObject("list", list);
		return mav;
	}

	/**
	 * 删除运行中的实例
	 */
	@RequestMapping(value = "delete/{processInstanceId}")
	public String deleteProcessInstance(
			@PathVariable("processInstanceId") String processInstanceId,
			String reson, RedirectAttributes redirectAttributes)
			throws Exception {
		workFlowService.deleteProcessInstance(processInstanceId, reson);
		redirectAttributes.addFlashAttribute("message", "流程实例："
				+ processInstanceId + "已经删除");
		return "redirect:/workflow/processinstance/process-instance/running/list";
	}

	/**
	 * 已结束的流程实例
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process-instance/finished/list")
	public ModelAndView finished(Model model, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("/workflow/finished-list");

		List<HistoricProcessInstance> list = workFlowService
				.queryFinishedProcessInstance();
		mav.addObject("list", list);
		return mav;
	}

	/**
	 * task列表
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "task/list")
	public ModelAndView taskList(Model model, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("workflow/undotask");
		// User user = UserUtil.getUserFromSession(request.getSession());
		String user = UserUtil.getUserFromSession();

		/*
		 * select a.* from ( select distinct RES.* from ACT_RU_TASK RES inner
		 * join ACT_RE_PROCDEF D on RES.PROC_DEF_ID_ = D.ID_ WHERE RES.ASSIGNEE_
		 * = 'user' and RES.SUSPENSION_STATE_ =1
		 * 
		 * 
		 * union all
		 * 
		 * 
		 * select distinct RES.* from ACT_RU_TASK RES inner join
		 * ACT_RU_IDENTITYLINK I on I.TASK_ID_ = RES.ID_ inner join
		 * ACT_RE_PROCDEF D on RES.PROC_DEF_ID_ = D.ID_ WHERE RES.ASSIGNEE_ is
		 * null and I.TYPE_ = 'candidate' and ( I.USER_ID_ = 'user' or
		 * I.GROUP_ID_ IN (select g.GROUP_ID_ from ACT_ID_MEMBERSHIP g where
		 * g.USER_ID_ = 'user' ) ) and RES.SUSPENSION_STATE_ = 1 ) a limit 0,3
		 */
		// 已经签收的或者直接分配到当前人的任务
		String asigneeSql = "select distinct RES.* from ACT_RU_TASK RES inner join ACT_RE_PROCDEF D on RES.PROC_DEF_ID_ = D.ID_ WHERE RES.ASSIGNEE_ = #{userId}"
				+ " and D.KEY_ = #{processDefinitionKey} and RES.SUSPENSION_STATE_ = #{suspensionState}";

		// 当前人在候选人或者候选组范围之内
		String needClaimSql = "select distinct RES.* from ACT_RU_TASK RES inner join ACT_RU_IDENTITYLINK I on I.TASK_ID_ = RES.ID_ inner join ACT_RE_PROCDEF D on RES.PROC_DEF_ID_ = D.ID_ WHERE"
				+ " D.KEY_ = #{processDefinitionKey} and RES.ASSIGNEE_ is null and I.TYPE_ = 'candidate'"
				+ " and ( I.USER_ID_ = #{userId} or I.GROUP_ID_ IN (select g.GROUP_ID_ from ACT_ID_MEMBERSHIP g where g.USER_ID_ = #{userId} ) )"
				+ " and RES.SUSPENSION_STATE_ = #{suspensionState}";
		List<Task> tasks = taskService
				.createNativeTaskQuery()
				.sql(asigneeSql + "union all " + needClaimSql)
				.parameter("processDefinitionKey", "leave-formkey")
				.parameter("suspensionState",
						SuspensionState.ACTIVE.getStateCode())
				.parameter("userId", user).list();

		mav.addObject("result", tasks);
		return mav;
	}

	/**
	 * 待办任务--Portlet
	 */
	@RequestMapping(value = "/task/todo/list")
	public String todoList(HttpSession session, Model model) throws Exception {
		// User user = UserUtil.getUserFromSession(session);
		String username = UserUtil.getUserFromSession();

		// 已经签收的任务
		List<Task> todoList = workFlowService.queryClaimedTask(username);
		List<Task> toClaimList = workFlowService.queryUnClaimTask(username);

		// 等待签收的任务

		model.addAttribute("todo", todoList);
		model.addAttribute("claim", toClaimList);
		return "workflow/undotask";
	}

	/**
	 * 已办理过的任务
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "task/finished/list")
	public ModelAndView finishedTask(Model model, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("/workflow/task/finished-list");

		List<HistoricTaskInstance> list = historyService
				.createHistoricTaskInstanceQuery().taskAssignee("user")
				.finished().list();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (HistoricTaskInstance historicTaskInstance : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			ProcessDefinition processDefinition = repositoryService
					.createProcessDefinitionQuery()
					.processDefinitionId(
							historicTaskInstance.getProcessDefinitionId())
					.singleResult();
			map.put("task", historicTaskInstance);
			map.put("definition", processDefinition);
			result.add(map);
		}
		mav.addObject("list", result);
		return mav;
	}

	/**
	 * 流转记录
	 */
	@RequestMapping(value = "/task/task-history/{processInstanceId}")
	public String taskHistory(HttpSession session, Model model,
			@PathVariable("processInstanceId") String processInstanceId)
			throws Exception {

		List<HistoricActivityInstance> list = historyService
				.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId).finished().list();
		HistoricVariableInstance variable = historyService
				.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstanceId).variableName("startUser")
				.singleResult();
		String username = (String) variable.getValue();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		for (HistoricActivityInstance historicActivityInstance : list) {
			Map<String, Object> map = new HashMap<String, Object>();

			if (historicActivityInstance.getActivityType().equals("startEvent")) {

				map.put("tassignee", username);
				map.put("tcomment",
						"新建[" + historicActivityInstance.getActivityName()
								+ "]");

			} else {
				String taskId = historicActivityInstance.getTaskId();
				HistoricVariableInstance taskComment = historyService
						.createHistoricVariableInstanceQuery()
						.processInstanceId(processInstanceId)
						.variableName(taskId + "_comment").singleResult();
				String tcomment = "";
				if (taskComment != null) {
					tcomment = (String) taskComment.getValue();
				}
				map.put("tcomment", tcomment);
				map.put("tassignee", historicActivityInstance.getAssignee());

			}
			map.put("aid", historicActivityInstance.getId());
			map.put("tname", historicActivityInstance.getActivityName());
			map.put("tstarttime", historicActivityInstance.getStartTime());
			map.put("tendtime", historicActivityInstance.getEndTime());
			result.add(map);
		}
		model.addAttribute("list", result);
		return "workflow/task/task-history";
	}

}
