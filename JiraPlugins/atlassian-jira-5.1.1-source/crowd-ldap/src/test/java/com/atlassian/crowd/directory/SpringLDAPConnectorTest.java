package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.name.SearchDN;
import com.atlassian.crowd.model.LDAPDirectoryEntity;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.ContextMapper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpringLDAPConnectorTest
{
    @Test
    public void standardiseDnDelegatesBasedOnProperties()
    {
        SpringLDAPConnector slc = mock(SpringLDAPConnector.class);

        slc.ldapPropertiesMapper = mock(LDAPPropertiesMapper.class);

        when(slc.ldapPropertiesMapper.isRelaxedDnStandardisation()).thenReturn(false);
        assertEquals("cn=test", slc.standardiseDN("CN = test"));

        when(slc.ldapPropertiesMapper.isRelaxedDnStandardisation()).thenReturn(true);
        assertEquals("cn = test", slc.standardiseDN("CN = test"));
    }

    @Test
    public void findEntityByDNStandardisesItsSearchDN() throws Exception
    {
        SpringLDAPConnector slc = mock(SpringLDAPConnector.class);
        slc.ldapPropertiesMapper = mock(LDAPPropertiesMapper.class);
        slc.searchDN = mock(SearchDN.class);
        when(slc.findEntityByDN(Mockito.anyString(), Mockito.<Class<? extends LDAPDirectoryEntity>>any())).thenCallRealMethod();

        assertEquals("dn=x", slc.standardiseDN("DN=X"));

        slc.findEntityByDN("DN=X", LDAPUserWithAttributes.class);
        verify(slc).findEntityByDN(Mockito.eq("dn=x"), Mockito.anyString(), Mockito.anyString(), Mockito.<ContextMapper>anyObject(), Mockito.eq(LDAPUserWithAttributes.class));
    }
}
