package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @since v3.13
 */
public class TestResolutionMapperValidator extends ListeningTestCase
{
   @Test
   public void testGetEntityDoesNotExistKey()
    {
        ResolutionMapperValidator resolutionMapperValidator = new ResolutionMapperValidator();
        assertEquals("admin.errors.project.import.resolution.validation.does.not.exist", resolutionMapperValidator.getEntityDoesNotExistKey());
    }
}
