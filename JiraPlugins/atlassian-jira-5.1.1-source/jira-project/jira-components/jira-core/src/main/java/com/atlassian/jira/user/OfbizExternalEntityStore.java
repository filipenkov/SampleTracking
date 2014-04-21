package com.atlassian.jira.user;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.util.concurrent.ConcurrentOperationMap;
import com.atlassian.util.concurrent.ConcurrentOperationMapImpl;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * This exists to create entities in the ExternalEntity table which is used to generate system id's for
 * usernames that exist in external systems.
 */
public class OfbizExternalEntityStore implements ExternalEntityStore
{
    /**
     * Name of the entity referenced in the entitymodel.xml
     */
    public static final String ENTITY_NAME_EXTERNAL_ENTITY = "ExternalEntity";

    /**
     * Set with the name of this class
     */
    private static final String ENTITY_TYPE = OfbizExternalEntityStore.class.getName();

    private final GenericDelegator genericDelegator;
    private final ConcurrentOperationMap<String, Long> concurrentOperationMap = new ConcurrentOperationMapImpl<String, Long>();

    /**
     * Creates a new instance of this class, initializing it with generic delegator that is used for user creation
     *
     * @param genericDelegator generic delegator that will be responsible for finding and creating user records in external_entities table
     */
    public OfbizExternalEntityStore(final GenericDelegator genericDelegator)
    {
        this.genericDelegator = genericDelegator;
    }

    /**
     * Checks the user with given name exists using the generic delegator and returns the user ID.
     * If it does not exist it creates it and returns the user ID.
     *
     * @param name profile name to create
     * @return Long id, this is the created or existing id for the name.
     * @throws IllegalArgumentException if the given name is null
     * @throws DataAccessException      if data access error occurs or more than one user with given name is found
     */
    public Long createIfDoesNotExist(final String name) throws IllegalArgumentException, DataAccessException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("ExternalEntity user name must not be null.");
        }

        // We want to wrap the operation we are going to perform in a Callable so that we can submit it
        // for execution such that only one execution will occur concurrently for a single name.
        final Callable<Long> operation = createCallableOperation(name);

        try
        {
            return concurrentOperationMap.runOperation(name, operation);
        }
        catch (final ExecutionException e)
        {
            throw new DataAccessException(e);
        }
    }

    private Callable<Long> createCallableOperation(final String name)
    {
        return new Callable<Long>()
        {
            public Long call()
            {
                final Map<String, Object> externalEntityParameters = new PrimitiveMap.Builder()
                        .add("name", IdentifierUtils.toLowerCase(name)).add("type", ENTITY_TYPE).toMap();
                try
                {
                    final Long idForName;
                    final List<GenericValue> entitiesByName = genericDelegator.findByAnd(ENTITY_NAME_EXTERNAL_ENTITY, externalEntityParameters);
                    if (entitiesByName.isEmpty())
                    {
                        final GenericValue entityGV = EntityUtils.createValue(ENTITY_NAME_EXTERNAL_ENTITY, externalEntityParameters);
                        idForName = entityGV.getLong("id");
                    }
                    else if (entitiesByName.size() == 1)
                    {
                        idForName = entitiesByName.get(0).getLong("id");
                    }
                    else
                    {
                        // JRA-25834 may be a case insensitive database, therefore returning more than 1 does not necessarily imply a problem
                        // see if we can get a unique name in this situation before throwing the exception
                        List<GenericValue> filteredGvs = Lists.newArrayList(Iterables.filter(entitiesByName, new Predicate<GenericValue>()
                        {
                            @Override
                            public boolean apply(@Nullable GenericValue input)
                            {
                                return name.equals(input.getString("name"));
                            }
                        }));
                        if (filteredGvs.size() == 1)
                        {
                             idForName = filteredGvs.get(0).getLong("id");
                        }
                        else
                        {
                            throw new DataAccessException("There is more than one row in the ExternalEntity table for name: " + name);
                        }
                    }
                    return idForName;
                }
                catch (final GenericEntityException e)
                {
                    throw new DataAccessException(e);
                }
            }
        };
    }
}
