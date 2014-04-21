package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import junit.framework.TestCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;


public class SunONETest extends TestCase {

    private SunONE sunOne;
    private static final String SUN_NAME = "Sun Directory Server Enterprise Edition";
    private static final long DIRECTORY_ID = 1;

    protected void setUp() throws Exception
    {
        super.setUp();

        sunOne = new SunONE(null, null, null);
    }

    protected void tearDown() throws Exception
    {
        sunOne = null;
        super.tearDown();
    }

    public void testGetDescriptiveName()
    {
        assertEquals(SUN_NAME, sunOne.getDescriptiveName());
    }

    public void testGetStaticDirectoryType()
    {
        assertEquals(SUN_NAME, SunONE.getStaticDirectoryType());
    }

    public void testEncodePassword()
    {
        String password = "secret";
        // Password should not be encoded
        assertEquals(password, sunOne.encodePassword(password));
    }


    public void testCreateChangeListenerTemplate()
    {
        LDAPPropertiesMapper mockLdapPropertiesMapper = mock(LDAPPropertiesMapper.class);
        when(mockLdapPropertiesMapper.getConnectionURL()).thenReturn("http://url.com");
        when(mockLdapPropertiesMapper.getUsername()).thenReturn("Bob");
        when(mockLdapPropertiesMapper.getPassword()).thenReturn("supersafepassword");
        Hashtable<String, String> dummyEnvironment = new Hashtable<String, String>();
        dummyEnvironment.put("dummy.field.one", "ichi");
        dummyEnvironment.put("dummy.field.two", "ni");
        dummyEnvironment.put("dummy.field.three", "san");
        when(mockLdapPropertiesMapper.getEnvironment()).thenReturn(dummyEnvironment);

        sunOne.ldapPropertiesMapper = mockLdapPropertiesMapper; // mock out the properties mapper

        // all needed attributes set should be able to create template with no error
        sunOne.createChangeListenerTemplate();
    }
    
    public void testGetNewUserDirectorySpecificAttributes() throws NamingException
    {
        LDAPPropertiesMapper mockLdapPropertiesMapper = mock(LDAPPropertiesMapper.class);
        when(mockLdapPropertiesMapper.getUserLastNameAttribute()).thenReturn(LDAPPropertiesMapper.USER_LASTNAME_KEY);

        User user = new UserTemplateWithAttributes("hello", DIRECTORY_ID);
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.USER_EMAIL_KEY, "hello@byebye.com");

        sunOne.ldapPropertiesMapper = mockLdapPropertiesMapper;

        sunOne.getNewUserDirectorySpecificAttributes(user, attributes);

        // a default sn should be set even though it was not specified
        assertEquals("",attributes.get(LDAPPropertiesMapper.USER_LASTNAME_KEY).get(0));
        assertEquals("hello@byebye.com",attributes.get(LDAPPropertiesMapper.USER_EMAIL_KEY).get(0));

    }

}
