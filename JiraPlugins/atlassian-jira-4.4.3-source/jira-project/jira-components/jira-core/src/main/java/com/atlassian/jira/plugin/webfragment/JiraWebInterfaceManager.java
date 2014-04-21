package com.atlassian.jira.plugin.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.JiraKeyUtilsBean;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Jira wrapper for the {@link WebInterfaceManager}.
 * If generating simple links for menus, use {@link com.atlassian.jira.plugin.webfragment.SimpleLinkManager} as then consumers
 * can insert {@link com.atlassian.jira.plugin.webfragment.SimpleLinkFactory} into the link generation process.
 *
 */
public class JiraWebInterfaceManager
{
    public static final String CONTEXT_KEY_USER = "user";
    public static final String CONTEXT_KEY_USERNAME = "username";
    public static final String CONTEXT_KEY_HELPER = "helper";
    public static final String CONTEXT_KEY_LOCATION = "location";
    public static final String CONTEXT_KEY_I18N = "i18n";

    private static final String PARAM_HIGHTLIGHT = "highlight";
    private static final String PARAM_SELECTED = "selected";
    private static final String PARAM_SELECTED_2 = "selected2";
    private static final String PARAM_SELECTED_3 = "selected3";
    private static final String PARAM_BROWSE_RPOJECT = "browseProject";

    private WebInterfaceManager webInterfaceManager;
    private JiraKeyUtilsBean jiraKeyUtilsBean;

    public JiraWebInterfaceManager(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.jiraKeyUtilsBean = new JiraKeyUtilsBean();
    }

    public boolean hasSectionsForLocation(String location)
    {
        return webInterfaceManager.hasSectionsForLocation(location);
    }

    public List getSections(String location)
    {
        return webInterfaceManager.getSections(location);
    }

    public List getDisplayableSections(String location, User remoteUser, JiraHelper jiraHelper)
    {
        return webInterfaceManager.getDisplayableSections(location, makeContext(remoteUser, jiraHelper));
    }

    public List getItems(String section)
    {
        return webInterfaceManager.getItems(section);
    }

    public List getDisplayableItems(String section, User remoteUser, JiraHelper jiraHelper)
    {
        List items = webInterfaceManager.getDisplayableItems(section, makeContext(remoteUser, jiraHelper));
        final HttpServletRequest servletRequest = jiraHelper.getRequest();
        if (servletRequest != null)
        {
            for (Object item : items)
            {
                WebItemModuleDescriptor webItemModuleDescriptor = (WebItemModuleDescriptor) item;
                Map<String, String> params = webItemModuleDescriptor.getParams();
                if (params.containsKey(PARAM_SELECTED) || params.containsKey(PARAM_SELECTED_2) || params.containsKey(PARAM_SELECTED_3))
                {
                    highlightItem(servletRequest, params);
                }
            }
        }
        return items;
    }

    //check if the item should be highlighted.
    private void highlightItem(HttpServletRequest servletRequest, Map<String, String> params)
    {
        final String selected = getParamFromMap(params, PARAM_SELECTED);
        final String selected2 = getParamFromMap(params, PARAM_SELECTED_2);
        final String selected3 = getParamFromMap(params, PARAM_SELECTED_3);
        final String servletPath = servletRequest.getServletPath();
        if (servletPath.startsWith(selected) || servletPath.startsWith(selected2) || servletPath.startsWith(selected3))
        {
            params.put(PARAM_HIGHTLIGHT, "true");
            //JRA-12737: Check if the item is trying to Browse Project.  If so we set the highlight param to true
            String browseProject = (String) params.get(PARAM_BROWSE_RPOJECT);
            final String pathInfo = servletRequest.getPathInfo();
            if (StringUtils.isNotEmpty(pathInfo) && !pathInfo.equals("/") &&
                    StringUtils.isNotEmpty(browseProject) && Boolean.parseBoolean(browseProject))
            {
                final String proj = pathInfo.indexOf("/", 1) == -1 ? pathInfo.substring(1) : pathInfo.substring(1, pathInfo.indexOf("/", 1));

                if (jiraKeyUtilsBean.validProjectKey(proj.toUpperCase()))
                {
                    params.put(PARAM_HIGHTLIGHT, "true");
                }
                else
                {
                    params.remove(PARAM_HIGHTLIGHT);
                }
            }
        }
        else
        {
            params.remove(PARAM_HIGHTLIGHT);
        }
    }

    /**
     * Looks up the parameter by given key and returns its value. If such parameter is not found, returns a space (" ").
     *
     * @param params map of params
     * @param key    parameter key
     * @return parameter value if found, "space" otherwise
     */
    private String getParamFromMap(Map params, String key)
    {
        final String value = (String) params.get(key);
        return value == null ? " " : value;
    }

    public void refresh()
    {
        webInterfaceManager.refresh();
    }

    public WebFragmentHelper getWebFragmentHelper()
    {
        return webInterfaceManager.getWebFragmentHelper();
    }

    @VisibleForTesting
    protected Map<String, Object> makeContext(User remoteUser, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();
        //TODO: Continue to put the OSUser object into the context (For the WebFragment Condition) until we remove OSUser, at which point we will convert to Crowd User.
        params.put(CONTEXT_KEY_USER, OSUserConverter.convertToOSUser(remoteUser));
        params.put(CONTEXT_KEY_HELPER, jiraHelper);
        
        return params;
    }
}
