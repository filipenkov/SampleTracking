package com.atlassian.jira.entity;

import java.util.List;

/**
 * Provides methods for working with the DB via Atlassian EntityEngine.
 *
 * These methods are considered a higeher level alternative to the OfBizDelegator.
 *
 * This interface is considered experimental at this stage.
 *
 * @since v4.4
 */
public interface EntityEngine
{
    <E> SelectFromContext<E> selectFrom(EntityFactory<E> entity);

    <E> E createValue(EntityFactory<E> entityFactory, E newValue);

    <E> void updateValue(EntityFactory<E> entityFactory, E newValue);

    interface SelectFromContext<E>
    {
        WhereContext<E> findAll();

        E findById(Long id);

        WhereEqualContext<E> whereEqual(String fieldName, String value);

        WhereEqualContext<E> whereEqual(String fieldName, Long value);
    }

    interface WhereContext<E>
    {
        List<E> orderBy(String... orderByColumn);

        List<E> list();

        /**
         * Returns the single Entity that is the result of this query.
         * <p>
         * Will return null if no rows were returned by the DB query.
         * Throws {@link com.atlassian.jira.jql.builder.PrecedenceSimpleClauseBuilder.IllegalState} if more than one row is found.
         *
         * @return the Entity found or null of none found.
         */
        E singleValue();
    }

    interface WhereEqualContext<E> extends WhereEqualAndContext<E>//, WhereEqualOrContext<E>
    {
    }

    interface WhereEqualAndContext<E> extends WhereContext<E>
    {
        WhereEqualAndContext<E> andEqual(String fieldName, String value);

        WhereEqualAndContext<E> andEqual(String fieldName, Long value);
    }

//    interface WhereEqualOrContext<E> extends WhereContext<E>
//    {
//        WhereEqualOrContext<E> orEqual(String fieldName, String value);
//
//        WhereEqualOrContext<E> orEqual(String fieldName, Long value);
//    }
}
