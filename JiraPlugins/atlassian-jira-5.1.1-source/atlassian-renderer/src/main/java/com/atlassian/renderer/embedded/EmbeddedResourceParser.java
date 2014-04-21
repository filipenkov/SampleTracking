package com.atlassian.renderer.embedded;

import com.atlassian.renderer.util.FileTypeUtil;
import com.atlassian.renderer.util.UrlUtil;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.StringTokenizer;

/**
 * A parser for an embedded resource. Parsed format is as follows:
 * <p/>
 * originalText = "resource|properties"
 * resource = "url" | "internal"
 * internal = "((spaceKey:)pageTitle^)attachment"
 * properties = "property(, property)"
 * property = "key" | "key=value"
 */
public class EmbeddedResourceParser
{
    private static final Logger log = Logger.getLogger(EmbeddedResourceParser.class);

    /**
     * The original embedded resource text.
     */
    private String originalText;
    /**
     * The resource reference, either an external Url or an internal attachment reference.
     */
    private String resource;
    /**
     * The resource filename, with the location details stripped. ie: for both space:page^resource.ext and
     * http://www.host.com/path/to/resource.ext, the filename is resource.ext.
     */
    private String filename;
    /**
     * The page name for an internal resource, if one is specified.
     */
    private String page;
    /**
     * The space name for an internal resource, if one is specified.
     */
    private String space;
    /**
     * The list of properties specified for this resource. ie: everything after the '|'
     */
    private Properties properties;
    /**
     * Is this embedded resource external to the current confluence instance?
     */
    private boolean isExternal;

    /**
     * The mime-type of the embedded resource, or null if it is unknown.
     */
    private String type;
    protected static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
    protected static final String UNKNOWN_IMAGE_MIME_TYPE = "image/unknown";

    public EmbeddedResourceParser(String originalText)
    {
        this.originalText = originalText;
        parse(this.originalText);
    }

    private void parse(String s)
    {
        // split on the first '|', everything before is the resourceString, everything after is a property
        String resourceString;
        String propertiesString;

        int index = s.indexOf('|');
        if (index == -1)
        {
            resourceString = s;
            propertiesString = "";
        }
        else
        {
            resourceString = s.substring(0, index);
            propertiesString = s.substring(index + 1);
        }

        parseResource(resourceString);

        // properties are a comma delimited string where each property contains an optional '=value'
        properties = parseProperties(propertiesString);
    }

    private void parseResource(String resourceString)
    {
        this.resource = resourceString;

        // stepA: internal or external reference?
        if (UrlUtil.startsWithUrl(resourceString)) // external, just properties.
        {
            this.isExternal = true;

            String tempResourceString;

            // Get rid of everything before the filename, e.g. /blah/foo/filename.jpg
            tempResourceString = resourceString.substring(resourceString.lastIndexOf('/') + 1);

            // Strip out any parameters, e.g "filename.jpg?version=1" (CONF-4849)
            if (tempResourceString.indexOf("?") > -1)
                tempResourceString = tempResourceString.substring(0, tempResourceString.indexOf("?"));

            this.filename = tempResourceString;
        }
        else // internal. parse space, page, attachment name.
        {
            // need to extract space:page^resourceString from
            if (resourceString.indexOf(':') != -1)
            {
                space = resourceString.substring(0, resourceString.indexOf(':'));
                resourceString = resourceString.substring(resourceString.indexOf(':') + 1);
            }
            if (resourceString.indexOf('^') != -1)
            {
                page = resourceString.substring(0, resourceString.indexOf('^'));
                resourceString = resourceString.substring(resourceString.indexOf('^') + 1);
            }
            this.filename = resourceString;
        }

        type = FileTypeUtil.getContentType(filename);

        // CONF-6119 - unknown external resources are assumed to be images, because most of the time
        // that's what they are!
        if (isExternal && UNKNOWN_MIME_TYPE.equals(type))
            type = UNKNOWN_IMAGE_MIME_TYPE;
    }

    private Properties parseProperties(String parameterString)
    {
        Properties props = new Properties();
        StringTokenizer st = new StringTokenizer(parameterString, ",");
        while (st.hasMoreTokens())
        {
            String paramPair = st.nextToken();
            if (paramPair.indexOf('=') > 0)
            {
                String paramName = paramPair.substring(0, paramPair.indexOf('=')).trim();
                String paramValue = paramPair.substring(paramPair.indexOf('=') + 1).trim();
                if (paramValue.startsWith("\"") && paramValue.endsWith("\""))
                {
                    paramValue = paramValue.substring(1,paramValue.length()-1);
                }
                props.put(paramName, paramValue);

                // When the type has been specified as a property, also set the type for the parser (CONF-4906)
                if ("type".equals(paramName))
                    this.type = paramValue;
            }
            else
            {
                props.put(paramPair, ""); // the paramPair consists only of the param name here
            }
        }
        return props;
    }

    /**
     * @see #originalText
     */
    public String getOriginalText()
    {
        return originalText;
    }

    /**
     * @see #resource
     */
    public String getResource()
    {
        return resource;
    }

    /**
     * @see #page
     */
    public String getPage()
    {
        return page;
    }

    /**
     * @see #space
     */
    public String getSpace()
    {
        return space;
    }

    /**
     * @see #properties
     */
    public Properties getProperties()
    {
        Properties newProps = new Properties();
        newProps.putAll(properties);
        return newProps;
    }

    /**
     * @see #isExternal
     */
    public boolean isExternal()
    {
        return isExternal;
    }

    /**
     * @see #filename
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @see #type
     */
    public String getType()
    {
        return type;
    }
}
