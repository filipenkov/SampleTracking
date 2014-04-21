package com.atlassian.renderer.wysiwyg;

import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.basic.PanelMacro;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.macro.RadeoxCompatibilityMacro;

import java.util.Map;
import java.io.Writer;
import java.io.IOException;

import org.radeox.macro.parameter.MacroParameter;


public class TestSimpleMarkup extends WysiwygTest
{
    public void testInsertRendersAsU()
    {
        assertEquals("<p><u>a</u></p>",getConverter().convertWikiMarkupToXHtml(getContext(), "+a+"));
    }
    public void testNewlyInsertedLinkAfterTable()
    {
        testXHTML("<table class=\"confluenceTable\"><tbody>\n" +
                "<tr>\n" +
                "<th class=\"confluenceTh\">x</th>\n" +
                "<th class=\"confluenceTh\">y</th>\n" +
                "</tr>\n" +
                "\n" +
                "</tbody></table>\n" +
                "<a href=\"/display/ds/Creating+pages+and+linking\" title=\"Creating pages and linking\" linktype=\"raw\" linktext=\"Creating pages and linking\">Creating pages and linking</a>",
                  "|| x || y ||\n[Creating pages and linking]");
    }

    public void testLayoutMacros()
    {
        testMarkup("{section}\n{column:width=30%}\n* foo\n* bar\n* baz\n{column}\n{column}\n| foo | bar |\n| 1 | 2 |\n{column}\n{section}");
    }

    public void testMarkupEscapes()
    {
        testXHTML("bar _foo_ baz", "bar \\_foo\\_ baz");
        testXHTML("_foo_", "\\_foo\\_");
        testXHTML("** foo * bar", "\\*\\* foo * bar");
        testXHTML("# foo # bar", "\\# foo # bar");
        testXHTML("## foo - bar", "\\## foo - bar");
    }

    public void testEntitiesPreserved()
    {
        testMarkup("&nbsp;[a][b]");
        testMarkup("�xxx�");
    }

    public void testLinkInMacro()
    {
        testXHTML("<div class=\"macro\" macrotext=\"{panel}\" command=\"panel\"><div class=\"panel\"><div class=\"panelContent\">\n" +
                "<p>This is a test <span class=\"nobr\"><a href=\"/pages/createpage.action?spaceKey=ds&amp;title=link&amp;linkCreation=true&amp;fromPageId=257\" title=\"Create Page: link\" class=\"createlink\" linktype=\"raw\" linktext=\"link\">link<sup><img class=\"rendericon\" src=\"/images/icons/plus.gif\" alt=\"\" align=\"middle\" border=\"0\" height=\"7\" width=\"7\"></sup></a></span><br>\n" +
                "</p>\n" +
                "\n" +
                "</div></div></div>", "{panel}\nThis is a test [link]\n{panel}");
    }

    public void testEmptyCodeMacro()
    {
        testMarkup("{code}\n\n{code}");
    }

    public void testEmptyMetadataMacro()
    {
        testMarkup("{metadata:Person}{metadata}");
    }

    public void testMacroInTable2()
    {
        testMarkup("| {quote}\nxxxx\n{quote} |");
    }

    public void testTextAfterParagraph()
    {
        testXHTML("<p>foo</p>bar", "foo\n\nbar");
    }

