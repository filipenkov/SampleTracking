/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

public class PrimaryEntityRelationDelete extends AbstractEntityRelation
{
    public PrimaryEntityRelationDelete(final OfBizDelegator ofBizDelegator, final int id, final String relationType, final String relationName)
    {
        super(ofBizDelegator, id, relationType, relationName);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.primary.entity.relation.desc", getEntityName(), getRelationType() + getRelationName());
    }

    @Override
    protected Amendment previewAmendment(final GenericValue entity)
    {
        return new DeleteEntityAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.primary.entity.relation.delete.preview",
            getRelationType() + getRelationName(), getEntityName()), entity);
    }

    public List correct() throws IntegrityException
    {
        final List toRemove = new ArrayList();

        OfBizListIterator allEntities = null;
        final List result = new ArrayList();

        // Check if the relation exists for each entity
        try
        {
            allEntities = getEntities(getEntityName());
            GenericValue entity = allEntities.next();
            while (entity != null)
            {

                try
                {
                    final GenericValue relatedEntity = entity.getRelatedOne(getRelationType() + getRelationName());
                    if (relatedEntity == null)
                    {
                        // Then the entity we are looking at is invalid so add an amendment for this.
                        result.add(new DeleteEntityAmendment(Amendment.CORRECTION, getI18NBean().getText(
                            "admin.integrity.check.primary.entity.relation.delete.message", getRelationType() + getRelationName(), getEntityName()),
                            entity));
                        toRemove.add(entity);
                    }
                }
                catch (final GenericEntityException e)
                {
                    throw new IntegrityException(e);
                }
                entity = allEntities.next();
            }
        }
        catch (final Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }
        finally
        {
            if (allEntities != null)
            {
                // Close the iterator
                allEntities.close();
            }

        }

        if (!toRemove.isEmpty())
        {
            try
            {
                ofBizDelegator.removeAll(toRemove);
            }
            catch (final Exception e)
            {
                throw new IntegrityException(e);
            }
        }

        return result;
    }

    public boolean isAvailable()
    {
        return true;
    }
}