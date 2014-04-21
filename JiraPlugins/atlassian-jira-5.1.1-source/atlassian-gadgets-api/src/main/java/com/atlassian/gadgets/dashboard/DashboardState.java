package com.atlassian.gadgets.dashboard;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.atlassian.gadgets.GadgetState;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.jcip.annotations.Immutable;

import static com.atlassian.gadgets.dashboard.util.Iterables.checkContentsNotNull;
import static com.atlassian.gadgets.dashboard.util.Iterables.elementsEqual;
import static com.atlassian.plugin.util.Assertions.notNull;
import static com.atlassian.plugin.util.collect.CollectionUtil.toList;
import static java.util.Collections.nCopies;

/**
 * <p>An immutable representation of a dashboard.  A dashboard's state consists of 
 * <ul>
 *   <li>a {@link DashboardId}, used to uniquely identify the dashboard within the system</li>
 *   <li>a title, the text that is displayed to the user</li>
 *   <li>a {@link Layout} that describes how many columns appear on the dashboard and how large they should be with
 *       respect to one another</li>
 *   <li>{@link GadgetState}s that represent the gadgets in the dashboard represented by this {@code DashboardState}
 *       are found in columns</li>
 * </ul></p>
 * 
 * <p>{@code DashboardState} objects should be built using the builders.  At a minimum, the {@link DashboardId}
 * and the title are required.</p>
 * 
 * <p>By doing a static import of the {@link #dashboard(DashboardId)} method, you can
 * create a {@code DashboardState} object with:
 * 
 * <pre>
 *     DashboardState state = dashboard(DashboardId.from(1000)).title("Menagerie").build();
 * </pre>
 * 
 * Or you can create a new {@code DashboardState} object from an existing one:
 * 
 * <pre>
 *     DashboardState state = dashboard(originalState).layout(Layout.AAA).build();
 * </pre>
 * </p>
 * 
 * <p>{@code DashboardState} implements the {@link Serializable} marker interface.  The marker is only implemented so that
 * {@code DashboardState} objects may be  distributed among remote systems that might be sharing a cache or need to
 * transfer {@code DashboardState}s for other reasons. Serialization is not meant to be used as a means of
 * persisting {@code DashboardState} objects between JVM restarts.</p>
 */
@Immutable
public final class DashboardState implements Serializable
{
    private static final long serialVersionUID = 4862870053224734927L;

    private final DashboardId id;
    private final String title;
    private final Layout layout;
    private List<? extends Iterable<GadgetState>> columns;
    private final long version;
    
    private DashboardState(DashboardState.Builder builder)
    {
        this.id = builder.id;
        this.title = builder.title;
        this.layout = builder.layout;
        this.version = builder.version;
        
        columns = copy(builder.columns, layout.getNumberOfColumns());
    }
    
    private List<? extends Iterable<GadgetState>> copy(Iterable<? extends Iterable<GadgetState>> columns, int size)
    {
        List<Iterable<GadgetState>> copy = new LinkedList<Iterable<GadgetState>>();
        if (columns != null)
        {
            for (Iterable<GadgetState> column : columns)
            {
                copy.add(Collections.unmodifiableList(toList(column)));
            }
        }
        pad(copy, size, Collections.<GadgetState>emptyList());
        return Collections.unmodifiableList(copy);
    }
    
    private <E, T extends E> void pad(Collection<E> list, int toSize, T obj)
    {
        list.addAll(nCopies(toSize - list.size(), obj));
    }
    
    /**
     * Reads the {@code DashboardState} object from the {@code ObjectInputStream}.  Checks that all class invariants
     * are respected.
     * 
     * @param in stream to read the object data from
     * @throws IOException thrown if there is a problem reading from the stream
     * @throws ClassNotFoundException if the class of a serialized object could not be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        
        columns = copy(columns, layout != null ? layout.getNumberOfColumns() : Layout.AA.getNumberOfColumns());
        
        if (id == null)
        {
            throw new InvalidObjectException("id cannot be null");
        }
        if (title == null)
        {
            throw new InvalidObjectException("title cannot be null");
        }
        if (layout == null)
        {
            throw new InvalidObjectException("layout cannot be null");
        }
        if (columns.size() != layout.getNumberOfColumns())
        {
            throw new InvalidObjectException("columns size must be " + layout.getNumberOfColumns());
        }
    }

    /**
     * Returns the unique identifier, represented by a {@code DashboardId}, for the dashboard's state.
     * 
     * @return the unique identifier for this dashboard's state.
     */
    public DashboardId getId()
    {
        return id;
    }

