package com.atlassian.jira.plugins.mail;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * Test utilities
 *
 */
public class UTUtils
{
    public static GenericValue getTestEntity(final String entity, final Map fields)
    {
        return new MockGenericValue(entity, fields);
    }

}
