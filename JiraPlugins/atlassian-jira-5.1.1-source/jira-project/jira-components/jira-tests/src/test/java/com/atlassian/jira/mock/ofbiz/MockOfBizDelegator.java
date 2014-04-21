package com.atlassian.jira.mock.ofbiz;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;
import org.ofbiz.core.entity.model.ModelReader;

/**
 * Provides a mock delegator with an in-memory database.
 * <p/>
 * The constructor accepts two lists. The genericValues list can be used to pre-populate the in-memory database with
 * GenericValue objects required for the test. It represents the initial state of the database.
 * <p/>
 * The expectedGenericValues list represents the final state of the database. It can be used to verify the final
 * contents of the in-memory database upon test completion.
 * <p/>
 * It is possible to verify the final contents of the in-memory database with the expectedGenericValues list through the methods
 * {@link #verify} (verify objects in expectedGenericValues exist in database) and {@link #verifyAll} (verify
 * objects in expectedGenericValues and only those objects exist in database).
 */
public class MockOfBizDelegator implements OfBizDelegator
{
    public static final int STARTING_ID = 1000;
    private final List<GenericValue> genericValues;
    private final List<GenericValue> expectedGenericValues;
    private final Map<List<Object>, List<Object>> relatedMap = new HashMap<List<Object>, List<Object>>();

    private int ids = STARTING_ID;
    private EntityCondition entityCondition;
    private List<String> orderBy;

    public MockOfBizDelegator()
    {
        this(null, null);
    }

