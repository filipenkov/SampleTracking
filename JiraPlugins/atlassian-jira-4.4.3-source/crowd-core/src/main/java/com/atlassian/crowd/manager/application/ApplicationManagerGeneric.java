package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.event.application.ApplicationDirectoryAddedEvent;
import com.atlassian.crowd.event.application.ApplicationDirectoryOrderUpdatedEvent;
import com.atlassian.crowd.event.application.ApplicationDirectoryRemovedEvent;
import com.atlassian.crowd.event.application.ApplicationRemoteAddressAddedEvent;
import com.atlassian.crowd.event.application.ApplicationRemoteAddressRemovedEvent;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.password.encoder.PasswordEncoder;
import com.atlassian.crowd.password.encoder.UpgradeablePasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.event.api.EventPublisher;
import org.apache.commons.lang.Validate;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.Validate.notNull;

public class ApplicationManagerGeneric implements ApplicationManager
{
    private final ApplicationDAO applicationDao;
    private final PasswordEncoderFactory passwordEncoderFactory;
    private final EventPublisher eventPublisher;

    public ApplicationManagerGeneric(ApplicationDAO applicationDao, PasswordEncoderFactory passwordEncoderFactory, EventPublisher eventPublisher)
    {
        this.applicationDao = applicationDao;
        this.passwordEncoderFactory = passwordEncoderFactory;
        this.eventPublisher = eventPublisher;
    }

    public Application add(Application application) throws InvalidCredentialException
    {
        Validate.notNull(application, "application should not be null");
        if (application.getCredential() == null)
        {
            throw new InvalidCredentialException("Password of the application cannot be null");
        }
        PasswordCredential encryptedCredential = encryptAndUpdateApplicationCredential(application.getCredential());

        return applicationDao.add(application, encryptedCredential);
    }

    public Application findById(long id) throws ApplicationNotFoundException
    {
        return applicationDao.findById(id);
    }

    public Application findByName(String name) throws ApplicationNotFoundException
    {
        return applicationDao.findByName(name);
    }

    public void remove(Application application) throws ApplicationManagerException
    {
        if (application.isPermanent())
        {
            throw new ApplicationManagerException("Cannot delete a permanent application");
        }

        // remove the application
        applicationDao.remove(application);
    }

    public void removeDirectoryFromApplication(Directory directory, Application application) throws ApplicationManagerException
    {
        DirectoryMapping mapping = application.getDirectoryMapping(directory.getId());
        if (mapping != null)
        {
            try
            {
                applicationDao.removeDirectoryMapping(application.getId(), directory.getId());

                eventPublisher.publish(new ApplicationDirectoryRemovedEvent(application, directory));
            }
            catch (ApplicationNotFoundException e)
            {
                // do nothing since we wanted to delete the directory anyway
            }
        }
    }

    public List<Application> search(EntityQuery query)
    {
        return applicationDao.search(query);
    }

    public List<Application> findAll()
    {
        return search(QueryBuilder.queryFor(Application.class, EntityDescriptor.application()).returningAtMost(EntityQuery.ALL_RESULTS));
    }

    public Application update(Application application) throws ApplicationManagerException, ApplicationNotFoundException
    {
        // cannot deactivate crowd
        if (application.getType() == ApplicationType.CROWD && !application.isActive())
        {
            throw new ApplicationManagerException("Cannot deactivate the Crowd application");
        }

        // cannot rename permanent applications
        if (application.isPermanent())
        {
            try
            {
                Application savedApp = findById(application.getId());
                if (!savedApp.getName().equals(application.getName()))
                {
                    throw new ApplicationManagerException("Cannot rename a permanent application");
                }
            }
            catch (ApplicationNotFoundException e)
            {
                throw new ApplicationManagerException(e.getMessage(), e);
            }
        }
        // Application names cannot be changed to the name of a current Crowd Application.
        Application currentApplication;
        try
        {
            currentApplication = findByName(application.getName());
        }
        catch (ApplicationNotFoundException e)
        {
            // We are changing the name of the application to one that doesn't exist, this is OK.
            // Just get the current application by Id, so we can make sure we are actually trying to update the correct application.
            currentApplication = findById(application.getId());
        }

        // If we are updating an application, make sure that it is the passed in Application
        if (application.getId().equals(currentApplication.getId()))
        {
            return applicationDao.update(application);
        }

        throw new ApplicationManagerException("You potentially tried to update an application with a different ID than the one you passed in");
    }

    public void updateCredential(Application application, PasswordCredential passwordCredential)
            throws ApplicationManagerException, ApplicationNotFoundException
    {
        notNull(application);
        notNull(passwordCredential);
        notNull(passwordCredential.getCredential());

        PasswordCredential encryptedCredential = encryptAndUpdateApplicationCredential(passwordCredential);

        applicationDao.updateCredential(application, encryptedCredential);
    }

