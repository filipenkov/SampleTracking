package com.atlassian.jira.issue.fields;

import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A type of List&lt;Long> that can be used as an entry in the fieldsvaluemap, but
 * that can keep track of error values (and hence return them in error edithtmls).
 *
 * @since v5.1
 */
public class LongIdsValueHolder extends LinkedList<Long>
{
    private final List<String> badStrings = new LinkedList<String>();

    public static LongIdsValueHolder fromFieldValuesHolder(String id, Map params)
    {

        Object o = params.get(id);
        if (o instanceof LongIdsValueHolder)
        {
            return (LongIdsValueHolder) o;
        }

        if (o instanceof Collection) { // we may need to up-cast it to a "better" collection
            LongIdsValueHolder vh = new LongIdsValueHolder(new ArrayList<Long>((Collection) o));
            params.put(vh, id); // put "better" value back
            return vh;
        }

        return null;
    }
    
    public LongIdsValueHolder(List<Long> componentIds)
    {
        super(componentIds);
    }

    public LongIdsValueHolder(String[] value)
    {
        this(null == value ? null : Arrays.asList(value));
    }

    public LongIdsValueHolder(Collection<String> value)
    {
        if (value != null && !value.isEmpty())
        {
            for (String aValue : value)
            {
                try
                {
                    this.add(new Long(aValue));
                }
                catch (NumberFormatException e)
                {
                    badStrings.add(aValue);
                }
            }
        }
    }

    public List<Long> getComponentIds()
    {
        return this;
    }

    public String getInputText()
    {
        return StringUtils.join(badStrings, "");
    }

    public void addBadId(Long componentId)
    {
        badStrings.add("" + componentId);
    }

    public void validateIds(Predicate<Long> predicate)
    {
        Iterator<Long> it = iterator();
        while (it.hasNext())
        {
            Long id =  it.next();
            if (!predicate.apply(id)) {
                it.remove();
                addBadId(id);
            }
        }
    }
}
