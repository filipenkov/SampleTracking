package com.atlassian.jira.issue.customfields.option;

import com.atlassian.jira.issue.fields.config.FieldConfig;

import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * should merge with the field.option.Option
 */
public interface Option
{
    Long getOptionId();

    Long getSequence();

    String getValue();

    /**
     * Get the disabled status.
     * A disabled option will is not available to be assigned to this associated custom field, It remains
     * valid historically and for searching with.
     */
    Boolean getDisabled();

    GenericValue getGenericValue();

    FieldConfig getRelatedCustomField();

    Option getParentOption();

    List<Option> getChildOptions();

    void setSequence(Long sequence);

    void setValue(String value);

    void setDisabled(Boolean disabled);

    //convenience method - candidate to be removed to a util class?
    List<Option> retrieveAllChildren(List<Option> listToAddTo);

    void store();
}
