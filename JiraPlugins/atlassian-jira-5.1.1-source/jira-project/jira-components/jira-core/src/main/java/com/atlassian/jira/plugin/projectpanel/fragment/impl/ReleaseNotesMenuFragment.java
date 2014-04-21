package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.log4j.Logger;

/**
 * Release Notes Link for a version
 *
 * @since v4.0
 */
public class ReleaseNotesMenuFragment implements MenuFragment
{
    private static final Logger log = Logger.getLogger(ReleaseNotesMenuFragment.class);

    final VelocityRequestContextFactory requestContextFactory;
    private final I18nBean.BeanFactory i18nFactory;

    public ReleaseNotesMenuFragment(VelocityRequestContextFactory requestContextFactory, I18nBean.BeanFactory i18nFactory)
    {
        this.requestContextFactory = requestContextFactory;
        this.i18nFactory = i18nFactory;
    }


    public String getId()
    {
        return "release-notes-lnk";
    }

    public String getHtml(BrowseContext ctx)
    {
        final VelocityRequestContext velocityRequestContext = requestContextFactory.getJiraVelocityRequestContext();
        final String baseUrl = velocityRequestContext.getBaseUrl();
        final I18nHelper i18n = i18nFactory.getInstance(ctx.getUser());
        try
        {
            final BrowseVersionContext versionContext = (BrowseVersionContext) ctx;
            return "<a id=\"release-notes-lnk\" class=\"lnk\"href=\"" + baseUrl + "/secure/ReleaseNote.jspa?projectId=" + ctx.getProject().getId() +
                    "&version=" + versionContext.getVersion().getId() + "\">" + i18n.getText("common.concepts.releasenotes") + "</a>";
        }
        catch (ClassCastException e)
        {
            log.error("The supplied context must be of type BrowseVersionContext", e);
            throw new IllegalArgumentException("The supplied context must be of type BrowseVersionContext", e);
        }
    }

    public boolean showFragment(BrowseContext ctx)
    {
        return true;
    }
}
