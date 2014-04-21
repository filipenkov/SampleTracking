package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import org.easymock.EasyMock;

/**
 * @since v4.0
 */
public class TestComponentClauseXmlHandler extends MockControllerTestCase
{
    ComponentClauseXmlHandler componentClauseXmlHandler;

    @Before
    public void setUp() throws Exception
    {
        final FieldFlagOperandRegistry flagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        flagOperandRegistry.getOperandForFlag(EasyMock.isA(String.class), EasyMock.isA(String.class));
        mockController.setDefaultReturnValue(null);
        mockController.replay();
        componentClauseXmlHandler = new ComponentClauseXmlHandler(flagOperandRegistry);
    }

    @After
    public void tearDown() throws Exception
    {
        componentClauseXmlHandler = null;

    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        assertTrue(componentClauseXmlHandler.isSafeToNamifyValue());
    }

    @Test
    public void testXmlFieldIdSupported() throws Exception
    {
        assertTrue(componentClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forComponent().getIndexField()));
        assertFalse(componentClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forResolution().getIndexField()));
    }
}
