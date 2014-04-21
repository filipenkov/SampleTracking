package com.atlassian.instrumentation.export;

import com.atlassian.instrumentation.AbsoluteAtomicCounter;
import com.atlassian.instrumentation.AtomicCounter;
import com.atlassian.instrumentation.AtomicGauge;
import com.atlassian.instrumentation.DerivedAtomicCounter;
import com.atlassian.instrumentation.Instrument;
import com.atlassian.instrumentation.operations.OpCounter;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 */
public class TestXmlInstrumentExporter extends TestCase
{
    public void testNullInput() throws IOException
    {
        XmlInstrumentExporter exporter = new XmlInstrumentExporter();
        try
        {
            exporter.export(null, new StringWriter());
            fail("argument barf expected");
        }
        catch (IllegalArgumentException expected)
        {
        }
        try
        {
            exporter.export(buildList(), null);
            fail("argument barf expected");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public void testMimeType()
    {
        InstrumentExporter exporter = new XmlInstrumentExporter();
        assertEquals("text/xml", exporter.getMimeType());
    }

    public void testEmptyList() throws IOException
    {
        List<Instrument> instruments = buildList();
        String actual = export(instruments);
        assertEquals("<instruments></instruments>", actual);
    }

    public void testNullEntries() throws IOException
    {
        List<Instrument> instruments = buildList(null, null);
        String actual = export(instruments);
        assertEquals("<instruments></instruments>", actual);
    }

    public void testBasicExport() throws IOException
    {

        List<Instrument> instruments = buildList(new OpCounter("name1", 333, 666, 999, 1212));
        String actual = export(instruments);
        assertEquals("<instruments><instrument name=\"name1\" value=\"666\" type=\"opInstrument\" count=\"333\" time=\"666\" size=\"999\" cpu=\"1212\"/></instruments>", actual);
    }

    public void testMoreTypes() throws IOException
    {
        List<Instrument> instruments = buildList(
                new AtomicGauge("ag1", 123),
                new AtomicCounter("ac1", 456),
                new AbsoluteAtomicCounter("aac1", 666),
                new DerivedAtomicCounter("dac1", 789)
        );

        String actual = export(instruments);

        assertEquals("<instruments>"
                + "<instrument name=\"ag1\" value=\"123\" type=\"gauge\"/>"
                + "<instrument name=\"ac1\" value=\"456\" type=\"counter\"/>"
                + "<instrument name=\"aac1\" value=\"666\" type=\"absoluteCounter\"/>"
                + "<instrument name=\"dac1\" value=\"789\" type=\"derivedCounter\"/>"
                + "</instruments>", actual);
    }


    private String export(final List<Instrument> instruments) throws IOException
    {
        StringWriter sw = new StringWriter();


        XmlInstrumentExporter exporter = new XmlInstrumentExporter();
        exporter.export(instruments, sw);
        return sw.toString();
    }

    private List<Instrument> buildList(Instrument... instruments)
    {
        return Arrays.asList(instruments);
    }
}
