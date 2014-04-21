package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.ofbiz.db.DataAccessException;
import com.atlassian.crowd.embedded.ofbiz.db.OfBizHelper;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.caseInsensitive;
import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.of;

public class OfBizApplicationDao implements ApplicationDAO
{
    private final OfBizHelper ofBiz;
    private final OfBizDirectoryDao directoryDao;

    // Must be volatile to ensure visibility to all threads.
    private volatile List<Application> applicationCache = null;

    public OfBizApplicationDao(final DelegatorInterface delegator, final OfBizDirectoryDao directoryDao)
    {
        this.ofBiz = new OfBizHelper(delegator);
        this.directoryDao = directoryDao;
        buildCache();
    }

    private Application buildApplication(final GenericValue applicationGenericValue)
    {
        final List<GenericValue> remoteAddressesGenericValue = findRemoteAddresses(applicationGenericValue.getLong(ApplicationEntity.APPLICATION_ID));
        OfBizApplication ofBizApplication = OfBizApplication.from(applicationGenericValue, remoteAddressesGenericValue);
        ofBizApplication.setDirectoryDao(directoryDao);
        return ofBizApplication;
    }

    public void updateCredential(final Application application, final PasswordCredential passwordCredential)
            throws ApplicationNotFoundException
    {
        Validate.notNull(application);
        Validate.notNull(passwordCredential);
        if (passwordCredential.getCredential() != null)
        {
            Validate.isTrue(passwordCredential.isEncryptedCredential(), "credential must be encrypted");
        }
        final GenericValue gv = findApplicationById(application.getId());
        gv.set(ApplicationEntity.CREDENTIAL, passwordCredential.getCredential());
        gv.set(ApplicationEntity.UPDATED_DATE, new Timestamp(System.currentTimeMillis()));
        ofBiz.store(gv);
        // Rebuild the full cache (this would be very rare)
        buildCache();
    }

