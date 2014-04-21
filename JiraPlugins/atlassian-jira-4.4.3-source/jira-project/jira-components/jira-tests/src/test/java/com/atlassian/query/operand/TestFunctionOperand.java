package com.atlassian.query.operand;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit test for {@link TestFunctionOperand}.
 *
 * @since v4.0
 */
public class TestFunctionOperand extends ListeningTestCase
{
    @Test
    public void testDisplayString()
    {
        FunctionOperand functionOperand = new FunctionOperand("funkyFunction", CollectionBuilder.newBuilder("get", "funky", "with", "me").asList());
        assertEquals("funkyFunction(get, funky, with, me)", functionOperand.getDisplayString());
    }

    @Test
    public void testDisplayStringNoArgs()
    {
        FunctionOperand functionOperand = new FunctionOperand("weGotDaFunk", Collections.<String>emptyList());
        assertEquals("weGotDaFunk()", functionOperand.getDisplayString());
    }

    @Test
    public void testConstructorNullName()
    {
        try
        {
            new FunctionOperand(null, Collections.<String>emptyList());
            fail("Expected IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testConstructorNullArguments()
    {
        try
        {
            new FunctionOperand("test", (List<String>)null);
            fail("Expected IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testName() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand("weGotDaFunk", Collections.<String>emptyList());
        assertEquals("weGotDaFunk", functionOperand.getName());
    }

    @Test
    public void testArgs() throws Exception
    {
        final List<String> strings = Arrays.asList("test", "value");
        FunctionOperand functionOperand = new FunctionOperand("weGotDaFunk", strings);
        assertEquals(2, functionOperand.getArgs().size());
        assertTrue(strings.contains(functionOperand.getArgs().get(0)));
        assertTrue(strings.contains(functionOperand.getArgs().get(1)));
    }

    @SuppressWarnings ({ "ObjectEqualsNull" })
    @Test
    public void testEquals() throws Exception
    {
        FunctionOperand function1 = new FunctionOperand("nAME");
        FunctionOperand function2 = new FunctionOperand("name");
        FunctionOperand function3 = new FunctionOperand("name", "asdfgh");
        FunctionOperand function4 = new FunctionOperand("NAMe", "asdfgh");

        assertTrue(function1.equals(function2));
        assertTrue(function2.equals(function1));
        assertFalse(function3.equals(function1));
        assertFalse(function1.equals(function3));
        assertFalse(function3.equals(function2));

        assertTrue(function3.equals(function4));
        assertTrue(function4.equals(function3));

        assertFalse(function1.equals(null));
    }

    @Test
    public void testHashCode() throws Exception
    {
        FunctionOperand function1 = new FunctionOperand("nAME");
        FunctionOperand function2 = new FunctionOperand("name");
        FunctionOperand function3 = new FunctionOperand("name", "asdfgh");
        FunctionOperand function4 = new FunctionOperand("NAMe", "asdfgh");

        assertEquals(function1.hashCode(), function2.hashCode());
        assertEquals(function3.hashCode(), function4.hashCode());
    }
}
