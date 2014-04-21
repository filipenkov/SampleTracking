package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import org.apache.commons.lang.Validate;

import java.util.*;

/**
 * Predominantly used for the 'bulk' adding of users to Crowd.
 */
public class UserTemplateWithCredentialAndAttributes extends UserTemplateWithAttributes
{
    private final PasswordCredential credential;
    private final List<PasswordCredential> credentialHistory = new ArrayList<PasswordCredential>();
    private Date createdDate = null;
    private Date updatedDate = null;

    public UserTemplateWithCredentialAndAttributes(String username, long directoryId, PasswordCredential credential)
    {
        super(username, directoryId);
        Validate.notNull(credential, "argument credential cannot be null");
        this.credential = credential;
    }

    public UserTemplateWithCredentialAndAttributes(User user, PasswordCredential credential)
    {
        super(user);
        Validate.notNull(credential, "argument credential cannot be null");
        this.credential = credential;
    }

    public UserTemplateWithCredentialAndAttributes(User user, Map<String, Set<String>> attributes, PasswordCredential credential)
    {
        this(user, credential);

        if (attributes != null)
        {
            for (Map.Entry<String, Set<String>> attributeEntry : attributes.entrySet())
            {
                setAttribute(attributeEntry.getKey(), attributeEntry.getValue());
            }
        }
    }

    public PasswordCredential getCredential()
    {
        return credential;
    }

    public List<PasswordCredential> getCredentialHistory()
    {
        return credentialHistory;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate)
    {
        this.updatedDate = updatedDate;
    }
}