    public void addRemoteAddress(final long applicationId, final RemoteAddress remoteAddress)
    {
        final GenericValue gv = ofBiz.makeValue(RemoteAddressEntity.ENTITY, RemoteAddressEntity.getData(applicationId,
                    remoteAddress.getAddress(), remoteAddress.getEncodedAddressBytes(), remoteAddress.getMask()));
        try
        {
            gv.create();
            ofBiz.store(gv);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        // Rebuild the full cache (this would be very rare)
        buildCache();
    }

    public void removeRemoteAddress(final long applicationId, final RemoteAddress remoteAddress)
    {
        ofBiz.removeByAnd(RemoteAddressEntity.ENTITY, of(RemoteAddressEntity.APPLICATION_ID, applicationId, RemoteAddressEntity.ADDRESS, remoteAddress.getAddress()));
        // Rebuild the full cache (this would be very rare)
        buildCache();
    }

    public void updateDirectoryMapping(long applicationId, long directoryId, int position)
            throws DirectoryNotFoundException
    {
        // We delegate this straight to the directory dao, as we don't support multiple applications.
        directoryDao.updateDirectoryPosition(directoryId, position);
    }

    public Application findById(final long id) throws ApplicationNotFoundException
    {
        for (Application application : applicationCache)
        {
            if (application.getId().longValue() == id)
            {
                return application;
            }
        }
        throw new ApplicationNotFoundException(id);
    }

    public Application findByName(final String name) throws ApplicationNotFoundException
    {
        for (Application application : applicationCache)
        {
            if (application.getName().equals(name))
            {
                return application;
            }
        }
        throw new ApplicationNotFoundException(name);
    }

    public Application add(final Application application, PasswordCredential credential)
    {
        final long applicationId;
        try
        {
            if (credential != null)
            {
                Validate.isTrue(credential.isEncryptedCredential(), "credential must be encrypted");
            }
            ApplicationImpl applicationToSave = ApplicationImpl.newInstance(application);
            applicationToSave.setCredential(credential);
            applicationToSave.setCreatedDateToNow();
            applicationToSave.setUpdatedDateToNow();

            final Map<String, Object> map = ApplicationEntity.getData(applicationToSave);
            // Create and store the application
            ofBiz.createValue(ApplicationEntity.ENTITY, map);

            // Retrieve the id from the newly created and stored object
            final GenericValue applicationGenericValue = EntityUtil.getOnly(findApplications(caseInsensitive(ApplicationEntity.LOWER_NAME, application.getName())));
            applicationId = applicationGenericValue.getLong(ApplicationEntity.APPLICATION_ID);

            for (final RemoteAddress remoteAddress : application.getRemoteAddresses())
            {
                final GenericValue genericValue = ofBiz.makeValue(RemoteAddressEntity.ENTITY, RemoteAddressEntity.getData(applicationId,
                    remoteAddress.getAddress(), remoteAddress.getEncodedAddressBytes(), remoteAddress.getMask()));
                genericValue.create();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        flushCache();

        // Find the saved copy of the Application
        try
        {
            return findById(applicationId);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private GenericValue findApplicationById(final Long id) throws ApplicationNotFoundException
    {
        try
        {
            final GenericValue applicationGenericValue = EntityUtil.getOnly(findApplications(of(ApplicationEntity.APPLICATION_ID, id)));
            if (applicationGenericValue != null)
            {
                return applicationGenericValue;
            }
            else
            {
                throw new ApplicationNotFoundException(id);
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findApplications(final Map<String, Object> filter) throws GenericEntityException
    {
        return ofBiz.findByAnd(ApplicationEntity.ENTITY, filter);
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findRemoteAddresses(final long applicationId)
    {
        return ofBiz.findByAnd(RemoteAddressEntity.ENTITY, of(RemoteAddressEntity.APPLICATION_ID, applicationId));
    }

    private List<Application> getAllApplications()
    {
        final ImmutableList.Builder<Application> applications = ImmutableList.builder();
        @SuppressWarnings("unchecked")
        final List<GenericValue> applicationGenericValues = ofBiz.findByAnd(ApplicationEntity.ENTITY, Collections.EMPTY_MAP);

        for (final GenericValue applicationGenericValue : applicationGenericValues)
        {
            applications.add(buildApplication(applicationGenericValue));
        }
        return applications.build();
    }

    /**
     * Invoked by {@link OfBizCacheFlushingManager} to ensure caches are being flushed in the right order on
     * {@link XMLRestoreFinishedEvent}
     */
    public synchronized void flushCache()
    {
        buildCache();
    }

    private void buildCache()
    {
        applicationCache = Collections.unmodifiableList(getAllApplications());
    }

    public Application add(Application application)
    {
        return add(application, null);
    }

    public Application update(Application application) throws ApplicationNotFoundException
    {
        Validate.notNull(application);
        final GenericValue gv = findApplicationById(application.getId());
        ApplicationEntity.setData(application, gv);
        // Create and store the application
        ofBiz.store(gv);
        // Remove all aold addresses and update with the new list
        ofBiz.removeByAnd(RemoteAddressEntity.ENTITY, of(RemoteAddressEntity.APPLICATION_ID, application.getId()));
        try
        {
            for (final RemoteAddress remoteAddress : application.getRemoteAddresses())
            {
                final GenericValue genericValue = ofBiz.makeValue(RemoteAddressEntity.ENTITY, RemoteAddressEntity.getData(application.getId(),
                    remoteAddress.getAddress(), remoteAddress.getEncodedAddressBytes(), remoteAddress.getMask()));
                genericValue.create();
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        // Rebuild the full cache (this would be very rare)
        buildCache();
        return application;
    }

    public void remove(Application application)
    {
        // Remove any addresses first then the application itself
        ofBiz.removeByAnd(RemoteAddressEntity.ENTITY, of(RemoteAddressEntity.APPLICATION_ID, application.getId()));
        ofBiz.removeByAnd(ApplicationEntity.ENTITY, of(ApplicationEntity.APPLICATION_ID, application.getId()));
        // Rebuild the full cache (this would be very rare)
        buildCache();
    }

    public List<Application> search(EntityQuery<Application> entityQuery)
    {
        return applicationCache;
    }

    public void addDirectoryMapping(long applicationId, long directoryId, boolean allowAllToAuthenticate, OperationType... operationTypes)
    {
        throw new UnsupportedOperationException("Not Supported by the Crowd Embedded API");
    }

    public void removeDirectoryMapping(long applicationId, long directoryId) throws ApplicationNotFoundException
    {
        // Nothing to do.  This gets called unconditionally, so we just ignore it.
    }

    public void removeDirectoryMappings(long directoryId)
    {
        // Nothing to do.  This gets called unconditionally, so we just ignore it.
    }

    public void addGroupMapping(long applicationId, long directoryId, String groupName)
    {
        throw new UnsupportedOperationException("Not Supported by the Crowd Embedded API");
    }

    public void removeGroupMapping(long applicationId, long directoryId, String groupName)
    {
        // Nothing to do.  This gets called unconditionally, so we just ignore it.
    }

    public void removeGroupMappings(long directoryId, String groupName)
    {
        // Nothing to do.  This gets called unconditionally, so we just ignore it.
    }

    public void renameGroupMappings(long directoryId, String oldGroupName, String newGroupName)
    {
        throw new UnsupportedOperationException("Not Supported by the Crowd Embedded API");
    }

    public List<Application> findAuthorisedApplications(long directoryId, List<String> groupNames)
    {
        throw new UnsupportedOperationException("Not Supported by the Crowd Embedded API");
    }

    public void updateDirectoryMapping(long applicationId, long directoryId, boolean allowAllToAuthenticate)
    {
        throw new UnsupportedOperationException("Not Supported by the Crowd Embedded API");    }

    public void updateDirectoryMapping(long applicationId, long directoryId, boolean allowAllToAuthenticate, Set<OperationType> operationTypes)
    {
        throw new UnsupportedOperationException("Not Supported by the Crowd Embedded API");
    }

    
}
