package com.atlassian.jira.external.beans;

import org.junit.Test;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Map;

/**
 * @since v3.13
 */
public class TestExternalProject extends ListeningTestCase
{
    @Test
    public void testToFieldsMap()
    {
        ExternalProject externalProject = new ExternalProject();

        Map fieldsMap = externalProject.toFieldsMap();
    }
}