    public void testJIRAReleasePlan()
    {
        testMarkup("h4. How to use this page\n\n" +
                "Before each release, uncheck all the tasks that has been completed for the previous release. And task away\\! Update this page with whatever information you think is useful. Make it better and easier for all that may follow your path\\!\n" +
                "{note}A \"done\" tick either means that it's done or strategically ignored (eg. irrelevent for beta / point releases.{note}\n" +
                "(i) Note: for the maven commands that upload stuff the repository.atlassian.com, you need some connection details defined in \\~/build.properties:\n" +
                "{quote}\n" +
                "maven.username=orion\n" +
                "maven.repo.central = repository.atlassian.com\n" +
                "maven.repo.central.directory = /var/www/html/repository\n" +
                "{quote}\n" +
                "\nh3. Things to check in JIRA\n\n" +
                "{tasklist: JIRA checks}\n" +
                "Compare *entitymodel.xml* with that from previous version, and check for SQL keyword clashes\n" +
                "{tasklist}\n" +
                "\nh3. Pre build tasks\n\n" +
                "{tasklist:Update [http://jira.atlassian.com]}\n" +
                "Create new [version(s) | http://jira.atlassian.com/secure/project/ManageVersions.jspa?pid=10240] if needed\n" +
                "Move current version unresolved issues to next version\n" +
                "Create a new directory on */resches/devfiles/releases/jira/JIRA-x.x.x*\n" +
                "Increment the build number in Jira's project.properties\n" +
                "Create a confluence page for the Jira release notes, [here|http://confluence.atlassian.com/display/JIRA/JIRA+3.2.1+Release+Notes] is an example. NOTE: make the page private until you really release Jira.\n" +
                "{tasklist}\n" +
                "\nh3. JIRA Docs\n\n" +
                "In the jira-docs module, increment the version number (3.2.1, etc) in files below. This task should only be done for full release versions (i.e. not previews and betas)\n" +
                "{tasklist: Increment version number}\n" +
                "*site.xml* (add a element in and move the contents from the previous version)\n" +
                "*external-refs.xml* (jars refs)\n" +
                "*api.xml* (javadoc links)\n" +
                "*site2properties.xsl* modify the version in the url-prefix property in the file to be the correct version\n" +
                "*index.xml* (change the docs version number)\n" +
                "Do a grep -rn '' * to discover any others. (eg config/ldap.xml, install/mysql-guide-linux.xml, install/servers/tomcatXX.xml, etc.)\n" +
                "Generate the *help-paths.properties* by running {{ant}} at {{jira-docs/etc}} and copy to {{jira/src/webapp/WEB-INF/classes}} cp help-paths.properties ../../jira/src/webapp/WEB-INF/classes\n" +
                "{tasklist}\n" +
                "Also synchronize the \\*.java files in web/src/documentation/content/examples/ with those from the JIRA release.\n" +
                "After you've updated the docs, it's time to build\\!\n" +
                "{tasklist: Building JIRA docs}\n" +
                "Tag the {{jira-docs}} module with *cvs tag atlassian_jira_x_x*\n" +
                "If doing a major (x.y) release, branch the {{jira-docs}} module with *cvs tag -b atlassian_jira_x_x_branch*\n" +
                "Add latest JIRA version to *{{/jeff/notes/jiraversions.txt}}* in atlassian-scripts and commit\n" +
                "SSH to orion@www.atlassian.com {{cvs update}} in the module {{/home/orion/src/atlassian-scripts}}. Note that CVS sits on Resches.\n" +
                "Run *{{docs_onserver_update 3.2.1}}*. This will download the docs from the specified tag, run forrest and copy to the right version location\n" +
                "Run *{{docsrc_onserver_update 3.2.1}}*. This will create and place the docs xml src archive so that the link on the docs site will work.\n" +
                "Run *{{local_rpcplugin_javadoc 3.2.3}}*. This will create the rpc docs and place them in the right place. You must then modify the soft link to latest, /opt/j2ee/atlassian/website-static/software/jira/docs/api/rpc-jira-plugin\n" +
                "Run *{{local_jira_javadoc 3.2.3}}*. This will create the jira javadocs and place them in the right place.\n" +
                "Link the newly created version of jira docs to appropriate e.g. latest /opt/j2ee/atlassian/website-static/software/jira/docs ln -fs v3.2.1 latest\n" +
                "Make sure the the server jars have been updated as described below.\n" +
                "{tasklist}\n" +
                "{panel:title=Update server JARS}\n" +
                "*Update server JARS*\n" +
                "* Build and upload server JARS in $JIRA_HOME$ for online docs if the have changed.\n" +
                "* If they haven't changed, create a sym link from the old directory to the new one e.g. {{{*}ln \\-s 3.0 3.2.1{*}}}\n" +
                "* The correct location is /opt/j2ee/atlassian/website/target/website/software/jira/docs/servers/jars\n" +
                "* FYI, if you log in as 'orion' and type 'ws', you just to the website directory.\n" +
                "{code}\n" +
                "maven jira:jars-jetty jira:jars-resin jira:jars-tomcat jira:jars-tomcat5\n" +
                "ssh -l orion zeus.atlassian.com mkdir /opt/j2ee/atlassian/website-app/new-website/website/web/software/jira/docs/servers/jars/3.2.1\n" +
                "scp release/jira-jars-*.zip orion@zeus.atlassian.com:/opt/j2ee/atlassian/website-app/new-website/website/web/software/jira/docs/servers/jars/3.2.1\n" +
                "{code}\n" +
                "{panel}\n" +
                "\nh3. Update plugins shipped with JIRA plugins\n\n" +
                "Currently only RPC is included with the JIRA build\n" +
                "\n" +
                "For each plugin:\n" +
                "{tasklist: RPC Plugin}\n" +
                "Rev. the versions of plugin in their *project.xml* files.\n" +
                "Test build with {{maven jar}}\n" +
                "Change the JIRA version that the plugin should be build against. Set the *build.jira.version* property in *project.properties* file to the new version of JIRA.\n" +
                "Tag to latest version\n" +
                "Update the JIRA's *project.properties* to include the correct cvs tags for the plugins (e.g. set the *plugin.rpcjiraplugin.cvstag* to rpc_jira_plugin_1_0)\n" +
                "{tasklist}\n" +
                "\nh3. Synchronise dependency versions\n\n" +
                "Run the Synchroniser plugin on the JIRA source, to ensure all the dependency versions are in synch. The synchroniser will:\n" +
                "# Run through all the dependent JAR's project.xml and ask you to whether to update to the version in jira's project.xml\n" +
                "# If a dependent's project.xml changes, a new revision number is asked for\n" +
                "# Once the first round is completed, you'll be asked for the updated CVS tag names for each of the modified dependency\n" +
                "# There will then be a second round of synchornisation, where dependency between the projects (e.g. something depending on atlassian-core) will be resolved.\n" +
                "\n" +
                "You need to keep track of *all* projects that were updated. You will need to build, deploy, check-in and tag all of those projects. ALso note that there is no *requirement* to upgrade the version, simply that it *should* be done. The more frequently this occurs, the less chance we have of versioning conflicts.\n" +
                "{panel}\n" +
                "export CVSROOT=:ext:cvs.atlassian.com:/cvsroot/atlassian\n" +
                "cvs co maven-plugins/\n" +
                "cd maven-plugins/atlassian-synchroniser-plugin\n" +
                "maven plugin:install\n" +
                "cd $JIRA\n" +
                "maven atlassian-synchroniser\n" +
                "{panel}\n" +
                "{tasklist: Synchronise dependencies}\n" +
                "Check out latest *HEAD* source dependencies\n" +
                "Run the *maven atlassian-synchroniser*\n" +
                "Build and deploy *{{maven jar:deploy}}* all projects that has been updated\n" +
                "Run *{{maven jar}}* for *jira* to ensure that the build is stil successful\n" +
                "Ensure that *atlassian.cvs.tag* matches *version* for all source dependency\n" +
                "Commit all changed projects\n" +
                "Tag all changed projects with the updated tags\n" +
                "{tasklist}\n" +
                "\nh3. Tag & Build new version of JIRA\n\n" +
                "Before you begin, make sure you have passwords for\n" +
                "* orion@www.atlassian.com (resches)\n" +
                "* j2ee@argus\n" +
                "* devuser@keg\n" +
                "{tasklist: Tag and Build}\n" +
                "Update *project.xml* with latest versioning info\n" +
                "Commit and tag JIRA to newest version\n" +
                "Login to *{{keg}}* & release JIRA (see below) NOTE: make sure you comment out the autobuild before you fire off the build 'crontab -e'\n" +
                "Run {{*maven jar:deploy*}} to create a JIRA jar and upload it to *repository.atlassian.com*\n" +
                "Comment back in the autobuild cron 'crontab -e'\n" +
                "{tasklist}\n" +
                "{panel:title=Releasing JIRA}\n" +
                "*Releasing JIRA*\n" +
                "$ ssh devuser@keg\n" +
                "\\*******************************************************\\*\n" +
                "Note: run 'sshagent' to preload the SSH passphrase\n" +
                "\\*******************************************************\\*\n" +
                "$ sshagent\n" +
                "Enter passphrase for /home/devuser/.ssh/id_dsa:\n" +
                "Identity added: /home/devuser/.ssh/id_dsa (/home/devuser/.ssh/id_dsa)\n" +
                "\\*******************************************************\\*\n" +
                "Note: run 'sshagent' to preload the SSH passphrase\n" +
                "\\*******************************************************\\*\n" +
                "$ rm \\-r jira\\*\n" +
                "$ cd /usr/local/jira_autobuild/\n" +
                "$ *./build_jira.sh \\-r atlassian_jira_3_2_beta*\n" +
                "{panel}\n" +
                "This will build, run tests and (if \\-r specified) upload the result to *devfiles* and the website.\n" +
                "{note}WMC 14/02/2005 - We need to update the scripts to take in passwords at the start so that it will run unassisted without password forwarding. {note}\n" +
                "{note} The process should copy the built files to {{/opt/j2ee/atlassian/website-static/software/jira/downloads/}}. You'll may have to do this manully if it fails - the files will be placed in /home/orion/ if it fails.{note}\n" +
                "{code:xml}\n" +
                "cp atlassian-jira-enterprise-3.2.1/atlassian-jira-enterprise-3.2.1.* /opt/j2ee/atlassian/website-static/software/jira/downloads\n" +
                "cp atlassian-jira-enterprise-3.2.1/atlassian-jira-enterprise-3.2.1-stand* /opt/j2ee/atlassian/website-static/software/jira/downloads\n" +
                "cp atlassian-jira-enterprise-3.2.1/atlassian-jira-enterprise-3.2.1-source* /opt/j2ee/atlassian/website-static/software/jira/downloads/source/enterprise\n" +
                "cp atlassian-jira-professional-3.2.1/atlassian-jira-professional-3.2.1.* /opt/j2ee/atlassian/website-static/software/jira/downloads\n" +
                "cp atlassian-jira-professional-3.2.1/atlassian-jira-professional-3.2.1-stand* /opt/j2ee/atlassian/website-static/software/jira/downloads\n" +
                "cp atlassian-jira-professional-3.2.1/atlassian-jira-professional-3.2.1-source* /opt/j2ee/atlassian/website-static/software/jira/downloads/source\n" +
                "cp atlassian-jira-standard-3.2.1/atlassian-jira-standard-3.2.1.* /opt/j2ee/atlassian/website-static/software/jira/downloads\n" +
                "cp atlassian-jira-standard-3.2.1/atlassian-jira-standard-3.2.1-stand* /opt/j2ee/atlassian/website-static/software/jira/downloads\n" +
                "cp atlassian-jira-standard-3.2.1/atlassian-jira-standard-3.2.1-source* /opt/j2ee/atlassian/website-static/software/jira/downloads/source\n" +
                "{code}\n" +
                "\nh3. Update Javadoc\n\n" +
                "{tasklist: Updating Javadoc}\n" +
                "{{*maven javadoc:deploy*}} (This uploads a jar of javadocs to the repository on Zeus)\n" +
                "{tasklist}\n" +
                "\nh3. Update doc links\n\n" +
                "{note}WMC 15/02/2005 - Not updated {note}\n" +
                "* Update the 'latest' symlink in api/ to point to api/. Check that [http://www.atlassian.com/software/jira/docs/api/latest/] works.\n" +
                "* In /opt/j2ee/atlassian/website-app/new-website/website/web/software/jira/docs, update the 'latest' symlink:\n" +
                "\n" +
                "rm latest ; ln \\-s vX.X latest\n" +
                "\n" +
                "Test that [http://www.atlassian.com/software/jira/docs/latest/] points to the latest docs.\n" +
                "\nh3. Update plugins jar and javadocs (if required)\n\n" +
                "For each plugin *update JIRA version*, run {{{*}maven jar:deploy dist:deploy-src javadoc:deploy site{*}}} to generate jar, source and javadocs. Also, update the [http://confluence.atlassian.com/display/JIRAEXT/] site if appropriate.\n" +
                "{tasklist: JIRA Plugins}\n" +
                "RPC Plugin\n" +
                "JIRA Toolkit\n" +
                "Perforce Plugin\n" +
                "SubVersion Plugin\n" +
                "JIRA Development Kit\n" +
                "{tasklist}\n" +
                "\nh3. Update Atlassian JIRA installations\n\n" +
                "{tasklist: http://support.atlassian.com upgrade}\n" +
                "[Backup JIRA data|http://support.atlassian.com/secure/admin/XmlBackup!default.jspa] to {{*/opt/java/webapps/support-backup/support-yyyyMMdd.xml*}}\n" +
                "Import the exported data into a local JIRA instance and test if it will start up *Make sure that the jira-toolkit.jar is in your installation*, you can download it from [here|http://confluence.atlassian.com/display/JIRAEXT/JIRA+Toolkit]\n" +
                "Copy the jira enterprise war from www.atlassian.com to jira.atlassian.com. SCP JIRA Enterprise WAR to {{/opt/java/src/jira-installations/deployments/wars}} (e.g. from www.atlassian.com {{*scp /home/orion/JIRA-3.2.1/atlassian-jira-enterprise-3.2.1/atlassian-jira-enterprise-3.2.1.tar.gz j2ee@jira.atlassian.com:/opt/java/src/jira-installations/deployments/wars/*}})and {{*tar -zxvf atlassian-jira-enterprise-3.2.1.tar.gz*}}\n" +
                "Update the */opt/java/src/jira-installations/deployments/wars/atlassian-jira-enterprise-3.2.1/webapp/WEB-INF/classes/entityengine.xml* as appropriate (see below)\n" +
                "Copy the webapp folder of atlassian-jira-enterprise-3.2.1 to atlassian-support-3.2.1 ({{*cp -r /opt/java/src/jira-installations/deployments/wars/atlassian-jira-enterprise-3.2.1/webapp /opt/java/webapps/atlassian-support-3.2.1*}})\n" +
                "Edit {{*/opt/java/webapps/atlassian-support-3.2.1/WEB-INF/classes/webwork.properties*}}, setting {{*webwork.multipart.maxSize=104857600*}}\n" +
                "Copy latest version of the {{*jira-toolkit*}} to {{*/opt/java/webapps/atlassian-support-3.2.1/WEB-INF/lib*}}\n" +
                "Backup PostGres Support data (see below)\n" +
                "E-mail [mailto:support@contegix.com] to say that support.atlassian.com is coming down & yell to all the dev team!\n" +
                "Login as *root* and stop the server {{*svc -d /var/svscan/atlassian-support*}}. Check that it has come down (*ps -ef | grep java*)\n" +
                "Remove temporary Orion files {{*rm -rf /etc/java/atlassian-support/application-deployments/default/atlassian-support*}}\n" +
                "Move sym link to new version ({{*cd /opt/java/webapps/ rm atlassian-support* and then *ln -fs atlassian-support-3.2.1 atlassian-support*}})\n" +
                "Restart the server {{*svc -u /var/svscan/atlassian-support*}} & monitor logs {{*tail -666f /var/log/java/atlassian-support/stdout.log*}}\n" +
                "Drop the [mailto:support@contegix.com] dudes another e-mail saying everything is up and hunky dory.\n" +
                "{tasklist}\n" +
                "{tasklist: http://jira.atlassian.com upgrade}\n" +
                "Copy the webapp folder of atlassian-jira-enterprise-3.2.1 to atlassian-jira-3.2.1 ({{*cp -r /opt/java/src/jira-installations/deployments/wars/atlassian-jira-enterprise-3.2.1/webapp /opt/java/src/jira-installations/deployments/atlassian-jira-3.2.1*}})\n" +
                "[Backup JIRA data|http://jira.atlassian.com/secure/admin/XmlBackup!default.jspa] to {{*/opt/java/src/jira-backup/jira-yyyyMMdd.xml*}}\n" +
                "Import the exported data into a local JIRA instance and test if it will start up (this takes too long atm, not recommended)\n" +
                "Backup PostGres JIRA data (see below)\n" +
                "E-mail [mailto:support@contegix.com] to say that jira.atlassian.com is coming down & yell to all the dev team!\n" +
                "Login as *root* and stop the server {{*svc -d /var/svscan/atlassian-jira*}}. Check that it has come down {{ps -ef | grep atlassian-jira}}\n" +
                "Remove temporary Orion files {{*rm -rf /etc/java/atlassian-jira/application-deployments/default/atlassian-jira*}}\n" +
                "Move sym link to new version ({{*cd /opt/java/src/jira-installations/deployments/ ln -fs atlassian-jira-3.2.1 atlassian-jira*}})\n" +
                "Restart the server {{*svc -u /var/svscan/atlassian-jira*}} & monitor logs {{*tail -666f /var/log/java/atlassian-jira/*.log*}}\n" +
                "Load up some pages in http://jira.atlassian.com and see how it goes\n" +
                "E-mail [mailto:support@contegix.com] to say that jira.atlassian.com is back up\n" +
                "{tasklist}\n" +
                "{panel:title=Backup PostGres data}\n" +
                "*Backup PostGres data*\n" +
                "*Backup PostGres data*\n" +
                "*Backup PostGres data*\n" +
                "*Backup PostGres data*\n" +
                "*Backup PostGres data*\n" +
                "su postgres\n" +
                "cd /var/lib/pgsql\n" +
                "pg_dump atlassian-support-restore > support_data_dump_20050531.sql\n" +
                "gzip \\-9 support_data_dump_20050531.sql\n" +
                "\n" +
                "su postgres\n" +
                "cd /var/lib/pgsql\n" +
                "pg_dump atlassian-jira > jira_data_dump_20050531.sql\n" +
                "gzip \\-9 jira_data_dump_20050531.sql\n" +
                "{panel}\n" +
                "{code:xml title=jira.atlassian.com entityengine snippet remove the 10 lines}\n" +
                "<datasource name=\"defaultDS\" field-type-name=\"postgres\"\n" +
                "schema-name=\"public\"\n" +
                "helper-class=\"org.ofbiz.core.entity.GenericHelperDAO\"\n" +
                "check-on-start=\"true\"\n" +
                "use-foreign-keys=\"false\"\n" +
                "use-foreign-key-indices=\"false\"\n" +
                "check-fks-on-start=\"false\"\n" +
                "check-fk-indices-on-start=\"false\"\n" +
                "add-missing-on-start=\"true\"\n" +
                "check-indices-on-start=\"true\">\n" +
                "<jndi-jdbc jndi-server-name=\"default\" jndi-name=\"jdbc/JiraDS\"/>\n" +
                "{code}\n" +
                "\nh3. Update website\n\n" +
                "Obviously you only need to do this once.\n" +
                "{tasklist: Setup your website dev environment}\n" +
                "Copy the atlassian-extras-0.7.1-encoders.jar from devfiles/releases/atlassian-extras\n" +
                "cvs up website run atlassian-idea\n" +
                "Create the website.properties file in the WEB-INF/classes\n" +
                "Create dummy file under websiteweb ewsheadlines.jsp. Do not check this in.\n" +
                "Comment out the tags in the file websitewebWEB-INFclassessecurity-config.xml\n" +
                "Add crap below to resin.xml\n" +
                "Updated entityengine.xml with the JNDI name jdbc/WebsiteDS *DO NOT CHECK THIS FILE IN!* Use\n" +
                "{code:xml}\n" +
                "\nh4. Upload and check\n\n" +
                "{note} As at 14/02/2005 - the process is to get [~dave@atlassian.com] to do it for you {note}\n" +
                "{tasklist: Build website}\n" +
                "website is on *Zeus* at */opt/j2ee/atlassian/website*\n" +
                "Update website from CVS {{*cvs -q update -dP as orion*}}\n" +
                "Build website on Zeus (run {{*maven war:webapp*}} in directory specified above)\n" +
                "Verify links work, events work, and download works.\n" +
                "{tasklist}\n" +
                "\nh3. Update Repository With Sources\n\n" +
                "{note}WMC 15/02/2005 - This section was not required for 3.2.1 thus un updated. Will probably need looking at {note}\n" +
                "* Check project.xml for any opensource dependencies that are patched. That is, if any dependency is patched by atlassian and the patch is not committed to the dependency's public CVS. The sources for such dependencies need to be zipped up and uploaded to a relevant directory in atlassian maven repository on zeus (under /var/www/html/repository).\n" +
                "Note: if the patch has been checked in, then JIRA users should be able to get the source form the public CVS themselves, and there is no need to upload the source to the repository.\n" +
                "\n" +
                "h3. Update versions on [http://support.atlassian.com]\n\n" +
                "Go to the admin section of support.atlassian.com, and add the newly released versions.\n" +
                "\nh3. Announce new release\n\n" +
                "{tasklist: Announcements}\n" +
                "Release Announcement on JIRA website - JIRA News blog\n" +
                "Release Announcement on frontpage - Atlassian News blog, category \"frontpage\"\n" +
                "Send announcement email to jira-user and jira-announce mailing lists. Format: ANN: JIRA x.y Released (!) For jira-announce, make sure it arrives! (check on NNTP) We have had problems with authorised posters\n" +
                "Post announcement on freshmeat, javalobby, The Server Side, Javaworld, Artima.com and Inria (see SitesToPostReleases for details)\n" +
                "{tasklist}");
    }

