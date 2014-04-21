AJS.test.require("jira.webresources:calendar");

(function() {
    test("getWeekNumber using ISO8601 algorithm", function() {
        // some dates with weird boundaries from http://www.staff.science.uu.nl/~gent0113/calendar/isocalendar.htm
        // format is month (1-based), day, year, expected weeknumber
        var dates = [
            [1, 1, 2005, 53],
            [1, 2, 2005, 53],
            [1, 3, 2005, 1],

            [12, 22, 2008, 52],
            [12, 28, 2008, 52],
            [12, 29, 2008, 1],
            [1, 4, 2009, 1],
            [1, 5, 2009, 2],
            [1, 11, 2009, 2],

            [12, 21, 2009, 52],
            [12, 27, 2009, 52],
            [12, 28, 2009, 53],
            [1, 3, 2010, 53],
            [1, 4, 2010, 1],
            [1, 10, 2010, 1]
        ];
        for (var i = 0; i < dates.length; i++) {
            var month = dates[i][0] - 1;
            var day = dates[i][1];
            var year = dates[i][2];
            var weeknumber = dates[i][3];
            equal(new Date(year, month, day).getISO8601WeekNumber(), weeknumber, "Weeknumber didn't match expectations for " + new Date(year, month, day));
        }

        // Some other dates that customers have complained about not working previously
        equal(new Date(2006, 0, 1).getISO8601WeekNumber(), 52, "Jan 1 2006 actually is in Week 52 of 2005.")
        equal(new Date(2006, 0, 2).getISO8601WeekNumber(), 1, "2/1 Week number incorrect.")
        equal(new Date(2006, 0, 8).getISO8601WeekNumber(), 1, "8/1 Week number incorrect.")

        equal(new Date(2011, 5, 6).getISO8601WeekNumber(), 23, "June 6, 2011, week number incorrect")
    })

    test("get Week Number 'standard' algorithm i.e. using 1-January as first week", function() {
        equal(new Date(2005, 0, 1).getSimpleWeekNumber(), 1, "Weeknumber didn't match expectations.")
        equal(new Date(2005, 0, 2).getSimpleWeekNumber(), 2, "Weeknumber didn't match expectations.")

        equal(new Date(2005, 11, 31).getSimpleWeekNumber(), 53, "Last day of year should be in week 53.")
        equal(new Date(2006, 0, 1).getSimpleWeekNumber(), 1, "1/1 Week number incorrect.")
        equal(new Date(2006, 0, 2).getSimpleWeekNumber(), 1, "2/1 Week number incorrect.")
        equal(new Date(2006, 0, 7).getSimpleWeekNumber(), 1, "7/1 Week number incorrect.")
        equal(new Date(2006, 0, 8).getSimpleWeekNumber(), 2, "8/1 Week number incorrect.")
    })

    test("get Week Number 'standard' algorithm when first day of week changes", function () {
        equal(new Date(2007, 0, 1).getSimpleWeekNumber(), 1, "Jan 1 in week 1 using Sunday")
        equal(new Date(2007, 0, 2).getSimpleWeekNumber(), 1, "Jan 2 in week 1 using Sunday")
        equal(new Date(2007, 0, 1).getSimpleWeekNumber(1), 1, "Jan 1 in week 1 using Monday as start of week.")
        equal(new Date(2007, 0, 2).getSimpleWeekNumber(1), 2, "Jan 2 in week 2 using Monday as start of week.")
    })
})();