package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.email.EmailKit;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.icegreen.greenmail.util.GreenMail;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Testing the upgrade task correctly updates saved filters as well as sending out emails to users.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.UPGRADE_TASKS })
public class TestUpgradeTask604 extends EmailFuncTestCase
{
    public void testDoUpgrade() throws Exception
    {
        // -- admin's filters --
        // BadXml 10020
        // FullConvert 10022
        // NoHandler 10023
        // BestGuess 10024
        // FailedToMapDocConst 10025

        // -- fred's filters --
        // OrderFailNoSortEl 10026
        // OrderFailNoSortField 10027
        // OrderFailNoClauseName 10028

        administration.restoreBlankInstance();

        //check if mail sending is disabled
        assertSendingMailIsEnabled();

        // start up GreenMail but dont configure it in JIRA
        final GreenMail greenMail = configureAndStartGreenMailSmtp();

        // mail server will be configured in the XML data
        final Map<String, String> replaceTokens = new LinkedHashMap<String, String>();
        replaceTokens.put("@@SMTPPORT@@", Integer.toString(greenMail.getSmtp().getPort()));
        replaceTokens.put("@@CREATED_FROM@@", createDate(2009, Calendar.FEBRUARY, 1));
        replaceTokens.put("@@CREATED_TO@@", createDate(2009, Calendar.MAY, 1));
        replaceTokens.put("@@UPDATED_FROM@@", createDate(2009, Calendar.JANUARY, 1));
        replaceTokens.put("@@UPDATED_TO@@", createDate(2009, Calendar.JUNE, 1));
        replaceTokens.put("@@DATEPICK_FROM@@", createDate(2009, Calendar.AUGUST, 1, 11, 11));
        replaceTokens.put("@@DATEPICK_TO@@", createDate(2009, Calendar.SEPTEMBER, 1, 12, 12));
        administration.restoreDataWithReplacedTokens("TestUpgradeTask604.xml", replaceTokens);

        // 2 emails - one for each user
        flushMailQueueAndWait(2);

        MimeMessage[] messages = getGreenMail().getReceivedMessages();

        MimeMessage currentMessage = EmailKit.findMessageAddressedTo("admin@example.com", messages);
        assertNotNull(currentMessage);
        String body = EmailKit.getBody(currentMessage);
        text.assertTextSequence(body, "BadXml", "/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=10020");
        text.assertTextSequence(body, "NoHandler", "/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=10023");
        text.assertTextSequence(body, "BestGuess", "/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=10024");
        text.assertTextSequence(body, "FailedToMapDocConst", "/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=10025");
        text.assertTextNotPresent(body, "FullConvert");

        currentMessage = EmailKit.findMessageAddressedTo("fred@example.com", messages);
        assertNotNull(currentMessage);
        body = EmailKit.getBody(currentMessage);
        text.assertTextSequence(body, "OrderFailNoSortEl", "/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=10026");
        text.assertTextSequence(body, "OrderFailNoSortField", "/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=10027");
        text.assertTextSequence(body, "OrderFailNoClauseName", "/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=10028");

        // the ones that converted successfully should still return the right result
        List<SearchResultsCondition> conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new ContainsIssueKeysCondition(assertions.getTextAssertions(), "HSP-2"));
        conditions.add(new NumberOfIssuesCondition(assertions.getTextAssertions(), 1));

        navigation.issueNavigator().loadFilter(10022L, null);
        assertions.getIssueNavigatorAssertions().assertSearchResults(conditions);
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.setWorkingForm("jqlform");
        final String convertedJql = tester.getDialog().getForm().getParameterValue("jqlQuery");
        assertTrue(convertedJql.contains("project = HSP"));
        assertTrue(convertedJql.contains("issuetype = \"New Feature\""));
        assertTrue(convertedJql.contains("summary ~ SOAP"));
        assertTrue(convertedJql.contains("description ~ SOAP"));
        assertTrue(convertedJql.contains("fixVersion = EMPTY"));
        assertTrue(convertedJql.contains("component = \"New Component 3\""));
        assertTrue(convertedJql.contains("affectedVersion = EMPTY"));
        assertTrue(convertedJql.contains("reporter = currentUser()"));
        assertTrue(convertedJql.contains("assignee = admin"));
        assertTrue(convertedJql.contains("status = Open"));
        assertTrue(convertedJql.contains("resolution = Unresolved"));
        assertTrue(convertedJql.contains("priority = Major"));
        assertTrue(convertedJql.contains("created >= 2009-02-01"));
        assertTrue(convertedJql.contains("created <= 2009-05-01"));
        assertTrue(convertedJql.contains("updated >= 2009-01-01"));
        assertTrue(convertedJql.contains("updated <= 2009-06-01"));

