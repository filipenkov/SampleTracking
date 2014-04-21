AJS.$(function(){
    AJS.$.fn.isDirty = function(){}; // disable dirty form check

    // Generic submit helper for all steps of the setup process

        // Prevent double submitting of the form and handle the other buttons
        AJS.$("#jira-setupwizard").submit(function(){
            AJS.$("#jira-setupwizard-submit, #jira-setupwizard-test-connection, #jira-setupwizard-test-mailserver-connection").attr('disabled','disabled');

            //JRADEV-6428: Disable the language flags while PICO is starting up. If we don't we can get deadlock if the
            //user clicks "next" on the DB screen and then clicks on a flag while the db is being setup.
            var serverLanguageSelectList = AJS.$("#jira-setupwizard-server-language");
            if (serverLanguageSelectList.length > 0) {
                serverLanguageSelectList.attr('disabled', 'disabled');
            }

            if (AJS.$('input[name=changingLanguage]').val() == "false" && AJS.$('input[name=testingConnection]').val() == "testconnection") {
                AJS.$('#test-connection-throbber').removeClass('hidden');
            } else if (AJS.$('input[name=changingLanguage]').val() === "false") {
                AJS.$('#submit-throbber').removeClass('hidden');
            }
            AJS.$('input[name=nextStep]').val("true");
            AJS.$('.throbber-message').removeClass('hidden');
        });

    // Step 1 of 4 - setup-db.jsp

        // Internal/External Database toggle
        AJS.$('input[name=databaseOption]').change(function() {
            var isExternal = AJS.$(this).val() == "EXTERNAL",
                testbutton = AJS.$('#jira-setupwizard-test-connection'),
                externalFields = AJS.$('#setup-db-external');

            if (isExternal) {
                externalFields.removeClass('hidden');
                testbutton.removeClass('hidden');
            } else {
                externalFields.addClass('hidden');
                testbutton.addClass('hidden');
            }
        });

        // Used to prefill fields on the page depending on the databaseType option selected
        var dbPrefills = { ports: {}, schemas: {} };

        dbPrefills.ports['postgres72'] = '5432';
        dbPrefills.schemas['postgres72'] = 'public';

        dbPrefills.ports['mysql'] = '3306';
        dbPrefills.schemas['mysql'] = '';

        dbPrefills.ports['oracle10g'] = '1521';
        dbPrefills.schemas['oracle10g'] = '';

        dbPrefills.ports['mssql'] = '1433';
        dbPrefills.schemas['mssql'] = 'dbo';

        // Set the initial show/hide state of the fields - for cases where the page posts back (validation failed, test connection, language change, etc)
        if (AJS.$('input[name=databaseOption]:checked').val() == "EXTERNAL") {
            AJS.$('#setup-db-external, #jira-setupwizard-test-connection').removeClass('hidden');
        }

        function showDbFields(){
            var db = AJS.$('select[name=databaseType]').val();
            if (db !== ""){
                AJS.$('.setup-fields').addClass('hidden');
                AJS.$('.db-option-'+db).removeClass('hidden');
            }
        }
        showDbFields();

        // Database Type toggle
        AJS.$('select[name=databaseType]').change(function(){
            var selectedDatabase = AJS.$(this).val(),
                jdbcField = AJS.$("input[name='jdbcPort']"),
                schemaField = AJS.$("input[name='schemaName']");

            jdbcField.val(dbPrefills.ports[selectedDatabase]);
            schemaField.val(dbPrefills.schemas[selectedDatabase]);
            
            showDbFields();
        });

        // For testing the connection
        AJS.$("#jira-setupwizard-test-connection").click(function() {
            AJS.$("input[name=changingLanguage]").val("false");
            AJS.$("input[name=testingConnection]").val("true");
            AJS.$("#jira-setupwizard").submit();
        });

        // For changing the language
        AJS.$('#jira-setupwizard-server-language').change(function()
        {
            var lang = AJS.$(this).val();
            AJS.$("input[name=language]").val(lang);
            AJS.$("input[name=changingLanguage]").val("true");
            AJS.$("#jira-setupwizard").submit();
        });

    // Step 2 of 4 - setup.jsp

        // Handle the fetch license link which sends them off to my.atlassian.com
        AJS.$("#fetchLicense").click(function(){
            var formValues = AJS.$("#jira-setupwizard").serializeArray();
            var url = AJS.$(this).attr("data-url");
            AJS.$.post(url, formValues, function(){
                return false;
            });
        });

    // Set 4 of 4 - setup3.jsp

        // Hide any leftover test-connection messages
        AJS.$('#test-connection-messages').hide();

        // Enable/Disable Notifications toggle
        AJS.$('input[name=noemail]').change(function() {
            var isEnabled = AJS.$(this).val() == "false",
                notificationFields = AJS.$('#setup-notification-fields'),
                testButton = AJS.$('#jira-setupwizard-test-mailserver-connection');

            if (isEnabled) {
                notificationFields.removeClass('hidden');
                testButton.removeClass('hidden');
            } else {
                notificationFields.addClass('hidden');
                testButton.addClass('hidden');
            }
        });

        // Set the initial show/hide state of the fields - for cases where the page posts back (validation failed, test connection, etc)
        if (AJS.$('input[name=noemail]:checked').val() == "false") {
            AJS.$('#setup-notification-fields, #jira-setupwizard-test-mailserver-connection').removeClass('hidden');
        }

        // SMTP/JNDI toggle
        AJS.$('input[name=mailservertype]').change(function() {
            var type = AJS.$(this).val(),
                fields = AJS.$('.setup-fields');

            if (type == "smtp") {
                fields.addClass('hidden');
                AJS.$('#email-notifications-smtp-fields').removeClass('hidden');
            } else {
                fields.addClass('hidden');
                AJS.$('#email-notifications-jndi-fields').removeClass('hidden');
            }
        });

        // Set the initial show/hide state of the fields - for cases where the page posts back (validation failed, test connection, language change, etc)
        if (AJS.$('input[name=mailservertype]:checked').val() == "smtp") {
            AJS.$('.setup-fields').addClass('hidden');
            AJS.$('#email-notifications-smtp-fields').removeClass('hidden');
        } else if (AJS.$('input[name=mailservertype]:checked').val() == "jndi") {
            AJS.$('.setup-fields').addClass('hidden');
            AJS.$('#email-notifications-jndi-fields').removeClass('hidden');
        }

        // For testing the connection
        AJS.$("#jira-setupwizard-test-mailserver-connection").click(function() {
            var action = "VerifySmtpServerConnection!setup.jspa";

            AJS.$("input[name=testingMailConnection]").val("true");
            AJS.$("#jira-setupwizard").attr('action', action).submit();
        });


    // Import Existing Data
    AJS.$("#reimport").click(function(e) {
        e.preventDefault();
        //set the form to import with default paths
        AJS.$('input[name=useDefaultPaths]').val("true");
        AJS.$("#jira-setupwizard").submit();
    });

});
