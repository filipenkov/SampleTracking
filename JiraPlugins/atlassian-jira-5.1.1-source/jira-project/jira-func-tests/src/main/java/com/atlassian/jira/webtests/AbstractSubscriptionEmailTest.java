package com.atlassian.jira.webtests;

import com.icegreen.greenmail.util.GreenMailUtil;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract class for tests that check the integrity of subscription e-mails.
 *
 * @since v3.13
 */

public abstract class AbstractSubscriptionEmailTest extends EmailFuncTestCase
{
    /**
     * Regular expression that links to the issue navigator should show.
     */
    private static final String VIEW_FILTER_REGEX = "secure/IssueNavigator.jspa\\?mode=hide\\&requestId=\\d+";

    /**
     * Regular expression that editing subscriptions should follow.
     */
    private static final String EDIT_FILTER_REGEX = "secure/FilterSubscription\\!default.jspa\\?subId=\\d+&filterId=\\d+";

    /**
     * Regular expression for the issue header.
     */
    private static final String TEXT_HEADER_REGEX = "Key\\s*Summary";

    /**
     * List of HTML table parameters in the HTML e-mail.
     */
    private static final String[] HTML_TABLE_HEADER = new String[] { "Key", "Summary", "Assignee", "Reporter", "Status",
                                                                     "Created", "Updated", "Due" };
    /**
     * Assert that a text subscription has the correct header, start and issue content. The optional content at the end
     * of the e-mail will also need to be validated.
     *
     * @param message  the message to validate.
     * @param config   the filter that generated the subscription e-mail.
     * @param from     the expected location where the e-mail should be from.
     * @param to       the expected location where the e-mail should be delivered to.
     * @param userName the username to associated with the e-mail. This is the person who created the subscroption (not
     *                 the filter).
     * @return the passed string minus all the content at the top of the email that has already been validated.
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    protected String assertTextMessageValid(MimeMessage message, FilterConfig config, String from, String to, String userName)
            throws Exception
    {
        assertMessageHeader(message, from, to, config);

        String body = GreenMailUtil.getBody(message);
        body = assertTextMessageStart(body, config, userName);
        return assertIssuesInTextMessage(config, body);
    }

    /**
     * Assert that a html subscription has the correct header, start and issue content. The optional content at the end
     * of the e-mail will also need to be validated.
     *
     * @param message  the message to validate.
     * @param config   the filter that generated the subscription e-mail.
     * @param from     the expected location where the e-mail should be from.
     * @param to       the expected location where the e-mail should be delivered to.
     * @param userName the username to associated with the e-mail. This is the person who created the subscroption (not
     *                 the filter).
     * @return the passed string minus all the content at the top of the email that has already been validated.
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */
    protected String assertHtmlMessageValid(MimeMessage message, FilterConfig config, String from, String to, String userName)
            throws Exception
    {
        assertMessageHeader(message, from, to, config);

        String body = GreenMailUtil.getBody(message);
        body = assertHtmlMessageStart(body, config, userName);
        assertHtmlMessageFooter(body);
        return assertIssuesInHtmlMessage(config, body);
    }

    /**
     * Ensure that the mail header is correct. This essentially ensures that the TO, FROM and Subject headers
     * are as expected.
     *
     * @param message      the message to validate.
     * @param from         address that the message should be from.
     * @param to           address that the message should be to.
     * @param filterConfig the filter that the message was generated from. This is used to generate the expected
     *                     subject.
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */
    private void assertMessageHeader(MimeMessage message, String from, String to, FilterConfig filterConfig)
            throws Exception
    {
        //lets check for from address
        assertEmailFromEquals(message, from);

        //lets check the to address
        assertEmailToEquals(message, to);

        //lets check the sibject is correct.
        assertEmailSubjectEquals(message, EmailFuncTestCase.DEFAULT_SUBJECT_PREFIX + " Subscription: " + filterConfig.getFilterName());
    }

