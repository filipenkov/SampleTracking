package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.favourites.FavouritesStore;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * This will do the following upgrade tasks - Update users "owned" portal pages to be favourites by default - Change the name of any called
 * "dashboard" to an I18N version name - Change the system default dashboard page to have a new "System Dashboard" name
 *
 * @since v3.13
 */
public class UpgradeTask_Build325 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build325.class);

    private static final String DASHBOARD_ORIG_NAME = "dashboard";

    private static final String PORTAL_PAGE_ENTITY = "PortalPage";

    private static final String USERNAME_COLUMN = "username";
    private static final String ID_COLUMN = "id";
    private static final String SEQUENCE_COLUMN = "sequence";

    private final GenericDelegator delegator;
    private final PortalPageManager portalPageManager;
    private final FavouritesStore favouritesStore;
    private final I18nHelper.BeanFactory i18n;

    public UpgradeTask_Build325(final GenericDelegator delegator, final FavouritesStore favouritesStore, final PortalPageManager portalPageManager, I18nHelper.BeanFactory i18n)
    {
        super(false);
        this.delegator = delegator;
        this.favouritesStore = favouritesStore;
        this.portalPageManager = portalPageManager;
        this.i18n = i18n;
    }

    public String getBuildNumber()
    {
        return "325";
    }

    public String getShortDescription()
    {
        return "Initialise favourite dashboards - make each dashboard a favourite of its owner.";
    }

    /**
     * This upgrade task need to do 2 things. It needs to migrate existing user's dashboard pages to become favourites and it needs to change the name
     * of the default system page to be "System Default Dashboard".
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        //
        // for every page in the system that is owner by someone, upgrade it to a favourite
        upgradeToFavourites();

        //
        // when the user name is null then the this is the system default page. We want to set its name
        // to a new value and also update some permission.
        upgradeSystemDefaultDashboardPage();

    }

    private void upgradeToFavourites()
    {
        final List pageList;
        try
        {
            pageList = delegator.findByCondition(PORTAL_PAGE_ENTITY, new EntityExpr(USERNAME_COLUMN, EntityOperator.NOT_EQUAL, null), EasyList.build(
                ID_COLUMN, USERNAME_COLUMN), EasyList.build(USERNAME_COLUMN, SEQUENCE_COLUMN, ID_COLUMN));
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving dashboards.", e);
        }

        for (final Iterator iterator = pageList.iterator(); iterator.hasNext();)
        {
            Long id = null;
            String userName = null;
            try
            {
                final GenericValue portalPageGV = (GenericValue) iterator.next();

                id = portalPageGV.getLong(ID_COLUMN);
                userName = portalPageGV.getString(USERNAME_COLUMN);
                if (userName != null)
                {
                    Locale locale = getUserLocale(userName);
                    // We don't need to check permissions as the user is the owner, so just get by PortalPageId.
                    PortalPage portalPage = getPortalPage(id);
                    if (portalPage != null)
                    {
                        portalPage = changeDashboardName(locale, portalPage);
                        addAsFavourite(userName, portalPage);
                    }
                    else
                    {
                        log.info("Could not retrieve dashboard with id '" + id + "'");
                    }
                }
            }
            catch (GenericEntityException e)
            {
                log.info("Could not find owner '" + userName + "' for dashboard with id '" + id + "'");
            }
            catch (final DataAccessException dae)
            {
                // lets ignore this and move onto the next one.
                final String errMsg = "Error occurred while getting request or adding favourite for user '" + userName + "' for dashboard '" + id + "'";
                if (log.isDebugEnabled())
                {
                    log.debug(errMsg, dae);
                }
                else
                {
                    log.info(errMsg);
                }
            }
        }
    }

    Locale getUserLocale(final String userName) throws GenericEntityException
    {
        UserDetailBean userDetails = null;
        userDetails = new UserDetailBean(userName);
        return userDetails.getLocale();
    }

    private PortalPage changeDashboardName(final Locale locale, PortalPage portalPage)
    {
        if (DASHBOARD_ORIG_NAME.equals(portalPage.getName()))
        {
            // TGC: can get this (I18nBean) using the locale directly from OSEntity
            final I18nHelper helper = createI18nHelper(locale);
            String portalPageName = helper.getText("common.concepts.dashboard");
            if (StringUtils.isBlank(portalPageName))
            {
                portalPageName = "Dashboard";
            }
            portalPage = portalPageManager.update(PortalPage.portalPage(portalPage).name(portalPageName).build());
        }
        return portalPage;
    }

    private void upgradeSystemDefaultDashboardPage()
    {
        try
        {
            final PortalPage systemDefaultPage = portalPageManager.getSystemDefaultPortalPage();
            PortalPage updatedPage = PortalPage.portalPage(systemDefaultPage).name("System Dashboard").permissions(SharePermissions.GLOBAL).build();
            updatedPage = portalPageManager.update(updatedPage);

            //give the system default page a count. It will be set to zero.
            portalPageManager.adjustFavouriteCount(updatedPage, 0);
        }
        catch (final DataAccessException dae)
        {
            final String errMsg = "Error occurred while updating system default dashboard!";
            if (log.isDebugEnabled())
            {
                log.debug(errMsg, dae);
            }
            else
            {
                log.info(errMsg);
            }
        }
    }

    I18nHelper createI18nHelper(final Locale locale)
    {
        return i18n.getInstance(locale);
    }

    ///CLOVER:ON

    /**
     * Get the portal page for a user, if he has permission.
     * @param portalPageId Portal page Id
     * @return a portal page or
     */
    private PortalPage getPortalPage(final Long portalPageId)
    {
        return portalPageManager.getPortalPageById(portalPageId);
    }

    private void addAsFavourite(final String username, final PortalPage portalPage)
    {
        // Use the store directly and pass user name but need to adjust fav count using portalPageManager
        favouritesStore.addFavourite(username, portalPage);
        portalPageManager.adjustFavouriteCount(portalPage, 1) ;
    }

    /**
     * Bean to hold very basic detail of a user.
     */
    private class UserDetailBean
    {
        private String email;
        private String fullName;
        private Locale locale;

        UserDetailBean (String username) throws GenericEntityException
        {
            // Try to get an Internal entity first.
            if (!getUserDetail(username, "OSUser"))
            {
                // Try for an External user
                if (!getUserDetail(username, "ExternalEntity"))
                {
                    throw new GenericEntityException("User not found");
                }
            }
        }

        private boolean getUserDetail(final String username, final String userEntityType)
        {
            List OSUserGVs = null;
            OSUserGVs = getOfBizDelegator().findByAnd(userEntityType, EasyMap.build("name", username), EasyList.build("name ASC"));
            // There should only ever be one, so we will take the first.
            if (OSUserGVs.size() == 0)
            {
                return false;
            }
            GenericValue osUser = (GenericValue) OSUserGVs.iterator().next();
            PropertySet ofbizPs = OFBizPropertyUtils.getPropertySet(osUser);
            email = ofbizPs.getString("email");
            fullName = ofbizPs.getString("fullName");
            String localeStr = ofbizPs.getString(PreferenceKeys.USER_LOCALE);

            if (StringUtils.isBlank(localeStr))
            {
                locale = ComponentAccessor.getApplicationProperties().getDefaultLocale();
            }
            else
            {
                locale = LocaleParser.parseLocale(localeStr);
            }
            return true;
        }

        public String getEmail()
        {
            return email;
        }

        public String getFullName()
        {
            return fullName;
        }

        public Locale getLocale()
        {
            return locale;
        }
    }

}