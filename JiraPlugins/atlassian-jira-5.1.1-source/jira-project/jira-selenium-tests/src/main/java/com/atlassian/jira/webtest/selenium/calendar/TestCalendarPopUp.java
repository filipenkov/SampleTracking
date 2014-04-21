package com.atlassian.jira.webtest.selenium.calendar;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.components.CalendarPopup;
import com.atlassian.jira.webtest.selenium.framework.dialogs.IssueActionDialog;
import com.atlassian.jira.webtest.selenium.framework.dialogs.LogWorkDialog;
import com.atlassian.jira.webtest.selenium.framework.dialogs.WorkflowTransitionDialog;
import com.atlassian.jira.webtest.selenium.framework.fields.DateFieldWithCalendar;
import com.atlassian.jira.webtest.selenium.framework.model.CancelType;
import com.atlassian.jira.webtest.selenium.framework.model.WorkflowTransition;
import com.google.common.base.Function;
import com.thoughtworks.selenium.Selenium;
import junit.framework.Test;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

@WebTest({Category.SELENIUM_TEST })
public class TestCalendarPopUp extends JiraSeleniumTest
{
    private static final WorkflowTransition TEST = new WorkflowTransition(711, "Test");

    // how long to wait for the calendar to pop up
    protected static final int WAIT_POPUP = 5000;
    private static final String SF_DUEDATE = "duedate-trigger";

    private static final String CF_MY_DATE_PICKER = "customfield_10000-trigger";
    private static final String CF_MY_DATE_TIME_PICKER = "customfield_10001-trigger";

    private static final String ISSUE = "HSP-1";
    private static final String SUMMARY = "Pellentesque vitae justo eget lacus fringilla porta.";
    private static final String DATE_TIME_PICKER_VALUE = "customfield_10001";
    private static final String DATETIMEPICKER_HOURS = "css=span.hour";

    private static final String EN_SUNDAY_SHORT = "Sun";
    private static final String EN_MONDAY_SHORT = "Mon";
    private static final String FIRST_DAY_NAME_CELL_LOCATOR = "jQuery=.name.day:first";
    private static final String WEEK_NUMBER_CELL_LOCATOR_FORMAT = "jQuery=.day.wn:eq(%d)";

    public static Test suite()
    {
        return suiteFor(TestCalendarPopUp.class);
    }

    //JRADEV-2725, JRADEV-2747: Make sure the calendar works in dialogs on the issue navigator.
    public void testDateTimePickerInDialog() throws Exception
    {
        restoreData("TestCalendarInDialog.xml");

        getNavigator().login(ADMIN_USERNAME);
        getNavigator().gotoIssue(ISSUE);

        //Check from the view issue page.
        checkCalendarOnNavigatorDialog(new Function<IssueActionDialog, Void>()
        {
            public Void apply(final IssueActionDialog auiDialog)
            {
                auiDialog.openFromViewIssue();
                return null;
            }
        });

        final Function<IssueActionDialog, Void> navigatorOpener = new Function<IssueActionDialog, Void>()
        {
            public Void apply(final IssueActionDialog auiDialog)
            {
                auiDialog.openFromIssueNav(10000);
                return null;
            }
        };

        final IssueNavigatorNavigation navigatorNavigation = getNavigator().issueNavigator();
        navigatorNavigation.displayAllIssues();

        //Check on the view search page.
        navigatorNavigation.gotoViewMode();
        checkCalendarOnNavigatorDialog(navigatorOpener);

        //Check on the simple edit page.
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        checkCalendarOnNavigatorDialog(navigatorOpener);

        //Check on the advanced edit page.
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        checkCalendarOnNavigatorDialog(navigatorOpener);
    }

