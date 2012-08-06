(function($) {
	if (window.h5 !== undefined) {
		return 1;
	}
	// 改行する.1

	if (window.h5 !== undefined) {
		return 2;
	} // 改行しない.2

	var errorInterceptor = function(invocation) {
		return 3;
	};
	// 改行する.3

	var errorInterceptor = function(invocation) {
		return 4;
	};// 改行しない.4

})(jQuery);
