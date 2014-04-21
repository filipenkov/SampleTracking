package com.atlassian.jira.rest.v2.issue.customfield;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.AbstractCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.impl.DateCFType;
import com.atlassian.jira.issue.customfields.impl.DateTimeCFType;
import com.atlassian.jira.issue.customfields.impl.LabelsCFType;
import com.atlassian.jira.issue.customfields.impl.MultiGroupCFType;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.customfields.impl.MultiUserCFType;
import com.atlassian.jira.issue.customfields.impl.ProjectCFType;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.customfields.impl.UserCFType;
import com.atlassian.jira.issue.customfields.impl.VersionCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.api.Dates;
import com.atlassian.jira.rest.v2.issue.ProjectResource;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.rest.v2.issue.project.ProjectBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.atlassian.jira.bc.ServiceOutcomeImpl.ok;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.apache.commons.lang.ObjectUtils.identityToString;

/**
 * Visitor class used to marshall built-in custom fields and their subclasses.
 */
class BuiltinCustomFieldMarshaller implements
        AbstractCustomFieldType.Visitor<ServiceOutcome>,
        CascadingSelectCFType.Visitor<ServiceOutcome>,
        DateCFType.Visitor<ServiceOutcome>,
        DateTimeCFType.Visitor<ServiceOutcome>,
        LabelsCFType.Visitor<ServiceOutcome>,
        MultiGroupCFType.Visitor<ServiceOutcome>,
        MultiUserCFType.Visitor<ServiceOutcome>,
        MultiSelectCFType.Visitor<ServiceOutcome>,
        SelectCFType.Visitor<ServiceOutcome>,
        ProjectCFType.Visitor<ServiceOutcome>,
        UserCFType.Visitor<ServiceOutcome>,
        VersionCFType.Visitor<ServiceOutcome>
{
    /**
     * Null service outcome.
     */
    private static final ServiceOutcomeImpl<Object> NULL = ok(null);

    /**
     * Logger for this BuiltinCustomFieldMarshaller instance.
     */
    private final Logger log = LoggerFactory.getLogger(BuiltinCustomFieldMarshaller.class);

    /**
     * The issue that contains the custom field that this class will marshall.
     */
    private final Issue issue;

    /**
     * The custom field instance that this class will marshall.
     */
    private final CustomField customField;

    /**
     * The JIRA base URI.
     */
    private final URI baseURI;

    /**
     * The request URI context.
     */
    private final UriInfo context;

    /**
     * A ResourceUriBuilder.
     */
    private final ResourceUriBuilder resourceUriBuilder;

    /**
     * Factory helper for {@link ProjectBean}s
     */
    private final ProjectBeanFactory projectBeanFactory;

    /**
     * Factory helper for {@link VersionBean}s
     */
    private final VersionBeanFactory versionBeanFactory;

    BuiltinCustomFieldMarshaller(Issue issue, CustomField customField, URI baseURI, UriInfo context,
            ResourceUriBuilder resourceUriBuilder, ProjectBeanFactory projectBeanFactory,
            VersionBeanFactory versionBeanFactory)
    {
        this.projectBeanFactory = projectBeanFactory;
        this.versionBeanFactory = versionBeanFactory;
        this.issue = notNull("issue", issue);
        this.customField = notNull("customField", customField);
        this.resourceUriBuilder = notNull("resourceUriBuilder", resourceUriBuilder);
        this.baseURI = notNull("baseURI", baseURI);
        this.context = notNull("context", context);
    }

    public ServiceOutcome visit(AbstractCustomFieldType cfType)
    {
        Object transportValue = transportValueOf(cfType);
        if (transportValue == null)
        {
            return NULL;
        }

        String msg = format("Don't know how to marshal transport object '%s' for: %s", identityToString(transportValue), cfType);
        log.trace(msg);
        return ServiceOutcomeImpl.error(msg);
    }

    public ServiceOutcome visitDate(DateCFType cfType)
    {
        Date date = (Date) transportValueOf(cfType);
        if (date == null) { return NULL; }

        return ok(Dates.asDateString(date));
    }

    public ServiceOutcome visitDateTime(DateTimeCFType cfType)
    {
        Date date = (Date) transportValueOf(cfType);
        if (date == null) { return NULL; }

        return ok(Dates.asTimeString(date));
    }

    public ServiceOutcome visitUser(UserCFType cfType)
    {
        User user = (User) transportValueOf(cfType);
        if (user == null) { return NULL; }

        return ok(new UserBeanBuilder().baseURL(baseURI).context(context).user(user).buildShort());
    }

    public ServiceOutcome visitMultiUser(MultiUserCFType cfType)
    {
        @SuppressWarnings ("unchecked")
        Collection<User> users = transportValueOf(cfType);
        if (users == null) { return NULL; }

        List<UserBean> userBeans = Lists.newArrayListWithCapacity(users.size());
        for (User user : users)
        {
            userBeans.add(new UserBeanBuilder().baseURL(baseURI).context(context).user(user).buildShort());
        }

        return ok(userBeans);
    }

    public ServiceOutcome visitMultiSelect(MultiSelectCFType cfType)
    {
        @SuppressWarnings ("unchecked")
        Collection<Option> options = transportValueOf(cfType);
        if (options == null) { return NULL; }

        List<CustomFieldOptionBean> optionBeans = Lists.newArrayListWithCapacity(options.size());
        for (Option option : options)
        {
            optionBeans.add(new CustomFieldOptionBeanBuilder().baseURI(baseURI).context(context).customFieldOption(option).buildShort());
        }

        return ok(optionBeans);
    }

    public ServiceOutcome visitSelect(SelectCFType cfType)
    {
        Option option = (Option) transportValueOf(cfType);
        if (option == null) { return NULL; }

        return ok(new CustomFieldOptionBeanBuilder().baseURI(baseURI).context(context).customFieldOption(option).buildShort());
    }

    public ServiceOutcome visitProject(ProjectCFType cfType)
    {
        GenericValue projectFields = transportValueOf(cfType);
        if (projectFields == null) { return NULL; }

        final String projectKey = projectFields.getString("key");
        final String projectName = projectFields.getString("name");
        URI selfUri = resourceUriBuilder.build(context, ProjectResource.class, projectKey);
        return ok(projectBeanFactory.shortProject(projectKey, projectName, selfUri));
    }

    public ServiceOutcome visitMultiGroup(MultiGroupCFType cfType)
    {
        Function<Group, String> groupNameExtractor = new Function<Group, String>()
        {
            public String apply(@Nullable Group from)
            {
                return from.getName();
            }
        };

        List<Group> groups = transportValueOf(cfType);
        if (groups == null) { return NULL; }

        if (cfType.isMultiple())
        {
            return ok(Lists.transform(groups, groupNameExtractor));
        }
        else
        {
            return ok(groupNameExtractor.apply(groups.get(0)));
        }
    }

    public ServiceOutcome visitLabels(LabelsCFType cfType)
    {
        Collection<Label> labels = transportValueOf(cfType);
        if (labels == null) { return NULL; }

        return ok(Collections2.transform(labels, new Function<Label, String>()
        {
            public String apply(@Nullable Label from)
            {
                return from.getLabel();
            }
        }));
    }

    public ServiceOutcome visitVersion(VersionCFType cfType)
    {
        List<Version> versions = transportValueOf(cfType);
        if (versions == null) { return NULL; }

        if (cfType.isMultiple())
        {
            return ok(versionBeanFactory.createVersionBeans(versions));
        }
        else
        {
            return ok(versionBeanFactory.createVersionBean(versions.get(0)));
        }
    }

    @SuppressWarnings ("unchecked")
    public ServiceOutcome visitCascadingSelect(CascadingSelectCFType cfType)
    {
        CustomFieldParams cascadingSelectParams = transportValueOf(cfType);
        if (cascadingSelectParams == null) { return NULL; }

        List<String> optionPath = new ArrayList<String>();

        String depth = null;
        while (cascadingSelectParams.containsKey(depth))
        {
            Collection<Option> collection = cascadingSelectParams.getValuesForKey(depth);
            for (Option opt : collection)
            {
                optionPath.add(opt.getValue());
            }

            // increment the depth
            depth = valueOf(depth == null ? "1" : Integer.valueOf(depth) + 1);
        }

        return ok(optionPath);
    }

    /**
     * Returns the transport value of the given CustomFieldType.
     *
     * @param cfType a CustomFieldType
     * @return the transport object
     */
    @SuppressWarnings ("unchecked")
    protected <T> T transportValueOf(CustomFieldType cfType)
    {
        return (T) cfType.getValueFromIssue(customField, issue);
    }
}
