package com.atlassian.jira.action.admin;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.license.LicenseStringFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.task.TaskProgressSink;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Sax handler for constructing GenericValues from data in XML format.
 */
public class OfbizImportHandler extends DefaultHandler
{
    private static final Logger log = Logger.getLogger(OfbizImportHandler.class);

    static final String OSPROPERTY_STRING = "OSPropertyString";
    static final String OSPROPERTY_TEXT = "OSPropertyText";
    static final String OSPROPERTY_ENTRY = "OSPropertyEntry";
    static final String OSPROPERTY_NUMBER = "OSPropertyNumber";

    private static final String ENTITY_ENGINE_XML = "entity-engine-xml";
    private static final String SQL_STATE_DEADLOCK = "40001";
    private static final int MAX_SQL_DEADLOCK_RETRIES = 3;
    private final OfBizDelegator ofBizDelegator;
    private final Executor executor;
    private TaskProgressSink taskProgressSink = TaskProgressSink.NULL_SINK;
    private final LicenseStringFactory licenseStringFactory;
    private final IndexPathManager indexPathManager;
    private final AttachmentPathManager attachmentPathManager;
    private final Map<String, String> osPropertyStringMap = new HashMap<String, String>();
    private final Map<String, String> osPropertyTextMap = new HashMap<String, String>();
    private final Map<String, String> osPropertyNumberMap = new HashMap<String, String>();
    private final AtomicReference<Throwable> importError = new AtomicReference<Throwable>();

    private final Map<String, String> licenseIds = new HashMap<String, String>();
    private StringBuffer textBuffer;
    private boolean hasRootElement = false;

    private String inEntity = null;

    private GenericValue value;
    // A flag to indicate whether to create entities in the database or just parse them
    private boolean createEntities;

    private boolean useDefaultIndexPath;
    private boolean useDefaultAttachmentPath;
    private long entityCount;
    private String buildNumberId = null;
    private String buildNumber = null;
    private String licenseString;
    private String indexPath;
    private String indexPathId;
    private String attachmentPath;
    private String attachmentPathId;
    private String indexDefaultId;
    private String attachmentDefaultId;
    private boolean useDefaultPaths;

    OfbizImportHandler(final OfBizDelegator ofBizDelegator, final Executor executor, final LicenseStringFactory licenseStringFactory, final IndexPathManager indexPathManager, final AttachmentPathManager attachmentPathManager)
    {
        this(ofBizDelegator, executor, licenseStringFactory, indexPathManager, attachmentPathManager, false);
    }

    public OfbizImportHandler(final OfBizDelegator ofBizDelegator, final Executor executor, final LicenseStringFactory licenseStringFactory,
            final IndexPathManager indexPathManager, final AttachmentPathManager attachmentPathManager, final boolean useDefaultPaths)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.executor = executor;
        this.licenseStringFactory = notNull("licenseStringFactory", licenseStringFactory);
        this.indexPathManager = indexPathManager;
        this.attachmentPathManager = attachmentPathManager;
        createEntities = false;
        this.useDefaultPaths = useDefaultPaths;
    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================
    @Override
    public void startDocument() throws SAXException
    {
        log.debug("Starting Document");
        entityCount = 0;
    }

