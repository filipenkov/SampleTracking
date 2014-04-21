package com.sysbliss.jira.workflow.controller {
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;

import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;

import mx.resources.IResourceManager;

import mx.resources.ResourceManager;

import org.swizframework.controller.AbstractController;

public class WorkflowAbstractController extends AbstractController {

    protected const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

    protected function get resourceManager():IResourceManager {
        return ResourceManager.getInstance();
    }

    public function WorkflowAbstractController() {
        super();
    }
}
}
