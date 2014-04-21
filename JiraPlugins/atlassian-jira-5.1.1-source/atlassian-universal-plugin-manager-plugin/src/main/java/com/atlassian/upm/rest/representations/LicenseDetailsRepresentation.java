package com.atlassian.upm.rest.representations;

import java.util.Date;

import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.LicenseType;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public final class LicenseDetailsRepresentation
{
    @JsonProperty private final boolean valid;
    @JsonProperty private final LicenseError error;
    @JsonProperty private final boolean evaluation;
    @JsonProperty private final boolean nearlyExpired;
    @JsonProperty private final Integer maximumNumberOfUsers;
    @JsonProperty private final Date maintenanceExpiryDate;
    @JsonProperty private final LicenseType licenseType;
    @JsonProperty private final Date expiryDate;
    @JsonProperty private final String rawLicense;
    @JsonProperty private final String maintenanceExpiryDateString;
    @JsonProperty private final String supportEntitlementNumber;
    @JsonProperty private final String organizationName;
    @JsonProperty private final String contactEmail;

    @JsonCreator
    public LicenseDetailsRepresentation(@JsonProperty("valid") Boolean valid,
                                        @JsonProperty("error") LicenseError error,
                                        @JsonProperty("evaluation") Boolean evaluation,
                                        @JsonProperty("nearlyExpired") Boolean nearlyExpired,
                                        @JsonProperty("maximumNumberOfUsers") Integer maximumNumberOfUsers,
                                        @JsonProperty("maintenanceExpiryDate") Date maintenanceExpiryDate,
                                        @JsonProperty("licenseType") LicenseType licenseType,
                                        @JsonProperty("expiryDate") Date expiryDate,
                                        @JsonProperty("rawLicense") String rawLicense,
                                        @JsonProperty("maintenanceExpiryDateString") String maintenanceExpiryDateString,
                                        @JsonProperty("pluginSupportEntitlementNumber") String supportEntitlementNumber,
                                        @JsonProperty("organizationName") String organizationName,
                                        @JsonProperty("contactEmail") String contactEmail)
    {
        this.valid = (valid == null) ? false : valid.booleanValue();
        this.error = error;
        this.evaluation = (evaluation == null) ? false : evaluation.booleanValue();
        this.nearlyExpired = (nearlyExpired == null) ? false : nearlyExpired.booleanValue();
        this.maximumNumberOfUsers = maximumNumberOfUsers;
        this.licenseType = licenseType;
        this.maintenanceExpiryDate = maintenanceExpiryDate;
        this.maintenanceExpiryDateString = maintenanceExpiryDateString;
        this.expiryDate = expiryDate;
        this.rawLicense = rawLicense;
        this.supportEntitlementNumber = supportEntitlementNumber;
        this.organizationName = organizationName;
        this.contactEmail = contactEmail;
    }

    public boolean isValid()
    {
        return valid;
    }

    public LicenseError getError()
    {
        return error;
    }

    public boolean isEvaluation()
    {
        return evaluation;
    }

    public boolean isNearlyExpired()
    {
        return nearlyExpired;
    }

    public Integer getMaximumNumberOfUsers()
    {
        return maximumNumberOfUsers;
    }

    public Date getMaintenanceExpiryDate()
    {
        return maintenanceExpiryDate;
    }

    public String getMaintenanceExpiryDateString()
    {
        return maintenanceExpiryDateString;
    }

    public LicenseType getLicenseType()
    {
        return licenseType;
    }

    public Date getExpiryDate()
    {
        return expiryDate;
    }

    public String getRawLicense()
    {
        return rawLicense;
    }

    public String getSupportEntitlementNumber()
    {
        return supportEntitlementNumber;
    }

    public String getOrganizationName()
    {
        return organizationName;
    }

    public String getContactEmail()
    {
        return contactEmail;
    }
}