    /**
     * Asserts that the start of a text message is correct.
     *
     * @param body         the body of the message to validate.
     * @param filterConfig the filter the message was generated from.
     * @param userName     the user that owns the subscription.
     * @return a string of the message minus the validate message start. This can be used to validate the
     *         contents of the body.
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    private String assertTextMessageStart(String body, FilterConfig filterConfig, String userName)
            throws Exception
    {

        //there should be no html.
        assertNotStringContains(body, "<html>");

        //this should be the title of the e-mail.
        body = assertStringContains(body, "Issue Subscription");

        //lets check the filter description.
        String filterStr = "Filter: " + filterConfig.getFilterName() + " " + getFilterStatus(filterConfig);
        body = assertStringContains(body, filterStr);

        //if there is a filter description, it should come next.
        if (filterConfig.getFilterDescription() != null)
        {
            body = assertStringContains(body, filterConfig.getFilterDescription());
        }

        //lets check to see the subscriber is there?
        String subscriber = "Subscriber: " + userName;
        return assertStringContains(body, subscriber);
    }

    /**
     * Asserts that the start of a html message is correct.
     *
     * @param body         the body of the message to validate.
     * @param filterConfig the filter the message was generated from.
     * @param username     the user that owns the subscription.
     * @return a string of the message minus the validate message start. This can be used to validate the
     *         contents of the end of the mesage.
     */

    private String assertHtmlMessageStart(String body, FilterConfig filterConfig, String username)
    {
        //there should be html.
        assertStringContains(body, "<html>");

        //this should be the title of the e-mail.
        body = assertStringContains(body, "Issue Subscription");

        //assert the filter status.
        body = assertStringContains(body, "Filter");
        body = assertStringMatchesRegex(body, VIEW_FILTER_REGEX);
        body = assertStringContains(body, filterConfig.getFilterName());
        body = assertStringContains(body, getFilterStatus(filterConfig));

        //assert the filter owner.
        return assertStringContains(body, username);
    }

    /**
     * Asserts that the start of a html message is correct.
     *
     * @param body         the body of the message to validate.
     */
    private void assertHtmlMessageFooter(String body)
    {
        assertStringContains(body, "This message is automatically generated by JIRA.");
    }

    /**
     * Make sure all the issues exist as expected in the text message.
     *
     * @param filterConfig the configuration of the filter used to create the message.
     * @param body         the body of the e-mail message.
     * @return a string of the message minus the validate message start. This can be used to validate the
     *         contents of body of the message.
     */

    private String assertIssuesInTextMessage(FilterConfig filterConfig, String body)
    {
        //heading should only be displayed when there are issues.
        if (filterConfig.getTotalIssues() > 0)
        {
            //make sure the header is there.
            body = assertStringMatchesRegex(body, TEXT_HEADER_REGEX);

            //make sure the issues is actually there.
            for (Iterator issuesIter = filterConfig.getIssueIterator(); issuesIter.hasNext();)
            {
                //there should be an issue key...
                Integer issue = (Integer) issuesIter.next();
                String issueKey = filterConfig.getProjectKey() + "-" + issue;
                body = assertStringContains(body, issueKey);

                //followed by a browse URL.
                String url = "browse/" + issueKey;
                body = assertStringContains(body, url);
            }
        }
        else
        {
            assertNotStringMatchesRegex(body, TEXT_HEADER_REGEX);
        }

        //No more issue keys should be mentioned.
        assertNotStringMatchesRegex(body, filterConfig.getProjectKey() + "-\\d+");

        return body;
    }

    /**
     * Make sure all the issues exist in the html message.
     *
     * @param filterConfig the configuration of the filter used to generate the message.
     * @param body         the body of the e-mail.
     * @return a string of the message minus the validate message start. This can be used to validate the
     *         contents of the end of the mesage.
     */

