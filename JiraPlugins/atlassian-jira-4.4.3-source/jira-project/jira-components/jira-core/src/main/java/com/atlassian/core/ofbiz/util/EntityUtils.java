/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 28/02/2002
 * Time: 15:15:42
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.ofbiz.util;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.ObjectUtils;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class EntityUtils
{
    private static final EntityOperator[] entityOperators = { EntityOperator.EQUALS, EntityOperator.NOT_EQUAL,
            EntityOperator.LESS_THAN, EntityOperator.GREATER_THAN, EntityOperator.LESS_THAN_EQUAL_TO,
            EntityOperator.GREATER_THAN_EQUAL_TO, EntityOperator.IN, EntityOperator.BETWEEN, EntityOperator.NOT,
            EntityOperator.AND, EntityOperator.OR };

    /**
     * Small utility method to get an entity operator from it's code
     */
    public static EntityOperator getOperator(final String code)
    {
        for (int i = 0; i < entityOperators.length; i++)
        {
            final EntityOperator operator = entityOperators[i];
            if (operator.toString().trim().equals(code.trim()))
            {
                return operator;
            }
        }
        return null;
    }

    /**
     * Create a new entity.
     *
     * If there is no "id" in the parameter list, one is created using the entity sequence
     */
    public static GenericValue createValue(final String entity, final Map<String, ?> paramMap) throws GenericEntityException
    {
        final Map<String, Object> params = (paramMap == null) ? new HashMap<String, Object>() : new HashMap<String, Object>(paramMap);

        if (params.get("id") == null)
        {
            final Long id = CoreFactory.getGenericDelegator().getNextSeqId(entity);
            params.put("id", id);
        }

        final GenericValue v = CoreFactory.getGenericDelegator().makeValue(entity, params);
        v.create();
        return v;
    }

    /**
     * Compare two GenericValues based on their content.
     *
     * This method will check the keys and values of both GenericValues.
     *
     * There is only a single difference between this method and GenericValue.equals(gv2),
     * that is that if one GV has no key of a certain type, and the other has a null value
     * for that key, they are still deemed to be identical (as GenericValue.get(key)
     * always returns null if the key exists or not).
     *
     * @return true if the issues are the same, false if they are different
     */
    public static boolean identical(final GenericValue v1, final GenericValue v2)
    {
        if ((v1 == null) && (v2 == null))
        {
            return true;
        }
        if ((v1 == null) || (v2 == null))
        {
            return false;
        }

        if (!v1.getEntityName().equals(v2.getEntityName()))
        {
            return false;
        }

        // get the keys of v1, make sure they are equal in v2
        for (final Iterator iterator = v1.getAllKeys().iterator(); iterator.hasNext();)
        {
            final String key = (String) iterator.next();

            if ((v1.get(key) == null) && (v2.get(key) == null))
            {
                continue;
            }
            if ((v1.get(key) == null) && (v2.get(key) != null))
            {
                return false;
            }
            else
            {
                // handle timestamps specially due to precision
                if ((v1.get(key) instanceof Timestamp) && (v2.get(key) instanceof Timestamp))
                {
                    final Timestamp t1 = (Timestamp) v1.get(key);
                    final Timestamp t2 = (Timestamp) v2.get(key);
                    if (!DateUtils.equalTimestamps(t1, t2))
                    {
                        return false;
                    }
                }
                else if (!v1.get(key).equals(v2.get(key)))
                {
                    return false;
                }
            }
        }

        // if they keys aren't the same, loop through v2
        final Collection uncheckedKeys = new ArrayList(v2.getAllKeys());
        uncheckedKeys.removeAll(v1.getAllKeys());

        // for the unchecked keys in v2, if they have values in v2, then the GVs are not identical
        if (uncheckedKeys.size() > 0)
        {
            for (final Iterator iterator = uncheckedKeys.iterator(); iterator.hasNext();)
            {
                final String key = (String) iterator.next();
                if (v2.get(key) == null)
                {
                    continue;
                }
                else
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a collection of entities contains an identical entity
     * to the one provided.
     *
     * @param entities The list of entities to search
     * @param entity The entity to search for and compare to
     * @return The matching entity, or null if no matching entity is found
     */
    public static boolean contains(final Collection<GenericValue> entities, final GenericValue entity)
    {
        for (final GenericValue gv : entities)
        {
            if (identical(gv, entity))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is analogous to the OFBiz EntityUtil.filterByAnd method
     * except that theirs does not filter on GT, GTE, LT, LTE yet.
     *
     * @param values A collection of GenericValues
     * @param exprs The expressions that must validate to true
     * @return Collection of GenericValues that match the expression list
     */
    public static List<GenericValue> filterByAnd(final List<GenericValue> values, final List<? extends EntityExpr> exprs)
    {
        if (values == null)
        {
            return null;
        }
        if ((exprs == null) || (exprs.size() == 0))
        {
            return values;
        }

        final List<GenericValue> result = new ArrayList<GenericValue>();

        for (final GenericValue value : values)
        {
            boolean include = true;

            for (final EntityExpr expr : exprs)
            {
                final Object lhs = value.get((String) expr.getLhs());
                final Object rhs = expr.getRhs();

                if (EntityOperator.EQUALS.equals(expr.getOperator()))
                {
                    //if the field named by lhs is not equal to rhs value, constraint fails
                    include = ObjectUtils.isIdentical(lhs, rhs);

                    if (!include)
                    {
                        break;
                    }
                }
                else if (EntityOperator.NOT_EQUAL.equals(expr.getOperator()))
                {
                    include = ObjectUtils.isDifferent(lhs, rhs);

                    if (!include)
                    {
                        break;
                    }
                }
                else if (EntityOperator.GREATER_THAN.equals(expr.getOperator()) || EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator()) || EntityOperator.LESS_THAN.equals(expr.getOperator()) || EntityOperator.LESS_THAN_EQUAL_TO.equals(expr.getOperator()))
                {
                    if ((rhs != null) && (lhs != null) && (rhs instanceof Comparable))
                    {
                        final Comparable rhsComp = (Comparable) rhs;
                        final Comparable lhsComp = (Comparable) lhs;

                        final int comparison = lhsComp.compareTo(rhsComp);

                        if ((comparison <= 0) && EntityOperator.LESS_THAN_EQUAL_TO.equals(expr.getOperator()))
                        {
                            include = true;
                        }
                        else if ((comparison < 0) && EntityOperator.LESS_THAN.equals(expr.getOperator()))
                        {
                            include = true;
                        }
                        else if ((comparison >= 0) && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator()))
                        {
                            include = true;
                        }
                        else if ((comparison > 0) && EntityOperator.GREATER_THAN.equals(expr.getOperator()))
                        {
                            include = true;
                        }
                        else
                        {
                            include = false;
                            break;
                        }
                    }
                    else
                    {
                        throw new IllegalArgumentException(
                                "Operation " + expr.getOperator().getCode() + " is not yet supported by filterByAnd with objects that do not implement Comparable");
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Operation " + expr.getOperator().getCode() + " is not yet supported by filterByAnd");
                }
            }

            if (include)
            {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Calculate a new entity ID (by basically taking one more than the max integer that exists).
     * If there are string IDs here, they will not be affected.
     */
    public static synchronized String getNextStringId(final String entityName) throws GenericEntityException
    {
        long maxID = 1;

        for (final Iterator iterator = CoreFactory.getGenericDelegator().findAll(entityName).iterator(); iterator.hasNext();)
        {
            final GenericValue entity = (GenericValue) iterator.next();
            try
            {
                final long entityId = Long.parseLong(entity.getString("id"));
                if (entityId >= maxID)
                {
                    maxID = entityId + 1;
                }
            }
            catch (final NumberFormatException nfe)
            {
                // ignore - we don't care about String constant IDs
            }
        }
        return Long.toString(maxID);
    }
}
