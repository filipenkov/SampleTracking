package com.atlassian.config.xml;

import com.atlassian.config.AbstractConfigurationPersister;
import com.atlassian.config.ConfigurationException;
import com.atlassian.core.util.Dom4jUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public abstract class AbstractDom4jXmlConfigurationPersister extends AbstractConfigurationPersister
{
    public static final Logger log = Logger.getLogger(AbstractDom4jXmlConfigurationPersister.class);
    private Document document;
    protected boolean useCData = false;

    public AbstractDom4jXmlConfigurationPersister()
    {
        //Create the document root
        clearDocument();

        //These define the mappings for the different objects stored in the config file
        addConfigMapping(String.class, Dom4jXmlStringConfigElement.class);
        addConfigMapping(Map.class, Dom4jXmlMapConfigElement.class);
        addConfigMapping(Map.Entry.class, Dom4jXmlMapEntryConfigElement.class);
        addConfigMapping(List.class, Dom4jXmlListConfigElement.class);
    }

    public Document loadDocument(File xmlFile) throws DocumentException, MalformedURLException
    {
        SAXReader xmlReader = new SAXReader();
        document = xmlReader.read(xmlFile);
        return document;
    }

    public Object load(InputStream istream) throws ConfigurationException
    {
        try
        {
            return loadDocument(istream);
        }
        catch (DocumentException e)
        {
            throw new ConfigurationException("Failed to load Xml doc: " + e.getMessage(), e);
        }
    }

    public Document loadDocument(InputStream istream) throws DocumentException
    {
        SAXReader xmlReader = new SAXReader();
        document = xmlReader.read(istream);
        return document;
    }

    public void save(String configPath, String configFile) throws ConfigurationException
    {
        saveDocument(configPath, configFile);
    }

    public void saveDocument(String configPath, String configFile) throws ConfigurationException
    {
        try
        {
            saveDocumentAtomically(getDocument(), configPath, configFile);
        }
        catch (IOException e)
        {
            throw new ConfigurationException("Couldn't save " + configFile + " to " + configPath + " directory.", e);
        }
    }

    private void saveDocumentAtomically(Document document, String configPath, String configFile) throws IOException
    {
        File tempFile = File.createTempFile(configFile, "tmp", new File(configPath));
        File saveFile = new File(configPath, configFile);

        try
        {
            Dom4jUtil.saveDocumentTo(document, configPath, tempFile.getName());

            // If the temp directory is on a different filesystem to the destination, the rename may fail on some
            // operating systems.
            if (!tempFile.renameTo(saveFile))
            {
                log.warn("Unable to move " + tempFile.getCanonicalPath() + " to " + saveFile.getCanonicalPath() + ". Falling back to non-atomic overwrite.");
                Dom4jUtil.saveDocumentTo(document, configPath, configFile);
            }
        }
        finally
        {
            tempFile.delete();
        }
    }

    public Document getDocument()
    {
        return document;
    }

    public Object getRootContext()
    {
        return document.getRootElement();
    }

    public Element getElement(String path)
    {
        return DocumentHelper.makeElement(document, path);
    }

    public void clear()
    {
        clearDocument();
    }

    private void clearDocument()
    {
        document = null;
        document = DocumentHelper.createDocument();
        document.addElement(getRootName());
    }

    public abstract String getRootName();

    public boolean isUseCData()
    {
        return useCData;
    }

    public void setUseCData(boolean useCData)
    {
        this.useCData = useCData;
    }
}