    public boolean authenticate(Application application, PasswordCredential testCredential)
            throws ApplicationNotFoundException
    {
        notNull(application);
        notNull(testCredential);
        notNull(testCredential.getCredential());

        final PasswordEncoder encoder = getAtlassianSecurityEncoder();

        if (!encoder.isPasswordValid(application.getCredential().getCredential(), testCredential.getCredential(), null))
        {
            return false;
        }

        upgradePasswordIfRequired(application, encoder, testCredential.getCredential());

        return true;
    }

    private void upgradePasswordIfRequired(Application application, PasswordEncoder encoder, String rawPass)
            throws ApplicationNotFoundException
    {
        // When using UpgradeablePasswordEncoder, we might be asked to re-encode the password.
        if (encoder instanceof UpgradeablePasswordEncoder)
        {
            final UpgradeablePasswordEncoder upgradeableEncoder = (UpgradeablePasswordEncoder) encoder;
            if (upgradeableEncoder.isUpgradeRequired(application.getCredential().getCredential()))
            {
                final String newEncPass = encoder.encodePassword(rawPass, null);
                applicationDao.updateCredential(application, new PasswordCredential(newEncPass, true));
            }
        }
    }

    public void addDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate, OperationType... operationTypes)
            throws ApplicationNotFoundException, DirectoryNotFoundException
    {
        notNull(application);
        notNull(application.getId());
        notNull(directory);
        notNull(directory.getId());

        applicationDao.addDirectoryMapping(application.getId(), directory.getId(), allowAllToAuthenticate, operationTypes);

        eventPublisher.publish(new ApplicationDirectoryAddedEvent(application, directory));
    }

    public void updateDirectoryMapping(Application application, Directory directory, int position)
            throws ApplicationNotFoundException, DirectoryNotFoundException
    {
        notNull(application);
        notNull(application.getId());
        notNull(directory);
        notNull(directory.getId());

        applicationDao.updateDirectoryMapping(application.getId(), directory.getId(), position);

        eventPublisher.publish(new ApplicationDirectoryOrderUpdatedEvent(application, directory));
    }

    public void updateDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate)
            throws ApplicationNotFoundException, DirectoryNotFoundException
    {
        notNull(application);
        notNull(application.getId());
        notNull(directory);
        notNull(directory.getId());

        applicationDao.updateDirectoryMapping(application.getId(), directory.getId(), allowAllToAuthenticate);
    }

    public void updateDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate, Set<OperationType> operationTypes)
            throws ApplicationNotFoundException, DirectoryNotFoundException
    {
        notNull(application);
        notNull(application.getId());
        notNull(directory);
        notNull(directory.getId());

        applicationDao.updateDirectoryMapping(application.getId(), directory.getId(), allowAllToAuthenticate, operationTypes);
    }

    public void addRemoteAddress(Application application, RemoteAddress remoteAddress) throws ApplicationNotFoundException
    {
        notNull(application);
        notNull(application.getId());

        applicationDao.addRemoteAddress(application.getId(), remoteAddress);
        eventPublisher.publish(new ApplicationRemoteAddressAddedEvent(application, remoteAddress));
    }

    public void removeRemoteAddress(Application application, RemoteAddress remoteAddress) throws ApplicationNotFoundException
    {
        notNull(application);
        notNull(application.getId());
        notNull(remoteAddress);
        applicationDao.removeRemoteAddress(application.getId(), remoteAddress);
        eventPublisher.publish(new ApplicationRemoteAddressRemovedEvent(application, remoteAddress));
    }

    public void addGroupMapping(Application application, Directory directory, String groupName)
            throws ApplicationNotFoundException
    {
        notNull(application);
        notNull(application.getId());
        notNull(directory);
        notNull(directory.getId());

        applicationDao.addGroupMapping(application.getId(), directory.getId(), groupName);
    }

    public void removeGroupMapping(Application application, Directory directory, String groupName)
    {
        notNull(application);
        notNull(application.getId());
        notNull(directory);
        notNull(directory.getId());

        applicationDao.removeGroupMapping(application.getId(), directory.getId(), groupName);
    }

    private PasswordCredential encryptAndUpdateApplicationCredential(PasswordCredential passwordCredential)
    {
        PasswordEncoder encoder = getAtlassianSecurityEncoder();

        String encryptedPassword = encoder.encodePassword(passwordCredential.getCredential(), null);
        passwordCredential.setCredential(encryptedPassword);
        passwordCredential.setEncryptedCredential(true);

        return passwordCredential;
    }

    private PasswordEncoder getAtlassianSecurityEncoder()
    {
        return passwordEncoderFactory.getEncoder(PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER);
    }
}
