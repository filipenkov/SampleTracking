package com.atlassian.jira.plugins.share;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mail.MailService;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.plugins.share.event.ShareIssueEvent;
import com.atlassian.jira.plugins.share.event.ShareJqlEvent;
import com.atlassian.jira.plugins.share.event.ShareSearchRequestEvent;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.UrlBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.web.action.issue.IssueNavigator.JQL_QUERY_PARAMETER;

/**
 * Listens for ShareIssueEvents and sends items to the Mail queue.
 *
 * @since v5.0
 */
public class ShareServiceImpl implements ShareService
{
    private static final Logger log = Logger.getLogger(ShareServiceImpl.class);

    private final EventPublisher eventPublisher;
    private I18nHelper.BeanFactory beanFactory;
    private UserUtil userUtil;
    private final PermissionManager permissionManager;
    private final MailService mailService;
    private final ShareManager shareManager;

    public ShareServiceImpl(EventPublisher eventPublisher, I18nHelper.BeanFactory beanFactory, UserUtil userUtil,
            final PermissionManager permissionManager, MailService mailService, ShareManager shareManager)
    {
        this.eventPublisher = eventPublisher;
        this.beanFactory = beanFactory;
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
        this.mailService = mailService;
        this.shareManager = shareManager;
    }


    @Override
    public ValidateShareIssueResult validateShareIssue(User remoteUser, ShareBean shareBean, Issue issue)
    {
        final ErrorCollection errors = validateShare(remoteUser, shareBean);
        return new ValidateShareIssueResult(errors, remoteUser, shareBean, issue);
    }

    @Override
    public void shareIssue(ValidateShareIssueResult result)
    {
        if (!result.isValid())
        {
            throw new IllegalStateException("Validation result was not valid.");
        }

        Map<String, Object> params = Maps.newHashMap();
        Issue issue = result.getIssue();
        params.put("issue", issue);
        params.put("remoteUser", result.getRemoteUser());

        sendShareIssueEmails("share-issue.vm", result, params);

        User from = result.getRemoteUser();
        ShareBean shareBean = result.getShareBean();
        eventPublisher.publish(new ShareIssueEvent(issue, from, shareBean.getUsernames(), shareBean.getEmails(), shareBean.getMessage()));
    }

    @Override
    public ValidateShareSearchRequestResult validateShareSearchRequest(User remoteUser, ShareBean shareBean, SearchRequest searchRequest)
    {
        final ErrorCollection errors = validateShare(remoteUser, shareBean);
        return new ValidateShareSearchRequestResult(errors, remoteUser, shareBean, searchRequest);
    }

    @Override
    public void shareSearchRequest(ValidateShareSearchRequestResult result)
    {
        if (!result.isValid())
        {
            throw new IllegalStateException("Validation result was not valid.");
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("remoteUser", result.getRemoteUser());

        String jql;
        SearchRequest searchRequest = result.getSearchRequest();
        if (searchRequest != null)
        {
            // Saved search
            UrlBuilder savedSearchUrlBuilder = new UrlBuilder(false);
            savedSearchUrlBuilder.addParameter("mode", "hide");
            savedSearchUrlBuilder.addParameter("requestId", searchRequest.getId());
            params.put("savedSearchLinkUrlParams", savedSearchUrlBuilder.asUrlString());
            params.put("filterName", searchRequest.getName());

            jql = searchRequest.getQuery().getQueryString();
        }
        else
        {
            // JQL only
            jql = result.shareBean.getJql();
        }

        UrlBuilder jqlUrlBuilder = new UrlBuilder(false);
        jqlUrlBuilder.addParameter("reset", true);
        jqlUrlBuilder.addParameter(JQL_QUERY_PARAMETER, jql);
        params.put("jqlSearchLinkUrlParams", jqlUrlBuilder.asUrlString());

        String message = result.getShareBean().getMessage();
        if (StringUtils.isNotBlank(message))
        {
            params.put("comment", message);
            params.put("htmlComment", TextUtils.htmlEncode(message));  // required by templates/email/html/includes/fields/comment.vm
        }

        sendShareSearchEmails(result, params);

        final ShareBean shareSearchBean = result.getShareBean();
        if (searchRequest != null)
        {
            eventPublisher.publish(new ShareSearchRequestEvent(result.getRemoteUser(), shareSearchBean.getUsernames(),
                    shareSearchBean.getEmails(), shareSearchBean.getMessage(), searchRequest));
        }
        else
        {
            eventPublisher.publish(new ShareJqlEvent(result.getRemoteUser(), shareSearchBean.getUsernames(),
                    shareSearchBean.getEmails(), shareSearchBean.getMessage(), jql));
        }
    }

    private ErrorCollection validateShare(User remoteUser, ShareBean shareBean)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nHelper = beanFactory.getInstance(remoteUser);
        if (shareBean.getUsernames().isEmpty() && shareBean.getEmails().isEmpty())
        {
            errors.addErrorMessage(i18nHelper.getText("jira-share-plugin.no.users.or.emails.provided"));
        }

        if (!permissionManager.hasPermission(Permissions.USER_PICKER, remoteUser))
        {
            errors.addErrorMessage(i18nHelper.getText("jira-share-plugin.no.permission.to.browse.users"));
        }
        return errors;
    }

