package com.atlassian.core.util;

import com.opensymphony.util.TextUtils;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class LocaleUtils
{
    private static final Category log = Category.getInstance(LocaleUtils.class);

    public static final String LANGUAGE_DESCRIPTOR_FILENAME = "language-descriptor.xml";
    public static final String LOCALE_TAG_NAME = "locale";

    public List installedLocales;


    /**
     * This method locates all installed language bundles that are found on the classpath by looking for
     * {@link #LANGUAGE_DESCRIPTOR_FILENAME} files on the classpath, and parsing the value of the
     * {@link #LOCALE_TAG_NAME} element.
     * 
     * @throws IOException when problems arise retrieving resources from the classpath
     */
    public List getInstalledLocales() throws IOException
    {
        if (installedLocales == null)
        {
            installedLocales = new ArrayList();
            URL url = null;
            final Enumeration localeDescriptors = ClassLoaderUtils.getResources(LANGUAGE_DESCRIPTOR_FILENAME, this.getClass());
            while (localeDescriptors.hasMoreElements())
            {
                try
                {
                    url = (URL) localeDescriptors.nextElement();
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document xmlDoc = db.parse(url.openConnection().getInputStream());
                    Element root = xmlDoc.getDocumentElement();
                    String locale = XMLUtils.getContainedText(root, LOCALE_TAG_NAME);
                    if (TextUtils.stringSet(locale))
                    {
                        installedLocales.add(getLocale(locale));
                    }
                    else
                    {
                        throw new IllegalArgumentException("The " + LOCALE_TAG_NAME + " element must be set in " + url);
                    }
                }
                catch (IOException e)
                {
                    log.error("Error while reading language descriptor '" + url + "'.", e);
                }
                catch (ParserConfigurationException e)
                {
                    log.error("Error while parsing language descriptor '" + url + "'.", e);
                }
                catch (SAXException e)
                {
                    log.error("Error while parsing language descriptor '" + url + "'.", e);
                }
            }
            // Sort the locales by their display names
            Collections.sort(installedLocales, new LocaleComparator());
        }

        return installedLocales;
    }

    /**
     * Creates a locale from the given string.
     * 
     * @param locale 
     * @return 
     */
    public Locale getLocale(String locale)
    {
        if (TextUtils.stringSet(locale))
        {
            int _pos = locale.indexOf("_");
            if (_pos != -1)
            {
                return new Locale(locale.substring(0, _pos), locale.substring(_pos + 1));
            }
            else
            {
                return new Locale(locale, "");
            }
        }
        return null;
    }
}
