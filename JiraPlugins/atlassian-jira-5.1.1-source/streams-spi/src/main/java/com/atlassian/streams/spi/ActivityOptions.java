package com.atlassian.streams.spi;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.spi.StreamsFilterOptionProvider.ActivityOption;

import com.google.common.base.Function;

import static com.atlassian.streams.spi.StandardStreamsFilterOption.ACTIVITY_OBJECT_VERB_SEPARATOR;

public final class ActivityOptions
{
    private ActivityOptions() {}

    public static Function<Pair<ActivityObjectType, ActivityVerb>, ActivityOption> toActivityOption(I18nResolver i18nResolver, String messageKeyPrefix)
    {
        return new ToActivityOption(i18nResolver, messageKeyPrefix);
    }
    
    private static final class ToActivityOption implements Function<Pair<ActivityObjectType, ActivityVerb>, ActivityOption>
    {
        private final I18nResolver i18nResolver;
        private final String messageKeyPrefix;

        public ToActivityOption(I18nResolver i18nResolver, String messageKeyPrefix)
        {
            this.i18nResolver = i18nResolver;
            this.messageKeyPrefix = messageKeyPrefix;
        }

        public ActivityOption apply(Pair<ActivityObjectType, ActivityVerb> a)
        {
            String name = i18nResolver.getText(messageKeyPrefix + "." + a.first().key() + "." + a.second().key());
            return new ActivityOption(name, a.first(), a.second());
        }
    }

    public static Function<ActivityOption, String> toActivityOptionKey()
    {
        return ActivityOptionValue.INSTANCE;
    }
    
    private enum ActivityOptionValue implements Function<ActivityOption, String>
    {
        INSTANCE;

        public String apply(ActivityOption a)
        {
            return a.getType().key() + ACTIVITY_OBJECT_VERB_SEPARATOR + a.getVerb().key();
        }
    }

}
