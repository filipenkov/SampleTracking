/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports;

import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterLogsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sourceforge.jwebunit.UnableToSetFormException;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ITUtils {

	public static final String BUGZILLA_2_20 = "bugzilla.222";
	public static final String BUGZILLA_3_6_4 = "bugzilla.363";
	public static final String BUGZILLA_LARGE = "bugzilla.large";
	public static final String BUGZILLA_3_6_4_POSTGRESQL = "bugzilla.364.pg";
	public static final String MANTIS_1_2_4 = "mantis.124";
	public static final String MANTIS_1_1_8 = "mantis.118";
	public static final String FOGBUGZ_7_3_6 = "fogbugz.736";

	private static final ResourceBundle I18N = ResourceBundle.getBundle("com.atlassian.jira.plugins.importer.web.action.util.messages");

	public static String getText(String key, String... args) {
		return MessageFormat.format(I18N.getString(key), args);
	}

	@Test
	public void empty() {

	}

	public static boolean skipExternalSystems() {
		return System.getenv("BAMBOO") != null;
	}

	public static Configuration getProperties() {
		try {
			final CompositeConfiguration configuration = new CompositeConfiguration();
			configuration.addConfiguration(new PropertiesConfiguration("it.properties"));
			configuration.addConfiguration(new PropertiesConfiguration("src/test/resources/it.properties"));

			return configuration;
		} catch (ConfigurationException e) {
			return new BaseConfiguration();
		}
	}

	public static void doWebSudoCrap(com.atlassian.jira.functest.framework.Navigation navigation,
			net.sourceforge.jwebunit.WebTester tester) {
		// setup web sudo session
		navigation.gotoAdmin();
		tester.clickLink("general_configuration");

		try {
			tester.setWorkingForm("login-form");
            if (tester.getDialog().hasFormParameterNamed("password")) {
			    tester.setFormElement("password", "admin");
            }
            if (tester.getDialog().hasFormParameterNamed("webSudoPassword")) {
                tester.setFormElement("webSudoPassword", "admin");
            }
			tester.submit("authenticate");
		} catch(UnableToSetFormException e) {

		}

		navigation.gotoAdmin();
	}

	public static void setupConnection(CommonImporterSetupPage setupPage, String systemName) {
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> configuration;
		try {
			configuration = mapper.readValue(new File("src/test/resources/it.json"),
					new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final Map<String, String> map = (Map<String, String>) configuration.get(systemName);
		if (map == null) {
			throw new RuntimeException(String.format("%s not found in configuration", systemName));
		}

		for(Map.Entry<String, String> entry : map.entrySet()) {
			setupPage.setField(entry.getKey(), entry.getValue());
		}
    }

	public static JiraRestClient createRestClient(JIRAEnvironmentData environmentData) {
		final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		try {
			return factory.createWithBasicHttpAuthentication(environmentData.getBaseUrl().toURI(), "admin", "admin");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getCurrentWorkingDirectory() {
		return new File(".").getAbsolutePath();
	}

	public static boolean isPortForwardEnv() throws Exception {
		try {
			new URL("http://192.168.157.160:80").getContent();
			return false;
		} catch (IOException e) {
			return true;
		}
	}

	public static void doCsvImport(JiraTestedProduct product,
			String csv, @Nullable String config) {

		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(getCurrentWorkingDirectory() + File.separator + csv);
		if (config != null) {
			setupPage.setConfigurationFile(getCurrentWorkingDirectory() + File.separator + config);
		}

		ImporterLogsPage logs = setupPage.next().next().next().next();
		logs.waitUntilFinished();
	}

    public static List<Issue> getIssuesByJql(final JiraRestClient restClient, String jql) throws IOException {
        final SearchResult result = restClient.getSearchClient().searchJql(jql, new NullProgressMonitor());
        return Lists.newArrayList(Iterables.transform(result.getIssues(), new Function<BasicIssue, Issue>() {
            @Override
            public Issue apply(BasicIssue basicIssue) {
                return restClient.getIssueClient().getIssue(basicIssue.getKey(), new NullProgressMonitor());
            }
        }));
    }

	public static Map<String, Field> getFieldsMap(Iterable<Field> fields) {
		Map<String, Field> result = Maps.newHashMap();
		for(Field field : fields) {
			result.put(field.getName(), field);
		}
		return result;
	}
}
