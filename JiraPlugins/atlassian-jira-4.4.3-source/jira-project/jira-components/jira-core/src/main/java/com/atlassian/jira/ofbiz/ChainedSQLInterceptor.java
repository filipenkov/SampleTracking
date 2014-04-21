package com.atlassian.jira.ofbiz;

import org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptor} that can chain together multiple
 * SQLInterceptors.
 *
 * It will call them in an enveloping order.
 *
 * <pre>
 *  eg if we add 'sqlI1' and 'sqlI2' then it will call
 *
 * sqlI1.beforeExecution(..)
 * sqlI2.beforeExecution(..)
 *
 * sqlI2.afterSuccessfulExecution(..)
 * sqlI1.afterSuccessfulExecution(..)
 * </pre>
 *
 * @since v4.0
 */
public class ChainedSQLInterceptor implements SQLInterceptor
{
    private final List<SQLInterceptor> interceptorsList;
    private final List<SQLInterceptor> reverseInterceptorsList;

    public static class Builder
    {
        private List<SQLInterceptor> interceptorsList = new ArrayList<SQLInterceptor>();

        public Builder add(SQLInterceptor sqlInterceptor)
        {
            interceptorsList.add(sqlInterceptor);
            return this;
        }

        public ChainedSQLInterceptor build()
        {
            return new ChainedSQLInterceptor(interceptorsList);
        }
    }

    public ChainedSQLInterceptor(final List<SQLInterceptor> interceptorsList)
    {
        this.interceptorsList = new ArrayList<SQLInterceptor>(interceptorsList);
        this.reverseInterceptorsList = new ArrayList<SQLInterceptor>(interceptorsList);
        Collections.reverse(reverseInterceptorsList);
    }

    public void beforeExecution(final String sqlString, final List<String> parameterValues, final Statement statement)
    {
        for (SQLInterceptor sqlInterceptor : interceptorsList)
        {
            sqlInterceptor.beforeExecution(sqlString, parameterValues, statement);
        }
    }

    public void afterSuccessfulExecution(final String sqlString, final List<String> parameterValues, final Statement statement, final ResultSet resultSet, final int rowsUpdated)
    {
        for (SQLInterceptor sqlInterceptor : reverseInterceptorsList)
        {
            sqlInterceptor.afterSuccessfulExecution(sqlString, parameterValues, statement, resultSet,rowsUpdated);
        }
    }

    public void onException(final String sqlString, final List<String> parameterValues, final Statement statement, final SQLException sqlException)
    {
        for (SQLInterceptor sqlInterceptor : reverseInterceptorsList)
        {
            sqlInterceptor.onException(sqlString, parameterValues, statement, sqlException);
        }
    }
}
