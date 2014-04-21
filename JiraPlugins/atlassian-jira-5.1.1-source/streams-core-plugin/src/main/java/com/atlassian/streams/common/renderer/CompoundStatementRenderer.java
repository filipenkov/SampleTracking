package com.atlassian.streams.common.renderer;

import java.util.Iterator;
import java.util.List;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.common.Option;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Options.catOptions;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.partition;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;

final class CompoundStatementRenderer<T> implements Function<Iterable<T>, Option<Html>>
{
    private final I18nResolver i18nResolver;
    private final Function<T, Option<Html>> render;

    public CompoundStatementRenderer(I18nResolver i18nResolver, Function<T, Option<Html>> render)
    {
        this.i18nResolver = i18nResolver;
        this.render = render;
    }

    public Option<Html> apply(Iterable<T> xs)
    {
        Iterable<Html> rendered = ImmutableList.copyOf(catOptions(transform(xs, render)));
        if (isEmpty(rendered))
        {
            return none();
        }
        int numRendered = size(rendered);
        if (numRendered == 1)
        {
            return some(get(rendered, 0));
        }

        Iterator<List<Html>> partitions = partition(rendered, numRendered - 1).iterator();
        Iterable<Html> allButLast = partitions.next();
        Iterable<Html> last = partitions.next();

        return some(new Html(new StringBuilder()
            .append(Joiner.on(", ").join(allButLast))
            .append(" ")
            .append(i18nResolver.getText("streams.and"))
            .append(" ")
            .append(get(last, 0))
            .toString()));
    }

}
