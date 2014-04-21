package com.atlassian.jira.entity;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

/**
 * This class is used to create a Delete SQL statement to be executed by {@link EntityEngine#delete(Delete.DeleteWhereContext)}.
 *
 * @since v5.0
 *
 * @see EntityEngine#delete(Delete.DeleteWhereContext)
 */
public class Delete
{
    public static DeleteFromContext from(String entityName)
    {
        return new DeleteFromContext(entityName);
    }

    public static DeleteFromContext from(EntityFactory entityFactory)
    {
        return new DeleteFromContext(entityFactory.getEntityName());
    }

    public static class DeleteFromContext
    {
        private final String entityName;

        private DeleteFromContext(String entityName)
        {
            this.entityName = entityName;
        }

        public DeleteWhereContext all()
        {
            return new DeleteWhereContext(entityName, new FieldMap());
        }

        public DeleteWhereContext whereIdEquals(Long id)
        {
            return new DeleteWhereContext(entityName, new FieldMap("id", id));
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        public DeleteWhereContext whereEqual(String fieldName, String value)
        {
            return new DeleteWhereContext(entityName, new FieldMap(fieldName, value));
        }

        public DeleteWhereContext whereEqual(String fieldName, Long value)
        {
            return new DeleteWhereContext(entityName, new FieldMap(fieldName, value));
        }
    }

    public static class DeleteWhereContext
    {
        private final String entityName;
        private final FieldMap fieldMap;

        private DeleteWhereContext(String entityName, FieldMap fieldMap)
        {
            this.entityName = entityName;
            this.fieldMap = fieldMap;
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        public DeleteWhereContext andEqual(String fieldName, String value)
        {
            return new DeleteWhereContext(entityName, fieldMap.add(fieldName, value));
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        public DeleteWhereContext andEqual(String fieldName, Long value)
        {
            return new DeleteWhereContext(entityName, fieldMap.add(fieldName, value));
        }

        String getEntityName()
        {
            return entityName;
        }

        FieldMap getFieldMap()
        {
            return fieldMap;
        }

        public int execute(EntityEngine entityEngine)
        {
            return entityEngine.delete(this);
        }

        public int execute(OfBizDelegator ofBizDelegator)
        {
            return ofBizDelegator.removeByAnd(getEntityName(), getFieldMap());
        }
    }
}
