package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.model.group.Group;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class GroupEntity
{
    static final String ENTITY = "Group";
    static final String ID = "id";
    static final String NAME = "groupName";
    static final String LOWER_NAME = "lowerGroupName";
    static final String ACTIVE = "active";
    static final String LOCAL = "local";
    static final String DESCRIPTION = "description";
    static final String LOWER_DESCRIPTION = "lowerDescription";
    static final String CREATED_DATE = "createdDate";
    static final String UPDATED_DATE = "updatedDate";
    static final String DIRECTORY_ID = "directoryId";
    static final String TYPE = "type";

    private GroupEntity()
    {}

    static Map<String, Object> getData(final Group group, final Timestamp updatedDate, final Timestamp createdDate, final boolean local)
    {
        final PrimitiveMap.Builder data = PrimitiveMap.builder();
        data.put(NAME, group.getName());
        data.putCaseInsensitive(LOWER_NAME, group.getName());
        data.put(ACTIVE, group.isActive());
        data.put(DESCRIPTION, group.getDescription());
        data.putCaseInsensitive(LOWER_DESCRIPTION, group.getDescription());
        data.put(DIRECTORY_ID, group.getDirectoryId());
        data.put(TYPE, group.getType().name());
        if (updatedDate != null)
        {
            data.put(UPDATED_DATE, updatedDate);
        }
        if (createdDate != null)
        {
            data.put(CREATED_DATE, createdDate);
        }
        data.put(LOCAL, local);
        return data.build();
    }

    /**
     * Map of all searchable fields along with their lowercase sibling field, if it is present.
     * All string fields are searched using case insensitive matching, against the lower case sibling field. */
    private static final Map<String, String> FIELD_NAME_TRANSLATION;
    static
    {
        final Map<String, String> builder = new HashMap<String, String>();
        builder.put(ID, null);
        builder.put(NAME, LOWER_NAME);
        builder.put(LOWER_NAME, LOWER_NAME);
        builder.put(DIRECTORY_ID, null);
        builder.put(ACTIVE, null);
        builder.put(LOCAL, null);
        builder.put(DESCRIPTION, LOWER_DESCRIPTION);
        builder.put(LOWER_DESCRIPTION, LOWER_DESCRIPTION);
        builder.put(TYPE, null);
        builder.put(CREATED_DATE, null);
        builder.put(UPDATED_DATE, null);
        FIELD_NAME_TRANSLATION = Collections.unmodifiableMap(builder);
    }

    /**
     * Return the name of the sibling lower case field of the supplied field.
     * Lower case fields return themselves as their own sibling.
     * @param fieldName Field name to search for sibling of.
     * @return name of lower case sibling.
     */
    static String getLowercaseFieldNameFor(final String fieldName)
    {
        return FIELD_NAME_TRANSLATION.get(fieldName);
    }

    /**
     * Returns true if the field name passed in is a first class field member of this entity.
     * That is it is a field rather than a secondary attribute, that may be stored elsewhere.
     * @param fieldName Field name to search
     * @return true if the field name passed in is a first class field member of this entity.
     */
    static boolean isSystemField(final String fieldName)
    {
        return FIELD_NAME_TRANSLATION.containsKey(fieldName);
    }
}