    /**
     * Creates new instance of MockOfBizDelegator. The genericValues list can be used to pre-populate the in-memory
     * database with GenericValue objects required for the test. It represents the initial state of the database.
     * <p/>
     * The expectedGenericValues list represents a list objects that should exist in the final state of the database.
     * It can be used to verify the final contents of the in-memory database upon test completion.
     *
     * @param genericValues         a list of GenericValue objects that represents the initial state of the database
     * @param expectedGenericValues a list of GenericValue objects that represents the objects that should exist in the
     *                              final state of the database - it will be a complete list for {@link #verifyAll}
     *                              verification and a subset for {@link #verify} method.
     */
    public MockOfBizDelegator(final List<? extends GenericValue> genericValues, final List<? extends GenericValue> expectedGenericValues)
    {
        this.genericValues = (genericValues != null ? new ArrayList<GenericValue>(genericValues) : new ArrayList<GenericValue>());
        this.expectedGenericValues = (expectedGenericValues != null ? Collections.unmodifiableList(expectedGenericValues) : Collections.<GenericValue> emptyList());
    }

    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue)
    {
        return findByAnd(entityName, new FieldMap(fieldName, fieldValue));
    }

    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue, final String orderBy)
    {
        return findByAnd(entityName, new FieldMap(fieldName, fieldValue), CollectionBuilder.list(orderBy));
    }

    public synchronized void setGenericValues(final List<? extends GenericValue> genericValues)
    {
        this.genericValues.clear();
        this.genericValues.addAll(genericValues);
    }

    public List<GenericValue> findByAnd(final String s, final Map map) throws DataAccessException
    {
        return EntityUtil.filterByAnd(findAll(s), map);
    }

    public List<GenericValue> findByAnd(final String s, final Map map, final List orderClause) throws DataAccessException
    {
        final List values = findByAnd(s, map);

        if (!orderClause.isEmpty())
        {
            return EntityUtil.orderBy(values, orderClause);
        }
        else
        {
            return values;
        }
    }

    public List findByAnd(final String s, final List expressions) throws DataAccessException
    {
        return EntityUtil.filterByAnd(findAll(s), expressions);
    }

    public List findByOr(final String entityName, final List expressions, final List orderBy) throws DataAccessException
    {
        final List returnValues = new ArrayList();
        final List allValues = findAll(entityName);
        for (final Iterator iterator = expressions.iterator(); iterator.hasNext();)
        {
            final Object o = iterator.next();
            if (!(o instanceof EntityExpr))
            {
                continue;
            }

            final EntityExpr condition = (EntityExpr) o;
            returnValues.addAll(EntityUtil.filterByAnd(allValues, EasyList.build(condition)));

        }
        return EntityUtil.orderBy(returnValues, orderBy);
    }

    public List<GenericValue> findByLike(final String entityName, final Map<String, ?> map, final List<String> orderBy) throws DataAccessException
    {
        return findByAnd(entityName, map, orderBy); // this is approximate, and should only be used for tests
    }

    public List<GenericValue> findByLike(final String entityName, final Map<String, ?> map) throws DataAccessException
    {
        return null;
    }

    public synchronized void removeAll(final List genericValues) throws DataAccessException
    {
        this.genericValues.removeAll(genericValues);
    }

    public synchronized int removeByAnd(final String s, final Map map) throws DataAccessException
    {
        final List matching = findByAnd(s, map);
        genericValues.removeAll(matching);
        return matching.size();
    }

    @Override
    public int removeById(String entityName, Long id)
    {
        return removeByAnd(entityName, FieldMap.build("id", id));
    }

    public int removeValue(final GenericValue value) throws DataAccessException
    {
        removeAll(EasyList.build(value));
        return 1;
    }

    public synchronized void storeAll(final List<GenericValue> genericValues) throws DataAccessException
    {
        for (final GenericValue newValue : genericValues)
        {
            for (final Iterator<GenericValue> iterator = this.genericValues.iterator(); iterator.hasNext();)
            {
                final GenericValue oldValue = iterator.next();
                if (oldValue.getLong("id").equals(newValue.getLong("id")) && oldValue.getEntityName().equals(newValue.getEntityName()))
                {
                    iterator.remove();
                }
            }
        }
        this.genericValues.addAll(genericValues);
    }

    public synchronized List<GenericValue> findAll(final String s)
    {
        final List<GenericValue> matchingValues = new ArrayList<GenericValue>();

        for (final GenericValue value : genericValues)
        {
            if (s.equals(value.getEntityName()))
            {
                matchingValues.add(value);
            }
        }
        return matchingValues;
    }

    public List<GenericValue> findAll(final String s, final List sortOrder) throws DataAccessException
    {
        final List<GenericValue> matchingValues = findAll(s);
        return EntityUtil.orderBy(matchingValues, sortOrder);
    }

    public void store(final GenericValue gv) throws DataAccessException
    {
        // TODO: Should store GVs in a Map, not a list to avoid duplicates
        final Object id = gv.get("id");
        GenericValue currentValue = findByPrimaryKey(gv.getEntityName(), FieldMap.build("id", id));
        if (currentValue == null)
        {
            genericValues.add(gv);
        }
        else
        {
            // allow for partial updates
            for (Map.Entry<String, Object> entry : gv.entrySet())
            {
                currentValue.set(entry.getKey(), entry.getValue());
            }
        }
    }

    public synchronized GenericValue createValue(final String entity, final Map<String, Object> params) throws DataAccessException
    {
        final Map<String, Object> fields = new HashMap<String, Object>(params);
        if (!fields.containsKey("id"))
        {
            fields.put("id", new Long(ids++));
        }

        final MockGenericValue gv = new MockGenericValue(entity, fields);
        genericValues.add(gv);
        return gv;
    }

    public GenericValue makeValue(final String entity)
    {
        return makeValue(entity, null);
    }

    @Override
    public GenericValue makeValue(String entityName, Map<String, Object> fields)
    {
        return new MockGenericValue(entityName, fields);
    }

    @Override
    public GenericValue findById(String entityName, Long id) throws DataAccessException
    {
        return findByPrimaryKey(entityName, id);
    }

    public GenericValue findByPrimaryKey(final String entityName, final Long id)
    {
        // Build up the Map for the caller
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("id", id);
        // and delegate to the original findByPrimaryKey() method.
        return findByPrimaryKey(entityName, fields);
    }

    public GenericValue findByPrimaryKey(final String s, final Map map)
    {
        return EntityUtil.getOnly(findByAnd(s, map));
    }

    public List getRelated(final String relationName, final GenericValue gv)
    {
        final List key = EasyList.build(relationName, gv);

        if (relatedMap.containsKey(key))
        {
            return relatedMap.get(key);
        }
        else
        {
            throw new AssertionFailedError("Unexpected call to getRelated with '" + relationName + "' and '" + gv + "'");
        }
    }

    public long getCount(final String entityName)
    {
        return findAll(entityName).size();
    }

    public OfBizListIterator findListIteratorByCondition(final String entityType, final EntityCondition condition)
    {
        List res = findAll(entityType);
        if ((condition != null) && (condition instanceof EntityExpr))
        {
            final EntityExpr exp = (EntityExpr) condition;
            //todo implement other operators
            if (exp.getOperator() == EntityOperator.IN)
            {
                res = new ArrayList();
                final Collection gvs = findAll(entityType);
                for (final Iterator iterator = gvs.iterator(); iterator.hasNext();)
                {
                    final GenericValue genericValue = (GenericValue) iterator.next();
                    final Collection rhs = (Collection) exp.getRhs();
                    if (rhs.contains(genericValue.get(exp.getLhs())))
                    {
                        res.add(genericValue);
                    }
                }

            }
        }
        return new MockOfBizListIterator(res);
    }

    public OfBizListIterator findListIteratorByCondition(final String entityName, final EntityCondition whereEntityCondition, final EntityCondition havingEntityCondition, final Collection fieldsToSelect, final List orderBy, final EntityFindOptions entityFindOptions)
    {
        return new MockOfBizListIterator(findAll(entityName));
    }

    public int bulkUpdateByPrimaryKey(final String entityName, final Map updateValues, final List keys)
    {
        return 0;
    }

    public int bulkUpdateByAnd(final String entityName, final Map<String, ?> updateValues, final Map<String, ?> criteria)
    {
        final List<GenericValue> results = findByAnd(entityName, criteria);

        for (GenericValue gv : results)
        {
            for (Map.Entry<String, ?> updateEntry : updateValues.entrySet())
            {
                gv.set(updateEntry.getKey(), updateEntry.getValue());
            }
        }
        return results.size();
    }

    /**
     * Look through all the fields that are expected, and checks that they have the correct values.
     *
     * @throws AssertionFailedError if does not match
     */
    public void verifyAll() throws AssertionFailedError
    {
        if (!expectedGenericValues.equals(genericValues))
        {
            throw new AssertionFailedError("Expected: \n" + expectedGenericValues + " \nbut was: \n" + genericValues);
        }
    }

    /**
     * Look through all the fields that are expected, and checks that they have the correct values.  Ignores
     * values that are not in the expected GenericValue.
     *
     * @throws AssertionFailedError if does not match
     */
    public void verify() throws AssertionFailedError
    {
        if (expectedGenericValues.size() > genericValues.size())
        {
            throw new AssertionFailedError(
                "Expected: " + expectedGenericValues.size() + " genericValues, but there are " + genericValues.size() + "." + " Expected: " + expectedGenericValues + ", received " + genericValues);
        }

        for (final GenericValue expectedValue : expectedGenericValues)
        {
            List<GenericValue> matchingValues;

            //if we have specified an id (primary key), search by that, else search on all fields
            if (expectedValue.getLong("id") != null)
            {
                matchingValues = findByAnd(expectedValue.getEntityName(), EasyMap.build("id", expectedValue.getLong("id")));
            }
            else
            {
                matchingValues = findByAnd(expectedValue.getEntityName(), expectedValue.getFields(expectedValue.getAllKeys()));
            }

            if ((matchingValues == null) || matchingValues.isEmpty())
            {
                throw new AssertionFailedError(
                    "Expected GenericValue " + expectedValue + " not found.  Found entities " + findAll(expectedValue.getEntityName()));
            }
            else if (matchingValues.size() > 1)
            {
                throw new AssertionFailedError(
                    "Multiple matches for GenericValue " + expectedValue.getEntityName() + " with id " + expectedValue.getLong("id"));
            }

            final GenericValue receivedValue = EntityUtil.getOnly(matchingValues);
            assertFieldsMatch(expectedValue, receivedValue);

        }
    }

    private void assertFieldsMatch(final GenericValue expectedValue, final GenericValue receivedValue)
    {
        for (final Iterator iterator = expectedValue.getAllKeys().iterator(); iterator.hasNext();)
        {
            final Object fieldName = iterator.next();
            final Object recievedField = receivedValue.get(fieldName);
            final Object expectedField = expectedValue.get(fieldName);

            if ((recievedField == null) && (expectedField == null))
            {
                continue;
            }

            if ((recievedField == null) || !recievedField.equals(expectedField))
            {
                throw new AssertionFailedError(
                    "Expected '" + expectedField + "' for field '" + fieldName + "', but received '" + recievedField + "'." + "Expected GV: " + expectedValue + ". Received " + receivedValue);
            }

        }

    }

    public void addRelatedMap(final String relationName, final GenericValue gv, final List listToReturn)
    {
        relatedMap.put(EasyList.build(relationName, gv), listToReturn);
    }

    public int removeByOr(final String entityName, final String entityId, final List ids)
    {
        final List expressions = new ArrayList();
        for (final Iterator iterator = expressions.iterator(); iterator.hasNext();)
        {
            expressions.add(new EntityExpr(entityId, EntityOperator.EQUALS, iterator.next()));
        }
        final List removees = findByOr(entityName, expressions, null);
        removeAll(removees);
        return removees.size();
    }

    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect, final List<String> orderBy) throws DataAccessException
    {
        // We rememeber the last condition and order by statements.
        // Test can look at these for assertions
        this.entityCondition = entityCondition;
        this.orderBy = orderBy;
        // Don't actually know how to filter via SQL, so we just return all for the mock:
        return findAll(entityName);
    }
    
    public EntityCondition lastEntityCondition()
    {
        return entityCondition;
    }

    public List<String> lastOrderBy()
    {
        return orderBy;
    }

    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModelReader getModelReader()
    {
        throw new UnsupportedOperationException("Not mocked yet.");
    }

    @Override
    public void refreshSequencer()
    {
        throw new UnsupportedOperationException("Not mocked yet.");
    }

    public int bulkCopyColumnValuesByAnd(final String entityName, final Map updateColumns, final Map criteria)
    {
        return 0; // nothing updated
    }
}
