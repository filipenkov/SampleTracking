package com.atlassian.jira.pageobjects.util;

import com.atlassian.pageobjects.elements.query.AbstractTimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Provides utility methods for waiting for ajax results. To send events from the javascript side, call JIRA.trace(key, args...).
 * To turn on logging: <pre>org.apache.log4j.Logger.getLogger(TraceContext.class).setLevel(org.apache.log4j.Level.DEBUG);</pre>
 */
public class TraceContext
{
    private static final Logger log = Logger.getLogger(TraceContext.class);

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private Timeouts timeouts;

    private static List<TraceEntry> allTraces = Collections.synchronizedList(new LinkedList<TraceEntry>());

    /**
     * Returns a tracer containing the current state of trace list.
     */
    public Tracer checkpoint()
    {
        try
        {
            // Attempt to retrieve all traces before snapshotting the checkpoint.
            retrieveTraces();
        }
        catch (WebDriverException ex)
        {
            // Ignore - this fails when the WebDriver browser has not yet loaded, eg if we are taking a checkpoint
            // before the first page load to wait for an ajax request that is fired from document.onready
        }
        int checkpoint = allTraces.size();
        log.debug("Returned checkpoint at position " + checkpoint);
        return new Tracer(checkpoint);
    }

    /**
     * Waits for the occurrence of a trace with the given key after the given tracer.
     * @param tracer checkpoint. Only traces after this checkpoint will be inspected.
     * @param key tracer key to watch for
     */
    public void waitFor(final Tracer tracer, final String key)
    {
        log.debug("Waiting for key " + key + " from position " + tracer.position);
        waitUntilTrue(new AbstractTimedCondition(timeouts.timeoutFor(TimeoutType.AJAX_ACTION), timeouts.timeoutFor(TimeoutType.EVALUATION_INTERVAL))
        {
            @Override
            protected Boolean currentValue()
            {
                retrieveTraces();
                for (int i = tracer.position; i < allTraces.size(); ++i)
                {
                    if (allTraces.get(i).id.equals(key))
                    {
                        log.debug("Matched tracer key " + key + " at position " + i);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void retrieveTraces()
    {
        int sizeBefore = allTraces.size();
        List<Map<String, Object>> traces = (List<Map<String, Object>>) driver.executeScript("return JIRA.trace.drain();");
        List<TraceEntry> traceEntries = convert(traces);
        allTraces.addAll(traceEntries);
        for (int i = sizeBefore; i < allTraces.size(); ++i)
        {
            log.debug("Retrieved trace entry " + allTraces.get(i).id + " at position " + i);
        }
    }

    private List<TraceEntry> convert(List<Map<String, Object>> traces)
    {
        return Lists.transform(traces, new Function<Map<String, Object>, TraceEntry>() {

            @Override
            public TraceEntry apply(@Nullable Map<String, Object> traceEntry)
            {
                return new TraceEntry(traceEntry);
            }
        });
    }

    private static class TraceEntry
    {
        public final String id;
        public final Object timestamp;
        public final List<Map<String, Object>> args;

        TraceEntry(Map<String, Object> entry)
        {
            this.id = (String) entry.get("id");
            this.timestamp = entry.get("ts");
            this.args = (List<Map<String, Object>>) entry.get("args");
        }
    }
}
