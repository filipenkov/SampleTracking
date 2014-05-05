//Handles reading in a file via javascript
//Requires a function
//org_jcvi_jira_plugins_customfield_shared_ParseFile(<String> fileContents, <String> customFieldID)
//to handle the file once it is loaded

//Example:
//        <div class="reset-origin-for-absolute-${customField.id}">
//            <!-- relative to the reset co-ordinate space-->
//                <span class="no-margin-${customField.id}">
//                    <input type="submit"
//                           value="Load From File"
//                           onclick="return false"/>
//                    <!-- onclick - ensure that this button doesn't actually do anything -->
//                </span>
//            <!-- absolute in the reset co-ordinate space to place this
//        over the previous span -->
//                <span class="transparent-overlay-${customField.id}">
//                    <input id="file-select-${customField.id}"
//                           type="file"
//                           name="fileLoader"
//                           onchange="loadFile(event,'${customField.id}');"/>
//                </span>
//        </div>
new function() {
   //if necessary setup the hash used to find the correct reader for a
   //particular field
   Reader_Hash = new Array();

   this.getReader = function(CustomFieldID) {
       return Reader_Hash[CustomFieldID];
   };

  
   this.setReader = function(customFieldID, value) {
       Reader_Hash[customFieldID] = value;
   };

   /**
    * Creates a new FileReader object and attaches the handler.
    */
   this.reset = function(CustomFieldID) {
        function onloadClosure(fieldID) {
            return function (oFREvent) {
            	//alert("Parsing file for field "+fieldID+" with org_jcvi_jira_plugins_customfield_ParseFile then resetting "+CustomFieldID);
            	org_jcvi_jira_plugins_customfield_ParseFile(oFREvent.target.result,fieldID);
            	reset(CustomFieldID);
            }
        }
       var reader = new FileReader();
       //is this needed with the closure?
       reader.CustomFieldID = CustomFieldID;
       //onload is called once the file requested has been uploaded
       reader.onload = onloadClosure(CustomFieldID);
       //replace the reader in the hash
       this.setReader(CustomFieldID,reader);
   };

   /*
    * The handler for the FileSelector. This starts the file loading process
    */
   this.loadFile = function(event, CustomFieldID) {
       var files = event.target.files;
      //alert("In FileSelector handler 2...");
       if (files.length == 0) {
    	   //alert("No files...");
    	   return;
       }
       
       //var reader = get_org_jcvi_jira_plugins_customfield_shared_Reader(CustomFieldID);
       var reader = this.getReader(CustomFieldID);
       reader.readAsText(files[0]);
   };
}