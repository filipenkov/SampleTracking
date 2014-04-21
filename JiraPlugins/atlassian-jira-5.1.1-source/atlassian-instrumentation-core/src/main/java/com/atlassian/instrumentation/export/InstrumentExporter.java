package com.atlassian.instrumentation.export;

import com.atlassian.instrumentation.Instrument;

import java.io.Writer;
import java.io.IOException;
import java.util.List;

/**
 * InstrumentExporter is able to export a list of {@link com.atlassian.instrumentation.Instrument}s to a specific file
 * format, such as XML or JSON.
 *
 * @since v4.0
 */
public interface InstrumentExporter
{
    /**
     * Called to export the list of {@link com.atlassian.instrumentation.Instrument}s into a specific format
     * 
     * @param instruments the list of {@link com.atlassian.instrumentation.Instrument}s
     * @param writer the writer to export the list to
     * @throws IOException if things go badly in the IO wold
     */
    void export(List<Instrument> instruments, Writer writer) throws IOException;

    /**
     * Converts a single {@link Instrument} to a string representation
     *
     * @param instrument an {@link Instrument} to export
     * @return A string representation of a single instrument as determined by the exporter implementation
     */
    String export(Instrument instrument);

    /**
     * Returns the mime type for the export
     * @return the mime type for the export
     */
    String getMimeType();
}
