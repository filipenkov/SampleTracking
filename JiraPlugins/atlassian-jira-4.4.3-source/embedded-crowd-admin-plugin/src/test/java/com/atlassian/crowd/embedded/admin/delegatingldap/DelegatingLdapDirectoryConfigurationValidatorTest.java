package com.atlassian.crowd.embedded.admin.delegatingldap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class DelegatingLdapDirectoryConfigurationValidatorTest
{
    @Mock
    private Errors errors;

    private DelegatingLdapDirectoryConfigurationValidator validator = new DelegatingLdapDirectoryConfigurationValidator();
    private DelegatingLdapDirectoryConfiguration conf = new DelegatingLdapDirectoryConfiguration();

    @Before
    public void setUp() throws Exception
    {
        validator = new DelegatingLdapDirectoryConfigurationValidator();
        conf = new DelegatingLdapDirectoryConfiguration();
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

    @Test
    public void testValidate_CreateUserOnAuth_ValidationFailsWhenRequiredFieldsMissing() throws Exception
    {
        conf.setLdapAutoAddGroups("");
        conf.setCreateUserOnAuth(true);
        conf.setLdapUserObjectclass("");
        conf.setLdapUserFilter("");
        conf.setLdapUserUsername("");
        conf.setLdapUserUsernameRdn("");
        conf.setLdapUserFirstname("");
        conf.setLdapUserLastname("");
        conf.setLdapUserDisplayname("");
        conf.setLdapUserEmail("");
        validator.validate(conf, errors);

        verify(errors).rejectValue("ldapUserObjectclass", "required");
        verify(errors).rejectValue("ldapUserFilter", "required");
        verify(errors).rejectValue("ldapUserUsername", "required");
        verify(errors).rejectValue("ldapUserUsernameRdn", "required");
        verify(errors).rejectValue("ldapUserFirstname", "required");
        verify(errors).rejectValue("ldapUserLastname", "required");
        verify(errors).rejectValue("ldapUserDisplayname", "required");
        verify(errors).rejectValue("ldapUserEmail", "required");
    }

    private void setValidCreateUserOnAuthValues()
    {
        conf.setCreateUserOnAuth(true);
        conf.setLdapUserObjectclass("blah");
        conf.setLdapUserFilter("blah");
        conf.setLdapUserUsername("blah");
        conf.setLdapUserUsernameRdn("blah");
        conf.setLdapUserFirstname("blah");
        conf.setLdapUserLastname("blah");
        conf.setLdapUserDisplayname("blah");
        conf.setLdapUserEmail("blah");
    }

    private void setValidSynchroniseGroupMembershipsValues()
    {
        conf.setLdapGroupObjectclass("blah");
        conf.setLdapGroupFilter("blah");
        conf.setLdapGroupName("blah");
        conf.setLdapGroupDescription("blah");
        conf.setLdapGroupUsernames("blah");
        conf.setLdapUserGroup("blah");
    }

    @Test
    public void testValidate_CreateUserOnAuth_ValidationSucceedsWhenRequiredFieldsPresent() throws Exception
    {
        conf.setLdapAutoAddGroups("");
        setValidCreateUserOnAuthValues();

        validator.validate(conf, errors);

        verifyZeroInteractions(errors);
    }

    @Test
    public void testValidate_SynchroniseGroupMemberships_ValidationFailsWhenRequiredFieldsMissing() throws Exception
    {
        conf.setLdapAutoAddGroups("");
        setValidCreateUserOnAuthValues();

        conf.setSynchroniseGroupMemberships(true);
        conf.setLdapGroupObjectclass("");
        conf.setLdapGroupFilter("");
        conf.setLdapGroupName("");
        conf.setLdapGroupDescription("");
        conf.setLdapGroupUsernames("");
        conf.setLdapUserGroup("");

        validator.validate(conf, errors);

        verify(errors).rejectValue("ldapGroupObjectclass", "required");
        verify(errors).rejectValue("ldapGroupFilter", "required");
        verify(errors).rejectValue("ldapGroupName", "required");
        verify(errors).rejectValue("ldapGroupDescription", "required");
        verify(errors).rejectValue("ldapGroupUsernames", "required");
        verify(errors).rejectValue("ldapUserGroup", "required");
    }

    @Test
    public void testValidate_SynchroniseGroupMemberships_ValidationSucceedsWhenRequiredFieldsPresent() throws Exception
    {
        conf.setLdapAutoAddGroups("");
        setValidCreateUserOnAuthValues();
        setValidSynchroniseGroupMembershipsValues();

        validator.validate(conf, errors);

        verifyZeroInteractions(errors);
    }
}