    private String assertIssuesInHtmlMessage(FilterConfig filterConfig, String body)
    {
        if (filterConfig.getTotalIssues() > 0)
        {
            //if there are issues then some of them should be mentioned. Check the table header.
            body = assertStringContains(body, HTML_TABLE_HEADER);

            //look for the issues in the message.
            for (Iterator issuesIter = filterConfig.getIssueIterator(); issuesIter.hasNext();)
            {
                Integer issue = (Integer) issuesIter.next();
                final String issueKey = filterConfig.getProjectKey() + "-" + issue;
                final String url = "browse/" + issueKey;

                //there is three links in the table to the issue.
                body = assertStringContains(body, url);
                body = assertStringContains(body, url);
                //there should also be an issue key.
                body = assertStringContains(body, issueKey);
                body = assertStringContains(body, url);
            }
        }
        else
        {
            assertNotStringContains(body, HTML_TABLE_HEADER);
        }

        //No more issue keys should be mentioned.
        assertNotStringMatchesRegex(body, filterConfig.getProjectKey() + "-\\d+");

        return body;
    }

    /**
     * Ensure that the passed message has a link to display the full results of the search.
     *
     * @param filterConfig the filter that produced the e-mail.
     * @param body         the body of the e-mal.
     * @return a string of the message minus the partial link.
     */
    protected String assertPartialLink(FilterConfig filterConfig, String body)
    {
        String partialString = "Displaying " + filterConfig.getReturnedIssues() + " of " + filterConfig.getTotalIssues() +
                               " matched issues. You may view all matched issues";

        body = assertStringContains(body, partialString);
        return assertStringMatchesRegex(body, VIEW_FILTER_REGEX);
    }

    /**
     * Ensure that the passed message does not have a link to see full seach results as all issues can be seen
     * in the e-mail.
     *
     * @param body the body of the e-mail to search.
     */

    protected void assertNotPartialLink(String body)
    {
        assertNotStringContains(body, "You may view all matched issues");
    }

    /**
     * Ensure that the message has a link to edit the subscription.
     *
     * @param body the body of the message to check.
     * @return a string of the message minus the edit link.
     */

    protected String assertEditLink(String body)
    {
        body = assertStringContains(body, "You may edit this subscription");
        return assertStringMatchesRegex(body, EDIT_FILTER_REGEX);
    }

    /**
     * Ensure that the message does not have a link to edit the subscription.
     *
     * @param body the string of the message minus the edit link.
     */

    protected void assertNotEditLink(String body)
    {
        assertNotStringContains(body, "You may edit this subscription");
    }

    /**
     * Ensure that the passed string is not contained within another.
     *
     * @param string    the string to search through.
     * @param substring the string to search for.
     */

    private void assertNotStringContains(String string, String substring)
    {
        if (string.indexOf(substring) >= 0)
        {
            fail("String '" + string + "' shouldn't contain '" + substring + "'.");
        }
    }

    /**
     * Ensure that the passed string does not contain the passed substrings in the specified order.
     *
     * @param string     the string to search through.
     * @param substrings the list of strings to search for. This method will fail only when the passed
     *                   strings are found in the order given in the passed array.
     */
    private void assertNotStringContains(String string, String substrings[])
    {
        for (int i = 0; string != null && i < substrings.length; i++)
        {
            String substring = substrings[i];
            string = findStringInString(string, substring);
        }

        if (string != null)
        {
            fail("Found strings '" + Arrays.asList(substrings).toString() + "' in text.");
        }
    }

    /**
     * Ensure that the passed string contains the passed substring.
     *
     * @param string    the search to seach through.
     * @param substring the string to search for.
     * @return string minus the match and all text that preceeded it.
     */

    private String assertStringContains(String string, String substring)
    {
        String result = findStringInString(string, substring);
        if (result == null)
        {
            fail("Unable to find string '" + substring + "' in " + string);
        }

        return result;
    }

    /**
     * Ensure that the passed string contains the passed substrings in the specified order.
     *
     * @param string     string the string to search through.
     * @param substrings the list of strings to search for. This method will work only when the passed
     *                   strings are found in the order given in the passed array.
     * @return the passed string minus any strings that where found.
     */

    private String assertStringContains(String string, String[] substrings)
    {
        for (int i = 0; i < substrings.length; i++)
        {
            String substring = substrings[i];
            string = assertStringContains(string, substring);
        }

        return string;
    }