    /**
     * This test restores jira from xml which has been prepared to setup jira with datetime formats in 24 mode. The test
     * will then attempt to open the date time picker and click on the hours 24 times validating the new value.
     * <p/>
     * The issue has its datetimepicker custom field set to 13:45 to verify correct init from a 24hour time.
     */
    public void testDateTimePickerWhenSystemIsSetTo24HourMode()
    {
        restoreData("TestCalendarPopUpIn24HourMode.xml");

        // login
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // edit issue and attempt to edit date/time picker field...
        getNavigator().editIssue(ISSUE);

        // if the date time picker isnt visible something has gone really wrong...
        assertThat.elementVisible(CF_MY_DATE_TIME_PICKER);

        // assert that we are editing the correct issue and that is hasnt been tampered with...
        assertThat.textPresent(SUMMARY);

        // click on the datetime picker field
        client.click(CF_MY_DATE_TIME_PICKER);
        waitForCalendarPopUp(WAIT_POPUP);

        // the xml has been cooked so the time is 13hours this ensures the js fix is working
        int hours = 13;

        // we try clicking 24 times to ensure that the hours wrap round correctly...
        for (int i = 1; i < 24; i++)
        {
            // increment hours wrapping around...
            hours = (hours + 1) % 24;

            //Hours string is zero padded (00, 01, 02,....,09, 10,......23)
            final String hoursString = String.format("%02d", hours);
            final String timeString = String.format("24/09/07 %d:45", hours);

            log("  Clicking on hours should update the time to \"" + hoursString + ":45\".");

            // TODO: This does not seem to up the date.
            // enter the new hours entry...
            client.mouseDown(DATETIMEPICKER_HOURS);
            client.click(DATETIMEPICKER_HOURS);
            client.mouseUp(DATETIMEPICKER_HOURS);

            // clicking on the hours control should increment its value..
            assertThat.elementHasText(DATETIMEPICKER_HOURS, hoursString);

            // assert that the date time picker value field has also been correctly updated.
            assertThat.formElementEquals(DATE_TIME_PICKER_VALUE, timeString);
        }
    }

    public void testCalendarMonthsEn() throws Exception
    {
        testCalendarMonths(TestCalendarPopUp.CalendarStrings.CALENDAR_EN_UK);
    }

    public void testCalendarMonthsJp() throws Exception
    {
        testCalendarMonths(TestCalendarPopUp.CalendarStrings.CALENDAR_JP);
    }

    public void testCalendarMonthsFr() throws Exception
    {
        testCalendarMonths(TestCalendarPopUp.CalendarStrings.CALENDAR_FR);
    }

    private void testCalendarMonths(CalendarStrings calendarStrings) throws Exception
    {
        restoreData("TestCalendarPopUp.xml");

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        changeUserLanguage(calendarStrings.locale);
        log("Checking months for pop-up calendar for " + calendarStrings.language);

        for (int i = 0; i < calendarStrings.shortMonths.length; i++)
        {
            getNavigator().editIssue("HSP-1");
            log("  Checking date (20/" + (i + 1) + "/2008): 20/" + calendarStrings.shortMonths[i] + "/2008");
            client.type("duedate", "20/" + calendarStrings.shortMonths[i] + "/2008");
            assertThat.elementVisible(SF_DUEDATE);
            client.click(SF_DUEDATE);
            waitForCalendarPopUp(WAIT_POPUP);

            calendarStrings.assertTextPresent(client, "this month", calendarStrings.longMonths[i]);
            client.click("Id=issue-edit-submit", true);

            assertThat.textPresent("20/" + calendarStrings.shortMonths[i] + "/08");
        }
    }

    //See https://jdog.atlassian.com/browse/JRADEV-7439
    @Ignore("Test passes locally, but somehow our remote agents have problems with japanese character encoding.")
    public void testDueDateCalendarInSeveralLanguages() throws Exception
    {
        restoreData("TestCalendarPopUp.xml");

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        for (CalendarStrings calendarStrings : CalendarStrings.list())
        {
            changeUserLanguage(calendarStrings.locale);
            getNavigator().editIssue("HSP-1");

            assertThat.elementVisible(SF_DUEDATE);
            client.click(SF_DUEDATE);
            waitForCalendarPopUp(WAIT_POPUP);

            calendarStrings.assertDateCalendarValid(client);
        }
    }

    public void testCustomFieldDateCalendarInSeveralLanguages() throws Exception
    {
        restoreData("TestCalendarPopUp.xml");

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        for (CalendarStrings calendarStrings : CalendarStrings.list())
        {
            changeUserLanguage(calendarStrings.locale);
            getNavigator().editIssue("HSP-1");

            assertThat.elementVisible(CF_MY_DATE_PICKER);
            client.click(CF_MY_DATE_PICKER);
            waitForCalendarPopUp(WAIT_POPUP);
            calendarStrings.assertDateCalendarValid(client);
        }
    }

