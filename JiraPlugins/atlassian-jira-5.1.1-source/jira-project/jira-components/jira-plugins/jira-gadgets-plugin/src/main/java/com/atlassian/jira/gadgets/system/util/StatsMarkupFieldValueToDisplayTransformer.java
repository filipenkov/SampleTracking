package com.atlassian.jira.gadgets.system.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.gadgets.system.StatsMarkup;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Util object for transformaing a field/value into a displayable format for the StatsGadget. This implementation
 * of FieldValueToDisplayTransformer returns a StatsMarkup object containing html markup and css classes specifically
 * for use with the StatsGadget
 *
 * @since v4.1
 */
public class StatsMarkupFieldValueToDisplayTransformer implements FieldValueToDisplayTransformer<StatsMarkup>
{

    private final JiraAuthenticationContext authenticationContext;
    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final VelocityRequestContextFactory contextFactory;

    public StatsMarkupFieldValueToDisplayTransformer(final JiraAuthenticationContext authenticationContext, final ConstantsManager constantsManager, final CustomFieldManager customFieldManager,
                                          VelocityRequestContextFactory contextFactory)
    {
        this.authenticationContext = authenticationContext;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.contextFactory = contextFactory;
    }

    public StatsMarkup transformFromIrrelevant(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        final String html = makeMarkupForCell(i18n.getText("common.concepts.irrelevant"), url);
        return new StatsMarkup(html);
    }

    public StatsMarkup transformFromProject(final String fieldType, final Object input, final String url)
    {
        return generateProjectMarkup(input, url);
    }

    public StatsMarkup transformFromAssignee(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateAssigneesMarkup(input, url, i18n);
    }

    public StatsMarkup transformFromReporter(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateReporterMarkup(input, url, i18n);
    }

    public StatsMarkup transformFromResolution(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        if (input == null)
        {
            final String html = makeMarkupForCell(i18n.getText("common.resolution.unresolved"), url);
            return new StatsMarkup(html);
        }
        return generateConstantsMarkup(input, url);
    }

    public StatsMarkup transformFromPriority(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        if (input == null)
        {
            final String html = makeMarkupForCell(i18n.getText("gadget.filterstats.priority.nopriority"), url);
            return new StatsMarkup(html);
        }
        return generateConstantsMarkup(input, url);

    }

    public StatsMarkup transformFromIssueType(final String fieldType, final Object input, final String url)
    {
        return generateConstantsMarkup(input, url);
    }

    public StatsMarkup transformFromStatus(final String fieldType, final Object input, final String url)
    {
        return generateConstantsMarkup(input, url);
    }

    public StatsMarkup transformFromComponent(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateComponentsMarkup(input, url, i18n);
    }

    public StatsMarkup transformFromVersion(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateVersionMarkup(input, url, i18n, "gadget.filterstats.raisedin.unscheduled");
    }

    public StatsMarkup transformFromFixFor(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateVersionMarkup(input, url, i18n, "gadget.filterstats.fixfor.unscheduled");

    }

    public StatsMarkup transformFromLabels(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        if(input != null)
        {
            return new StatsMarkup(makeMarkupForCell(((Label) input).getLabel(), url));
        }
        return new StatsMarkup(makeMarkupForCell(i18n.getText("gadget.filterstats.labels.none"), url));
    }

    public StatsMarkup transformFromCustomField(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        if (input != null)
        {
            final CustomField customField = customFieldManager.getCustomFieldObject(fieldType);

            if (customField != null)
            {
                final CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
                final CustomFieldSearcherModuleDescriptor moduleDescriptor = searcher.getDescriptor();
                return new StatsMarkup(moduleDescriptor.getStatHtml(customField, input, url));
            }
        }
        final String html = makeMarkupForCell(i18n.getText("common.words.none"), url);
        return new StatsMarkup(html);

    }

    private StatsMarkup generateVersionMarkup(Object input, String url, I18nHelper i18n, String noneKey)
    {
        if (input != null)
        {
            final Version version = (Version) input;
            final String html = makeMarkupForCellWithDescription(TextUtils.htmlEncode(version.getName()), TextUtils.htmlEncode(version.getDescription()), url);
            return new StatsMarkup(html, getVersionClasses(version));
        }
        else
        {
            final String html = makeMarkupForCell(i18n.getText(noneKey), url);
            return new StatsMarkup(html);
        }
    }

