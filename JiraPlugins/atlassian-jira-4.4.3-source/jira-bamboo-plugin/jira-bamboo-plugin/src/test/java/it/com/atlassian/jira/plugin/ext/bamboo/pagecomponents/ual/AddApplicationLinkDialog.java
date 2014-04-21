package it.com.atlassian.jira.plugin.ext.bamboo.pagecomponents.ual;

import com.atlassian.pageobjects.PageBinder;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class AddApplicationLinkDialog
{
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(AddApplicationLinkDialog.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private String serverUrl;

    private boolean reciprocalLink;
    private String reciprocalUrl;
    private String reciprocalUser;
    private String reciprocalPassword;

    private boolean sameUsers;
    private boolean trusted;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    @Inject
    private PageBinder pageBinder;

    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    public AddApplicationLinkDialog withServerUrl(@NotNull String url)
    {
        this.serverUrl = url;
        return this;
    }

    public AddApplicationLinkDialog withReciprocalLink(@Nullable String url, @NotNull String userName, @NotNull String password)
    {
        this.reciprocalLink = true;
        this.reciprocalUrl = url;
        this.reciprocalUser = userName;
        this.reciprocalPassword = password;
        return this;
    }

    public AddApplicationLinkDialog withSameUsers()
    {
        this.sameUsers = true;
        return this;
    }

    public AddApplicationLinkDialog withDifferentUsers()
    {
        this.sameUsers = false;
        return this;
    }

    public AddApplicationLinkDialog withTrustedRelationship()
    {
        this.trusted = true;
        return this;
    }

    public AddApplicationLinkDialog withNoTrustedRelationship()
    {
        this.trusted = false;
        return this;
    }

    public void submit()
    {
        // bind ServerUrlScreen - it should already be on this page...
        pageBinder.bind(ServerUrlScreen.class)
                .submitServerUrl(serverUrl)
                .submitReciprocalDetails(reciprocalLink, reciprocalUrl, reciprocalUser, reciprocalPassword)
                .submitTrustDetails(sameUsers, trusted);
    }
    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
