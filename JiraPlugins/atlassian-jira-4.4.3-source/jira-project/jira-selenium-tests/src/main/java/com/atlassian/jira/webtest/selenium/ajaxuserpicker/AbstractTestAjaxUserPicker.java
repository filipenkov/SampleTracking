package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsVisibleCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.thoughtworks.selenium.SeleniumException;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

public abstract class AbstractTestAjaxUserPicker extends JiraSeleniumTest
{
    private static final String[] USERS = new String[]{"admin", "fred", "johnr", "johnw", "nouserpickeruser", "rastusw", "waterm"};
    protected static final String NOUSERPICKERUSER = "nouserpickeruser";
    protected static final String MULTI_CUSTOM_FIELD = "customfield_10001";
    protected static final String ITEM_KEY = "_i_";
    protected static final String MULTI_CUSTOM_FIELD_KEY = MULTI_CUSTOM_FIELD + ITEM_KEY;
    protected static final String READY_DROPDOWN_LOCATOR_TEMPLATE = "div#%s_container div.suggestions.dropdown-ready";

    public void onSetUp()
    {
        super.onSetUp();
        restoreAjaxUserPickerData();
    }

    /**
     * Restore data specific to this test.
     */
    protected void restoreAjaxUserPickerData()
    {
        restoreData("TestAjaxUserPicker.xml");
    }

    /**
     * Asserts tthat a set of users in the know set are present and that the other knows users ARE not present
     *
     * @param key        the key prefix
     * @param shownUsers the users to check for
     */
    protected void assertUsersShown(String key, String[] shownUsers)
    {
        Collection notShown = Arrays.asList(USERS);
        if (shownUsers != null)
        {
            notShown = CollectionUtils.subtract(Arrays.asList(USERS), Arrays.asList(shownUsers));
            for (int i = 0; i < shownUsers.length; i++)
            {
                String shownUser = shownUsers[i];
                assertThat.elementPresent(key + shownUser);
            }
        }
        for (Iterator iterator = notShown.iterator(); iterator.hasNext();)
        {
            String notShownUser = (String) iterator.next();
            assertThat.elementNotPresentByTimeout(key + notShownUser, 10000);
        }
    }

    /**
     * Waits until the AC drop down is made visible or until the timeout finishes.  Does its work queitly.
     *
     * @param fieldId   the JIRA form field name
     * @param maxMillis the time to wait
     * @
     */
    protected void waitForACDropDown(String fieldId, int maxMillis)
    {
        assertThat(dropDownReadyCondition(fieldId), isTrue().by(maxMillis));
    }

    protected void waitForACDropDownNotAppear(String fieldId, int maxMillis)
    {
        assertThat(dropDownReadyCondition(fieldId), isFalse().by(maxMillis));
    }

    /**
     * @param fieldId - the JIRA field name
     * @return true if the AC drop down is visible
     */
    protected boolean isVisibleACDropDown(String fieldId)
    {
        return dropDownReadyCondition(fieldId).byDefaultTimeout();
    }

    protected TimedCondition dropDownReadyCondition(String fieldId)
    {
        // TODO this is temporary and wrong, we need to have it encapsulated within page objects
        return IsVisibleCondition.forContext(context()).locator(readyDropDownLocator(fieldId)).defaultTimeout(DROP_DOWN_WAIT).build();
    }

    private SeleniumLocator readyDropDownLocator(String fieldId)
    {
        // TODO this is temporary and wrong, we need to have it encapsulated within page objects
        return SeleniumLocators.jQuery(String.format(READY_DROPDOWN_LOCATOR_TEMPLATE, fieldId), context());
    }

    private String getSelectedItemId() {
        return client.getEval("this.browserbot.getCurrentWindow().jQuery('.suggestions li.active').find(\":first-child\").attr(\"id\");");
    }

