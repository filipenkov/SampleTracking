package com.atlassian.crowd.embedded.admin.delegatingldap;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.LdapTypeConfig;
import com.atlassian.crowd.embedded.admin.ConfigurationController;
import com.atlassian.crowd.embedded.api.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DelegatingLdapConfigurationController extends ConfigurationController
{
    private static final Logger log = LoggerFactory.getLogger(DelegatingLdapConfigurationController.class);
    
    private LDAPPropertiesMapper ldapPropertiesMapper;

    protected Map referenceData(HttpServletRequest request) throws Exception
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("ldapDirectoryTypes", getLdapDirectoryTypes());
        model.put("ldapTypeConfigurations", getLdapTypeConfigurations());
        return model;
    }
    
    protected Directory createDirectory(Object command)
    {
        return directoryMapper.buildDelegatingLdapDirectory((DelegatingLdapDirectoryConfiguration) command);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception
    {
        if (directoryRetriever.hasDirectoryId(request))
        {
            Directory directory = directoryRetriever.getDirectory(request);
            return directoryMapper.toDelegatingLdapConfiguration(directory);
        }
        DelegatingLdapDirectoryConfiguration configuration = (DelegatingLdapDirectoryConfiguration) createCommand();
        configuration.setLdapAutoAddGroups(getDefaultLdapAutoAddGroups());
        return configuration;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
    {
        super.initBinder(request, binder);
        binder.setRequiredFields(new String[] {
            "name", "type", "hostname", "port", "ldapBasedn", "ldapUserUsername"
        });
    }

    private Map<String, String> getLdapDirectoryTypes()
    {
        Map<String, String> directoryTypes = new LinkedHashMap<String, String>();
        Map<String, String> implementations = ldapPropertiesMapper.getImplementations();

        // switch keys and values because the Crowd map is back-to-front
        for (Map.Entry<String, String> implementation : implementations.entrySet())
        {
            directoryTypes.put(implementation.getValue(), implementation.getKey());
        }
        return directoryTypes;
    }

    private List<LdapTypeConfig> getLdapTypeConfigurations()
    {
        return ldapPropertiesMapper.getLdapTypeConfigurations();
    }

    public void setLdapPropertiesMapper(LDAPPropertiesMapper ldapPropertiesMapper)
    {
        this.ldapPropertiesMapper = ldapPropertiesMapper;
    }
}
