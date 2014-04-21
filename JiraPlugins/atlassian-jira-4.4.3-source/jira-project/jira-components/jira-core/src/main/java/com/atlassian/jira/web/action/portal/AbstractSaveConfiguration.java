package com.atlassian.jira.web.action.portal;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.configurable.ObjectConfigurationTypes;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationException;
import com.atlassian.jira.portal.PortletConfigurationManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An utter utter utter utter abomination...unfortunately it has to stick around until legacy portlets are supported
 * no more in JIRA.  This code is still used when saving prefs for legacy portlets via the gadget bridge.
 *
 * As soon as we no longer support legacy portlets, this class should be taken out the back and shot.
 */
public abstract class AbstractSaveConfiguration extends ProjectActionSupport
{
    public static final String MULTISELECT_SEPARATOR = "_*|*_";

    private static final String PREFIX_PROJECT = "project-";
    private static final String PREFIX_FILTER = "filter-";

    protected static final int DEFAULT_SETUP = 0;
    protected static final int VALIDATE_SETUP = 1;
    protected static final int EXECUTE_SETUP = 2;

    private final PermissionManager permissionManager;
    private final SearchRequestService searchRequestService;
    private final PortalPageService portalPageService;
    private final PortletConfigurationManager portletConfigurationManager;

    protected Map localParameters = new HashMap();

    private Long portletConfigId = null;
    private String portletIdStr = null;
    private Portlet portlet = null;
    protected boolean allHidden = true;
    private ObjectConfiguration objectConfiguration = null;
    private PortalPage portalPage = null;

    protected AbstractSaveConfiguration(ProjectManager projectManager, PermissionManager permissionManager,
            SearchRequestService searchRequestService, PortalPageService portalPageService, PortletConfigurationManager portletConfigurationManager)
    {
        super(projectManager, permissionManager);
        this.permissionManager = permissionManager;
        this.searchRequestService = searchRequestService;
        this.portalPageService = portalPageService;
        this.portletConfigurationManager = portletConfigurationManager;
    }

    protected boolean checkForValidPortalParameters(final JiraServiceContext serviceContext)
    {
        PortalPage portalPage = getPortalPage();
        if (portalPage == null)
        {
            addErrorMessage(getText("admin.errors.portal.select.page"));
        }
        else
        {
            if (portalPageService.validateForUpdate(serviceContext, portalPage))
            {
                final PortletConfiguration config = portletConfigurationManager.getByPortletId(portletConfigId);
                if (config == null)
                {
                    addErrorMessage(getText("admin.errors.portal.portlet.does.not.exist"));
                }
                else
                {
                    portlet = config.getPortlet();
                }
            }
        }
        return !serviceContext.getErrorCollection().hasAnyErrors();
    }

    public String doDefault() throws Exception
    {
        final JiraServiceContext serviceContext = getJiraServiceContext();
        if (!checkForValidPortalParameters(serviceContext))
        {
            return ERROR;
        }
        setupConfiguration(DEFAULT_SETUP);
        if (allHidden)
        {
            return doExecute();
        }
        return INPUT;
    }

    public void doValidation()
    {
        final JiraServiceContext serviceContext = getJiraServiceContext();
        if (checkForValidPortalParameters(serviceContext))
        {
            try
            {
                localParameters = ActionContext.getParameters();
                setupConfiguration(VALIDATE_SETUP);
            }
            catch (ObjectConfigurationException e)
            {
                addErrorMessage(getText("portlet.retrieve.error") + ": " + portletIdStr);
                log.error(getText("portlet.retrieve.error") + ": " + portletIdStr, e);
            }
        }
    }

