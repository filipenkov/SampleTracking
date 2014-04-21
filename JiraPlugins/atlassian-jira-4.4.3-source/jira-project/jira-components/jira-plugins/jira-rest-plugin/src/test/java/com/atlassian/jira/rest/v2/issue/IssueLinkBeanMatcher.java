package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.field.FieldBean;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

import java.util.List;

/**
 * EasyMock matcher for IssueLinkBean instances.
 *
 * @since v4.2
 */
class IssueLinkBeanMatcher
{
    static FieldBean hasKey(final String key)
    {
        EasyMock.reportMatcher(new IArgumentMatcher()
        {
            public boolean matches(Object argument)
            {
                FieldBean field = (FieldBean) argument;
                IssueLinkBean actual = (IssueLinkBean) field.getValue();
                return key == null ? actual.getKey() == null : key.equals(actual.getKey());
            }

            public void appendTo(StringBuffer buffer)
            {
                buffer.append("IssueLinkBean[key=").append(key).append("]");
            }
        });

        return null;
    }
}
