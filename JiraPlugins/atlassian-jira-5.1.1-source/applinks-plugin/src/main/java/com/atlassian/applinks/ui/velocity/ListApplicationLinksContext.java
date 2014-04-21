package com.atlassian.applinks.ui.velocity;

import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.auth.OrphanedTrustCertificate;
import com.atlassian.applinks.core.auth.OrphanedTrustDetector;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.StaticUrlApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class lists all objects which are used in the velocity templates in the list application
 * links screen.
 *
 * @since 3.0
 */
public class ListApplicationLinksContext extends AbstractVelocityContext
{
    private final I18nResolver i18nResolver;
    private final OrphanedTrustDetector orphanedTrustDetector;
    private List<OrphanedTrustCertificate> orphanedTrustCertificates;

    /**
     * The set of entity types this application supports.
     */
    private final Set<String> entityTypeIdStrings;

    /* (non-javadoc)
     * Use VelocityContextFactory to construct
     */
    ListApplicationLinksContext(final InternalHostApplication internalHostApplication,
                                final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                final InternalTypeAccessor typeAccessor,
                                final I18nResolver i18nResolver,
                                final DocumentationLinker documentationLinker,
                                final OrphanedTrustDetector orphanedTrustDetector,
                                final String contextPath)
    {

        super(contextPath, internalHostApplication, batchedJSONi18NBuilderFactory, typeAccessor, documentationLinker);
        this.i18nResolver = i18nResolver;
        this.orphanedTrustDetector = orphanedTrustDetector;

        entityTypeIdStrings = Sets.newHashSet(
                Iterables.transform(typeAccessor.getEntityTypesForApplicationType(TypeId.getTypeId(internalHostApplication.getType())),
                        new Function<EntityType, String>()
                {
                    public String apply(@Nullable EntityType from)
                    {
                        return TypeId.getTypeId(from).get();
                    }
                }));
    }
    @HtmlSafe
    public JSONArray getNonAppLinksApplicationTypes()
    {
        return getApplicationTypesJSON();
    }
    @HtmlSafe
    public JSONArray getLocalEntityTypeIdStrings()
    {
        final JSONArray entityTypeIdJSON = new HTMLSafeJSONArray();

        for (final String typeId : entityTypeIdStrings)
        {
            entityTypeIdJSON.put(typeId);
        }
        return entityTypeIdJSON;
    }

    public String getApplicationType()
    {
        return TypeId.getTypeId(internalHostApplication.getType()).get();
    }

    private JSONArray getApplicationTypesJSON()
    {
        final JSONArray applicationTypesJSON = new HTMLSafeJSONArray();

        final Iterable<NonAppLinksApplicationType> applicationTypes = Iterables.filter(typeAccessor.getEnabledApplicationTypes(), NonAppLinksApplicationType.class);
        for (final NonAppLinksApplicationType nonAppLinksApplicationType : applicationTypes)
        {
            final JSONObject appType = new HTMLSafeJSONObject();
            try
            {
                appType.put("typeId", nonAppLinksApplicationType.getId().get());
                appType.put("label", i18nResolver.getText(nonAppLinksApplicationType.getI18nKey()));
                applicationTypesJSON.put(appType);
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
        return applicationTypesJSON;
    }

    public Collection<StaticUrlApplicationType> getStaticUrlApplicationTypes()
    {
        // Stupid iterables, can't check if an iterable is empty without .iterator().hasNext() == fase
        Collection<StaticUrlApplicationType> types = new ArrayList<StaticUrlApplicationType>();
        for (StaticUrlApplicationType type :Iterables.filter(typeAccessor.getEnabledApplicationTypes(), StaticUrlApplicationType.class))
        {
            types.add(type);
        }
        return types;
    }

    public List<OrphanedTrustCertificate> getOrphanedTrustCertificates()
    {
        if (orphanedTrustCertificates == null)
        {
            orphanedTrustCertificates = orphanedTrustDetector.findOrphanedTrustCertificates();
        }
        return orphanedTrustCertificates;
    }

    public UnescapedI18nResolver getI18nNoEscape()
    {
        return new UnescapedI18nResolver();
    }

    public class UnescapedI18nResolver
    {
        @HtmlSafe
        public String getText(final String key, final String... arguments)
        {
            return i18nResolver.getText(key, (String[]) arguments);
        }
    }

    protected static class HTMLSafeJSONObject extends JSONObject
    {
        @Override
        @HtmlSafe
        public String toString()
        {
            return super.toString();
        }
    }

    protected static class HTMLSafeJSONArray extends JSONArray
    {
        @Override
        @HtmlSafe
        public String toString()
        {
            return super.toString();
        }
    }

}
