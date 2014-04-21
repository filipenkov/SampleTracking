package com.atlassian.streams.api;

import com.atlassian.streams.api.common.Option;

import static com.google.common.base.Preconditions.checkNotNull;

public class StreamsFeed
{
    private final String title;
    private final Option<String> subtitle;
    private final Iterable<StreamsEntry> entries;

    public StreamsFeed(String title, Iterable<StreamsEntry> entries, Option<String> subtitle)
    {
        this.title = checkNotNull(title, "title");
        this.entries = checkNotNull(entries, "entries");
        this.subtitle = checkNotNull(subtitle, "subtitle");
    }

    public String getTitle()
    {
        return title;
    }

    public Iterable<StreamsEntry> getEntries()
    {
        return entries;
    }

    public Option<String> getSubtitle()
    {
        return subtitle;
    }
}
