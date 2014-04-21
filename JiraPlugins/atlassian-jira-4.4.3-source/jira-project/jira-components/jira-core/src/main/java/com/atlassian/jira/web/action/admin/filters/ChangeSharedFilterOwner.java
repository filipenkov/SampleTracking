package com.atlassian.jira.web.action.admin.filters;


import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.type.ShareTypeValidator;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;


import java.net.URI;

/**
 * Action for changing the SharedFilter owner
 *
 * @since v4.4
 */
public class ChangeSharedFilterOwner extends AbstractAdministerFilter
{


    private User ownerUserObj;
    private String owner;
    private String ownerError;
    private final static String FILTERNAME = "filterName";

    private final UserPickerSearchService userPickerSearchService;
    private final AvatarService avatarService;
    private final UserManager userManager;
    private final SearchRequestService searchRequestService;
    private final PermissionManager permissionManager;
    private final ShareTypeValidatorUtils shareTypeValidatorUtils;

    public ChangeSharedFilterOwner(IssueSearcherManager issueSearcherManager, SearchRequestService searchRequestService,
            FavouritesService favouriteService, SearchService searchService, SearchSortUtil searchSortUtil,
            FilterSubscriptionService subscriptionService, PermissionManager permissionManager,
            SearchRequestManager searchRequestManager, UserPickerSearchService userPickerSearchService,
            AvatarService avatarService, UserManager userManager, ShareTypeValidatorUtils shareTypeValidatorUtils)
    {
        super(issueSearcherManager, searchRequestService, favouriteService, searchService, searchSortUtil, subscriptionService, permissionManager, searchRequestManager);
        this.permissionManager = permissionManager;
        this.searchRequestService = searchRequestService;
        this.userPickerSearchService = userPickerSearchService;
        this.avatarService = avatarService;
        this.userManager = userManager;
        this.shareTypeValidatorUtils = shareTypeValidatorUtils;
    }

    @Override
    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    @Override
    protected String doExecute() throws Exception
    {
        JiraServiceContext ctx = getJiraServiceContext(owner);
        searchRequestService.validateFilterForChangeOwner(ctx, getFilter() );
        addErrorCollection(convertDelegatedUserAndFilterNameErrorsToMessages(ctx.getErrorCollection()));
        if (hasAnyErrors())
        {
            return ERROR;
        }
        searchRequestService.updateFilterOwner(ctx, getLoggedInUser(), getFilter());
        addErrorCollection(convertDelegatedUserAndFilterNameErrorsToMessages(ctx.getErrorCollection()));
        if (hasAnyErrors())
        {
            return ERROR;
        }
        if (isInlineDialogMode())
        {
            return returnCompleteWithInlineRedirect(buildReturnUri());
        }
        else
        {
            String returnUrl =  buildReturnUri();
            setReturnUrl(null);
            return forceRedirect(returnUrl);
        }
    }

    // The ShareTypeValidator returns validation failures as field errors, the permissions checker
    //  returns them as messages - simply turn them  into messages.  We are also only interested in the delegated user form
    // of the error message
    private ErrorCollection convertDelegatedUserAndFilterNameErrorsToMessages(ErrorCollection errorCollection)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessages(errorCollection.getErrorMessages());
        String delegatedUserError = errorCollection.getErrors().get(ShareTypeValidator.DELEGATED_ERROR_KEY);
        String filterNameError = errorCollection.getErrors().get(FILTERNAME);
        if (StringUtils.isNotBlank(delegatedUserError))
        {
            errors.addErrorMessage(delegatedUserError);
        }
        if (StringUtils.isNotBlank(filterNameError))
        {
            errors.addErrorMessage(filterNameError);
        }
        return errors;
    }

    @Override
    protected void doValidation()
    {
        if (StringUtils.isBlank(ownerError))
        {
            setOwnerError(null);
        }
        if (StringUtils.isBlank(owner))
        {
            setOwnerError("");
            addError("owner", getText("sharedfilters.admin.filter.owner.empty"));
        }
        else
        {
            validateUserExists(owner);
        }
        if (!hasAnyErrors())
        {
            final JiraServiceContext serviceCtx = getJiraServiceContext(owner);
            getFilter().setOwnerUserName(owner);
            //searchRequestService.validateFilterForUpdate(getJiraServiceContext(owner),getFilter());
            shareTypeValidatorUtils.isValidSharePermission(serviceCtx, getFilter());
            addErrorCollection(convertDelegatedUserAndFilterNameErrorsToMessages(serviceCtx.getErrorCollection()));
        }
    }

    public boolean canChangeOwner()
    {
        return !hasAnyErrors();
    }

    public boolean userPickerDisabled()
    {
        return !userPickerSearchService.canPerformAjaxSearch(this.getJiraServiceContext());
    }

    public User getOwnerUserObj() throws Exception
    {
        if (getOwner() != null && ownerUserObj == null)
        {
            ownerUserObj = userManager.getUserObject(getOwner());
        }
        return ownerUserObj;
    }

    public String getOwner()
    {
        if (owner == null)
        {
            owner = getFilter().getOwnerUserName();
        }
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    private void validateUserExists(String owner)
    {
        if (userManager.getUserObject(owner) == null)
        {
            addError("owner", String.format("The user %s does not exist", owner));
            setOwnerError(owner);
        }
    }

    public URI getOwnerUserAvatarUrl()
    {
        return avatarService.getAvatarUrlNoPermCheck(getOwner(), Avatar.Size.SMALL);
    }

    public String getOwnerError()
    {
        return ownerError;
    }

    public void setOwnerError(String ownerError)
    {
        this.ownerError = ownerError;
    }

    private JiraServiceContext getJiraServiceContext(String owner)
    {
        JiraServiceContext ctx;
        if (permissionManager.hasPermission(Permissions.ADMINISTER, getLoggedInUser()))
        {
            ctx = new JiraServiceContextImpl(UserUtils.getUser(owner));
        }
        else
        {
            ctx =  getJiraServiceContext();
        }
        return ctx;
    }

}
