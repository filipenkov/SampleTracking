package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Factory to return the options for the different sorting options
 *
 * @since v5.0
 */
public class AttachmentSortingOptionsFactory implements SimpleLinkFactory
{
    private final VelocityRequestContextFactory requestContextFactory;
    private final JiraAuthenticationContext authenticationContext;

    public AttachmentSortingOptionsFactory(VelocityRequestContextFactory requestContextFactory, JiraAuthenticationContext authenticationContext)
    {
        this.requestContextFactory = requestContextFactory;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    @Override
    public List<SimpleLink> getLinks(User user, Map<String, Object> params)
    {
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        final Issue issue = (Issue) params.get("issue");


        final VelocityRequestSession session = requestContext.getSession();
        final String baseUrl = requestContext.getBaseUrl();
        final String sortingOrder = (String) session.getAttribute(SessionKeys.VIEWISSUE_ATTACHMENT_SORTBY);

        boolean sortedByName = "fileName".equals(sortingOrder) || StringUtils.isBlank(sortingOrder);


        final SimpleLink allLink = new SimpleLinkImpl("attachment-sort-key-name", i18n.getText("viewissue.attachments.sort.key.name"), i18n.getText("viewissue.subtasks.tab.show.all.name"),
                null, sortedByName ? "aui-list-checked aui-checked" : "aui-list-checked", baseUrl + "/browse/" + issue.getKey() + "?attachmentSortBy=fileName#attachmentmodule", null);
        final SimpleLink openLink = new SimpleLinkImpl("attachment-sort-key-date", i18n.getText("viewissue.attachments.sort.key.date"), i18n.getText("viewissue.attachments.sort.key.date"),
                null, !sortedByName ? "aui-list-checked aui-checked" : "aui-list-checked", baseUrl + "/browse/" + issue.getKey() + "?attachmentSortBy=dateTime#attachmentmodule", null);

        return CollectionBuilder.list(allLink, openLink);
    }
}
