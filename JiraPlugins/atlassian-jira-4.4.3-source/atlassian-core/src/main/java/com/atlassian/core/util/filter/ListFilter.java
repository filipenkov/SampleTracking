package com.atlassian.core.util.filter;

import com.atlassian.core.util.filter.Filter;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Filter the contents of a list based on some criteria.
 */
public class ListFilter
{
    private Filter filter;
    private static final Object MYNULL = new Object();

    private class FilteredIterator implements Iterator
    {
        private Iterator innerIterator;

        // We need to define our own "null", since there may be real
        // nulls in a list. Funky, eh.
        private Object savedObject = MYNULL;

        public FilteredIterator(Iterator innerIterator)
        {
            this.innerIterator = innerIterator;
        }

        public void remove()
        {
            innerIterator.remove();
        }

        public boolean hasNext()
        {
            if (savedObject != MYNULL)
                return true;

            while (innerIterator.hasNext())
            {
                savedObject = innerIterator.next();
                if (filter.isIncluded(savedObject))
                    return true;
            }
            savedObject = MYNULL;

            return false;
        }

        public Object next()
        {
            if (savedObject != MYNULL)
                return clearSavedObject();

            while (true)
            {
                Object o = innerIterator.next();
                if (filter.isIncluded(o))
                    return o;
            }
        }

        private Object clearSavedObject()
        {
            Object ret = savedObject;
            savedObject = MYNULL;
            return ret;
        }
    }

    /**
     * Constructor, taking the filter implementation that will be used to
     * filter the list.
     *
     * @param filter the filter implementation that will be used to filter the
     *        list.
     */
    public ListFilter(Filter filter)
    {
        this.filter = filter;
    }

    /**
     * Filter the contents of a list. Returns a new list with the filtered
     * objects removed. Does not change the list passed in.
     *
     * @param list the list to filter
     * @return a new list with the filtered objects removed.
     */
    public List filterList(List list)
    {
        if (list ==  null)
            return list;
        
        List filteredList = new ArrayList();
        Iterator i = filterIterator(list.iterator());
        while (i.hasNext())
            filteredList.add(i.next());

        return filteredList;
    }

    /**
     * Filter the contents of an iterator. Returns an iterator that will only
     * return un-filtered members of the supplied iterator.
     */
    public Iterator filterIterator(Iterator iterator)
    {
        return new FilteredIterator(iterator);
    }
}
