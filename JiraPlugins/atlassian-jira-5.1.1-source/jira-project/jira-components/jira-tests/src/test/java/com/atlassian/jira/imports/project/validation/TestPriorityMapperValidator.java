package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @since v3.13
 */
public class TestPriorityMapperValidator extends ListeningTestCase
{
   @Test
   public void testGetEntityDoesNotExistKey()
    {
        PriorityMapperValidator priorityMapperValidator = new PriorityMapperValidator();
        assertEquals("admin.errors.project.import.priority.validation.does.not.exist", priorityMapperValidator.getEntityDoesNotExistKey());
    }
}
