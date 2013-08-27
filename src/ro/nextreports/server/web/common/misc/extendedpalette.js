Wicket.Palette.addAll=function(choicesId, selectionId, recorderId) {
	var choices=Wicket.Palette.$(choicesId);
	var selection=Wicket.Palette.$(selectionId);

	if (Wicket.Palette.moveAllHelper(choices, selection)) {
		var recorder=Wicket.Palette.$(recorderId);
		Wicket.Palette.updateRecorder(selection, recorder);
	}
}

Wicket.Palette.removeAll=function(choicesId, selectionId, recorderId) {
	var choices=Wicket.Palette.$(choicesId);
	var selection=Wicket.Palette.$(selectionId);

	if (Wicket.Palette.moveAllHelper(selection, choices)) {
		var recorder=Wicket.Palette.$(recorderId);
		Wicket.Palette.updateRecorder(selection, recorder);
	}
}

Wicket.Palette.moveAllHelper=function(source, dest) {
	var dirty=false;
	for (var i=0;i<source.options.length;i++) {
		dest.appendChild(source.options[i]);
		i--;
		dirty=true;
	}
	return dirty;
}
