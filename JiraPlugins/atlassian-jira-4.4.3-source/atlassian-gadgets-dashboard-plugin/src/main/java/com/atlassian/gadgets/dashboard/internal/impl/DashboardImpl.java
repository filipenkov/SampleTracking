package com.atlassian.gadgets.dashboard.internal.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetNotFoundException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.GadgetLayoutException;
import com.atlassian.gadgets.dashboard.internal.StateConverter;
import com.atlassian.gadgets.dashboard.internal.UserPref;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.gadgets.dashboard.spi.changes.AddGadgetChange;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.gadgets.dashboard.spi.changes.GadgetColorChange;
import com.atlassian.gadgets.dashboard.spi.changes.RemoveGadgetChange;
import com.atlassian.gadgets.dashboard.spi.changes.UpdateGadgetUserPrefsChange;
import com.atlassian.gadgets.dashboard.spi.changes.UpdateLayoutChange;
import com.atlassian.gadgets.spec.DataType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;

/**
 * Implements methods for operating on a backing {@link DashboardState} object.
 */
public class DashboardImpl implements Dashboard
{
    private DashboardState state;
    private StateConverter stateConverter;
    private List<DashboardChange> changes = Lists.newArrayList();

    private final GadgetRequestContext gadgetRequestContext;

    public DashboardImpl(DashboardState state, StateConverter stateConverter, GadgetRequestContext gadgetRequestContext)
    {
        this.state = checkNotNull(state, "state");
        this.stateConverter = checkNotNull(stateConverter, "stateConverter");
        this.gadgetRequestContext = checkNotNull(gadgetRequestContext, "gadgetRequestContext");
    }

    public DashboardId getId()
    {
        return state.getId();
    }

    public String getTitle()
    {
        return state.getTitle();
    }

    public Layout getLayout()
    {
        return state.getLayout();
    }
    
    public Iterable<Gadget> getGadgetsInColumn(ColumnIndex column)
    {
        if (!getLayout().contains(column))
        {
            throw new IllegalArgumentException("Invalid column index for layout " + getLayout());
        }
        return transform(state.getGadgetsInColumn(column), toGadgets());
    }

    public void appendGadget(Gadget gadget)
    {
        appendGadget(ColumnIndex.ZERO, gadget);
    }

    public void appendGadget(ColumnIndex columnIndex, Gadget gadget)
    {
        state = state.appendGadgetToColumn(gadget.getState(), columnIndex);
        changes.add(new AddGadgetChange(gadget.getState(), columnIndex, size(state.getGadgetsInColumn(columnIndex)) - 1));
    }

    public void addGadget(Gadget gadget)
    {
        addGadget(ColumnIndex.ZERO, gadget);
    }

    public void addGadget(ColumnIndex column, Gadget gadget)
    {
        state = state.prependGadgetToColumn(gadget.getState(), column);
        changes.add(new AddGadgetChange(gadget.getState(), column, 0));
    }

    public void changeLayout(Layout layout, GadgetLayout gadgetLayout) throws GadgetLayoutException
    {
        assertGadgetLayoutIsValid(layout, gadgetLayout);
        assertAllGadgetsPresent(gadgetLayout);

        state = DashboardState.dashboard(state)
            .layout(layout)
            .columns(getRearrangedColumns(layout, gadgetLayout))
            .build();
        changes.add(new UpdateLayoutChange(layout, gadgetLayout));
    }

    public void rearrangeGadgets(GadgetLayout gadgetLayout) throws GadgetLayoutException
    {
        assertGadgetLayoutIsValid(getLayout(), gadgetLayout);
        assertAllGadgetsPresent(gadgetLayout);

        state = DashboardState.dashboard(state)
            .columns(getRearrangedColumns(getLayout(), gadgetLayout))
            .build();
        changes.add(new UpdateLayoutChange(getLayout(), gadgetLayout));
    }

    public void changeGadgetColor(GadgetId gadgetId, Color color)
    {
        checkNotNull(gadgetId, "gadgetId");
        checkNotNull(color, "color");

        try
        {
            state = DashboardState.dashboard(state)
                .columns(updateGadget(withId(gadgetId), changeColorTo(color)))
                .build();
            changes.add(new GadgetColorChange(gadgetId, color));
        }
        catch (InternalGadgetNotFoundException e)
        {
            throw new GadgetNotFoundException(gadgetId);
        }
    }

