package com.atlassian.streams.api;

import java.net.URI;
import java.util.Map;

import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

public final class ActivityObjectTypes
{
    public static final String STANDARD_IRI_BASE = "http://activitystrea.ms/schema/1.0/";
    public static final String ATLASSIAN_IRI_BASE = "http://streams.atlassian.com/syndication/types/";

    public static Iterable<ActivityObjectType> getActivityObjectTypes(Iterable<ActivityObject> os)
    {
        return transform(os, getActivityObjectType());
    }
    
    public static Function<ActivityObject, ActivityObjectType> getActivityObjectType()
    {
        return GetActivityObjectType.INSTANCE;
    }
    
    private enum GetActivityObjectType implements Function<ActivityObject, ActivityObjectType>
    {
        INSTANCE;

        public ActivityObjectType apply(ActivityObject o)
        {
            return o.getActivityObjectType();
        }
    }
    
    public static TypeFactory newTypeFactory(String baseIri)
    {
        return new TypeFactoryImpl(baseIri);
    }

    public interface TypeFactory
    {
        ActivityObjectType newType(String key);
        ActivityObjectType newType(String key, ActivityObjectType parent);
    }

    private static final class TypeFactoryImpl implements TypeFactory
    {
        private final Map<Pair<String, Option<ActivityObjectType>>, ActivityObjectType> objectTypes;

        public TypeFactoryImpl(String baseIri)
        {
            objectTypes = new MapMaker().makeComputingMap(ObjectTypeFactory(baseIri));
        }

        public ActivityObjectType newType(String key)
        {
            return objectTypes.get(pair(key, none(ActivityObjectType.class)));
        }

        public ActivityObjectType newType(String key, ActivityObjectType parent)
        {
            return objectTypes.get(pair(key, some(parent)));
        }

        private static Function<Pair<String, Option<ActivityObjectType>>, ActivityObjectType> ObjectTypeFactory(final String baseIri)
        {
            return new ObjectTypeFactory(baseIri);
        }

        private static final class ObjectTypeFactory implements Function<Pair<String, Option<ActivityObjectType>>, ActivityObjectType>
        {
            private final String baseIri;

            private ObjectTypeFactory(String baseIri)
            {
                this.baseIri = baseIri;
            }

            public ActivityObjectType apply(Pair<String, Option<ActivityObjectType>> keyParent)
            {
                return newObjectType(keyParent.first(), URI.create(baseIri + keyParent.first()), keyParent.second());
            }

            public static ActivityObjectType newObjectType(String key, URI iri, Option<ActivityObjectType> parent)
            {
                return new ActivityObjectTypeImpl(key, iri, parent);
            }
        }

        private final static class ActivityObjectTypeImpl implements ActivityObjectType
        {
            private final String key;
            private final URI iri;
            private final Option<ActivityObjectType> parent;

            public ActivityObjectTypeImpl(String key, URI iri, Option<ActivityObjectType> parent)
            {
                this.key = checkNotNull(key, "key");
                this.iri = checkNotNull(iri, "iri");
                this.parent = checkNotNull(parent, "parent");
            }

            public URI iri()
            {
                return iri;
            }

            public String key()
            {
                return key;
            }

            public Option<ActivityObjectType> parent()
            {
                return parent;
            }

            @Override
            public String toString()
            {
                return key;
            }

            @Override
            public int hashCode()
            {
                return iri.hashCode();
            }

            @Override
            public boolean equals(Object obj)
            {
                if (this == obj)
                {
                    return true;
                }
                if (obj == null)
                {
                    return false;
                }
                if (!ActivityObjectType.class.isAssignableFrom(obj.getClass()))
                {
                    return false;
                }
                ActivityObjectType other = (ActivityObjectType) obj;
                return this.iri.equals(other.iri());
            }
        }
    }

    private static final TypeFactory standardTypes = newTypeFactory(STANDARD_IRI_BASE);

    public static ActivityObjectType comment()
    {
        return standardTypes.newType("comment");
    }

    public static ActivityObjectType article()
    {
        return standardTypes.newType("article");
    }

    public static ActivityObjectType file()
    {
        return standardTypes.newType("file");
    }

    public static ActivityObjectType status()
    {
        return standardTypes.newType("status");
    }
}
