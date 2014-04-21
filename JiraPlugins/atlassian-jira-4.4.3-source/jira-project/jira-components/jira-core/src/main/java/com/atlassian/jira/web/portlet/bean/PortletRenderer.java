package com.atlassian.jira.web.portlet.bean;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.portal.LazyLoadingPortlet;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.util.profiling.UtilTimerStack;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to render all portlets.
 * @since Jira 3.7
 */

public class PortletRenderer
{

    private static final Logger log = Logger.getLogger(PortletRenderer.class);

    public PortletRenderer()
    {

    }

    /**
     * Renders the portlet refered to by the portlet configuration.
     * @param req
     * @param res
     * @param portletConfiguration
     * @param allowLazyLoading This overrides the portlet configuration to disable lazy loading if set to false.
     * @return HTML portlet
     */
    public String renderPortlet(HttpServletRequest req, HttpServletResponse res, PortletConfiguration portletConfiguration, boolean allowLazyLoading)
    {
        StringBuffer outputBuffer = new StringBuffer();

        Portlet portlet = portletConfiguration.getPortlet();
        boolean lazyLoad = portlet.getDescriptor().isLazyLoad();

        if(portlet instanceof LazyLoadingPortlet)
        {
            outputBuffer.append(((LazyLoadingPortlet) portlet).getStaticHtml(portletConfiguration));
        }

        if(lazyLoad && allowLazyLoading)
        {
            outputBuffer.append(getLazyLoadHtml(portletConfiguration, portlet, req));
        }
        else
        {
            String normalHtml = getNormalHtml(portletConfiguration, portlet);
            if (normalHtml != null)
            {
                outputBuffer.append(normalHtml);
            }
            else
            {
                return null;
            }
        }
        return outputBuffer.toString();
    }

    private static String getNormalHtml(PortletConfiguration portletConfiguration, Portlet portlet)
    {
        StringBuffer outputBuffer = new StringBuffer();

        //currently we only cater for both Velocity
        // JSP portlets are called from the JSP directly
        UtilTimerStack.push("Running velocity portlet: " + portletConfiguration.getId());
        String viewHtml = portlet.getViewHtml(portletConfiguration);
        UtilTimerStack.pop("Running velocity portlet: " + portletConfiguration.getId());
        if (viewHtml != null)
        {
            //velocity portlet
            outputBuffer.append(viewHtml);
        }
        else
        {

            // ToDo: Clean this up! Decide whether we should handle JSPs in the portlet renderer
            // The JSP where this is embedded needs to include the JSP itself
            return null;

        }
        return outputBuffer.toString();
    }

    private String getLazyLoadHtml(PortletConfiguration portletConfiguration, Portlet portlet, HttpServletRequest req)
    {
        StringBuffer outputBuffer = new StringBuffer();
        Map wrapperParams = EasyMap.build("portletId", portletConfiguration.getId(),
                "req", req, "portletName", portletConfiguration.getPortlet().getName());

        outputBuffer.append(renderVelocityTemplate("templates/plugins/jira/portlets/lazyloadingportlet-prefix.vm", wrapperParams));

        Map velocityParams = new HashMap();//super.getVelocityParams(portletConfiguration);
        velocityParams.put("portletConfig", portletConfiguration);

        velocityParams.put("portletId", portletConfiguration.getId());

        UtilTimerStack.push("Running velocity portlet: " + portletConfiguration.getId());

        String loadingPage;

        if (portlet instanceof LazyLoadingPortlet)
        {
            loadingPage = ((LazyLoadingPortlet) portlet).getLoadingHtml(portletConfiguration);
        }
        else
        {
            loadingPage = portlet.getDescriptor().getHtml("loading", velocityParams);
        }

        if (loadingPage == null)
        {
            loadingPage = renderVelocityTemplate("templates/plugins/jira/portlets/lazyloadingportlet-loading.vm", wrapperParams);
        }

        UtilTimerStack.pop("Running velocity portlet: " + portletConfiguration.getId());

        // the identity check is intensional. If the magic string instance is returned we dont want to render or include the ajax callback stuff.
        if( RENDER_NO_OUTPUT_AND_NO_AJAX_CALLHOME != loadingPage ){
            outputBuffer.append(loadingPage);
            outputBuffer.append(renderVelocityTemplate("templates/plugins/jira/portlets/lazyloadingportlet-suffix.vm", wrapperParams));
        }
        return outputBuffer.toString();
    }


    private static String renderVelocityTemplate(String templateLocation, Map startingParams)
    {
        try
        {
            VelocityManager velocityManager = ManagerFactory.getVelocityManager();

            return velocityManager.getEncodedBody(templateLocation, "", ManagerFactory.getApplicationProperties().getEncoding(), startingParams);
        }
        catch (Throwable e)
        {
            log.error("Error while rendering velocity template for '" + templateLocation + "'.", e);
            return "";
        }
    }

    /**
     * Portlets must return this sentinel value to indicate that they dont wish to render any content as well as not
     * including the ajax call home javascript.
     */
    static final public String RENDER_NO_OUTPUT_AND_NO_AJAX_CALLHOME = PortletRenderer.class.getName() + ".RENDER_NO_OUTPUT_AND_NO_AJAX_CALLHOME";
}