    public void testMacroAfterPre()
    {
        testMarkup("{code}\nfoo\n{code}\n{nosuch}\nx\n{nosuch}");
        testMarkup("{code:language=klingon}\nfoo\n{code}\n{nosuch}\nx\n{nosuch}");
    }

    public void testImageAfterMacro()
    {
        testMarkup("{panel}\nfoo\n{panel}\n!foo.jpg!");
    }

    public void testCodeHtml()
    {
        testMarkup("{code:html}\n<li>\n{code}");
    }

    // Make sure that whitespace in the code macro is respected (Tests for CONF-4490)
    public void testCodeWithWhitespace()
    {
        testMarkup("{code}\nhello\n    foo\n  bar\n{code}");
    }

    public void testNoformat()
    {
        testMarkup("{noformat}\nblah\nfoobar\n{noformat}");
    }

    // Make sure that whitespace in the noformat macro is respected (Tests for CONF-4490)
    public void testNoformatWithWhitespace()
    {
        testMarkup("{noformat}\nhello\n    foo\n  bar\n{noformat}");
    }

    public void testLinkAfterList()
    {
        testMarkup("| foo | bar |\n| xxx | * a\n* b \\\\\n[XXX] |\nmore text");
        testXHTML("<ul> <li>foo</li> </ul><a linktext=\"*Advantages* Over Standard _Wikis_|Advantages Over Standard Wikis\" linktype=\"raw\" title=\"Advantages Over Standard Wikis\" href=\"/confluence/display/SALES/Advantages Over Standard Wikis\"><b>Advantages</b> Over Standard <em>Wikis</em></a>",
                  "* foo\n\n[*Advantages* Over Standard _Wikis_|Advantages Over Standard Wikis]");
        testMarkup("| a | b |\n[YYY]");
    }

