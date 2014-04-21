package com.atlassian.crowd.integration.http.util;

import com.atlassian.crowd.model.authentication.ValidationFactor;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Extracts ValidationFactors.
 */
public class CrowdHttpValidationFactorExtractorImpl implements CrowdHttpValidationFactorExtractor
{
    private static final CrowdHttpValidationFactorExtractor INSTANCE = new CrowdHttpValidationFactorExtractorImpl();

    private CrowdHttpValidationFactorExtractorImpl() {}

    public List<ValidationFactor> getValidationFactors(HttpServletRequest request)
    {
        List<ValidationFactor> validationFactors = new ArrayList<ValidationFactor>();

        if (request != null)
        {
            // add the proxy through address is necessary
            String remoteAddress = request.getRemoteAddr();
            if (remoteAddress != null && remoteAddress.length() > 0)
            {
                validationFactors.add(new ValidationFactor(ValidationFactor.REMOTE_ADDRESS, remoteAddress));
            }

            String remoteAddressXForwardFor = request.getHeader(ValidationFactor.X_FORWARDED_FOR);
            if (remoteAddressXForwardFor != null && !remoteAddressXForwardFor.equals(remoteAddress))
            {
                validationFactors.add(new ValidationFactor(ValidationFactor.X_FORWARDED_FOR, remoteAddressXForwardFor));
            }

            // Not including USER_AGENT as a validation factor because IE8 sometimes gives IE7 user agent string (see CWD-1827)
        }

        return validationFactors;
    }

    public static CrowdHttpValidationFactorExtractor getInstance()
    {
        return INSTANCE;
    }
}
