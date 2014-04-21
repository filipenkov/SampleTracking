/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package it.com.atlassian.jira.webtest.selenium.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;

/**
 * Tests the admin/sys-admin visibility of pages in the admin section.
 *
 * @since v3.12
 */
public class TestAdminSectionVisibility extends FuncTestCase {
	@Before
	public void setUpTest() {
        ITUtils.doWebSudoCrap(navigation, tester);

		administration.restoreBlankInstance();

		ITUtils.doWebSudoCrap(navigation, tester);
	}

	public void testAdminCanSeeProtectedPages() {
		try {
			administration.restoreData("TestWithSystemAdmin.xml");

			ITUtils.doWebSudoCrap(navigation, tester);

			String[] urlsToCheck = new String[] {
					"/secure/admin/views/ExternalImport1.jspa",
					"/secure/admin/views/CsvSetupPage!default.jspa?externalSystem=CSV",
					"/secure/admin/views/ImporterSetupPage!default.jspa?externalSystem=Bugzilla",
					"/secure/admin/views/ImporterCustomFieldsPage!default.jspa?externalSystem=Bugzilla",
                    "/secure/admin/views/ImporterFieldMappingsPage!default.jspa?externalSystem=Bugzilla",
					"/secure/admin/views/ImporterLinksPage!default.jspa?externalSystem=Bugzilla",
					"/secure/admin/views/ImporterLogsPage!default.jspa?externalSystem=Bugzilla",
					"/secure/admin/views/ImporterProjectMappingsPage!default.jspa?externalSystem=Bugzilla",
					"/secure/admin/views/ImporterValueMappingsPage!default.jspa?externalSystem=Bugzilla",
			};

			checkUrlsForPerm(urlsToCheck);
		} finally {
			navigation.logout();
			navigation.login("root", "root");
			administration.restoreBlankInstance();
		}
	}

	/**
	 * Check all the links that an Admin should not be able to see.
	 */
	public void testAdminCanSeeExternalImportLink() {
		try {
			administration.restoreData("TestWithSystemAdmin.xml");

            ITUtils.doWebSudoCrap(navigation, tester);

            navigation.gotoAdmin();

			tester.assertLinkPresent("external_import");
		} finally {
			navigation.logout();
			navigation.login("root", "root");
			administration.restoreBlankInstance();
		}
	}

	private void checkUrlsForPerm(String[] urls) {
		for (int i = 0; i < urls.length; i++) {
			String url = urls[i];
			tester.gotoPage(url);
			tester.assertTextNotPresent(
                    "It seems that you have tried to perform an operation which you are not permitted to perform.");
		}
	}

}