    public void testExclamationMark()
    {
        testMarkup("Foo\\! Bar");
        testMarkup("To: user\n" +
                "Subject: JIRA 2 t-shirt\\!\n" +
                "Message:");
    }

    public void testForcedBreakAfterMacro()
    {
        testMarkup("{note}\n" +
                "The JIRA staff will generally pick up the call without asking for an explanation.\n" +
                "{note}\n" +
                "\\\\\n" +
                "more text");
    }

    public void testEscapedLinesInTable()
    {
        testXHTML("<table class=\"confluenceTable\">\n" +
                "    <tbody>\n" +
                "        <tr>\n" +
                "            <td class=\"confluenceTd\"><b>Newsletter</b> </td>\n" +
                "            <td class=\"confluenceTd\"> Kindly sign us up for the Altassian  Partner Newsletter.<br clear=\"all\"/>\n" +
                "            <ul>\n" +
                "                <li>jaqueline@logon-int.com<br clear=\"all\"/></li>\n" +
                "                <li>anil@logon-int.com<br clear=\"all\"/></li>\n" +
                "                <li>philip@logon-int.com<br clear=\"all\"/></li>\n" +
                "                <li>ajay@logon-int.com<br clear=\"all\"/></li>\n" +
                "                <li>eddie.wong@logon-int.com</li>\n" +
                "            </ul>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td class=\"confluenceTd\"> <b>Questions or Comments</b> </td>\n" +
                "            <td class=\"confluenceTd\">\n" +
                "            <ul>\n" +
                "                <li>We would like to know the organization structure of Altassian.<br clear=\"all\"/></li>\n" +
                "                <li>Our Support Contact, Sales Contact and Marketing Contact.<br clear=\"all\"/></li>\n" +
                "                <li>Order Processing Procedures.</li>\n" +
                "            </ul>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </tbody>\n" +
                "</table>", "| *Newsletter* | Kindly sign us up for the Altassian  Partner Newsletter. " +
                "\\\\\n" +
                "* jaqueline@logon-int.com " +
                "\\\\\n" +
                "* anil@logon-int.com " +
                "\\\\\n" +
                "* philip@logon-int.com " +
                "\\\\\n" +
                "* ajay@logon-int.com " +
                "\\\\\n" +
                "* eddie.wong@logon-int.com |\n" +
                "| *Questions or Comments* | * We would like to know the organization structure of Altassian. " +
                "\\\\\n" +
                "* Our Support Contact, Sales Contact and Marketing Contact. " +
                "\\\\\n" +
                "* Order Processing Procedures. |");
    }

    public void testApostrophe()
    {
        testMarkup("[Sales' Access to Customer Reported Issues and the Status/ Resolution|#Sales' Access to Customer Reported Issues and the Status Resolution]");
    }

    public void testPreserveEscapeCharacters()
    {
        testMarkup("not \\[a link\\]");
        testMarkup("not a \\{macro\\}");
        testMarkup("- xxx-xxx not \\*bold\\* or \\_italic\\_ or \\-strike\\- or \\+under\\+ or \\{macro\\} or \\!image\\! or \\^super\\^ or \\~sub\\~ or \\|not\\|a\\|table\\|");
    }

    public void testPreserveBlankLinesInCodeMacro()
    {
        testMarkup("{code}\nfoo\n\nbar\n{code}");
    }

    public void testMonoSpace()
    {
        testMarkup("{{test text}}");
        testMarkup("{{test *bold* text}}");
        testMarkup("| a | {{b monospaced}} |\n| {{mono and *bold{*}}} | z |");
    }

    public void testBQ()
    {
        testMarkup("unquoted line\nbq. quoted line\nunquoted line");
    }

    public void testQuoteMacro()
    {
        testMarkup("{quote}\nA line\nAnother line\n{quote}");
    }

    public void testListHeadingList()
    {
        testMarkup("* foo\n* bar\n\nh1. heading\n\n* baz\n* xxx");
    }

    public void testAnchorInHeading()
    {
        testMarkup("h1. heading text {anchor:foo}\n\nfollowing paragraph");
    }

    public void testForcedNewlines()
    {
        testMarkup("text\n\\\\\n\\\\\n* list");
        testMarkup("text\n\\\\\n\\\\\ntext");
    }

    public void testAddMacroAfterTable()
    {
        testXHTML("<table class=\"confluenceTable\">\n" +
                "            <tbody>\n" +
                "                <tr>\n" +
                "                    <th class=\"confluenceTh\">xxx</th> <th class=\"confluenceTh\">yyy</th>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td class=\"confluenceTd\">1</td>\n" +
                "                    <td class=\"confluenceTd\">2</td>\n" +
                "                </tr>\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                " <div class=\"macro\">" +
                "        {quote}<br/>\n" +
                "        1<br/>\n" +
                "        2<br/>\n" +
                "        {quote} </div>", "|| xxx || yyy ||\n| 1 | 2 |\n{quote}\n1\n2\n{quote}");
    }

    public void testTwoTables()
    {
        testMarkup("|| a || b ||\n| a | b |\n\n|| x || y ||\n| x | y |");
    }

    private static final String EMOTICONS = ":-) :-( ;-) (y) (n) (i) (/) (x) (!) (+) (-) (?) (on) (off) (*y) (*r) (*g) (*b) (*y)";
    private static final String EMOTICONS2 = ":p :D";

    public void testMacroAfterTable()
    {
        testMarkup("|| a || b ||\n| c | d |\n{panel}\nIn a panel\n{panel}");
        testMarkup("|| Total || || || ||\n" +
                "{quote}\n" +
                "foo\n" +
                "bar\n" +
                "baz\n" +
                "{quote}");
    }

    public void testEmoticonsNextToStyledText()
    {
        String[] emoticons = EMOTICONS.split(" ");
        for (int i = 0; i < emoticons.length; ++i)
        {
            testEmoticonMarkup(emoticons[i] + "*bold words*" + emoticons[i], emoticons[i] + " *bold words*" + emoticons[i]);
        }
        emoticons = EMOTICONS2.split(" ");
        for (int i = 0; i < emoticons.length; ++i)
        {
            testEmoticonMarkup(emoticons[i] + "{*}bold words*" + emoticons[i], emoticons[i] + " *bold words*" + emoticons[i]);
        }
    }

    public void testColourInTable()
    {
        testMarkup("| {color:red}xyz{color} | {color:blue}abc{color} |\n| a | b |");
    }

    public void testColor()
    {
        testMarkup("foo{color:red}bar{color}baz");
        testMarkup("{color:#ffffff}bar{color}");
        testXHTML("<p style=\"color: rgb(50, 100, 22);\">the cat sat on the mat, thinking of cheese.</p>",
                  "{color:#326416}the cat sat on the mat, thinking of cheese.{color}");
        // FCK under IE does colour this way
        testXHTML("<font color=\"#326416\">the cat sat on the mat, thinking of cheese.</font>",
                  "{color:#326416}the cat sat on the mat, thinking of cheese.{color}");
        testXHTML("<div id=\"PageContent\" class=\"wiki-content\">\n" +
                "<p>\n" +
                "<span class=\"macro\" macrotext=\"{color:#ff6600}\" command=\"color\">\n" +
                "  <font color=\"#FF6600\">this</font>\n" +
                "</span> is a test</p></div>",
                  "{color:#ff6600}this{color} is a test");
    }

    public void testTextFollowedByListInTableCell()
    {
        testMarkup("| x | asd asdsad asdasd\n# foo\n# bar\n# baz |\n| aaa | bbb |");
    }

    public void testParagraphBeforeMacro()
    {
        testMarkup("foo bar baz\n{quote}\na b c\n{quote}");
    }

    public void testNestedLists()
    {
        testMarkup("# a\n" +
                "#* with\n" +
                "#* nested\n" +
                "#* bullet\n" +
                "# list\n" +
                "\n" +
                "* a\n" +
                "* bulletted\n" +
                "*# with\n" +
                "*# nested\n" +
                "*# numbered\n" +
                "* list");
    }

    public void testLinks()
    {
        testMarkup("random text [#anchor] more text");
        testMarkup("random text\n[#anchor]\nmore text");
        testMarkup("[^foo.jpg]");
        testMarkup("[Another Page]");
        testMarkup("[Another Page#anchor]");
        testMarkup("[Another Page^another.jpg]");
        //mockPageManager.matchAndReturn("getPage",C.anyArgs(2),null);
        //testMarkup("[Space:Another Page]");
        //testMarkup("[Space:Another Page#anchor]");
        //testMarkup("[Space:Another Page^anchor.jpg]");
        testMarkup("[link alias|#anchor|link tip]");
        testMarkup("[link alias|^attachment.ext|link tip]");

        testMarkup("[link alias|pagetitle|link tip]");
        testMarkup("[link alias|pagetitle#anchor|link tip]");
        testMarkup("[link alias|pagetitle^attachment.ext|link tip]");
        testMarkup("{anchor:anchorname}");
    }

