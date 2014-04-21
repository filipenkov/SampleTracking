package com.atlassian.crowd.model;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;

import com.google.common.base.Function;

import java.util.Comparator;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.compareToInLowerCase;

/**
 * Provides case-insensitive normalisation for {@link String}, model {@link User}s and model {@link Group}s.
 */
public class NameComparator
{
    private static final DirectoryEntityNameComparator DIRECTORY_ENTITY_NAME_COMPARATOR = new DirectoryEntityNameComparator();
    private static final StringNameComparator STRING_NAME_COMPARATOR = new StringNameComparator();

    private static final DirectoryEntityNameNormaliser DIRECTORY_ENTITY_NAME_NORMALISER = new DirectoryEntityNameNormaliser();
    private static final StringNameNormaliser STRING_NAME_NORMALISER = new StringNameNormaliser();

    private NameComparator()
    {
    }

    /**
     * @deprecated use a {@link #normaliserOf(Class)} instead to reduce the number of transformations
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> of(Class<T> type)
    {
        if (String.class.isAssignableFrom(type))
        {
            return (Comparator<T>) STRING_NAME_COMPARATOR;
        }
        else if (User.class.isAssignableFrom(type))
        {
            return (Comparator<T>) DIRECTORY_ENTITY_NAME_COMPARATOR;
        }
        else if (Group.class.isAssignableFrom(type))
        {
            return (Comparator<T>) DIRECTORY_ENTITY_NAME_COMPARATOR;
        }
        else
        {
            throw new IllegalArgumentException("Can't find name comparator for type " + type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<T, String> normaliserOf(Class<T> type)
    {
        if (String.class.isAssignableFrom(type))
        {
            return (Function<T, String>) STRING_NAME_NORMALISER;
        }
        else if (User.class.isAssignableFrom(type))
        {
            return (Function<T, String>) DIRECTORY_ENTITY_NAME_NORMALISER;
        }
        else if (Group.class.isAssignableFrom(type))
        {
            return (Function<T, String>) DIRECTORY_ENTITY_NAME_NORMALISER;
        }
        else
        {
            throw new IllegalArgumentException("Can't find name normaliser for type " + type);
        }
    }

    private static class DirectoryEntityNameComparator implements Comparator<DirectoryEntity>
    {
        @Override
        public int compare(DirectoryEntity o1, DirectoryEntity o2)
        {
            return compareToInLowerCase(o1.getName(), o2.getName());
        }
    }

    private static class StringNameComparator implements Comparator<String>
    {
        public int compare(String o1, String o2)
        {
            return compareToInLowerCase(o1, o2);
        }
    }

    private static class DirectoryEntityNameNormaliser implements Function<DirectoryEntity, String>
    {
        @Override
        public String apply(DirectoryEntity from)
        {
            return IdentifierUtils.toLowerCase(from.getName());
        }
    }

    private static class StringNameNormaliser implements Function<String, String>
    {
        @Override
        public String apply(String from)
        {
            return IdentifierUtils.toLowerCase(from);
        }
    }
}
