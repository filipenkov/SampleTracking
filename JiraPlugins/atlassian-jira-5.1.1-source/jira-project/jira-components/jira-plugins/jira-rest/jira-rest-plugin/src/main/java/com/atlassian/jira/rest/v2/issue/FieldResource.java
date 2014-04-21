package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 5.0
 */
@Path ("field")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class FieldResource
{
    private static final Logger LOG = Logger.getLogger(FieldResource.class);

    private final ContextI18n i18n;
    private final JiraBaseUrls baseUrls;
    private final FieldManager fieldManager;
    private final JiraAuthenticationContext authenticationContext;

    public FieldResource(ContextI18n i18n, JiraBaseUrls baseUrls, FieldManager fieldManager, JiraAuthenticationContext authenticationContext)
    {
        this.baseUrls = baseUrls;
        this.i18n = i18n;
        this.fieldManager = fieldManager;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Returns a list of all fields, both System and Custom
     *
     * @return a response containing all fields as short Field Meta Beans
     *
     * @response.representation.200.qname
     *      List of field
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Contains a full representation of all visible fields in JSON.
     *
     * @response.representation.200.example
     *      {@link FieldBean#DOC_EXAMPLE_LIST}
     *
     */
    @GET
    public Response getFields()
    {
        Set<Field> fields = new HashSet<Field>();
        Set<OrderableField> orderableFields = fieldManager.getOrderableFields();
        for (OrderableField orderableField : orderableFields)
        {
            // We only add the non-navigable fields here.  We get the navigable ones next, but only if the user can see them.
            if (!(orderableField instanceof NavigableField))
            {
                fields.add(orderableField);
            }
        }
        
        try
        {
            fields.addAll(fieldManager.getAvailableNavigableFields(authenticationContext.getLoggedInUser()));
        }
        catch (FieldException e)
        {
            throw new RESTException(Response.Status.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }

        FieldMetaBean.newBean();
        return Response.ok(FieldBean.shortBeans(fields, fieldManager)).build();
    }
}