package com.atlassian.velocity;

import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

import java.text.DateFormat;
import java.util.Map;

public interface VelocityManager
{
    String getBody(final String templateDirectory, String template, Map contextParameters) throws VelocityException;

    String getBody(final String templateDirectory, String template, String baseurl, Map contextParameters) throws VelocityException;

    String getEncodedBody(final String templateDirectory, String template, String encoding, Map contextParameters) throws VelocityException;

    String getEncodedBody(final String templateDirectory, String template, String baseurl, String encoding, Map contextParameters) throws VelocityException;

    String getEncodedBodyForContent(String content, String baseurl, Map contextParameters) throws VelocityException;

    DateFormat getDateFormat();

    String getEncodedBody(String templateDirectory, String template, String baseurl, String encoding, Context context) throws VelocityException;
}