    private void sendShareIssueEmails(final String template, ValidateShareResult result, Map<String, Object> params)
    {
        User from = result.getRemoteUser();
        ShareBean shareBean = result.getShareBean();
        List<NotificationRecipient> recipients = getRecipients(shareBean);

        String message = shareBean.getMessage();
        if (StringUtils.isNotBlank(message))
        {
            params.put("comment", message);
            params.put("htmlComment", TextUtils.htmlEncode(message));  // required by templates/email/html/includes/fields/comment.vm
        }

        for (NotificationRecipient recipient : recipients)
        {
            String subjectTemplatePath = "templates/email/subject/" + template;
            String bodyTemplatePath = "templates/email/" + recipient.getFormat() + "/" + template;
            mailService.sendRenderedMail(from, recipient, subjectTemplatePath, bodyTemplatePath, params);
        }
    }

    private void sendShareSearchEmails(ValidateShareSearchRequestResult result, Map<String, Object> params)
    {
        User from = result.getRemoteUser();
        ShareBean shareBean = result.getShareBean();
        SearchRequest searchRequest = result.getSearchRequest();
        List<NotificationRecipient> recipients = getRecipients(shareBean);

        // Each email might be of two kinds - if it's to a user with permission to see the specified saved search (if
        // specified), then share the filter. If not, share the JQL.
        for (NotificationRecipient recipient : recipients)
        {
            User userRecipient = recipient.getUserRecipient();
            boolean shareSavedSearch = searchRequest != null && userRecipient != null && shareManager.isSharedWith(userRecipient, searchRequest);
            String template = shareSavedSearch ? "share-saved-search.vm" : "share-jql-search.vm";
            String subjectTemplatePath = "templates/email/subject/" + template;
            String bodyTemplatePath = "templates/email/" + recipient.getFormat() + "/" + template;

            mailService.sendRenderedMail(from, recipient, subjectTemplatePath, bodyTemplatePath, params);
        }
    }

    private List<NotificationRecipient> getRecipients(ShareBean shareBean)
    {
        List<NotificationRecipient> recipients = Lists.newArrayList();

        if (shareBean.getUsernames() != null)
        {
            for (String toUsername : shareBean.getUsernames())
            {
                User user = userUtil.getUser(toUsername);
                if (user != null)
                {
                    recipients.add(new NotificationRecipient(user));
                }
                else
                {
                    // The front should normally catch this, more likely someone hit the REST resource directly.
                    log.warn("No user found for name: " + toUsername);
                }
            }
        }
        if (shareBean.getEmails() != null)
        {
            for (String toEmail : shareBean.getEmails())
            {
                recipients.add(new NotificationRecipient(toEmail));
            }
        }
        return recipients;
    }
}