    /**
     * Returns the title of the dashboard.
     * 
     * @return the title of the dashboard
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * Returns the {@code Layout} of the dashboard.
     * 
     * @return the {@code Layout} of the dashboard
     */
    public Layout getLayout()
    {
        return layout;
    }
    
    /**
     * Returns an immutable {@code Iterable} of the {@code GadgetState}s in the given {@code column}.  They are
     * returned in the order that they appear on the dashboard from top to bottom.
     * 
     * @param column the index of the column to retrieve the {@code GadgetState}s for
     * @return an immutable {@code Iterable} of the {@code GadgetState} in the {@code column}
     */
    public Iterable<GadgetState> getGadgetsInColumn(ColumnIndex column)
    {
        return columns.get(column.index());
    }

    /**
     * Returns an immutable {@code Iterable} of the columns in this {@code DashboardState} (which contain the
     * {@code GadgetState}s).
     */
    public Iterable<? extends Iterable<GadgetState>> getColumns()
    {
        return columns;
    }

    /**
     * Returns this state's version so that it can be used by implementations when storing {@code DashboardState}s, to
     * make sure the same object is stored and retrieved
     * @return the version
     */
    public long getVersion()
    {
        return version;
    }

    /**
     * Returns a new DashboardState built with the same data as {@code this}, except that the column with index
     * {@code index} has had a new gadget added
     * @param gadgetState the new gadget to add
     * @param index the index of the column to add the new state to
     * @param prepend true if the new gadget should be prepended to its column, false if it should be appended
     * @return the new DashboardState
     */
    private DashboardState add(GadgetState gadgetState, ColumnIndex index, boolean prepend)
    {
        boolean foundRequestedColumn = false;
        List<Iterable<GadgetState>> modifiedColumns = new LinkedList<Iterable<GadgetState>>();
        for (ColumnIndex i : layout.getColumnRange())
        {
            Iterable<GadgetState> column = getGadgetsInColumn(i);

            // if we're on the column to add the gadget to
            if (i.equals(index))
            {
                foundRequestedColumn = true;

                // create a new list, and add the new gadget plus all the old ones
                List<GadgetState> newColumn = new LinkedList<GadgetState>();

                if(prepend)
                {
                    newColumn.add(gadgetState);
                    addExistingGadgetsToColumn(column, newColumn);
                }
                else
                {
                    addExistingGadgetsToColumn(column, newColumn);
                    newColumn.add(gadgetState);
                }

                modifiedColumns.add(Collections.unmodifiableList(toList(newColumn)));
            }
            else
            {
                modifiedColumns.add(column);
            }
        }

        if(!foundRequestedColumn)
        {
            throw new IllegalArgumentException("index is out of this dashboard's columns range");
        }

        // make a new DashboardState with the Builder that has the same data except for the columns
        return DashboardState.dashboard(this.id).title(this.title).layout(this.layout)
                .columns(modifiedColumns).version(this.version).build();
    }

    private void addExistingGadgetsToColumn(Iterable<GadgetState> column, List<GadgetState> newColumn)
    {
        for (GadgetState gadget : column)
        {
            newColumn.add(gadget);
        }
    }

    /**
     * Returns a new DashboardState built with the same data as {@code this}, except that the column with index
     * {@code index} has had a new gadget added to its top
     * @param gadgetState the new gadget to add
     * @param index the index of the column to add the new state to
     * @return the new DashboardState
     */
    public DashboardState prependGadgetToColumn(GadgetState gadgetState, ColumnIndex index)
    {
        return add(gadgetState, index, true);
    }

