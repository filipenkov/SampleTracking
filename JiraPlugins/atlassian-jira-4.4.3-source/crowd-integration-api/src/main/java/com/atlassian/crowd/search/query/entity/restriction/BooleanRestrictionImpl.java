package com.atlassian.crowd.search.query.entity.restriction;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class BooleanRestrictionImpl implements BooleanRestriction
{
    private final Collection<SearchRestriction> restrictions;
    private final BooleanLogic booleanLogic;

    public BooleanRestrictionImpl(final BooleanLogic booleanLogic, final SearchRestriction... restrictions)
    {
        this(booleanLogic, Arrays.asList(restrictions));
    }

    public BooleanRestrictionImpl(final BooleanLogic booleanLogic, final Collection<SearchRestriction> restrictions)
    {
        Validate.notNull(booleanLogic, "booleanLogic cannot be null");
        Validate.notEmpty(restrictions, "booleanLogic cannot be empty");

        this.restrictions = restrictions;
        this.booleanLogic = booleanLogic;
    }

    public final Collection<SearchRestriction> getRestrictions()
    {
        return Collections.unmodifiableCollection(restrictions);
    }

    public final BooleanLogic getBooleanLogic()
    {
        return booleanLogic;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("restrictions", restrictions).
                append("booleanLogic", booleanLogic).
                toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof BooleanRestriction)) return false;

        BooleanRestriction that = (BooleanRestriction) o;

        if (booleanLogic != that.getBooleanLogic()) return false;
        if (restrictions == null)
        {
            if (that.getRestrictions() != null)
            {
                return false;
            }
        }
        else
        {
            boolean sizeEqual = restrictions.size() == that.getRestrictions().size();
            if (!sizeEqual || !restrictions.containsAll(that.getRestrictions()))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = restrictions != null ? restrictions.hashCode() : 0;
        result = 31 * result + (booleanLogic != null ? booleanLogic.hashCode() : 0);
        return result;
    }
}
