/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugins.mail.HandlerDetailsValidator;
import com.atlassian.jira.plugins.mail.handlers.AbstractMessageHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateOrCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.RegexCommentHandler;
import com.atlassian.jira.plugins.mail.model.HandlerDetailsModel;
import com.atlassian.jira.plugins.mail.model.IssueTypeModel;
import com.atlassian.jira.plugins.mail.model.OptionModel;
import com.atlassian.jira.plugins.mail.model.ProjectModel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class EditHandlerDetailsWebAction extends AbstractEditHandlerDetailsWebAction
{
    // this mapping is used both in VM to render the form labels and in handlers table - order matters.
    private static final Map<String, String> fieldLabels = ImmutableMap.<String, String>builder()
            .put("project", "common.concepts.project")
            .put("issuetype", "common.concepts.issuetype")
            .put("stripquotes", "jmp.editHandlerDetails.stripquotes")
            .put("reporterusername", "jmp.editHandlerDetails.reporterusername")
            .put("splitregex", "jmp.editHandlerDetails.splitregex")
            .put("catchemail", "jmp.editHandlerDetails.catchemail")
            .put("bulk", "jmp.editHandlerDetails.bulk")
            .put("forwardEmail", "admin.service.common.handler.forward.email")
            .put("createusers", "jmp.editHandlerDetails.createusers")
            .put("notifyusers", "jmp.editHandlerDetails.notifyusers")
            .put("ccassignee", "jmp.editHandlerDetails.ccassignee")
            .put("ccwatcher", "jmp.editHandlerDetails.ccwatcher")
            .put("port", "jmp.editHandlerDetails.port")
            .put("usessl", "jmp.editHandlerDetails.usessl")
            .build();

    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final HandlerDetailsValidator detailsValidator;

    private HandlerDetailsModel details;

    private final List<Project> projects;

    private String detailsJson;

    public EditHandlerDetailsWebAction(IssueTypeSchemeManager issueTypeSchemeManager,
            ProjectManager projectManager, HandlerDetailsValidator detailsValidator,
            PluginAccessor pluginAccessor)
    {
        super(pluginAccessor);
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.detailsValidator = detailsValidator;
        this.details = new HandlerDetailsModel();
        this.details.setNotifyusers(true);

        this.projects = projectManager.getProjectObjects();
    }

    @Override
    protected void copyServiceSettings(JiraServiceContainer serviceContainer) throws ObjectConfigurationException
    {
        details.setForwardEmail(serviceContainer.getProperty("forwardEmail"));
        details.fromServiceParams(serviceContainer.getProperty("handler.params"));
    }


    @Override
    protected Map<String, String> getHandlerParams()
    {
        return details.toServiceParams();
    }

    @Override
    protected Map<String, String[]> getAdditionalServiceParams() throws Exception
    {
        return MapBuilder.<String, String[]>newBuilder(MailFetcherService.FORWARD_EMAIL, new String[] { details.getForwardEmail() })
                .toMutableMap();
    }

    @Override
    protected void doValidation()
    {
        if (configuration == null) {
            return; // short-circuit in case we lost session, goes directly to doExecute which redirects user
        }

        super.doValidation();
        if (detailsJson == null) {
            addErrorMessage("No configuration data sent via detailsJson field.");
            return;
        }

        try {
			details = new ObjectMapper().readValue(detailsJson, HandlerDetailsModel.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

        addErrorCollection(detailsValidator.validateDetails(details));
    }

    public static Map<String, String> getFieldLabels() {
        return fieldLabels;
    }

    @SuppressWarnings("unused")
    public void setDetailsJson(String json) {
        this.detailsJson = json;
    }

    @Nonnull
    public String getDetailsJson() {
        try {
			return new ObjectMapper().writeValueAsString(details);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    @Nonnull
    public String getBulkOptionsJson() {
        final List<OptionModel> options = Lists.newArrayList(
                new OptionModel(getText("jmp.editHandlerDetails.bulk.ignore"), AbstractMessageHandler.VALUE_BULK_IGNORE),
                new OptionModel(getText("jmp.editHandlerDetails.bulk.forward"), AbstractMessageHandler.VALUE_BULK_FORWARD),
                new OptionModel(getText("jmp.editHandlerDetails.bulk.delete"), AbstractMessageHandler.VALUE_BULK_DELETE),
                new OptionModel(getText("jmp.editHandlerDetails.bulk.accept"), AbstractMessageHandler.VALUE_BULK_ACCEPT));
        try {
			return new ObjectMapper().writeValueAsString(options);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    @Nonnull
    public String getProjectsJson() {
        final List<ProjectModel> suggestions = Lists.newArrayList(
                Iterables.transform(projects, new Function<Project, ProjectModel>()
        {
            @Override
            public ProjectModel apply(final Project project)
            {
                return new ProjectModel(project.getName(), project.getKey(), Lists.<IssueTypeModel>newArrayList(
                    Iterables.transform(issueTypeSchemeManager.getIssueTypesForProject(project), new Function<IssueType, IssueTypeModel>()
                    {
                        @Override
                        public IssueTypeModel apply(IssueType from)
                        {
                            return new IssueTypeModel(from.getName(), from.getId());
                        }
                    })));
            }
        }));

        try {
			return new ObjectMapper().writeValueAsString(suggestions);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    public boolean isCreateOrCommentHandlerSelected() {
        return CreateOrCommentHandler.class.isAssignableFrom(descriptor.getMessageHandler());
    }

    public boolean isCreateIssueHandlerSelected() {
        return CreateIssueHandler.class.isAssignableFrom(descriptor.getMessageHandler());
    }

    public boolean isRegexCommentHandlerSelected() {
        return RegexCommentHandler.class.isAssignableFrom(descriptor.getMessageHandler());
    }

}
