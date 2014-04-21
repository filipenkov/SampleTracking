package com.atlassian.mail.config;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.core.util.XMLUtils;
import com.atlassian.mail.server.MailServerManager;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 9, 2002
 * Time: 2:44:02 PM
 * To change this template use Options | File Templates.
 */
public class ConfigLoader
{
    private static final Category log = Category.getInstance(ConfigLoader.class);
    private static final String DEFAULT_CONFIG_FILE = "mail-config.xml";
    private MailServerManager loadedManager;

    public ConfigLoader(String file)
    {
        //This is a bit of a hack for JBOSS
        InputStream is = ClassLoaderUtils.getResourceAsStream(file, ConfigLoader.class);
        try
        {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xmlDoc = db.parse(is);
            Element root = xmlDoc.getDocumentElement();
            Element manager = (Element) root.getElementsByTagName("manager").item(0);
            Class aClass = ClassLoaderUtils.loadClass(manager.getAttribute("class"), this.getClass());
            MailServerManager msm = (MailServerManager) aClass.newInstance();
            Map params = new HashMap();
            NodeList properties = manager.getElementsByTagName("property");
            if (properties.getLength() > 0)
            {
                for (int i = 0; i < properties.getLength(); i++)
                {
                    Element property = (Element) properties.item(i);
                    String name = XMLUtils.getContainedText(property, "name");
                    String value = XMLUtils.getContainedText(property, "value");
                    params.put(name, value);
                }
            }
            msm.init(params);
            setLoadedManager(msm);
        }
        catch (Exception e)
        {
            log.fatal(e, e);
            throw new RuntimeException("Error in mail config: " + e.getMessage(), e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e2)
            {
                log.error(e2);
            }
        }
    }

    public static MailServerManager getServerManager()
    {
        return getServerManager(DEFAULT_CONFIG_FILE);
    }

    public static MailServerManager getServerManager(String file)
    {
        ConfigLoader configLoader = new ConfigLoader(file);
        return configLoader.getLoadedManager();
    }

    public MailServerManager getLoadedManager()
    {
        return loadedManager;
    }

    public void setLoadedManager(MailServerManager loadedManager)
    {
        this.loadedManager = loadedManager;
    }
}
