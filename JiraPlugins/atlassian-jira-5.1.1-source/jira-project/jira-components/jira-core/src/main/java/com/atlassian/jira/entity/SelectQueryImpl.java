package com.atlassian.jira.entity;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since v5.2
 */
public class SelectQueryImpl<E> implements SelectQuery<E>
{
    private final boolean distinct;
    private final Collection<String> fieldsToSelect;
    private final String entityName;
    private final EntityBuilder<E> entityBuilder;
    private final FieldMap whereClause;
    private final List<String> orderBy;

    public SelectQueryImpl(boolean distinct, Collection<String> fieldsToSelect, String entityName, EntityBuilder<E> entityBuilder,
            FieldMap whereClause, List<String> orderBy)
    {
        this.distinct = distinct;
        this.fieldsToSelect = fieldsToSelect;
        this.entityName = entityName;
        this.entityBuilder = entityBuilder;
        this.whereClause = whereClause;
        this.orderBy = orderBy;
    }

    @Override
    public ExecutionContext<E> runWith(OfBizDelegator ofBizDelegator)
    {
        return new ExecutionContextImpl<E>(ofBizDelegator, entityBuilder);
    }

    @Override
    public ExecutionContext<E> runWith(EntityEngine entityEngine)
    {
        return entityEngine.run(this);
    }

    private EntityCondition getWhereEntityCondition()
    {
        if (whereClause == null)
            return null;
        return new EntityFieldMap(whereClause, EntityOperator.AND);
    }

    @SuppressWarnings ("UnusedDeclaration")
    public final class ExecutionContextImpl<E> implements ExecutionContext<E>
    {
        private OfBizDelegator ofBizDelegator;
        private final EntityBuilder<E> entityBuilder;

        public ExecutionContextImpl(OfBizDelegator ofBizDelegator, EntityBuilder<E> entityBuilder)
        {
            this.ofBizDelegator = ofBizDelegator;
            this.entityBuilder = entityBuilder;
        }

        @Override
        public List<E> asList()
        {
            return consumeWith(new EntityListConsumer<E, List<E>>()
            {
                public List<E> list = new ArrayList<E>();

                @Override
                public void consume(E entity)
                {
                    list.add(entity);
                }

                @Override
                public List<E> result()
                {
                    return list;
                }
            });
        }

        @Override
        public E singleValue() throws IllegalStateException
        {
            return consumeWith(new EntityListConsumer<E, E>()
            {
                private E value = null;
                private boolean found = false;

                @Override
                public void consume(E entity)
                {
                    if (found)
                        throw new IllegalStateException("Too many rows found for query on " + entityName);
                    value = entity;
                    found = true;
                }

                @Override
                public E result()
                {
                    return value;
                }
            });
        }

        @Override
        public <R> R consumeWith(EntityListConsumer<E, R> consumer)
        {
            // DISTINCT clause
            EntityFindOptions entityFindOptions = new EntityFindOptions();
            entityFindOptions.setDistinct(distinct);
            // Run query
            final OfBizListIterator ofBizListIterator = ofBizDelegator.findListIteratorByCondition(entityName, getWhereEntityCondition(), null, fieldsToSelect, orderBy, entityFindOptions);
            try
            {
                for (GenericValue genericValue : ofBizListIterator)
                {
                    consumer.consume(entityBuilder.build(genericValue));
                }
            }
            finally
            {
                ofBizListIterator.close();
            }
            return consumer.result();
        }
    }
}