    /**
     * Search the passed sting for the passed substring.
     *
     * @param string    The string to search.
     * @param substring The sub-string to search for.
     * @return string minus the match and any text leading to the match. It will return null if a match could not
     *         be found.
     */
    private static String findStringInString(String string, String substring)
    {
        int pos = string.indexOf(substring);
        if (pos < 0)
        {
            return null;
        }
        else
        {
            return string.substring(pos + substring.length());
        }
    }

    /**
     * Ensure that the passed string matches the passed regular expression.
     *
     * @param string the string to match against.
     * @param regex  the regular expression to use in the query.
     * @return string minus any text upto and including the regular expression.
     */

    private String assertStringMatchesRegex(String string, String regex)
    {
        String result = findRegexInString(string, regex);
        if (result == null)
        {
            fail("Unable to find regex '" + regex + "' in string '" + string + "'.");
        }
        return result;
    }

    /**
     * Ensure that the passed string does not match the passed regular expression.
     *
     * @param string the string to search through.
     * @param regex  the regular expression to try to match.
     */

    private void assertNotStringMatchesRegex(String string, String regex)
    {
        if (findRegexInString(string, regex) != null)
        {
            fail("The regular expression '" + regex + "' should not match the string '" + string + "'.");
        }
    }

    /**
     * Search through the passed string for the passed regular expression.
     *
     * @param string the string to match against.
     * @param regex  the regular expression to use in the query.
     * @return string minus any text upto and including the regular expression.
     */

    private static String findRegexInString(String string, String regex)
    {
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher match = pattern.matcher(string);
        if (!match.find())
        {
            return null;
        }
        return string.substring(match.end());
    }

    /**
     * Return the expected e-mail filter status for the passed filter configuration.
     *
     * @param filterConfig the configuration to generate the status string from.
     * @return the filter status string from the passed configuration.
     */

    private static String getFilterStatus(FilterConfig filterConfig)
    {
        if (!filterConfig.isPaged())
        {
            if (filterConfig.getTotalIssues() == 1)
            {
               return "(" + filterConfig.getTotalIssues() + " issue)";
            }
           return "(" + filterConfig.getTotalIssues() + " issues)";
        }
        else
        {
            return "(" + filterConfig.getReturnedIssues() + " of " + filterConfig.getTotalIssues() + " issues)";
        }
    }

    /**
     * Goto the page that allows the passed filter to be subscribed to.
     *
     * @param config the filter configuration.
     */

    private void gotoSubscribeFilter(FilterConfig config)
    {
        navigation.manageFilters().allFilters();
        tester.clickLink("subscribe_" + config.getFilterName());
    }

    /**
     * Subscribe the current user to a filter and then run it.
     *
     * @param config the configuration for the filter to subscribe to.
     * @param group  the group to subscribe to the filter for. This can be left null
     *               to indicate that a personal subscription should be made.
     */

    protected void subscribeToFilterAndRun(FilterConfig config, String group)
    {
        gotoSubscribeFilter(config);

        if (group != null)
        {
            tester.setFormElement("groupName", group);
        }

        tester.setFormElement("emailOnEmpty", "true");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 5);

        String cronExpr = "0 0 0 1 1 ? " + calendar.get(Calendar.YEAR);

        tester.setFormElement("filter.subscription.prefix.dailyWeeklyMonthly", "advanced");
        tester.setFormElement("filter.subscription.prefix.cronString", cronExpr);

        tester.submit();

        tester.assertTextPresent("View Subscriptions for " + config.getFilterName());
        tester.assertTextPresent(cronExpr);

        tester.clickLinkWithText("Run now");