    protected String doExecute() throws Exception
    {
        final JiraServiceContext serviceContext = getJiraServiceContext();
        final PortletConfiguration pc = setupConfiguration(EXECUTE_SETUP);
        portalPageService.saveLegacyPortletConfiguration(serviceContext, pc);
        if (serviceContext.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        return getHomeRedirect();
    }

    protected abstract String getHomeRedirect();

    protected abstract PortalPage loadPortalPage();

    public String getPortletIdStr()
    {
        return portletIdStr;
    }

    public void setPortletIdStr(String portletIdStr)
    {
        this.portletIdStr = portletIdStr;
    }

    public Portlet getPortlet()
    {
        return portlet;
    }

    public Long getPortletConfigId()
    {
        return portletConfigId;
    }

    public void setPortletConfigId(Long portletConfigId) throws PortletConfigurationException
    {
        this.portletConfigId = portletConfigId;
    }

    public String getParamValue(String key)
    {
        Object value = localParameters.get(key);
        if (value == null)
        {
            return null;
        }
        else if (value instanceof String[])
        {
            // String array - get the first value
            String[] strings = (String[]) value;
            return strings.length == 0 ? null : strings[0];
        }
        else if (value instanceof String)
        {
            // single String
            return (String) value;
        }
        else
        {
            log.error("The value of parameter '" + key + "' is not String! "
                    + "Was '" + value + "' of " + value.getClass().getName() + " class");
            return value.toString();
        }
    }

    public List getParamValues(String key)
    {
        Object val = localParameters.get(key);
        if (val == null)
        {
            try
            {
                val = getObjectConfiguration().getFieldDefault(key);
            }
            catch (ObjectConfigurationException e)
            {
                log.error("Could not get object configuration", e);
            }
        }
        if (val == null)
        {
            return Collections.EMPTY_LIST;
        }
        else if (val instanceof String[])
        {
            return Arrays.asList((String[]) val);
        }
        else
        {
            return Arrays.asList(new String[] { val.toString() });
        }
    }

    public String getLocalParameter(String key)
    {
        return (String) localParameters.get(key);
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        if (objectConfiguration == null)
        {
            objectConfiguration = getPortlet().getObjectConfiguration(EasyMap.build("User", getRemoteUser()));
        }
        return objectConfiguration;
    }

    protected PortletConfiguration setupConfiguration(int operation) throws ObjectConfigurationException
    {
        if (getPortlet() == null)
        {
            addErrorMessage(getText("portlet.select.configure"));
        }
        else
        {
            PortletConfiguration portletConf = portletConfigurationManager.getByPortletId(portletConfigId);
            PropertySet ps = null;
            if (portletConf != null)
            {
                ps = portletConf.getProperties();
            }
            syncPropertySetToParams(ps, localParameters, operation);
            return portletConf;
        }
        return null;
    }

    protected void syncPropertySetToParams(PropertySet ps, Map params, int operation)
            throws ObjectConfigurationException
    {
        ObjectConfiguration oc = getObjectConfiguration();
        for (int i = 0; i < oc.getFieldKeys().length; i++)
        {
            String key = oc.getFieldKeys()[i];
            String defaultValue = oc.getFieldDefault(key);
            final int fieldType = oc.getFieldType(key);
            if ((fieldType == ObjectConfigurationTypes.STRING)
                    || (fieldType == ObjectConfigurationTypes.SELECT)
                    || (fieldType == ObjectConfigurationTypes.CASCADINGSELECT))
            {
                allHidden = false;
                switch (operation)
                {
                    case DEFAULT_SETUP:
                    {
                        loadParams(ps, params, key, defaultValue);
                        break;
                    }

                    case VALIDATE_SETUP:
                    {
                        if (!params.containsKey(key) && !getExcludedFieldKeys().contains(key))
                        {
                            addError(key, getText("portlet.fill.out") + " " + getText(oc.getFieldName(key)));
                        }
                        break;
                    }

                    case EXECUTE_SETUP:
                    {
                        if (params.containsKey(key))
                        {
                            ps.setString(key, ((String[]) params.get(key))[0]);
                        }
                        break;
                    }
                }
            }
            else
            if (fieldType == ObjectConfigurationTypes.FILTERPICKER || fieldType == ObjectConfigurationTypes.FILTERPROJECTPICKER)
            {
                allHidden = false;
                switch (operation)
                {
                    case DEFAULT_SETUP:
                    {
                        loadParams(ps, params, key, defaultValue);
                        break;
                    }

                    case VALIDATE_SETUP:
                    {
                        if (!params.containsKey(key) && !getExcludedFieldKeys().contains(key))
                        {
                            addError(key, getText("portlet.fill.out") + " " + getText(oc.getFieldName(key)));
                        }
                        String value = ((String[]) params.get(key))[0];
                        if (StringUtils.isBlank(value))
                        {
                            addError(key, getText("portlet.fill.out") + " " + getText(oc.getFieldName(key)));
                        }
                        else if (fieldType != ObjectConfigurationTypes.FILTERPROJECTPICKER && !isVisibleFilter(value))
                        {
                            addError(key, getText("admin.errors.filters.nonexistent"));
                        }
                        else if (fieldType == ObjectConfigurationTypes.FILTERPROJECTPICKER)
                        {
                            if (isProjectValue(value))
                            {
                                Long projectId = getProjectIdFromValue(value);
                                final Project project = projectManager.getProjectObj(projectId);
                                if (project == null)
                                {
                                    addError(key, getText("admin.errors.portal.project.nonexist"));
                                }
                                else if (!permissionManager.hasPermission(Permissions.BROWSE, project, getRemoteUser()))
                                {
                                    addError(key, getText("admin.errors.portal.project.no.permission"));
                                }
                            }
                            else if (isFilterValue(value))
                            {
                                if (!isVisibleFilter((value.substring(PREFIX_FILTER.length()))))
                                {
                                    addError(key, getText("admin.errors.filters.nonexistent"));
                                }
                            }
                            else
                            {
                                log.warn("Could not parse value for filterprojectpicker");
                            }
                        }
                        break;
                    }

                    case EXECUTE_SETUP:
                    {
                        if (params.containsKey(key))
                        {
                            ps.setString(key, ((String[]) params.get(key))[0]);
                        }
                        break;
                    }
                }
            }
            else if (fieldType == ObjectConfigurationTypes.MULTISELECT)
            {
                allHidden = false;
                switch (operation)
                {
                    case DEFAULT_SETUP:
                    {
                        if (ps != null && ps.exists(key))
                        {
                            if (ps.getType(key) == PropertySet.STRING)
                            {
                                String[] vals = org.apache.commons.lang.StringUtils.splitByWholeSeparator(ps.getString(key), MULTISELECT_SEPARATOR);
                                params.put(key, vals);
                            }
                        }
                        else if (defaultValue != null)
                        {
                            params.put(key, defaultValue);
                        }
                        break;
                    }

                    case VALIDATE_SETUP:
                    {
                        if (!params.containsKey(key) && !getExcludedFieldKeys().contains(key))
                        {
                            addError(key, getText("portlet.fill.out") + " " + getText(oc.getFieldName(key)));
                        }
                        break;
                    }

                    case EXECUTE_SETUP:
                    {
                        if (params.containsKey(key))
                        {
                            StringBuffer value = new StringBuffer();
                            String[] strings = ((String[]) params.get(key));
                            for (int j = 0; j < strings.length; j++)
                            {
                                value.append(strings[j]);
                                if ((j + 1) < strings.length)
                                {
                                    value.append(MULTISELECT_SEPARATOR);
                                }
                            }
                            ps.setString(key, value.toString());
                        }
                        break;
                    }
                }
            }
            else if (fieldType == ObjectConfigurationTypes.HIDDEN)
            {
                switch (operation)
                {
                    case DEFAULT_SETUP:
                    {
                        loadParams(ps, params, key, defaultValue);
                        break;
                    }

                    case EXECUTE_SETUP:
                    {
                        ps.setString(key, oc.getFieldDefault(key));
                        break;
                    }
                }
            }
            else if (fieldType == ObjectConfigurationTypes.TEXT)
            {
                allHidden = false;
                switch (operation)
                {
                    case DEFAULT_SETUP:
                    {
                        loadParams(ps, params, key, defaultValue);
                        break;
                    }
                    case VALIDATE_SETUP:
                    {
                        if (!params.containsKey(key) && !getExcludedFieldKeys().contains(key))
                        {
                            addError(key, getText("portlet.fill.out") + " " + getText(oc.getFieldName(key)));
                        }
                        break;
                    }
                    case EXECUTE_SETUP:
                    {
                        if (params.containsKey(key))
                        {
                            ps.setText(key, ((String[]) params.get(key))[0]);
                        }
                        break;
                    }
                }
            }
            else if (fieldType == ObjectConfigurationTypes.LONG)
            {
                allHidden = false;
                switch (operation)
                {
                    case DEFAULT_SETUP:
                    {
                        loadParams(ps, params, key, defaultValue);
                        break;
                    }
                    case VALIDATE_SETUP:
                    {
                        if (params.containsKey(key) && !getExcludedFieldKeys().contains(key))
                        {
                            String param = ((String[]) params.get(key))[0];
                            if (param.length() > 0)
                            {
                                try
                                {
                                    Long.parseLong(param);
                                }
                                catch (NumberFormatException ex)
                                {
                                    addError(key, getText(oc.getFieldName(key)) + " " + getText("portlet.must.be.number"));
                                }
                            }
                        }
                        else if (!getExcludedFieldKeys().contains(key))
                        {
                            addError(key, getText("portlet.fill.out") + " " + getText(oc.getFieldName(key)));
                        }
                        break;
                    }
                    case EXECUTE_SETUP:
                    {
                        if (params.containsKey(key))
                        {
                            // This has been extracted so that the child classes can use this method but perhaps
                            // do a setLong instead of a setString
                            addLongToPropertySet(ps, params, key);
                        }
                        break;
                    }
                }
            }
// As best I can tell - this code works, but I didn't have time to check & test, so decided to use a select instead
// of a checkbox.  If you do use this code - make sure you do some testing. - Scott Farquhar, 9/Oct/2008
//            else if (fieldType == ObjectConfigurationTypes.CHECKBOX)
//            {
//                allHidden = false;
//                switch (operation)
//                {
//                    case DEFAULT_SETUP:
//                    {
//                        loadParams(ps, params, key, defaultValue);
//                        break;
//                    }
//                    case VALIDATE_SETUP:
//                    {
//                        break;
//                    }
//                    case EXECUTE_SETUP:
//                    {
//                        if (params.containsKey(key))
//                        {
//                            ps.setString(key, ((String[]) params.get(key))[0]);
//                        }
//                        else
//                        {
//                            ps.remove(key);
//                        }
//                        break;
//                    }
//                }
//            }
            else
            {
                throw new UnsupportedOperationException(getText("portlet.have.not.implemented") + ": " + fieldType);
            }
        }
    }

    private Long getProjectIdFromValue(final String value)
    {
        return new Long(Long.parseLong(value.substring(PREFIX_PROJECT.length())));
    }

    private boolean isFilterValue(final String value)
    {
        return value.substring(0, PREFIX_FILTER.length()).equals(PREFIX_FILTER);
    }

    private boolean isProjectValue(final String value)
    {
        return value.substring(0, PREFIX_PROJECT.length()).equals(PREFIX_PROJECT);
    }

    boolean isVisibleFilter(String value)
    {
        try
        {
            Long filterId = new Long(value);
            JiraServiceContextImpl ctx = new JiraServiceContextImpl(getRemoteUser());
            return !ctx.getErrorCollection().hasAnyErrors() && searchRequestService.getFilter(ctx, filterId) != null;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    protected void addLongToPropertySet(PropertySet ps, Map params, String key)
    {
        ps.setString(key, ((String[]) params.get(key))[0]);
    }

    private void loadParams(PropertySet ps, Map params, String key, String defaultValue)
    {
        if (ps != null && ps.exists(key))
        {
            switch (ps.getType(key))
            {
                case PropertySet.STRING:
                    params.put(key, ps.getString(key));
                    break;
                case PropertySet.TEXT:
                    params.put(key, ps.getText(key));
                    break;
            }
        }
        else if (defaultValue != null)
        {
            params.put(key, defaultValue);
        }
    }

    // Needs to be public so it can be used by the com.atlassian.jira.web.tags.TextTag
    public String getUnescapedText(String key)
    {
        final Portlet portlet = getPortlet();
        if (portlet == null)
        {
            return super.getUnescapedText(key);
        }

        return portlet.getDescriptor().getI18nBean().getUnescapedText(key);
    }

    /**
     * Returns the list of keys that are not enabled keys.
     *
     * @return the excluded keys.
     */
    protected List /*<String>*/ getExcludedFieldKeys()
    {
        try
        {
            final ObjectConfiguration oc = getObjectConfiguration();
            final String[] fieldKeys = oc.getFieldKeys();
            if (fieldKeys != null && fieldKeys.length > 0)
            {
                final List excludedKeys = new ArrayList(Arrays.asList(fieldKeys));
                final String[] enabledFieldKeys = oc.getEnabledFieldKeys();
                if (enabledFieldKeys != null && enabledFieldKeys.length > 0)
                {
                    excludedKeys.removeAll(Arrays.asList(enabledFieldKeys));
                }
                return excludedKeys;
            }
        }
        catch (ObjectConfigurationException e)
        {
            log.error("Error getting excluded fields", e);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns the user this action if performed for - remote user. This action can be only performed for the same user
     * as remote user.
     *
     * @return remote user
     */
    public User getUser()
    {
        return getRemoteUser();
    }

    /**
     * Does a name lookup on the given filter or project value.
     *
     * @param filterIdParamName the name of the parameter whose value should be used to look up the filter by id.
     * @return the name of the filter.
     */
    public String getFilterName(String filterIdParamName)
    {
        if (filterIdParamName == null || filterIdParamName.equals(""))
        {
            log.warn("filteridParamName was " + filterIdParamName);
            return "";
        }
        String filterId = getParamValue(filterIdParamName);
        if (filterId == null)
        {
            log.warn("no param with key " + filterIdParamName);
            return "";
        }
        return getFilterDisplayName(filterId);
    }

    private String getFilterDisplayName(final String filterId)
    {
        String name = "";
        try
        {
            Long filterIdLong = new Long(Long.parseLong(filterId));
            final SearchRequest filter = searchRequestService.getFilter(new JiraServiceContextImpl(getRemoteUser()), filterIdLong);
            if (filter != null)
            {
                name = filter.getName();
            }
            else
            {
                log.warn("no filter with id: " + filterIdLong);
                name = getText("admin.errors.portal.filters.invalid");
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("no filter found with id " + filterId);
        }
        return name;
    }

    /**
     * Takes a parameter name, looks up its value and from that, eats a magic encoded project-or-filter value formatted
     * in accordance with the protocol:
     * <p/>
     * <ul> <li>project-12345</li> <li>filter-67890</li> </ul> The prefix must be either {@link #PREFIX_FILTER} or
     * {@link #PREFIX_PROJECT}
     *
     * @param filterOrProjectParamName the name of a parameter whose value adheres to the above protocol.
     * @return the name of the filter or project whose id is encoded in the value of the given parameter.
     */
    public String getFilterProjectName(String filterOrProjectParamName)
    {
        if (filterOrProjectParamName == null || filterOrProjectParamName.equals(""))
        {
            log.warn("filteridParamName was " + filterOrProjectParamName);
            return "";
        }
        String value = getParamValue(filterOrProjectParamName);
        if (StringUtils.isBlank(value))
        {
            if (value == null)
            {
                log.warn("no param with key " + filterOrProjectParamName);
            }
            return "";
        }
        String name = "";

        try
        {
            if (isProjectValue(value))
            {
                // we have a project, crop of the project indicator
                Long projectId = getProjectIdFromValue(value);
                final Project project = projectManager.getProjectObj(projectId);
                if (project == null)
                {
                    log.warn("no such project id: " + projectId);
                    name = getText("admin.errors.portal.project.invalid");
                }
                else if (!permissionManager.hasPermission(Permissions.BROWSE, project, getRemoteUser()))
                {
                    log.warn("cannot browse project id: " + projectId);
                    name = getText("admin.errors.portal.project.invalid");
                }
                else
                {
                    name = project.getName();
                }
            }
            else if (isFilterValue(value))
            {
                // we have a filter, crop off the filter indicator
                name = getFilterDisplayName(value.substring(PREFIX_FILTER.length()));
            }
            else
            {
                log.warn("cannot understand value of parameter " + filterOrProjectParamName + ": " + value);
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("no filter or project found for " + value);
        }
        return name;
    }

    public PortalPageService getPortalPageService()
    {
        return portalPageService;
    }

    private PortalPage getPortalPage()
    {
        if (portalPage == null)
        {
            portalPage = loadPortalPage();
        }
        return portalPage;
    }
}
