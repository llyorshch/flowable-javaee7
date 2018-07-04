package com.flowable.support.poc.javaee7.rest;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("processEngine")
@Singleton
@Produces("application/json")
public class ProcessEngineRestService {

    @Inject
    private Logger logger;

    @Inject
    private ProcessEngine processEngine;

    private ProcessDefinition processDefinition;

    private ProcessDefinition getProcessDefinition() {
        if (processDefinition == null) {
            logger.info("Initializing process definition.");
            this.processDefinition = MoreObjects.firstNonNull(queryProcessDefinition(), deployProcessDefinition());
        }
        return this.processDefinition;
    }

    private ProcessDefinition queryProcessDefinition() {
        return processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("a-process")
                .latestVersion().singleResult();
    }

    private ProcessDefinition deployProcessDefinition() {
        processEngine.getRepositoryService().createDeployment().addClasspathResource("diagrams/a-process.bpmn20.xml")
                .deploy();
        ProcessDefinition processDefinition = queryProcessDefinition();
        if (processDefinition == null) {
            throw new RuntimeException("Unable to deploy the process.");
        } else {
            return processDefinition;
        }
    }

    @GET
    @Path("name")
    public Map<String,String> processEngine() {
        return ImmutableMap.of("processEngineName",processEngine.getProcessEngineConfiguration().getEngineName());
    }

    @GET
    @Path("processDefinitions")
    public List<String> getDeployedProcesses() {
        return processEngine.getRepositoryService().createProcessDefinitionQuery()
                .active().list().stream().map(ProcessDefinition::getId).collect(Collectors.toList());
    }

    @POST
    @Path("processes")
    public Map<String, String> startProcess() {
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(this.getProcessDefinition().getKey());
        return getMapFromProcessInstance(processInstance);
    }

    private Map<String, String> getMapFromProcessInstance(ProcessInstance processInstance) {
        return ImmutableMap.of("id", processInstance.getId());
    }

    @GET
    @Path("processes")
    public List<Map<String,String>> getProcesses() {
        return processEngine.getRuntimeService().createProcessInstanceQuery().active().list()
                .stream().map(this::getMapFromProcessInstance).collect(Collectors.toList());
    }

    @GET
    @Path("tasks")
    public List<Map<String,String>> getTasks() {
        return processEngine.getTaskService().createTaskQuery().active().list()
                .stream().map(this::getMapFromTask).collect(Collectors.toList());

    }

    private ImmutableMap<String, String> getMapFromTask(Task task) {
        return ImmutableMap.of("id", task.getId(),
                "name", MoreObjects.firstNonNull(task.getName(),""));
    }

    @POST
    @Path("tasks/{taskId}")
    public Response completeTask(@PathParam("taskId") String taskId, @QueryParam("action") String action) {
        if (!"complete".equals(action)) {
            throw new RuntimeException("Not implemented");
        }

        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            logger.info("Task id '{}' found. Proceeding to complete the task.", taskId);
            taskService.complete(task.getId());
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
