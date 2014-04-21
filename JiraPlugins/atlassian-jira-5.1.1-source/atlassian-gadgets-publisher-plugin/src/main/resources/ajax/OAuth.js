jQuery.namespace("AG.ajax.OAuth");

/**
 * Makes a request using oauth with jQuery.ajax formatted options.
 *
 * @param options
 */
AG.ajax.OAuth = function (options) {

    var xhr = new AG.MockXHR(),
        params;

    options.authorization = "oauth";
    options.useToken = "always";

    params = AG.mapToMakeRequestOptions(options);

    options.send();

    gadgets.io.makeRequest(params.url, function (response) {
        xhr.updateFromMakeRequestResp(response);
        AG.ajax.OAuth.handleResponse(options, response, xhr);
    }, params);

    return xhr;
};

/**
 * Handles makeRequest response
 *
 * @param options - ajax settings
 * @param {object} response - makeRequest response  object
 */
AG.ajax.OAuth.handleResponse = function (options, response, xhr) {
    
    if (response.oauthApprovalUrl || response.rc === 403) {

        if (response.oauthApprovalUrl) {
            jQuery(document).trigger("ajax.oAuthAvailable", [response.oauthApprovalUrl, options]);
        }
        
        if (options.unauthorized) {
            options.unauthorized(options, xhr, response.oauthApprovalUrl);
        }
    } else {
        AG.ajax.handleCallbacks(options, response.data, xhr);
    }
};

/**
 * Gets oauth approvalButton. This button will trigger an approval popup that will guide the user through
 * the authentication process. After finishing authentication the gadget iframe will be refreshed.
 *
 */
AG.ajax.OAuth.getApprovalButton = function (approvalUrl, requestOptions) {

    var $approvalButton,
        oauthPopup;

    if (approvalUrl) {

        oauthPopup =  new gadgets.oauth.Popup(approvalUrl, null, function(){}, function() {

            requestOptions.complete = function () {
                window.location.reload();
            };

            AG.ajax.OAuth(requestOptions);

        });

        $approvalButton = jQuery("<button class='oauth-approve'>").text(AG.getText("gadget.common.oauth.approve.button"));

        $approvalButton.click(oauthPopup.createOpenerOnClick()); // bind popup

        return $approvalButton;
    }
};