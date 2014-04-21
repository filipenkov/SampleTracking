package com.atlassian.jira.plugin.issuenav.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * REST resource for the issue navigator.
 *
 * @since v5.1
 */
@AnonymousAllowed
@Path ("spud")
public class SpudSpikeResource
{
    private final SearchProvider sp;
    private final SearchService searchService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private SearchProviderFactory searchProviderFactory;

    public SpudSpikeResource(SearchProvider sp, SearchService searchService, JiraAuthenticationContext jiraAuthenticationContext, SearchProviderFactory searchProviderFactory) {
        this.sp = sp;
        this.searchService = searchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchProviderFactory = searchProviderFactory;
    }

    @GET
    @Produces ({ "text/plain" })
    public Response search(@QueryParam("jql") String jql) throws SearchException {
        final Timer t = new Timer();
        
        User user = jiraAuthenticationContext.getLoggedInUser();
        SearchService.ParseResult parseResult = searchService.parseQuery(user, jql == null ? "" : jql);
        
        if (!parseResult.isValid())
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(parseResult.getErrors().getErrorMessages()));
        }

        final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        // TODO:
        // - we actually know totalHits BEFORe collector.collect() is called (we should refactor with a beatter SearchResultsListener)
        final ArrayList<String> issueIds = new ArrayList<String>();
        
        Collector collector = new Collector() {
            FieldSelector fields = new MapFieldSelector(DocumentConstants.ISSUE_ID);

            boolean first = true;
            @Override
            public void setScorer(Scorer scorer) throws IOException {
                throw new UnsupportedOperationException("Not implemented");// theoretically not called in our case
            }

            @Override
            public void collect(int doc) throws IOException {
                if (first) {
                    first = false;
                    t.mark("collected first doc");
                }
                String issueId = searcher.doc(doc, fields).get(DocumentConstants.ISSUE_ID);
                issueIds.add(issueId);
            }

            @Override
            public void setNextReader(IndexReader reader, int docBase) throws IOException {
                throw new UnsupportedOperationException("Not implemented"); // theoretically not called in our case
            }

            @Override
            public boolean acceptsDocsOutOfOrder() {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

        t.mark("parse query");
        
        PagerFilter pf = new PagerFilter(searcher.maxDoc());
        t.mark("start search, sort and collect");
        sp.searchAndSort(parseResult.getQuery(), user, collector, pf);
        t.mark("search, sort and collect done");
        
        final StreamingOutput out = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), false);
                t.mark("start streaming");
                out.printf("total: %d\n", issueIds.size());
                t.mark("sent total");
                int step = 1000;
                for (int i = 0; i < issueIds.size(); i++) {
                    if ((i%step) == 0) {
                        out.flush();
                        t.mark("sent block");
                        out.printf("\nids: ");
                    }
                    out.printf(" %s", issueIds.get(i));
                }
                out.flush();
                t.mark("sent all");
                t.dump(out);
                out.flush();
            }
        };

        return Response.ok(out).cacheControl(never()).build();
    }

    static class Timer {
        
        static class Info {
            final String msg;
            final long elapsed;
            final long last;

            Info(String msg, long elapsed, long last) {
                this.msg = msg;
                this.elapsed = elapsed;
                this.last = last;
            }
        }
        
        final long t0 = System.nanoTime();
        long tlast = t0;
        List<Info> infos = new LinkedList<Info>();
                
        public void mark(String s) {
            long t1 = System.nanoTime();
            infos.add(new Info(s, t1 - t0, t1 - tlast));
            tlast = t1;
        }

        public void dump(PrintWriter out) {
            out.printf("\nTiming:\n");
            for (Info info : infos) {
                out.printf("%10.3fs %10.3fs %s\n", info.last/1000000000.0, info.elapsed/1000000000.0, info.msg);
            }
        }
    }
}
