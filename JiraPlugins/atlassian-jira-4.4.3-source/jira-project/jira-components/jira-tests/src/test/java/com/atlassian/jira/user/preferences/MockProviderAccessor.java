package com.atlassian.jira.user.preferences;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.memory.SerializablePropertySet;
import com.opensymphony.user.ProviderAccessor;
import com.opensymphony.user.UserManager;
import com.opensymphony.user.provider.AccessProvider;
import com.opensymphony.user.provider.CredentialsProvider;
import com.opensymphony.user.provider.ProfileProvider;
import com.opensymphony.user.provider.UserProvider;

import java.util.List;
import java.util.Properties;

/**
 * Mock implementation of ProviderAccessor
 *
 * @since v3.12
 */
public class MockProviderAccessor implements ProviderAccessor
{
    private UserManager userManager = new UserManager();
    private AccessProvider accessProvider;
    private CredentialsProvider credentialsProvider = new MockCredentialsProvider();
    private MockProfileProvider profileProvider = new MockProfileProvider();

    public AccessProvider getAccessProvider(String name)
    {
        return accessProvider;
    }

    public void setAccessProvider(AccessProvider accessProvider)
    {
        this.accessProvider = accessProvider;
    }

    public CredentialsProvider getCredentialsProvider(String name)
    {
        return credentialsProvider;
    }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider)
    {
        this.credentialsProvider = credentialsProvider;
    }

    public ProfileProvider getProfileProvider(String name)
    {
        return profileProvider;
    }

    public MockProfileProvider getProfileProvider()
    {
        return profileProvider;
    }

    public UserManager getUserManager()
    {
        return userManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    class MockProfileProvider extends MockUserProvider implements ProfileProvider {
        PropertySet propertySet;
        public MockProfileProvider() {
            propertySet = new SerializablePropertySet();
            propertySet.init(null, null);
        }
        public void setPropertySet(PropertySet propertySet)
        {
            this.propertySet = propertySet;
        }

        public PropertySet getPropertySet(String name)
        {
            return propertySet;
        }
    }

    class MockCredentialsProvider extends MockUserProvider implements CredentialsProvider {

        public boolean authenticate(String name, String password)
        {
            return false;
        }

        public boolean changePassword(String name, String password)
        {
            return false;
        }
    }

    class MockUserProvider implements UserProvider
    {

        public boolean create(String name)
        {
            return false;
        }

        public void flushCaches()
        {
        }

        public boolean handles(String name)
        {
            return false;
        }

        public boolean init(Properties properties)
        {
            return false;
        }

        public List<String> list()
        {
            return null;
        }

        public boolean remove(String name)
        {
            return false;
        }
    }
}
