package com.atlassian.jira.plugin.viewissue.issuelink;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.issuelink.IssueLinkRenderer;
import com.atlassian.jira.plugin.issuelink.IssueLinkRendererModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Converts a remote link to a LinkSource that is used by the velocity macro to display web links.
 *
 * @since v5.0
 */
public class RemoteIssueLinkUtils
{
    public static final String DEFAULT_RELATIONSHIP_I18N_KEY = "issuelinking.remote.link.relationship.default";

    private RemoteIssueLinkUtils() {}

    public static Map<String, List<IssueLinkContext>> convertToIssueLinkContexts(List<RemoteIssueLink> remoteIssueLinks, Long issueId, String baseUrl, I18nHelper i18n, PluginAccessor pluginAccessor)
    {
        Map<String, List<IssueLinkContext>> contextsMap = Maps.newHashMap();
        for (RemoteIssueLink remoteIssueLink : remoteIssueLinks)
        {
            final IssueLinkRendererModuleDescriptor descriptor = getIssueLinkRendererModuleDescriptor(pluginAccessor, remoteIssueLink.getApplicationType());
            final IssueLinkRenderer issueLinkRenderer = descriptor.getModule();
            if (!issueLinkRenderer.shouldDisplay(remoteIssueLink))
            {
                continue;
            }
            
            String relationship = StringUtils.defaultIfEmpty(remoteIssueLink.getRelationship(), i18n.getText(DEFAULT_RELATIONSHIP_I18N_KEY));
            final List<IssueLinkContext> contexts;
            if (contextsMap.containsKey(relationship))
            {
                contexts = contextsMap.get(relationship);
            }
            else
            {
                contexts = Lists.newArrayList();
                contextsMap.put(relationship, contexts);
            }

            final String deleteUrl = String.format(baseUrl + "/secure/DeleteRemoteIssueLink.jspa?id=%d&remoteIssueLinkId=%d", issueId, remoteIssueLink.getId());
            final String html = descriptor.getInitialHtml(remoteIssueLink);

            contexts.add(IssueLinkContext.newRemoteIssueLinkContext("remote-" + remoteIssueLink.getId(), deleteUrl, true, html, remoteIssueLink.getId(), issueLinkRenderer.requiresAsyncLoading(remoteIssueLink)));
        }

        return contextsMap;
    }

    /**
     * Returns the final HTML for the remote issue link.
     *
     * @param remoteIssueLink remote issue link
     * @param pluginAccessor plugin accessor
     * @return final HTML for the remote issue link
     */
    public static String getFinalHtml(RemoteIssueLink remoteIssueLink, PluginAccessor pluginAccessor)
    {
        final IssueLinkRendererModuleDescriptor descriptor = getIssueLinkRendererModuleDescriptor(pluginAccessor, remoteIssueLink.getApplicationType());
        return descriptor.getFinalHtml(remoteIssueLink);
    }

    private static IssueLinkRendererModuleDescriptor getIssueLinkRendererModuleDescriptor(final PluginAccessor pluginAccessor, final String applicationType)
    {
        if (StringUtils.isNotBlank(applicationType))
        {
            List<IssueLinkRendererModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(IssueLinkRendererModuleDescriptor.class);
            for (IssueLinkRendererModuleDescriptor descriptor : descriptors)
            {
                if (descriptor.handlesApplicationType(applicationType))
                {
                    return descriptor;
                }
            }
        }

        return getDefaultLinkRendererModuleDescriptor(pluginAccessor);
    }

    private static IssueLinkRendererModuleDescriptor getDefaultLinkRendererModuleDescriptor(final PluginAccessor pluginAccessor)
    {
        List<IssueLinkRendererModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(IssueLinkRendererModuleDescriptor.class);
        for (IssueLinkRendererModuleDescriptor descriptor : descriptors)
        {
            if (descriptor.isDefaultHandler())
            {
                return descriptor;
            }
        }
        throw new IllegalStateException("No default issue link renderer module descriptor found");
    }
}
