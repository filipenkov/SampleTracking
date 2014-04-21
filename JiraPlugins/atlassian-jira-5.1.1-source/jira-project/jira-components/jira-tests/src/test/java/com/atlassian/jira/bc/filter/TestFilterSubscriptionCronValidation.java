package com.atlassian.jira.bc.filter;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.util.FilterCronValidationErrorMappingUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.component.cron.parser.CronExpressionParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestFilterSubscriptionCronValidation extends ListeningTestCase
{
    private FilterSubscriptionService service;
    private JiraServiceContext context;


    @Before
    public void setUp() throws Exception
    {
        service = createService();
        context = createContext();
    }


    @After
    public void tearDown() throws Exception
    {
        service = null;
        context = null;
    }

    @Test
    public void testFilterCronNullExpr()
    {
        service.validateCronExpression(context, null);
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.mode.error"));
    }

    @Test
    public void testFilterCronEmptyStringExpr()
    {
        service.validateCronExpression(context, "");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.unexpected.end.of.expr"));
    }

    @Test
    public void testFilterCronUnexpectedEnd()
    {
        service.validateCronExpression(context, "0 0 0 ? 1");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.unexpected.end.of.expr"));
    }

    @Test
    public void testFilterCronIllegalFormat()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 NO");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.illegal.format:java.lang.StringIndexOutOfBoundsException: String index out of range: 3"));
    }

    @Test
    public void testFilterCronInvalidMonthName()
    {
        service.validateCronExpression(context, "0 0 0 ? FEB MON");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? FEB-NOV MON");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? NOT MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        // Due to bug http://jira.opensymphony.com/browse/QUARTZ-574, proper error messgae is not thrown.
        assertFalse("Happy Fail!!  This will fail when QUARTZ-574 is fixed", context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.invalid.month:NOT"));
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.month.between.one.twelve"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? FEB-NOT MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        // Due to bug http://jira.opensymphony.com/browse/QUARTZ-574, proper error messgae is not thrown.
        assertFalse("Happy Fail!!  This will fail when QUARTZ-574 is fixed", context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.invalid.month:NOT"));
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.general.error:0 0 0 ? FEB-NOT MON"));
    }

    @Test
    public void testFilterCronInvalidDayName()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 MON");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON-TUE");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 NOT");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.invalid.day.of.week:NOT"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON-NOT");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.invalid.day.of.week:NOT"));

    }

    @Test
    public void testFilterCronNumericAfterHash()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 MON#3");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 1#3");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON#8");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.numeric.value.between.after.hash"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON#three");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.numeric.value.between.after.hash"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 1#8");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.numeric.value.between.after.hash"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 1#three");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.numeric.value.between.after.hash"));

    }

    @Test
    public void testFilterCronInvalidChars()
    {
        service.validateCronExpression(context, "0 XXX 0 ? 1 MON-TUE");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.illegal.characters.for.position:XXX"));
    }

    @Test
    public void testFilterCronIllegalCharAfterQuestionMark()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 MON-TUE");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ?\t1 MON-TUE");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ?X 1 MON-TUE");
        assertFalse("Happy Fail!!  This will fail when QUARTZ-575 is fixed.", context.getErrorCollection().hasAnyErrors());
        // Uncomment me when QUARTZ-575 (http://jira.opensymphony.com/browse/QUARTZ-575) is fixed
        //assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.illegal.character.after.question.mark:X"));


        context = createContext();
        service.validateCronExpression(context, "0 0 0 ?XX 1 MON-TUE");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue("Happy Fail!!  This will fail when QUARTZ-575 is fixed.  Add extra 'X' to error messge check",
                context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.illegal.character.after.question.mark:X"));
    }

    @Test
    public void testFilterCronInvalidQuestionMark()
    {
        service.validateCronExpression(context, "0 ? 0 ? 1 1");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.question.mark.invalid.position"));
    }

    @Test
    public void testFilterCronInvalidQuestionMarkForBoth()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.question.mark.invalid.for.both"));
    }

    @Test
    public void testFilterCronInvalidIncrements()
    {
        service.validateCronExpression(context, "0 /5 0 ? 1 MON");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 5/5 0 ? 1 MON");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 */5 0 ? 1 MON");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 / 0 ? 1 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.slash.must.be.followed.by.integer"));

        context = createContext();
        service.validateCronExpression(context, "/65 0 0 ? 1 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.increment.greater.than.sixty:65"));

        context = createContext();
        service.validateCronExpression(context, "0 /65 0 ? 1 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.increment.greater.than.sixty:65"));

        context = createContext();
        service.validateCronExpression(context, "0 0 /26 ? 1 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.increment.greater.than.twentyfour:26"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? /15 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.increment.greater.than.twelve:15"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 /9");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.increment.greater.than.seven:9"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 /36 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.increment.greater.than.thirtyone:36"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 0/6a 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.unexpected.character.after.slash:A"));
    }

    @Test
    public void testFilterCronInvalidCharacter()
    {
        service.validateCronExpression(context, "0 @ 0 1 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.unexpected.character:@"));

    }

    @Test
    public void testFilterCronInvalidLOption()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 L");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 L 1 ?");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "L 0 0 1 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertFalse("Happy Fail!!  This will fail when QUARTZ-576 is fixed.", context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.l.not.valid"));
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.general.error:L 0 0 1 1 ?"));

        context = createContext();
        service.validateCronExpression(context, "6L 0 0 1 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.l.not.valid:1"));
    }

    @Test
    public void testFilterCronInvalidWOption()
    {
        service.validateCronExpression(context, "0 0 0 LW 1 ?");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 15W 1 ?");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "6W 0 0 1 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.w.not.valid:1"));
    }

    @Test
    public void testFilterCronInvalidHashOption()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 1#3");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON#3");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "6#4 0 0 1 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.hash.not.valid:1"));
    }


    @Test
    public void testFilterCronInvalidCOption()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 5C");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "0 0 0 5C 1 ?");
        assertFalse(context.getErrorCollection().hasAnyErrors());

        context = createContext();
        service.validateCronExpression(context, "6C 0 0 1 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.c.not.valid:1"));
    }

    @Test
    public void testFilterCronInvalidValues()
    {
        service.validateCronExpression(context, "65 0 0 ? 1 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.minute.and.seconds.between.zero.fiftynine"));

        context = createContext();
        service.validateCronExpression(context, "0 65 0 ? 1 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.minute.and.seconds.between.zero.fiftynine"));

        context = createContext();
        service.validateCronExpression(context, "0 0 26 ? 1 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.hour.between.zero.twentythree"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 33 1 ?");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.day.of.month.between.one.thirtyone"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 15 MON");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.month.between.one.twelve"));

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 9");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.day.of.week.between.one.seven"));
    }

    /**
     * Check validity of default string used to seed the cron editor.
     */
    @Test
    public void testFilterWithDefaultValue()
    {
        service.validateCronExpression(context, CronExpressionParser.DEFAULT_CRONSTRING);
        assertFalse(context.getErrorCollection().hasAnyErrors());
    }

    private DefaultFilterSubscriptionService createService()
    {
        FilterCronValidationErrorMappingUtil mapper = new FilterCronValidationErrorMappingUtil(null)
        {

            protected String getText(String key)
            {
                return key;
            }

            protected String getText(String key, Object param)
            {
                return key + ":" + param;
            }

        };

        return new DefaultFilterSubscriptionService(mapper, null, null)
        {

            @Override
            protected String getText(String key)
            {
                return key;
            }

            protected String getText(String key, Object param)
            {
                return key + ":" + param;
            }
        };
    }

    private JiraServiceContextImpl createContext()
    {
        return new JiraServiceContextImpl(null, new SimpleErrorCollection());
    }
}
