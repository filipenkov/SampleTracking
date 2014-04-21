package com.atlassian.streams.api;

import java.net.URI;
import java.util.Map;

import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for working with {@link ActivityVerb}s
 */
public class ActivityVerbs
{
    public static final String STANDARD_IRI_BASE = "http://activitystrea.ms/schema/1.0/";
    public static final String ATLASSIAN_IRI_BASE = "http://streams.atlassian.com/syndication/verbs/";

    public static Function<ActivityVerb, String> verbsToKeys()
    {
        return VerbsToKeys.INSTANCE;
    }

    private enum VerbsToKeys implements Function<ActivityVerb, String>
    {
        INSTANCE;
        
        public String apply(ActivityVerb verb)
        {
            return verb.key();
        }
    };

    public static VerbFactory newVerbFactory(String baseIri)
    {
        return new VerbFactoryImpl(baseIri);
    }
    
    public interface VerbFactory
    {
        ActivityVerb newVerb(String key);
        ActivityVerb newVerb(String key, ActivityVerb parent);
    }
    
    private static final class VerbFactoryImpl implements VerbFactory
    {
        private final Map<Pair<String, Option<ActivityVerb>>, ActivityVerb> verbs;
        
        public VerbFactoryImpl(String baseIri)
        {
            verbs = new MapMaker().makeComputingMap(verbFactory(baseIri));
        }

        public ActivityVerb newVerb(String key)
        {
            return verbs.get(pair(key, none(ActivityVerb.class)));
        }

        public ActivityVerb newVerb(String key, ActivityVerb parent)
        {
            return verbs.get(pair(key, some(parent)));
        }    

        private static Function<Pair<String, Option<ActivityVerb>>, ActivityVerb> verbFactory(final String baseIri)
        {
            return new VerbFactory(baseIri);
        }

        private static final class VerbFactory implements Function<Pair<String, Option<ActivityVerb>>, ActivityVerb>
        {
            private final String baseIri;

            private VerbFactory(String baseIri)
            {
                this.baseIri = baseIri;
            }

            public ActivityVerb apply(Pair<String, Option<ActivityVerb>> keyParent)
            {
                return newVerb(keyParent.first(), URI.create(baseIri + keyParent.first()), keyParent.second());
            }

            public static ActivityVerb newVerb(String key, URI iri, Option<ActivityVerb> parent)
            {
                return new ActivityVerbTypeImpl(key, iri, parent);
            }
        }
        
        private final static class ActivityVerbTypeImpl implements ActivityVerb
        {
            private final String key;
            private final URI iri;
            private final Option<ActivityVerb> parent;

            public ActivityVerbTypeImpl(String key, URI iri, Option<ActivityVerb> parent)
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

            public Option<ActivityVerb> parent()
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
                if (!ActivityVerb.class.isAssignableFrom(obj.getClass()))
                {
                    return false;
                }
                ActivityVerb other = (ActivityVerb) obj;
                return this.iri.equals(other.iri());
            }
        }
    }

    private static final VerbFactory standardVerbs = newVerbFactory(STANDARD_IRI_BASE);

    public static ActivityVerb post()
    {
        return standardVerbs.newVerb("post");
    }
    
    public static ActivityVerb update()
    {
        return standardVerbs.newVerb("update");
    }
    
    public static ActivityVerb like()
    {
        return standardVerbs.newVerb("like");
    }
}
