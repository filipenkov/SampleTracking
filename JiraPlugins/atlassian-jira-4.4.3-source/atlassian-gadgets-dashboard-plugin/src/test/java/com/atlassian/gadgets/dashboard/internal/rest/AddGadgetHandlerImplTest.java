package com.atlassian.gadgets.dashboard.internal.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.GadgetSpecUrlChecker;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.DashboardUrlBuilder;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.GadgetFactory;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.gadgets.dashboard.internal.UserPref;
import com.atlassian.gadgets.dashboard.internal.rest.representations.GadgetRepresentation;
import com.atlassian.gadgets.dashboard.internal.rest.representations.RepresentationFactory;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddGadgetHandlerImplTest
{
    @Mock private DashboardRepository repository;
    @Mock private GadgetFactory gadgetFactory;
    @Mock private DashboardUrlBuilder urlBuilder;
    @Mock private GadgetSpecUrlChecker gadgetUrlChecker;
    @Mock private RepresentationFactory representationFactory;
    @Mock private I18nResolver i18n;
    @Mock private GadgetRepresentation gadgetRepresentation;
    @Mock private EventPublisher eventPublisher;
    private TransactionTemplate txTemplate = new TransactionTemplate()
    {
        public Object execute(TransactionCallback action)
        {
            return action.doInTransaction();
        }
    };
    private GadgetRequestContext gadgetRequestContext;

    AddGadgetHandler handler;

    @Mock HttpServletRequest request;

    private static final String TEST_DASHBOARD_ID = "100";
    private static final String TEST_DASHBOARD2_ID = "101";
    private static final String TEST_GADGET_ID = "1000";
    private static final String MONKEY_GADGET_SPEC_URI = "http://www.example.com/monkey.xml";
    private static final String MONKEY_GADGET_SPEC_FILE_URI = "file:///home/kkong/monkey.xml";
    private static final String GADGET_RESOURCE_URI = "http://www.example.com/rest/dashboards/1.0/1/gadgets/"
            + TEST_GADGET_ID;

    private final DashboardId dashboardId = DashboardId.valueOf(TEST_DASHBOARD_ID);
    private final DashboardId dashboard2Id = DashboardId.valueOf(TEST_DASHBOARD2_ID);
    @Mock private Dashboard dashboard;
    @Mock private Dashboard dashboard2;
    private final GadgetId gadgetId = GadgetId.valueOf(TEST_GADGET_ID);
    @Mock private Gadget gadget;
    @Mock private Gadget gadget1;
    @Mock private Gadget gadget2;

    @Before
    public void setUp()
    {
        gadgetRequestContext = gadgetRequestContext().build();
        handler = new AddGadgetHandlerImpl(gadgetUrlChecker, gadgetFactory, repository, representationFactory, i18n, txTemplate, eventPublisher);
        when(dashboard.getId()).thenReturn(dashboardId);
        when(gadget.getId()).thenReturn(gadgetId);
        when(gadget.getUserPrefs()).thenReturn(ImmutableList.<UserPref>of());
        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        when(gadgetRepresentation.getGadgetUrl()).thenReturn("http://www.example.com/rest/dashboards/1.0/1/gadgets/1000");
        when(representationFactory.createGadgetRepresentation(eq(dashboardId), eq(gadget), isA(GadgetRequestContext.class), eq(true), eq(DashboardState.ColumnIndex.ZERO))).thenReturn(gadgetRepresentation);
    }

    @Test
    public void assertThatCreatedCodeAndLocationHeaderAreSetOnSuccess() throws Exception
    {
        when(gadgetFactory.createGadget(eq(MONKEY_GADGET_SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(gadget);
        when(urlBuilder.buildGadgetUrl(dashboardId, gadgetId)).thenReturn(GADGET_RESOURCE_URI);

        Response response = handler.addGadget(dashboardId, gadgetRequestContext, MONKEY_GADGET_SPEC_URI);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard).addGadget(DashboardState.ColumnIndex.ZERO, gadget);
        verify(repository).save(dashboard);
        assertThat(response.getStatus(), is(equalTo(Response.Status.CREATED.getStatusCode())));
        assertThat(response.getMetadata().getFirst("Location").toString(), is(equalTo(GADGET_RESOURCE_URI)));
        verifyNoMoreInteractions(repository);
    }

    @Test(expected=DashboardNotFoundException.class)
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatHandlerReturnsImmediatelyIfDashboardNotFound() throws Exception
    {
        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenThrow(new DashboardNotFoundException(dashboardId));

        Response response = null;
        try
        {
            response = handler.addGadget(dashboardId, gadgetRequestContext, "");
        }
        finally
        {
            assertThat(response, is(nullValue()));
            verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatInvalidGadgetSpecUriCausesBadRequestResponse() throws Exception
    {
        String invalidGadgetSpecUri = "a monkey doing stuff";

        doThrow(new GadgetSpecUriNotAllowedException("")).when(gadgetUrlChecker).assertRenderable(invalidGadgetSpecUri);

        Response response = handler.addGadget(dashboardId, gadgetRequestContext, invalidGadgetSpecUri);

        verifyZeroInteractions(repository);
        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatNonHttpOrHttpsGadgetSpecUriCausesBadRequest() throws Exception
    {
        doThrow(new GadgetSpecUriNotAllowedException("")).when(gadgetUrlChecker).
                assertRenderable(MONKEY_GADGET_SPEC_FILE_URI);

        Response response = handler.addGadget(dashboardId, gadgetRequestContext, MONKEY_GADGET_SPEC_FILE_URI);

        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
        verifyZeroInteractions(repository);
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatBadRequestResponseIsSentIfGadgetFactoryCannotParseGadgetSpec() throws Exception
    {
        when(gadgetFactory.createGadget(eq(MONKEY_GADGET_SPEC_URI), isA(GadgetRequestContext.class))).
                thenThrow(new GadgetParsingException("Your gadget spec is broken"));

        Response response = handler.addGadget(dashboardId, gadgetRequestContext, MONKEY_GADGET_SPEC_URI);

        verifyZeroInteractions(repository);
        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatInconsistentStateExceptionReturnsConflict()
    {
        when(gadgetFactory.createGadget(eq(MONKEY_GADGET_SPEC_URI),
                isA(GadgetRequestContext.class))).thenReturn(gadget);
        when(urlBuilder.buildGadgetUrl(dashboardId, gadgetId)).thenReturn(GADGET_RESOURCE_URI);

        doThrow(new InconsistentDashboardStateException("")).when(repository).save(dashboard);

        Response response = handler.addGadget(dashboardId, gadgetRequestContext, MONKEY_GADGET_SPEC_URI);

        assertThat(response.getStatus(), is(equalTo(Response.Status.CONFLICT.getStatusCode())));
    }
    
    @Test
    public void assertGadgetIsMovedSuccessfully()
    {
    	InOrder inOrder = inOrder(repository);
        final GadgetId gadget1Id = GadgetId.valueOf("gadget1");
        when(gadget1.getId()).thenReturn(gadget1Id);
        final GadgetId gadget2Id = GadgetId.valueOf("gadget2");
        when(gadget2.getId()).thenReturn(gadget2Id);

        when(repository.get(eq(dashboard2Id), isA(GadgetRequestContext.class))).thenReturn(dashboard2);
        when(dashboard.findGadget(gadgetId)).thenReturn(gadget);
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO)).thenReturn(newArrayList(gadget));
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ONE)).thenReturn(Collections.<Gadget>emptyList());
        when(dashboard2.getLayout()).thenReturn(Layout.AA);
        when(dashboard2.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO)).thenReturn(newArrayList(gadget1, gadget2));
        when(dashboard2.getGadgetsInColumn(DashboardState.ColumnIndex.ONE)).thenReturn(Collections.<Gadget>emptyList());

        when(representationFactory.createGadgetRepresentation(eq(dashboard2Id), eq(gadget), isA(GadgetRequestContext.class), eq(true), eq(DashboardState.ColumnIndex.ZERO))).thenReturn(gadgetRepresentation);

        final Response response = handler.moveGadget(dashboard2Id, gadgetId, dashboardId, DashboardState.ColumnIndex.ZERO, 1, gadgetRequestContext);

        List<List<GadgetId>> newLayout = new ArrayList<List<GadgetId>>();
        newLayout.add(newArrayList(gadget1Id, gadgetId, gadget2Id));
        newLayout.add(Collections.<GadgetId>emptyList());
        final GadgetLayout layout = new GadgetLayout(newLayout);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(repository).get(eq(dashboard2Id), isA(GadgetRequestContext.class));
        verify(dashboard).findGadget(gadgetId);
        verify(dashboard).removeGadget(gadgetId);
        verify(dashboard2).addGadget(gadget);
        verify(dashboard2).rearrangeGadgets(argThat(is(equalTo(layout))));
        inOrder.verify(repository).save(dashboard);
        inOrder.verify(repository).save(dashboard2);
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        assertThat(response.getMetadata().getFirst("Location").toString(), is(equalTo(GADGET_RESOURCE_URI)));
        verifyNoMoreInteractions(repository);
    }
    
    @Test
    public void assertThatBadResponseIsSentIfGadgetDoesntExist()
    {
        when(repository.get(eq(dashboard2Id), isA(GadgetRequestContext.class))).thenReturn(dashboard2);
        when(dashboard.getLayout()).thenReturn(Layout.AA);
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO)).thenReturn(Collections.<Gadget>emptyList());
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ONE)).thenReturn(Collections.<Gadget>emptyList());

        final Response response = handler.moveGadget(dashboard2Id, gadgetId, dashboardId, DashboardState.ColumnIndex.ZERO, 1, gadgetRequestContext);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(repository).get(eq(dashboard2Id), isA(GadgetRequestContext.class));
        assertThat(response.getStatus(), is(equalTo(Response.Status.CONFLICT.getStatusCode())));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void assertThatGadgetIsMovedCorrectlyWithLargeRowIndex()
    {
        final GadgetId gadget1Id = GadgetId.valueOf("gadget1");
        when(gadget1.getId()).thenReturn(gadget1Id);
        final GadgetId gadget2Id = GadgetId.valueOf("gadget2");
        when(gadget2.getId()).thenReturn(gadget2Id);

        when(repository.get(eq(dashboard2Id), isA(GadgetRequestContext.class))).thenReturn(dashboard2);
        when(dashboard.findGadget(gadgetId)).thenReturn(gadget);
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO)).thenReturn(newArrayList(gadget));
        when(dashboard.getGadgetsInColumn(DashboardState.ColumnIndex.ONE)).thenReturn(Collections.<Gadget>emptyList());
        when(dashboard2.getLayout()).thenReturn(Layout.AA);
        when(dashboard2.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO)).thenReturn(Collections.<Gadget>emptyList());
        when(dashboard2.getGadgetsInColumn(DashboardState.ColumnIndex.ONE)).thenReturn(newArrayList(gadget1, gadget2));

        when(representationFactory.createGadgetRepresentation(eq(dashboard2Id), eq(gadget), isA(GadgetRequestContext.class), eq(true), eq(DashboardState.ColumnIndex.ONE))).thenReturn(gadgetRepresentation);

        //there's no gadgets on the target dashboard yet.  adding with a row index that's too large should still work.
        final Response response = handler.moveGadget(dashboard2Id, gadgetId, dashboardId, DashboardState.ColumnIndex.ONE, 14, gadgetRequestContext);

        List<List<GadgetId>> newLayout = new ArrayList<List<GadgetId>>();
        newLayout.add(Collections.<GadgetId>emptyList());
        newLayout.add(newArrayList(gadget1Id, gadget2Id, gadgetId));
        final GadgetLayout layout = new GadgetLayout(newLayout);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(repository).get(eq(dashboard2Id), isA(GadgetRequestContext.class));
        verify(dashboard).findGadget(gadgetId);
        verify(dashboard2).addGadget(gadget);
        verify(dashboard2).rearrangeGadgets(argThat(is(equalTo(layout))));
        verify(dashboard).removeGadget(gadgetId);
        verify(repository).save(dashboard);
        verify(repository).save(dashboard2);
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        assertThat(response.getMetadata().getFirst("Location").toString(), is(equalTo(GADGET_RESOURCE_URI)));
        verifyNoMoreInteractions(repository);
    }


    private static <T> Matcher<? super T> equalTo(T actual)
    {
        return org.hamcrest.Matchers.equalTo(actual);
    }

    private static Matcher<GadgetLayout> equalTo(GadgetLayout layout)
    {
        return new GadgetLayoutIsEqual(layout);
    }

    private static class GadgetLayoutIsEqual extends BaseMatcher<GadgetLayout>
    {
        private final GadgetLayout actualLayout;

        public GadgetLayoutIsEqual(GadgetLayout actualLayout)
        {
            this.actualLayout = actualLayout;
        }

        public boolean matches(Object o)
        {
            if (o == actualLayout)
            {
                return true;
            }
            if (o == null)
            {
                return false;
            }
            if (!(o instanceof GadgetLayout))
            {
                return false;
            }
            GadgetLayout expectedLayout = (GadgetLayout) o;
            if (actualLayout.getNumberOfColumns() != expectedLayout.getNumberOfColumns())
            {
                return false;
            }
            for (int i = 0; i < actualLayout.getNumberOfColumns(); i++)
            {
                if (!Iterables.elementsEqual(actualLayout.getGadgetsInColumn(i), expectedLayout.getGadgetsInColumn(i)))
                {
                    return false;
                }
            }
            return true;
        }

        public void describeTo(Description description)
        {
            description.appendText("equal to AddGadgetChange(numberOfColumns=");
            description.appendValue(actualLayout.getNumberOfColumns());
            description.appendText(", columns={");
            for (int i = 0; i < actualLayout.getNumberOfColumns(); ++i)
            {
                description.appendText("[");
                description.appendValue(i);
                description.appendText("] = ");

                description.appendValue(Iterables.toString(actualLayout.getGadgetsInColumn(i)));

                if (i != actualLayout.getNumberOfColumns() - 1)
                {
                    description.appendText(", ");
                }
            }
            description.appendText("})");
        }
    }
}