    private StatsMarkup generateComponentsMarkup(Object input, String url, I18nHelper i18n)
    {
        if (input != null)
        {
            final String name;
            final String desc;
            if (input instanceof ProjectComponent)
            {
                final ProjectComponent component = (ProjectComponent) input;
                name = component.getName();
                desc = component.getDescription();
            }
            else
            {
                final GenericValue gv = (GenericValue) input;
                name = gv.getString("name");
                desc = gv.getString("description");
            }
            final String html = makeMarkupForCellWithDescription(TextUtils.htmlEncode(name), TextUtils.htmlEncode(desc), url);
            final List<String> classes = CollectionBuilder.list("default_image", "default_image_component");

            return new StatsMarkup(html, classes);
        }
        else
        {
            final String html = makeMarkupForCell(i18n.getText("gadget.filterstats.component.nocomponent"), url);
            return new StatsMarkup(html);
        }
    }

    private StatsMarkup generateConstantsMarkup(Object input, String url)
    {
        IssueConstant constant;
        if (input instanceof GenericValue)
        {
            constant = constantsManager.getIssueConstant((GenericValue) input);
        }
        else
        {
            constant = (IssueConstant) input;
        }
        final String html = makeConstantMarkup(constant, url);
        final List<String> classes = CollectionBuilder.list("gadget_image");
        return new StatsMarkup(html, classes);
    }

    private StatsMarkup generateReporterMarkup(Object input, String url, I18nHelper i18n)
    {
        String html;
        if (input != null)
        {
            final User assignee = (User) input;
            html = makeMarkupForCell(TextUtils.htmlEncode(assignee.getDisplayName()), url);
        }
        else
        {
            html = makeMarkupForCell(i18n.getText("common.concepts.no.reporter"), url);
        }
        return new StatsMarkup(html);
    }

    private StatsMarkup generateAssigneesMarkup(Object input, String url, I18nHelper i18n)
    {
        String html;
        if (input != null)
        {
            final User assignee = (User) input;
            html = makeMarkupForCell(TextUtils.htmlEncode(assignee.getDisplayName()), url);
        }
        else
        {
            html = makeMarkupForCell(i18n.getText("gadget.filterstats.assignee.unassigned"), url);
        }

        return new StatsMarkup(html);
    }

    private StatsMarkup generateProjectMarkup(Object input, String url)
    {
        String html;
        if (input instanceof GenericValue)
        {
            final GenericValue project = (GenericValue) input;
            html = makeMarkupForCell(TextUtils.htmlEncode(project.getString("name")), url);
        }
        else
        {
            final Project project = (Project) input;
            html = makeMarkupForCell(TextUtils.htmlEncode(project.getName()), url);
        }
        return new StatsMarkup(html);
    }

    private String makeConstantMarkup(IssueConstant constant, String url)
    {
        return makeConstantIconMarkup(constant) + makeMarkupForCellWithDescription(TextUtils.htmlEncode(constant.getNameTranslation()), TextUtils.htmlEncode(constant.getDescTranslation()), url);
    }

    private String makeConstantIconMarkup(IssueConstant constant)
    {
        String iconUrl = constant.getIconUrl();
        if (iconUrl != null && !"".equals(iconUrl))
        {
            if (iconUrl.startsWith("http://") || iconUrl.startsWith("https://"))
            {
                return makeImageMarkup(constant, iconUrl);
            }
            else
            {
                final VelocityRequestContext context = contextFactory.getJiraVelocityRequestContext();
                final String baseUrl = context.getCanonicalBaseUrl();

                return makeImageMarkup(constant, baseUrl + iconUrl);
            }
        }
        return "";
    }

    private String makeImageMarkup(IssueConstant constant, String url)
    {
        String name = TextUtils.htmlEncode(constant.getNameTranslation());
        String result = "<img src=\"" + url + "\" height=\"16\" width=\"16\" alt=\"" + name + "\" title=\"" + name + " - ";
        String descTranslation = constant.getDescTranslation() == null ? "" : constant.getDescTranslation();
        result += TextUtils.htmlEncode(descTranslation) + "\"/>";
        return result;
    }

    private String makeMarkupForCell(String value, String url)
    {
        return "<a href='" + url + "'>" + value + "</a>";
    }

    private String makeMarkupForCellWithDescription(String value, String desc, String url)
    {
        if (StringUtils.isBlank(desc))
        {
            return makeMarkupForCell(value, url);
        }
        return "<a href='" + url + "' title='" + desc + "'>" + value + "</a>";
    }

    private List<String> getVersionClasses(final Version version)
    {
        final List<String> classes = new ArrayList<String>();
        if (version.isArchived())
        {
            classes.add("archived_version");
        }
        classes.add("default_image");

        if (version.isReleased() && !version.isArchived())
        {
            classes.add("released_unarchived_version");
        }
        else if (version.isReleased() && version.isArchived())
        {
            classes.add("released_archived_version");
        }
        else if (!version.isReleased() && !version.isArchived())
        {
            classes.add("unreleased_unarchived_version");
        }
        else if (!version.isReleased() && version.isArchived())
        {
            classes.add("unreleased_archived_version");
        }

        return classes;

    }

}
