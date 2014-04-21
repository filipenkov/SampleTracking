package com.atlassian.jira;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.ProviderAccessor;
import com.opensymphony.user.UserManager;
import com.opensymphony.user.provider.AccessProvider;
import com.opensymphony.user.provider.CredentialsProvider;
import com.opensymphony.user.provider.ProfileProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A mock implementation of the OSUser ProviderAccessor abomination. TODO provide manipulation methods and implement
 * ProviderAccessor in those terms
 */
public class MockProviderAccessor implements ProviderAccessor
{
    /**
     * delegate
     */
    private ProviderAccessor providerAccessorProxy;

    public MockProviderAccessor()
    {
        this("Administrator", "admin@example.com");
    }

    /**
     * Specify the name and email
     *
     * @param fullName full name
     * @param email email
     */
    public MockProviderAccessor(final String fullName, final String email)
    {
        this(fullName, email, "en_AU");
    }

    /**
     * Specify the name and email
     *
     * @param fullName full name
     * @param groupNameList the liust of groups names
     */
    public MockProviderAccessor(final String fullName, final /*<String>*/ List groupNameList)
    {
        this(groupNameList,EasyMap.build("fullName", fullName,PreferenceKeys.USER_LOCALE, "en_AU"));
    }

    /**
     * Main ctor for User.
     * <p/>
     * Specify the name, email and locale (if used by i18nBeans for instance).
     *
     * @param fullName full name
     * @param email email
     * @param locale the user's locale
     */
    public MockProviderAccessor(final String fullName, final String email, final String locale)
    {
        this(EasyMap.build("fullName", fullName,"email", email,PreferenceKeys.USER_LOCALE, locale));
    }

    public MockProviderAccessor(final Map properties)
    {
        this(Collections.EMPTY_LIST, properties);
    }

    private MockProviderAccessor(final /*<String>*/ List groupNameList, final Map properties)
    {
        Object credentialsProvider = new Object()
        {
        };
        final CredentialsProvider credentialsProviderProxy = (CredentialsProvider) DuckTypeProxy.getProxy(CredentialsProvider.class, credentialsProvider);

        Object accessProvider = new Object()
        {
            public boolean inGroup(String username, String groupname)
            {
                return "test".equals(username) && "test".equals(groupname);
            }

            public List listGroupsContainingUser(String username)
            {
                return groupNameList;
            }
        };
        final AccessProvider accessProviderProxy = (AccessProvider) DuckTypeProxy.getProxy(AccessProvider.class, accessProvider);

        Object propertySet = new Object()
        {
            public String getString(String name)
            {
                return (String) properties.get(name);
            }

            public void setString(String name, String value)
            {
                properties.put(name, value);
            }

            public boolean exists(String name)
            {
                return properties.containsKey(name);
            }

            public void remove(java.lang.String name) throws com.opensymphony.module.propertyset.PropertyException
            {
                properties.remove(name);
            }
        };
        final PropertySet propertySetProxy = (PropertySet) DuckTypeProxy.getProxy(PropertySet.class, propertySet);

        Object profileProvider = new Object()
        {
            public boolean handles(String string)
            {
                return true;
            }

            public PropertySet getPropertySet(String string)
            {
                return propertySetProxy;
            }

        };
        final ProfileProvider profileProviderProxy = (ProfileProvider) DuckTypeProxy.getProxy(ProfileProvider.class, profileProvider);


        Object providerAccessor = new Object()
        {
            public ProfileProvider getProfileProvider(String name)
            {
                return profileProviderProxy;
            }

            public CredentialsProvider getCredentialsProvider(String name)
            {
                return credentialsProviderProxy;
            }

            public AccessProvider getAccessProvider(String name)
            {
                return accessProviderProxy;
            }
        };
        providerAccessorProxy = (ProviderAccessor) DuckTypeProxy.getProxy(ProviderAccessor.class, providerAccessor);
    }


    public AccessProvider getAccessProvider(String s)
    {
        return providerAccessorProxy.getAccessProvider(s);
    }

    public CredentialsProvider getCredentialsProvider(String s)
    {
        return providerAccessorProxy.getCredentialsProvider(s);
    }

    public ProfileProvider getProfileProvider(String s)
    {
        return providerAccessorProxy.getProfileProvider(s);
    }

    public UserManager getUserManager()
    {
        throw new UnsupportedOperationException("uh-oh, world of pain. if you see this, you are probably calling User.inGroup(String) or Group.add/removeMember(Principal) methods. You need to resolve all User and Group instances before you get to the User/Group calls.");
    }
}
