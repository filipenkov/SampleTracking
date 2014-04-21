package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.ofbiz.db.DataAccessException;
import com.atlassian.crowd.embedded.ofbiz.db.OfBizHelper;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.NullRestriction;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.google.common.collect.ImmutableList;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.crowd.embedded.ofbiz.DirectoryEntity.getData;
import static com.atlassian.crowd.embedded.ofbiz.OfBizDirectory.from;
import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.caseInsensitive;
import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.of;

public class OfBizDirectoryDao implements DirectoryDao
{
    private final OfBizHelper ofBiz;

    // Must be volatile to ensure visibility to all threads.
    private volatile List<DirectoryImpl> directoryCache = null;

    public OfBizDirectoryDao(final DelegatorInterface delegator)
    {
        this.ofBiz = new OfBizHelper(delegator);
        buildCache();
    }

    public DirectoryImpl findById(final long id) throws DirectoryNotFoundException
    {
        for (DirectoryImpl directory : directoryCache)
        {
            if (directory.getId().longValue() == id)
            {
                return directory;
            }
        }
        throw new DirectoryNotFoundException(id);
    }

    public DirectoryImpl findByName(final String name) throws DirectoryNotFoundException
    {
        for (DirectoryImpl directory : directoryCache)
        {
            if (directory.getName().equalsIgnoreCase(name))
            {
                return directory;
            }
        }
        throw new DirectoryNotFoundException(name);
    }

    public List findAll()
    {
        return directoryCache;
    }

    private DirectoryImpl buildDirectory(final GenericValue directoryGenericValue)
    {
        final List<GenericValue> attributesGenericValues = findAttributesGenericValues(directoryGenericValue.getLong(DirectoryEntity.DIRECTORY_ID));
        final List<GenericValue> operationGenericValues = findOperations(directoryGenericValue.getLong(DirectoryEntity.DIRECTORY_ID));
        return from(directoryGenericValue, attributesGenericValues, operationGenericValues);
    }

