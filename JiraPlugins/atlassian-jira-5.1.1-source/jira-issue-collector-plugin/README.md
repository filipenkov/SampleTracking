# JIRA Issue Collector Plugin

## Overview
The issue collector plugin makes creating issues easy.  If you are developing a web-application and want your users to raise bugs in JIRA the issue collector will make this a very painless process.

Instead of having to navigate to JIRA to raise bugs users will be able to raise issues directly from within your web-application!

Currently creating issues in JIRA requires users to:

* Navigate to your JIRA instance first
* Signup/login
* Create the issue

The issue collector reduces this to one step:

* In your web-application click on a trigger provided by the collector and raise an issue!

All you have to do is to configure a collector and  copy the javascript provided to you into your web-application.

Users will then see a trigger they can click to raise bugs and will get a form directly in your webapp to provide more details.  They can do so anonymously or if they are already logged in in the JIRA instance where the feedback will be created than their login credentials can be used.

## Building this plugin

This plugin is built with the Plugins SDK (more information available at in our [Developer Docs](https://developer.atlassian.com/)).
If youre using Intellij Idea simply import the pom.xml file to get started developing.

To run the plugin type:
*atlas-debug*

This will start an instance of JIRA (with debug parameters so you can connect via a remote debugger) with the plugin
deployed.
