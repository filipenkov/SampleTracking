package com.atlassian.soy.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Singleton;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.internal.base.CharEscaper;
import com.google.template.soy.internal.base.CharEscapers;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcFunction;
import com.google.template.soy.tofu.restricted.SoyTofuFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I18N getText() soy function.
 * 
 * @since v4.4
 */
@Singleton
public class GetTextFunction implements SoyJsSrcFunction, SoyTofuFunction
{
    private static final Logger log = LoggerFactory.getLogger(GetTextFunction.class);
    
    private static final Pattern STRING_ARG = Pattern.compile("^'(.*)'$");
    private static final Function<SoyData, Serializable> SOY_DATA_TO_SERIALIZABLE_FUNCTION = new Function<SoyData, Serializable>()
    {
        @Override
        public Serializable apply(SoyData fromSoyData)
        {
            Object convertedSoyData = SoyDataConverter.convertFromSoyData(fromSoyData);
            if (convertedSoyData instanceof Serializable)
            {
                return (Serializable) convertedSoyData;
            }
            if (convertedSoyData == null)
            {
                return NullData.INSTANCE.toString();
            }
            if (log.isWarnEnabled())
            {
                log.warn("Conversion of {} from {} is not a Serializable, defaulting to toString() invocation.", convertedSoyData.getClass().getName(), fromSoyData.getClass().getName());
            }
            return convertedSoyData.toString();
        }
    };

    private static final Set<Integer> ARGS_SIZES;
    final I18nResolver i18nResolver;

    @Inject
    public GetTextFunction(I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    static
    {
        final ImmutableSet.Builder<Integer> args = ImmutableSet.builder();
        // we support 1 or more args, lets just pick an upper limit
        for (int i = 1; i < 20; i++)
        {
            args.add(i);
        }
        ARGS_SIZES = args.build();

    }

    @Override
    public String getName()
    {
        return "getText";
    }

    @Override
    public Set<Integer> getValidArgsSizes()
    {
        return ARGS_SIZES;
    }

    @Override
    public JsExpr computeForJsSrc(List<JsExpr> args)
    {
        JsExpr keyExpr = args.get(0);
        final Matcher m = STRING_ARG.matcher(keyExpr.getText());
        if (!m.matches())
        {
            throw new SoySyntaxException("Argument to getText() is not a literal string: " + keyExpr.getText());
        }
        String key = m.group(1);

        CharEscaper jsEscaper = CharEscapers.javascriptEscaper();

        StringBuilder call = new StringBuilder();
        if (args.size() == 1)
        {
            // don't bother with call to format, render on server side
            call.append("\"").append(jsEscaper.escape(i18nResolver.getText(key))).append("\"");
        }
        else
        {
            final String msg = i18nResolver.getRawText(key);
            call.append("AJS.format(");
            call.append("\"").append(jsEscaper.escape(msg)).append("\"");
            for (int i = 1; i < args.size(); i++)
            {
                JsExpr arg = args.get(i);
                call.append(",");
                call.append(arg.getText());
            }
            call.append(")");
        }

        return new JsExpr(call.toString(), Integer.MAX_VALUE);
    }

    @Override
    public SoyData computeForTofu(List<SoyData> args)
    {
        SoyData data = args.get(0);
        if (!(data instanceof StringData))
        {
            throw new SoySyntaxException("Argument to getText() is not a literal string");
        }

        List<SoyData> params = args.subList(1, args.size());
        StringData stringData = (StringData) data;
        String text = i18nResolver.getText(stringData.getValue(), transformSoyDataListToSerializableArray(params));
        return StringData.forValue(text);
    }

    private Serializable[] transformSoyDataListToSerializableArray(List<SoyData> params)
    {
        return Iterables.toArray(Iterables.transform(params, SOY_DATA_TO_SERIALIZABLE_FUNCTION), Serializable.class);
    }
}
