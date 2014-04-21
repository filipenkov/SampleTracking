package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.util.Iterator;
import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestDateRangePicker extends JIRAWebTest
{

    private static final String URL = "/secure/popups/DateRangePicker.jspa";

    public TestDateRangePicker(String name)
    {
        super(name);
    }


    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    /**
     * Tests that all required fields report an error if not set with a value.
     */
    public void testAllMissingValues()
    {
        Map params = EasyMap.build(
                "formName", "",
                "previousFieldName", "",
                "nextFieldName", "",
                "fieldId", "");
        gotoDateRangePicker(params);
        assertValuesOrErrorMessages(params);
    }

    /**
     * Tests that all required fields report an error if not set with a value, plus field ID validates the ID.
     */
    public void testMissingValuesAndInvalidFieldId()
    {
        Map params = EasyMap.build(
                "formName", "",
                "previousFieldName", "",
                "nextFieldName", "",
                "fieldId", "DOESNOTEXIST");
        gotoDateRangePicker(params);
        assertValuesOrErrorMessages(params);

        params = EasyMap.build(
                "formName", "",
                "previousFieldName", "",
                "nextFieldName", "",
                "fieldId", "NOSUCHTHING");
        gotoDateRangePicker(params);
        assertValuesOrErrorMessages(params);
    }

    /**
     * Tests randomly missing values for required fields
     */
    public void testMissingValues()
    {
        Map params = EasyMap.build(
                "formName", "f",
                "previousFieldName", "pfn",
                "nextFieldName", "nfn",
                "fieldId", "");
        gotoDateRangePicker(params);
        assertValuesOrErrorMessages(params);

        params = EasyMap.build(
                "formName", "f",
                "previousFieldName", "",
                "nextFieldName", "nfn",
                "fieldId", "");
        gotoDateRangePicker(params);
        assertValuesOrErrorMessages(params);

        params = EasyMap.build(
                "formName", "",
                "previousFieldName", "pfn",
                "nextFieldName", "nfn",
                "fieldId", "");
        gotoDateRangePicker(params);
        assertValuesOrErrorMessages(params);

        params = EasyMap.build(
                "formName", "f",
                "previousFieldName", "pfn",
                "nextFieldName", "",
                "fieldId", "");
        gotoDateRangePicker(params);
        assertValuesOrErrorMessages(params);
    }

    /**
     * Tests that no errors are reported when all required fields have valid values
     */
    public void testNoErrors()
    {
        Map params = EasyMap.build(
                "formName", "f",
                "previousFieldName", "pfn",
                "nextFieldName", "nfn",
                "fieldId", "environment");
        gotoDateRangePicker(params);
        assertNoErrorMessages(params);
    }

    /**
     * Constructs the url to go to using given map of parameters
     * @param params parameter map (name-value)
     */
    private void gotoDateRangePicker(Map params)
    {
        StringBuilder url = new StringBuilder();
        for (Iterator i = params.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();
            url.append("&");
            url.append(entry.getKey());
            url.append("=");
            url.append(entry.getValue());
        }
        url.replace(0, 1, "?");
        gotoPage(URL + url.toString());
    }

    private void assertNoErrorMessages(Map params)
    {
        for (Iterator i = params.keySet().iterator(); i.hasNext();)
        {
            assertFieldErrorMessageNotPresent((String) i.next());
        }
    }

    private void assertValuesOrErrorMessages(Map params)
    {
        for (Iterator i = params.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();
            String paramName = (String) entry.getKey();
            String paramValue = (String) entry.getValue();
            assertValueOrErrorMessage(paramName, paramValue);
        }
    }

    private void assertValueOrErrorMessage(String paramName, String paramValue)
    {
        final boolean valueNotSet = paramValue.length() == 0;
        if ("fieldId".equals(paramName))
        {
            if (valueNotSet)
            {
                assertTextNotPresent("Invalid field! Field with ID = " + paramValue + " not found!");
            }
            else
            {
                assertTextPresent("Invalid field! Field with ID = " + paramValue + " not found!");
            }
        }
        if (valueNotSet)
        {
            assertFieldErrorMessagePresent(paramName);
        }
        else
        {
            assertFieldErrorMessageNotPresent(paramName);
        }
    }

    private void assertFieldErrorMessagePresent(String field)
    {
        assertTextPresent("Value for " + field + " field was not set!");
    }

    private void assertFieldErrorMessageNotPresent(String field)
    {
        assertTextNotPresent("Value for " + field + " field was not set!");
    }

}
