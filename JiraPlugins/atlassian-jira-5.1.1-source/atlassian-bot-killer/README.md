# Atlassian Bot Session Killer

This plugin will kill sessions for requests that exhibit Bot like behavior

It does this by doing the following :

* If the request has a user associated, then its left alone
* If the HttpSession has NO marker attribute in it then\

** The inactivity timeout is screwed down to 1 minute
** A Bot typically wont be back and hence this will quickly timeout

* If the HttpSession has the marker attribute then
** This must be the second request for that session
** The inactivity timeout is restored to the original value
** A bot does not typically preserve cookies and hence this is a real session