    public void updateGadgetUserPrefs(GadgetId gadgetId, Map<String, String> prefValues)
    {
        checkNotNull(gadgetId, "gadgetId");
        checkNotNull(prefValues, "prefValues");

        try
        {
            state = DashboardState.dashboard(state)
                .columns(updateGadget(withId(gadgetId), updateUserPrefs(prefValues)))
                .build();
            changes.add(new UpdateGadgetUserPrefsChange(gadgetId, prefValues));
        }
        catch (InternalGadgetNotFoundException e)
        {
            throw new GadgetNotFoundException(gadgetId);
        }
    }

    public void removeGadget(GadgetId gadgetId)
    {
        state = DashboardState.dashboard(state)
            .columns(transform(state.getColumns(), removeGadget(withId(gadgetId))))
            .build();
        changes.add(new RemoveGadgetChange(gadgetId));
    }

    public DashboardState getState()
    {
        return state;
    }

    public Gadget findGadget(final GadgetId gadgetId)
    {
        for (DashboardState.ColumnIndex columnIndex : getLayout().getColumnRange())
        {
            for (GadgetState gadget : state.getGadgetsInColumn(columnIndex))
            {
                if(withId(gadgetId).apply(gadget))
                {
                    return toGadgets().apply(gadget);
                }
            }
        }
        return null;
    }

    public List<DashboardChange> getChanges()
    {
        return ImmutableList.copyOf(changes);
    }

    public void clearChanges()
    {
        changes = Lists.newArrayList();
    }

    private Function<GadgetState, Gadget> toGadgets()
    {
        return new GadgetStateConverter();
    }

    private final class GadgetStateConverter implements Function<GadgetState, Gadget>
    {
        public Gadget apply(GadgetState gadget)
        {
            return stateConverter.convertStateToGadget(gadget, gadgetRequestContext);
        }
    }

    private Function<GadgetState, GadgetState> changeColorTo(Color color)
    {
        return new ChangeColorTo(color);
    }

    private static final class ChangeColorTo implements Function<GadgetState, GadgetState>
    {
        private final Color color;

        public ChangeColorTo(Color color)
        {
            this.color = color;
        }

        public GadgetState apply(GadgetState gadget)
        {
            return GadgetState.gadget(gadget)
                .color(color)
                .build();
        }
    }

    private Function<GadgetState, GadgetState> updateUserPrefs(Map<String, String> prefValues)
    {
        return new UpdatePrefValues(prefValues);
    }

    private final class UpdatePrefValues implements Function<GadgetState, GadgetState>
    {
        private final Map<String, String> newPrefValues;

        public UpdatePrefValues(Map<String, String> newPrefValues)
        {
            this.newPrefValues = newPrefValues;
        }

        public GadgetState apply(GadgetState gadget)
        {
            return GadgetState.gadget(gadget)
                .userPrefs(updatedUserPrefs(stateConverter.convertStateToGadget(gadget, gadgetRequestContext), newPrefValues))
                .build();
        }
    }

    private Iterable<? extends Iterable<GadgetState>> updateGadget(Predicate<GadgetState> predicate, Function<GadgetState, GadgetState> function)
    {
        checkNotNull(predicate, "predicate");
        checkNotNull(function, "function");

        boolean foundGadget = false;
        ImmutableList.Builder<Iterable<GadgetState>> columnsBuilder = ImmutableList.builder();
        for (Iterable<GadgetState> column : state.getColumns())
        {
            ImmutableList.Builder<GadgetState> columnBuilder = ImmutableList.builder();
            for (GadgetState gadget : column)
            {
                if (predicate.apply(gadget))
                {
                    foundGadget = true;
                    columnBuilder.add(function.apply(gadget));
                }
                else
                {
                    columnBuilder.add(gadget);
                }
            }
            columnsBuilder.add(columnBuilder.build());
        }
        if (!foundGadget)
        {
            throw new InternalGadgetNotFoundException();
        }
        return columnsBuilder.build();
    }

    private static final class InternalGadgetNotFoundException extends RuntimeException {}

    private Map<String, String> updatedUserPrefs(Gadget gadget, Map<String, String> newPrefValues)
    {
        ImmutableMap.Builder<String, String> newUserPrefs = ImmutableMap.builder();
        for (UserPref userPref : gadget.getUserPrefs())
        {
            if (newPrefValues.containsKey(userPref.getName()))
            {
                String newValue = newPrefValues.get(userPref.getName());
                // AG-136: convert empty string to "false" for boolean values
                if (DataType.BOOL.equals(userPref.getDataType()) && StringUtils.isBlank(newValue))
                {
                    newValue = Boolean.FALSE.toString();
                }
                if (userPref.isRequired() && StringUtils.isBlank(newValue))
                {
                    throw new IllegalArgumentException("pref '" + userPref.getName() + "' is required " + "and must have a non-null, non-empty value");
                }
                newUserPrefs.put(userPref.getName(), newValue);
            }
            else
            {
                newUserPrefs.put(userPref.getName(), userPref.getValue());
            }
        }
        return newUserPrefs.build();
    }

