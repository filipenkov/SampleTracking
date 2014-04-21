package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.model.InternalDirectoryEntity;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.util.InternalEntityUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * Encapsulates the concept of crowd user.
 */
public class InternalUser extends InternalDirectoryEntity implements TimestampedUser
{
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String displayName;
    private PasswordCredential credential;

    // lower case versions for index
    private String lowerName;
    private String lowerEmailAddress;
    private String lowerFirstName;
    private String lowerLastName;
    private String lowerDisplayName;

    private List<InternalUserCredentialRecord> credentialRecords = new ArrayList<InternalUserCredentialRecord>();

    protected InternalUser()
    {

    }

    /**
     * This constructor is used by the importer only.
     *
     * @param internalEntityTemplate template.
     * @param directory              directory reference.
     * @param userTemplate           user template.
     * @param credential             encrypted credential.
     */
    public InternalUser(final InternalEntityTemplate internalEntityTemplate, final Directory directory, final UserTemplate userTemplate, final PasswordCredential credential)
    {
        super(internalEntityTemplate, directory);

        updateDetailsFrom(userTemplate);

        setCredential(credential);
    }

    public InternalUser(final UserTemplateWithCredentialAndAttributes user, final Directory directory)
    {
        this(user, directory, user.getCredential());

        if (user.getCreatedDate() != null)
        {
            this.createdDate = user.getCreatedDate();
        }
        else
        {
            this.setCreatedDateToNow();
        }

        if (user.getUpdatedDate() != null)
        {
            this.updatedDate = user.getUpdatedDate();
        }
        else
        {
            this.setUpdatedDateToNow();
        }

        for (PasswordCredential credential : user.getCredentialHistory())
        {
            this.getCredentialRecords().add(new InternalUserCredentialRecord(this, credential.getCredential()));
        }
    }

    /**
     * Constructor used for adding a new user.
     *
     * @param user       user template.
     * @param directory  directory of user.
     * @param credential password of user.
     */
    public InternalUser(final User user, final Directory directory, final PasswordCredential credential)
    {
        super();
        Validate.notNull(user, "user argument cannot be null");
        Validate.notNull(directory, "directory argument cannot be null");
        validateCredential(credential);

        setName(user.getName());
        this.directory = directory;

        updateDetailsFrom(user);

        this.credential = credential;
    }

    private static void validateCredential(final PasswordCredential credential)
    {
        if (credential != null)
        {
            Validate.notNull(credential.getCredential(), "credential argument cannot have null value");
            Validate.isTrue(credential.isEncryptedCredential(), "credential must be encrypted");
        }
    }

    private void validateUser(User user)
    {
        // the param "user" must have the same name username and directory id as "this"

        Validate.notNull(user, "user argument cannot be null");
        Validate.notNull(user.getDirectoryId(), "user argument cannot have a null directoryId");
        Validate.notNull(user.getName(), "user argument cannot have a null name");

        Validate.isTrue(user.getDirectoryId() == this.getDirectoryId(), "directoryId of updated user (" + user.getDirectoryId() + ") does not match the directoryId of the existing user(" + this.getDirectoryId() + ").");
        Validate.isTrue(user.getName().equals(this.getName()), "username of updated user does not match the username of the existing user.");
    }

    // MUTATOR
    public void updateDetailsFrom(User user)
    {
        validateUser(user);

        this.active = user.isActive();

        this.emailAddress = InternalEntityUtils.truncateValue(user.getEmailAddress());
        this.lowerEmailAddress = this.emailAddress == null ? null : this.emailAddress.toLowerCase(Locale.ENGLISH);

        this.firstName = InternalEntityUtils.truncateValue(user.getFirstName());
        this.lowerFirstName = this.firstName == null ? null : toLowerCase(this.firstName);

        this.lastName = InternalEntityUtils.truncateValue(user.getLastName());
        this.lowerLastName = this.lastName == null ? null : toLowerCase(this.lastName);

        this.displayName = InternalEntityUtils.truncateValue(user.getDisplayName());
        this.lowerDisplayName = this.displayName == null ? null : toLowerCase(this.displayName);
    }

