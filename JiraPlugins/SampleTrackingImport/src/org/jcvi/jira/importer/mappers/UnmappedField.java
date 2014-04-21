package org.jcvi.jira.importer.mappers;

import javax.management.InvalidAttributeValueException;

/**
 * Store the information about the failure of the mapping in a more
 * structured manor to allow comparison and suppression of duplicate
 * error messages.
 */
public class UnmappedField extends InvalidAttributeValueException {
    private final String fieldName;
    private final String value;


    public UnmappedField(String field, String value) {
        this.fieldName = field;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getMessage() {
        return "Failed to map field:'"+getFieldName()+"' value '"+getValue()+"'";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null ||
                !(object instanceof UnmappedField)) {
            return false;
        }
        UnmappedField unmappedEntity = (UnmappedField)object;
        return nullStringEquals(unmappedEntity.getFieldName(),getFieldName())
            && nullStringEquals(unmappedEntity.getValue(),getValue());
    }

    @Override
    public int hashCode() {
        int total = 0;
        if (fieldName != null) {
            total += fieldName.hashCode();
        }
        if (value != null) {
            total += value.hashCode();
        }
        return total;
    }

    private boolean nullStringEquals(String a, String b) {
        return a == null && b == null
               || (
                       !(a == null || b == null)
                       && a.equals(b)
               );
    }
}
