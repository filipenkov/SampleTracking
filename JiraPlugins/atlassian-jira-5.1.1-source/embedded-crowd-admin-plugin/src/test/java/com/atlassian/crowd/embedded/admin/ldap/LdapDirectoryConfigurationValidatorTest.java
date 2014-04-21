package com.atlassian.crowd.embedded.admin.ldap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LdapDirectoryConfigurationValidatorTest
{
    @Mock
    private Errors errors;

    private LdapDirectoryConfigurationValidator validator = new LdapDirectoryConfigurationValidator();
    private LdapDirectoryConfiguration conf = new LdapDirectoryConfiguration();

    @Before
    public void setUp() throws Exception
    {
        validator = new LdapDirectoryConfigurationValidator();
        conf = new LdapDirectoryConfiguration();
    }

    @Test
    public void testValidate_AutoAddGroups() throws Exception
    {
        conf.setLdapAutoAddGroups("group1");
        validator.validate(conf, errors);

        verifyZeroInteractions(errors);
    }

    @Test
    public void testValidate_AutoAddGroups_Invalid() throws Exception
    {
        conf.setLdapAutoAddGroups("group|");
        validator.validate(conf, errors);

        verify(errors).rejectValue("ldapAutoAddGroups", "invalid");
    }
}
