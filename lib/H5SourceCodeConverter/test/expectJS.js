// testtest
/*
 * これはBlockコメントです。
 */
(function(window, $) {
  // aaaaaaaaaaaaaaaaaaaaa
  // bbbbbbbbbbbbbbbbbbbbb
  var a = {};
  // ccccccccccccccccccccc
  // ddddddddddddddddddddd
  var b = {};

  /**
   * これは一行目の関数の説明です。<br>
   * これは二行目の関数の説明です。<br>
   * var test = new function(){}<br>
   *
   * @param {ParamType} paramName paramの説明です
   * @returns {ReturnsType} returnsの説明です
   */
  var test = function(paramName) {
    // a変数です。\n
    var a = {};
    // b変数です。\n
    var b = {};

    /**
     * これはインナー関数です。
     *
     * @param {ParamType} innerParam
     * @return {String} sampleを返します
     */
    function(innerParam) {
      // sampleを返します。
      return "sample";
    }
    (a);
    return test0(a);
  };
});
