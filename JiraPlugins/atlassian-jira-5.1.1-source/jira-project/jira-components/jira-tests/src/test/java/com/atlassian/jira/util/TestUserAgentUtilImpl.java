package com.atlassian.jira.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;

import static com.atlassian.jira.util.UserAgentUtil.Browser;
import static com.atlassian.jira.util.UserAgentUtil.BrowserFamily;
import static com.atlassian.jira.util.UserAgentUtil.BrowserMajorVersion;
import static com.atlassian.jira.util.UserAgentUtil.OperatingSystem;
import static com.atlassian.jira.util.UserAgentUtil.OperatingSystem.OperatingSystemFamily;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for BrowserUtils.
 *
 * @since v3.13
 */
public class TestUserAgentUtilImpl extends ListeningTestCase
{

    /**
     * This test was just a pile of agents scraped out of www.atlassian.com access logs.
     * Noteworthy is that we are only testing for version 6 or 7 and
     */
    @Test
    public void testUserAgents()
    {
        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();

        Browser browser = new Browser(BrowserFamily.UKNOWN, BrowserMajorVersion.UNKNOWN, "0");
        OperatingSystem operatingSystem = new OperatingSystem(OperatingSystemFamily.UNKNOWN);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo(null));

        browser = new Browser(BrowserFamily.UKNOWN, BrowserMajorVersion.UNKNOWN, "0");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.UNKNOWN);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo(""));


        browser = new Browser(BrowserFamily.GOOGLE_BOT, BrowserMajorVersion.GOOGLE_BOT, "Google2.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.GOOGLE_BOT);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"));

        browser = new Browser(BrowserFamily.GOOGLE_BOT, BrowserMajorVersion.GOOGLE_BOT, "Google2.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.GOOGLE_BOT);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html) (via babelfish.yahoo.com)"));


        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX3, "Firefox3.0b3");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9b3) Gecko/2008020511 Firefox/3.0b3"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; de; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en) AppleWebKit/523.12.2 (KHTML, like Gecko) Version/3.0.4 Safari/523.12.2"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-us) AppleWebKit/523.12.2 (KHTML, like Gecko) Version/3.0.4 Safari/523.12.2"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-us) AppleWebKit/523.15.1 (KHTML, like Gecko) Version/3.0.4 Safari/523.15"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/523.12 (KHTML, like Gecko) Version/3.0.4 Safari/523.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX15, "Firefox1.5.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.0.4) Gecko/20060508 Firefox/1.5.0.4"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.6");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6"));


        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX15, "Firefox1.5.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.12) Gecko/20070508 Firefox/1.5.0.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.7");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.7) Gecko/20070914 Firefox/2.0.0.7"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.9");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.11");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11"));
        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ca; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12 Creative ZENcast v2.00.14"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; es-ES; rv:1.8.1.12) Gecko/20080201 Dealio Toolbar 3.3 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; fr; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ja; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ko; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-CN; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-TW; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));


        browser = new Browser(BrowserFamily.GECKO, BrowserMajorVersion.NETSCAPE_UNKNOWN, "Netscape8.0");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20050512 Netscape/8.0"));

        browser = new Browser(BrowserFamily.GECKO, BrowserMajorVersion.GECKO_UNKNOWN, "Gecko20021130");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.LINUX);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.2.1; Rojo 1.0; http://www.rojo.com/corporate/help/agg/; Aggregating on behalf of 1 subscriber(s) online at http://www.rojo.com/?feed-id=2425550) Gecko/20021130"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.11");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.LINUX);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.11) Gecko/20071204 Ubuntu/7.10 (gutsy) Firefox/2.0.0.11"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/523.12.9 (KHTML, like Gecko) Version/3.0 Safari/523.12.9"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.LINUX);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080207 Ubuntu/7.10 (gutsy) Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080208 Fedora/2.0.0.12-1.fc7 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080208 Fedora/2.0.0.12-1.fc8 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080212 Firefox/2.0.0.12 (Dropline GNOME)"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.3");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.3) Gecko/20060201 Firefox/2.0.0.3 (MEPIS)"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.6");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070208 Mandriva/2.0.0.6-12mdv2008.0 (2008.0) Firefox/2.0.0.6"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070818 Firefox/2.0.0.6"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.8");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.8) Gecko/20071030 Fedora/2.0.0.8-2.fc8 Firefox/2.0.0.8"));

        browser = new Browser(BrowserFamily.GECKO, BrowserMajorVersion.GECKO_UNKNOWN, "Gecko20070308");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9a1) Gecko/20070308 Minefield/3.0a1"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; es-AR; rv:1.8.1.12) Gecko/20080207 Ubuntu/7.10 (gutsy) Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.11");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-AU; rv:1.8.1.11) Gecko/20071130 Firefox/2.0.0.11"));

        browser = new Browser(BrowserFamily.MSNBOT, BrowserMajorVersion.MSNBOT, "MSNBot1.0");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MSNBOT);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("msnbot/1.0 (+http://search.msn.com/msnbot.htm)"));

        browser = new Browser(BrowserFamily.MSNBOT, BrowserMajorVersion.MSNBOT, "MSNBot1.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MSNBOT);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("msnbot/1.1 (+http://search.msn.com/msnbot.htm)"));

        browser = new Browser(BrowserFamily.MSNBOT, BrowserMajorVersion.MSNBOT, "MSNBotmedia");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("msnbot-media/1.0 (+http://search.msn.com/msnbot.htm)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA8, "Opera8.01");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.UNKNOWN);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/8.01 (J2ME/MIDP; Opera Mini/2.0.6530/1724; en; U; ssr)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA9, "Opera9.24");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.24 (Windows NT 5.1; U; en)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA9, "Opera9.25");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.LINUX);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.25 (X11; Linux i686; U; en)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA9, "Opera9.26");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.26 (Windows NT 5.1; U; en)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA9, "Opera9.26");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.26 (Windows NT 5.1; U; zh-cn)"));


        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0.5730.11");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows 5.2; MSIE 7.0.5730.11)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 7.0.5730.11)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0.5730.13");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 7.0.5730.13)"));
        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0.6000.16609");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1602.1060-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1606.6690-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 5.0.1112.7760-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE6, "MSIE6.0.2900.2180");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 6.0.2900.2180)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE6, "MSIE6.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; digit_may2002; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; FDM; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; QQDownload 1.7; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible ; MSIE 6.0; Windows NT 5.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; EmbeddedWB 14.52 from: http://www.bsalsa.com/ EmbeddedWB 14.52; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; FunWebProducts; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; MS-RTC LM 8)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MS-RTC LM 8)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; Seekmo 10.0.406.0; MSIECrawler)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.2; MS-RTC LM 8)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; MAXTHON 2.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Maxthon; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; InfoPath.1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; FDM)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.2; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 1.0.3705)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2; MS-RTC LM 8)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; FDM)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 1.1.4322; MAXTHON 2.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MAXTHON 2.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; SIMBAR={8DE7AC4A-433D-4ABC-9233-948C3C18974B}; InfoPath.1; FDM; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Avant Browser; .NET CLR 1.1.4322; .NET CLR 2.0.50727; MS-RTC LM 8; InfoPath.2; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; {B2F4C407-0A49-41BB-B754-20BA1F3F9E39}; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Comcast Install 1.0; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Dialect Solutions Group; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.1; .NET CLR 3.0.04506.648; Dialect Solutions Group)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; FunWebProducts)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; FDM)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.2; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Mozilla/4.0(Compatible Mozilla/4.0(Compatible-EmbeddedWB 14.59 http://bsalsa.com/ EmbeddedWB- 14.59  from: http://bsalsa.com/ ; Mozilla/4.0(Compatible Mozilla/4.0EmbeddedWB- 14.59  from: http://bsalsa.com/ ; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; MSDigitalLocker; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; Media Center PC 4.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; InfoPath.2; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.2; .NET CLR 3.0.04506.590; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MS-RTC LM 8; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; IEMB3)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 1.1.4322; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; Win64; x64; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; QQDownload 1.7; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.2; .NET CLR 3.5.21022; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; eMusic DLM/4)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; .NET CLR 1.1.4322; .NET CLR 1.0.3705)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; Zune 2.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; WOW64; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.1; .NET CLR 1.1.4322)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE4, "MSIE4.01");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)"));
        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE5, "MSIE5.01");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)"));
        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE5, "MSIE5.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)"));
    }

    private static final OperatingSystem WINDOWS_OS = new OperatingSystem(OperatingSystemFamily.WINDOWS);
    private static final UserAgentUtil.UserAgent IE7_AGENT = new UserAgentUtil.UserAgent(new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0"), WINDOWS_OS);
    private static final UserAgentUtil.UserAgent IE8_AGENT = new UserAgentUtil.UserAgent(new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE8, "MSIE8.0"), WINDOWS_OS);
    private static final UserAgentUtil.UserAgent IE9_AGENT = new UserAgentUtil.UserAgent(new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE9, "MSIE9.0"), WINDOWS_OS);
    private static final UserAgentUtil.UserAgent IE10_AGENT = new UserAgentUtil.UserAgent(new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE10, "MSIE10.0"), WINDOWS_OS);

    @Test
    public void testInternetExplorerCompatibilityMode() throws Exception
    {
        // https://jdog.atlassian.com/browse/JRADEV-8923
        Browser browser;
        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();

        // IE7
        assertEquals(IE7_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"));
        // IE7 for Windows Phone - http://blogs.msdn.com/b/iemobile/archive/2010/03/25/ladies-and-gentlemen-please-welcome-the-ie-mobile-user-agent-string.aspx
        assertEquals(IE7_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows Phone OS 7.0; Trident/3.1; IEMobile/7.0; <DeviceManufacturer>;<DeviceModel>)"));

        // IE8 in standards mode
        assertEquals(IE8_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"));
        // IE8 in compatibility mode
        assertEquals(IE8_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"));

        // IE9 in standards mode
        assertEquals(IE9_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)"));
        // IE9 in compatibility mode
        assertEquals(IE9_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)"));

        // IE10 in standards mode
        assertEquals(IE10_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)"));
        // IE10 in compatibility mode
        assertEquals(IE10_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/6.0)"));

    }

}
