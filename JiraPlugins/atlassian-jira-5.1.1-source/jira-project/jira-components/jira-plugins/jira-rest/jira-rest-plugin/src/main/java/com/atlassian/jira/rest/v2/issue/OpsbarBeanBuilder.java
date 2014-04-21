package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.util.IssueOperationsBarUtil;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
public class OpsbarBeanBuilder
{

    private final IssueOperationsBarUtil issueOperationsBarUtil;
    private final SimpleLinkManager simpleLinkManager;
    private final JiraAuthenticationContext authContext;
    private final Issue issue;
    private final PluginAccessor pluginAccessor;
    private final JiraHelper helper;
    private final ContextI18n i18n;

    public OpsbarBeanBuilder(Issue issue, ApplicationProperties applicationProperties,
            SimpleLinkManager simpleLinkManager,
            JiraAuthenticationContext authContext,
            ContextI18n i18n,
            IssueManager issueManager,
            PluginAccessor pluginAccessor)
    {
        this.simpleLinkManager = simpleLinkManager;
        this.authContext = authContext;
        this.issue = issue;
        this.pluginAccessor = pluginAccessor;

        this.i18n = i18n;
        helper = getJiraHelper(issue);
        issueOperationsBarUtil = new IssueOperationsBarUtil(helper, authContext.getLoggedInUser(), simpleLinkManager,
                applicationProperties, issueManager, i18n);
    }

    public OpsbarBean build()
    {
        LinkGroupBean opsbarGroups = buildOpsbarGroup();
        LinkGroupBean toolsBuilder = buildToolsGroup();

        return new OpsbarBean(Lists.newArrayList(opsbarGroups, toolsBuilder));
    }

    private LinkGroupBean buildToolsGroup()
    {
        LinkGroupBean.Builder toolsBuilder = new LinkGroupBean.Builder();
        toolsBuilder.id("jira.issue.tools");
        toolsBuilder.addLinks(toBeans(simpleLinkManager.getLinksForSection("jira.issue.tools", authContext.getLoggedInUser(), helper)));

        List<IssueViewModuleDescriptor> issueViews = pluginAccessor.getEnabledModuleDescriptorsByClass(IssueViewModuleDescriptor.class);
        LinkGroupBean.Builder viewsGroupBuilder = new LinkGroupBean.Builder();
        viewsGroupBuilder.header(new SimpleLinkBean(null, null, i18n.getText("common.concepts.views"), i18n.getText("admin.issue.views.plugin.tooltip"), null, "icon-view"));
        for (IssueViewModuleDescriptor issueView : issueViews)
        {
            viewsGroupBuilder.addLinks(new SimpleLinkBean(null, null, issueView.getName(), null,
                    helper.getRequest().getContextPath() + issueView.getURLWithoutContextPath(issue.getKey()), null));
        }

        toolsBuilder.addGroups(viewsGroupBuilder.build());
        return toolsBuilder.build();
    }

    private LinkGroupBean buildOpsbarGroup()
    {
        LinkGroupBean.Builder opsbarGroups = new LinkGroupBean.Builder();
        opsbarGroups.id("view.issue.opsbar");

        SimpleLink editOrLoginLink = issueOperationsBarUtil.getEditOrLoginLink(issue);
        if (editOrLoginLink != null)
        {
            opsbarGroups.addGroups(new LinkGroupBean.Builder().addLinks(toBean(editOrLoginLink)).build());
        }

        for (SimpleLinkSection group : issueOperationsBarUtil.getGroups())
        {
            final LinkGroupBean.Builder builder = new LinkGroupBean.Builder();
            builder.addLinks(toBeans(issueOperationsBarUtil.getPromotedLinks(group)));
            List<SimpleLinkSection> dropdownSections = issueOperationsBarUtil.getNonEmptySectionsForGroup(group);
            final LinkGroupBean.Builder dropdown = new LinkGroupBean.Builder();
            dropdown.header(new SimpleLinkBean(group.getId() + "_more", null, group.getLabel(), null, null, null));
            for (SimpleLinkSection dropdownSection : dropdownSections)
            {
                final LinkGroupBean.Builder dropdownSectionBuilder = new LinkGroupBean.Builder();
                List<SimpleLink> dropdownSectionLinks = issueOperationsBarUtil.getNonPromotedLinksForSection(group, dropdownSection);
                dropdownSectionBuilder.addLinks(toBeans(dropdownSectionLinks));
                dropdown.addGroups(dropdownSectionBuilder.build());
            }
            builder.addGroups(dropdown.build());
            opsbarGroups.addGroups(builder.build());
        }
        return opsbarGroups.build();
    }

    private List<SimpleLinkBean> toBeans(List<SimpleLink> input)
    {
        final List<SimpleLinkBean> ret = new ArrayList<SimpleLinkBean>();
        for (SimpleLink simpleLink : input)
        {
            ret.add(toBean(simpleLink));
        }
        return ret;
    }

    private SimpleLinkBean toBean(SimpleLink input)
    {
        return new SimpleLinkBean(input);
    }

    private JiraHelper getJiraHelper(final Issue issue)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("issue", issue);
        params.put("issueId", issue.getId());
        return new JiraHelper(ExecutingHttpRequest.get(), issue.getProjectObject(), params);
    }
}
