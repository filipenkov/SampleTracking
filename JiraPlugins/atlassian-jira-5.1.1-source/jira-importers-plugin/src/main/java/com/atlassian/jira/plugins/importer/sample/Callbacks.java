package com.atlassian.jira.plugins.importer.sample;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;

@ExperimentalApi
public class Callbacks {

    public void afterProjectCreated(ExternalProject project, Project jiraProject) {
    }

    public void afterComponentCreated(ExternalComponent component, ProjectComponent jiraComponent) {
    }

    public void afterVersionCreated(ExternalVersion version, Version jiraVersion) {
    }

    public void afterUserCreated(ExternalUser user, User jiraUser) {
    }

    public void afterCustomFieldCreated(ExternalCustomField customField, CustomField jiraCf) {
    }

}
