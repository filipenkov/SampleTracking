package com.atlassian.crowd.directory.ldap.util;

import com.atlassian.crowd.directory.GenericLDAP;
import com.atlassian.crowd.directory.MicrosoftActiveDirectory;
import com.atlassian.crowd.directory.OpenLDAP;
import com.atlassian.crowd.directory.OpenLDAPRfc2307;
import com.atlassian.crowd.directory.Rfc2307;
import com.atlassian.crowd.directory.ldap.LdapTypeConfig;
import com.google.common.collect.Iterators;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.TextNode;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LDAPPropertiesHelperImplTest
{
    private final LDAPPropertiesHelperImpl ldapPropertiesHelper;

    private final Map<String, JsonNode> configByDirectory;

    public LDAPPropertiesHelperImplTest() throws Exception
    {
        ldapPropertiesHelper = new LDAPPropertiesHelperImpl();

        final ObjectMapper objectMapper = new ObjectMapper();
        configByDirectory = new HashMap<String, JsonNode>();
        for (LdapTypeConfig config : ldapPropertiesHelper.getLdapTypeConfigs())
        {
            configByDirectory.put(config.getKey(), objectMapper.readTree(config.getLdapTypeAsJson()));
        }
    }

    @Test
    public void testLdapUserEncryptionVisibility() throws Exception
    {
        assertTrue(isFieldHidden(configByDirectory.get(MicrosoftActiveDirectory.class.getName()), "ldap-user-encryption"));
        assertFalse(isFieldHidden(configByDirectory.get(OpenLDAP.class.getName()), "ldap-user-encryption"));
        assertFalse(isFieldHidden(configByDirectory.get(OpenLDAPRfc2307.class.getName()), "ldap-user-encryption"));
        assertFalse(isFieldHidden(configByDirectory.get(GenericLDAP.class.getName()), "ldap-user-encryption"));
        assertFalse(isFieldHidden(configByDirectory.get(Rfc2307.class.getName()), "ldap-user-encryption"));
    }

    @Test
    public void testLdapUsermembershipUseForGroupsVisibility() throws Exception
    {
        assertTrue(isFieldHidden(configByDirectory.get(OpenLDAP.class.getName()), "ldap-usermembership-use-for-groups"));
        assertFalse(isFieldHidden(configByDirectory.get(MicrosoftActiveDirectory.class.getName()), "ldap-usermembership-use-for-groups"));
    }

    @Test
    public void testIncrementalSyncEnabledVisibility() throws Exception
    {
        assertTrue(isFieldHidden(configByDirectory.get(OpenLDAP.class.getName()), "crowd-sync-incremental-enabled"));
        assertFalse(isFieldHidden(configByDirectory.get(MicrosoftActiveDirectory.class.getName()), "crowd-sync-incremental-enabled"));
    }

    public boolean isFieldHidden(JsonNode jsonConfig, String fieldName)
    {
        final Iterator<JsonNode> elements = jsonConfig.get("hidden").getElements();
        return Iterators.contains(elements, TextNode.valueOf(fieldName));
    }
}
