/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer;

import com.atlassian.jira.plugins.importer.external.beans.NamedExternalObject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.Collection;
import java.util.List;

public class SqlUtils {

	public static final String META_COLUMN_NAME = "COLUMN_NAME";

	public static void close(final PreparedStatement ps, final ResultSet rs)
    {
        close(ps);
        close(rs);
    }

    public static void close(final PreparedStatement ps)
    {
        try
        {
            if (ps != null)
            {
                ps.close();
            }
        }
        catch (final SQLException ignore)
        {
            // ignored
        }
    }

    public static void close(final ResultSet rs)
    {
        try
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        catch (final SQLException ignore)
        {
            // ignored
        }
    }

	/**
     * Generate SQL-friendly comma-separated list of ?'s.
     */
    public static String getSQLTokens(final String[] names)
    {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < names.length; i++)
        {
            sb.append(" ? ");
            if (i != names.length - 1)
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

	public static void close(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch(final Exception e) {
			// ignored
		}
	}

	public static List<String> getColumnNames(Connection connection, String tableName) throws SQLException {
		final ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, null);
		try {
			final List<String> columns = Lists.newArrayList();

			while (rs.next()) {
				columns.add(rs.getString(META_COLUMN_NAME));
			}

			return columns;
		} finally {
			SqlUtils.close(rs);
		}
	}

	public static List<String> getColumnNames(ResultSetMetaData metaData) throws SQLException {
		List<String> set = Lists.newArrayList();
		for(int i=1, s=metaData.getColumnCount(); i<=s; ++i) {
			set.add(metaData.getColumnName(i));
		}
		return set;
	}

	public static String or(Collection<? extends NamedExternalObject> objs) {
		return StringUtils.join(Iterables.transform(objs, NamedExternalObject.ID_FUNCTION).iterator(), " OR ");
	}

	public static String comma(Collection<? extends NamedExternalObject> objs) {
		return StringUtils.join(Iterables.transform(objs, NamedExternalObject.ID_FUNCTION).iterator(), ",");
	}

    public static boolean hasTable(@Nonnull final Connection connection, @Nonnull final String tableName) throws SQLException {
        final ResultSet columns = connection.getMetaData().getTables(null, null, tableName, null);
        try {
            while (columns.next()) {
                return true;
            }
            return false;
        }
        finally {
            if (columns != null) {
                columns.close();
            }
        }
    }
}
