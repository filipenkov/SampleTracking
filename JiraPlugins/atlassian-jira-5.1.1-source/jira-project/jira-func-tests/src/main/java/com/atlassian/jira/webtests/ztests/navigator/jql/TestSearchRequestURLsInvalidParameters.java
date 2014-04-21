package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.meterware.httpunit.HttpUnitOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSearchRequestURLsInvalidParameters extends FuncTestCase
{
    private static final String QUERY = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?tempMax=1000&";

    public void setUpTest()
    {
        administration.restoreData("TestSearchRequestURLsInvalidParameters.xml");
    }

    @Override
    protected void setUpHttpUnitOptions()
    {
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        super.setUpHttpUnitOptions();
    }

    public void testEverySingleField()
    {
        final List<Scenario> urls = new ArrayList<Scenario>();

        // system fields
        urls.addAll(affectedVersion());
        urls.addAll(comment());
        urls.addAll(component());
        urls.addAll(created());
        urls.addAll(dueDate());
        urls.addAll(updated());
        urls.addAll(resolutionDate());
        urls.addAll(description());
        urls.addAll(environment());
        urls.addAll(summary());
        urls.addAll(fixVersion());
        urls.addAll(priority());
        urls.addAll(project());
        urls.addAll(resolution());
        urls.addAll(status());
        urls.addAll(type());
        urls.addAll(workRatio());

        // custom fields
        urls.addAll(datePicker());
        urls.addAll(dateTime());
        urls.addAll(freeTextField());
        urls.addAll(readOnlyTextField());
        urls.addAll(textField());
        urls.addAll(groupPicker());
        urls.addAll(importIdExactSearcher());
        urls.addAll(importIdRangeSearcher());
        urls.addAll(cascadingSelect());
        urls.addAll(numberFieldExactSearcher());
        urls.addAll(numberFieldRangeSearcher());
        urls.addAll(singleVersionPicker());
        urls.addAll(versionPicker());
        urls.addAll(projectPicker());
        urls.addAll(multiGroupPicker());
        urls.addAll(multiCheckboxes());
        urls.addAll(multiSelect());
        urls.addAll(selectList());
        urls.addAll(radioButton());

        for (final Scenario url : urls)
        {
            assertErrorResponse(url);
        }
    }

    public void testEverySingleWarningField()
    {
        final List<Scenario> urls = new ArrayList<Scenario>();

        // system fields
        urls.addAll(assignee());
        urls.addAll(reporter());

        // custom fields
        urls.addAll(userPicker());
        urls.addAll(multiUserPicker());

        for (final Scenario url : urls)
        {
            assertWarningResponse(url);
        }
    }

    private List<Scenario> affectedVersion()
    {
        return buildIdScenarios("version", "affectedVersion", "10011");
    }

    private List<Scenario> assignee()
    {
        return buildUserScenarios("assignee", "assignee");
    }

    private List<Scenario> comment()
    {
        return buildSystemTextScenario("body", "comment");
    }

    private List<Scenario> component()
    {
        return buildIdScenarios("component", "component", "10010");
    }

    private List<Scenario> created()
    {
        return buildDateScenarios("created");
    }

    private List<Scenario> dueDate()
    {
        return buildRelativeDateScenarios("duedate", "due");
    }

    private List<Scenario> updated()
    {
        return buildDateScenarios("updated");
    }

    private List<Scenario> resolutionDate()
    {
        return buildDateScenarios("resolutiondate", "resolved");
    }

    private List<Scenario> description()
    {
        return buildSystemTextScenario("description", "description");
    }

    private List<Scenario> environment()
    {
        return buildSystemTextScenario("environment", "environment");
    }

    private List<Scenario> summary()
    {
        return buildSystemTextScenario("summary", "summary");
    }

    private List<Scenario> fixVersion()
    {
        return buildIdScenarios("fixfor", "fixVersion", "10011");
    }

    private List<Scenario> priority()
    {
        return buildIdScenarios("priority", "priority", "1");
    }

    private List<Scenario> project()
    {
        return buildIdScenarios("pid", "project", "10010");
    }

    private List<Scenario> resolution()
    {
        return buildIdScenarios("resolution", "resolution", "1");
    }

    private List<Scenario> status()
    {
        return buildIdScenarios("status", "status", "1");
    }

    private List<Scenario> type()
    {
        return buildIdScenarios("type", "issuetype", "1");
    }

    private List<Scenario> reporter()
    {
        return buildUserScenarios("reporter", "reporter");
    }

    private List<Scenario> workRatio()
    {
        final String field = "workratio";

        return CollectionBuilder.newBuilder(
                new Scenario(field + ":min=20&" + field + ":max=baddate", ErrorType.REQUIRES_INTEGER.formatError(field, "baddate")),
                new Scenario(field + ":min=baddate", ErrorType.REQUIRES_INTEGER.formatError(field, "baddate")),
                new Scenario(field + ":max=baddate", ErrorType.REQUIRES_INTEGER.formatError(field, "baddate")),
                new Scenario(field + ":max=20&" + field + ":min=baddate", ErrorType.REQUIRES_INTEGER.formatError(field, "baddate"))).asList();
    }

    private List<Scenario> datePicker()
    {
        return buildRelativeDateScenarios("customfield_10001", "DP");
    }

    private List<Scenario> dateTime()
    {
        final String urlParam = "customfield_10002";
        final String field = "DT";

        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + ":before=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":after=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":next=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":before=11/May/08+11:34+AM&" + urlParam + ":after=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=1h&" + urlParam + ":next=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH"))
        ).asList();
    }

    private List<Scenario> freeTextField()
    {
        return buildTextScenario("customfield_10003", "FTF");
    }

    private List<Scenario> readOnlyTextField()
    {
        return buildTextScenario("customfield_10013", "ROTF");
    }

    private List<Scenario> textField()
    {
        return buildTextScenario("customfield_10016", "TF");
    }

    private List<Scenario> groupPicker()
    {
        final String urlParam = "customfield_10004";
        final String field = "GP";
        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + "=1", ErrorType.GROUP_DOESNT_EXIST.formatError(field, "1"))
        ).asList();
    }

    private List<Scenario> importIdExactSearcher()
    {
        final String urlParam = "customfield_10005";
        final String field = "II exact";

        return buildExactNumberScenarios(urlParam, field);
    }

    private List<Scenario> importIdRangeSearcher()
    {
        final String urlParam = "customfield_10020";
        final String field = "II range";

        return buildRangeNumberScenarios(urlParam, field);
    }

    private List<Scenario> cascadingSelect()
    {
        final String urlParam = "customfield_10000";
        final String field = "CSF";

        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + "=10010&" + urlParam + ":1=invalid", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "invalid")),
                new Scenario(urlParam + "=yes", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "yes")),
                new Scenario(urlParam + "=20", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "20")),
                new Scenario(urlParam + ":1=yes", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "yes")),
                new Scenario(urlParam + ":1=20", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "20"))
        ).asList();
    }

    private List<Scenario> numberFieldExactSearcher()
    {
        return buildExactNumberScenarios("customfield_10010", "NF exact");
    }

    private List<Scenario> numberFieldRangeSearcher()
    {
        return buildRangeNumberScenarios("customfield_10021", "NF range");
    }

    private List<Scenario> userPicker()
    {
        return buildUserScenarios("customfield_10018", "UP");
    }

    private List<Scenario> singleVersionPicker()
    {
        return buildIdScenarios("customfield_10015", "SVP", "10011");
    }

    private List<Scenario> versionPicker()
    {
        return buildIdScenarios("customfield_10019", "VP", "10011");
    }

    private List<Scenario> projectPicker()
    {
        final String urlName = "customfield_10011";
        final String field = "PP";
        return CollectionBuilder.newBuilder(
                new Scenario(urlName + "=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError(field, "yes")),
                new Scenario(urlName + "=50000", ErrorType.NUMBER_VALUE_DOESNT_EXIST.formatError(field, "50000"))
        ).asList();
    }

    private List<Scenario> multiGroupPicker()
    {
        final String urlParam = "customfield_10007";
        final String field = "MGP";
        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + "=1", ErrorType.GROUP_DOESNT_EXIST.formatError(field, "1"))).asList();
    }

    private List<Scenario> multiUserPicker()
    {
        return buildUserScenarios("customfield_10009", "MUP");
    }

    private List<Scenario> multiCheckboxes()
    {
        final String urlParam = "customfield_10006";
        final String field = "MC";
        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1")),
                new Scenario(urlParam + "=opt1&" + urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1"))
        ).asList();
    }

    private List<Scenario> multiSelect()
    {
        final String urlParam = "customfield_10008";
        final String field = "MS";
        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1")),
                new Scenario(urlParam + "=select1&" + urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1"))
        ).asList();
    }

    private List<Scenario> selectList()
    {
        final String urlParam = "customfield_10014";
        final String field = "SL";
        return CollectionBuilder.newBuilder(new Scenario(urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1"))).asList();
    }

    private List<Scenario> radioButton()
    {
        final String urlParam = "customfield_10012";
        final String field = "RB";
        return CollectionBuilder.newBuilder(new Scenario(urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1"))).asList();
    }

    private List<Scenario> buildUserScenarios(final String urlParam, final String field)
    {
        return CollectionBuilder.newBuilder(new Scenario(urlParam + "=1", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError(field, "1"))).asList();
    }

    private List<Scenario> buildSystemTextScenario(final String urlParam, final String field)
    {
        return CollectionBuilder.newBuilder(new Scenario("query=%3Fbadtextquery&" + urlParam + "=true", ErrorType.TEXT_START_CHAR.formatError(field, "?badtextquery"))).asList();
    }

    private List<Scenario> buildTextScenario(final String urlParam, final String field)
    {
        return CollectionBuilder.newBuilder(new Scenario(urlParam + "=%3Fbadtextquery", ErrorType.TEXT_START_CHAR.formatError(field, "?badtextquery"))).asList();
    }

    private List<Scenario> buildDateScenarios(final String field)
    {
        return buildDateScenarios(field, field);
    }

    private List<Scenario> buildRelativeDateScenarios(final String field)
    {
        return buildDateScenarios(field, field);
    }

    private List<Scenario> buildRelativeDateScenarios(final String urlParam, final String field)
    {
        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + ":before=baddate", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":after=baddate", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=HHH", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":next=HHH", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":equals=HHH", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":before=11/May/08&" + urlParam + ":after=baddate", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=1h&" + urlParam + ":next=HHH", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "HHH"))
        ).asList();
    }

    private List<Scenario> buildDateScenarios(final String urlParam, final String field)
    {
        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + ":before=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":after=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":next=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":before=11/May/08&" + urlParam + ":after=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=1h&" + urlParam + ":next=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH"))
        ).asList();
    }

    private List<Scenario> buildIdScenarios(final String urlName, final String field, final String validId)
    {
        return CollectionBuilder.newBuilder(
                new Scenario(urlName + "=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError(field, "yes")),
                new Scenario(urlName + "=50000", ErrorType.NUMBER_VALUE_DOESNT_EXIST.formatError(field, "50000")),
                new Scenario(urlName + "=" + validId + "&" + urlName + "=50000", ErrorType.NUMBER_VALUE_DOESNT_EXIST.formatError(field, "50000"))
        ).asList();
    }

    private List<Scenario> buildExactNumberScenarios(final String urlParam, final String field)
    {
        return CollectionBuilder.newBuilder(new Scenario(urlParam + "=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber"))).asList();
    }

    private List<Scenario> buildRangeNumberScenarios(final String urlParam, final String field)
    {
        return CollectionBuilder.newBuilder(
                new Scenario(urlParam + ":greaterThan=20&" + urlParam + ":lessThan=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber")),
                new Scenario(urlParam + ":greaterThan=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber")),
                new Scenario(urlParam + ":lessThan=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber")),
                new Scenario(urlParam + ":lessThan=20&" + urlParam + ":greaterThan=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber"))
        ).asList();
    }

    private void assertErrorResponse(final Scenario scenario)
    {
        log(QUERY + scenario.urlParams);
        tester.gotoPage(QUERY + scenario.urlParams);
        assertEquals(400, tester.getDialog().getResponse().getResponseCode());
        assertions.html().assertResponseContains(tester, scenario.msg);
    }

    private void assertWarningResponse(final Scenario scenario)
    {
        log(QUERY + scenario.urlParams);
        tester.gotoPage(QUERY + scenario.urlParams);
        assertEquals(200, tester.getDialog().getResponse().getResponseCode());
    }

    private static class Scenario
    {
        private final String urlParams;
        private final String msg;

        private Scenario(final String urlParams, final String msg)
        {
            this.urlParams = urlParams;
            this.msg = msg;
        }
    }

    private static enum ErrorType
    {
        TEXT_START_CHAR()
                {
                    // ignore the actual start char for brevity
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The text query '%s' for field '%s' is not allowed to start with ", value, fieldName);
                    }
                },
        INVALID_DATE()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("Date value '%s' for field '%s' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.", value, fieldName);
                    }
                },
        INVALID_RELATIVE_DATE()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("Date value '%s' for field '%s' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'.", value, fieldName);
                    }
                },
        GROUP_DOESNT_EXIST()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The group '%s' for field '%s' does not exist.", value, fieldName);
                    }
                },
        STRING_VALUE_DOESNT_EXIST()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The value '%s' does not exist for the field '%s'.", value, fieldName);
                    }
                },
        NUMBER_VALUE_DOESNT_EXIST()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("A value with ID '%s' does not exist for the field '%s'.", value, fieldName);
                    }
                },
        REQUIRES_INTEGER()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The value '%s' for field '%s' is invalid - please specify an integer.", value, fieldName);
                    }
                },
        REQUIRES_NUMBER()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("Value '%s' for the '%s' field is not a valid number.", value, fieldName);
                    }
                },
        OPTION_DOESNT_EXIST()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The option '%s' for field '%s' does not exist.", value, fieldName);
                    }};

        abstract String formatError(String fieldName, String value);
    }
}
