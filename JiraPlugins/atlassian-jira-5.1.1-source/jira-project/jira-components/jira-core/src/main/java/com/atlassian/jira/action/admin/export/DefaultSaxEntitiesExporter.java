package com.atlassian.jira.action.admin.export;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.startup.FormattedLogMsg;
import com.atlassian.jira.startup.JiraSystemInfo;
import com.atlassian.jira.util.BuildUtilsInfo;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityListIterator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelViewEntity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicLong;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultSaxEntitiesExporter implements EntitiesExporter
{
    public static final int DEFAULT_BUFFER_SIZE = 32768;
    protected static final String NL = System.getProperty("line.separator");
    private static final Logger log = Logger.getLogger(DefaultSaxEntitiesExporter.class);

    private final DelegatorInterface delegator;
    private final ApplicationProperties applicationProperties;
    private final BuildUtilsInfo buildUtilsInfo;

    public DefaultSaxEntitiesExporter(final DelegatorInterface delegator, final ApplicationProperties applicationProperties, final BuildUtilsInfo buildUtilsInfo)
    {
        this.delegator = notNull("delegator", delegator);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public long exportEntities(final OutputStream outputStream, final SortedSet<String> entityNames, final EntityXmlWriter entityWriter, final User exportingUser) throws IOException, GenericEntityException
    {
        long numberWritten = 0;
        final EntityCounter entityCounter = new EntityCounter();
        // Ensure that we use JIRA's encoding for output
        // Buffer 32KB at a time
        final PrintWriter printWriter = getWriter(outputStream);
        writeHeader(printWriter, exportingUser);

        try
        {
            for (final String curEntityName : entityNames)
            {
                log.debug("curEntityName = " + curEntityName);

                final ModelEntity modelEntity = delegator.getModelReader().getModelEntity(curEntityName);
                // Export only normal (non-view) entities
                if (!(modelEntity instanceof ModelViewEntity))
                {
                    EntityListIterator listIterator = null;
                    try
                    {
                        listIterator = delegator.findListIteratorByCondition(curEntityName, null, null, null);
                        GenericValue genericValue = listIterator.next();
                        // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
                        // if there are any results left in the iterator is to iterate over it until null is returned
                        // (i.e. not use hasNext() method)
                        // The documentation mentions efficiency only - but the functionality is totally broken when using
                        // hsqldb JDBC drivers (hasNext() always returns true).
                        // So listen to the OfBiz folk and iterate until null is returned.
                        while (genericValue != null)
                        {
                            entityWriter.writeXmlText(genericValue, printWriter);
                            numberWritten++;
                            entityCounter.increment(curEntityName);
                            genericValue = listIterator.next();
                        }
                    }
                    finally
                    {
                        if (listIterator != null)
                        {
                            // Close the iterator
                            listIterator.close();
                        }
                    }
                }
                else
                {
                    log.debug("No need to export entity '" + curEntityName + "' as it is a view entity.");
                }
            }
            writeFooter(printWriter, entityCounter);

        }
        finally
        {
            // Do NOT close the writer here!!! The writer is constructed over a stream that is passed in. It is the responsibility
            // of the calling code to close the stream (as teh caller must have opened it). If we close the writrer here, it will close the
            // underlying stream, and when the caller tries to close the stream they might get an exception. See JRA-4964 (Only fails under jdk 1.3)
            // Flush the buffer so that all the contents is written.
            if (printWriter != null)
            {
                printWriter.flush();
            }
        }
        return numberWritten;
    }

    protected PrintWriter getWriter(final OutputStream outputStream) throws UnsupportedEncodingException
    {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, applicationProperties.getEncoding()), DEFAULT_BUFFER_SIZE));
    }

    /**
     * This closes out the XML documment.
     *
     * @param printWriter a PrintWriter to write to
     * @param entityCounter a count of entities written
     */
    protected void writeFooter(final PrintWriter printWriter, final EntityCounter entityCounter)
    {
        writeEntityCountComment(printWriter, entityCounter);
        printWriter.write("</entity-engine-xml>");
    }

    /**
     * Writes an XML comment containing entity count information
     *
     * @param printWriter a PrintWriter to us
     * @param entityCounter the enitytCounter with the counts
     */
    private void writeEntityCountComment(final PrintWriter printWriter, final EntityCounter entityCounter)
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg();
        try
        {
            entityCounter.outputToMessage(logMsg);
        }
        catch (final RuntimeException rte)
        {
            // This is done as a worst case scenario.  We would rather have the exported datafile than
            // have corrupted system info in it
            log.error("An exception occuring while writing the JIRA system info end comment", rte);
            return;
        }

        printWriter.println("<!-- ");
        printWriter.println(escapeXmlComment(logMsg.toString()));
        printWriter.println(" -->");
    }

    /**
     * This will write the start of the exported XML file out.  This includes the XML declaration and the top level root
     * element
     *
     * @param printWriter a PrintWriter to write to
     * @param exportingUser the user doing the export
     */
    protected void writeHeader(final PrintWriter printWriter, final User exportingUser)
    {
        printWriter.write("<?xml version=\"1.0\" encoding=\"" + applicationProperties.getEncoding() + "\"?>" + NL);
        writeSysInfoComment(printWriter, exportingUser);
        printWriter.write("<entity-engine-xml>" + NL);
    }

    /**
     * This writes a XML comment to the start of the exported XML indicating the JIRA system information at the time the
     * data was exported.
     *
     * @param printWriter the printwriter to write to
     * @param exportingUser the use who is requesting the export
     */
    private void writeSysInfoComment(final PrintWriter printWriter, final User exportingUser)
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg();
        try
        {
            final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            final String when = df.format(new Date());
            String exportingUserName = "Unknown???";
            if (exportingUser != null)
            {
                exportingUserName = exportingUser.getName();
                if (exportingUser.getDisplayName() != null)
                {
                    exportingUserName += " ( " + exportingUser.getDisplayName() + " )";
                }
            }
            logMsg.outputHeader("Exported on");
            logMsg.outputProperty("on", when);
            logMsg.outputProperty("by", exportingUserName);

            final JiraSystemInfo info = new JiraSystemInfo(logMsg, buildUtilsInfo);
            info.obtainBasicInfo(null);
            info.obtainDatabaseConfigurationInfo();
            info.obtainJiraAppProperties();
            info.obtainDatabaseStatistics();
            info.obtainUpgradeHistory();
            info.obtainFilePaths();
            info.obtainPlugins();
            info.obtainListeners();
            info.obtainServices();
            info.obtainTrustedApps();
        }
        catch (final RuntimeException rte)
        {
            // This is done as a worst case scenario.  We would rather have the exported datafile than
            // have corrupted system info in it
            log.error("An exception occuring while writing the JIRA system info start commment", rte);
            return;
        }
        printWriter.println("<!-- ");
        printWriter.println(escapeXmlComment(logMsg.toString()));
        printWriter.println(" -->");
    }

    /**
     * Just in case the logged data has a closing --> XML comment in it.
     * <p/>
     * I put this in because of Dylan's threat that Anton would never forgive me if I broke data export / import!
     * <p/>
     * Update : JRA-15753 - but guess what it did any ways!
     * <p/>
     * Rules are at http://www.w3.org/TR/REC-xml/#dt-comment
     *
     * @return an escaped version so that the XML Comment stays just
     */
    private String escapeXmlComment(final String xmlComment)
    {
        String escapedComment = xmlComment;
        if (xmlComment.indexOf("--") != -1)
        {
            escapedComment = escapedComment.replaceAll("--", "-:");
            // and some explanation
            escapedComment = new StringBuffer().append(
                "\nThe comment data contained one of more occurences of a '-' character followed immediately by another '-' character.").append(
                "\nThis is not allowed according to http://www.w3.org/TR/REC-xml/#dt-comment.").append(
                "\nThese have been replaced by '-:' characters to make the XML valid").append("\n\n").append(escapedComment).toString();
        }
        return escapedComment;
    }

    /**
     * A simple class to count the entities as they go out the door
     */
    private static final class EntityCounter
    {
        Map<String, AtomicLong> map = new LinkedHashMap<String, AtomicLong>();
        AtomicLong total = new AtomicLong(0);

        private void increment(final String entityName)
        {
            AtomicLong count = map.get(entityName);
            if (count == null)
            {
                count = new AtomicLong(0);
                map.put(entityName, count);
            }
            count.incrementAndGet();
            total.incrementAndGet();
        }

        private void outputToMessage(final FormattedLogMsg logMsg)
        {
            logMsg.outputHeader("Entities");
            logMsg.outputProperty("Total", String.valueOf(total));
            logMsg.add("");
            for (final Map.Entry<String, AtomicLong> entry : map.entrySet())
            {
                logMsg.outputProperty(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
    }
}