    private Function<Iterable<GadgetState>, Iterable<GadgetState>> removeGadget(Predicate<GadgetState> predicate)
    {
        return new RemoveGadgetFunction(predicate);
    }

    private static final class RemoveGadgetFunction implements Function<Iterable<GadgetState>, Iterable<GadgetState>>
    {
        private final Predicate<GadgetState> predicate;

        public RemoveGadgetFunction(Predicate<GadgetState> predicate)
        {
            this.predicate = predicate;
        }

        public Iterable<GadgetState> apply(@Nullable Iterable<GadgetState> column)
        {
            return filter(column, not(predicate));
        }
    }

    private Predicate<GadgetState> withId(GadgetId gadgetId)
    {
        return new WithIdPredicate(gadgetId);
    }

    private static final class WithIdPredicate implements Predicate<GadgetState>
    {
        private final GadgetId gadgetId;

        public WithIdPredicate(GadgetId gadgetId)
        {
            this.gadgetId = gadgetId;
        }

        public boolean apply(@Nullable GadgetState gadgetState)
        {
            return gadgetState.getId().equals(gadgetId);
        }
    }

    private Iterable<Iterable<GadgetState>> getRearrangedColumns(Layout layout, GadgetLayout gadgetLayout)
    {
        Map<GadgetId, GadgetState> gadgets = getAllGadgets();
        ImmutableList.Builder<Iterable<GadgetState>> columnsBuilder = ImmutableList.builder();
        for (int i = 0; i < layout.getNumberOfColumns(); i++)
        {
            ImmutableList.Builder<GadgetState> columnBuilder = ImmutableList.builder();
            for (GadgetId gadgetId : gadgetLayout.getGadgetsInColumn(i))
            {
                if (!gadgets.containsKey(gadgetId))
                {
                    continue;
                }
                columnBuilder.add(gadgets.get(gadgetId));
                gadgets.remove(gadgetId);
            }
            columnsBuilder.add(columnBuilder.build());
        }
        return columnsBuilder.build();
    }

    private Map<GadgetId, GadgetState> getAllGadgets()
    {
        Map<GadgetId, GadgetState> gadgets = new HashMap<GadgetId, GadgetState>();
        for (Iterable<GadgetState> column : state.getColumns())
        {
            for (GadgetState gadgetState : column)
            {
                gadgets.put(gadgetState.getId(), gadgetState);
            }
        }
        return gadgets;
    }

    public int getNumberOfGadgets()
    {
        int numberOfGadgets = 0;
        for (Iterable<GadgetState> column : state.getColumns())
        {
            numberOfGadgets += size(column);
        }
        return numberOfGadgets;
    }

    private void assertAllGadgetsPresent(GadgetLayout gadgetLayout) throws GadgetLayoutException
    {
        Map<GadgetId, GadgetState> gadgets = getAllGadgets();
        for (int i = 0; i < gadgetLayout.getNumberOfColumns(); i++)
        {
            for (GadgetId gadgetId : gadgetLayout.getGadgetsInColumn(i))
            {
                gadgets.remove(gadgetId);
            }
        }
        if (!gadgets.isEmpty())
        {
            throw new GadgetLayoutException("Gadgets cannot be removed by changing the layout, they need to be removed explicitly");
        }
    }

    /**
     * Checks to make sure there are no gadgets in out of bounds columns.
     *
     * @param layout the layout to verify against
     * @param gadgetLayout the gadget layout to verify
     * @throws GadgetLayoutException if the gadgetLayout contains more columns than this layout allows
     */
    public void assertGadgetLayoutIsValid(Layout layout, GadgetLayout gadgetLayout) throws GadgetLayoutException
    {
        if (gadgetLayout.getNumberOfColumns() > layout.getNumberOfColumns())
        {
            throw new GadgetLayoutException("New layout has " + gadgetLayout.getNumberOfColumns() +
                    " but the current layout only allows " + layout.getNumberOfColumns() + " columns.");
        }
    }
}