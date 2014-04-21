package com.atlassian.jira.collector.plugin.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorActivityHelper;
import com.atlassian.jira.collector.plugin.components.CollectorFieldValidatorImpl;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.collector.plugin.components.ErrorLog;
import com.atlassian.jira.collector.plugin.components.IssueCollectorEventDispatcher;
import com.atlassian.jira.collector.plugin.components.Template;
import com.atlassian.jira.collector.plugin.components.TemplateStore;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueUtils;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.config.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.collector.plugin.components.ErrorLog.ErrorType;

@Path ("template")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.TEXT_HTML })
@AnonymousAllowed
public class TemplateResource
{
    private static final Logger log = Logger.getLogger(TemplateResource.class);
    private static final String CUSTOM_TEMPLATE_ID = "custom";

    private static final Pattern URL_PATTERN = Pattern.compile("((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]*)", Pattern.CASE_INSENSITIVE);
    private static final String RESP_PREFIX = "<html><body><textarea>";
    private static final String RESP_POSTFIX = "</textarea></body></html>";
	private static final int MAX_SUMMARY_LENGTH = 50;
	public static final String LOCATION_COOKIE_NAME = "location_name";
	private final CollectorService collectorService;
    private final JiraAuthenticationContext authenticationContext;
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final IssueService issueService;
    private final UserManager userManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final TemplateStore templateStore;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final AttachmentManager attachmentManager;
    private final XsrfTokenGenerator xsrfTokenGenerator;
    private final ErrorLog errorLog;
    private final RendererManager rendererManager;
    private final ApplicationProperties applicationProperties;
	private final IssueFactory issueFactory;
	private final FieldScreenRendererFactory fieldScreenRendererFactory;
	private final CollectorFieldValidatorImpl collectorFieldValidator;

    @Context
    private HttpServletRequest request;
	private final IssueCollectorEventDispatcher eventDispatcher;

	public TemplateResource(final CollectorService collectorService, final JiraAuthenticationContext authenticationContext,
			final TemplateRenderer templateRenderer, final WebResourceManager webResourceManager, final IssueService issueService,
			final UserManager userManager, final VelocityRequestContextFactory velocityRequestContextFactory,
			final TemplateStore templateStore, final PermissionManager permissionManager, final ProjectManager projectManager,
			final AttachmentManager attachmentManager, final XsrfTokenGenerator xsrfTokenGenerator, final ErrorLog errorLog,
			final RendererManager rendererManager, final ApplicationProperties applicationProperties, final IssueFactory issueFactory,
			final FieldScreenRendererFactory fieldScreenRendererFactory, final CollectorFieldValidatorImpl collectorFieldValidator,
			final IssueCollectorEventDispatcher eventDispatcher)
    {
        this.collectorService = collectorService;
        this.authenticationContext = authenticationContext;
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.issueService = issueService;
        this.userManager = userManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.templateStore = templateStore;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.attachmentManager = attachmentManager;
        this.xsrfTokenGenerator = xsrfTokenGenerator;
        this.errorLog = errorLog;
        this.rendererManager = rendererManager;
        this.applicationProperties = applicationProperties;
		this.issueFactory = issueFactory;
		this.fieldScreenRendererFactory = fieldScreenRendererFactory;
		this.collectorFieldValidator = collectorFieldValidator;
		this.eventDispatcher = eventDispatcher;
	}

