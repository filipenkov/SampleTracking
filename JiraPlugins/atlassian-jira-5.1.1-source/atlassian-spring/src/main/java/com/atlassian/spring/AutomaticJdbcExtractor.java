package com.atlassian.spring;

import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

import java.sql.*;
import java.util.Iterator;
import java.util.Map;

public class AutomaticJdbcExtractor implements NativeJdbcExtractor
{
    private Map extractors;
    private NativeJdbcExtractor defaultJdbcExtractor;
    private NativeJdbcExtractor jdbcExtractor;

    public boolean isNativeConnectionNecessaryForNativeStatements()
    {
        return true;
    }

    public boolean isNativeConnectionNecessaryForNativePreparedStatements()
    {
        return true;
    }

    public boolean isNativeConnectionNecessaryForNativeCallableStatements()
    {
        return true;
    }

    /**
     * When this method is called, the connection object passed in is checked to determine
     * what type of connection pooling is being used (based on class prefix)
     * <p/>
     * <p> Once we find out what pooling is used we then set the jdbcExtractor field to the appropriate
     * extractor and delegate all method calls to this extractor
     *
     * @param con
     * @return
     * @throws SQLException
     */
    public Connection getNativeConnection(Connection con) throws SQLException
    {
        return getJdbcExtractor(con).getNativeConnection(con);
    }

    private synchronized NativeJdbcExtractor getJdbcExtractor(Object o)
    {
        if (jdbcExtractor == null)
        {
            String objClass = o.getClass().getName();

            for (Iterator iterator = extractors.keySet().iterator(); iterator.hasNext();)
            {
                String classPrefix = (String) iterator.next();
                if (objClass.indexOf(classPrefix) != -1)
                {
                    // set the extractor to delegate to
                    jdbcExtractor = (NativeJdbcExtractor) extractors.get(classPrefix);
                }
            }

            if (jdbcExtractor == null)
                jdbcExtractor = defaultJdbcExtractor;
        }
        
        return jdbcExtractor;
    }

    public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException
    {
        return getJdbcExtractor(stmt).getNativeConnectionFromStatement(stmt);
    }

    public Statement getNativeStatement(Statement stmt) throws SQLException
    {
        return getJdbcExtractor(stmt).getNativeStatement(stmt);
    }

    public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException
    {
        return getJdbcExtractor(ps).getNativePreparedStatement(ps);
    }

    public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException
    {
        return getJdbcExtractor(cs).getNativeCallableStatement(cs);
    }

    public ResultSet getNativeResultSet(ResultSet rs) throws SQLException
    {
        return getJdbcExtractor(rs).getNativeResultSet(rs);
    }

    public Map getExtractors()
    {
        return extractors;
    }

    public void setExtractors(Map extractors)
    {
        this.extractors = extractors;
    }

    public NativeJdbcExtractor getDefaultJdbcExtractor()
    {
        return defaultJdbcExtractor;
    }

    public void setDefaultJdbcExtractor(NativeJdbcExtractor defaultJdbcExtractor)
    {
        this.defaultJdbcExtractor = defaultJdbcExtractor;
    }

}