    public void testHeadings()
    {
        testMarkup("random text\n\nh1. a heading\n\nmore text");
        testMarkup("* list\n* items\n\nh2. a heading\n\nmore text");
        testMarkup("random text\n\nh3. a heading\n\nmore text");
        testMarkup("random text\n\nh4. a heading\n\nmore text");
        testMarkup("random text\n\nh5. a heading\n\nmore text");
        testMarkup("| a | table |\n\nh6. a heading\n\nmore text");

    }

    public void testLineBreaks()
    {
        testMarkup("foo\nbar");
        testMarkup("foo\n\nbar");
        testMarkup("foo\n\n----\nbar");
        testMarkup("foo --- bar");
        testMarkup("foo -- bar");
    }

    public void testImageLink()
    {
        testMarkup("!foo.jpg!");
        //testMarkup("!foo.jpg|thumbnail!"); needs actual attachment
        testMarkup("!foo.jpg|vspace=4, align=right!");
        testMarkup("!foo.jpg|thumbnail, align=right!");
    }

    public void testBold()
    {
        testMarkup("this *is* a test");
    }

    public void testItalic()
    {
        testMarkup("this is a _test_");
    }

    public void testStrikethrough()
    {
        testMarkup("this -is- a test");
    }

    public void testUnderline()
    {
        testMarkup("+this is a test+");
    }

    public void testCombinations()
    {
        testMarkup("*bold and* *{_}italic{_}*");
    }

    public void testNumberedList()
    {
        testMarkup("# foo\n# bar\n# baz");
    }

    public void testBulletedList()
    {
        testMarkup("* foo\n* bar\n* baz");
    }

    public void testTable()
    {
        // test a simple table
        testMarkup("|| a || b || c ||\n| 1 | 2 | 3 |\n| | | |");
        // test a list in a table
        testMarkup("| * foo\n* bar\n* baz | xxx |\n| a | b |");
        // test an empty line in a table
        testMarkup("| first line \\\\\n\\\\\nthird line | xxx |\n" +
                "| a | b |");
        // test a list with following text in a table
        testMarkup("| * foo\n* bar\n* baz \\\\\n\\\\\n*bold test* | xxx |\n| a | b |");
        testMarkup("| asdasdasdasd | asdsadasd |\n" +
                "| asdasdasd | # a\n" +
                "# b\n" +
                "## c\n" +
                "## d\n" +
                "## e\n" +
                "# f\n" +
                "# g |\n" +
                "| | |");
    }

    public void testForcedNewlinesInTables()
    {
        testMarkup("| a | b |\n| c | d \\\\\ne \\\\ |");
    }

    public void testMacro()
    {
        testMarkup("{invalid_macro:with|arguments=foo}\n* this\n* shouldn't be interpreted\n{xyz}\n{invalid_macro}");
        testMarkup("{panel}\n|| x || y ||\n| 1 | 2 |\n{panel}");
    }

    public void testTextAfterList()
    {
        // as part of list
        testMarkup("* foo\n* bar\n\\\\\n*xxx*");
        // as seperate from list
        testMarkup("* foo\n* bar\n\n*xxx*");
        // two lists
        testMarkup("* foo\n\n* bar");
    }

    public void testListsAndTables()
    {
        testMarkup("| a | b |\n* foo\n* bar");
    }

    public void testUnknownMacrosPreserveLeadingAndTrailingNewlines()
    {
        testMarkup("{foo}a{foo}");
        testMarkup("{foo}\na{foo}");
        testMarkup("{foo}a\n{foo}");
        testMarkup("{foo}\na\n{foo}");
    }

    /**
     * Test that we can handle some of the nastier HTML produced for nested lists by FCKeditor
     */
    public void testTableXHTML()
    {
        testXHTML("<link type=\"text/css\" href=\"/confluence/styles/main-action.css?spaceKey=ds\" rel=\"stylesheet\"/>\n" +
                "<div id=\"Content\">\n" +
                "<table width=\"200\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\" align=\"\">\n" +
                "    <tbody>\n" +
                "        <tr>\n" +
                "            <td>x</td>\n" +
                "            <td>\n" +
                "            <ol>\n" +
                "                <li>a</li>\n" +
                "                <li>b</li>\n" +
                "                <li>c</li>\n" +
                "            </ol>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>x</td>\n" +
                "            <td>x</td>\n" +
                "        </tr>\n" +
                "    </tbody>\n" +
                "</table>\n" +
                "<br/>\n" +
                "</div>", "| x | " +
                "# a\n" +
                "# b\n" +
                "# c |\n| x | x |");
        testXHTML("<table width=\"200\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\" align=\"\">\n" +
                "    <tbody>\n" +
                "        <tr>\n" +
                "            <td>a</td>\n" +
                "            <td>b</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>c</td>\n" +
                "            <td>\n" +
                "            <ol>\n" +
                "                <li>a</li>\n" +
                "                <li>b</li>\n" +
                "                <ol>\n" +
                "                    <li>c</li>\n" +
                "                    <li>d</li>\n" +
                "                </ol>\n" +
                "                <li>e</li>\n" +
                "            </ol>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </tbody>\n" +
                "</table>\n",
                  "| a | b |\n" +
                          "| c | # a\n" +
                          "# b\n" +
                          "## c\n" +
                          "## d\n" +
                          "# e |");
        testXHTML("<table class=\"confluenceTable\">\n" +
                "    <tbody>\n" +
                "      <tr>\n" +
                "        <td class=\"confluenceTd\">x</td>\n" +
                "        <td class=\"confluenceTd\">\n" +
                "          <ol>\n" +
                "            <li>a</li>\n" +
                "            <li>b \n" +
                "            <ol>\n" +
                "              <li>xxxx</li>\n" +
                "              <li>as</li>\n" +
                "              <li>as</li>\n" +
                "              <li style=\"list-style: none\">\n" +
                "                <ol>\n" +
                "                  <li>q</li>\n" +
                "                  <li>w</li>\n" +
                "                  <li>e</li>\n" +
                "                  <li>r</li>\n" +
                "                  <li>t</li>\n" +
                "                </ol>\n" +
                "              </li>\n" +
                "              <li>y</li>\n" +
                "              <li>u</li>\n" +
                "              <li style=\"list-style: none\">\n" +
                "                <ol>\n" +
                "                  <li>i</li>\n" +
                "                </ol>\n" +
                "              </li>\n" +
                "              <li>zzzz \n" +
                "              <ol>\n" +
                "                <li>ccc</li>\n" +
                "                <li>ddd</li>\n" +
                "              </ol></li>\n" +
                "              <li>eee \n" +
                "              <ol>\n" +
                "                <li>f</li>\n" +
                "              </ol></li>\n" +
                "            </ol></li>\n" +
                "            <li>c \n" +
                "            <ol>\n" +
                "              <li>d</li>\n" +
                "              <li>xxxx</li>\n" +
                "              <li>e</li>\n" +
                "            </ol></li>\n" +
                "            <li>f</li>\n" +
                "          </ol>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "      <tr>\n" +
                "        <td class=\"confluenceTd\">x</td>\n" +
                "        <td class=\"confluenceTd\">x</td>\n" +
                "      </tr>\n" +
                "    </tbody>\n" +
                "  </table>", "| x | # a\n" +
                "# b\n" +
                "## xxxx\n" +
                "## as\n" +
                "## as\n" +
                "### q\n" +
                "### w\n" +
                "### e\n" +
                "### r\n" +
                "### t\n" +
                "## y\n" +
                "## u\n" +
                "### i\n" +
                "## zzzz\n" +
                "### ccc\n" +
                "### ddd\n" +
                "## eee\n" +
                "### f\n" +
                "# c\n" +
                "## d\n" +
                "## xxxx\n" +
                "## e\n" +
                "# f |\n" +
                "| x | x |"
        );
    }

    public void testEmoticons()
    {
        testMarkup(EMOTICONS);
        testMarkup(EMOTICONS2);
    }

    public void testEmoticonWithTextPutsWhiteSpace()
    {
        testXHTML("<p><img mce_src=\"/images/icons/emoticons/smile.gif\" src=\"/images/icons/emoticons/smile.gif\">test</p>",":-) test");
    }

    public void testNewlinesInLists()
    {
        testXHTML("<ul><li>\nitem\n</li></ul>", "* item");
        testXHTML("<ol type=\"1\">\n" +
                "    <li>\n" +
                "    <p>xxx</p>\n" +
                "    <p>yyy</p>\n" +
                "    </li></ol>", "# xxx\n\\\\\nyyy");
    }

    public void testMacroInHeading()
    {
        //testMarkup("h1. {excerpt}Foo{excerpt}");
    }

