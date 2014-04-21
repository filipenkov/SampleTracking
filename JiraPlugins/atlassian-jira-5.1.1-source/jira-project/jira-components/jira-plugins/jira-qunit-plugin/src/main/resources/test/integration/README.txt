This directory contains sub-directories that contain QUnit Integration tests.

These tests run against a JIRA instance but are limited to a single screen and should run very
quickly (e.g. server calls within the page should be mocked where possible)

Sub-directories should be created for each different JIRA page to be tested, e.g. issue edit / create,
navigator, dashboard, etc.

Test files in the subdirectories should be of the form test-*.js