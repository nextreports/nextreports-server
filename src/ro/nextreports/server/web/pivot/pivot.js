function onStopFieldMove(areasId) {
	var items = [];
	$("#" + areasId).find("ul.fields-container").each(function() {
		var areaId = $(this).attr("id");
		var areaName = areaId.substring(areaId.indexOf("-") + 1, areaId.lastIndexOf("-")); // "area-<name>-<uid>"
		$(this).children().each(function(i) {
			var fieldId = $(this).attr("id");
			var fieldIndex = fieldId.substring(6); // "field-<index>"
			// create item object for current panel  
			var item = {
				areaName : areaName,
				fieldIndex : fieldIndex, 
				sortIndex : i
			};

			// push item object into items array  
			items.push(item);
		});
	});

	// pass items variable to server to save state
	var data = $.toJSON(items);
	
	return data;
}
