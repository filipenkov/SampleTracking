package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * This abstract class implements the standard way of rendering this fragment's HTML in {@link #getHtml(BrowseContext)}
 * method and base Velocity parameters creation via {@link #createVelocityParams(BrowseContext)} method.
 *
 * @since v4.0
 */
public abstract class AbstractFragment implements ProjectTabPanelFragment
{
    protected static final Logger log = Logger.getLogger(AbstractFragment.class);

    protected final VelocityTemplatingEngine velocityManager;
    protected final JiraAuthenticationContext authenticationContext;

    protected AbstractFragment(final VelocityTemplatingEngine templatingEngine,
            final JiraAuthenticationContext authenticationContext)
    {
        this.velocityManager = templatingEngine;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Returns the path to the template directory
     *
     * @return the path to the template directory
     */
    protected abstract String getTemplateDirectoryPath();

    /**
     * Creates new map of velocity parameters.
     * <p/>
     * This map contains:
     * <p/>
     * <ul> <li>fragid - value returned by {@link #getId()}</li> <li>project - project got from the context passed
     * in</li> <li>i18n - i18n bean from the authentication context</li> </ul>
     *
     * @param ctx browse context
     * @return new velocity parameters map
     */
    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        velocityParams.put("fragid", getId());
        velocityParams.put("i18n", authenticationContext.getI18nHelper());
        velocityParams.put("browseContext", ctx);
        velocityParams.putAll(ctx.createParameterMap());
        return velocityParams;
    }

    /**
     * Renders the fragment.
     * <p/>
     * It uses Veocity parameters created by {@link #createVelocityParams(BrowseContext)} method. If you need to pass in
     * more parameters, please override {@link #createVelocityParams(BrowseContext)} method.
     *
     * @param ctx the context that this fragment is being rendered in.
     * @return the escaped HTML to include.
     */
    public String getHtml(final BrowseContext ctx)
    {
        final String template = getId() + ".vm";
        try
        {
            final Map<String, Object> velocityParams = createVelocityParams(ctx);
            return velocityManager.render(file(getTemplateDirectoryPath() + template)).applying(velocityParams).asHtml();
        }
        catch (VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + getTemplateDirectoryPath() + template + "'.", e);
            return "";
        }
    }

}
