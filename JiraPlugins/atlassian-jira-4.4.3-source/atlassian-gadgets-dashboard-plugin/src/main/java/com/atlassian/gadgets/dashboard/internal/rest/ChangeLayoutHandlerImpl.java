package com.atlassian.gadgets.dashboard.internal.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.GadgetLayoutException;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.sal.api.message.I18nResolver;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Default implementation.
 */
public class ChangeLayoutHandlerImpl implements ChangeLayoutHandler
{
    private final Log log = LogFactory.getLog(ChangeLayoutHandlerImpl.class);

    private final DashboardRepository repository;
    private final I18nResolver i18n;
    private final EventPublisher eventPublisher;

    public ChangeLayoutHandlerImpl(DashboardRepository repository, I18nResolver i18n, final EventPublisher eventPublisher)
    {
        this.repository = repository;
        this.i18n = i18n;
        this.eventPublisher = eventPublisher;
    }

    public Response changeLayout(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext,
                                 JSONObject newLayout)
    {
        String layout = newLayout.optString("layout");
        Dashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
        try
        {
            eventPublisher.publish(new LayoutChangedEvent(gadgetRequestContext.getViewer(), dashboardId, layout));
            if (StringUtils.isBlank(layout))
            {
                return rearrangeGadgets(dashboard, newLayout);
            }
            else
            {
                return persistLayout(dashboard, newLayout);
            }
        }
        catch (GadgetLayoutException e)
        {
            return Response.status(Response.Status.BAD_REQUEST).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.changing.dashboard.layout", e.getMessage())).
                    build();
        }
        catch (ParseGadgetLayoutException e)
        {
            return Response.status(Response.Status.BAD_REQUEST).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.changing.dashboard.layout", e.getMessage())).
                    build();
        }
        catch (IOException ioe)
        {
            return Response.serverError().
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.changing.dashboard.layout", ioe.getMessage())).
                    build();
        }
        catch (InconsistentDashboardStateException idse)
        {
            log.error("ChangeLayoutHandlerImpl: Unexpected error occurred", idse);
            return Response.status(Response.Status.CONFLICT).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.please.reload")).
                    build();
        }
    }

    private Response rearrangeGadgets(Dashboard dashboard, JSONObject newLayout)
    {
        GadgetLayout gadgetLayout = parseGadgetLayout(dashboard.getLayout(), newLayout);
        dashboard.rearrangeGadgets(gadgetLayout);
        repository.save(dashboard);
        return Response.noContent().build();
    }

    private Response persistLayout(Dashboard dashboard, JSONObject newLayout)
            throws IOException
    {
        Layout layout;
        String layoutParam = null;
        try
        {
            layoutParam = newLayout.getString("layout");
            layout = Layout.valueOf(layoutParam);
        }
        catch (IllegalArgumentException e)
        {
            return invalidLayout(layoutParam);
        }
        catch (JSONException e)
        {
            return invalidLayout(layoutParam);
        }
        GadgetLayout gadgetLayout = parseGadgetLayout(layout, newLayout);
        dashboard.changeLayout(layout, gadgetLayout);
        repository.save(dashboard);
        return Response.noContent().build();
    }

    private Response invalidLayout(String layoutParam)
    {
        return Response.status(Response.Status.BAD_REQUEST).
            type(MediaType.TEXT_PLAIN).
            entity(i18n.getText("invalid.layout.parameter", layoutParam, Layout.values())).
            build();
    }

    private GadgetLayout parseGadgetLayout(Layout layout, JSONObject newLayout)
            throws ParseGadgetLayoutException
    {
        List<Iterable<GadgetId>> columns = new ArrayList<Iterable<GadgetId>>(layout.getNumberOfColumns());
        columns.addAll(Collections.<Iterable<GadgetId>>nCopies(layout.getNumberOfColumns(), Collections.<GadgetId>emptyList()));
        for (int i = 0; i < columns.size(); i++)
        {
            try
            {
                JSONArray gadgetIds = newLayout.optJSONArray(Integer.toString(i));
                if (gadgetIds == null)
                {
                    // there are no gadgets in this column
                    continue;
                }
                columns.set(i, toGadgetIds(gadgetIds));
            }
            catch (NumberFormatException e)
            {
                throw new ParseGadgetLayoutException("gadget ids must be integers");
            }
            catch (JSONException e)
            {
                throw new ParseGadgetLayoutException("could not parse layout JSON: " + newLayout);
            }
        }
        return new GadgetLayout(columns);
    }

    private List<GadgetId> toGadgetIds(JSONArray gadgetIds) throws NumberFormatException, JSONException
    {
        List<GadgetId> columnLayout = new LinkedList<GadgetId>();
        for (int i = 0; i < gadgetIds.length(); i++)
        {
            columnLayout.add(GadgetId.valueOf(gadgetIds.getString(i)));
        }
        return columnLayout;
    }

    private final class ParseGadgetLayoutException extends RuntimeException
    {
        public ParseGadgetLayoutException(String message)
        {
            super(message);
        }
    }

    public static final class LayoutChangedEvent
    {
        public final DashboardId dashboardId;
        public final String user;
        public final String layout;

        public LayoutChangedEvent(final String user, final DashboardId dashboardId, final String layout)
        {
            this.user = user;
            this.dashboardId = dashboardId;
            this.layout = layout;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
