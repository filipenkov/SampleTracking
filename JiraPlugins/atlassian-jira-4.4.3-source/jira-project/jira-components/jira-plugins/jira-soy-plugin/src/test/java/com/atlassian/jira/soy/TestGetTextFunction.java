package com.atlassian.jira.soy;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.google.template.soy.jssrc.restricted.JsExpr;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.4
 */
public class TestGetTextFunction
{
    private IMocksControl control;
    private I18nHelper i18nHelper;
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Before
    public void setup()
    {
        control = createControl();
        jiraAuthenticationContext = control.createMock(JiraAuthenticationContext.class);

        i18nHelper = control.createMock(I18nHelper.class);
        expect(jiraAuthenticationContext.getI18nHelper()).andReturn(i18nHelper).anyTimes();
    }

    @Test
    public void testSimpleTranslation()
    {
        expect(i18nHelper.getText("some.key")).andReturn("Actung!");

        control.replay();

        JsExpr expr = callGetText("'some.key'");
        assertNotNull(expr);
        assertEquals("\"Actung!\"", expr.getText());
    }

    @Test
    public void testMultiArg()
    {
        expect(i18nHelper.getUnescapedText("some.key")).andReturn("Actung {0}!");

        control.replay();

        JsExpr expr = callGetText("'some.key'", "'baby'");
        assertNotNull(expr);
        assertEquals("AJS.format(\"Actung {0}!\",'baby')", expr.getText());
    }

    @Test
    public void testEscapedChars()
    {
        expect(i18nHelper.getText("some.key")).andReturn("foo ' \" < > & ; bar").anyTimes();
        expect(i18nHelper.getUnescapedText("some.key")).andReturn("foo '' \" < > & ; bar").anyTimes();

        control.replay();

        JsExpr expr = callGetText("'some.key'");
        assertNotNull(expr);
        assertEquals("\"foo \\x27 \\x22 \\x3c \\x3e \\x26 ; bar\"", expr.getText());

        expr = callGetText("'some.key'", "1");
        assertNotNull(expr);
        assertEquals("AJS.format(\"foo \\x27\\x27 \\x22 \\x3c \\x3e \\x26 ; bar\",1)", expr.getText());
    }

    private JsExpr callGetText(String arg0, String... args)
    {
        GetTextFunction fn = new GetTextFunction(jiraAuthenticationContext);
        List<JsExpr> exprs = new ArrayList<JsExpr>();
        exprs.add(new JsExpr(arg0, Integer.MAX_VALUE));
        for (String arg : args)
        {
            exprs.add(new JsExpr(arg, Integer.MAX_VALUE));
        }

        return fn.computeForJsSrc(exprs);
    }
}
