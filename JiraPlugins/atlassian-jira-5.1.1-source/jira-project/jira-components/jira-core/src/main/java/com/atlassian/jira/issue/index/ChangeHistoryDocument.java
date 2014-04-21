package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Locale;

/**
 * Returns a Lucene {@link org.apache.lucene.document.Document} from a given {@link com.atlassian.jira.issue.changehistory.ChangeHistoryGroup}
 *
 * @since v4.3
 */
public class ChangeHistoryDocument
{
    public static Document getDocument(ChangeHistoryGroup changeHistoryGroup)
    {
        Document doc = new Document();
        if (changeHistoryGroup != null)
        {
            String changeItemUser = CaseFolding.foldUsername(changeHistoryGroup.getUser());
            doc.add(new Field(DocumentConstants.PROJECT_ID, String.valueOf(changeHistoryGroup.getProjectId()), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field(DocumentConstants.ISSUE_ID, String.valueOf(changeHistoryGroup.getIssueId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field(DocumentConstants.ISSUE_KEY, String.valueOf(changeHistoryGroup.getIssueKey()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field(DocumentConstants.CHANGE_ACTIONER, encodeProtocol(changeItemUser), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field(DocumentConstants.CHANGE_DATE, LuceneUtils.dateToString(changeHistoryGroup.getCreated()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            for (ChangeHistoryItem changeItem : changeHistoryGroup.getChangeItems())
            {
                final String changedField = changeItem.getField();

                doc.add(new Field(encodeChangedField(changedField, DocumentConstants.CHANGE_DURATION), String.valueOf(changeItem.getDuration()), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
                doc.add(new Field(encodeChangedField(changedField, DocumentConstants.NEXT_CHANGE_DATE), LuceneUtils.dateToString(changeItem.getNextChangeCreated()), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
                for (String from:changeItem.getFroms().values())
                {
                    doc.add(new Field(encodeChangedField(changedField, DocumentConstants.CHANGE_FROM), encodeProtocol(from), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
                }
                for (String to:changeItem.getTos().values())
                {
                        doc.add(new Field(encodeChangedField(changedField, DocumentConstants.CHANGE_TO), encodeProtocol(to), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
                }
                for (String fromValue:changeItem.getFroms().keySet())
                {
                    doc.add(new Field(encodeChangedField(changedField, DocumentConstants.OLD_VALUE), encodeProtocol(fromValue), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
                }
                for (String toValue:changeItem.getTos().keySet())
                {
                    doc.add(new Field(encodeChangedField(changedField, DocumentConstants.NEW_VALUE), encodeProtocol(toValue), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
                }
            }
            return doc;
        }
        return null;
    }

    private static String encodeChangedField(String changedField, String docConstant)
    {
        return changedField + "." + docConstant;
    }

    private static String encodeProtocol(final String changeItem)
    {
        return DocumentConstants.CHANGE_HISTORY_PROTOCOL + (changeItem == null ? "" : changeItem.toLowerCase());
    }
}
