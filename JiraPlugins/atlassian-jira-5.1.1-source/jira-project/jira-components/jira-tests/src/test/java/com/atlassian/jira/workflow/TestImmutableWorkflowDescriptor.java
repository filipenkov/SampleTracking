package com.atlassian.jira.workflow;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

/**
 * Test ImmutableWorkflowDescriptor
 *
 * @since v3.13
 */
public class TestImmutableWorkflowDescriptor extends ListeningTestCase
{
    @Test
    public void testSettersThrowUnsupportedException() throws InvocationTargetException, IllegalAccessException
    {
        ImmutableWorkflowDescriptor immutableWorkflowDescriptor = new ImmutableWorkflowDescriptor(null);
        try
        {
            immutableWorkflowDescriptor.setId(2);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.setEntityId(2);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.setName("Tommy");
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.setParent(null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.setTriggerFunction(3, null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }
    }

    @Test
    public void testMutatorsThrowUnsupportedException() throws InvocationTargetException, IllegalAccessException
    {
        ImmutableWorkflowDescriptor immutableWorkflowDescriptor = new ImmutableWorkflowDescriptor(null);
        try
        {
            immutableWorkflowDescriptor.addCommonAction(null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.addGlobalAction(null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.addInitialAction(null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.addJoin(null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.addSplit(null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.addStep(null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        try
        {
            immutableWorkflowDescriptor.init(null);
            fail("This immutable object should throw UnsupportedOperationException on setter methods.");
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }
    }
}
