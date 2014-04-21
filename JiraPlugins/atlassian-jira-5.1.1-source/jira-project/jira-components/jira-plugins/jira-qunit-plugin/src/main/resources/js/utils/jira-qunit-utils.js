/**
 * JIRA-specific utilities for QUnit integration tests.
 */
(function($) {
    QUtil.JIRA = {

        /**
         * Provides an OK response for a Sinon fakeServer call, with the Dialog control's magic 'DONE' header.
         * @param body the body of the response, if undefined an empty string will be used
         */
        dialogOk: function (body) {
            return [200, {"X-Atlassian-Dialog-Control": 'DONE'}, body || ''];
        }
    };
}(AJS.$));