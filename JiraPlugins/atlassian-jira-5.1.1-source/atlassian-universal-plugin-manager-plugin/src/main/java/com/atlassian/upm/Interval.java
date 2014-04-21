package com.atlassian.upm;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class Interval<T extends Comparable<T>>
{
    public static abstract class Bound<T extends Comparable<T>>
    {
        public static enum Type
        {
            INCLUSIVE, EXCLUSIVE
        }

        private final T value;
        private final Type type;

        public Bound(T value, Type type)
        {
            this.value = checkNotNull(value, "value");
            this.type = checkNotNull(type, "type");
        }

        public T getValue()
        {
            return value;
        }

        public Type getType()
        {
            return type;
        }

        public abstract boolean contains(T value);
    }

    public static final class Floor<T extends Comparable<T>> extends Bound<T>
    {
        private static final BiMap<Type, Character> typeToChar =
            ImmutableBiMap.of(Type.INCLUSIVE, '[', Type.EXCLUSIVE, '(');
        private static final BiMap<Character, Type> charToType =
            typeToChar.inverse();

        public Floor(T value, Type type)
        {
            super(value, type);
        }

        public boolean contains(T value)
        {
            int comparison = getValue().compareTo(value);
            switch (getType())
            {
                case INCLUSIVE:
                    if (comparison > 0) return false;
                    break;
                case EXCLUSIVE:
                    if (comparison >= 0) return false;
                    break;
            }
            return true;
        }

        public static Type getType(char type)
        {
            return charToType.get(type);
        }

        public String toString()
        {
            return String.format("%c%s", typeToChar.get(getType()), getValue());
        }
    }

    public static final class Ceiling<T extends Comparable<T>> extends Bound<T>
    {
        private static final BiMap<Type, Character> typeToChar =
            ImmutableBiMap.of(Type.INCLUSIVE, ']', Type.EXCLUSIVE, ')');
        private static final BiMap<Character, Type> charToType =
            typeToChar.inverse();

        public Ceiling(T value, Type type)
        {
            super(value, type);
        }

        public boolean contains(T value)
        {
            int comparison = getValue().compareTo(value);
            switch (getType())
            {
                case INCLUSIVE:
                    if (comparison < 0) return false;
                    break;
                case EXCLUSIVE:
                    if (comparison <= 0) return false;
                    break;
            }
            return true;
        }

        public static Type getType(char type)
        {
            return charToType.get(type);
        }

        public String toString()
        {
            return String.format("%s%c", getValue(), typeToChar.get(getType()));
        }
    }

    private final @Nullable Floor<T> floor;
    private final @Nullable Ceiling<T> ceiling;

    public Interval(@Nullable Floor<T> floor, @Nullable Ceiling<T> ceiling)
    {
        this.floor = floor;
        this.ceiling = ceiling;
    }

    @Nullable
    public Floor<T> getFloor()
    {
        return floor;
    }

    @Nullable
    public Ceiling<T> getCeiling()
    {
        return ceiling;
    }

    public boolean contains(T value)
    {
        return
            (floor == null || floor.contains(value)) &&
                (ceiling == null || ceiling.contains(value));
    }

    public String toString()
    {
        return String.format("%s,%s",
            floor == null ? "(_" : floor,
            ceiling == null ? "_)" : ceiling);
    }
}
