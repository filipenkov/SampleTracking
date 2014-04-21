package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.embedded.ofbiz.db.OfBizHelper;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Creates OfBiz EntityCondition objects from Crowd search SearchRestriction objects for Users.
 *
 * @since 0.1
 */
class UserEntityConditionFactory extends EntityConditionFactory
{
    private final String attributeTableName;

    UserEntityConditionFactory(OfBizHelper ofBizHelper)
    {
        final ModelEntity modelEntity = ofBizHelper.getModelEntity(UserAttributeEntity.ENTITY);
        attributeTableName = modelEntity.getTableName(ofBizHelper.getEntityHelperName(UserAttributeEntity.ENTITY));
    }

    @Override
    String getEntityTableIdColumnName()
    {
        return "id";
    }

    @Override
    String getAttributeTableName()
    {
        return attributeTableName;
    }

    @Override
    String getAttributeIdColumnName()
    {
        return "user_id";
    }

    @Override
    boolean isCoreProperty(final Property<?> property)
    {
        return property.equals(UserTermKeys.USERNAME) ||
            property.equals(UserTermKeys.EMAIL) ||
            property.equals(UserTermKeys.FIRST_NAME) ||
            property.equals(UserTermKeys.LAST_NAME) ||
            property.equals(UserTermKeys.DISPLAY_NAME) ||
            property.equals(UserTermKeys.CREATED_DATE) ||
            property.equals(UserTermKeys.UPDATED_DATE) ||
            property.equals(UserTermKeys.ACTIVE);
    }

    @Override
    String getLowerFieldName(final Property<?> property)
    {
        if (property.equals(UserTermKeys.USERNAME))
        {
            return UserEntity.LOWER_USER_NAME;
        }
        else if (property.equals(UserTermKeys.EMAIL))
        {
            return UserEntity.LOWER_EMAIL_ADDRESS;
        }
        else if (property.equals(UserTermKeys.FIRST_NAME))
        {
            return UserEntity.LOWER_FIRST_NAME;
        }
        else if (property.equals(UserTermKeys.LAST_NAME))
        {
            return UserEntity.LOWER_LAST_NAME;
        }
        else if (property.equals(UserTermKeys.DISPLAY_NAME))
        {
            return UserEntity.LOWER_DISPLAY_NAME;
        }
        else if (property.equals(UserTermKeys.ACTIVE))
        {
            return UserEntity.ACTIVE;
        }
        else if (property.equals(UserTermKeys.CREATED_DATE))
        {
            return UserEntity.CREATED_DATE;
        }
        else if (property.equals(UserTermKeys.UPDATED_DATE))
        {
            return UserEntity.UPDATED_DATE;
        }
        throw new UnsupportedOperationException("Unknown user property: " + property);
    }
}
