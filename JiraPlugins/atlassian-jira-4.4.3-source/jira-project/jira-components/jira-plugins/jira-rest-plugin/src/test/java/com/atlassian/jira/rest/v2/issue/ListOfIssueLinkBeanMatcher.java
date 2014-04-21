package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.field.FieldBean;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

import java.util.List;

/**
 * EasyMock matcher for List<IssueLinkBean>.
 *
 * @since v4.2
 */
abstract class ListOfIssueLinkBeanMatcher
{
    static FieldBean hasKeys(final String... issueKeys)
    {
        EasyMock.reportMatcher(new IArgumentMatcher()
        {
            @SuppressWarnings ("unchecked")
            public boolean matches(Object object)
            {
                FieldBean field = (FieldBean) object;
                List<IssueLinkBean> issueBeanList = (List) field.getValue();
                if (issueKeys.length != issueBeanList.size())
                {
                    return false;
                }

                for (int i = 0; i < issueBeanList.size(); i++)
                {
                    if (!issueKeys[i].equals(issueBeanList.get(i).getKey()))
                    {
                        return false;
                    }
                }

                return true;
            }

            public void appendTo(StringBuffer buffer)
            {
                buffer.append("List<IssueLinkBean>[");
                for (String issueKey : issueKeys) { buffer.append("IssueLinkBean[key=").append(issueKey).append("]"); }
                buffer.append("]");
            }
        });

        return null;
    }
}
