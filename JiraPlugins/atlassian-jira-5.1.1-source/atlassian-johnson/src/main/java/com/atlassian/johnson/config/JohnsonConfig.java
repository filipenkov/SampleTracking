package com.atlassian.johnson.config;

import com.atlassian.johnson.event.*;
import com.atlassian.johnson.setup.ContainerFactory;
import com.atlassian.johnson.setup.DefaultContainerFactory;
import com.atlassian.johnson.setup.SetupConfig;
import com.atlassian.johnson.Initable;
import com.atlassian.seraph.util.PathMapper;
import com.opensymphony.util.ClassLoaderUtil;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.*;

/**
 * The configuration class for Johnson! :)
 */
public class JohnsonConfig
{
    private static final Category log = Category.getInstance(JohnsonConfig.class);
    private static JohnsonConfig instance;

    private static final String DEFAULT_CONFIGURATION_FILE = "johnson-config.xml";

    private String configurationFile;
    private SetupConfig setupConfig;
    private ContainerFactory containerFactory;
    private List eventChecks;
    private List requestEventChecks;
    private List applicationEventChecks;
    private Map eventChecksById;
    private Map params;
    private String setupPath;
    private String errorPath;
    private PathMapper ignoreMapper;
    private Map eventLevels;
    private Map eventTypes;
    private List ignorePaths;

    synchronized public static JohnsonConfig getInstance()
    {
        if (instance == null)
        {
            try
            {
                instance = new JohnsonConfig(DEFAULT_CONFIGURATION_FILE);
            }
            catch (ConfigurationException e)
            {
                log.error("Could not configure JohnsonConfig instance: " + e, e);
            }
        }

        return instance;
    }

    public JohnsonConfig(String configurationFile) throws ConfigurationException
    {
        this.configurationFile = configurationFile;
        init();
    }

    public List getEventChecks()
    {
        return eventChecks;
    }

    public List getRequestEventChecks()
    {
        return requestEventChecks;
    }

    public List getApplicationEventChecks()
    {
        return applicationEventChecks;
    }

    public EventCheck getEventCheck(int id)
    {
        return (EventCheck) eventChecksById.get(id);
    }

    public Map getParams()
    {
        return params;
    }

    public SetupConfig getSetupConfig()
    {
        return setupConfig;
    }

    public ContainerFactory getContainerFactory()
    {
        return containerFactory;
    }

    public String getSetupPath()
    {
        return setupPath;
    }

    public String getErrorPath()
    {
        return errorPath;
    }

    public List getIgnorePaths()
    {
        return ignorePaths;
    }

    public boolean isIgnoredPath(String uri)
    {
        return ignoreMapper.get(uri) != null;
    }

    public EventLevel getEventLevel(String level)
    {
        return (EventLevel) eventLevels.get(level);
    }

    public EventType getEventType(String type)
    {
        return (EventType) eventTypes.get(type);
    }

    private void init() throws ConfigurationException
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            URL fileUrl = ClassLoaderUtil.getResource(configurationFile, this.getClass());

            if (fileUrl == null)
                throw new IllegalArgumentException("No such XML file:" + configurationFile);

            // Parse document
            org.w3c.dom.Document doc = factory.newDocumentBuilder().parse(fileUrl.toString());
            Element rootEl = doc.getDocumentElement();

            setupConfig = (SetupConfig) configureClass(rootEl, "setupconfig");
            containerFactory = (ContainerFactory) configureClass(rootEl, "container-factory");
            // If no container factory was specified then fall back to the old behaviour by default
            if (containerFactory == null)
            {
                containerFactory = new DefaultContainerFactory();
            }
            configureEventChecks(rootEl);
            eventLevels = configureEventConstants(rootEl, "eventlevels", "eventlevel");
            eventTypes = configureEventConstants(rootEl, "eventtypes", "eventtype");
            params = configureParameters(rootEl);
            setupPath = (String) configurePaths(rootEl, "setup").get(0);
            errorPath = (String) configurePaths(rootEl, "error").get(0);
            ignorePaths = configurePaths(rootEl, "ignore");