    private GenericValue findDirectoryByName(final String name) throws DirectoryNotFoundException
    {
        try
        {
            final GenericValue directoryGenericValue = EntityUtil.getOnly(findDirectories(caseInsensitive(DirectoryEntity.LOWER_NAME, name)));
            if (directoryGenericValue != null)
            {
                return directoryGenericValue;
            }
            else
            {
                throw new DirectoryNotFoundException(name);
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DirectoryNotFoundException(name, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findAttributesGenericValues(final long directoryId)
    {
        return ofBiz.findByAnd(DirectoryAttributeEntity.ENTITY, of(DirectoryAttributeEntity.DIRECTORY_ID, directoryId));
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findOperations(final long directoryId)
    {
        return ofBiz.findByAnd(DirectoryOperationEntity.ENTITY, of(DirectoryAttributeEntity.DIRECTORY_ID, directoryId));
    }

    private GenericValue findDirectoryById(final Long id) throws DirectoryNotFoundException
    {
        try
        {
            final GenericValue directoryGenericValue = EntityUtil.getOnly(findDirectories(of(DirectoryEntity.DIRECTORY_ID, id)));
            if (directoryGenericValue != null)
            {
                return directoryGenericValue;
            }
            else
            {
                throw new DirectoryNotFoundException(id);
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findDirectories(final Map<String, Object> filter) throws GenericEntityException
    {
        return ofBiz.findByAnd(DirectoryEntity.ENTITY, filter);
    }

    public synchronized DirectoryImpl add(final Directory directory)
    {
        final long directoryId;
        try
        {
            DirectoryImpl directoryToSave = new DirectoryImpl(directory);
            directoryToSave.setCreatedDateToNow();
            directoryToSave.setUpdatedDateToNow();

            final Map<String, Object> map = getData(directoryToSave);

            // Create and store the directory
            final GenericValue directoryGenericValue = ofBiz.createValue(DirectoryEntity.ENTITY, map);

            directoryId = directoryGenericValue.getLong(DirectoryEntity.DIRECTORY_ID);

            for (final Map.Entry<String, String> entry : directory.getAttributes().entrySet())
            {
                final GenericValue genericValue = ofBiz.makeValue(DirectoryAttributeEntity.ENTITY, DirectoryAttributeEntity.getData(directoryId,
                    entry.getKey(), entry.getValue()));
                genericValue.create();
            }

            for (final OperationType operationType : directory.getAllowedOperations())
            {
                final GenericValue genericValue = ofBiz.makeValue(DirectoryOperationEntity.ENTITY, DirectoryOperationEntity.getData(directoryId,
                    operationType));
                genericValue.create();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // Rebuild the full cache, we need this now, so the resequeence works properly.
        flushCache();

        // Force the directory to the last position
        try
        {
            updateDirectoryPosition(directoryId, directoryCache.size());
        }
        catch (DirectoryNotFoundException e)
        {
            // Very unlikely unless a very unlucky race condition.
            throw new OperationFailedException(e);
        }

        // Find the saved copy of the Directory
        try
        {
            return findById(directoryId);
        }
        catch (DirectoryNotFoundException e)
        {
            // Very unlikely unless a very unlucky race condition.
            throw new OperationFailedException(e);
        }
    }

    public synchronized Directory update(final Directory directory) throws DirectoryNotFoundException
    {
        final GenericValue gv = DirectoryEntity.setData(directory, findDirectoryById(directory.getId()));
        gv.set(DirectoryEntity.UPDATED_DATE, new Timestamp(System.currentTimeMillis()));
        storeDirectory(gv);
        storeAttributes(directory);
        storeOperations(directory);
        // Rebuild the full cache (this would be very rare)
        buildCache();
        // Find the latest copy of the Directory
        return findById(directory.getId());
    }

    private void storeAttributes(final Directory directory)
    {
        final List<GenericValue> attributeGenericValues = new ArrayList<GenericValue>();
        for (final Map.Entry<String, String> entry : directory.getAttributes().entrySet())
        {
            attributeGenericValues.add(ofBiz.makeValue(DirectoryAttributeEntity.ENTITY, DirectoryAttributeEntity.getData(directory.getId(),
                entry.getKey(), entry.getValue())));
        }
        ofBiz.removeByAnd(DirectoryAttributeEntity.ENTITY, of(DirectoryAttributeEntity.DIRECTORY_ID, directory.getId()));
        ofBiz.storeAll(attributeGenericValues);
    }

    private void storeOperations(final Directory directory)
    {
        final List<GenericValue> operationGenericValues = new ArrayList<GenericValue>();
        for (final OperationType operationType : directory.getAllowedOperations())
        {
            operationGenericValues.add(ofBiz.makeValue(DirectoryOperationEntity.ENTITY, DirectoryOperationEntity.getData(directory.getId(),
                operationType)));
        }

        ofBiz.removeByAnd(DirectoryOperationEntity.ENTITY, of(DirectoryOperationEntity.DIRECTORY_ID, directory.getId()));
        ofBiz.storeAll(operationGenericValues);
    }

    private GenericValue storeDirectory(final GenericValue directoryGenericValue)
    {
        ofBiz.store(directoryGenericValue);
        return directoryGenericValue;
    }

    public synchronized void remove(final Directory directory) throws DirectoryNotFoundException
    {
        // TODO: Should we remove Users and Groups as well?

        final GenericValue directoryGenericValue = findDirectoryById(directory.getId());
        ofBiz.removeByAnd(DirectoryAttributeEntity.ENTITY, of(DirectoryAttributeEntity.DIRECTORY_ID, directoryGenericValue.getLong(DirectoryEntity.DIRECTORY_ID)));
        ofBiz.removeByAnd(DirectoryOperationEntity.ENTITY, of(DirectoryOperationEntity.DIRECTORY_ID, directoryGenericValue.getLong(DirectoryEntity.DIRECTORY_ID)));
        ofBiz.removeByAnd(DirectoryEntity.ENTITY, of(DirectoryEntity.DIRECTORY_ID, directory.getId()));
        // rebuild the full cache
        buildCache();
    }

    public List search(final EntityQuery<Directory> query)
    {
        // JIRA does not support anything but an empty Query
        if (query == null || query.getSearchRestriction() == null || query.getSearchRestriction() instanceof NullRestriction)
        {
            return directoryCache;
        }
        if (query.getSearchRestriction() instanceof TermRestriction)
        {
            final TermRestriction termRestriction = (TermRestriction) query.getSearchRestriction();
            final Property property = termRestriction.getProperty();
            System.out.println("property = " + property);

            if (!property.getPropertyName().equals("name"))
            {
                throw new UnsupportedOperationException("Searching on '" + property.getPropertyName() + "' not supported.");
            }
            final MatchMode matchMode = termRestriction.getMatchMode();
            switch (matchMode)
            {
                case EXACTLY_MATCHES:
                    return searchByName((String) termRestriction.getValue());
                default:
                    throw new UnsupportedOperationException("Unsupported MatchMode " + matchMode);
            }
        }
        throw new UnsupportedOperationException("Complex Directory searching is not supported.");
    }

    private List<Directory> searchByName(final String value)
    {
        final List<Directory> results = new ArrayList<Directory>(1);
        for (Directory directory : directoryCache)
        {
            if (directory.getName().equals(value))
            {
                results.add(directory);
            }
        }
        return results;
    }

    private List<DirectoryImpl> getAllDirectories()
    {
        final ImmutableList.Builder<DirectoryImpl> directories = ImmutableList.builder();
        @SuppressWarnings("unchecked")
        final List<GenericValue> directoryGenericValues = ofBiz.findByAnd(DirectoryEntity.ENTITY, Collections.EMPTY_MAP, Collections.singletonList("position"));

        for (final GenericValue directoryGenericValue : directoryGenericValues)
        {
            directories.add(buildDirectory(directoryGenericValue));
        }
        return directories.build();
    }

    /**
     * Invoked by {@link OfBizCacheFlushingManager} to ensure caches are being flushed in the right order on
     * {@link com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent}
     */
    public synchronized void flushCache()
    {
        buildCache();
    }

    private void buildCache()
    {
        directoryCache = Collections.unmodifiableList(getAllDirectories());
    }

    public void updateDirectoryPosition(long directoryId, int position) throws DirectoryNotFoundException
    {
        List<OfBizDirectory> directories = new ArrayList<OfBizDirectory>(findAll());
        int currentPos = getCurrentDirectoryPosition(directories, directoryId);
        if (currentPos == -1)
        {
            throw new IllegalArgumentException("Directory to set position of does not exist");
        }
        if (position != currentPos)
        {
            OfBizDirectory directory = directories.remove(currentPos);
            if (position < 0)
            {
                position = 0;
            }
            else if (position > directories.size())
            {
                position = directories.size();
            }
            directories.add(position, directory);
            resequenceDirectories(directories);
        }
        else
        {
            // Position = current position, nothing to do
        }
        flushCache();
    }

    /**
     * Get the current position of the directory in the list of directories.
     * @param directories List of all directories
     * @param directoryId Directory to find in the list
     *
     * @return position in the directory list, zero based.
     */
    private int getCurrentDirectoryPosition(List<OfBizDirectory> directories, long directoryId)
    {
        for (int i = 0; i < directories.size(); i++)
        {
            if (directories.get(i).getId().equals(directoryId))
            {
                return i;
            }
        }
        return -1;
    }

    private void resequenceDirectories(List<OfBizDirectory> directories) throws DirectoryNotFoundException
    {
        long i = 0;
        for (OfBizDirectory directory : directories)
        {
            storeDirectoryPosition(directory.getId(), i);
            i++;
        }
    }

    private void storeDirectoryPosition(final Long id, final long position) throws DirectoryNotFoundException
    {
        final GenericValue gv = findDirectoryById(id);
        gv.set("position", position);
        storeDirectory(gv);
    }


}
