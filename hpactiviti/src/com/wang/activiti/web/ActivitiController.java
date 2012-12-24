package com.wang.activiti.web;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wang.activiti.service.WorkFlowService;
import com.wang.activiti.service.WorkflowProcessDefinitionService;
import com.wang.activiti.service.WorkflowTraceService;
import com.wang.activiti.util.UserUtil;

/**
 * 流程管理控制器
 * 
 * @author HenryYan
 */
@Controller
@RequestMapping(value = "/workflow")
public class ActivitiController {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected WorkflowProcessDefinitionService workflowProcessDefinitionService;

	protected RepositoryService repositoryService;

	protected RuntimeService runtimeService;

	protected TaskService taskService;

	protected WorkflowTraceService traceService;
	@Autowired
	private WorkFlowService workFlowService;

	protected static Map<String, ProcessDefinition> PROCESS_DEFINITION_CACHE = new HashMap<String, ProcessDefinition>();

	/**
	 * 流程定义列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/process-list")
	public ModelAndView processList() {
		ModelAndView mav = new ModelAndView("workflow/process-list1");

		/*
		 * 保存两个对象，一个是ProcessDefinition（流程定义），一个是Deployment（流程部署）
		 */
		List<Object[]> objects = new ArrayList<Object[]>();

		List<ProcessDefinition> processDefinitionList = repositoryService
				.createProcessDefinitionQuery().latestVersion().list();
		for (ProcessDefinition processDefinition : processDefinitionList) {
			String deploymentId = processDefinition.getDeploymentId();
			Deployment deployment = repositoryService.createDeploymentQuery()
					.deploymentId(deploymentId).singleResult();
			objects.add(new Object[] { processDefinition, deployment });
		}

		mav.addObject("objects", objects);

		return mav;
	}

	/**
	 * 部署全部流程
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/redeploy/all")
	public String redeployAll() throws Exception {
		workflowProcessDefinitionService.deployAllFromClasspath();
		return "redirect:/workflow/process-list";
	}

	/**
	 * 读取资源，通过部署ID
	 * 
	 * @param deploymentId
	 *            流程部署的ID
	 * @param resourceName
	 *            资源名称(foo.xml|foo.png)
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/resource/deployment")
	public void loadByDeployment(
			@RequestParam("deploymentId") String deploymentId,
			@RequestParam("resourceName") String resourceName,
			HttpServletResponse response) throws Exception {
		InputStream resourceAsStream = repositoryService.getResourceAsStream(
				deploymentId, resourceName);
		byte[] b = new byte[1024];
		int len = -1;
		while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
			response.getOutputStream().write(b, 0, len);
		}
	}

	/**
	 * 读取资源，通过流程ID
	 * 
	 * @param resourceType
	 *            资源类型(xml|image)
	 * @param processInstanceId
	 *            流程实例ID
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/resource/process-instance")
	public void loadByProcessInstance(
			@RequestParam("type") String resourceType,
			@RequestParam("pid") String processInstanceId,
			HttpServletResponse response) throws Exception {
		InputStream resourceAsStream = null;
		ProcessInstance processInstance = runtimeService
				.createProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		ProcessDefinition processDefinition = repositoryService
				.createProcessDefinitionQuery()
				.processDefinitionId(processInstance.getProcessDefinitionId())
				.singleResult();

		String resourceName = "";
		if (resourceType.equals("image")) {
			resourceName = processDefinition.getDiagramResourceName();
		} else if (resourceType.equals("xml")) {
			resourceName = processDefinition.getResourceName();
		}
		resourceAsStream = repositoryService.getResourceAsStream(
				processDefinition.getDeploymentId(), resourceName);
		byte[] b = new byte[1024];
		int len = -1;
		while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
			response.getOutputStream().write(b, 0, len);
		}
	}

	/**
	 * 删除部署的流程，级联删除流程实例
	 * 
	 * @param deploymentId
	 *            流程部署ID
	 */
	@RequestMapping(value = "/process/delete")
	public String delete(@RequestParam("deploymentId") String deploymentId) {
		repositoryService.deleteDeployment(deploymentId, true);
		return "redirect:/workflow/process-list";
	}

	/**
	 * 输出跟踪流程信息
	 * 
	 * @param processInstanceId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/process/trace")
	@ResponseBody
	public List<Map<String, Object>> traceProcess(
			@RequestParam("pid") String processInstanceId) throws Exception {
		List<Map<String, Object>> activityInfos = traceService
				.traceProcess(processInstanceId);
		return activityInfos;
	}

	/**
	 * 挂起、激活流程定义
	 */
	@RequestMapping(value = "processdefinition/update/{state}/{processDefinitionId}")
	public String updateState(@PathVariable("state") String state,
			@PathVariable("processDefinitionId") String processDefinitionId,
			RedirectAttributes redirectAttributes) {
		if (state.equals("active")) {
			redirectAttributes.addFlashAttribute("message", "已激活ID为["
					+ processDefinitionId + "]的流程定义。");
			repositoryService.activateProcessDefinitionById(
					processDefinitionId, true, null);
		} else if (state.equals("suspend")) {
			repositoryService.suspendProcessDefinitionById(processDefinitionId,
					true, null);
			redirectAttributes.addFlashAttribute("message", "已挂起ID为["
					+ processDefinitionId + "]的流程定义。");
		}
		return "redirect:/workflow/process-list";
	}

	@RequestMapping(value = "/deploy")
	public String deploy(
			@RequestParam(value = "file", required = false) MultipartFile file) {

		String fileName = file.getOriginalFilename();

		try {
			InputStream fileInputStream = file.getInputStream();

			String extension = FilenameUtils.getExtension(fileName);
			if (extension.equals("zip") || extension.equals("bar")) {
				ZipInputStream zip = new ZipInputStream(fileInputStream);
				repositoryService.createDeployment().addZipInputStream(zip)
						.deploy();
			} else if (extension.equals("png")) {
				repositoryService.createDeployment()
						.addInputStream(fileName, fileInputStream).deploy();
			} else if (fileName.indexOf("bpmn20.xml") != -1) {
				repositoryService.createDeployment()
						.addInputStream(fileName, fileInputStream).deploy();
			} else if (extension.equals("bpmn")) {
				/*
				 * bpmn扩展名特殊处理，转换为bpmn20.xml
				 */
				String baseName = FilenameUtils.getBaseName(fileName);
				repositoryService
						.createDeployment()
						.addInputStream(baseName + ".bpmn20.xml",
								fileInputStream).deploy();
			} else {
				throw new ActivitiException("no support file type of "
						+ extension);
			}
		} catch (Exception e) {
			logger.error(
					"error on deploy process, because of file input stream", e);
		}

		return "redirect:/workflow/process-list";
	}

	@Autowired
	public void setWorkflowProcessDefinitionService(
			WorkflowProcessDefinitionService workflowProcessDefinitionService) {
		this.workflowProcessDefinitionService = workflowProcessDefinitionService;
	}

	@Autowired
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	@Autowired
	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	@Autowired
	public void setTraceService(WorkflowTraceService traceService) {
		this.traceService = traceService;
	}

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

}
