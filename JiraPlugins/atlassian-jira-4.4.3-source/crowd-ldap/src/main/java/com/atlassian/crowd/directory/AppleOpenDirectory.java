package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;

import javax.naming.Context;
import java.util.Map;

/**
 * Provides limited support for Apple's Open Directory. It's a customised version of OpenLDAP, using an RFC230-alike
 * schema.
 * <p/>
 * It has some quirks, and is not particularly well documented. We've been unable to determine exactly how to change
 * user passwords.
 */
public class AppleOpenDirectory extends Rfc2307
{
    public AppleOpenDirectory(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory, PasswordEncoderFactory passwordEncoderFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "Apple Open Directory (Read-Only)";
    }

    public String getDescriptiveName()
    {
        return AppleOpenDirectory.getStaticDirectoryType();
    }

    /**
     * Returns the properties used to set up the Ldap ContextSource. Overridden to make sure CRAM-MD5 is
     * used for authentication - for security reasons, Open Directory does not support Simple Authentication.
     *
     * @return
     */
    @Override
    protected Map<String, String> getBaseEnvironmentProperties()
    {
        Map<String, String> environment = super.getBaseEnvironmentProperties();
        environment.put(Context.SECURITY_AUTHENTICATION, "CRAM-MD5");

        return environment;
    }

// It appears that Spring-LDAP 1.3-RC1 obviates the need for this.        
//    /**
//     * Apple Open Directory doesn't like authentication using the DN, so we use the username (uid=)
//     * @param username
//     * @return
//     */
//    @Override
//    protected String getAuthenticationName(String username)
//    {
//        return username;
//    }

    /**
     * We don't support changing passwords in Open Directory, mainly because we can't figure out how.
     */
    @Override
    public void updateUserCredential(final String name, final PasswordCredential credential) throws UserNotFoundException, InvalidCredentialException
    {
        throw new UnsupportedOperationException("Password changes not supported in Open Directory (" + name + ")");
    }


}
