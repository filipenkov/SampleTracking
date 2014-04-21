package com.atlassian.jira.portal;

/**
 * This class allows the implementation of lazily loaded portlets. The portal
 * page loads and shows a placeholder. An AJAX request is then sent to retrieve
 * the portlet's actual body. Normal portlets can be lazily loaded with a default
 * placeholder or a portlet can implement this interface and display customized
 * loading HTML and additionally static HTML that will not be swapped out when the
 * actual portlet body is displayed.
 *
 * In order for a portlet to be lazily loaded the attribute of the portlet tag "lazy"
 * in the atlassian-plugin.xml must be set to true.
 * @since Jira 3.7
 * @see PortletImpl
 * @see Portlet
 * @see com.atlassian.jira.web.portlet.bean.PortletRenderer
 */
public interface LazyLoadingPortlet extends Portlet
{
    /**
     * This method should return temporary HTML 
     * @param portletConfiguration The portlet configuration for the current request
     * @return A HTML fragment
     */
    public String getLoadingHtml(PortletConfiguration portletConfiguration);

    /**
     * <strong>All JavaScript should be returned by this method</strong>.
     * If the JavaScript is not returned by this method it may not bound in the when you try and
     * invoke it from your portlet.
     *
     * This method should return any HTML that will not be swapped out because of lazy loading.
     * The resultant HTML returned by this method will be displayed regardless of whether lazy
     * loading is active or not. The static HTML will placed in front of the Loading HTML.
     *
     *
     * @param portletConfiguration
     * @return A HTML fragment
     */
    public String getStaticHtml(PortletConfiguration portletConfiguration);
}
