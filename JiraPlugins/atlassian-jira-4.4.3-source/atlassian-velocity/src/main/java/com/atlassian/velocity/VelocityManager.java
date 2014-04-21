/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Dec 6, 2002
 * Time: 10:51:18 AM
 * CVS Revision: $Revision: 1.3 $
 * Last CVS Commit: $Date: 2005/01/20 05:31:45 $
 * Author of last CVS Commit: $Author: sfarquhar $
 * To change this template use Options | File Templates.
 */
package com.atlassian.velocity;

import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

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
