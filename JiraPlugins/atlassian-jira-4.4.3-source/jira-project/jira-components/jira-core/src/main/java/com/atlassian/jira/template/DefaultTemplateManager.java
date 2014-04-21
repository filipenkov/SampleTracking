/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.template;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.scheme.SchemeEntity;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager for the email velocity templates within the system.
 * <p/>
 * Currently, the system initialises the tempalte table by reading from the email-template-id-mappings.xml file.
 * Custom templates can be added to this file - ensuring that an appropriate ID is given to the new template (e.g. >= 10000).
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MT_CORRECTNESS", justification="TODO Needs to be fixed")
public class DefaultTemplateManager implements TemplateManager
{
    private static final Logger log = Logger.getLogger(DefaultTemplateManager.class);

    private final ApplicationProperties applicationProperties;
    private final EventTypeManager eventTypeManager;

    private static final String templateIDMappingConfigFile = "email-template-id-mappings.xml";
    private static final String EMAIL_TEMPLATES = "templates/email/";

    // Map: template ids -> template objects
    private Map templatesMap;

    // Map: template ids -> template file name
    private Map templateFileMap;

    public DefaultTemplateManager(ApplicationProperties applicationProperties, EventTypeManager eventTypeManager)
    {
        this.applicationProperties = applicationProperties;
        this.eventTypeManager = eventTypeManager;
    }

    // ---- Template Retrieval Methods ---------------------------------------------------------------------------------
    public Template getTemplate(Long templateId)
    {
        if (templatesMap == null)
        {
            getAllTemplatesMap();
        }

        return (Template) templatesMap.get(templateId);
    }

    /**
     * Retrieve the template from the notification scheme entity.
     * <p/>
     * If the scheme entity is not related to a template id - return the default template for the event type.
     * The eventTypeId corresponds to the default template id if there is no specific template association.
     *
     * @param notificationSchemeEntity a notification scheme entity object
     */
    public Template getTemplate(SchemeEntity notificationSchemeEntity)
    {
        Template template;

        // Check if the notification scheme entity has overriden the default template for this event type.
        Long templateId = (Long) notificationSchemeEntity.getTemplateId();

        if (templateId == null)
        {
            Long eventTypeId = (Long) notificationSchemeEntity.getEntityTypeId();
            EventType eventType = eventTypeManager.getEventType(eventTypeId);

            if (eventTypeId == null || eventType == null)
            {
                log.error("Unable to determine the email template for the notification scheme entity : " + notificationSchemeEntity.getId() + ".");
                throw new DataAccessException("Unable to determine the email template for the notification scheme entity : " + notificationSchemeEntity.getId() + ".");
            }
            else
            {
                // Return default template for the event type
                template = getDefaultTemplate(eventType);
            }
        }
        else
        {
            template = getTemplate(templateId);
        }

        return template;
    }

    /**
     * Retrieve the default template for specified event type.
     *
     * @param eventType event type to retrieve the default template for
     * @return template default template for specified event type
     */
    public Template getDefaultTemplate(EventType eventType)
    {
        return getTemplate(eventType.getTemplateId());
    }

    /**
     * Retieve all the templates within the system
     * <p/>
     * The templates are read from the email-template-id-mappings.xml file if not already loaded and cached for future
     * reference.
     * <p/>
     *
     * @return a collection of all Template objects from the database
     */
    private Collection getAllTemplates()
    {
        return new ArrayList(getAllTemplatesMap().values());
    }

    /**
     * Retrieve a map of templates of the specified type.
     *
     * @param type String specifiying the type of template to retrieve
     * @return Map  template IDs -> template objects
     */
    public Map getTemplatesMap(String type)
    {
        Map templatesOfType = new ConcurrentHashMap();
        Collection allTemplates = getAllTemplates();

        for (Iterator iterator = allTemplates.iterator(); iterator.hasNext();)
        {
            Template template = (Template) iterator.next();
            if (type != null && type.equals(template.getType()))
            {
                templatesOfType.put(template.getId(), template);
            }
        }
        return templatesOfType;
    }

    /**
     * Retrieve a map: template Ids -> template objects.
     * <p/>
     * Used within template select lists
     * <p/>
     * This method is synchronized as it lazy loads the template ids, names and filenames when first called.
     *
     * @return map  template ids -> template objects
     */
    private synchronized Map getAllTemplatesMap()
    {
        if (templatesMap == null)
        {
            initTemplates();
        }
        return templatesMap;
    }

    public String getTemplateContent(Long templateId, String format)
    {
        String templateContent = null;

        String templateFileName = getTemplateFilename(templateId);
        String resourceName = EMAIL_TEMPLATES + format + "/" + templateFileName;

        BufferedReader reader = null;
        try
        {

            reader = new BufferedReader(new InputStreamReader(ClassLoaderUtils.getResourceAsStream(resourceName, this.getClass()), applicationProperties.getString(APKeys.JIRA_WEBWORK_ENCODING)));
            StringWriter stringWriter = new StringWriter();

            char buf[] = new char[1024];
            int len;
            while ((len = reader.read(buf, 0, 1024)) != -1)
            {
                stringWriter.write(buf, 0, len);
            }
            templateContent = stringWriter.toString();

        }
        catch (Exception e)
        {
            log.error("Problem retrieving template [" + resourceName + "]", e);
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (IOException e)
            {
                log.error("Could not close the file '" + resourceName + "'.");
            }
        }

        return templateContent;
    }

    /**
     * Retrieve the velocity template associated with the specified templateId
     *
     * @param templateId template ID
     * @return filename of the velocity template
     */
    private String getTemplateFilename(Long templateId)
    {
        if (templateFileMap == null)
        {
            // Ensure templates are loaded
            getAllTemplates();
        }

        return (String) templateFileMap.get(templateId);
    }

    // ---- Helper methods --------------------------------------------------------------------------------------------
    /**
     * Initialize the templates as read from the file email-template-id-mappings.xml file.
     * <p/>
     * Templates not stored in the database yet - template details should be stored in the email-template-id-mappings.xml file.
     * <p/>
     * Ensure calls to this method are synchronized as the templates are lazy loaded.
     */
    private void initTemplates()
    {
        templatesMap = new ListOrderedMap();
        templateFileMap = new HashMap();

        InputStream is = ClassLoaderUtils.getResourceAsStream(templateIDMappingConfigFile, DefaultTemplateManager.class);
        try
        {
            Document doc = new Document(is);
            Element root = doc.getRoot();
            Elements actions = root.getElements("templatemapping");

            while (actions.hasMoreElements())
            {
                Element action = (Element) actions.nextElement();
                Long id = new Long(action.getAttributeValue("id"));
                String name = action.getElement("name").getTextString();
                String templateFile = action.getElement("template").getTextString();
                String templateType = action.getElement("templatetype").getTextString();

                // Add a template object to the templates map
                Template template = new Template(id, name, null, null, null, templateType);
                templatesMap.put(id, template);

                // Update the templateFileMap for each entry
                templateFileMap.put(id, templateFile);
            }
        }
        catch (ParseException e)
        {
            log.error("Error parsing " + templateIDMappingConfigFile + ": " + e, e);
        }

        try
        {
            is.close();
        }
        catch (IOException e)
        {
            log.warn("Could not close template id mappings inputStream.", e);
        }
    }
}
