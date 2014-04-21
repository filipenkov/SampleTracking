package com.atlassian.crowd.embedded.admin.ldap;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.LdapTypeConfig;
import com.atlassian.crowd.embedded.admin.ConfigurationController;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PermissionOption;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LdapConfigurationController extends ConfigurationController
{
    private PasswordEncoderFactory passwordEncoderFactory;
    private LDAPPropertiesMapper ldapPropertiesMapper;

    protected Map referenceData(HttpServletRequest request) throws Exception
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("ldapDirectoryTypes", getLdapDirectoryTypes());
        model.put("ldapTypeConfigurations", getLdapTypeConfigurations());
        model.put("ldapPermissionOptions", getLdapPermissionOptions());
        model.put("ldapPasswordEncryptionTypes", getPasswordEncryptionTypes());
        return model;
    }

    protected Directory createDirectory(Object command)
    {
        return directoryMapper.buildLdapDirectory((LdapDirectoryConfiguration) command);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception
    {
        if (directoryRetriever.hasDirectoryId(request))
        {
            Directory directory = directoryRetriever.getDirectory(request);
            return directoryMapper.toLdapConfiguration(directory);
        }

        LdapDirectoryConfiguration configuration = (LdapDirectoryConfiguration) createCommand();

        // support AD specific selection
        if (request.getPathInfo().endsWith("/activedirectory/"))
        {
            configuration.setType("com.atlassian.crowd.directory.MicrosoftActiveDirectory");
            configuration.setName(getI18nResolver().getText("embedded.crowd.directory.edit.ldap.field.default.ad"));
        }
        else
        {
            configuration.setName(getI18nResolver().getText("embedded.crowd.directory.edit.ldap.field.default.ldap"));
        }
        configuration.setLdapAutoAddGroups(getDefaultLdapAutoAddGroups());

        return configuration;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
    {
        super.initBinder(request, binder);
        binder.setRequiredFields(new String[] {
            "name", "type", "hostname", "port", "ldapBasedn", "ldapPermissionOption",
            "ldapUserObjectclass", "ldapUserFilter", "ldapUserUsername", "ldapUserFirstname", "ldapUserLastname", "ldapUserDisplayname", "ldapUserEmail", "ldapUserGroup", "ldapUserPassword",
            "ldapGroupObjectclass", "ldapGroupFilter", "ldapGroupName", "ldapGroupDescription", "ldapGroupUsernames", "ldapCacheSynchroniseIntervalInMin",
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

    private List<String> getLdapPermissionOptions()
    {
        List<String> options = new ArrayList<String>();
        for (PermissionOption option : PermissionOption.values())
        {
            options.add(option.name());
        }
        return options;
    }

    private List<LdapTypeConfig> getLdapTypeConfigurations()
    {
        return ldapPropertiesMapper.getLdapTypeConfigurations();
    }

    public Map<String, String> getPasswordEncryptionTypes()
    {
        Map<String, String> encryptionTypes = new LinkedHashMap<String, String>();
        for (String encoder : passwordEncoderFactory.getSupportedLdapEncoders())
        {
            encryptionTypes.put(encoder, encoder.toUpperCase());
        }
        return encryptionTypes;
    }

    public void setPasswordEncoderFactory(PasswordEncoderFactory passwordEncoderFactory)
    {
        this.passwordEncoderFactory = passwordEncoderFactory;
    }

    public void setLdapPropertiesMapper(LDAPPropertiesMapper ldapPropertiesMapper)
    {
        this.ldapPropertiesMapper = ldapPropertiesMapper;
    }
}