    @GET
    @Path ("{templateId}")
    public Response renderTemplate(@PathParam ("templateId") String templateId, @QueryParam("preview") Boolean isPreview)
    {
        final Template template = templateStore.getTemplate(templateId);
        if (template == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(CacheControl.NO_CACHE).build();
        }
        final HashMap<String, Object> context = new HashMap<String, Object>();
        context.put("showContactForm", true);

        if (CUSTOM_TEMPLATE_ID.equals(templateId)) {
            context.put("contactFormEditable", true);
        }
		context.put("preview",isPreview);
		context.put("attachmentsEnabled",applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS));
        return Response.ok(renderTemplate(template, context, false)).cacheControl(CacheControl.NO_CACHE).build();
    }

    @GET
    @Path ("form/{collectorId}")
    public Response getForm(@PathParam ("collectorId") String collectorId, @QueryParam("preview") Boolean isPreview)
    {
        final ServiceOutcome<Collector> result = collectorService.getCollector(collectorId);
        final Collector collector = result.getReturnedValue();
        if (collector == null || !collector.isEnabled())
        {
            reportCollectorError(collector, collectorId, null, null, null);
            return createErrorResponse(collector == null ? "collector.plugin.collector.not.found" : "collector.plugin.collector.disabled.msg");
        }

        //clear previous entries first!
        TemporaryAttachmentsMonitorLocator.getAttachmentsMonitor(request, collector.getId()).clearEntriesForIssue(TemporaryAttachmentsResource.UNKNOWN_ISSUE_ID);
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("collector", collector);
        boolean showContactForm = !hasCreateIssuePermission(collector, authenticationContext.getLoggedInUser());
        context.put("preview",isPreview);
		context.put("showContactForm", showContactForm);
        if (StringUtils.isNotBlank(collector.getCustomMessage()))
        {
            context.put("showCustomMessage", true);
            context.put("customMessageHtml", rendererManager.getRenderedContent(AtlassianWikiRenderer.RENDERER_TYPE, collector.getCustomMessage(), null));
        }
        if (collector.getTemplate().getId().equals(CUSTOM_TEMPLATE_ID))
        {
            context.put("fields", new JSONArray(collector.getCustomTemplateFields()).toString());
            context.put("customLabels", collector.getCustomTemplateLabels());
            context.put("issueType", collector.getIssueTypeId());
            context.put("projectKey", projectManager.getProjectObj(collector.getProjectId()).getKey());
            context.put("atl_token", xsrfTokenGenerator.generateToken());
        }

        if (!showContactForm)
        {
            context.put("user", authenticationContext.getLoggedInUser());
        }

        context.put("attachmentsEnabled",applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS));
        return Response.ok(renderTemplate(collector.getTemplate(), context, true)).cacheControl(CacheControl.NO_CACHE).build();
    }

    public enum Rating
    {
        AWESOME("collector.plugin.template.awesome", ":D"),
        GOOD("collector.plugin.template.good", ":)"),
        MEH("collector.plugin.template.meh", ";)"),
        BAD("collector.plugin.template.bad", ":("),
        HORRIBLE("collector.plugin.template.horrible", "(n)");

        private final String i18nKey;
        private final String smiley;

        Rating(String i18nKey, String smiley)
        {
            this.i18nKey = i18nKey;
            this.smiley = smiley;
        }

        public String getI18nKey()
        {
            return i18nKey;
        }

        public String getSmiley()
        {
            return smiley;
        }
    }

    @POST
    @Path ("feedback/{collectorId}")
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED })
    public Response createFeedback(@PathParam ("collectorId") String collectorId,
            @FormParam ("description-good") String descriptionGood,
            @FormParam ("description-bad") String descriptionBad,
            @FormParam ("rating") String rating,
            @FormParam ("fullname") String fullname,
            @FormParam ("email") String email,
            @FormParam ("webInfo") String webInfo,
            @FormParam ("filetoconvert") List<Long> screenshotIds)
    {
        final ServiceOutcome<Collector> outcome = collectorService.getCollector(collectorId);
        final Collector collector = outcome.getReturnedValue();
        if (collector == null || !collector.isEnabled())
        {
            return reportCollectorError(collector, collectorId, fullname, email, webInfo);
        }

        //validate the rating!
        try
        {
            if (StringUtils.isNotBlank(rating))
            {
                Rating.valueOf(rating);
            }
            else
            {
                return reportCollectorError(collector, collectorId, fullname, email, webInfo);
            }
        }
        catch (IllegalArgumentException e)
        {
            return reportCollectorError(collector, collectorId, fullname, email, webInfo);
        }

        final Rating ratingValue = Rating.valueOf(rating);
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("ratingValue", ratingValue);
        context.put("descriptionGoodHtml", descriptionGood);
        context.put("descriptionBadHtml", descriptionBad);
        final String description = render("templates/feedback-wiki-markup.vm", context);

        final IssueInputParameters params = new IssueInputParametersImpl();
        params.setSummary(getSummary(descriptionGood));
        params.setDescription(description);
        return createIssue(params, collector, fullname, email, webInfo, screenshotIds);
    }

    @POST
    @Path ("form/{collectorId}")
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED })
    public Response createIssue(@PathParam ("collectorId") String collectorId,
            @FormParam ("description") String description,
            @FormParam ("fullname") String fullname,
            @FormParam ("email") String email,
            @FormParam ("webInfo") String webInfo,
            @FormParam ("filetoconvert") List<Long> screenshotIds)
    {
        final ServiceOutcome<Collector> outcome = collectorService.getCollector(collectorId);
        final Collector collector = outcome.getReturnedValue();
        if (collector == null || !collector.isEnabled())
        {
            return reportCollectorError(collector, collectorId, fullname, email, webInfo);
        }

        final IssueInputParameters params = new IssueInputParametersImpl();
        params.setSummary(getSummary(description));
        params.setDescription(description);
        return createIssue(params, collector, fullname, email, webInfo, screenshotIds);
    }

    @POST
    @Path ("custom/{collectorId}")
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED })
    public Response createCustomIssue(@PathParam ("collectorId") String collectorId,
            @FormParam ("fullname") String fullname,
            @FormParam ("email") String email,
            @FormParam ("webInfo") String webInfo,
            @FormParam ("filetoconvert") List<Long> screenshotIds)
    {
        final ServiceOutcome<Collector> outcome = collectorService.getCollector(collectorId);
        final Collector collector = outcome.getReturnedValue();
        if (collector == null || !collector.isEnabled())
        {
            return reportCollectorError(collector, collectorId, fullname, email, webInfo);
        }

        @SuppressWarnings ("unchecked")
        final Map<String, String[]> requestParams = new HashMap<String, String[]>(request.getParameterMap());
        //strip out attachments since they can cause validation errors and they are handled separately anyways.
        requestParams.remove("filetoconvert");
        final IssueInputParameters params = new IssueInputParametersImpl(requestParams);
        return createIssue(params, collector, fullname, email, webInfo, screenshotIds);
    }

    private Response createIssue(final IssueInputParameters params, final Collector collector, final String fullname, final String email, final String webInfo, final List<Long> screenshotIds)
    {
        final User remoteUser = authenticationContext.getLoggedInUser();
        final User reporter = getReporter(collector, fullname, email);
        final String env = params.getEnvironment() == null ? "" : params.getEnvironment();
        params.getActionParameters().put(IssueFieldConstants.LABELS, new String[] { CollectorActivityHelper.COLLECTOR_LABEL_PREFIX + collector.getId() });
        params.setEnvironment(env + "\n\n" + webInfo);
        params.setReporterId(reporter.getName());
        params.setAssigneeId(IssueUtils.AUTOMATIC_ASSIGNEE);
        params.setIssueTypeId(collector.getIssueTypeId().toString());
        params.setProjectId(collector.getProjectId());
        String description = params.getDescription();
        if (StringUtils.isNotBlank(fullname))
        {
            description += "\n\n" + authenticationContext.getI18nHelper().getText("collector.plugin.template.contact.name", fullname);
        }
        if (StringUtils.isNotBlank(email))
        {
            description += "\n" + authenticationContext.getI18nHelper().getText("collector.plugin.template.contact.email", email);
        }
        params.setDescription(description);

		final Map<String, String[]> requiredCustomFieldsValues = getValuesForRequiredCustomFieldsNotSetInForm(collector, params, reporter);
		for (final String customFieldId : requiredCustomFieldsValues.keySet()) {
			params.addCustomFieldValue(customFieldId, requiredCustomFieldsValues.get(customFieldId));
		}

        //this is kinda dodgy but the issue service does actually use the authentication context as well as the user
        //that's passed in :(.
        authenticationContext.setLoggedInUser(reporter);
        try
        {
            final IssueService.CreateValidationResult validationResult = issueService.validateCreate(reporter, params);
            if (validationResult.isValid())
            {
                final IssueService.IssueResult result = issueService.create(reporter, validationResult);
				eventDispatcher.issueSubmitted(collector);
                final Issue issue = result.getIssue();
                //add attachments (if there were any!)
                final TemporaryAttachmentsMonitor attachmentsMonitor = TemporaryAttachmentsMonitorLocator.getAttachmentsMonitor(request, collector.getId());
                final Collection<TemporaryAttachment> attachments = attachmentsMonitor.getByIssueId(TemporaryAttachmentsResource.UNKNOWN_ISSUE_ID);
                if (!attachments.isEmpty())
                {
                    try
                    {
                        attachmentManager.convertTemporaryAttachments(reporter, issue, screenshotIds, attachmentsMonitor);
                    }
                    catch (AttachmentException e)
                    {
                        log.error("Error attaching files", e);
                    }
                    catch (Throwable t)
                    {
                        log.error("Generic Error attaching files", t);
                    }
                }

                String feedbackLocation = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl() + "/browse/" + issue.getKey();
                //check if the user submitting this feedback can see the issue. (Could be anonymous).
                if (permissionManager.hasPermission(Permissions.BROWSE, issue, remoteUser))
                {
                    return Response.ok(RESP_PREFIX + "{\"key\":\"" + issue.getKey() + "\", \"url\":\"" + feedbackLocation + "\"}" + RESP_POSTFIX).cacheControl(CacheControl.NO_CACHE).build();
                }
                else
                {
                    return Response.ok(RESP_PREFIX + "{}" + RESP_POSTFIX).cacheControl(CacheControl.NO_CACHE).build();
                }
            }
            else
            {
                if (CUSTOM_TEMPLATE_ID.equals(collector.getTemplate().getId()))
                {
                    final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
					// In general HTTP 400 BAD_REQUEST should be returned here but due to cross domain problems
					// on IE while getting 400 response it has to be changed to 200
                    return Response.status(Response.Status.OK).entity(RESP_PREFIX +
                            marshaller.marshal(ErrorCollection.of(validationResult.getErrorCollection())) +
                            RESP_POSTFIX).cacheControl(CacheControl.NO_CACHE).build();
                }
                else
                {
                    return reportCollectorError(collector, collector.getId(), fullname, email, webInfo);
                }
            }
        }
        finally
        {
            authenticationContext.setLoggedInUser(remoteUser);
        }
    }

	private Map<String, String[]> getValuesForRequiredCustomFieldsNotSetInForm(final Collector collector, final IssueInputParameters params, final User loggedUser) {
		final MutableIssue issue = issueFactory.getIssue();
		issue.setProjectId(collector.getProjectId());
		issue.setIssueTypeId(collector.getIssueTypeId().toString());
		final Project project = projectManager.getProjectObj(collector.getProjectId());

		final FieldScreenRenderer fieldScreenRenderer = fieldScreenRendererFactory
				.getFieldScreenRenderer(loggedUser, issue, IssueOperations.CREATE_ISSUE_OPERATION, false);
		final List<FieldScreenRenderTab> fieldScreenRenderTabs = fieldScreenRenderer.getFieldScreenRenderTabs();
		final Set<String> allowedCustomFieldIds = collectorFieldValidator.getAllowedCustomFieldIds(project, issue
				.getIssueTypeObject().getId());
		final Map<String, String[]> defaultRequiredValues = Maps.newHashMap();

		for (final FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderTabs) {
			for (final FieldScreenRenderLayoutItem fsrli : fieldScreenRenderTab.getFieldScreenRenderLayoutItems()) {

				final FieldLayoutItem field = fsrli.getFieldLayoutItem();
				final String fieldId = field.getOrderableField().getId();
				final String customFieldValidationId = field.getOrderableField().getId().split(":")[0];

				if (allowedCustomFieldIds.contains(customFieldValidationId)
						&& field.isRequired()
						&& field.getOrderableField().getDefaultValue(issue) != null) {

					if (params.getCustomFieldValue(fieldId) == null) {
						final Map defaultValuesHolder = Maps.newHashMap();
						field.getOrderableField().populateDefaults(defaultValuesHolder, issue);
						defaultRequiredValues.putAll(getRequiredCustomFieldsValues(fieldId, defaultValuesHolder));
					}
				}
			}
		}
		return defaultRequiredValues;
	}

	private Map<? extends String, ? extends String[]> getRequiredCustomFieldsValues(String fieldId, Map defaultValuesHolder) {
		final Map<String, String[]> result = Maps.newHashMap();
		final Map keysAndValues = ((CustomFieldParams) (defaultValuesHolder.get(fieldId))).getKeysAndValues();
		final Collection<String> defaultParamValues = ((CustomFieldParams) (defaultValuesHolder.get(fieldId)))
				.getValuesForNullKey();

		if (keysAndValues.keySet().size() > 1) {
			for (Object o : keysAndValues.keySet()) {
				if (o != null) {
					final List values = (List) keysAndValues.get(o);
					final String value = (String) values.get(0);
					final String idPostfix = (String) o;
					result.put(fieldId + ":" + idPostfix, Iterables.toArray(ImmutableList.of(value), String.class));
				} else {
					result.put(fieldId, Iterables.toArray(defaultParamValues, String.class));
				}
			}
		} else {
			result.put(fieldId, Iterables.toArray(defaultParamValues, String.class));
		}
		return result;
	}

    private Response reportCollectorError(final Collector collector, final String collectorId, final String fullname, final String email, final String webInfo)
    {
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        if (collector == null)
        {
            status = Response.Status.NOT_FOUND;
        }
        else if (!collector.isEnabled())
        {
            status = Response.Status.FORBIDDEN;
        }

		String remoteAddress = getRemoteHost(request);

		//webinfo will most likely have a more accurate location
        if (StringUtils.isNotBlank(webInfo))
        {
            final Matcher matcher = URL_PATTERN.matcher(webInfo);
            if (matcher.find())
            {
                remoteAddress = matcher.group(0);
            }
        }

        String realName = fullname;
        String realEmail = email;
        final User loggedInUser = authenticationContext.getLoggedInUser();
        if (StringUtils.isBlank(fullname) && loggedInUser != null)
        {
            realName = loggedInUser.getDisplayName();
            realEmail = loggedInUser.getEmailAddress();
        }
        //need to lookup the collector id if the collector's been deleted!
        final Project project = projectManager.getProjectObj(collector != null ? collector.getProjectId() : collectorService.getArchivedProjectId(collectorId));
		String collectorName = collector == null ? collectorId : collector.getName();
        errorLog.logError(project, collectorName, realName, realEmail, remoteAddress, ErrorType.from(status));
        return Response.status(status).cacheControl(CacheControl.NO_CACHE).build();
    }

	private String getRemoteHost(HttpServletRequest request) {
		final String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null) {
			final String[] hosts = xForwardedFor.split(",");
			if (hosts.length > 0) {
				return hosts[0].trim();
			}
		}
		return request.getRemoteHost();
	}

	private User getReporter(final Collector collector, final String fullname, final String email)
    {
        //only use the loggedin user if they have permission to create and if no contact form was filled in!
        final User loggedInUser = authenticationContext.getLoggedInUser();
        if (hasCreateIssuePermission(collector, loggedInUser) && StringUtils.isBlank(fullname) && StringUtils.isBlank(email))
        {
            return loggedInUser;
        }
        else if (StringUtils.isNotBlank(email) && collector.isUseCredentials())
        {
            //if an e-mail address was provided and the collector specifies to use credentials then lets try to find
            //the user by e-mail
            for (final User user : userManager.getUsers())
            {
                //if there's a user with an match for the lowercaase e-mail address then set that person as the reporter.
                if (StringUtils.equalsIgnoreCase(user.getEmailAddress(), email) && hasCreateIssuePermission(collector, user))
                {
                    return user;
                }
            }
        }

        return userManager.getUserObject(collector.getReporter());
    }

    private Response createErrorResponse(final String messageKey)
    {
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", authenticationContext.getI18nHelper().getText(messageKey));
        context.put("webResourcesHtml", getWebresourceTags());
        render("templates/collector/disabled.vm", context);
        return Response.ok(render("templates/collector/disabled.vm", context)).cacheControl(CacheControl.NO_CACHE).build();
    }

    private String renderTemplate(Template template, final Map<String, Object> context, boolean includeResources)
    {
        context.put("webResourcesHtml", includeResources ? getWebresourceTags() : "");
        context.put("canocialBaseurl", velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl());
        return render(template.getTemplatePath(), context);
    }

    private String getWebresourceTags()
    {
        final StringWriter out = new StringWriter();
        webResourceManager.requireResource("jira.webresources:calendar");
        webResourceManager.requireResource("jira.webresources:calendar-" + authenticationContext.getLocale().getLanguage());
        webResourceManager.requireResource("com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:form-collector");
        webResourceManager.includeResources(out, UrlMode.RELATIVE);

        String[] lines = out.toString().split("\\r?\\n");
        final StringBuilder rewrite = new StringBuilder();
        for (String line : lines)
        {
            //prefix the superbatch or any other jira.webresource with a custom 'collector-resource'.
            //This means this resource will then get filtered by the {@link com.atlassian.jira.collector.plugin.transformer.WebResourceFixererUpper}
            //to replace window.top with window. to avoid cross-domain javascript requests!
            if (line.contains("/superbatch/js/batch.js") || line.matches(".*jira\\.webresources\\:.*\\.js.*"))
            {
                rewrite.append(StringUtils.replaceOnce(line, "s/", "s/collector-resource-")).append("\n");
            }
            else
            {
                rewrite.append(line).append("\n");
            }
        }

        return rewrite.toString();
    }

    private String render(String templatePath, final Map<String, Object> context)
    {
        final StringWriter out = new StringWriter();
        try
        {
            context.putAll(JiraVelocityUtils.createVelocityParams(authenticationContext));
            context.put("maxAttachSize", Long.parseLong(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE)));
            templateRenderer.render(templatePath, context, out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return out.toString();
    }

    private String getSummary(final String description)
    {
		if (description.length() > MAX_SUMMARY_LENGTH) {
			return description.substring(0, MAX_SUMMARY_LENGTH) + "...";
		}
        return description;
    }

    private boolean hasCreateIssuePermission(final Collector collector, final User user)
    {
        if (user == null)
        {
            return false;
        }
        if (!collector.isUseCredentials())
        {
            return false;
        }

        final Project projectObj = projectManager.getProjectObj(collector.getProjectId());
        return permissionManager.hasPermission(Permissions.CREATE_ISSUE, projectObj, user);
    }
}