            // now cache the ignore paths in our mapper
            ignoreMapper = new PathMapper();
            for (Iterator iterator = ignorePaths.iterator(); iterator.hasNext();)
            {
                String path = (String) iterator.next();
                ignoreMapper.put(path, path);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ConfigurationException("Exception configuring: " + e);
        }
    }

    private Map configureEventConstants(Element rootEl, String tagname, String childname)
    {
        NodeList nl = rootEl.getElementsByTagName(tagname);

        if (nl != null && nl.getLength() > 0)
        {
            Element element = (Element) nl.item(0);
            NodeList children = element.getElementsByTagName(childname);
            Map result = new HashMap(children.getLength());

            for (int i = 0; i < children.getLength(); i++)
            {
                Element child = (Element) children.item(i);
                String key = child.getAttribute("key");
                String description = getContainedText(child, "description");

                if (childname.equals("eventlevel"))
                {
                    result.put(key, new EventLevel(key, description));
                }
                else if (childname.equals("eventtype"))
                {
                    result.put(key, new EventType(key, description));
                }
            }

            return result;
        }

        return Collections.EMPTY_MAP;
    }

    private List configurePaths(Element rootEl, String tagname)
    {
        NodeList nl = rootEl.getElementsByTagName(tagname);

        if (nl != null && nl.getLength() > 0)
        {
            Element element = (Element) nl.item(0);
            NodeList children = element.getElementsByTagName("path");
            List result = new ArrayList(children.getLength());

            for (int i = 0; i < children.getLength(); i++)
            {
                Element child = (Element) children.item(i);
                result.add(((Text) child.getFirstChild()).getData().trim());
            }

            return result;
        }

        return Collections.EMPTY_LIST;
    }

    private Map configureParameters(Element rootEl)
    {
        NodeList nl = rootEl.getElementsByTagName("parameters");

        if (nl.getLength() > 0)
        {
            Element parametersEl = (Element) nl.item(0);
            return getInitParameters(parametersEl);
        }

        return Collections.EMPTY_MAP;
    }

    private Initable configureClass(Element rootEl, String tagname) throws ConfigurationException
    {
        try
        {
            NodeList elementList = rootEl.getElementsByTagName(tagname);

            for (int i = 0; i < elementList.getLength(); i++)
            {
                Element authEl = (Element) elementList.item(i);
                String clazz = authEl.getAttribute("class");

                Initable initable = (Initable) ClassLoaderUtil.loadClass(clazz, this.getClass()).newInstance();
                Map params = getInitParameters(authEl);
                initable.init(params);
                return initable;
            }
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Could not create: " + tagname + ": ", e);
        }

        return null;
    }


    private void configureEventChecks(Element rootEl) throws ConfigurationException
    {
        NodeList nl = rootEl.getElementsByTagName("eventchecks");

        if (nl != null && nl.getLength() > 0)
        {
            eventChecks = new LinkedList();
            requestEventChecks = new LinkedList();
            applicationEventChecks = new LinkedList();
            eventChecksById = new HashMap();

            Element eventChecksEl = (Element) nl.item(0);
            NodeList eventCheckList = eventChecksEl.getElementsByTagName("eventcheck");

            for (int i = 0; i < eventCheckList.getLength(); i++)
            {
                Element eventCheckEl = (Element) eventCheckList.item(i);
                String eventCheckClazz = eventCheckEl.getAttribute("class");

                if (eventCheckClazz == null || "".equals(eventCheckClazz))

                    throw new ConfigurationException("eventcheck element with bad class attribute");

                Object o = null;
                try
                {
                    log.debug("Adding eventcheck of class: " + eventCheckClazz);
                    o = ClassLoaderUtil.loadClass(eventCheckClazz, this.getClass()).newInstance();
                }
                catch (Exception e)
                {
                    log.error(e);
                    throw new ConfigurationException("Could not create eventcheck: " + eventCheckClazz + ". Exception: " + e);
                }

                EventCheck eventCheck = null;
                if (o instanceof EventCheck)
                {
                    eventCheck = (EventCheck) o;
                    eventChecks.add(eventCheck);
                }
                else
                {
                    throw new ConfigurationException("Eventcheck " + eventCheckClazz + " does not implement EventCheck interface.");
                }

                Map params = getInitParameters(eventCheckEl);
                eventCheck.init(params);

                if (eventCheck instanceof RequestEventCheck)
                {
                    requestEventChecks.add(eventCheck);
                }

                if (eventCheck instanceof ApplicationEventCheck)
                {
                    applicationEventChecks.add(eventCheck);
                }

                String eventCheckId = eventCheckEl.getAttribute("id");
                if (eventCheckId != null && !"".equals(eventCheckId))
                {
                    try
                    {
                        Integer id = Integer.valueOf(eventCheckId);
                        if (!eventChecksById.containsKey(id))
                        {
                            eventChecksById.put(id, eventCheck);
                        }
                        else
                        {
                            throw new ConfigurationException("Duplicate eventcheck id '" + id + "'.");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ConfigurationException("Eventcheck id must be an integer.", e);
                    }
                }
            }
        }
        else
        {
            eventChecks = Collections.EMPTY_LIST;
            requestEventChecks = Collections.EMPTY_LIST;
            applicationEventChecks = Collections.EMPTY_LIST;
            eventChecksById = Collections.EMPTY_MAP;
        }
    }

    private Map getInitParameters(Element el)
    {
        Map params = new HashMap();

        NodeList nl = el.getElementsByTagName("init-param");

        for (int i = 0; i < nl.getLength(); i++)
        {
            Node initParam = nl.item(i);
            String paramName = getContainedText(initParam, "param-name");
            String paramValue = getContainedText(initParam, "param-value");
            params.put(paramName, paramValue);
        }

        return params;
    }

    public static void setInstance(JohnsonConfig johnsonConfig)
    {
        instance = johnsonConfig;
    }

    private String getContainedText(Node parent, String childTagName)
    {
        try
        {
            Node tag = ((Element) parent).getElementsByTagName(childTagName).item(0);
            return ((Text) tag.getFirstChild()).getData();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
