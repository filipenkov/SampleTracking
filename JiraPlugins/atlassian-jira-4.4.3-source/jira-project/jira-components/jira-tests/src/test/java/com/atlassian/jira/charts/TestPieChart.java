package com.atlassian.jira.charts;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;

/**
 * @since v4.0
 */
public class TestPieChart extends MockControllerTestCase
{

    @Test
    public void testGetStatisticsTypeI18nName()
    {
        final StattableSearcher selectSearcher = new StattableSearcher();

        final CustomField mockCustomField = mockController.getMock(CustomField.class);
        mockCustomField.getCustomFieldSearcher();
        mockController.setReturnValue(selectSearcher);
        mockCustomField.getName();
        mockController.setReturnValue("Custom Field Name");

        final CustomFieldManager mockCustomFieldManager = mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.getCustomFieldObject("customfield_10001");
        mockController.setReturnValue(mockCustomField);

        final I18nHelper mockI18nHelper = mockController.getMock(I18nHelper.class);
        mockI18nHelper.getText("gadget.filterstats.field.statistictype.type");
        mockController.setReturnValue("Issue Type");
        mockI18nHelper.getText("gadget.filterstats.field.statistictype.priorities");
        mockController.setReturnValue("Priorities");
        mockController.replay();

        PieChart pieChart = new PieChart(null, mockCustomFieldManager, null, null);


        String i18nName = pieChart.getStatisticsTypeI18nName(mockI18nHelper, "type");
        assertEquals("Issue Type", i18nName);

        i18nName = pieChart.getStatisticsTypeI18nName(mockI18nHelper, "priorities");
        assertEquals("Priorities", i18nName);

        i18nName = pieChart.getStatisticsTypeI18nName(mockI18nHelper, "customfield_10001");
        assertEquals("Custom Field Name", i18nName);
    }

    private class StattableSearcher implements CustomFieldStattable, CustomFieldSearcher
    {

        public StatisticsMapper getStatisticsMapper(final CustomField customField)
        {
            return null;
        }

        public void init(final CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor)
        {
        }

        public CustomFieldSearcherModuleDescriptor getDescriptor()
        {
            return null;
        }

        public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()
        {
            return null;
        }

        public void init(final CustomField field)
        {
        }

        public SearcherInformation<CustomField> getSearchInformation()
        {
            return null;
        }

        public SearchInputTransformer getSearchInputTransformer()
        {
            return null;
        }

        public SearchRenderer getSearchRenderer()
        {
            return null;
        }

        public SearchableField getField()
        {
            return null;
        }
    }

}
