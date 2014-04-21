package com.atlassian.jira.collector.plugin.components;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectorServiceImpl implements CollectorService
{
    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final CollectorStore collectorStore;
    private final ProjectManager projectManager;
    private final UserUtil userUtil;
    private final TemplateStore templateStore;
    private final CollectorFieldValidator collectorFieldValidator;


    public CollectorServiceImpl(final PermissionManager permissionManager,
                                final I18nHelper.BeanFactory beanFactory, final CollectorStore collectorStore, final ProjectManager projectManager,
                                final UserUtil userUtil, final TemplateStore templateStore, final CollectorFieldValidator collectorFieldValidator)
    {
        this.permissionManager = permissionManager;
        this.beanFactory = beanFactory;
        this.collectorStore = collectorStore;
        this.projectManager = projectManager;
        this.userUtil = userUtil;
        this.templateStore = templateStore;
        this.collectorFieldValidator = collectorFieldValidator;
    }

    @Override
    public ServiceOutcome<Collector> getCollector(final String collectorId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        //no permission checks here since anonymous users may be requesting a collector!

        return new ServiceOutcomeImpl<Collector>(errors, collectorStore.getCollector(collectorId));
    }

    @Override
    public ServiceOutcome<Map<Long, List<Collector>>> getCollectorsPerProject(final User remoteUser)
    {
        final Map<Long, List<Collector>> ret = new LinkedHashMap<Long, List<Collector>>();
        final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.PROJECT_ADMIN, remoteUser);
        for (final Project project : projects)
        {
            final ServiceOutcome<List<Collector>> result = getCollectors(remoteUser, project);
            if(result.isValid() && !result.getReturnedValue().isEmpty())
            {
                ret.put(project.getId(), result.getReturnedValue());
            }
        }
        return new ServiceOutcomeImpl<Map<Long, List<Collector>>>(new SimpleErrorCollection(), ret);
    }

    @Override
    public ServiceOutcome<List<Collector>> getCollectors(final User remoteUser, final Project project)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(remoteUser);
        if (!hasProjectAdminPermission(project, remoteUser))
        {
            errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.view.no.permission"));
            return new ServiceOutcomeImpl<List<Collector>>(errors);
        }

        return new ServiceOutcomeImpl<List<Collector>>(errors, collectorStore.getCollectors(project.getId()));
    }

    @Override
    public ServiceOutcome<Collector> validateCreateCollector(final User remoteUser, final String name, final Long projectId,
            final Long issueTypeId, final String reporter, final String description, final String templateId,
            final boolean recordWebInfo, final boolean useCredentials, final Trigger trigger, final String customMessage,
            final List<String> customTemplateFields, final String customTemplateTitle,
            final String customTemplateLabels)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(remoteUser);
        final Project project = projectManager.getProjectObj(projectId);

        if (project == null)
        {
            errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.no.project"));
        }

        if (!hasProjectAdminPermission(project, remoteUser))
        {
            errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.view.no.permission"));
            return new ServiceOutcomeImpl<Collector>(errors);
        }

        final Set<String> notAllowedFields = collectorFieldValidator.getRequiredInvalidFieldsForIssueType(remoteUser, project, issueTypeId
				.toString());
        if (!notAllowedFields.isEmpty())
        {
            //error is reported, however not displayed - javascript handels error display
			errors.addError("requireInvalidFields", i18n.getText("collector.plugin.admin.error.not.allowed.fields",
					StringUtils.join(notAllowedFields, ", ")));
			return new ServiceOutcomeImpl<Collector>(errors);
        }

        if(StringUtils.isBlank(name))
        {
            errors.addError("collectorName",i18n.getText("collector.plugin.admin.error.no.name"));
        }

        if (reporter == null) {
            errors.addError("reporter",i18n.getText("collector.plugin.admin.error.empty.reporter"));
        } else {
            final User reporterUser = userUtil.getUserObject(reporter);
            if (reporterUser == null) {
                errors.addError("reporter",i18n.getText("collector.plugin.admin.error.no.reporter", reporter));
            } else {
                final boolean reporterHasPermissions = permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, remoteUser);
                if (!reporterHasPermissions) {
                    errors.addError("reporter",i18n.getText("collector.plugin.admin.error.invalid.reporter", reporter));
                }
            }
        }

        final Template template = templateStore.getTemplate(templateId);
        if (template == null)
        {
            errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.no.template", templateId));
        }
        else if ("custom".equals(template.getId()) && customTemplateFields.isEmpty())
        {
            errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.no.template.fields", templateId));
        }


        if (errors.hasAnyErrors())
        {
            return new ServiceOutcomeImpl<Collector>(errors);
        }

        final Collector collector = new Collector.Builder().
                name(name).
                projectId(projectId).
                issueTypeId(issueTypeId).
                creator(remoteUser == null ? null : remoteUser.getName()).
                reporter(reporter).
                description(description).
                template(template).
                enabled(true).
                recoredWebInfo(recordWebInfo).
                useCredentials(useCredentials).
                trigger(trigger).
                customMessage(customMessage).
                customTemplateFields(customTemplateFields).
                customTemplateTitle(customTemplateTitle).
                customTemplateLabels(customTemplateLabels).
                build();
        return new ServiceOutcomeImpl<Collector>(errors, collector);
    }

    @Override
    public ServiceOutcome<Collector> createCollector(final User remoteUser, final ServiceOutcome<Collector> outcome)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        if (outcome.getReturnedValue() == null)
        {
            throw new IllegalArgumentException("Validation result contained no value!");
        }

        return new ServiceOutcomeImpl<Collector>(errors, collectorStore.addCollector(outcome.getReturnedValue()));
    }

    @Override
    public ServiceOutcome<Boolean> enableCollector(final User remoteUser, final Project project, final String collectorId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(remoteUser);
        if (!hasProjectAdminPermission(project, remoteUser))
        {
            errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.view.no.permission"));
            return new ServiceOutcomeImpl<Boolean>(errors);
        }

        //TODO: Should check if collector exists!

        return new ServiceOutcomeImpl<Boolean>(errors, collectorStore.enableCollector(collectorId));
    }

    @Override
    public ServiceOutcome<Boolean> disableCollector(final User remoteUser, final Project project, final String collectorId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(remoteUser);
        if (!hasProjectAdminPermission(project, remoteUser))
        {
            errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.view.no.permission"));
            return new ServiceOutcomeImpl<Boolean>(errors);
        }

        //TODO: Should check if collector exists!

        return new ServiceOutcomeImpl<Boolean>(errors, collectorStore.disableCollector(collectorId));
    }

    @Override
    public ServiceOutcome<Collector> validateDeleteCollector(final User remoteUser, final Project project, final String collectorId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(remoteUser);
        if (!hasProjectAdminPermission(project, remoteUser))
        {
            errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.view.no.permission"));
            return new ServiceOutcomeImpl<Collector>(errors);
        }

        final Collector collector = collectorStore.getCollector(collectorId);

        return new ServiceOutcomeImpl<Collector>(errors, collector);
    }

    @Override
    public void deleteCollector(final User loggedInUser, final ServiceOutcome<Collector> validationResult)
    {
        final Collector collector = validationResult.getReturnedValue();
        if(collector == null)
        {
            throw new IllegalArgumentException("Got emtpy validation result!");
        }

        collectorStore.deleteCollector(collector.getProjectId(), collector.getId());
    }

    @Override
    public Long getArchivedProjectId(final String collectorId)
    {
        return collectorStore.getArchivedProjectId(collectorId);
    }

	@Override
	public ServiceOutcome<Boolean> validateAddCollectorPremission(final Project project, final User user) {
		final ErrorCollection errors = new SimpleErrorCollection();
		final I18nHelper i18n = beanFactory.getInstance(user);

		if (!hasCreateIssuePermission(project, user)) {
			errors.addErrorMessage(i18n.getText("collector.plugin.admin.error.add.no.permission"));
			return new ServiceOutcomeImpl<Boolean>(errors,false);
		}

		return new ServiceOutcomeImpl<Boolean>(errors,true);
	}

	private boolean hasProjectAdminPermission(final Project project, final User user)
    {
        return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user) ||
                permissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

	private boolean hasCreateIssuePermission(final Project project, final User user)
	{
		return permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
	}
}
