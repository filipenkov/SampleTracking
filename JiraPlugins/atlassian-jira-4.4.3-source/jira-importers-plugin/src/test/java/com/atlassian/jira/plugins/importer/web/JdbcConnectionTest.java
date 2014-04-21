package com.atlassian.jira.plugins.importer.web;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcConnectionTest {

	@Test(expected = SQLException.class)
	public void testInstantiatingWrongDriver() throws Exception {
		new JdbcConnection("java.lang.String", "url", null, null).getConnection();
	}

	@Test(expected = SQLException.class)
	public void testInstantiatingAbsentDriver() throws Exception {
		new JdbcConnection("unknown.Driver", "url", null, null).getConnection();
	}

	@Test(expected = SQLException.class)
	public void testInaccessibleDriver() throws Exception {
		new JdbcConnection(InaccessibleClass.class.getName(), "url", null, null).getConnection();
	}

	private static class InaccessibleClass {
	}

	@Test(expected = SQLException.class)
	public void testInstantiationErrorDriver() throws Exception {
		new JdbcConnection(InstantiationErrorClass.class.getName(), "url", null, null).getConnection();
	}

	static class InstantiationErrorClass {
		InstantiationErrorClass() {
			throw new RuntimeException("Instantiation test");
		}
	}

	@Test
	public void testInstantiateProperly() throws Exception {
		JdbcConnection bean = new JdbcConnection(ValidDriver.class.getName(), "supportedUrl", "usr", "pass");
		Assert.assertNotNull(bean.getConnection());
	}

	@Test(expected = SQLException.class)
	public void testGetConnectionNeverReturnsNull() throws Exception {
		JdbcConnection bean = new JdbcConnection(ValidDriver.class.getName(), "unsupportedUrl", null, null);
		Assert.assertNotNull(bean.getConnection());
	}
}

class ValidDriver implements Driver {
	String username;
	String password;

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		username = info.getProperty("username");
		password = info.getProperty("password");
		return acceptsURL(url) ? Mockito.mock(Connection.class) : null;
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith("supportedUrl");
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return new DriverPropertyInfo[0];
	}

	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public boolean jdbcCompliant() {
		return true;
	}
}