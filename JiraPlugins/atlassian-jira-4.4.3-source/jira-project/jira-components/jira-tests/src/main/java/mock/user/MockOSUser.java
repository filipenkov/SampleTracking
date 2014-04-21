package mock.user;

import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.user.MockCrowdService;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;
import com.opensymphony.user.Group;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;
import com.opensymphony.user.provider.AccessProvider;
import com.opensymphony.user.provider.CredentialsProvider;
import com.opensymphony.user.provider.ProfileProvider;

import java.util.List;

/**
 * A Mock for the OSUser User object.
 * com.opensymphony.user.User
 * 
 * @since v4.3
 */
public class MockOSUser extends User
{
    private String emailAddress;
    private String displayName;
    private PropertySet propertySet;

    /**
     * @param name             username.
     */
    public MockOSUser(String name)
    {
        this(name, name, null);
    }

    public MockOSUser(String name, String fullName, String emailAddress)
    {
        super(name, new MockProviderAccessor(), new MockCrowdService());
        this.displayName = fullName;
        this.emailAddress = emailAddress;
        this.propertySet = new MemoryPropertySet();
        this.propertySet.init(null, null);
    }

    @Override
    public void setEmail(final String email)
    {
        this.emailAddress = email;
    }

    @Override
    public String getEmail()
    {
        return emailAddress;
    }

    @Override
    public void setFullName(final String fullName)
    {
        this.displayName = fullName;
    }

    @Override
    public String getFullName()
    {
        return displayName;
    }

    @Override
    public List<String> getGroups()
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPassword(final String password) throws ImmutableException
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addToGroup(final Group group)
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(final String password)
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean inGroup(final Group group)
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean inGroup(final String groupName)
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove() throws ImmutableException
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeFromGroup(final Group group)
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public void store() throws ImmutableException
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    protected void update()
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActive()
    {
        return true;
    }

    @Override
    public String getEmailAddress()
    {
        return emailAddress;
    }

    @Override
    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public long getDirectoryId()
    {
        return 1;
    }

    @Override
    public CredentialsProvider getCredentialsProvider()
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName()
    {
        return super.getName();
    }

    @Override
    public ProfileProvider getProfileProvider()
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertySet getPropertySet()
    {
        return propertySet;
    }

    @Override
    public AccessProvider getAccessProvider()
    {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMutable()
    {
        return true;
    }

    @Override
    public UserManager getUserManager()
    {
        return super.getUserManager();
    }
}