    public void testCustomFieldDateTimeCalendarInSeveralLanguages() throws Exception
    {
        restoreData("TestCalendarPopUp.xml");

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        for (CalendarStrings calendarStrings : CalendarStrings.list())
        {
            changeUserLanguage(calendarStrings.locale);
            getNavigator().editIssue("HSP-1");
            assertThat.elementVisible(CF_MY_DATE_TIME_PICKER);
            client.click(CF_MY_DATE_TIME_PICKER);
            waitForCalendarPopUp(WAIT_POPUP);
            calendarStrings.assertDateTimeCalendarValid(client);
        }
    }

    public void testUsingServerDate() throws Exception
    {
        restoreData("TestCalendarPopUp.xml");

        String serverEval = "dom=this.browserbot.getCurrentWindow().jQuery(\"<input type='text' id='serverdate' name='serverdate' /><button id='getServerDate'</button>\").appendTo(\"body\");\n"
                + "this.browserbot.getCurrentWindow().Calendar.setup({\n"
                + "firstDay : 0, // first day of the week\n"
                + "inputField : \"serverdate\", // id of the input field\n"
                + "button : \"getServerDate\", // trigger for the calendar (button ID)\n"
                + "singleClick : true,\n"
                + "date : 124295067577"
                + "});";

        String clientEval = "dom=this.browserbot.getCurrentWindow().jQuery(\"<input type='text' id='clientdate' name='clientdate' /><button id='getClientDate'</button>\").appendTo(\"body\");\n"
                + "this.browserbot.getCurrentWindow().Calendar.setup({"
                + "firstDay : 0,"
                + "inputField : \"clientdate\","
                + "button : \"getClientDate\","
                + "singleClick : true,"
                + "date : " + System.currentTimeMillis()
                + "});";

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoPage("secure/IssueNavigator.jspa", true);
        // server date
        client.getEval(serverEval);
        client.click("getServerDate");
        String serverMonth = client.getText("css=td.title");
        // reload
        getNavigator().gotoPage("secure/IssueNavigator.jspa", true);
        // client date
        client.getEval(clientEval);
        client.click("getClientDate");
        client.mouseUp("css=td.day.selected.today");
        String clientMonth = client.getText("css=td.title");
        if (serverMonth.equals(clientMonth))
        {
            throw new RuntimeException("Expected Server Date and Client Date to be different");
        }
    }


    public void testISO8601CalendarOff() throws Exception
    {
        iso8601TestRun(false, EN_SUNDAY_SHORT, 0);
    }

    public void testISO8601CalendarOn() throws Exception
    {
        iso8601TestRun(true, EN_MONDAY_SHORT, 1);
    }

    private void iso8601TestRun(boolean isoOn, String expectedFirstDayInWeek, int expectedFirstWeekCellIndex)
            throws Exception
    {
        prepareISOTest(isoOn);
        openCalendarOnIssueEdit();
        assertFirstDayAndWeek(expectedFirstDayInWeek, expectedFirstWeekCellIndex);
    }

