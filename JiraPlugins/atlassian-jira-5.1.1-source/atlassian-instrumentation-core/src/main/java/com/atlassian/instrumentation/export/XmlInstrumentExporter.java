package com.atlassian.instrumentation.export;

import com.atlassian.instrumentation.AbsoluteCounter;
import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.DerivedCounter;
import com.atlassian.instrumentation.Gauge;
import com.atlassian.instrumentation.Instrument;
import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;

import com.atlassian.instrumentation.caches.CacheInstrument;
import com.atlassian.instrumentation.operations.OpInstrument;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * An XML implementation
 *
 * @since v4.0
 */
public class XmlInstrumentExporter implements InstrumentExporter
{
    public String getMimeType()
    {
        return "text/xml";
    }

    public void export(final List<Instrument> instruments, final Writer writer) throws IOException
    {
        notNull("instruments", instruments);
        notNull("writer", writer);

        writer.write("<instruments>");
        for (Instrument instrument : instruments)
        {
            if (instrument != null)
            {
                writeInstrument(instrument, writer);
            }
        }
        writer.write("</instruments>");
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
        appendAttr(attrs, "name", instrument.getName());
        appendAttr(attrs, "value", instrument.getValue());
        getOtherAttrs(attrs, instrument);

        writer.write("<instrument");
        writer.write(attrs.toString());
        writer.write("/>");

    }

    private void getOtherAttrs(final StringBuilder attrs, final Instrument instrument)
    {
        if (instrument instanceof OpInstrument)
        {
            appendAttr(attrs, "type", "opInstrument");

            OpInstrument opInstrument = (OpInstrument) instrument;
            appendAttr(attrs, "count", opInstrument.getInvocationCount());
            appendAttr(attrs, "time", opInstrument.getMillisecondsTaken());
            appendAttr(attrs, "size", opInstrument.getResultSetSize());
            appendAttr(attrs, "cpu", opInstrument.getCpuTime());
        }
        else if (instrument instanceof CacheInstrument)
        {
            appendAttr(attrs, "type", "cacheInstrument");

            CacheInstrument cacheInstrument = (CacheInstrument) instrument;
            appendAttr(attrs, "misses", cacheInstrument.getMisses());
            appendAttr(attrs, "missTime", cacheInstrument.getMissTime());
            appendAttr(attrs, "hits", cacheInstrument.getHits());
            appendAttr(attrs, "size", cacheInstrument.getCacheSize());
            appendAttr(attrs, "hitMissRatio", String.valueOf(cacheInstrument.getHitMissRatio()));
        }
        else if (instrument instanceof AbsoluteCounter)
        {
            appendAttr(attrs, "type", "absoluteCounter");
        }
        else if (instrument instanceof DerivedCounter)
        {
            appendAttr(attrs, "type", "derivedCounter");
        }
        else if (instrument instanceof Counter)
        {
            appendAttr(attrs, "type", "counter");
        }
        else if (instrument instanceof Gauge)
        {
            appendAttr(attrs, "type", "gauge");
        }
        else
        {
            appendAttr(attrs, "type", instrument.getClass().getName());
        }
    }

    private void appendAttr(StringBuilder sb, String name, String value)
    {
        sb.append(" ").append(name).append("=\"").append(esc(value)).append("\"");
    }

    private void appendAttr(StringBuilder sb, String name, long value)
    {
        sb.append(" ").append(name).append("=\"").append(value).append("\"");
    }

    private String esc(String value)
    {
        return StringEscapeUtils.escapeXml(value);
    }
}
