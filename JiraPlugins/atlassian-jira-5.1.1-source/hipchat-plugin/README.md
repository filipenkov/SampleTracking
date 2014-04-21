# HipChat JIRA Plugin

This is a simple JIRA 5 compatible-plugin that allows you to notify one or more HipChat rooms based on a JIRA workflow post function and a JQL expression.

I'll likely be adding more integrations over time. If you'd like to contribute, feel free to fork and send me a pull request.

# Sample Usage

A common request is something along these lines:

	"I want to notify my dev team about new Blocker issues."

This plugin can allow you to do that in a very flexible manner - by combining any workflow transition and any JQL expression to notify your team.

In the example above:

* add an **Admin** HipChat API token to **Administration** > **System** > **Security** > **HipChat Configuration** (you only need to do this once per JIRA install)
* add the **Notify Hipchat** post-function to the **Create Issue** transition (or any other transition you want) -- if you choose to assign it to the Create Issue transition, make sure the HipChat post function is the last one in the list, else you'll be greeted by an error.
* choose one or more **Rooms** you wish to notify
* complete the **JQL** field with **priority = "Blocker"** (or any JQL expression - the issue must return "true" to this expression)

Create a blocker issue, and it will be posted to your selected chatroom - voila!