    private void prepareISOTest(final boolean isoOn)
    {
        restoreData("TestCalendarPopUpISO8601.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        String sundayFirstInWeekLocale = CalendarStrings.CALENDAR_EN_UK.locale;
        changeUserLanguage(sundayFirstInWeekLocale);
        switchISO8601Calendar(isoOn);
    }


    private void openCalendarOnIssueEdit()
    {
        getNavigator().editIssue("HSP-1");
        assertThat.elementVisible(SF_DUEDATE);
        client.click(SF_DUEDATE);
    }

    private void assertFirstDayAndWeek(final String expectedFirstDayInWeek, final int expectedFirstWeekCellIndex)
    {
        waitForCalendarPopUp(WAIT_POPUP);
        assertThat.elementHasText(FIRST_DAY_NAME_CELL_LOCATOR, expectedFirstDayInWeek);
        assertThat.elementHasText(firstWeekCellLocator(expectedFirstWeekCellIndex), "1");
    }

    private String firstWeekCellLocator(int expectedCellIndex)
    {
        return String.format(WEEK_NUMBER_CELL_LOCATOR_FORMAT, expectedCellIndex);
    }

    private void switchISO8601Calendar(boolean on)
    {
        getNavigator().gotoAdmin().clickAndWaitForPageLoad("lookandfeel").clickAndWaitForPageLoad("editlookandfeel");
        if (on)
        {
            client.check("useISO8601");
        }
        else
        {
            client.uncheck("useISO8601");
        }
        client.submit("jiraform", true);
    }

    private void changeUserLanguage(String language)
    {
        getNavigator().gotoUserProfile();
        client.click("edit_prefs_lnk");
        assertThat.elementPresentByTimeout("update-user-preferences", 10000);
        client.select("userLocale", "value=" + language);
        client.click("id=update-user-preferences-submit", true);
    }

    protected void waitForCalendarPopUp(int maxMillis)
    {
        // The old expression doesn't seem to work (xpath=//div[@class='calendar']). Probably a js bug in selenium
        assertThat.visibleByTimeout("css=div.calendar", maxMillis);
    }

    private void checkCalendarWorks(final DateFieldWithCalendar datePicker)
    {
        final Date expectedDate;
        final Date actualDate;
        expectedDate = clickDay(datePicker.getValue(), datePicker.openCalendar());
        actualDate = datePicker.getValue();
        assertEquals(expectedDate, actualDate);
    }

    private Date clickDay(Date startDate, CalendarPopup popup)
    {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        //Work out which day to click on.
        int clickDay = 3;
        if (cal.get(Calendar.DAY_OF_MONTH) == clickDay)
        {
            clickDay++;
        }
        cal.set(Calendar.DAY_OF_MONTH, clickDay);
        popup.clickDay(clickDay);
        return cal.getTime();
    }

    private void checkCalendarOnNavigatorDialog(Function<IssueActionDialog, Void> openFunction)
    {
        final LogWorkDialog logWorkDialog = new LogWorkDialog(context());

        //open the dialog.
        openFunction.apply(logWorkDialog);

        //Make sure that we can open the calendar on the log work screen.
        final CalendarPopup calendarPopup = logWorkDialog.openDateStartedCalendar();
        final Date expectedDate = clickDay(logWorkDialog.getStartDateEnglish(), calendarPopup);
        final Date actualDate = logWorkDialog.getStartDateEnglish();
        assertEquals(expectedDate, actualDate);
        logWorkDialog.cancel(CancelType.BY_CLICK);

        //Make sure that we can open the calendar on a workflow dialog.
        checkWorkflowDialog(openFunction, new WorkflowTransitionDialog(context(), TEST));
        checkWorkflowDialog(openFunction, new WorkflowTransitionDialog(context(), WorkflowTransition.CLOSE));
    }

    private void checkWorkflowDialog(final Function<IssueActionDialog, Void> openFunction, final WorkflowTransitionDialog dialog)
    {
        //open the dialog.
        openFunction.apply(dialog);
        checkCalendarWorks(dialog.getDateTimePicker(10001));
        checkCalendarWorks(dialog.getDatePicker(10000));
        dialog.cancel(CancelType.BY_CLICK);
    }

    private static final class CalendarStrings
    {
        private final String language;
        private final String locale;
        private final String selectDay;
        private final String today;
        private final String time;
        private final String week;
        private final String thisMonth;
        private final String[] daysOfWeek;
        private final String[] shortMonths;
        private final String[] longMonths;
        private final String am;

        private static final CalendarStrings CALENDAR_DE =
                new CalendarStrings("German (Germany)", "de_DE", "Datum ausw\u00e4hlen", "Heute", "Zeit:", "KW", "September",
                        new String[] { "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So" },
                        new String[] { "Jan", "Feb", "Mrz", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez" },
                        new String[] { "Januar", "Februar", "M\u00e4rz", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" },
                        "am");
        private static final CalendarStrings CALENDAR_EN_UK =
                new CalendarStrings("English (UK)", "en_UK", "Select date", "Today", "Time", "wk", "September",
                        new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" },
                        new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" },
                        new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" },
                        "am");

        private static final CalendarStrings CALENDAR_FR =
                new CalendarStrings("French (France)", "fr_FR", "S\u00e9lectionner une date", "Aujourd'hui", "Heure :", "Sem.", "Septembre",
                        new String[] { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" },
                        new String[] { "janv.", "f\u00e9vr.", "mars", "avr.", "mai", "juin", "juil.", "ao\u00fbt", "sept.", "oct.", "nov.", "d\u00e9c." },
                        new String[] { "Janvier", "F\u00e9vrier", "Mars", "Avril", "Mai", "Juin", "Juillet", "Ao\u00fbt", "Septembre", "Octobre", "Novembre", "D\u00e9cembre" },
                        "am");
        private static final CalendarStrings CALENDAR_ES =
                new CalendarStrings("Spanish (Spain)", "es_ES", "Seleccionar fecha", "Hoy", "Hora", "sem", "Septiembre",
                        new String[] { "Lun", "Mar", "Mi\u00e9", "Jue", "Vie", "S\u00e1b", "Dom" },
                        new String[] { "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic" },
                        new String[] { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" },
                        "am");
        private static final CalendarStrings CALENDAR_JP =
                new CalendarStrings("Japanese (Japan)", "ja_JP", "\u65e5\u4ed8\u9078\u629e", "\u4eca\u65e5", "", "\u9031", "9\u6708",
                        new String[] { "\u65e5", "\u6708", "\u706b", "\u6c34", "\u6728", "\u91d1", "\u571f" },
                        new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" },
                        new String[] { "1\u6708", "2\u6708", "3\u6708", "4\u6708", "5\u6708", "6\u6708", "7\u6708", "8\u6708", "9\u6708", "10\u6708", "11\u6708", "12\u6708" },
                        "??");

        public static Collection<CalendarStrings> list()
        {
            return Arrays.asList(CALENDAR_JP, CALENDAR_EN_UK, CALENDAR_FR);
        }

        public CalendarStrings(String language, String locale, String selectDay, String today, String time, String week, String thisMonth, String[] daysOfWeek, String[] shortMonths, String[] longMonths, final String am)
        {
            this.language = language;
            this.locale = locale;
            this.daysOfWeek = daysOfWeek;
            this.selectDay = selectDay;
            this.thisMonth = thisMonth;
            this.today = today;
            this.week = week;
            this.time = time;
            this.shortMonths = shortMonths;
            this.longMonths = longMonths;
            this.am = am;
        }

        public CalendarStrings(String language, String locale, CalendarStrings otherCalendarStrings, final String am)
        {
            this.language = language;
            this.locale = locale;
            this.am = am;
            this.daysOfWeek = otherCalendarStrings.daysOfWeek;
            this.selectDay = otherCalendarStrings.selectDay;
            this.thisMonth = otherCalendarStrings.thisMonth;
            this.today = otherCalendarStrings.today;
            this.week = otherCalendarStrings.week;
            this.time = otherCalendarStrings.time;
            this.shortMonths = otherCalendarStrings.shortMonths;
            this.longMonths = otherCalendarStrings.longMonths;
        }

        public void assertDateTimeCalendarValid(Selenium selenium)
        {
            assertTextPresent(selenium, "TIME", time);
            assertDateCalendarValid(selenium);
        }

        public void assertDateCalendarValid(Selenium selenium)
        {
            assertTextPresent(selenium, "SEL_DATE", selectDay);
            assertTextPresent(selenium, "TODAY", today);
            assertTextPresent(selenium, "WK", week);
            assertTextPresent(selenium, "this month", thisMonth);
            assertTextPresent(selenium, "AMPM", am);
            for (int i = 0; i < daysOfWeek.length; i++)
            {
                if (i == 0)
                {
                    assertIsValidFirstDayOfWeek(selenium, daysOfWeek[i]);
                }


                assertTextPresent(selenium, "day of week [" + i + "]", daysOfWeek[i]);
            }
        }

        private void assertIsValidFirstDayOfWeek(Selenium selenium, String dayofweek)
        {
            Boolean isFirstDayOfWeek = Boolean.parseBoolean(selenium.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\".name.day:first:contains(" + dayofweek + ")\").length === 1"));
            if (!isFirstDayOfWeek)
            {
                throw new RuntimeException("Expected first day of week to be '" + dayofweek + "'");
            }
        }

        private void assertTextPresent(Selenium selenium, String what, String value)
        {
            assertTrue("Expected string for '" + what + "'='" + value + "' Language: " + language,
                    selenium.isTextPresent(value));
        }

        public String toString()
        {
            return language + "(" + locale + ")";
        }
    }
}
