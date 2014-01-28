function applySearch(context, searchContext, urlTo) {

	if (searchContext !== undefined) {
	    var resultMap = {};
		$('#search-query').typeahead({
			remote: {
				url: context + searchContext + '/filterbyjson/%QUERY',
				filter: function(items) {
					var searchResults = [];
					for (var i = 0; i < items.length; i++) {
						var item = items[i];
						var label = item.name;
						if (label === undefined) {
							label = item.title;
						}
						searchResults[i] = {
							value: label,
							url: item.url,
							field_url: item.field_url
						};
					}				
		          	return searchResults;
				}
			},
			 template: '<p><strong>{{value}}</strong></p><p><a href="{{field_url}}">{{field_url}}</a></p>',
			 engine: Hogan
		}).on('typeahead:selected', function(event, datum) {
			console.log("contextTo: " + urlTo);
			if (urlTo !== undefined) {
				document.location = context + urlTo + "/" + datum.url; 
			} else {
				document.location = context + searchContext + "/" + datum.url; 
			}
		});
	}
}

function scopeCheck(context) {
    var idle_timeout,
    SCOPE_URI = context + 'api/scope/',
    MIN_TEXT_LENGTH = 4, // minimum length annotation must have before being allowed to the doScope server
    TRIGGER_CHARS = ". ,", // characters that force an doScope lookup
    IDLE_THRESHOLD = 500; // doScope is also done after IDLE_THRESHOLD milliseconds of key idleness

    // Does the Scope lookup
    var doScope = function(text) {
	    $.getJSON(SCOPE_URI + text, function(data) {
		    //alert("Is in scope: " + data);
		    var btn = document.getElementById('addentry');
		    var btnsearch = document.getElementById('search');
		    if (data) {
		            btn.style = "background-color: hell-green";
		            btnsearch.style = "background-color: hell-green";
		    } else {
		            btn.style = "background-color: red";
		            btnsearch.style = "background-color: red";
		    }
	    });
    };
   
    // Restarts the keyboard-idleness timeout
    var restartIdleTimeout = function(text) {
	    if (idle_timeout) {
		    window.clearTimeout(idle_timeout);
	    }
	    idle_timeout = window.setTimeout(function() { 
	    	doScope(text); 
	    }, IDLE_THRESHOLD);
    };                
   
    $("#search-query").keyup(function() {
    	var text = $(this).val();

    	if (text.length > MIN_TEXT_LENGTH) {
    		restartIdleTimeout(text);
           
            if (TRIGGER_CHARS.indexOf(text[text.length - 1]) > -1)
	            doScope(text);
            }
    });
}
