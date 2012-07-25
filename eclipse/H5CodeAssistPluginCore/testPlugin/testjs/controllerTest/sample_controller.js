
$(function() {

	// 赤色の範囲のdiv
	var grandParentBase = {
		option: {
			// ここの値によってブロックする範囲が変わる。
			receiveBlock: true
		}
	};

	// 青色の範囲のdiv
	var parentBase = {

		option: {
			// ここの値によってブロックする範囲が変わる。
			receiveBlock: false
		},

		'#childrenStart click': function() {
			// child1StartボタンのselfBlockイベント、child2Startボタンのclickイベントをトリガ.
			$('#child1Start').trigger('selfBlock');
			$('#child2Start').trigger('click');
		},

		'#parentBlock click': function() {
			// child1Startボタン、child2Startボタンのclickイベントをトリガ.
			$('#child1Start').trigger('parentClick');
			$('#child2Start').trigger('parentClick');
		}
	};

	// 緑色の範囲のdiv
	var child1Base = {
		'#child1Start click': function(context) {
			dump('child1 start');

			// グローバルコントローラにブロックしてくれるように頼む
			this.blockParent({message: 'child1 global block'}, 'blockGlobal');

			// 非同期処理の中で使用するために退避
			var dfd = this.deferred();
			var that = this;
			setTimeout(function() {
				// グローバルコントローラにブロックを解除するように頼む
				that.unblockParent('unblockGlobal');
				dump('child1 end');
				dfd.resolve();
			}, 800);
			return dfd.promise();
		},

		'#child1Start selfBlock': function(context) {
			dump('child1 start');

			// 自身をブロックする
			this.block({message: 'child1 self block'});

			// 非同期処理の中で使用するために退避
			var dfd = this.deferred();
			var that = this;
			setTimeout(function() {
				// 自身のブロックを解除
				that.unblock();
				dump('child1 end');
				dfd.resolve();
			}, 800);
			return dfd.promise();
		},

		'#child1Start parentClick': function(promise, resolve, reject) {
			dump('child1 start');

			// 親コントローラにブロックしてくれるように頼む
			this.blockParent({message: 'child1 parent block'});

			// 非同期処理の中で使用するために退避
			var dfd = this.deferred();
			var that = this;
			setTimeout(function() {
				// 親コントローラにブロックを解除するように頼む
				that.unblockParent();
				dump('child1 end');
				dfd.resolve();
			}, 800);
			return dfd.promise();
		}
	};

	// 黄色の範囲のdiv
	var child2Base = {
		'#child2Start click': function(promise, resolve, reject) {
			dump('child2 start');
			this.block({message: 'child2 self block'});
			var dfd = this.deferred();
			var that = this;
			setTimeout(function() {
				that.unblock();
				dump('child2 end');
				dfd.resolve();
			}, 2500);
			return dfd.promise();
		},

		'#child2Start parentClick': function(promise, resolve, reject) {
			dump('child2 start');
			this.blockParent({message: 'child2 parent block'});
			var dfd = this.deferred();
			var that = this;
			setTimeout(function() {
				that.unblockParent();
				dump('child2 end');
				dfd.resolve();
			}, 2500);
			return dfd.promise();
		}
	};


	// コントローラの作成と要素へのバインド.
	h5.core.controller('#grandParent', 'GrandParentController', grandParentBase);
	h5.core.controller('#parent', 'ParentController', parentBase);
	h5.core.controller('#child1', 'Child1Controller', child1Base);
	h5.core.controller('#child2', 'Child2Controller', child2Base);
});