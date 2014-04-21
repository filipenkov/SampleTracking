package com.atlassian.core.ofbiz.test;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockSequenceUtil;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.action.DefaultActionDispatcher;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.MemoryHelper;
import org.ofbiz.core.entity.model.ModelEntity;

import webwork.action.ActionContext;

import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.Group;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

public class UtilsForTests
{
    /**
     * Get a test Entity
     * @param entity the name of the entity
     * @param fields the field values
     * @return the created GenericValue
     * @throws EntityException if OfBiz throws a {@link GenericEntityException}
     */
    public static GenericValue getTestEntity(final String entity, final Map fields)
    {
        try
        {
            return EntityUtils.createValue(entity, fields);
        }
        catch (final GenericEntityException e)
        {
            throw new EntityException(e);
        }
    }

    /**
     * Get a test User
     * @param username the username
     * @return the created GenericValue
     * @throws DuplicateException if OfBiz throws a {@link DuplicateEntityException}
     */
    public static User getTestUser(final String username)
    {
        try
        {
            return UserManager.getInstance().createUser(username);
        }
        catch (final DuplicateEntityException e)
        {
            throw new DuplicateException(e);
        }
        catch (final ImmutableException e)
        {
            throw new UnchangeableException(e);
        }
    }

    public static Group getTestGroup(final String groupname)
    {
        try
        {
            return UserManager.getInstance().createGroup(groupname);
        }
        catch (final DuplicateEntityException e)
        {
            throw new DuplicateException(e);
        }
        catch (final ImmutableException e)
        {
            throw new UnchangeableException(e);
        }
    }

    public static GenericValue getTestConstant(final String entity, Map params)
    {
        try
        {
            if (params == null)
            {
                params = new HashMap();
            }

            if (params.get("id") == null)
            {
                final String id = EntityUtils.getNextStringId(entity);
                params.put("id", id);
            }

            GenericValue v = CoreFactory.getGenericDelegator().makeValue(entity, params);
            v = v.create();
            return v;
        }
        catch (final GenericEntityException e)
        {
            throw new EntityException(e);
        }
    }

    /**
     * Remove all users and groups.
     */
    public static void cleanUsers() throws Exception
    {
        for (final Iterator iterator = UserManager.getInstance().getUsers().iterator(); iterator.hasNext();)
        {
            ((User) iterator.next()).remove();
        }

        for (final Iterator iterator = UserManager.getInstance().getGroups().iterator(); iterator.hasNext();)
        {
            ((Group) iterator.next()).remove();
        }
    }

    public static void cleanWebWork()
    {
        ActionContext.setContext(new ActionContext());
        CoreFactory.setActionDispatcher(new DefaultActionDispatcher());
    }

    public static void cleanOFBiz()
    {
        MemoryHelper.clearCache();

        final String helperName = CoreFactory.getGenericDelegator().getEntityHelperName("SequenceValueItem");
        final ModelEntity seqEntity = CoreFactory.getGenericDelegator().getModelEntity("SequenceValueItem");

        CoreFactory.getGenericDelegator().setSequencer(new MockSequenceUtil(helperName, seqEntity, "seqName", "seqId"));
    }

    /**
     * Check that a collection has only one element, and that is the object provided
     */
    public static void checkSingleElementCollection(final Collection collection, final Object expected)
    {
        Assert.assertEquals(1, collection.size());
        Assert.assertTrue(collection.contains(expected));
    }

    //
    // specialised runtime exceptions in case anyone ever wants to catch them explicitly
    //

    public static class DuplicateException extends RuntimeException
    {
        DuplicateException(final DuplicateEntityException ex)
        {
            super(ex);
        }
    }

    public static class EntityException extends RuntimeException
    {
        EntityException(final GenericEntityException ex)
        {
            super(ex);
        }
    }

    public static class UnchangeableException extends RuntimeException
    {
        UnchangeableException(final ImmutableException ex)
        {
            super(ex);
        }
    }
}
