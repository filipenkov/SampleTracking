package com.atlassian.jira.web.action.admin;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.service.services.imap.ImapService;
import com.atlassian.jira.service.services.pop.PopService;
import com.atlassian.jira.util.PathUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import webwork.action.ParameterAware;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The action to allow you to edit service definitions
 * <p/>
 * NOTE TO FUTURE DEVELOPERS : The FileService and ExportService uses to allow the directory to be edited online. Since
 * the "breached as" incident we have taken this away - JRA-21400.  However we need to preserve the directory for
 * existing customers so you will see some code malarky to preserve any current values but not allowed them to be input
 * from the web.
 *
 * The methods are namely {@link #primeParamsWithSavedValues(java.util.Map)} and {@link #sanitizeParams(java.util.Map)} 
 */
@WebSudoRequired
public class EditService extends JiraWebActionSupport implements ParameterAware
{
    private final ServiceManager serviceManager;

    private Long id;
    private Map params;
    private long delay;
    private ObjectConfiguration oc;
    private String removedPath;

    private JiraServiceContainer service = null;

    public EditService(ServiceManager serviceManager)
    {
        this.serviceManager = serviceManager;
    }

    public String doDefault() throws Exception
    {
        if (canEditService(id))
        {
            delay = getService().getDelay() / (60 * 1000);

            // prime our data for display
            getObjectConfigurationKeys();

            return INPUT;
        }
        else
        {
            return "securitybreach";
        }
    }

    private boolean canEditService(final Long serviceId) throws Exception
    {
        return Iterables.any(serviceManager.getServicesManageableBy(getLoggedInUser()), new Predicate<JiraServiceContainer>()
        {
            @Override
            public boolean apply(@Nullable JiraServiceContainer aServiceManageableByTheUser)
            {
                return serviceId.equals(aServiceManageableByTheUser.getId());
            }
        });

        // return Iterables.contains(serviceManager.getServicesManageableBy(getLoggedInUser()), serviceToDelete);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (canEditService(id))
        {
            try
            {
                serviceManager.editService(id, delay * 60000, primeParamsWithSavedValues(params));
            }
            catch (Exception e)
            {
                log.error("Error occurred trying to update service properties: " + e, e);
                addErrorMessage(getText("admin.errors.error.occured.trying.to.update.service.properties", e));
            }

            if (getHasErrorMessages())
            {
                return ERROR;
            }
            else
            {
                return getRedirect("ViewServices!default.jspa");
            }
        }
        else
        {
            return "securitybreach";
        }
    }

    /**
     * This will preserve any of the banned values that have been removed from view but need to continue for backwards
     * compatibility reasons
     *
     * @param webParameters the web parameters as input
     *
     * @return the sensible parameters for the server
     *
     * @throws Exception because the code is shite!
     */
    private Map<String, String[]> primeParamsWithSavedValues(final Map<String, String[]> webParameters) throws Exception
    {
        HashMap<String, String[]> newParameters = new HashMap<String, String[]>();
        newParameters.putAll(webParameters);

        for (String bannedKey : BANNED_KEYS)
        {
            String value = getParamValue(bannedKey);
            if (StringUtils.isNotBlank(value))
            {
                newParameters.put(bannedKey, new String[] { value });
            }
        }

        return newParameters;
    }

    protected void doValidation()
    {
        if (!serviceManager.containsServiceWithId(id))
        {
            addErrorMessage(getText("admin.errors.service.does.not.exist"));
        }
        if (delay < 1)
        {
            addError("delay", getText("admin.errors.delay.too.short"));
        }
        if (this.params.containsKey(FileService.KEY_SUBDIRECTORY))
        {
            try
            {
                String subdirectory = ((String[]) this.params.get(FileService.KEY_SUBDIRECTORY))[0];
                File subdirectoryFile =  new File(getJiraHome().getHome(), PathUtils.joinPaths(FileService.MAIL_DIR, subdirectory)).getCanonicalFile();
                File mailDir = new File(getJiraHome().getHome(), FileService.MAIL_DIR).getCanonicalFile();
                if (!(subdirectoryFile.getParentFile().equals(mailDir) || subdirectoryFile.equals(mailDir)))
                {
                    addError("subdirectory",getText("admin.errors.fileservice.directory.is.not.subdirectory"));
                }
                if (!subdirectoryFile.exists())
                {
                    addError("subdirectory",getText("admin.errors.fileservice.directory.not.exist"));

                }

            }
            catch (IOException e)
            {
                addError("subdirectory",getText("admin.errors.fileservice.could.not.find"));
            }
            catch (Exception e)
            {
                addError("subdirectory",getText("admin.errors.fileservice.could.not.find"));
            }
        }
        super.doValidation();
    }

    /**
     * @return
     */
    public boolean isUnsafeService()
    {
        try
        {
            if (serviceManager.containsServiceWithId(id))
            {
                final JiraServiceContainer serviceWithId = serviceManager.getServiceWithId(id);
                final String serviceClass = serviceWithId.getServiceClass();
                return "com.atlassian.jira.service.services.export.ExportService".equals(serviceClass) || "com.atlassian.jira.service.services.file.FileService".equals(serviceClass);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return false;
    }

    public ObjectConfiguration getObjectConfiguration() throws Exception
    {
        if (oc == null)
        {
            oc = getService().getObjectConfiguration();
        }
        return oc;
    }

    public String[] getObjectConfigurationKeys() throws Exception
    {
        final String[] fieldKeys = getObjectConfiguration().getFieldKeys();
        final List<String> sanitizedFieldKeys = new ArrayList<String>(fieldKeys.length);
        for (String fieldKey : fieldKeys)
        {
            if (!isBannedKey(fieldKey))
            {
                sanitizedFieldKeys.add(fieldKey);
            }
            else
            {
                removedPath = getParamValue(fieldKey);
            }
        }
        return sanitizedFieldKeys.toArray(new String[sanitizedFieldKeys.size()]);
    }

    private static final Set<String> BANNED_KEYS = new HashSet<String>();

    static
    {
        BANNED_KEYS.add("DIR_NAME");
        BANNED_KEYS.add("directory");
    }

    private boolean isBannedKey(final String fieldKey)
    {
        return (isUnsafeService() && BANNED_KEYS.contains(fieldKey));
    }

    private Map sanitizeParams(Map parameters)
    {
        Map<String, Object> sanitizedMap = new HashMap<String, Object>();
        //noinspection unchecked
        sanitizedMap.putAll(parameters);
        for (Iterator<Map.Entry<String, Object>> iterator = sanitizedMap.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry<String, Object> entry = iterator.next();
            if (isBannedKey(entry.getKey()))
            {
                iterator.remove();
            }
        }
        return sanitizedMap;
    }

    public void setParameters(Map map)
    {
        this.params = sanitizeParams(map);
    }

    public Map getParameters()
    {
        return params;
    }

    public String getParamValue(String s) throws Exception
    {
        if (getService().getProperties() != null)
        {
            return getService().getProperty(s);
        }
        else
        {
            return "";
        }
    }

    public List getParamValues(String key)
    {
        Map inputParams = getParameters();
        if (inputParams == null)
        {
            return getDefaultValue(key);
        }
        else
        {
            Object val = inputParams.get(key);
            if (val == null)
            {
                return getDefaultValue(key);
            }
            else
            {
                if (val instanceof String[])
                {
                    return Arrays.asList((String[]) val);
                }
                else
                {
                    return Arrays.asList(val.toString());
                }
            }
        }
    }

    private List getDefaultValue(String key)
    {
        try
        {
            String s = getObjectConfiguration().getFieldDefault(key);
            return s == null ? Collections.EMPTY_LIST : Arrays.asList(s);
        }
        catch (ObjectConfigurationException e)
        {
            log.warn("Unable to obtain default value for '" + key + "'. Using empy value.", e);
        }
        catch (Exception e)
        {
            log.warn("Unable to obtain default value for '" + key + "'. Using empy value.", e);
        }
        return Collections.EMPTY_LIST;
    }

    public JiraServiceContainer getService() throws Exception
    {
        if (service == null)
        {
            service = serviceManager.getServiceWithId(id);
        }
        return service;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }


    public long getDelay()
    {
        return delay;
    }

    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    public String getDescription() throws Exception
    {
        return getObjectConfiguration().getDescription(null);
    }

    public String getRemovedPath()
    {
        return removedPath;
    }

    public boolean isValidMailParameters()
    {
        if (!JiraSystemProperties.isDecodeMailParameters())
        {
            if (id != null)
            {
                try
                {
                    final JiraServiceContainer serviceContainer = getService();
                    if (serviceContainer != null)
                    {
                        //We only want to show the error messages for these services.
                        final Set<String> mailServices = CollectionBuilder.newBuilder(ImapService.class.getName(), PopService.class.getName(), FileService.class.getName()).asSet();
                        return !mailServices.contains(serviceContainer.getServiceClass());
                    }
                }
                catch (Exception e)
                {
                    //ignored.
                }
            }
        }
        return true;
    }

    private JiraHome getJiraHome()
    {
        return ComponentManager.getComponentInstanceOfType(JiraHome.class);
    }

}
