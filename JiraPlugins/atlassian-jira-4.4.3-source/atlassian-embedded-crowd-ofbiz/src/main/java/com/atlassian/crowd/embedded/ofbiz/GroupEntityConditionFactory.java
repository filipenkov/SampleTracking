package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.search.query.entity.restriction.Property;

/**
 * Creates OfBiz EntityCondition objects from Crowd search SearchRestriction objects for Users.
 *
 * @since 0.1
 */
class GroupEntityConditionFactory extends EntityConditionFactory
{
    @Override
    String getEntityTableIdColumnName()
    {
        return "id";
    }

    @Override
    String getAttributeTableName()
    {
        return "cwd_group_attributes";
    }

    @Override
    String getAttributeIdColumnName()
    {
        return "group_id";
    }

    @Override
    boolean isCoreProperty(final Property<?> property)
    {
        return GroupEntity.isSystemField(property.getPropertyName());
    }

    @Override
    String getLowerFieldName(final Property<?> property)
    {
        return GroupEntity.getLowercaseFieldNameFor(property.getPropertyName());
    }
}