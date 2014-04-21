package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.DelegatedAuthenticationDirectory;
import com.atlassian.crowd.directory.InternalDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.event.api.EventPublisher;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class DelegatedAuthenticationDirectoryInstanceLoaderImpl extends CachingDirectoryInstanceLoader implements DelegatedAuthenticationDirectoryInstanceLoader
{
    private final DirectoryInstanceLoader ldapDirectoryInstanceLoader;
    private final DirectoryInstanceLoader internalDirectoryInstanceLoader;
    private final DirectoryDao directoryDao;

    public DelegatedAuthenticationDirectoryInstanceLoaderImpl(LDAPDirectoryInstanceLoader ldapDirectoryInstanceLoader, InternalDirectoryInstanceLoader internalDirectoryInstanceLoader, EventPublisher eventPublisher, DirectoryDao directoryDao)
    {
        super(eventPublisher);
        this.ldapDirectoryInstanceLoader = ldapDirectoryInstanceLoader;
        this.internalDirectoryInstanceLoader = internalDirectoryInstanceLoader;
        this.directoryDao = directoryDao;
    }

    @Override
    protected RemoteDirectory getNewDirectory(Directory directory) throws DirectoryInstantiationException
    {
        RemoteDirectory ldapDirectory = ldapDirectoryInstanceLoader.getDirectory(getLdapVersionOfDirectory(directory));
        RemoteDirectory internalDirectory = internalDirectoryInstanceLoader.getDirectory(getInternalVersionOfDirectory(directory));

        return new DelegatedAuthenticationDirectory(ldapDirectory, internalDirectory, getEventPublisher(), directoryDao);
    }

    private Directory getLdapVersionOfDirectory(Directory directory)
    {
        DirectoryImpl ldap = new DirectoryImpl(directory);

        String ldapClass = directory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_LDAP_DIRECTORY_CLASS);
        ldap.setImplementationClass(ldapClass);

        return ldap;
    }

    private Directory getInternalVersionOfDirectory(Directory directory)
    {
        DirectoryImpl internal = new DirectoryImpl(directory);

        internal.setImplementationClass(InternalDirectory.class.getCanonicalName());

        // internal directory needs a password encoder (even if it's just to store blank passwords)
        // attributes is probably immutable - build a new Map
        final Map<String, String> newAttributes = new HashMap<String, String>(internal.getAttributes());
        newAttributes.put(InternalDirectory.ATTRIBUTE_USER_ENCRYPTION_METHOD, PasswordEncoderFactory.SHA_ENCODER);
        internal.setAttributes(newAttributes);

        return internal;
    }

    public RemoteDirectory getRawDirectory(Long id, String className, Map<String, String> attributes) throws DirectoryInstantiationException
    {
        String ldapClass = attributes.get(DelegatedAuthenticationDirectory.ATTRIBUTE_LDAP_DIRECTORY_CLASS);

        RemoteDirectory ldapDirectory = ldapDirectoryInstanceLoader.getRawDirectory(id, ldapClass, attributes);
        RemoteDirectory internalDirectory = internalDirectoryInstanceLoader.getRawDirectory(id, InternalDirectory.class.getCanonicalName(), attributes);

        return new DelegatedAuthenticationDirectory(ldapDirectory, internalDirectory, getEventPublisher(), directoryDao);
    }

    public boolean canLoad(String className)
    {
        try
        {
            Class clazz = Class.forName(className);
            return DelegatedAuthenticationDirectory.class.isAssignableFrom(clazz);
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }
}
