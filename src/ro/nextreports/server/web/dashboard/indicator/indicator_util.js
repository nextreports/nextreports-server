
// This function starts by creating a dummy <canvas> element which is never 
// attached to the page, so no one will ever see it. 
// As soon as we create the dummy <canvas> element, we test for the presence 
// of a getContext() method. This method will only exist if browser supports the canvas API.
// Finally, we use the double-negative trick to force the result to a Boolean value (true or false). 
function isCanvasEnabled() {
	return !!document.createElement('canvas').getContext;
}

function getIndicatorHeight() {
	return $(window).height();
}