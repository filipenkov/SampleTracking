package com.atlassian.jira.bean;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.web.bean.PercentageGraphModel;
import com.atlassian.jira.web.bean.PercentageGraphRow;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TestSubTaskBeanImpl extends LegacyJiraMockTestCase
{
    private SubTaskBeanImpl bean;
    private MockGenericValue issue;
    private MockGenericValue subTaskOpen1;
    private MockGenericValue subTaskOpen2;
    private MockGenericValue subTaskClosed1;
    private MockGenericValue subTaskClosed2;
    private MockGenericValue subTaskClosed3;

    public TestSubTaskBeanImpl(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        bean  = new SubTaskBeanImpl();

        issue = new MockGenericValue("Issue", EasyMap.build("summary", "test issue"));
        subTaskOpen1 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 1"));
        subTaskOpen2 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 2"));
        subTaskClosed1 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 3", "resolution", "something"));
        subTaskClosed2 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 3", "resolution", "something else"));
        subTaskClosed3 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 3", "resolution", "something totally different"));

        int i = 0;
        bean.addSubTask(new Long(i++), subTaskOpen1, issue);
        bean.addSubTask(new Long(i++), subTaskClosed1, issue);
        bean.addSubTask(new Long(i++), subTaskOpen2, issue);
        bean.addSubTask(new Long(i++), subTaskClosed2, issue);
        bean.addSubTask(new Long(i++), subTaskClosed3, issue);
    }

    public void testAllView()
    {
        final String view = SubTaskBean.SUB_TASK_VIEW_ALL;
        final Collection subTasks = bean.getSubTasks(view);
        assertEquals(5, subTasks.size());
        final Iterator iterator = subTasks.iterator();
        assertEquals(new SubTask(new Long(0), subTaskOpen1, issue), iterator.next());
        assertEquals(new SubTask(new Long(1), subTaskClosed1, issue), iterator.next());
        assertEquals(new SubTask(new Long(2), subTaskOpen2, issue), iterator.next());
        assertEquals(new SubTask(new Long(3), subTaskClosed2, issue), iterator.next());
        assertEquals(new SubTask(new Long(4), subTaskClosed3, issue), iterator.next());

        assertEquals(new Long(1), bean.getNextSequence(new Long(0), view));
        assertEquals(new Long(0), bean.getPreviousSequence(new Long(1), view));
    }

    public void testOpenView()
    {
        final String view = SubTaskBean.SUB_TASK_VIEW_UNRESOLVED;
        final Collection subTasks = bean.getSubTasks(view);
        assertEquals(2, subTasks.size());
        final Iterator iterator = subTasks.iterator();
        assertEquals(new SubTask(new Long(0), subTaskOpen1, issue), iterator.next());
        assertEquals(new SubTask(new Long(2), subTaskOpen2, issue), iterator.next());

        assertEquals(new Long(2), bean.getNextSequence(new Long(0), view));
        assertEquals(new Long(0), bean.getPreviousSequence(new Long(2), view));
    }

    public void testGetSubTaskProgress()
    {
        final PercentageGraphModel subTaskProgress = bean.getSubTaskProgress();
        final List rows = subTaskProgress.getRows();
        assertEquals(2, rows.size());
        final Iterator iterator = rows.iterator();
        PercentageGraphRow percentageGraphRow = (PercentageGraphRow) iterator.next();
        assertEquals(new PercentageGraphRow("#51A825", 3L, "Resolved Sub-Tasks", null), percentageGraphRow);
        assertEquals(60, subTaskProgress.getPercentage(percentageGraphRow));
        percentageGraphRow = (PercentageGraphRow) iterator.next();
        assertEquals(new PercentageGraphRow("#cccccc", 2L, "Unresolved Sub-Tasks", null), percentageGraphRow);
        assertEquals(40, subTaskProgress.getPercentage((PercentageGraphRow) percentageGraphRow));
    }
}
