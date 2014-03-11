var Stopwatch = function(elem, options) {

	var timer = createTimer(), offset, clock, interval;

	// default options
	options = options || {};
	options.delay = options.delay || 1;
	options.call = options.call || null;

	// append elements     
	elem.appendChild(timer);
	// initialize
	reset();

	// private functions
	function createTimer() {
		return document.createElement("span");
	}

	function createButton(action, handler) {
		var a = document.createElement("a");
		a.href = "#" + action;
		a.innerHTML = action;
		a.addEventListener("click", function(event) {
			handler();
			event.preventDefault();
		});
		return a;
	}

	function start() {
		offset = Date.now();
		interval = setInterval(update, options.delay);
	}

	function stop() {
		if (interval) {
			clearInterval(interval);
		}
	}

	function reset() {
		clock = 0;
		render();
	}

	function update() {

		options.call();
		clock += delta();
		render();
	}

	function render() {
		timer.innerHTML = clock / 1000;
	}

	function delta() {
		var now = Date.now(), d = now - offset;

		offset = now;
		return d;
	}

	// public API
	this.start = start;
	this.stop = stop;
	this.reset = reset;
};