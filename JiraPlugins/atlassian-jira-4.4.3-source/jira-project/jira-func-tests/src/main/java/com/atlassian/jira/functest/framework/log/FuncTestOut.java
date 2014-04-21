package com.atlassian.jira.functest.framework.log;

import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * We need to write output to a PrintStream that we know goes to file.  Maven 1, in its infinite wisdom, holds test
 * output including System.out calls in memory.  If all hell breaks loose, the extra generated output (such as exceptions
 * and so on) will be kept in memory and LOST if the process runs out of memory.
 * <p/>
 * We need to put it in a file in this case AS we go so its available for debugging reasons.
 * <p/>
 * So dont write <b>System.out.println()</b> but rather <b>FuncTestOut.out.println()<b/>;
 * <p/>
 *
 * @since v4.0
 */
public class FuncTestOut
{
    /**
     * This {@link java.io.PrintStream} is tee'ed to write to {@link System#out} and to a Maven aware output filec
     * called JiraFuncTests.out.log
     */
    public final static PrintStream out = new PrintStream(new EnvironmentDetectingPrintStream("JiraFuncTests.out.log", System.out));
    /**
     * This {@link java.io.PrintStream} is tee'ed to write to {@link System#err} and to a Maven aware output filec
     * called JiraFuncTests.out.log
     */
    public final static PrintStream err = new PrintStream(new EnvironmentDetectingPrintStream("JiraFuncTests.err.log", System.err));


    /**
     * This will log to a StringBuffer first if need be and replace the new lines with "indent + newline"
     *
     * @param logData the data to log
     */
    public static void log(Object logData)
    {
        FuncTestLoggerImpl.logIndented(logData);
    }

    /**
     * This will log to a StringBuffer first if need be and replace the new lines with "indent + newline"
     *
     * @param indentLevel the indented level required
     * @param logData the data to log
     */
    public static void log(int indentLevel, Object logData)
    {
        FuncTestLoggerImpl.logIndented(indentLevel, logData);
    }

    private static class EnvironmentDetectingPrintStream extends OutputStream
    {
        private final OutputStream fileOutputStream;
        private final PrintStream systemPrintStream;

        private EnvironmentDetectingPrintStream(String fileName, PrintStream systemPrintStream)
        {
            FileOutputStream fileOut;
            String targetFileName = EnvironmentUtils.getMavenAwareOutputDir() + File.separator + fileName;
            try
            {
                fileOut = new FileOutputStream(targetFileName, false);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace(System.err);
                fileOut = null;
            }
            this.fileOutputStream = fileOut;
            this.systemPrintStream = systemPrintStream;
        }

        /*
        * SOMETHING TO THINK ABOUT
         *
         * If Maven continues its mmemory hungry System.out memory caching then maybe
         * we will want to stop writing to it.  Just to teach it a lesson.  But we want
         * to while in IDEA.
         */

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException
        {
            if (fileOutputStream != null)
            {
                fileOutputStream.write(b, off, len);
            }
            systemPrintStream.write(b, off, len);
        }

        @Override
        public void write(final byte[] b) throws IOException
        {
            if (fileOutputStream != null)
            {
                fileOutputStream.write(b);
            }
            systemPrintStream.write(b);
        }

        @Override
        public void write(final int b) throws IOException
        {
            if (fileOutputStream != null)
            {
                fileOutputStream.write(b);
            }
            systemPrintStream.write(b);
        }

        @Override
        public void flush() throws IOException
        {
            if (fileOutputStream != null)
            {
                fileOutputStream.flush();
            }
            systemPrintStream.flush();
        }

        /**
         * At least have a go at trying to close the file
         *
         * @throws Throwable according to method signature
         */
        @Override
        protected void finalize() throws Throwable
        {
            super.finalize();
            if (fileOutputStream != null)
            {
                fileOutputStream.close();
            }
        }
    }
}
