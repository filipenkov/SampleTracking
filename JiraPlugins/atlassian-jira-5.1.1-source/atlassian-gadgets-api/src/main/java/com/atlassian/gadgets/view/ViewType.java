package com.atlassian.gadgets.view;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a context under which a gadget will be viewed. {@code ViewType}s can be registered via
 * {@code createViewType} and unregistered via {@code removeViewType}.
 */
public final class ViewType
{
    private static final Map<String, ViewType> allViewTypes = new HashMap<String, ViewType>();
    private static final ReentrantReadWriteLock viewTypeRegistrationLock = new ReentrantReadWriteLock();
    private static final Lock readLock = viewTypeRegistrationLock.readLock();
    private static final Lock writeLock = viewTypeRegistrationLock.writeLock();

    public static final ViewType DEFAULT = createViewType("default", "DEFAULT", "DASHBOARD", "profile", "home");
    public static final ViewType CANVAS = createViewType("canvas");


    private final String name;
    private final List<String> aliases;

    private ViewType(String name, String... aliases)
    {
        this.name = name;
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    }

    /**
     * Creates a {@code ViewType} with the given canonical name and optional aliases. The {@code name} and
     * {@code aliases} must be unique : no two {@code ViewTypes} can share names or aliases. If any of the {@code name}
     * or {@code aliases} is associated with another {@code ViewType}, an {@code IllegalArgumentException} will be thrown
     * @param name the unique canonical name for the {@code ViewType}
     * @param aliases optional aliases for this {@code ViewType}
     * @return the created {@code ViewType}
     */
    public static ViewType createViewType(String name, String... aliases)
    {
        writeLock.lock();

        try
        {
            // First, check for uniqueness of the name and all aliases
            if (allViewTypes.containsKey(name))
            {
                throw new IllegalArgumentException("Failed to create ViewType; an existing ViewType with name  " + name + " already exists");
            }

            for (String alias : aliases)
            {
                if (allViewTypes.containsKey(alias))
                {
                    throw new IllegalArgumentException("Failed to create ViewType; an existing ViewType with alias  " + alias + " already exists");
                }
            }

            // Separately, register all the names and aliases
            ViewType viewType = new ViewType(name, aliases);
            allViewTypes.put(name, viewType);
            for (String alias : aliases)
            {
                allViewTypes.put(alias, viewType);
            }

            return viewType;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Removes a {@code ViewType}. Its name and all its aliases are free to be used in new {@code ViewTypes}
     * @param viewType
     * @return
     */
    public static boolean removeViewType(ViewType viewType)
    {
        writeLock.lock();
        try
        {
            boolean result = allViewTypes.remove(viewType.getCanonicalName()) != null;

            if (result)
            {
                for (String alias : viewType.getAliases())
                {
                   ViewType aliasedView = allViewTypes.remove(alias);
                   assert viewType.equals(aliasedView);
                }
            }
            return result;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Gets the canonical name of this {@code ViewType}
     * @return the canonical name
     */
    public String getCanonicalName()
    {
        return name;
    }

    /**
     * Gets the aliases for this {@code ViewType}
     * @return this {@code ViewType}'s aliases
     */
    public Collection<String> getAliases()
    {
        return aliases;
    }

    /**
     * Returns the {@code ViewType} associated with the {@code value}. Previously, a {@code ViewType} must
     * have been created (and not subsequently deleted) with the name {@code value} or with an alias {@code value}.
     * If no such {@code ViewType} exists, an {@code IllegalArgumentException} will be thrown.
     * @param value
     * @return
     */
    public static ViewType valueOf(final String value)
    {
        readLock.lock();
        try
        {
            ViewType result = allViewTypes.get(value);

            if (result == null)
            {
                throw new IllegalArgumentException("No such ViewType: " + value);
            }
            return result;
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public String toString()
    {
        return getCanonicalName();
    }
}
