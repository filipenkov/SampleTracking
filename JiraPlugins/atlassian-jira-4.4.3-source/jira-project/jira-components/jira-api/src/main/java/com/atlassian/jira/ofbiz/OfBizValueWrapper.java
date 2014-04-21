package com.atlassian.jira.ofbiz;

import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;

public interface OfBizValueWrapper
{
    /** Retrieve a String field. */
    String getString(String name);

    /** Retrieve a timestamp field. */
    Timestamp getTimestamp(String name);

    /** Retrieve a numeric field. */
    Long getLong(String name);

    /** Get the backing GenericValue object. */
    GenericValue getGenericValue();

    /** Persist this object's immediate fields. */
    void store();
}