    public void testCodeMacroInList()
    {
        testMarkup("* a\n* list\n" +
                "{code}\n" +
                "maven jira:jars-jetty jira:jars-resin jira:jars-tomcat jira:jars-tomcat5\n" +
                "ssh -l orion zeus.atlassian.com mkdir /opt/j2ee/atlassian/website-app/new-website/website/web/software/jira/docs/servers/jars/3.2.1\n" +
                "scp release/jira-jars-*.zip orion@zeus.atlassian.com:/opt/j2ee/atlassian/website-app/new-website/website/web/software/jira/docs/servers/jars/3.2.1\n" +
                "{code}");
    }

    public void testMacroInTable()
    {
        testMarkup("|| foo || bar ||\n| {nomarkup}{xxx}{nomarkup} | {xxx} |");
    }

    public void testNoteAfterText()
    {
        testMarkup("xxx\n" +
                "{quote}\nyyy\n{quote}");
    }

    public void testTableAfterList()
    {
        testMarkup("* foo\n" +
                "* bar\n" +
                "* baz\n\n|| a || b ||\n" +
                "| c | d |");
    }

    public void testIntraWordStyles()
    {
        testMarkup("*foo{*}bar");
        testMarkup("foo{*}bar*");
        testMarkup("\\{*\\}");
        testMarkup("foo{_}bar{_}{*}baz{*}arg");
        testMarkup("{panel}\nfoo{*}bar{*}baz\n{panel}");
        testMarkup("foo{{{}bar{}}}baz");
        testMarkup("*foo bar ba{*}z");
    }

    public void testMultipleIntrawordStyles()
    {
        testMarkup("Create{*}{_}Interface{_}*");
    }

    public void testIntraWordXHTML()
    {
        testXHTML("<b>some <strike>te</strike>xt</b>", "*some* *{-}te{-}{*}{*}xt*");
    }

    // CONF-4544
    public void testLinksWithSpecialCharactersInTooltipAndAlias()
    {
        testMarkup("[Some Text|SomePage|Goto \"my\" homepage]");
        testMarkup("[Some \"Blah\" Text|SomePage]");
        testMarkup("[G�ttt]");
    }

    // CONF-4820, CONF-4889
    public void testDecoratedLinks()
    {
        testMarkup("-[Some link]-");
        testMarkup("*[Some link]*");
        testMarkup("_[Some link]_");
        testMarkup("+[Some link]+");
        testMarkup("^[Some link]^");
        testMarkup("~[Some link]~");
        testMarkup("??[Some link]??");
        testMarkup("{color:red}[Some link]{color}");
    }

    public void testStylesInLink()
    {
        testMarkup("[*foo* _bar_|link]");
    }

    private static class TestRecentlyUpdated extends org.radeox.macro.BaseMacro implements com.atlassian.renderer.macro.Macro
    {

        public String getName()
        {
            return "recently-updated";
        }

        public void execute(Writer writer, MacroParameter macroParameter) throws IllegalArgumentException, IOException
        {

        }
    }

