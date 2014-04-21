package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityListIterator;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericDataSourceException;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.jdbc.AutoCommitSQLProcessor;
import org.ofbiz.core.entity.jdbc.SQLProcessor;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import org.ofbiz.core.entity.model.ModelReader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Upgrade task that copies old worklog data from the jiraaction table to the new worklog table
 */
public class UpgradeTask_Build255 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build255.class);

    private final DelegatorInterface delegatorInterface;
    private final OfBizConnectionFactory ofBizConnectionFactory = new DefaultOfBizConnectionFactory();
    protected static final String ACTION_ENTITY_NAME = "Action";
    protected static final String WORKLOG_ENTITY_NAME = "Worklog";
    protected static final String SEQUENCE_VALUE_ITEM_ENTITY_NAME = "SequenceValueItem";
    protected static final long SEQ_INCREMENT_VALUE = 50;
    protected static final String SEQ_NAME = "seqName";
    protected static final String SEQ_ID = "seqId";

    public UpgradeTask_Build255(GenericDelegator delegatorInterface)
    {
        this.delegatorInterface = delegatorInterface;
    }

    public String getShortDescription()
    {
        return "Copies old worklog data from the jiraaction table to the new worklog table";
    }

    public String getBuildNumber()
    {
        return "255";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // First check to see if data exists in the Action table to move, if so delete any records that may exist in
        // Worklog table
        cleanWorklogTableIfNeeded();

        // Now lets copy the data from the Action table to the Worklog table
        copyFromActionToWorklog();

        // Next lets update the sequence for the Worklog table
        updateWorklogSequence();

        // Last lets remove all the worklog data from the Action table
        removeWorklogsFromActionTable();
    }

    /**
     * Checks the contents of the Worklog and Action table for worklogs. If the same worklog appears in both tables it
     * will be deleted from the Worklog table and re-copied from the Action table later in this upgrade task.
     */
    void cleanWorklogTableIfNeeded()
    {
        EntityListIterator newWorklogIterator = null;
        EntityListIterator oldWorklogIterator = null;

        try
        {
            List newWorklogIds;
            try
            {
                newWorklogIterator = delegatorInterface.findListIteratorByCondition(WORKLOG_ENTITY_NAME, null, EasyList.build("id"), null);
                newWorklogIds = iteratorToList(newWorklogIterator);
            }
            finally
            {
                if (newWorklogIterator != null)
                {
                    newWorklogIterator.close();
                }
            }

            if (!newWorklogIds.isEmpty())
            {
                List oldWorklogIds;
                try
                {
                    oldWorklogIterator = delegatorInterface.findListIteratorByCondition(ACTION_ENTITY_NAME, new EntityFieldMap(EasyMap.build("type", ActionConstants.TYPE_WORKLOG), EntityOperator.AND), EasyList.build("id"), null);
                    oldWorklogIds = iteratorToList(oldWorklogIterator);
                }
                finally
                {
                    if (oldWorklogIterator != null)
                    {
                        oldWorklogIterator.close();
                    }

                }
                if (!oldWorklogIds.isEmpty())
                {
                    log.warn("There are records in the Worklog table and original worklog records in the Action table, deleting coresponding Worklog table entries.");

                    Collection intersection = CollectionUtils.intersection(newWorklogIds, oldWorklogIds);
                    if (!intersection.isEmpty())
                    {
                        deleteFromWorklog(intersection);
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private List iteratorToList(EntityListIterator iterator)
    {
        List worklogs = new ArrayList();

        GenericValue worklogIdGV = (GenericValue) iterator.next();
        while (worklogIdGV != null)
        {
            worklogs.add(worklogIdGV.getLong("id"));
            worklogIdGV = (GenericValue) iterator.next();
        }
        return worklogs;
    }

    void deleteFromWorklog(Collection ids) throws GenericEntityException
    {
        OfBizDelegator delegator = new DefaultOfBizDelegator(delegatorInterface);
        ArrayList list = new ArrayList(ids);
        delegator.removeByOr(WORKLOG_ENTITY_NAME, "id", list);
    }

    void copyFromActionToWorklog()
    {
        log.info("Copying old worklog data from the jiraaction table to newer worklog table");
        try
        {
            GenericHelper entityHelper = delegatorInterface.getEntityHelper(ACTION_ENTITY_NAME);
            SQLProcessor processor = new AutoCommitSQLProcessor(entityHelper.getHelperName());

            try
            {
                processor.prepareStatement(buildConversionSQL());
                processor.setValue(ActionConstants.TYPE_WORKLOG);
                processor.executeUpdate();
            }
            finally
            {
                try
                {
                    processor.close();
                }
                catch (GenericDataSourceException e)
                {
                    log.warn("Could not close the SQLProcessor", e);
                }
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        catch (SQLException e)
        {
            log.warn("", e);
        }
        finally
        {
        }
    }

    void updateWorklogSequence() throws GenericEntityException, SQLException
    {
        GenericHelper entityHelper;
        SQLProcessor processor;

        log.info("Creating sequence record for worklog table");

        entityHelper = delegatorInterface.getEntityHelper(WORKLOG_ENTITY_NAME);
        processor = new AutoCommitSQLProcessor(entityHelper.getHelperName());

        try
        {
            // Get the max id from the WorklogMax view
            Long maxId = getWorklogIdMax();
            if (maxId != null)
            {
                processor.prepareStatement(buildSequencerSQL());
                processor.setValue(new Long(maxId.longValue() + SEQ_INCREMENT_VALUE));
                processor.setValue(WORKLOG_ENTITY_NAME);
                processor.executeUpdate();
            }
        }
        finally
        {
            try
            {
                processor.close();
            }
            catch (GenericDataSourceException e)
            {
                log.warn("Could not close the SQLProcessor", e);
            }

            // Flush the Sequence now that we have updated it
            delegatorInterface.refreshSequencer();
        }
    }

    void removeWorklogsFromActionTable() throws GenericEntityException
    {
        log.info("Removing old worklog data from the Action table.");
        delegatorInterface.removeByAnd(ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG));
    }

    Long getWorklogIdMax() throws GenericEntityException
    {
        List workLogMaxValues = delegatorInterface.findAll("WorklogMax");
        Long maxId = null;
        // Get the first element since this is a max
        if (workLogMaxValues.size() == 1)
        {
            GenericValue maxIdGV = (GenericValue) workLogMaxValues.get(0);
            maxId = maxIdGV.getLong("max");
        }

        return maxId;
    }

    String buildConversionSQL()
    {
        StringBuffer sql = new StringBuffer();

        ModelEntity worklogModelEntity = getModelEntityForName(WORKLOG_ENTITY_NAME);
        ModelEntity actionModelEntity = getModelEntityForName(ACTION_ENTITY_NAME);

        String helperName = ofBizConnectionFactory.getDatasourceInfo().getName();

        sql.append("insert into ").append(worklogModelEntity.getTableName(helperName));
        sql.append(" (");
        sql.append(getDbColumnName(worklogModelEntity, "id")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "issue")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "author")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "grouplevel")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "rolelevel")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "body")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "created")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "updateauthor")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "updated")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "startdate")).append(", ");
        sql.append(getDbColumnName(worklogModelEntity, "timeworked"));
        sql.append(") ");

        sql.append("select ");
        sql.append(getDbColumnName(actionModelEntity, "id")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "issue")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "author")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "level")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "rolelevel")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "body")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "created")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "author")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "created")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "created")).append(", ");
        sql.append(getDbColumnName(actionModelEntity, "numvalue"));
        sql.append(" from ").append(actionModelEntity.getTableName(helperName));
        sql.append(" where ");
        sql.append(getDbColumnName(actionModelEntity, "type")).append(" = ?");

        return sql.toString();
    }

    String buildSequencerSQL()
    {
        ModelEntity modelEntity = getModelEntityForName(SEQUENCE_VALUE_ITEM_ENTITY_NAME);
        String sequencerTable = modelEntity.getTableName(ofBizConnectionFactory.getDatasourceInfo().getName());
        String seqNameColumnName = modelEntity.getField(SEQ_NAME).getColName();
        String seqIdColumnName = modelEntity.getField(SEQ_ID).getColName();

        // Check to see if the record already exists, if so then generate update sql, otherwise generate insert sql
        List worklogSequenceEntryList = null;
        try
        {
            worklogSequenceEntryList = delegatorInterface.findByAnd(SEQUENCE_VALUE_ITEM_ENTITY_NAME, EasyMap.build(SEQ_NAME, WORKLOG_ENTITY_NAME));
            if (worklogSequenceEntryList != null && !worklogSequenceEntryList.isEmpty())
            {
                log.warn("Updating the Worklog sequencer, we should only really ever be inserting a row...");
                return buildSequencerUpdateSQL(sequencerTable, seqNameColumnName, seqIdColumnName);
            }
            else
            {
                return buildSequencerInsertSQL(sequencerTable, seqNameColumnName, seqIdColumnName);
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    String buildSequencerInsertSQL(String sequencerTable, String seqNameColumnName, String seqIdColumnName)
    {
        StringBuffer sql = new StringBuffer("insert into ");
        sql.append(sequencerTable);
        sql.append(" (");
        // NOTE: please keep the ordering of the fields since the values for the prepared statement need to be ordered
        // to match the updateSQL that may be generated
        sql.append(seqIdColumnName).append(", ");
        sql.append(seqNameColumnName);
        sql.append(") ");
        sql.append("values (?, ?)");
        return sql.toString();
    }

    String buildSequencerUpdateSQL(String sequencerTable, String seqNameColumnName, String seqIdColumnName)
    {
        StringBuffer sql = new StringBuffer("update ");
        sql.append(sequencerTable);
        // NOTE: please keep the ordering of the fields since the values for the prepared statement need to be ordered
        // to match the insertSQL that may be generated
        sql.append(" set ");
        sql.append(seqIdColumnName);
        sql.append(" = ? ");
        sql.append("where ");
        sql.append(seqNameColumnName);
        sql.append(" = ?");
        return sql.toString();
    }

    String getDbColumnName(ModelEntity modelEntity, String columnName)
    {
        ModelField field = modelEntity.getField(columnName);
        if (field == null)
        {
            throw new DataAccessException("Unable to find column: " + columnName + " in table: " + modelEntity.getEntityName());
        }
        return field.getColName();
    }

    ModelEntity getModelEntityForName(String entityName)
    {
        ModelReader modelReader;
        try
        {
            modelReader = ModelReader.getModelReader(ofBizConnectionFactory.getDelegatorName());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        try
        {
            return modelReader.getModelEntity(entityName);
        }
        catch (GenericEntityException e)
        {
            throw new IllegalArgumentException("Unable to locate the modelEntity for " + entityName + " table: " + e.getMessage());
        }
    }
}