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
  var test = function(paramName) {
    /// <summary>
    ///  これは一行目の関数の説明です。
    ///  これは二行目の関数の説明です。
    ///  var test = new function(){}
    /// </summary>
    /// <param  name = "paramName" type = "ParamType" >
    ///  paramの説明です
    /// </param>
    /// <returns  type = "ReturnsType" >
    ///  returnsの説明です
    /// </returns>
    // a変数です。\n
    var a = {};
    // b変数です。\n
    var b = {};
    function(innerParam) {
      /// <summary>
      ///  これはインナー関数です。
      /// </summary>
      /// <param  name = "innerParam" type = "ParamType" >
      /// </param>
      /// <returns  type = "String" >
      ///  sampleを返します
      /// </returns>
      // sampleを返します。
      return "sample";
    }
    (a);
    return test0(a);
  };
});