    /**
     * @return the LI of the currently selected AC item eg with 'yui-ac-highlight' or null
     */
    public String getCurrentSelectedACKey()
    {
        try
        {
            String str = getSelectedItemId();
            if (str.indexOf("_i_") > -1)
            {
                return str.substring(str.lastIndexOf("_") + 1);
            }
        }
        catch (SeleniumException se)
        {
        }
        return null;
    }

    /*
     * A series of tests that can be run when the user is not permitted to do user picking
     */
    protected void testNotPermittedSimpleUserPicker(String fieldId)
    {
        notPermittedACAsserts(fieldId);
    }

    /*
     * A series of tests that can be run when the user is permitted to do user picking
     */
    protected void testSimpleUserPicker(String fieldId)
    {
        commonPostiveACAsserts(fieldId);
        commonNegativeACAsserts(fieldId);
    }

    /*
     * A series of tests that can be run when the user is permitted to do user picking but nothing should be returned
     */
    protected void commonNegativeACAsserts(String fieldId)
    {
        client.keyPress(fieldId, VK_ESC);
        client.type(fieldId, "rubbish");
        client.keyPress(fieldId, "\\16");
        waitFor(DROP_DOWN_WAIT);
        assertFalse(isVisibleACDropDown(fieldId));
        assertFalse(client.isElementPresent(fieldId + ITEM_KEY + "rubbish"));

        assertEquals(null, getCurrentSelectedACKey());
        client.keyUp(fieldId, VK_DOWN);
        assertEquals(null, getCurrentSelectedACKey());
        client.keyUp(fieldId, VK_UP);
        assertEquals(null, getCurrentSelectedACKey());
    }

    /*
     * A series of tests that can be run when the user is NOT permitted to do user picking and hence nothing should be returned
     */
    protected void notPermittedACAsserts(String fieldId)
    {
        // we should not even have ajax yui code support

        assertFalse(client.isElementPresent("userPickerEnabled"));

        client.keyPress(fieldId, VK_ESC);
        client.type(fieldId, "water");
        client.keyPress(fieldId, "\\16");
        waitForACDropDownNotAppear(fieldId, DROP_DOWN_WAIT);
        assertFalse(isVisibleACDropDown(fieldId));
        assertThat.elementNotPresent(fieldId + ITEM_KEY + "water");

        assertEquals(null, getCurrentSelectedACKey());
        client.keyDown(fieldId, VK_DOWN);
        assertEquals(null, getCurrentSelectedACKey());
        client.keyDown(fieldId, VK_UP);
        assertEquals(null, getCurrentSelectedACKey());
    }

    /*
     * A series of tests that can be run when the user is permitted to do user picking but something should be returned
     */
    protected void commonPostiveACAsserts(String fieldId)
    {
        client.keyPress(fieldId, VK_ESC);
        
        client.type(fieldId, "water");
        client.keyPress(fieldId, "\\16");

        waitForACDropDown(fieldId, DROP_DOWN_WAIT);
        assertTrue(isVisibleACDropDown(fieldId));
        assertUsersShown(fieldId + ITEM_KEY, new String[] { "waterm", "rastusw"} );

        String currentKey = getCurrentSelectedACKey();
        assertNotNull(currentKey);
        client.keyDown(fieldId, VK_DOWN);

        currentKey = getCurrentSelectedACKey();
        assertNotNull(currentKey);
        // make sure its still the same we should only get two entries
        client.keyDown(fieldId, VK_DOWN);
        String newKey = getCurrentSelectedACKey();
        assertNotNull(newKey);
        assertEquals(currentKey, newKey);

        // test for selection
        client.keyPress(fieldId, VK_ESC);
        client.type(fieldId, "water");
        client.keyPress(fieldId, "\\16");
        waitForACDropDown(fieldId, DROP_DOWN_WAIT);
        assertTrue(isVisibleACDropDown(fieldId));
        assertUsersShown(fieldId + ITEM_KEY, new String[] { "waterm", "rastusw"} );
        client.click(fieldId + ITEM_KEY + "waterm");
        assertEquals("waterm", client.getValue(fieldId));

    }
}
