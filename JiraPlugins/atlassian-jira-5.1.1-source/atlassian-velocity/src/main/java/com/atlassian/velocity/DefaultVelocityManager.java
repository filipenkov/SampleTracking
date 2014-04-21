package com.atlassian.velocity;

import com.atlassian.core.util.ClassLoaderUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;

import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultVelocityManager implements VelocityManager
{
    private VelocityEngine ve;
    private static final Logger log = Logger.getLogger(DefaultVelocityManager.class);

    public DefaultVelocityManager()
    {
        /**
         * Initialise Velocity
         */
    }

    public String getBody(final String templateDirectory, String template, Map contextParameters) throws VelocityException
    {
        return getEncodedBody(templateDirectory, template, null, null, contextParameters);
    }

    public String getBody(final String templateDirectory, String template, String baseurl, Map contextParameters) throws VelocityException
    {
        return getEncodedBody(templateDirectory, template, baseurl, null, contextParameters);
    }

    public String getEncodedBody(final String templateDirectory, String template, String encoding, Map contextParameters) throws VelocityException
    {
        return getEncodedBody(templateDirectory, template, null, encoding, contextParameters);
    }

    public String getEncodedBody(final String templateDirectory, String template, String baseurl, String encoding, Map contextParameters) throws VelocityException
    {
        return getEncodedBody(templateDirectory, template, baseurl, encoding, createVelocityContext(createContextParams(baseurl, contextParameters)));
    }

    public String getEncodedBody(final String templateDirectory, String template, String baseurl, String encoding, Context context) throws VelocityException
    {
        //Check to see if the template exists
        if (template == null)
            throw new VelocityException("Trying to send mail with no template.");

        try
        {
            StringWriter writer = new StringWriter();

            if (encoding == null)
                getVe().mergeTemplate(templateDirectory + template, context, writer);
            else
                getVe().mergeTemplate(templateDirectory + template, encoding, context, writer);

            return writer.toString();
        }
        catch (ResourceNotFoundException e)
        {
            log.error("ResourceNotFoundException occurred whilst loading resource " + template);
            URL templateUrl = ClassLoaderUtils.getResource(templateDirectory + template, this.getClass());
            if (templateUrl == null)
                throw new VelocityException("Could not find template '" + templateDirectory + template + "' ensure it is in the classpath.");
            return "Could not locate resource " + templateDirectory + template;

        }
        catch (MethodInvocationException mie)
        {
            Throwable e = mie.getWrappedThrowable();
            log.error("MethodInvocationException occurred getting message body from Velocity: " + e, e);
            return getErrorMessageForException(mie);
        }
        catch (Exception e)
        {
            log.error("Exception getting message body from Velocity: " + e, e);
            return getErrorMessageForException(e);
        }
    }

    protected Map createContextParams(String baseurl, Map contextParameters)
    {
        Map params = new HashMap();
        params.put("baseurl", baseurl);
        params.put("formatter", getDateFormat());
        params.putAll(contextParameters);
        return params;
    }

    protected VelocityContext createVelocityContext(Map params)
    {
        if (params != null)
        {
            params.put("ctx", params);
        }

        return new VelocityContext(params)
        {
            //this overrides the functionality that doesn't allow null values to be set in the context
            public Object put(String key, Object value)
            {
                if (key == null)
                     return null;
                else
                    return internalPut(key, value);
            }
        };
    }

    public String getEncodedBodyForContent(String content, String baseurl, Map contextParameters) throws VelocityException
    {
        //Check to see if the content is not null
        if (content == null)
            throw new VelocityException("Trying to send mail with no content.");

        try
        {
            VelocityContext context = createVelocityContext(createContextParams(baseurl, contextParameters));
            StringWriter writer = new StringWriter();

            getVe().evaluate(context, writer, "getEncodedBodyFromContent", content);

            return writer.toString();
        }
        catch (Exception e)
        {
            log.error("Exception getting message body from Velocity: " + e, e);
            return getErrorMessageForException(e);
        }
    }

    protected String getErrorMessageForException(Exception e)
    {
        return "An error occurred whilst rendering this message.  Please contact the administrators, and inform them of this bug.\n\nDetails:\n-------\n" + StringEscapeUtils.escapeHtml(ExceptionUtils.getFullStackTrace(e));
    }

    public DateFormat getDateFormat()
    {
        return new SimpleDateFormat("EEE, d MMM yyyy h:mm a");
    }

    protected synchronized VelocityEngine getVe()
    {
        if (ve == null)
        {
            ve = new VelocityEngine();
            initVe(ve);
        }

        return ve;
    }

    protected void initVe(VelocityEngine velocityEngine)
    {
       try
        {
            Properties props = new Properties();

            try
            {

                props.load(ClassLoaderUtils.getResourceAsStream("velocity.properties", getClass()));
            }
            catch (Exception e)
            {
                //log.warn("Could not configure DefaultVelocityManager from velocity.properties, manually configuring.");
                props.put("resource.loader", "class");
                props.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
                props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            }

            velocityEngine.init(props);
        }
        catch (Exception e)
        {
            log.error("Exception initialising Velocity: " + e, e);
        }
    }
}
