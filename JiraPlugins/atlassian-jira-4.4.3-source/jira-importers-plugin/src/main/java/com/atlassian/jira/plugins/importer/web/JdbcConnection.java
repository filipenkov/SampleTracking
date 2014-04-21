/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.web;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugins.importer.SQLRuntimeException;
import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Bean for managing a JDBC connection - storing connection details,
 * a Connection instance, and checking if a connection can be made.
 * Most of this code is generic, and could be factored out if
 * something other than Bugzilla needs it.
 * <p/>
 * PLEASE NOTE: This is not Thread-Safe, and should only be used
 * from the Web Actions!!!!!!!
 */
@NotThreadSafe
public class JdbcConnection {
	private static final Logger log = Logger.getLogger(JdbcConnection.class);

	protected String driverName;
	private final String url;
	private final String username;
	private final String password;
	private Connection connection;

	public JdbcConnection(final String driverName, final String url, @Nullable final String username,
			@Nullable final String password) {
		this.driverName = driverName;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public boolean isValidDriverName() {
		try {
			ClassLoaderUtils.loadClass(driverName, JdbcConnection.class);
			return true;
		}
		catch (final ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * @return a Connection instance - always the same one unless JDBC details are changed.
	 * @throws java.sql.SQLException when things go HORRIBLY wrong
	 */
	public synchronized Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			final Driver driver = loadDriver(driverName);

			final StringBuilder connDescription = new StringBuilder(url);
			final Properties connectionProperties = new Properties();
			if(StringUtils.isNotBlank(username)) {
				connectionProperties.setProperty("user", username);
				connDescription.append(" with username '").append(username).append("'");
			}
			if (StringUtils.isNotBlank(password)) {
				connectionProperties.setProperty("password", password);
				connDescription.append(" and password");
			}

			log.info("Connecting to JDBC using connection string: " + connDescription);
			connection = driver.connect(url, connectionProperties);
			if (connection == null) {
				throw new SQLException("Connection string not supported by selected driver.");
			}

			connection.setReadOnly(true);
		}
		return connection;
	}

	private Driver loadDriver(final String driverName) throws SQLException {
		try {
			final Class clazz = ClassLoaderUtils.loadClass(driverName, JdbcConnection.class);
			return (Driver) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new SQLException("JDBC driver class not found: " + driverName, e);
		} catch (Exception e) {
			throw new SQLException("Cannot instantiate JDBC driver class " + driverName + ": " + e.getMessage(), e);
		}
	}

	public synchronized void closeConnection() {
		try {
			if (connection != null) {
				connection.close();
			}
		}
		catch (final SQLException ignored) {
			// policy
		}
		connection = null;
	}

	// Setters and getters

	public synchronized String getDriverName() {
		return driverName;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * Queries the database with the SqlQuery and then adds and transforms each row of the result into an object which are
	 * then returned in a list
	 *
	 * @param transformer object transformer from resultset to domain object
	 * @return List of transformed objects
	 */
	public <T> List<T> queryDb(final ResultSetTransformer<T> transformer) {
		return queryDb(transformer, false);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> queryDbAppendCollection(final ResultSetTransformer<Collection<T>> transformer) {
		// it's not beautiful I must admit, but it's just to workaround dual-nature of queryDb() method below
		return (List<T>) queryDb(transformer, true);
	}

	private <T> List<T> queryDb(final ResultSetTransformer<T> transformer, final boolean addAllIfCollection) {
		final List<T> objects = new ArrayList<T>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection().prepareStatement(transformer.getSqlQuery(), ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			while (rs.next()) {
				final T o = transformer.transform(rs);
				if (o != null) {
					if (addAllIfCollection && (o instanceof Collection)) {
						@SuppressWarnings("unchecked")
						final Collection<T> collection = (Collection<T>) o;
						objects.addAll(collection);
					} else {
						objects.add(o);
					}
				}
			}
		}
		catch (final SQLException e) {
			throw new SQLRuntimeException(e);
		}
		finally {
			SqlUtils.close(ps, rs);
		}
		return objects;
	}

	public void validateConnection(final ErrorCollection errors) {
		if (!isValidDriverName()) {
			errors.addErrorMessage(getI18nBean().getText(
					"jira-importer-plugin.database.connection.bean.must.specify.valid.jdbc.driver"));
		}
		try {
			getConnection();
		} catch (Exception e) {
			final String msg = getI18nBean().getText("jira-importer-plugin.database.connection.error", e.getMessage());
			log.warn(msg, e);
			errors.addErrorMessage(msg);
		}
	}

	protected I18nHelper getI18nBean() {
		return ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper();
	}

}