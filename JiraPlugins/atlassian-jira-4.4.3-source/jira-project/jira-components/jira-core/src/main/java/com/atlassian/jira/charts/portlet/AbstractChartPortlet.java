package com.atlassian.jira.charts.portlet;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.portal.LazyLoadingPortlet;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.portlet.bean.PortletRenderer;

import java.util.Map;

import static com.atlassian.jira.charts.ChartFactory.PORTLET_IMAGE_HEIGHT;
import static com.atlassian.jira.charts.ChartFactory.PORTLET_IMAGE_WIDTH;

/**
 * Abstract chart to be extended by others
 *
 * @since v4.0
 */
public abstract class AbstractChartPortlet extends PortletImpl implements LazyLoadingPortlet
{
    private static final String DAYSPREVIOUS = "daysprevious";

    protected final VelocityRequestContextFactory velocityRequestContextFactory;
    protected final ChartUtils chartUtils;

    public AbstractChartPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, VelocityRequestContextFactory velocityRequestContextFactory,
            ChartUtils chartUtils)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.chartUtils = chartUtils;
    }

    protected int getDaysPrevious(PortletConfiguration portletConfiguration) throws ObjectConfigurationException
    {
        int days = 30;

        final Long daysPrevious = portletConfiguration.getLongProperty(DAYSPREVIOUS);
        if (daysPrevious != null)
        {
            days = daysPrevious.intValue();
        }
        return days;
    }

    public Map<String, Object> generateCommonParameters(PortletConfiguration portletConfiguration)
    {
        final Map<String, Object> params = super.getVelocityParams(portletConfiguration);

        params.put("user", authenticationContext.getUser());
        params.put("imageWidth", PORTLET_IMAGE_WIDTH);
        params.put("imageHeight", PORTLET_IMAGE_HEIGHT);
        return params;
    }

    public String getLoadingHtml(PortletConfiguration portletConfiguration)
    {
        if (authenticationContext.getUser() == null)
        {
            return PortletRenderer.RENDER_NO_OUTPUT_AND_NO_AJAX_CALLHOME;
        }

        final Map<String, Object> velocityParams = super.getVelocityParams(portletConfiguration);

        velocityParams.put("portletConfig", portletConfiguration);
        velocityParams.put("portletId", portletConfiguration.getId());
        return getDescriptor().getHtml("loading", velocityParams);
    }

    public String getStaticHtml(PortletConfiguration portletConfiguration)
    {
        return "";
    }
}