        tester.assertTextPresent("View Subscriptions for " + config.getFilterName());
        tester.assertTextPresent(cronExpr);
    }

    /**
     * Holds the configuration for a filter that can be used to create a subscription. Each filter is assumed to query
     * all the issues from a project.
     *
     * @since v3.13
     */

    protected static class FilterConfig
    {
        /*
         * Some admin filters.
         */
        public final static AbstractSubscriptionEmailTest.FilterConfig ADMIN_FILTER_ZERO_RESULTS = new AbstractSubscriptionEmailTest.FilterConfig("ZeroFilter", "This is the zero filter.", "ProjectOne", "ONE", 0, 0);
        public final static AbstractSubscriptionEmailTest.FilterConfig ADMIN_FILTER_PARTIAL;
        public final static AbstractSubscriptionEmailTest.FilterConfig ADMIN_FILTER_FULL;

        /*
         * Filters for fred.
         */

        public final static AbstractSubscriptionEmailTest.FilterConfig FRED_FILTER_ZERO_RESULTS = new AbstractSubscriptionEmailTest.FilterConfig("FredZeroFilter", "Fred Zero Filter for test.", "ProjectOne", "ONE", 0, 0);
        public final static AbstractSubscriptionEmailTest.FilterConfig FRED_FILTER_PARTIAL;
        public final static AbstractSubscriptionEmailTest.FilterConfig FRED_FILTER_FULL;

        static
        {
            ADMIN_FILTER_PARTIAL = new AbstractSubscriptionEmailTest.FilterConfig("FilterOne", "Filter One Description.", "ProjectOne", "ONE", 200, 210)
            {
                public Iterator getIssueIterator()
                {
                    return new AbstractSubscriptionEmailTest.CountingIterator(getTotalIssues(), -getReturnedIssues());
                }
            };

            ADMIN_FILTER_FULL = new AbstractSubscriptionEmailTest.FilterConfig("FilterTwo", null, "ProjectTwo", "TWO", 2, 2)
            {
                public Iterator getIssueIterator()
                {
                    return new AbstractSubscriptionEmailTest.CountingIterator(1, getReturnedIssues());
                }
            };

            FRED_FILTER_PARTIAL = new AbstractSubscriptionEmailTest.FilterConfig("FredFilterOne", null, "ProjectOne", "ONE", 200, 210)
            {
                public Iterator getIssueIterator()
                {
                    return new AbstractSubscriptionEmailTest.CountingIterator(1, getReturnedIssues());
                }
            };

            FRED_FILTER_FULL = new AbstractSubscriptionEmailTest.FilterConfig("FredFilterTwo", "This is a simple filter that will return all results.", "ProjectTwo", "TWO", 2, 2)
            {
                public Iterator getIssueIterator()
                {
                    return new AbstractSubscriptionEmailTest.CountingIterator(getTotalIssues(), -getReturnedIssues());
                }
            };
        }

        private final String filterName;
        private final String filterDescription;
        private final int returnedIssues;
        private final int totalIssues;
        private final String projectName;
        private final String projectKey;

        protected FilterConfig(String filterName, String filterDescription, String projectName, String projectKey, int returnedIssues, int totalIssues)
        {
            this.projectName = projectName;
            this.projectKey = projectKey;
            this.filterName = filterName;
            this.filterDescription = filterDescription;
            this.returnedIssues = returnedIssues;
            this.totalIssues = totalIssues;
        }

        public String getFilterName()
        {
            return filterName;
        }

        public String getFilterDescription()
        {
            return filterDescription;
        }

        public int getReturnedIssues()
        {
            return returnedIssues;
        }

        public int getTotalIssues()
        {
            return totalIssues;
        }

        public boolean isPaged()
        {
            return returnedIssues != totalIssues;
        }

        public Iterator getIssueIterator()
        {
            return new Iterator()
            {
                public boolean hasNext()
                {
                    return false;
                }

                public Object next()
                {
                    throw new IllegalStateException();
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public String getProjectName()
        {
            return projectName;
        }

        public String getProjectKey()
        {
            return projectKey;
        }
    }

    /**
     * Iterator that can be used to return a sequence of numbers.
     *
     * @since v3.13
     */

    protected static class CountingIterator implements Iterator
    {
        private int current;
        private final int endRange;
        private final int increment;

        public CountingIterator(int start, int length)
        {
            endRange = start + length;
            current = start;

            if (length < 0)
            {
                increment = -1;
            }
            else
            {
                increment = 1;
            }
        }

        public boolean hasNext()
        {
            return current != endRange;
        }

        public Object next()
        {
            if (!hasNext())
            {
                throw new IllegalStateException();
            }

            int ret = current;
            current += increment;

            return new Integer(ret);
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
