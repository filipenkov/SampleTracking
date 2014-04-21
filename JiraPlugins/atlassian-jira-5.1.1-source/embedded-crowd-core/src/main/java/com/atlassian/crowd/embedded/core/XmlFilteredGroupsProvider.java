package com.atlassian.crowd.embedded.core;

import com.atlassian.core.util.ClassLoaderUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Group Provider implementation which reads from xml configuration file.
 */
public class XmlFilteredGroupsProvider implements FilteredGroupsProvider
{
    private final FilteredGroupsFileReader fileReader;

    /**
     * This constructor makes use of the default reader which reads from classpath.
     */
    public XmlFilteredGroupsProvider()
    {
        // by default, read from classpath.
        fileReader = new ClassPathFilteredGroupsFileReader();
    }

    public XmlFilteredGroupsProvider(FilteredGroupsFileReader fileReader)
    {
        this.fileReader = fileReader;
    }

    public Set<String> getGroups()
    {
        // read the configuration file.
        final InputStream stream = fileReader.getStream();

        // if the file is not present, simply there is no group to be filtered.
        if (stream == null)
        {
            return Collections.emptySet();
        }

        final SAXReader reader = new SAXReader();
        Document doc;

        try
        {
            doc = reader.read(stream);
        }
        catch (DocumentException e)
        {
            throw new IllegalStateException("Cannot read filtered group file. make sure it is well-formed.", e);
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }

        return parse(doc);
    }

    private Set<String> parse(Document doc)
    {
        // this will keep the result
        final Set<String> groups = new HashSet<String>();

        // read from /filteredgroups/filteredgroup
        final Element root = doc.getRootElement();
        if (root.getName().equals("filteredgroups"))
        {
            for(Iterator i = root.elements().iterator(); i.hasNext();)
            {
                Element elem = (Element)i.next();
                if (elem.getName().equals("filteredgroup"))
                {
                    groups.add(elem.getTextTrim());
                }
            }
        }

        return groups;
    }

    static interface FilteredGroupsFileReader
    {
        InputStream getStream();
    }

    static class ClassPathFilteredGroupsFileReader implements FilteredGroupsFileReader
    {
        // The default file name which the implementation will look for.
        private static final String FILTERED_GROUPS_FILE = "crowd-filtered-groups.xml";

        private String getFilename()
        {
            return System.getProperty("crowd.service.filteredgroups.file", FILTERED_GROUPS_FILE);
        }

        public InputStream getStream()
        {
            return ClassLoaderUtils.getResourceAsStream(getFilename(), this.getClass());
        }
    }
}
