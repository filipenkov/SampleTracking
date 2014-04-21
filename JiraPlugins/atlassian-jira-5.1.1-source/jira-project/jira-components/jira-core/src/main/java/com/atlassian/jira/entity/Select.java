package com.atlassian.jira.entity;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Select is the entry point to building up a {@link SelectQuery} which can be run in OfBiz Entity Engine.
 * <p/>
 * eg
 * <pre>
 * {@literal
 * SelectQuery<GenericValue> query = Select.columns().from("FilterSubscription")
 *      .whereEqual("group", (String) null).andEqual("username", username)
 *      .orderBy("id desc");
 * }
 * </pre>
 *
 * <p/>
 * If you are selecting a single column, then you can it can return String objects instead of GenericValues like
 * <pre>
 * {@literal
 * SelectQuery<String> query = Select.distinctString("username").from("FilterSubscription");
 * List<String> vals = query.runWith(delegator).asList();
 * }
 * </pre>
 *
 * <p/>
 * You can also use an {@link EntityFactory} to automatically convert the GenericValues to other entity objects.
 * <pre>
 * {@literal
 * List<ProjectCategory> categories = Select.from(Entity.PROJECT_CATEGORY).runWith(delegator).asList();
 * }
 * </pre>
 *
 * @since v5.2
 */
@SuppressWarnings ("UnusedDeclaration")
public class Select
{
    public static SelectColumnsContext columns(String... columns)
    {
        return new SelectColumnsContext(columns, false);
    }

    public static SelectColumnsContext distinct(String... columns)
    {
        return new SelectColumnsContext(columns, true);
    }

    public static SelectSingleColumnContext<String> distinctString(String columnName)
    {
        EntityBuilder<String> entityBuilder = new StringEntityBuilder(columnName);
        return new SelectSingleColumnContext<String>(columnName, true, entityBuilder);
    }

    public static <E> SelectColumnsFromContext<E> from(EntityFactory<E> entityFactory)
    {
        final QueryBuilder<E> queryBuilder = new QueryBuilder<E>();
        queryBuilder.fieldsToSelect = null;
        queryBuilder.distinct = false;
        queryBuilder.entityBuilder = entityFactory;
        queryBuilder.entityName = entityFactory.getEntityName();

        return new SelectColumnsFromContext<E>(queryBuilder);
    }

    public static class SelectSingleColumnContext<E>
    {
        private final QueryBuilder<E> queryBuilder = new QueryBuilder<E>();

        private SelectSingleColumnContext(String columnName, boolean distinct, EntityBuilder<E> entityBuilder)
        {
            queryBuilder.fieldsToSelect = Collections.singletonList(columnName);
            queryBuilder.distinct = distinct;
            queryBuilder.entityBuilder = entityBuilder;
        }

        public SelectColumnsFromContext<E> from(String entityName)
        {
            queryBuilder.entityName = entityName;
            return new SelectColumnsFromContext<E>(queryBuilder);
        }
    }

    public static class SelectColumnsContext
    {
        private final QueryBuilder<GenericValue> queryBuilder = new QueryBuilder<GenericValue>();

        private SelectColumnsContext(String[] columns, boolean distinct)
        {
            queryBuilder.fieldsToSelect = Arrays.asList(columns);
            queryBuilder.distinct = distinct;
        }

        public SelectColumnsFromContext<GenericValue> from(String entityName)
        {
            queryBuilder.entityName = entityName;
            queryBuilder.entityBuilder = EntityBuilders.NO_OP_BUILDER;
            return new SelectColumnsFromContext<GenericValue>(queryBuilder);
        }
    }

    @SuppressWarnings ("UnusedDeclaration")
    public static class SelectColumnsFromContext<E> implements SelectQuery<E>
    {
        private final QueryBuilder<E> queryBuilder;

        private SelectColumnsFromContext(QueryBuilder<E> queryBuilder)
        {
            this.queryBuilder = queryBuilder;
        }

        public WhereContext<E> whereEqual(String fieldName, String value)
        {
            queryBuilder.whereClause = new FieldMap(fieldName, value);
            return new WhereContext<E>(queryBuilder);
        }

        public WhereContext<E> whereEqual(String fieldName, Long value)
        {
            queryBuilder.whereClause = new FieldMap(fieldName, value);
            return new WhereContext<E>(queryBuilder);
        }

        public SelectQueryImpl.ExecutionContext<E> runWith(OfBizDelegator ofBizDelegator)
        {
            return queryBuilder.toQuery().runWith(ofBizDelegator);
        }

        public SelectQueryImpl.ExecutionContext<E> runWith(EntityEngine entityEngine)
        {
            return queryBuilder.toQuery().runWith(entityEngine);
        }
    }

    @SuppressWarnings ("UnusedDeclaration")
    public static class WhereContext<E> implements SelectQuery<E>
    {
        private final QueryBuilder<E> queryBuilder;

        private WhereContext(QueryBuilder<E> queryBuilder)
        {
            this.queryBuilder = queryBuilder;
        }

        public WhereContext andEqual(String fieldName, String value)
        {
            queryBuilder.whereClause.add(fieldName, value);
            return this;
        }

        public WhereContext andEqual(String fieldName, Long value)
        {
            queryBuilder.whereClause.add(fieldName, value);
            return this;
        }

        public OrderByContext<E> orderBy(String... orderByColumn)
        {
            queryBuilder.orderBy = Arrays.asList(orderByColumn);
            return new OrderByContext<E>(queryBuilder);
        }

        public SelectQuery.ExecutionContext<E> runWith(OfBizDelegator ofBizDelegator)
        {
            return queryBuilder.toQuery().runWith(ofBizDelegator);
        }

        public SelectQuery.ExecutionContext<E> runWith(EntityEngine entityEngine)
        {
            return queryBuilder.toQuery().runWith(entityEngine);
        }
    }

    @SuppressWarnings ("UnusedDeclaration")
    public static class OrderByContext<E> implements SelectQuery<E>
    {
        private final QueryBuilder<E> queryBuilder;
        private OrderByContext(QueryBuilder<E> queryBuilder)
        {
            this.queryBuilder = queryBuilder;
        }

        public SelectQuery.ExecutionContext<E> runWith(OfBizDelegator ofBizDelegator)
        {
            return queryBuilder.toQuery().runWith(ofBizDelegator);
        }

        public SelectQuery.ExecutionContext<E> runWith(EntityEngine entityEngine)
        {
            return queryBuilder.toQuery().runWith(entityEngine);
        }
    }

    private static class QueryBuilder<E>
    {
        private boolean distinct;
        private Collection<String> fieldsToSelect;
        private String entityName;
        private FieldMap whereClause;
        private List<String> orderBy;
        public EntityBuilder<E> entityBuilder;

        public SelectQuery<E> toQuery()
        {
            return new SelectQueryImpl<E>(distinct, fieldsToSelect, entityName, entityBuilder, whereClause, orderBy);
        }
    }
}