    // MUTATOR
    public void renameTo(String newUsername)
    {
        Validate.isTrue(StringUtils.isNotBlank(newUsername), "newUsername cannot be null or blank");

        setName(newUsername);
    }

    // MUTATOR
    public void updateCredentialTo(PasswordCredential newCredential, int maxCredentialHistory)
    {
        validateCredential(newCredential);

        this.credential = newCredential;

        // update the credential records
        if (maxCredentialHistory > 0)
        {

            // Trim off the old history if it is too big
            if (getCredentialRecords().size() > maxCredentialHistory - 1)
            {
                for (Iterator<InternalUserCredentialRecord> iterator = getCredentialRecords().iterator(); iterator.hasNext();)
                {
                    iterator.next();
                    if (getCredentialRecords().size() > maxCredentialHistory - 1)
                    {
                        iterator.remove();
                    }
                }
            }

            InternalUserCredentialRecord record = new InternalUserCredentialRecord(this, newCredential.getCredential());
            getCredentialRecords().add(record);
        }
    }

    public void setName(final String name)
    {
        InternalEntityUtils.validateLength(name);
        this.name = name;
        this.lowerName = toLowerCase(name);
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getLowerEmailAddress()
    {
        return lowerEmailAddress;
    }

    public String getLowerFirstName()
    {
        return lowerFirstName;
    }

    public String getLowerLastName()
    {
        return lowerLastName;
    }

    public String getLowerDisplayName()
    {
        return lowerDisplayName;
    }

    public String getLowerName()
    {
        return lowerName;
    }

    public PasswordCredential getCredential()
    {
        return credential;
    }

    public List<InternalUserCredentialRecord> getCredentialRecords()
    {
        return credentialRecords;
    }

    public List<PasswordCredential> getCredentialHistory()
    {
        List<PasswordCredential> credentials = new ArrayList<PasswordCredential>();

        for (InternalUserCredentialRecord record : getCredentialRecords())
        {
            credentials.add(record.getCredential());
        }

        return credentials;
    }

    private void setEmailAddress(final String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    private void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    private void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    private void setDisplayName(final String displayName)
    {
        this.displayName = displayName;
    }

    private void setCredential(final PasswordCredential credential)
    {
        this.credential = credential;
    }

    private void setCredentialRecords(final List<InternalUserCredentialRecord> credentialRecords)
    {
        this.credentialRecords = credentialRecords;
    }

    private void setLowerEmailAddress(final String lowerEmailAddress)
    {
        this.lowerEmailAddress = lowerEmailAddress;
    }

    private void setLowerFirstName(final String lowerFirstName)
    {
        this.lowerFirstName = lowerFirstName;
    }

    private void setLowerLastName(final String lowerLastName)
    {
        this.lowerLastName = lowerLastName;
    }

    private void setLowerDisplayName(final String lowerDisplayName)
    {
        this.lowerDisplayName = lowerDisplayName;
    }

    private void setLowerName(final String lowerName)
    {
        this.lowerName = lowerName;
    }

    public boolean equals(Object o)
    {
        return UserComparator.equalsObject(this, o);
    }

    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public int compareTo(com.atlassian.crowd.embedded.api.User other)
    {
        return UserComparator.compareTo(this, other);
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("name", getName()).
                append("createdDate", getCreatedDate()).
                append("updatedDate", getUpdatedDate()).
                append("active", isActive()).
                append("emailAddress", getEmailAddress()).
                append("firstName", getFirstName()).
                append("lastName", getLastName()).
                append("displayName", getDisplayName()).
                append("credential", getCredential()).
                append("lowerName", getLowerName()).
                append("lowerEmailAddress", getLowerEmailAddress()).
                append("lowerFirstName", getLowerFirstName()).
                append("lowerLastName", getLowerLastName()).
                append("lowerDisplayName", getLowerDisplayName()).
                append("directoryId", getDirectoryId()).
                toString();
    }
}
