package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.core.auth.OrphanedTrustCertificate;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "orphanedTrustList")
public class OrphanedTrustEntityList
{
    @XmlElement(name = "orphanedTrust")
    private List<OrphanedTrust> orphanedTrustList;

    @SuppressWarnings("unused")
    private OrphanedTrustEntityList()
    {
    }

    public OrphanedTrustEntityList(final List<OrphanedTrustCertificate> orphanedTrustCertificates)
    {
        this.orphanedTrustList = Lists.transform(orphanedTrustCertificates,
                new Function<OrphanedTrustCertificate, OrphanedTrust>()
                {
                    public OrphanedTrust apply(final OrphanedTrustCertificate from)
                    {
                        return new OrphanedTrust(from.getId(), from.getType());
                    }
                });
    }

    public List<OrphanedTrust> getOrphanedTrustList()
    {
        return orphanedTrustList;
    }

}
