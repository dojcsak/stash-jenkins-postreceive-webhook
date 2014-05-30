AJS.toInit(function() {
	
  function populateForm() {
    AJS.$.ajax({
      url: baseUrl + "/rest/stash-webhook-jenkins-config/1.0/",
      dataType: "json",
      success: function(config) {
    	if(!config.executablePath){
    		AJS.$("#executablePath").attr("value",  "/usr/local/bin/jenkins-makejobs-git");
    	}
    	else{
    		AJS.$("#executablePath").attr("value", config.executablePath);
    	}
    	if(!config.configHome){
    		AJS.$("#configHome").attr("value", "/opt/jenkins-autojobs");
    	}
    	else{
    		AJS.$("#configHome").attr("value", config.configHome);
    	}
      }
    });
  }
  
  function updateConfig() {
	  AJS.$.ajax({
	    url: baseUrl + "/rest/stash-webhook-jenkins-config/1.0/",
	    type: "PUT",
	    contentType: "application/json",
		data: '{ "executablePath": "' + AJS.$("#executablePath").attr("value") + '", "configHome": "' +  AJS.$("#configHome").attr("value") + '" }',	    
		processData: false
	  });
	}
  
  populateForm();
  
  AJS.$("#config").submit(function(e) {
	    e.preventDefault();
	    if(!(AJS.$("#executablePath").attr("value")) || !(AJS.$("#configHome").attr("value"))){
	    	AJS.messages.error({
		        title: "Error!",
		        body: "Please fill all mandatory fields!"
		     }); 
	    }	
	    else{
		    updateConfig();
		    AJS.messages.success({
		        title: "Success!",
		        body: "The configurations have been succesfully saved.",
				fadeout: true
		     }); 
	    }
	});
});