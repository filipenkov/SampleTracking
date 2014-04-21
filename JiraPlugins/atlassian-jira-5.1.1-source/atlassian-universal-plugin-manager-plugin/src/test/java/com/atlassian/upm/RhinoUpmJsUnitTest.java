package com.atlassian.upm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

@RunWith(RhinoUpmJsUnitTest.RhinoJsUnitRunner.class)
public class RhinoUpmJsUnitTest
{
    /**
     * Override the default Junit4 runner class to get the {RunNotifier} before testing.
     */
    public static class RhinoJsUnitRunner extends BlockJUnit4ClassRunner
    {
        public RhinoJsUnitRunner(java.lang.Class<?> klass) throws InitializationError
        {
            super(klass);
        }

        @Override
        public void run(RunNotifier notifier)
        {
            runNotifier = notifier;
            super.run(notifier);
        }
    }

    /**
     * A passed JsUnit testcase prints a line starts with this.
     */
    private static String START_TEST_CASE_PREFIX = "-- Running test";

    /**
     * A failed JsUnit testcase prints a line starts with this.
     */
    private static String FAILED_TEST_CASE_PREFIX = "FAILURE in ";
    private static String ERROR_TEST_CASE_PREFIX = "ERROR in ";
    private static String COMPLETED_TEST_SUITE_PREFIX = " <= Completed";

    /**
     * Current executing JsUnit testcase.
     */
    private static Description currentTest = null;

    /**
     * Use this notifier to programatically add passed/failed tests.
     */
    protected static RunNotifier runNotifier;

    /**
     * Execute JsUnit tests using Rhino in its own process.
     * Since Rhino seems to  alway call System.exit() no matter whether the \
     * tests fails, I create a separate process for it.
     *
     * @throws java.io.IOException
     */
    private static void runJsUnitInRhino(String entryPoint) throws IOException
    {
        // build Rhino command-line
        final StringBuilder command = new StringBuilder();
        command.append("java -cp ");
        command.append(System.getProperty("java.class.path"));
        command.append(" org.mozilla.javascript.tools.shell.Main ");
        command.append(entryPoint);

        // create a Rhino process
        final Process proc = Runtime.getRuntime().exec(command.toString());
        final BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        // receive test results
        String lineResult = null;
        while ((lineResult = reader.readLine()) != null)
        {
            System.out.println(lineResult);
            checkJsUnitResultByLine(lineResult);
        }
    }

    /**
     * Get the string between two double-quotes in a line.
     *
     * @param line The one-line String with quoted text.
     * @return The text between tow double-quotes.
     */
    private static String getTextBetweenQuotes(String line)
    {
        final int start = line.indexOf("\"") + 1;
        final int end = line.lastIndexOf("\"");
        if (start >= end)
        {
            return "";
        }
        else
        {
            return line.substring(start, end);
        }
    }

    /**
     * A line-by-line parser for JsUnit output result.
     */
    private static void checkJsUnitResultByLine(String line)
    {

        if (line.startsWith(FAILED_TEST_CASE_PREFIX) || line.startsWith(ERROR_TEST_CASE_PREFIX))
        {
            fail(line);
        }
        else
        {
            // ignore any other lines except from our rhino's test result writer.
            if (currentTest != null && (line.startsWith(COMPLETED_TEST_SUITE_PREFIX) || line.startsWith(START_TEST_CASE_PREFIX)))
            {
                pass();
            }

            if (line.startsWith(START_TEST_CASE_PREFIX))
            {
                currentTest = Description.createTestDescription(RhinoUpmJsUnitTest.class, getTextBetweenQuotes(line));
                runNotifier.fireTestStarted(currentTest);
            }
        }
    }

    /**
     * Finishing current JsUnit testcase as passed.
     */
    private static void pass()
    {
        if (currentTest != null)
        {
            runNotifier.fireTestFinished(currentTest);
            currentTest = null;
        }
    }

    /**
     * Finishing current JsUnit testcase as failed.
     *
     * @param details JsUnit provided failure details.
     */
    private static void fail(String details)
    {
        if (currentTest != null)
        {
            Failure failure = new Failure(currentTest, new RuntimeException(details));
            runNotifier.fireTestFailure(failure);
            currentTest = null;
        }
    }

    /**
     * This is a JsUnit test trigger. It will execute all the JsUnit testcases
     * using Rhino and fire standard JUnit4 test pass/fail signal based on
     * JsUnit's output result.
     *
     * @throws IOException
     */
    @Test
    public void JsUnitTriggerTest() throws IOException
    {
        runJsUnitInRhino(System.getProperty("jsunit.entrypoint"));
    }
}
