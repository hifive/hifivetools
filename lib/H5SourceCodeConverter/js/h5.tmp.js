(function($) {

	return
	str//
	.replace(/\\/g, '\\\\')//
	.replace(/"/g, '\"')//
	.replace(/'/g, "\'")//
	.replace(/\//g,'\/')//
	.replace(/</g, '&#x3c;')//
	.replace(/>/g, '&#x3e;')//
	.replace(/&#x0d/g, '\r')//
	.replace(/&#x0a/g, '\n');//



})(jQuery);

