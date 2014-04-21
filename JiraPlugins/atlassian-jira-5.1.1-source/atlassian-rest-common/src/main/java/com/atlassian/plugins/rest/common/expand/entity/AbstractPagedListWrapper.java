package com.atlassian.plugins.rest.common.expand.entity;

import com.atlassian.plugins.rest.common.expand.parameter.Indexes;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * An abstract list wrapper that provides support for paging information:
 * <ul>
 * <li>size</li>
 * <li>max-results</li>
 * <li>start-index</li>
 * </ul>
 * @param <T> the type of element in the list to wrap.
 */
@XmlRootElement
@XmlAccessorType(FIELD)
public abstract class AbstractPagedListWrapper<T> implements ListWrapper<T>
{
    @XmlAttribute
    private final int size;

    @XmlAttribute(name = "max-results")
    private final int maxResults;

    @XmlAttribute(name = "start-index")
    private Integer startIndex;

    // this is for JAXB
    private AbstractPagedListWrapper()
    {
        size = 0;
        maxResults = 0;
    }

    protected AbstractPagedListWrapper(int size, int maxResults)
    {
        this.size = size;
        this.maxResults = maxResults;
    }

    public Integer getStartIndex()
    {
        return startIndex;
    }

    public int getSize()
    {
        return size;
    }

    public int getMaxResults()
    {
        return maxResults;
    }

    public void setStartIndex(int startIndex)
    {
        this.startIndex = startIndex;
    }

    public final ListWrapperCallback<T> getCallback()
    {
        return new ListWrapperCallback<T>()
        {
            public List<T> getItems(Indexes indexes)
            {
                final int startIndex = indexes.getMinIndex(size);
                if (startIndex != -1)
                {
                    setStartIndex(startIndex);
                }
                return getPagingCallback().getItems(indexes);
            }
        };
    }

    /**
     * Gets the call back that will actually "retrieve" the necessary element to populate the List. Size, index and max
     * result attributes are already set by this abstract class, no need to worry about them.
     * @return the call back that does all the work.
     */
    public abstract ListWrapperCallback<T> getPagingCallback();
}