        // these custom field names don't conflict
        assertTrue(convertedJql.contains("\"custom field 1\" ~ abc"));
        assertTrue(convertedJql.contains("\"custom field 2\" ~ \"123\""));
        assertTrue(convertedJql.contains("\"custom field 3\" in cascadeOption(10000, 10002)"));

        // project picker has name "project" which conflicts with system field
        assertTrue(convertedJql.contains("cf[10020] = HSP"));

        navigation.issueNavigator().clickEditModeFlipLink();
        assertEquals(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, navigation.issueNavigator().getCurrentEditMode());

        conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new ContainsIssueKeysCondition(assertions.getTextAssertions(), "HSP-2", "HSP-1"));
        conditions.add(new NumberOfIssuesCondition(assertions.getTextAssertions(), 2));

        navigation.issueNavigator().loadFilter(10024L, null);
        assertions.getIssueNavigatorAssertions().assertSearchResults(conditions);

        // 10029 didn't belong to anyone, but is shared with all. it was also "best guess" so just confirm the query came out as expected
        conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new ContainsIssueKeysCondition(assertions.getTextAssertions(), "HSP-2", "HSP-1"));
        conditions.add(new NumberOfIssuesCondition(assertions.getTextAssertions(), 2));
        navigation.issueNavigator().loadFilter(10029L, null);
        assertions.getIssueNavigatorAssertions().assertSearchResults(conditions);
        assertJqlQueryInTextArea(10029, "project in (10000, 10001) order by key desc");

        // 10031 and 10032 use the flags for issue types, components and versions
        assertJqlQueryInTextArea(10031, "issuetype in standardissuetypes() and component = empty and fixversion in unreleasedversions() and affectedversion in releasedversions()");
        assertJqlQueryInTextArea(10032, "issuetype in subtaskissuetypes() and fixversion = empty");

        // 10033 references a field which is invisible to the user - it was still converted, however the search won't run
        assertJqlQueryInTextArea(10033, "cf[10002] ~ invisible");

        // 10034 references a date custom field which has a non-midnight date, so the left hand side is namified, but not the right
        assertJqlQueryInTextArea(10034, "MyDateP >= \"2009-08-01 11:11\" AND MyDateP <= \"2009-09-01 12:12\"");

        // the order by failures arent as catastrophic so we should still have some converted JQL to check
        // but we must be fred to see them
        navigation.logout();
        navigation.login(FRED_USERNAME);
        assertJqlQueryInTextArea(10026, "project = hsp order by updated desc");
        assertJqlQueryInTextArea(10027, "project = hsp order by created desc");
        assertJqlQueryInTextArea(10028, "project = hsp");
    }

    private void assertJqlQueryInTextArea(final int filterId, final String expectedJqlQuery)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=" + filterId);
        tester.setWorkingForm("jqlform");
        String jql = tester.getDialog().getForm().getParameterValue("jqlQuery");
        assertEquals(expectedJqlQuery.toLowerCase(), jql.toLowerCase());
    }

    private static String createDate(final int year, final int month, final int day)
    {
        return createDate(year, month, day, 0, 0);
    }

    private static String createDate(final int year, final int month, final int day, final int hour, final int minute)
    {
        final Calendar instance = Calendar.getInstance();
        instance.setLenient(true);
        instance.set(year, month, day, hour, minute, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return String.valueOf(instance.getTime().getTime());
    }
}
