package org.ofbiz.core.entity.model;

import java.util.List;

/**
 * @since v3.13
 */
public class MockModelEntity extends ModelEntity
{
    /**
     * Constructs a MockModelEntity with the given entityName
     * @param entityName The entity name.
     */
    public MockModelEntity(String entityName)
    {
        setEntityName(entityName);
    }

    /**
     * Takes a List of Strings and adds ModelFields to this MockModelEntity accordingly.
     *
     * @param fieldNames List of String objects containing field names for the new ModelFields.
     */
    public void setFieldNames(List<String> fieldNames)
    {
        for (String fieldName : fieldNames)
        {
            ModelField modelField = new ModelField();
            modelField.setName(fieldName);
            modelField.setColName(fieldName);
            addField(modelField);
        }
    }
}
