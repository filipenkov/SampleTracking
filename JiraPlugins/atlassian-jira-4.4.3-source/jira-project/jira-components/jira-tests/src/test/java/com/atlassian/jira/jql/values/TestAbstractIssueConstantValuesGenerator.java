package com.atlassian.jira.jql.values;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.MockIssueConstant;
import com.opensymphony.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @since v4.0
 */
public class TestAbstractIssueConstantValuesGenerator extends MockControllerTestCase
{
    private MyIssueConstValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testGetPossibleValuesHappyPath() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "", 5);

        assertEquals(4, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName()));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName()));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result(type3.getName()));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result(type4.getName()));
    }

    @Test
    public void testGetPossibleValuesDoesMatchFullValue() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Aa it", 5);

        assertEquals(1, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName()));
    }

    @Test
    public void testGetPossibleValuesMatchNone() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Z", 5);

        assertEquals(0, possibleValues.getResults().size());
    }

    @Test
    public void testGetPossibleValuesMatchSome() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "a", 5);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName()));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName()));
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "Aa it blah");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Aa it", 5);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName()));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName()));
    }

    @Test
    public void testGetPossibleValuesMatchToLimit() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "", 3);

        assertEquals(3, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result(type1.getName()));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result(type2.getName()));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result(type3.getName()));
    }

    private static MyIssueConstValuesGenerator createGenerator(IssueConstant... allConstants)
    {
        return new MyIssueConstValuesGenerator(allConstants)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };
    }

    static class MyIssueConstValuesGenerator extends AbstractIssueConstantValuesGenerator
    {
        private final List<IssueConstant> allConstants;

        MyIssueConstValuesGenerator(IssueConstant... allConstants)
        {
            this.allConstants = Arrays.asList(allConstants);
        }

        protected List<IssueConstant> getAllConstants()
        {
            return allConstants;
        }
    }
}