    public void setUp() throws Exception
    {
        super.setUp();
        macroManager.registerMacro("excerpt",
                                   new BaseMacro()
                                   {

                                       public boolean isInline()
                                       {
                                           return true;
                                       }

                                       public boolean hasBody()
                                       {
                                           return true;
                                       }

                                       public RenderMode getBodyRenderMode()
                                       {
                                           return null;
                                       }

                                       public String execute(Map parameters, String body, RenderContext renderContext)
                                       {
                                           return body;
                                       }

                                       public boolean suppressSurroundingTagDuringWysiwygRendering()
                                       {
                                           return false;
                                       }

                                       public boolean suppressMacroRenderingDuringWysiwyg()
                                       {
                                           return true;
                                       }
                                   }
        );
        macroManager.registerMacro("recently-updated",new RadeoxCompatibilityMacro(new TestRecentlyUpdated()));
        macroManager.registerMacro("excerpt-include", new PanelMacro() {



            public boolean hasBody()
            {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean suppressMacroRenderingDuringWysiwyg()
                {
                    return true;
                }

        });
    }

    protected void tearDown() throws Exception
    {
        macroManager.unregisterMacro("excerpt");
        macroManager.unregisterMacro("recently-updated");
        macroManager.unregisterMacro("excerpt-include");
        super.tearDown();
    }

    /**
     * CONF-4916
      */
    public void testExcerptInTable()
    {

        testMarkup("| *Usage*: | \\{flowchart\\} |\n" +
                "| *Description*: | {excerpt}Allows display of flowcharts or other diagrams composed of shapes joined by lines.{excerpt} |");
    }

    public void testExcerpt()
    {
        testMarkup("{excerpt}foo bar{excerpt}");
    }

    public void testNewlinesAsWordSeperators()
    {
        testXHTML("this\nis\na test", "this is a test");
        testXHTML("<div class=\"macro\">{info}<br>From Confluence 1.5DR2 onwards you can use\n" +
                "URL=\"...\" attributes and the macro will generate an image map. You\n" +
                "can use Confluence links, so URL=\"[SomePage]\" will link to the\n" +
                "page SomePage.<br>{info}</div>", "{info}\nFrom Confluence 1.5DR2 onwards you can use URL=\"...\" attributes and the macro will generate an image map. You can use Confluence links, so URL=\"[SomePage]\" will link to the page SomePage.\n{info}");
    }

    /**
     * CONF-4865
     */
    public void testImageAfterRule()
    {
        testXHTML("<hr>\n" +
                "<p></p>\n" +
                "<div align=\"center\"><img src=\"/download/attachments/18/harbour.jpg\" imagetext=\"image.jpg|align=center\" border=\"0\"></div>\n" +
                "<p></p>", "----\n!image.jpg|align=center!");
    }

    // CONF-4919
    /*
    public void testColorTagsInLink()
    {
        testMarkup("[{color:blue}Foo{color}|Bar]");
    }
    */

    // CONF-4920
    public void testColorTagsSurroundingLink()
    {
        testMarkup("{color:red}[Some link]{color}");
    }

    public void testLinkEditedInWysiwyg()
    {
        testXHTML(
                "<p><a href=\"/display/ds/Confluence+Overview\"  title=\"Confluence Overview\" linktype=\"raw\" linktext=\"Foo|Confluence Overview\">Bar</a></p>",
                "[Bar|Confluence Overview]"
        );
    }

    public void testLinksInNestedList()
    {
        testMarkup("# [a]\n" +
                "## [b]\n" +
                "### [c]");
    }

    public void testInlineHTML()
    {
        testMarkup("{html}\n<P>some text</P>\n{html}");
        testMarkup("{html}\n<IFRAME frameborder=\"0\" height=\"674\" noborder src=\"http://wiki.riptown.com/applications/ResourceLocator/Seymour10.svg\" width=\"1124\"></IFRAME>\n{html}");
    }

    public void testBrInTableHTML()
    {
        testXHTML("<table class=\"confluenceTable\"><tbody>\n" +
                "<tr>\n" +
                "<th class=\"confluenceTh\"> b&nbsp; </th>\n" +
                "<th class=\"confluenceTh\"> b <br>&nbsp; </th>\n" +
                "</tr>\n" +
                "</tbody></table>", "|| b&nbsp; || b \\\\\n&nbsp; ||");
        testXHTML("<table class=\"confluenceTable\"><tbody>\n" +
                "<tr>\n" +
                "<th class=\"confluenceTh\"> b</th>\n" +
                "<th class=\"confluenceTh\"> b <br>c</th>\n" +
                "</tr>\n" +
                "\n" +
                "<tr>\n" +
                "<td class=\"confluenceTd\">d </td>\n" +
                "\n" +
                "<td class=\"confluenceTd\">e </td>\n" +
                "</tr>\n" +
                "</tbody></table>", "|| b || b \\\\\nc ||\n| d | e |");
    }

    public void testHRWithFollowingThings()
    {
        testMarkup("----\n{panel}\n\n{panel}");
        testMarkup("----\nh1. A Heading");
        testMarkup("----\na test");
        testMarkup("----\n* a test");
        testMarkup("----\n# a test");
        testMarkup("----\n| a test |");

    }

    public void testEscapingAlreadyEscaped()
    {
        testMarkup("p\\\\\\!d");
        testMarkup("p\\\\\\|d");
        testMarkup("p\\\\\\[d");
        testMarkup("p\\\\\\]d");
    }

    public void testMailto()
    {
        testMarkup("[mailto:foo@bar.com]");
        testMarkup("[Send an email to foo|mailto:foo@bar.com]");
        testMarkup("[Send an email to foo|mailto:foo@bar.com|a useless tip]");
        testMarkup("[Send an *email* to foo|mailto:foo@bar.com]");
        testMarkup("[_Send an email to foo_|mailto:foo@bar.com|a useless tip]");

        testXHTML("<p><span class=\"nobr\"><a href=\"mailto:foo@bar.com\"\n" +
                " title=\"Send mail to Send an +email+ to foo\"linktype=\"raw\" linktext=\"Send an +email+ to foo|mailto:foo@bar.com\">Send an <b>email</b> to foo<sup><img class=\"rendericon\" src=\"http://localhost:8080/images/icons/mail_small.gif\" height=\"12\" width=\"13\" align=\"absmiddle\" alt=\"\" border=\"0\"/></sup></a></span>&#8201;</p>",
                  "[Send an *email* to foo|mailto:foo@bar.com]");
    }

    public void testListWithEmptyItem()
    {
        testXHTML("<ul>\n" +
                "<li>foo</li>\n" +
                "<li><br></li>\n" +
                "<li>&nbsp;</li>\n" +
                "</ul>\n",
                  "* foo\n* &nbsp;\n* &nbsp;");
    }

    public void testMoreListInTable()
    {
        testXHTML("\n" +
                "<table class=\"confluenceTable\"><tbody>\n" +
                "<tr>\n" +
                "<td class=\"confluenceTd\">x&nbsp;</td>\n" +
                "<td class=\"confluenceTd\">&nbsp;y</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td class=\"confluenceTd\">\n" +
                "<ul>\n" +
                "<li>foo</li>\n" +
                "<li><br></li>\n" +
                "<li>&nbsp;</li>\n" +
                "</ul>\n" +
                "</td>\n" +
                "<td class=\"confluenceTd\">&nbsp;</td>\n" +
                "</tr>\n" +
                "</tbody></table>",
                  "| x | y |\n| * foo\n* &nbsp;\n*  | |");
    }

    public void testRadeoxMacroWithNoBody()
    {
        testMarkup("{recently-updated}");
    }

    public void testUrlWhichIsnt()
    {
        testMarkup("*Profile:*");
    }

    public void testAnchorLinks()
    {
        testMarkup("{anchor:foo}\n[#foo]");
    }

    public void testBrInList()
    {
        testXHTML("<ul>\n" +
                "\n" +
                "<li>foo\n" +
                "\n" +
                "<ul>\n" +
                "\n" +
                "<li>bar<br></li>\n" +
                "<li>baz</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "</ul>",
                  "* foo\n** bar\n** baz");
        testXHTML("<ul>\n" +
                "\n" +
                "<li>foo\n" +
                "\n" +
                "<ul>\n" +
                "\n" +
                "<li>bar<br><br></li>\n" +
                "<li>baz</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "</ul>",
                  "* foo\n** bar\n** baz");
    }

    public void testExtraSpaceCreation()
    {
        testMarkup("{quote}\nThis is a quote\n{quote}\n" +
                "This is a paragraph.\n" +
                "{note}This is a note{note}\n" +
                "This is another paragraph.");
    }

    public void testMoreExtraSpaceCreation()
    {
        testXHTML("<ol>\n" +
                "\t\n" +
                "<li>No formatting\n" +
                "<div class=\"macro\" macrotext=\"{noformat}\" command=\"noformat\"><div class=\"preformatted\"><div class=\"preformattedContent\">\n" +
                "<pre>something not formatted\n" +
                "</pre>\n" +
                "</div></div></div>\n" +
                "<p> </p>\n" +
                "</li>\n" +
                "\t\n" +
                "<li>Something else not formatted\n" +
                "<div class=\"macro\" macrotext=\"{noformat}\" command=\"noformat\"><div class=\"preformatted\"><div class=\"preformattedContent\">\n" +
                "<pre>something else not formatted\n" +
                "</pre>\n" +
                "</div></div></div>\n" +
                "<p> </p>\n" +
                "</li>\n" +
                "</ol>",
                  "# No formatting\n" +
                          "{noformat}\n" +
                          "something not formatted\n" +
                          "{noformat}\n" +
                          "# Something else not formatted\n" +
                          "{noformat}\n" +
                          "something else not formatted\n" +
                          "{noformat}");
    }

    public void testCONF5492()
    {
        testXHTML("<table class=\"confluenceTable\"><tbody>\n" +
                "<tr>\n" +
                "<th class=\"confluenceTh\">&nbsp;</th>\n" +
                "<th class=\"confluenceTh\"> test </th>\n" +
                "</tr>\n" +
                "\n" +
                "<tr>\n" +
                "<td class=\"confluenceTd\"> test </td>\n" +
                "\n" +
                "<td class=\"confluenceTd\"> test </td>\n" +
                "</tr>\n" +
                "\n" +
                "<tr>\n" +
                "<td class=\"confluenceTd\"> test </td>\n" +
                "\n" +
                "<td class=\"confluenceTd\">\n" +
                "<ul>\n" +
                "\t\n" +
                "<li>mangle<br></li>\n" +
                "<li>row2</li>\n" +
                "</ul>\n" +
                "</td>\n" +
                "</tr>\n" +
                "\n" +
                "<tr>\n" +
                "<td class=\"confluenceTd\"> test </td>\n" +
                "\n" +
                "<td class=\"confluenceTd\"> test </td>\n" +
                "</tr>\n" +
                "</tbody></table>",
                  "|| || test ||\n" +
                          "| test | test |\n" +
                          "| test | * mangle\n" +
                          "* row2 |\n" +
                          "| test | test |");
    }

    public void testCONF5044TrimmingNbspInTableCells()
    {
        testXHTML("<table class=\"confluenceTable\"><tbody>\n" +
                "<tr>\n" +
                "<th class=\"confluenceTh\">&nbsp;</th>\n" +
                "<th class=\"confluenceTh\">&nbsp;test</th>\n" +
                "</tr>\n" +
                "\n" +
                "<tr>\n" +
                "<td class=\"confluenceTd\">&nbsp;test&nbsp;</td>\n" +
                "\n" +
                "<td class=\"confluenceTd\">test&nbsp;</td>\n" +
                "</tr>\n" +
                "\n" +
                "</tbody></table>",
                "|| || test ||\n| test | test |");
    }

    public void testCONF5829WordTableImport()
    {
        testXHTML("<table class=\"MsoNormalTable\" style=\"border: medium none ; width: 430.85pt; margin-left: 36pt; border-collapse: collapse;\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\" width=\"574\">\n" +
                " <tbody>\n" +
                "<tr style=\"page-break-inside: avoid;\">\n" +
                "  \n" +
                "<td style=\"border: 1pt solid windowtext; padding: 0cm 3.5pt; width: 18.5pt;\" valign=\"top\" width=\"25\">\n" +
                "  \n" +
                "<p class=\"MsoNormal\" style=\"margin-left: 0cm; text-indent: 0cm;\"><!--[if !supportLists]--><span style=\"\" lang=\"DE\"><span style=\"\">1.<span style=\"font-family: &quot;Times New Roman&quot;; font-style: normal; font-variant: normal; font-weight: normal; font-size: 7pt; line-height: normal; font-size-adjust: none; font-stretch: normal;\"> </span></span></span><!--[endif]--><span lang=\"DE\"><o:p>&nbsp;</o:p></span></p>\n" +
                "  </td>\n" +
                "  \n" +
                "<td style=\"border-style: solid solid solid none; border-color: windowtext windowtext windowtext -moz-use-text-color; border-width: 1pt 1pt 1pt medium; padding: 0cm 3.5pt; width: 273.9pt;\" valign=\"top\" width=\"365\">\n" +
                "  \n" +
                "<p class=\"MsoNormal\"><span lang=\"DE\">Ausw�hlen einer SWD-</span><span style=\"\" lang=\"DE\">Sachgruppe </span><i style=\"\"><span lang=\"DE\">(Feld 810</span></i><span lang=\"DE\">),<br>\n" +
                "  z.B. Pharmazie</span></p>\n" +
                "  \n" +
                "<p class=\"MsoNormal\"><span lang=\"DE\"><span style=\"\">&nbsp;&nbsp;&nbsp;&nbsp; </span></span><span style=\"\" lang=\"EN-GB\">f sn 27.8a und nad s<span style=\"\">&nbsp;&nbsp;&nbsp; </span><o:p></o:p></span></p>\n" +
                "  \n" +
                "<p class=\"MsoNormal\"><span style=\"\" lang=\"EN-GB\"><o:p>&nbsp;</o:p></span></p>\n" +
                "  \n" +
                "<p class=\"MsoNormal\"><span lang=\"DE\">Es erfolgt die Anzeige des Treffer-SET der\n" +
                "  Ts-S�tze in der Kurzanzeige;</span></p>\n" +
                "  \n" +
                "<p class=\"MsoNormal\"><span lang=\"DE\"><o:p>&nbsp;</o:p></span></p>\n" +
                "  </td>\n" +
                "  \n" +
                "<td style=\"border-style: solid solid solid none; border-color: windowtext windowtext windowtext -moz-use-text-color; border-width: 1pt 1pt 1pt medium; padding: 0cm 3.5pt; width: 138.45pt;\" valign=\"top\" width=\"185\">\n" +
                "  \n" +
                "<p class=\"MsoNormal\"><b style=\"\"><span lang=\"DE\">CrissSet<br style=\"\">\n" +
                "  <!--[if !supportLineBreakNewLine]--><br style=\"\">\n" +
                "  <!--[endif]--><o:p></o:p></span></b></p>\n" +
                "  \n" +
                "<p class=\"MsoNormal\"><span lang=\"DE\"><o:p>&nbsp;</o:p></span></p>\n" +
                "  </td>\n" +
                " </tr>\n" +
                "</tbody></table>"
                ,"| 1. | Ausw�hlen einer SWD-Sachgruppe _(Feld 810_), \\\\\n" +
                "z.B. Pharmazie \\\\\n" +
                "&nbsp;&nbsp;&nbsp;&nbsp; f sn 27.8a und nad s&nbsp;&nbsp;&nbsp; \\\\\n" +
                "\\\\\n" +
                "Es erfolgt die Anzeige des Treffer-SET der   Ts-S�tze in der Kurzanzeige; \\\\ | *CrissSet* \\\\\n" +
                "\\\\\n" +
                "\\\\ |");
    }

    public void testYetAnotherTableThing()
    {
        testMarkup("| a | b \\\\\n" +
                "c \\\\\n" +
                "d |\n" +
                "| e | f \\\\ |");
    }

    public void testColoursAndLists()
    {
        testMarkup("- {color:#ef7224}*[Planning]*{color}\n" +
                "-* {color:#ef7224}[Concerto Planning]{color}");
    }

    public void testImageParametersInLink()
    {
        testMarkup("[!jiraSupportThumbnail.png|width=397,height=194!|~admin]");
    }

    // CONF-6701
    public void testQuotedImageSizes()
    {
        testMarkupWhichShouldntBePreserved("!priority_critical.gif|width=\"16\", height=\"16\"!","!priority_critical.gif|width=16, height=16!");
    }

    //CONF-6508
    public void testBulletsInsideTable()
    {
        testXHTML("<table class=\"confluenceTable\"><tbody>\n" +
            "<tr>\n" +
            "<td class=\"confluenceTd\">A</td>\n" +
            "<td class=\"confluenceTd\">B</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "\n" +
            "<td class=\"confluenceTd\"><p>a</p><ul><li>1</li><li>2</li><li>3&nbsp;</li></ul></td>\n" +
            "<td class=\"confluenceTd\">b</td>\n" +
            "</tr>\n" +
            "</tbody></table>"
            ,
            "| A | B |\n" +
            "| a\n" +
            "* 1\n" +
            "* 2\n" +
            "* 3 | b |");
    }

    // CONF-6527
    public void testLeadingSpacesOnFirstLineOfNoFormat()
    {
        testMarkup("{noformat}\n   xxx\n{noformat}");
    }

    // CONF-5496
    public void testAliasesAreCleanedUp()
    {
        testMarkupWhichShouldntBePreserved("[foo|#foo]","[#foo]");
        testMarkupWhichShouldntBePreserved("[foo@bar.com|mailto:foo@bar.com]","[mailto:foo@bar.com]");
        testMarkupWhichShouldntBePreserved("[foo|^foo]","[^foo]");
        testMarkup("[foo|^food]");
    }
    // CONF-6995
    public void testImageAfterText()
    {
        testXHTML(
                "xxx<div align=\"center\"><img src=\"/download/attachments/18/harbour.jpg\" imagetext=\"image.jpg|align=center\" border=\"0\"></div>\n" +
                "<p></p>", "xxx !image.jpg|align=center!");
        testMarkup("xxx\n!image.jpg!");
    }

    // CONF-7085
    public void testNestedListsInTables()
    {
        testMarkup("* Point One\n" +
            "| * a | b |\n" +
            "* Point Two");

        testMarkup("Point One\n" +
            "| * a\n" +
            "** b | c |");
    }

    public void testNestedListsInWikiSrcDivs()
    {
        testXHTML(
            "<ul>\n" +
            "<li> Point One\n" +
            "<div class=\"wikisrc\">{section}</div>\n" +
            "<ul>\n" +
            "<li>food and drink</li>\n" +
            "<li>washrooms</li>\n" +
            "</ul>\n" +
            "<div class=\"wikisrc\">{section}</div>\n" +
            "</li>\n" +
            "</ul>", "* Point One\n{section}\n* food and drink\n* washrooms{section}");
    }

    public void testNestedListsHTML()
    {
        testXHTML("<ul>\n" +
                "<li>foo</li>\n" +
                "<ul>\n" +
                "<li>bar</li>\n" +
                "</ul>\n" +
                "</ul>", "* foo\n** bar");
    }

    public void testLists()
    {
        testMarkup(
            "* a\n" +
            "** aa\n" +
            "** aaa\n" +
            "* b\n" +
            "** b\n" +
            "** bb");
    }

    public void testInvalidLink()
    {
        testXHTML("<a class=\"linkerror\" linktext=\"foo|bar\" linktype=\"raw\" title=\"foo|bar\" href=\"http://localhost:8080/pages/\">foo|bar</a>", "[foo|bar]");
    }

    public void testEmptyPara()
    {
        testXHTML("<div class=\"macro\" command=\"code\" macrotext=\"{code}\">\n" +
                "<div class=\"code\">\n" +
                "<div class=\"codeContent\">\n" +
                "<pre class=\"code-java\">\n" +
                "<span class=\"code-keyword\">this</span> is codexxx\n" +
                "</pre>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "<p/>",
                "{code}\nthis is codexxx\n{code}");
    }

    public void testShortcutLinksWithTitles()
    {
        testXHTML("<span class=\"nobr\">\n" +
                "<a linktext=\"CONF-7717@jira\" linktype=\"raw\" mce_href=\"http://jira.atlassian.com/browse/?CONF-7717\" href=\"http://jira.atlassian.com/browse/?CONF-7717\">\n" +
                "JIRA Issue CONF-7717\n" +
                "<sup>\n" +
                "<img class=\"rendericon\" width=\"7\" height=\"7\" border=\"0\" align=\"absmiddle\" alt=\"\" mce_src=\"/images/icons/linkext7.gif\" src=\"/images/icons/linkext7.gif\"/>\n" +
                "</sup>\n" +
                "</a>\n" +
                "</span>", "[CONF-7717@jira]");
    }


    public void testNewlinesInCode()
    {
        // this is the XHTML produced in Firefox when you insert a newline in a code block
        testXHTML("<div class=\"macro\" macrotext=\"{code}\" command=\"code\"><div class=\"code\"><div class=\"codeContent\">\n" +
                "<pre class=\"code-java\">a</pre>\n" +
                "<pre class=\"code-java\">\n" +
                "b</pre>\n" +
                "</div></div></div>", "{code}\na\n\nb\n{code}");
    }

    public void testAddingTextBelowListInTable()
    {
        testXHTMLWithoutTestingMarkupStability("<table class=\"confluenceTable\"><tbody>\n" +
                "<tr>\n" +
                "<td class=\"confluenceTd\">\n" +
                "<ul>\n" +
                "\t\n" +
                "<li>a</li>\n" +
                "\t\n" +
                "<li>b</li>\n" +
                "</ul>\n" +
                "c<br>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</tbody></table>","| * a\n* b\\\\\n c \\\\ |");
    }

    /**
     * EDITLIVE-16: tables should work if they aren't surrounded by a <p>
     */
    public void testBareTable()
    {
        testXHTML("<table class=\"confluenceTable\"><tbody> \n" +
                "<tr> \n" +
                "<td class=\"confluenceTd\">&nbsp;</td> \n" +
                "\n" +
                "<td class=\"confluenceTd\">&nbsp;</td> \n" +
                "</tr> \n" +
                "\n" +
                "<tr> \n" +
                "<td class=\"confluenceTd\">&nbsp;</td> \n" +
                "\n" +
                "<td class=\"confluenceTd\">&nbsp;</td> \n" +
                "</tr> \n" +
                "</tbody></table>\n<p>&nbsp;</p> ","| | |\n| | |\n\\\\");
    }

    /**
     * CONF-7333
     */
    public void testTwoLists()
    {
        testXHTML("<ul><li>foo</li></ul><br><ul><li>baz<br></li></ul>", "* foo\n\n* baz");
    }
    /**
     * CONF-8282 - test horizontal rule before macro
     */
    public void testHorizontalRuleBeforeMacro()
    {
        testXHTML("<hr/>\n" +
                "<span class=\"macro\">{anchor:top_of_page}</span>","----\n" +
                "{anchor:top_of_page}");
    }

    /**
     * CONF-7985 - test that a code macro containing &lt;p&gt; tags correctly adds newlines
     */
    public void testParagraphsInCodeMacro()
    {
        testXHTML("<div class=\"macro\" macrotext=\"{code}\" command=\"code\"><div class=\"code\"><div class=\"codeContent\"><pre class=\"code-java\"><p>foo</p><p>bar</p></pre></div></div></div>","{code}\nfoo\nbar\n{code}");
    }

    /**
     * CONF-8445 - test that image size params are correctly parsed into markup 
     */
    public void testImageWithSizeParams()
    {
        testXHTML("<img src=\"download/attachments/4522034/computer.png\" imagetext=\"computer.png\" align=\"absmiddle\" " +
                "border=\"0\" height=\"155\" width=\"155\">", "!computer.png|width=155,height=155!");

        testMarkup("!computer.png|width=155,height=155!");
    }
}
