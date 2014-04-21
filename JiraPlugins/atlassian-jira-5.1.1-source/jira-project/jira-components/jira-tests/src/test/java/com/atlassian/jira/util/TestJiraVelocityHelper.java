package com.atlassian.jira.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import org.ofbiz.core.entity.GenericEntityException;

/**
 * Unit test for JiraVelocityHelper.
 */
public class TestJiraVelocityHelper extends ListeningTestCase
{

    private static final JiraVelocityHelper VELOCITY_HELPER = new JiraVelocityHelper(null);

    /**
     * This is a simple test that makes sure that this method will not explode when passed a null
     * GenericValue.
     */
    @Test
    public void testWasDeletedHandlesNull()
    {
        try
        {
            assertFalse(VELOCITY_HELPER.wasDeleted(null, "fake value", null));
        }
        catch (GenericEntityException e)
        {
            fail();
        }
    }

}
