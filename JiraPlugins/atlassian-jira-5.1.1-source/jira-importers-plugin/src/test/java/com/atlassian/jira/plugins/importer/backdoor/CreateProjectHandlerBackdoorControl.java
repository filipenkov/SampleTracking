package com.atlassian.jira.plugins.importer.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

public class CreateProjectHandlerBackdoorControl extends JimBackdoorControl<CreateProjectHandlerBackdoorControl> {

    public CreateProjectHandlerBackdoorControl(JIRAEnvironmentData environmentData) {
        super(environmentData);
    }

    public boolean canCreateProjects() {
        return post(createResource().path("createProjectHandler"), true, Boolean.class);
    }

    public boolean cantCreateProjects() {
        return post(createResource().path("createProjectHandler"), false, Boolean.class);
    }
}