    @Override
    public void endDocument() throws SAXException
    {
        if (hasRootElement)
        {
            throw new SAXException("XML file ended too early.  There was no </entity-engine-xml> tag.");
        }
        createBuildNumber();
        createLicenseString();
        createIndexDefault();
        createAttachmentDefault();
        createIndexPath();
        createAttachmentPath();
        log.debug("Ending Document");
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException
    {
        //if we have an exception - refuse to process any more nodes, and throw the exception
        if (importError.get() != null)
        {
            throw new SAXException(new NestableException(importError.get()));
        }

        if (hasRootElement)
        {
            if (inEntity != null)
            {
                if (value == null)
                {
                    throw new SAXException("Somehow we have got inside an Entity without creating a GenericValue for it.");
                }
                else
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Read opening subelement " + qName + " of entity " + value.getEntityName());
                    }
                    // It will contain the contents of the tag when it is closed.
                    textBuffer = null;
                }
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Read opening " + qName + " element");
                }
                // Then we must be looking at a GenericValue element,
                // Construct a new one and set its attributes
                inEntity = qName;
                value = ofBizDelegator.makeValue(qName);
                final ModelEntity modelEntity = value.getModelEntity();
                for (final Iterator i = modelEntity.getFieldsIterator(); i.hasNext();)
                {
                    final ModelField modelField = (ModelField) i.next();
                    final String name = modelField.getName();
                    final String attr = attributes.getValue(name);

                    if (attr != null)
                    {
                        try
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("Setting attribute " + name + " with value " + attr);
                            }
                            value.setString(name, attr);
                        }
                        catch (RuntimeException e)
                        {
                            log.error("Failed to set attribute '" + qName + "." + name + "' with value '" + attr + "'. Error: " + e.getMessage());
                            //no need to for dumping the stacktrace here since it will get dumped further up in the stack.
                            throw e;
                        }
                    }
                }
                /**
                 * We look ahead here for build numbers and licenses strings and we are making he assumption that they are
                 * only in attribute values for a given entity.  This will break in the future if they end up
                 * being in CDATA sections (eg they have new lines in the data values)
                 */
                recordElementsInfo(qName, attributes);
                // JRADEV-2376 Need to store default path if using custom paths , the path doesn't exist
                // and the user says use defaultpaths

                if (isPropertyString(qName))
                {
                    setDefaultPaths(attributes);
                }
                if (isPropertyNumber(qName))
                {
                    setUseDefaultPaths(attributes);
                }
            }
        }
        else if (ENTITY_ENGINE_XML.equals(qName))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Read opening ROOT element");
            }
            // Set that the document has started correctly
            hasRootElement = true;
        }
        else
        {
            throw new SAXException("The XML document does not contain the <entity-engine-xml> root element or it was closed too early.");
        }
    }

    private void setDefaultPaths(final Attributes attributes)
    {
        if (useDefaultPaths && attributes.getValue("id").equals(indexPathId))
        {
            value.setString("value", indexPathManager.getDefaultIndexRootPath());
        }
        if (useDefaultPaths && attributes.getValue("id").equals(attachmentPathId))
        {
            value.setString("value", attachmentPathManager.getDefaultAttachmentPath());
        }
    }

    private void setUseDefaultPaths(final Attributes attributes)
    {
        if (useDefaultPaths && attributes.getValue("id").equals(indexDefaultId))
        {
            value.setString("value", "1");
        }
        if (useDefaultPaths && attributes.getValue("id").equals(attachmentDefaultId))
        {
            value.setString("value", "1");
        }
    }

    /**
     * Sets the buildNumber property based on the collected property key and value found in the xml. The buildNumber
     * will be set to null if this info couldn't be properly collected (i.e. it was absent).
     */
    void createBuildNumber()
    {
        if (buildNumberId != null)
        {
            buildNumber = osPropertyStringMap.get(buildNumberId);
        }
    }

    private void createAttachmentDefault()
    {
        if (useDefaultPaths)
        {
            useDefaultAttachmentPath = true;
        }
        else
        {
            if (attachmentDefaultId != null)
            {
                useDefaultAttachmentPath = "1".equals(osPropertyNumberMap.get(attachmentDefaultId));
            }
        }
    }

    private void createIndexDefault()
    {
        if (useDefaultPaths)
        {
            useDefaultIndexPath = true;
        }
        else
        {
            if (indexDefaultId != null)
            {
                useDefaultIndexPath = "1".equals(osPropertyNumberMap.get(indexDefaultId));
            }
        }
    }

    void createIndexPath()
    {
        if (useDefaultIndexPath)
        {
            indexPath = indexPathManager.getDefaultIndexRootPath();
        }
        else if (indexPathId != null)
        {
            indexPath = osPropertyStringMap.get(indexPathId);
        }
    }

    void createAttachmentPath()
    {
        if (useDefaultAttachmentPath)
        {
            attachmentPath = attachmentPathManager.getDefaultAttachmentPath();
        }
        else if (attachmentPathId != null)
        {
            attachmentPath = osPropertyStringMap.get(attachmentPathId);
        }
    }

    void createLicenseString()
    {
        if (licenseIds.containsKey(APKeys.JIRA_LICENSE))
        {
            licenseString = osPropertyTextMap.get(licenseIds.get(APKeys.JIRA_LICENSE));
        }
        else if (licenseIds.containsKey(APKeys.JIRA_LICENSE_V1_HASH) && licenseIds.containsKey(APKeys.JIRA_LICENSE_V1_MESSAGE))
        {
            final String hash = osPropertyTextMap.get(licenseIds.get(APKeys.JIRA_LICENSE_V1_HASH));
            final String msg = osPropertyTextMap.get(licenseIds.get(APKeys.JIRA_LICENSE_V1_MESSAGE));
            licenseString = licenseStringFactory.create(msg, hash);
        }
        else if (licenseIds.containsKey(APKeys.JIRA_OLD_LICENSE_V1_HASH) && licenseIds.containsKey(APKeys.JIRA_OLD_LICENSE_V1_MESSAGE))
        {
            final String hash = osPropertyStringMap.get(licenseIds.get(APKeys.JIRA_OLD_LICENSE_V1_HASH));
            final String msg = osPropertyStringMap.get(licenseIds.get(APKeys.JIRA_OLD_LICENSE_V1_MESSAGE));
            licenseString = licenseStringFactory.create(msg, hash);
        }
    }

    /**
     * Looks at the given elements for build number information and licensing.
     * <p/>
     * We're looking for the OSPropertyEntry with the right propertyKey that references an OSPropertyString (linked by
     * id) which contains the build number value.
     *
     * @param qName element qName
     * @param attributes attributes of the element.
     */
    void recordElementsInfo(final String qName, final Attributes attributes)
    {
        recordProperties(qName, attributes, OSPROPERTY_STRING, osPropertyStringMap);
        recordProperties(qName, attributes, OSPROPERTY_TEXT, osPropertyTextMap);
        recordProperties(qName, attributes, OSPROPERTY_NUMBER, osPropertyNumberMap);

        if (isPropertyEntry(qName, attributes, APKeys.JIRA_PATCHED_VERSION))
        {
            buildNumberId = attributes.getValue("id");
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_LICENSE))
        {
            licenseIds.put(APKeys.JIRA_LICENSE, attributes.getValue("id"));
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_LICENSE_V1_HASH))
        {
            licenseIds.put(APKeys.JIRA_LICENSE_V1_HASH, attributes.getValue("id"));
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_LICENSE_V1_MESSAGE))
        {
            licenseIds.put(APKeys.JIRA_LICENSE_V1_MESSAGE, attributes.getValue("id"));
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_OLD_LICENSE_V1_HASH))
        {
            licenseIds.put(APKeys.JIRA_OLD_LICENSE_V1_HASH, attributes.getValue("id"));
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_OLD_LICENSE_V1_MESSAGE))
        {
            licenseIds.put(APKeys.JIRA_OLD_LICENSE_V1_MESSAGE, attributes.getValue("id"));
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_PATH_INDEX))
        {
            indexPathId = attributes.getValue("id");
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY))
        {
            indexDefaultId = attributes.getValue("id");
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_PATH_ATTACHMENTS))
        {
            attachmentPathId = attributes.getValue("id");
        }
        else if (isPropertyEntry(qName, attributes, APKeys.JIRA_PATH_ATTACHMENTS_USE_DEFAULT_DIRECTORY))
        {
            attachmentDefaultId = attributes.getValue("id");
        }

    }

    private void recordProperties(final String qName, final Attributes attributes, final String entityName, final Map<String, String> store)
    {
        if (entityName.equals(qName))
        {
            final String id = attributes.getValue("id");
            final String value = attributes.getValue("value");
            if ((id != null) && (value != null))
            {
                store.put(id, value);
            }
        }
    }

    private boolean isPropertyEntry(final String qName, final Attributes attributes, final String property)
    {
        return StringUtils.equals(OSPROPERTY_ENTRY, qName)
                && StringUtils.equals(property, attributes.getValue("propertyKey"))
                && StringUtils.isNotBlank(attributes.getValue("id"));
    }

    private boolean isPropertyString(final String qName)
    {
        return StringUtils.equals(OSPROPERTY_STRING, qName);
    }

    private boolean isPropertyNumber(final String qName)
    {
        return StringUtils.equals(OSPROPERTY_NUMBER, qName);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
        if (hasRootElement)
        {
            if (ENTITY_ENGINE_XML.equals(qName))
            {
                log.debug("Read closing ROOT element");
                hasRootElement = false;
            }
            else
            {
                if (inEntity == null)
                {
                    throw new SAXException("There is no entity set");
                }
                else
                {
                    if (inEntity.equals(qName))
                    {
                        log.debug("Read closing " + qName + " element");
                        entityCount++;
                        // We have closed the entity so you can add the generic value to the list of entities and reset the current one.
                        if (createEntities)
                        {
                            createValue(value);
                            taskProgressSink.makeProgress(entityCount, "data.import.store.entities", "data.import.store.entities.progress");
                        }

                        value = null;
                        inEntity = null;
                    }
                    else
                    {
                        log.debug("Read closing subelement " + qName + " of entity " + value.getEntityName());
                        if (textBuffer != null)
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("Setting attribute " + qName + " with value " + textBuffer.toString());
                            }
                            // Do not trim the textBuffer as we need to preserve the leading and trailing space of the attribute
                            value.setString(qName, textBuffer.toString());
                            textBuffer = null;
                        }
                    }
                }
            }
        }
        else
        {
            throw new SAXException("How did we get here an exception should already have been thrown");
        }
    }

    private void createValue(final GenericValue value)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                try
                {
                    createWithDeadlockRetry(value);
                }
                catch (final Error e)
                {
                    log.error("Exception importing entity: " + e, e);
                    importError.set(e);
                    throw e;
                }
                catch (final Exception e)
                {
                    log.error("Exception importing entity: " + e, e);
                    importError.set(new DataAccessException(e));
                }
            }
        });
    }

    /**
     * Saves the row to the database and retries if a deadlock occurs.
     *
     * @param value GenericValue to save
     * @throws GenericEntityException if the save fails.
     */
    void createWithDeadlockRetry(GenericValue value) throws GenericEntityException
    {
        // This can deadlock and needs to be retried under some rare circumstances.
        // Probably when under load and an index page lock escalation occurs.
        boolean deadlocked;
        int attempts = 0;
        do
        {
            deadlocked = false;
            try
            {
                value.create();
            }
            catch (GenericEntityException e)
            {
                attempts++;
                String sqlState = getSqlState(e);
                if (attempts <= MAX_SQL_DEADLOCK_RETRIES && sqlState != null && sqlState.equals(SQL_STATE_DEADLOCK))
                {
                    deadlocked = true;
                }
                else
                {
                    throw e;
                }
            }
        }
        while (deadlocked);
    }

    private static String getSqlState(GenericEntityException e)
    {
        // SQL State may be nested at any depth in a wrapped chain of GenericEntityExceptions
        Throwable ex = e.getNested();
        while (ex != null)
        {
            if (ex instanceof SQLException)
            {
                return ((SQLException) ex).getSQLState();
            }
            else if (ex instanceof GenericEntityException)
            {
                ex = ((GenericEntityException) ex).getNested();
            }
            else
            {
                ex = null;
            }
        }
        return null;
    }

    @Override
    public void characters(final char ch[], final int start, final int length) throws SAXException
    {
        final String s = new String(ch, start, length);
        if (textBuffer == null)
        {
            textBuffer = new StringBuffer(s);
        }
        else
        {
            textBuffer.append(s);
        }
    }

    public void setCreateEntities(final boolean createEntities)
    {
        this.createEntities = createEntities;
    }

    public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
    {
        this.taskProgressSink = taskProgressSink;
    }

    public long getEntityCount()
    {
        return entityCount;
    }

    /**
     * @return the build number as read from the xml file or null if none found.
     */
    public String getBuildNumber()
    {
        return buildNumber;
    }

    public String getLicenseString()
    {
        return licenseString;
    }

    public Throwable getImportError()
    {
        return importError.get();
    }

    public boolean isUseDefaultIndexPath()
    {
        return useDefaultIndexPath;
    }

    public boolean isUseDefaultAttachmentPath()
    {
        return useDefaultAttachmentPath;
    }

    public String getIndexPath()
    {
        return indexPath;
    }

    public String getAttachmentPath()
    {
        return attachmentPath;
    }
}
