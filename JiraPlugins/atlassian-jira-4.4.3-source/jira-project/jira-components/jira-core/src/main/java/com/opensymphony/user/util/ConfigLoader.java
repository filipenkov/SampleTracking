/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user.util;

import com.opensymphony.user.Configuration;
import com.opensymphony.user.provider.UserProvider;
import com.opensymphony.util.ClassLoaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * ConfigLoader parses the opensymphony-user.xml file, creates suitable instances of
 * providers, reads their properties and initializes them.
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.3 $
 * @see com.opensymphony.user.UserManager
 * @see com.opensymphony.user.provider.UserProvider
 */
@Deprecated
public class ConfigLoader extends DefaultHandler
{
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    //~ Instance fields ////////////////////////////////////////////////////////

    protected Properties currentProperties;
    protected String currentClass;
    protected Configuration.Builder configurationBuilder;

    //~ Methods ////////////////////////////////////////////////////////////////

    public synchronized Configuration load(final InputStream in, final Configuration.Builder configurationBuilder)
    {
        this.configurationBuilder = configurationBuilder;

        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Loading config");
            }

            final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(in, new ConfigHandler());
        }
        catch (final SAXException e)
        {
            logger.error("Could not parse config XML", e);
            throw new RuntimeException(e);
        }
        catch (final IOException e)
        {
            logger.error("Could not read config from stream", e);
            throw new RuntimeException(e);
        }
        catch (final ParserConfigurationException e)
        {
            logger.error("Could not obtain SAX parser", e);
            throw new RuntimeException(e);
        }
        catch (final RuntimeException e)
        {
            logger.error("RuntimeException", e);
            throw e;
        }
        catch (final Throwable e)
        {
            logger.error("Exception", e);
            throw new RuntimeException(e);
        }
        return configurationBuilder.toConfiguration();
    }

    private void addProvider()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("UserProvider class = " + currentClass + " " + currentProperties);
        }

        if (configurationBuilder != null)
        {
            try
            {
                UserProvider provider = (UserProvider) ClassLoaderUtil.loadClass(currentClass, this.getClass()).newInstance();

                if (provider.init(currentProperties))
                {
                    configurationBuilder.addProvider(provider);
                }
                else
                {
                    logger.error("Could not initialize provider " + currentClass);
                    throw new RuntimeException("Could not initialize provider " + currentClass);
                }
            }
            catch (final Exception e)
            {
                logger.error("Could not create instance of provider", e);
                throw new RuntimeException(e);
            }
        }
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    /**
     * SAX Handler implementation for handling tags in config file and building
     * config objects.
     */
    private class ConfigHandler extends DefaultHandler
    {
        private String _currentPropertyName;
        private StringBuffer _currentPropertyValue;

        public void characters(final char[] chars, final int offset, final int len) throws SAXException
        {
            if (_currentPropertyValue != null)
            {
                _currentPropertyValue.append(chars, offset, len);
            }
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException
        {
            if (qName.equals("provider"))
            {
                addProvider();
                currentProperties = null;
                currentClass = null;
            }
            else if (qName.equals("property"))
            {
                currentProperties.put(_currentPropertyName, _currentPropertyValue.toString());
                _currentPropertyName = null;
                _currentPropertyValue = null;
            }
        }

        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
        {
            if (qName.equals("provider") || qName.equals("authenticator"))
            {
                currentClass = attributes.getValue("class");
                currentProperties = new Properties();
            }
            else if (qName.equals("property"))
            {
                _currentPropertyName = attributes.getValue("name");
                _currentPropertyValue = new StringBuffer();
            }
        }
    }
}
