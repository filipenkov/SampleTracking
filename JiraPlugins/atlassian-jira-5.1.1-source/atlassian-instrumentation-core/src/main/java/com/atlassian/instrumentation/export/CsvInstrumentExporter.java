package com.atlassian.instrumentation.export;

import com.atlassian.instrumentation.AbsoluteCounter;
import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.DerivedCounter;
import com.atlassian.instrumentation.Gauge;
import com.atlassian.instrumentation.Instrument;
import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;

import com.atlassian.instrumentation.caches.CacheInstrument;
import com.atlassian.instrumentation.operations.OpInstrument;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * A CSV implementation of {@link InstrumentExporter}
 *
 * @since v4.0
 */
public class CsvInstrumentExporter implements InstrumentExporter
{
    public String getMimeType()
    {
        return "text/csv";
    }

    public void export(final List<Instrument> instruments, final Writer writer) throws IOException
    {
        notNull("instruments", instruments);
        notNull("writer", writer);

        writer.write("#name, value, type, count, time, size\n");
        for (Instrument instrument : instruments)
        {
            if (instrument != null)
            {
                writeInstrument(instrument, writer);
                writer.write("\n");
            }
        }
    }

    @Override
    public String export(final Instrument instrument)
    {
        notNull("instrument", instrument);

        final StringWriter out = new StringWriter();
        try
        {
            writeInstrument(instrument, out);
        }
        catch(IOException e)
        {
            //should never happen since we're using a StringWriter.
            return "";
        }
        return out.toString();
    }

    private void writeInstrument(final Instrument instrument, final Writer writer) throws IOException
    {
        final StringBuilder attrs = new StringBuilder();
        appendAttr(attrs, instrument.getName());
        appendAttr(attrs, instrument.getValue());
        getOtherAttrs(attrs, instrument);

        writer.write(attrs.toString());
    }

    private void getOtherAttrs(final StringBuilder attrs, final Instrument instrument)
    {
        if (instrument instanceof OpInstrument)
        {
            appendAttr(attrs, "opInstrument");

            OpInstrument opInstrument = (OpInstrument) instrument;
            appendAttr(attrs, opInstrument.getInvocationCount());
            appendAttr(attrs, opInstrument.getMillisecondsTaken());
            appendAttr(attrs, opInstrument.getResultSetSize());
            appendAttr(attrs, opInstrument.getCpuTime());
        }
        else if (instrument instanceof CacheInstrument)
        {
            appendAttr(attrs, "cacheInstrument");

            CacheInstrument cacheInstrument = (CacheInstrument) instrument;
            appendAttr(attrs, cacheInstrument.getMisses());
            appendAttr(attrs, cacheInstrument.getMissTime());
            appendAttr(attrs, cacheInstrument.getHits());
            appendAttr(attrs, cacheInstrument.getCacheSize());
            appendAttr(attrs, String.valueOf(cacheInstrument.getHitMissRatio()));
        }
        else if (instrument instanceof AbsoluteCounter)
        {
            appendAttr(attrs, "absoluteCounter");
        }
        else if (instrument instanceof DerivedCounter)
        {
            appendAttr(attrs, "derivedCounter");
        }
        else if (instrument instanceof Counter)
        {
            appendAttr(attrs, "counter");
        }
        else if (instrument instanceof Gauge)
        {
            appendAttr(attrs, "gauge");
        }
        else
        {
            appendAttr(attrs, instrument.getClass().getName());
        }
    }

    private void appendAttr(StringBuilder sb, String value)
    {
        needsAComma(sb);
        sb.append(esc(value));
    }

    private void appendAttr(StringBuilder sb, long value)
    {
        needsAComma(sb);
        sb.append(value);
    }

    private void needsAComma(final StringBuilder sb)
    {
        if (sb.length() > 0)
        {
            sb.append(",");
        }
    }

    private String esc(String value)
    {
        return value;
    }

}