package com.atlassian.rpc.jsonrpc;

import com.atlassian.voorhees.ApplicationException;
import junit.framework.TestCase;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.stub;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestSoapModuleMethodMapper extends TestCase
{
    @Mock SimpleSoapService simpleSoapService;
    @Mock AuthenticatedSoapService authenticatedSoapService;

    private SoapModuleMethodMapper simpleMapper;
    private SoapModuleMethodMapper authenticatedMapper;

    @Override
    protected void setUp() throws Exception
    {
        initMocks(this);
        simpleMapper = new SoapModuleMethodMapper(simpleSoapService, SimpleSoapService.class, false);
        authenticatedMapper = new SoapModuleMethodMapper(authenticatedSoapService, AuthenticatedSoapService.class, true);
    }

    @Override
    protected void tearDown() throws Exception
    {
        simpleMapper = null;
    }

    public void testSillyNegatives() throws Exception
    {
        testSillyNegatives(simpleMapper);
        testSillyNegatives(authenticatedMapper);
    }

    public void testNoArgMethodSimple() throws Exception
    {
        stub(simpleSoapService.getCheese()).toReturn("Stilton");
        testNoArgMethod(simpleMapper);
    }

    public void testNoArgMethodAuthenticated() throws Exception
    {
        stub(authenticatedSoapService.getCheese("")).toReturn("Stilton");
        testNoArgMethod(authenticatedMapper);
    }

    public void testOneArgSimple() throws Exception
    {
        stub(simpleSoapService.getCheeseByName("George")).toReturn("Stilton");
        testOneArg(simpleMapper);
    }

    public void testOneArgAuthenticated() throws Exception
    {
        stub(authenticatedSoapService.getCheeseByName("", "George")).toReturn("Stilton");
        testOneArg(authenticatedMapper);
    }

    public void testOverloadedMethodSimple() throws Exception
    {
        stub(simpleSoapService.getOverloadedCheese()).toReturn("Cheddar");
        stub(simpleSoapService.getOverloadedCheese("George")).toReturn("Stilton");
        stub(simpleSoapService.getOverloadedCheese("George", 1234L)).toReturn("Gruyere");

        testOverloadedMethod(simpleMapper);
    }

    public void testOverloadedMethodAuthenticated() throws Exception
    {
        stub(authenticatedSoapService.getOverloadedCheese("")).toReturn("Cheddar");
        stub(authenticatedSoapService.getOverloadedCheese("", "George")).toReturn("Stilton");
        stub(authenticatedSoapService.getOverloadedCheese("", "George", 1234L)).toReturn("Gruyere");

        testOverloadedMethod(authenticatedMapper);
    }

    public void testClashingMethodSimple() throws Exception
    {
        stub(simpleSoapService.getClashingCheese(1234L, "George")).toReturn("Cheddar");
        stub(simpleSoapService.getClashingCheese("George", 1234L)).toReturn("Stilton");

        testClashingMethod(simpleMapper);
    }

    public void testClashingMethodAuthenticated() throws Exception
    {
        stub(authenticatedSoapService.getClashingCheese("", 1234L, "George")).toReturn("Cheddar");
        stub(authenticatedSoapService.getClashingCheese("", "George", 1234L)).toReturn("Stilton");

        testClashingMethod(authenticatedMapper);
    }

    public void testApplicationException() throws Exception
    {
        stub(simpleSoapService.getCheese()).toThrow(new RuntimeException("Yogurt"));
        stub(simpleSoapService.getExceptionalCheese()).toThrow(new Exception("Milk"));

        try
        {
            simpleMapper.call("getCheese", new Class[0], new Object[0]);
        }
        catch (ApplicationException e)
        {
            assertEquals("Yogurt", e.getCause().getMessage());
        }

        try
        {
            simpleMapper.call("getExceptionalCheese", new Class[0], new Object[0]);
        }
        catch (ApplicationException e)
        {
            assertEquals("Milk", e.getCause().getMessage());
        }
    }

    public void testClashingMethod(SoapModuleMethodMapper mapper) throws Exception
    {
        assertTrue(mapper.methodExists("getClashingCheese"));
        assertTrue(mapper.methodExists("getClashingCheese", 2));
        assertFalse(mapper.methodExists("getClashingCheese", 0));

        List<Class[]> args = mapper.getPossibleArgumentTypes("getClashingCheese", 2);
        assertEquals(2, args.size());

        // Java sucks. Arrays don't have a useful impl of equals(), and Arrays.asList returns something you can't
        // remove elements from. Doing a simple "does this list contain both of these elements in any order" becomes
        // a nightmare.
        Class[][] expectedArgsArr = {
                {Long.TYPE, String.class},
                {String.class, Long.TYPE}};

        List<Class[]> expectedArgs = new ArrayList<Class[]>(Arrays.asList(expectedArgsArr));

        for (Class[] arg : args)
        {
            for (Iterator<Class[]> iterator = expectedArgs.iterator(); iterator.hasNext();)
            {
                Class[] expectedArg = iterator.next();
                if (Arrays.equals(arg, expectedArg))
                {
                    iterator.remove();
                }
            }
        }

        assertTrue("All args present and correct", expectedArgs.isEmpty());

        assertEquals("Cheddar", mapper.call("getClashingCheese", expectedArgsArr[0], new Object[] { 1234L, "George"}));
        assertEquals("Stilton", mapper.call("getClashingCheese", expectedArgsArr[1], new Object[] {"George", 1234L}));
    }

    private void testOverloadedMethod(SoapModuleMethodMapper mapper) throws Exception
    {
        assertTrue(mapper.methodExists("getOverloadedCheese"));
        assertTrue(mapper.methodExists("getOverloadedCheese", 0));
        assertTrue(mapper.methodExists("getOverloadedCheese", 1));
        assertTrue(mapper.methodExists("getOverloadedCheese", 2));
        assertFalse(mapper.methodExists("getOverloadedCheese", 3));

        List<Class[]> zeroArgs = mapper.getPossibleArgumentTypes("getOverloadedCheese", 0);
        assertEquals(1, zeroArgs.size());
        assertTrue(Arrays.equals(new Class[0], zeroArgs.get(0)));

        List<Class[]> oneArg = mapper.getPossibleArgumentTypes("getOverloadedCheese", 1);
        assertEquals(1, oneArg.size());
        assertTrue(Arrays.equals(new Class[] { String.class }, oneArg.get(0)));

        List<Class[]> twoArgs = mapper.getPossibleArgumentTypes("getOverloadedCheese", 2);
        assertEquals(1, twoArgs.size());
        assertTrue(Arrays.equals(new Class[] { String.class, Long.TYPE }, twoArgs.get(0)));


        assertEquals("Cheddar", mapper.call("getOverloadedCheese", new Class[0], new Object[0]));
        assertEquals("Stilton", mapper.call("getOverloadedCheese", new Class[] { String.class }, new Object[] { "George"}));
        assertEquals("Gruyere", mapper.call("getOverloadedCheese",
                new Class[] { String.class, Long.TYPE }, new Object[] { "George", 1234L}));
    }

    private void testOneArg(SoapModuleMethodMapper mapper) throws Exception
    {
        assertTrue(mapper.methodExists("getCheeseByName"));
        assertTrue(mapper.methodExists("getCheeseByName", 1));
        assertFalse(mapper.methodExists("getCheeseByName", 0));

        List<Class[]> methodTypes = mapper.getPossibleArgumentTypes("getCheeseByName", 1);
        assertEquals(1, methodTypes.size());
        assertTrue(Arrays.equals(new Class[] { String.class }, methodTypes.get(0)));

        assertEquals("Stilton", mapper.call("getCheeseByName", new Class[] { String.class }, new Object[] { "George"}));
    }

    private void testSillyNegatives(SoapModuleMethodMapper mapper) throws Exception
    {
        assertFalse(mapper.methodExists("getMonkey"));
        assertFalse(mapper.methodExists("getMonkey", 1));

        try
        {
            mapper.getPossibleArgumentTypes("getMonkey", 1);
        }
        catch (IllegalStateException s)
        {
            // expected
        }

        try
        {
            mapper.call("getMonkey", new Class[0], new Object[0]);
        }
        catch (NoSuchMethodException e)
        {
            // expected
        }
    }

    private void testNoArgMethod(SoapModuleMethodMapper mapper) throws Exception
    {
        assertTrue(mapper.methodExists("getCheese"));
        assertTrue(mapper.methodExists("getCheese", 0));
        assertFalse(mapper.methodExists("getCheese", 1));

        List<Class[]> methodTypes = mapper.getPossibleArgumentTypes("getCheese", 0);
        assertEquals(1, methodTypes.size());
        assertEquals(0, methodTypes.get(0).length);

        assertEquals("Stilton", mapper.call("getCheese", new Class[0], new Object[0]));
    }
}
