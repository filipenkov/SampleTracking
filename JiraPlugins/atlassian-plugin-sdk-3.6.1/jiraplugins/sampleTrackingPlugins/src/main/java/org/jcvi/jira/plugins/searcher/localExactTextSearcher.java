package org.jcvi.jira.plugins.searcher;

import com.atlassian.jira.issue.customfields.searchers.ExactTextSearcher;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.jcvi.jira.plugins.statisticsmapper.shared.StringCFStatisticsMapper;

/**
 * <p>Included to workaround limitations in JIRA. Searchers must specify
 * the fields they can use and so to use a standard searcher with a
 * customField the searcher must be defined in the plug-in's XML.
 * As only locally defined classes can be used this class is needed
 * to allow access to the real NumberRangeSearcher</p>
 * <p>See 'Practical JIRA Plugins p51</p>
 * <p>This won't be necessary after v5.2 as customFields can now specify
 * the searchers that will work and can include system searchers.</p>
 */
public class localExactTextSearcher extends ExactTextSearcher implements CustomFieldStattable {
    public localExactTextSearcher(JqlOperandResolver jqlOperandResolver, CustomFieldInputHelper customFieldInputHelper, FieldVisibilityManager fieldVisibilityManager) {
        super(jqlOperandResolver, customFieldInputHelper, fieldVisibilityManager);
        log.info("Local Exact Text Searcher : "+jqlOperandResolver.toString()+" / "+customFieldInputHelper.toString());
    }
    @Override
    public StatisticsMapper getStatisticsMapper(CustomField customField) {
        return new StringCFStatisticsMapper(customField,
                                            true);//use Exact Match
    }

}
