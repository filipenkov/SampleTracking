/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;
import org.ofbiz.core.entity.jdbc.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractUpgradeTask implements UpgradeTask
{
    private final boolean reindexRequired;

    protected AbstractUpgradeTask(boolean reindexRequired)
    {
        this.reindexRequired = reindexRequired;
    }

    @Override
    public boolean isReindexRequired()
    {
        return reindexRequired;
    }

    public abstract String getBuildNumber();

    private final List<String> errors = new ArrayList<String>();

    public abstract void doUpgrade(boolean setupMode) throws Exception;

    public String getShortDescription()
    {
        return "Upgrade to build number: " + getBuildNumber();
    }


    /*
     * Please use the OfBizDelegator now.
     *
     * @since v5.0
     */
    @Deprecated
    protected GenericDelegator getDelegator()
    {
        return CoreFactory.getGenericDelegator();
    }

    protected static OfBizDelegator getOfBizDelegator()
    {
        return ComponentAccessor.getOfBizDelegator();
    }

    protected ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    protected void addError(final String error)
    {
        errors.add(error);
    }

    /**
     * Useful for adding a bunch of errors (like from a command) with a prefix
     */
    public void addErrors(final String prefix, final Collection<String> errors)
    {
        for (final String errorMessage : errors)
        {
            errors.add(prefix + errorMessage);
        }
    }

    public void addErrors(final Collection<String> errors)
    {
        addErrors("", errors);
    }

    public Collection<String> getErrors()
    {
        return errors;
    }

    protected I18nHelper getI18nBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

    protected Connection getDatabaseConnection()
    {
        try
        {
            GenericHelper helper = getDelegator().getEntityHelper("User");
            DatabaseUtil utils = new DatabaseUtil(helper.getHelperName());

            return utils.getConnection();
        }
        catch (SQLException e)
        {
            throw new DataAccessException("Unable to obtain a DB connection", e);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Unable to obtain a DB connection", e);
        }
    }

    protected String convertToSchemaTableName(String tableName)
    {
        GenericHelper helper = null;
        try
        {
            helper = getDelegator().getEntityHelper("User");
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
        DatasourceInfo datasourceInfo = EntityConfigUtil.getInstance().getDatasourceInfo(helper.getHelperName());

        return ensureTablePrefixed(tableName, datasourceInfo.getSchemaName());
    }

    static String ensureTablePrefixed(String tableName, String schemaName)
    {
        if (StringUtils.isNotBlank(schemaName))
        {
            String prefix = schemaName + ".";
            if (!tableName.startsWith(prefix))
            {
                // Prepend the schema name
                return prefix + tableName;
            }
        }
        return tableName;
    }
}
