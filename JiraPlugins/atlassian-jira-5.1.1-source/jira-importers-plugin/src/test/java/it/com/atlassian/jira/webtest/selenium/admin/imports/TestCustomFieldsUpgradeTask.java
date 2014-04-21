package it.com.atlassian.jira.webtest.selenium.admin.imports;

import com.atlassian.jira.plugins.importer.po.ConfigureCustomFieldPage;
import com.atlassian.jira.plugins.importer.po.ViewCustomFieldsPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestCustomFieldsUpgradeTask extends BaseJiraWebTest {

    @Test
    public void checkCustomField() {
        backdoor.restoreData("brokenCustomField.xml");

        final ViewCustomFieldsPage customFieldsPage = jira.gotoLoginPage().loginAsSysAdmin(ViewCustomFieldsPage.class);
        final List<ViewCustomFieldsPage.CustomFieldItem> customFields = customFieldsPage.getCustomFields();
        assertThat(customFields.size(), greaterThan(1));
        assertEquals("A select", customFields.get(0).name);

        final ConfigureCustomFieldPage configurePage = jira.visit(ConfigureCustomFieldPage.class, customFields.get(0).id);
        assertTrue(configurePage.isEditOptionsVisible());
    }
}
