package com.atlassian.crowd.embedded.ofbiz.db;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

/**
 * Wrapper around GenericDelegator to add Generics, and wrap GenericEntityException in a RuntimeException (DataAccessException).
 */
public class OfBizHelper
{
    private DelegatorInterface genericDelegator;

    public OfBizHelper(final DelegatorInterface genericDelegator)
    {
        this.genericDelegator = checkNotNull(genericDelegator);        
    }

    public List<GenericValue> findAll(final String entityName)
    {
        try
        {
            //noinspection unchecked
            return genericDelegator.findAll(entityName);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findByAnd(String entityName, Map<String, Object> fields)
    {
        try
        {
            //noinspection unchecked
            return genericDelegator.findByAnd(entityName, fields);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findByAnd(final String entityName, final Map<String, Object> fields, final List<String> orderBy)
    {
        try
        {
            //noinspection unchecked
            return genericDelegator.findByAnd(entityName, fields, orderBy);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect, final List<String> orderBy)
    {
        try
        {
            //noinspection unchecked
            return genericDelegator.findByCondition(entityName, entityCondition, fieldsToSelect, orderBy);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public GenericValue makeValue(final String entityName, final Map<String, Object> fields)
    {
        return genericDelegator.makeValue(entityName, fields);
    }

    public void store(final GenericValue genericValue)
    {
        try
        {
            genericDelegator.store(genericValue);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void storeAll(final List<GenericValue> values)
    {
        try
        {
            genericDelegator.storeAll(values);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void removeValue(final GenericValue genericValue)
    {
        try
        {
            genericDelegator.removeValue(genericValue);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public int removeByAnd(final String entityName, final Map<String, Object> fields)
    {
        try
        {
            return genericDelegator.removeByAnd(entityName, fields);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public ModelEntity getModelEntity(final String entityName)
    {
        return genericDelegator.getModelEntity(entityName);
    }

    public String getEntityHelperName(final String entityName)
    {
        return genericDelegator.getEntityHelperName(entityName);
    }

     /**
     * Create a new entity.
     *
     * If there is no "id" in the parameter list, one is created using the entity sequence
     */
    public GenericValue createValue(final String entity, final Map<String, Object> paramMap)
    {
        final Map<String, Object> params = Maps.newHashMap(paramMap);

        try
        {
            if (params.get("id") == null)
            {
                final Long id = genericDelegator.getNextSeqId(entity);
                params.put("id", id);
            }

            final GenericValue v =genericDelegator.makeValue(entity, params);
            v.create();
            return v;
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    /**
     * Converts a java.sql.Timestamp to a java.util.Date.
     * @param timestamp The java.sql.Timestamp
     * @return the java.util.Date.
     */
    public static java.util.Date convertToUtilDate(final Timestamp timestamp)
    {
        if (timestamp == null)
        {
            return null;
        }
        return new java.util.Date(timestamp.getTime());
    }

    /**
     * Converts a java.util.Date to a java.sql.Timestamp.
     * @param date The java.util.Date
     * @return the java.sql.Timestamp.
     */
    public static Timestamp convertToSqlTimestamp(final java.util.Date date)
    {
        if (date == null)
        {
            return null;
        }
        return new Timestamp(date.getTime());
    }
}