    /**
     * Returns a new DashboardState built with the same data as {@code this}, except that the column with index
     * {@code index} has had a new gadget added to its bottom
     * @param gadgetState the new gadget to add
     * @param index the index of the column to add the new state to
     * @return the new DashboardState
     */
    public DashboardState appendGadgetToColumn(GadgetState gadgetState, ColumnIndex index)
    {
        return add(gadgetState, index, false);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DashboardState))
        {
            return false;
        }
        DashboardState rhs = (DashboardState) o;
        boolean equals = new EqualsBuilder()
            .append(getId(), rhs.getId())
            .append(getTitle(), rhs.getTitle())
            .append(getLayout(), rhs.getLayout())
            .isEquals();
        if (!equals)
        {
            return false;
        }
        for (ColumnIndex columnIndex : getLayout().getColumnRange())
        {
            equals = elementsEqual(getGadgetsInColumn(columnIndex), rhs.getGadgetsInColumn(columnIndex));
            if (!equals)
            {
                break;
            }
        }
        return equals;
    }
    
    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder()
            .append(getId())
            .append(getTitle())
            .append(getLayout());
        for (ColumnIndex columnIndex : getLayout().getColumnRange())
        {
            builder.append(getGadgetsInColumn(columnIndex));
        }
        return builder.toHashCode();
    }
    
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("title", getTitle())
            .append("layout", getLayout())
            .append("columns", columns)
            .toString();
    }
    

    /**
     * Factory method which allows you to create a new {@code DashboardState} object based on an existing
     * {@code DashboardState}.
     * 
     * @param state the {@code DashboardState} to start with when building the new {@code DashboardState}
     * @return a {@code GadgetState.Builder} which allows you to set the layout or change the columns
     */
    public static Builder dashboard(DashboardState state)
    {
        return new Builder(notNull("state", state));
    }

    /**
     * Factory method to create a new builder which can be used to create {@code DashboardState} objects.  It returns
     * a {@code TitleBuilder} which only allows you to set the title.  After setting the title, a {@code Builder} will be
     * returned allowing other properties to be set. 
     * 
     * @param id unique identifier for the new {@code DashboardState} object
     * @return a {@code TitleBuilder} which can be used to set the title of the dashboard
     */
    public static TitleBuilder dashboard(DashboardId id)
    {
        return new TitleBuilder(notNull("id", id));
    }

    /**
     * A builder that allows you to set the title of the {@code DashboardState} object under construction.
     */
    public static class TitleBuilder
    {
        private DashboardId id;

        private TitleBuilder(DashboardId id)
        {
            this.id = id;                
        }
        
        /**
         * Sets the title of the {@code DashboardState} under construction and returns a {@code Builder} to allow
         * the {@link Layout} and the columns to be set.
         * 
         * @param title the title to use for the dashboard
         * @return {@code Builder} allowing further construction of the {@code DashboardState}
         */
        public Builder title(String title)
        {
            return new Builder(id, notNull("title", title));
        }
    }
    
    /**
     * A builder that allows the {@link Layout} or the columns of the {@code DashboardState} under construction to
     * be set.  Creating the final {@code DashboardState} is done by calling the {@link DashboardState.Builder#build}
     * method.
     */
    public static class Builder
    {
        private DashboardId id;
        private String title;
        private Layout layout = Layout.AA;
        private Iterable<? extends Iterable<GadgetState>> columns = Collections.emptyList();
        private long version = 0;
        
        private Builder(DashboardId id, String title)
        {
            this.id = id;
            this.title = title;
        }
        
        private Builder(DashboardState state)
        {
            this.id = state.getId();
            this.title = state.getTitle();
            this.layout = state.getLayout();
            List<Iterable<GadgetState>> columns = new ArrayList<Iterable<GadgetState>>();
            for (ColumnIndex columnIndex : layout.getColumnRange())
            {
                columns.add(state.getGadgetsInColumn(columnIndex));
            }
            this.columns = columns;
            this.version = state.getVersion();
        }
        
        /**
         * Set the {@code Layout} of the {@code DashboardState} under construction and return this {@code Builder}
         * to allow further construction to be done.
         * 
         * @param layout the {@code Layout} to use for the {@code DashboardState}
         * @return this builder to allow for further construction
         */
        public Builder layout(Layout layout)
        {
            this.layout = notNull("layout", layout);
            return this;
        }

        /**
         * Set the title of the {@code DashboardState} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param title the title to use for the {@code DashboardState}
         * @return this builder to allow for further construction
         */
        public Builder title(String title)
        {
            this.title = notNull("title", title);
            return this;
        }
        
        /**
         * Set the columns of the {@code DashboardState} under construction and return this {@code Builder}
         * to allow further construction to be done.
         * 
         * @param columns an {@code Iterable} list of {@code GadgetState} objects
         * @return this builder to allow for further construction
         */
        public Builder columns(Iterable<? extends Iterable<GadgetState>> columns)
        {
            this.columns = checkContentsNotNull(notNull("columns", columns));
            return this;
        }

        /**
         * Set the version of the {@code DashboardState} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param version the version of this DashboardState object
         * @return this builder to allow for further construction
         */
        public Builder version(long version)
        {
            this.version = version;
            return this;
        }

        /**
         * Returns the final constructed {@code DashboardState}
         * 
         * @return the {@code DashboardState}
         */
        public DashboardState build()
        {
            return new DashboardState(this);
        }
    }
    
    /**
     * There are a predetermined number of columns that a dashboard can contain, and {@code ColumnIndex} is the enumeration
     * of those columns. 
     */
    public enum ColumnIndex
    {
        /** The first, left-most column on the dashboard. */
        ZERO(0),
        
        /** The second, middle column (in a three column layout) on the dashboard */
        ONE(1),
        
        /** The third, and last column (in a three column layout) on the dashboard */
        TWO(2);
        
        private final int index;
        
        private ColumnIndex(int index)
        {
            this.index = index;
        }
        
        /**
         * Returns the actual value of the {@code ColumnIndex} as an {@code int}.
         * 
         * @return the actual value of the {@code ColumnIndex} as an {@code int}.
         */
        public int index()
        {
            return index;
        }
        
        /**
         * Returns {@code true} if there are more possible columns after this one, {@code false} otherwise.
         * 
         * @return {@code true} if there are more possible columns after this one, {@code false} otherwise.
         */
        public boolean hasNext()
        {
            return this != TWO;
        }
        
        /**
         * Returns the next column index after this one if there is one, equivalent to doing i+1 when the index is an
         * {@code int}.
         * 
         * @return the next column index after this one if there is one
         * @throws IllegalStateException if this is the last possible {@code ColumnIndex}
         */
        public ColumnIndex next()
        {
            if (!hasNext())
            {
                throw new IllegalStateException("No next column index, already at the max");
            }
            return from(index + 1);
        }
        
        /**
         * Returns the column {@code index} as an instance of {@code ColumnIndex}.
         * 
         * @param index the value we want as a {@code ColumnIndex}
         * @return the {@code ColumnIndex} that {@code index} corresponds to
         * @throws IllegalArgumentException if {@code index} is outside the range of valid column indices.
         */
        public static ColumnIndex from(int index)
        {
            switch(index)
            {
                case 0:
                    return ZERO;
                case 1:
                    return ONE;
                case 2:
                    return TWO;
                default:
                    throw new IllegalArgumentException("Valid values for Column are 0-2");
            }
        }
        
        /**
         * Returns an immutable {@code Iterable} over {@code ColumnIndex}es starting from {@code start} and 
         * ending with {@code end}, inclusive.  Extremely useful for looping over columns using the foreach construct.
         * 
         * @param start the first value to use in the {@code Iterable}
         * @param end the last value to use in the {@code Iterable}
         * @return an immutable {@code Iterable} over {@code ColumnIndex}es starting from {@code start} and ending with 
         *         {@code end}, inclusive
         */
        public static Iterable<ColumnIndex> range(final ColumnIndex start, final ColumnIndex end)
        {
            return new Iterable<ColumnIndex>()
            {
                public Iterator<ColumnIndex> iterator()
                {
                    return new Iterator<ColumnIndex>()
                    {
                        private ColumnIndex nextIndex = start;

                        public boolean hasNext()
                        {
                            return nextIndex != null;
                        }

                        public ColumnIndex next()
                        {
                            if (!hasNext())
                            {
                                throw new NoSuchElementException();
                            }
                            ColumnIndex currentIndex = nextIndex;
                            if (currentIndex.hasNext() && currentIndex != end)
                            {
                                nextIndex = currentIndex.next();
                            }
                            else
                            {
                                nextIndex = null;
                            }
                            return currentIndex;
                        }

                        public void remove()
                        {
                            throw new UnsupportedOperationException("Cannot remove elements from this iterator");
                        }
                    };
                }
            };
        }
    }
}