package com.atlassian.jira.service.util.handler;

import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MessageErrorHandler
{
    private static final Logger log = Logger.getLogger(MessageErrorHandler.class);
    private String errorMessage;
    private String exception;

    public MessageErrorHandler()
    {
        errorMessage = null;
        exception = null;
    }

    public boolean hasErrors()
    {
        return errorMessage != null || exception != null;
    }

    public String getError()
    {
        return errorMessage;
    }

    public String getException()
    {
        return exception;
    }

    public void setError(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public void setError(String errorMessage, Exception exception)
    {
        this.errorMessage = errorMessage;

//        StringBuffer exceptionString = new StringBuffer();
//
//        if (exception != null)
//        {
//            StackTraceElement[] stackTrace = exception.getStackTrace();
//            exceptionString.append(exception.toString() + "\n");
//
//            for (int i = 0; i < stackTrace.length; i++)
//            {
//                exceptionString.append("\t" + stackTrace[i].toString() + "\n");
//            }
//
//            Throwable cause = exception.getCause();
//            while (true)
//            {
//                if (cause == null)
//                    break;
//                StackTraceElement[] causeStackTrace = cause.getStackTrace();
//                exceptionString.append("Caused by: " + cause.toString() + "\n");
//                for (int i = 0; i < causeStackTrace.length; i++)
//                {
//                    exceptionString.append("\t" + causeStackTrace[i].toString() + "\n");
//                }
//                cause = cause.getCause();
//            }
//        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter p = new PrintWriter(stringWriter);
        exception.printStackTrace(p);
        p.flush();
        this.exception = stringWriter.toString();
    }
}
