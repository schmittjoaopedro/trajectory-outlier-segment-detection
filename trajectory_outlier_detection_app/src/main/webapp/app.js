var drawingManager, 
	directionsService, 
	trajectory, 
	routes = [], 
	map, 
	regionStart = undefined, 
	regionEnd = undefined, 
	region = undefined, 
	all_overlays = [], 
	polylines = [];

if (typeof (Number.prototype.toRad) === "undefined") {
	Number.prototype.toRad = function() {
		return this * Math.PI / 180;
	}
}

function initMap() {
	map = new google.maps.Map(document.getElementById('map'), {
		center:{lat:-26.295199,lng:-48.847915}, // Joinville
		//center: {lat: 37.746015, lng: -122.362731}, //Uber
		scrollwheel : true,
		zoom : 12
	});

	directionsService = new google.maps.DirectionsService();
	drawingManager = new google.maps.drawing.DrawingManager();

	drawingManager
			.setOptions({
				drawingMode : google.maps.drawing.OverlayType.RECTANGLE,
				drawingControl : false,
				drawingControlOptions : {
					position : google.maps.ControlPosition.TOP_CENTER,
					drawingModes : [ google.maps.drawing.OverlayType.RECTANGLE ]
				},
				markerOptions : {
					icon : 'https://developers.google.com/maps/documentation/javascript/examples/full/images/beachflag.png'
				},
				rectangleOptions : {
					fillOpacity : 0.5,
					strokeWeight : 0.5,
					clickable : false,
					editable : false
				}
			});

	google.maps.event.addListener(drawingManager, 'rectanglecomplete',
			function(event) {
				var b = event.getBounds();
				if (region === 'start') {
					regionStart = {
						latMin : b.getSouthWest().lat(),
						latMax : b.getNorthEast().lat(),
						lngMin : b.getSouthWest().lng(),
						lngMax : b.getNorthEast().lng()
					};
				} else if (region === 'end') {
					regionEnd = {
						latMin : b.getSouthWest().lat(),
						latMax : b.getNorthEast().lat(),
						lngMin : b.getSouthWest().lng(),
						lngMax : b.getNorthEast().lng()
					};
				}
				region = undefined;
				drawingManager.setMap(null);
				drawingManager.setOptions({
					drawingControl : false
				});
			});

	google.maps.event.addListener(drawingManager, 'overlaycomplete',
			function(e) {
				all_overlays.push(e);
				if (e.type != google.maps.drawing.OverlayType.RECTANGLE) {
					drawingManager.setDrawingMode(null);
					var newShape = e.overlay;
					newShape.type = e.type;
					google.maps.event.addListener(newShape, 'click',
							function() {
								setSelection(newShape);
							});
					setSelection(newShape);
				}
			});

}

function drawPoints(path, color) {
	if (color == '#FFFFFF')
		color = '#00FFF0';
	polylines.push(new google.maps.Polyline({
		path : path,
		geodesic : true,
		strokeColor : color || '#FF0000',
		strokeOpacity : 1.0,
		strokeWeight : 2,
		map : map
	}));
}

function drawCircle(lat, lng, radius, color) {

	var R = 6371; // km
	var dLat = (radius).toRad();
	var dLon = (0).toRad();
	var lat1 = lat.toRad();
	var lat2 = lng.toRad();
	var hDLat = dLat / 2;
	var hDLng = dLon / 2;

	var a = Math.sin(hDLat) * Math.sin(hDLat) + Math.sin(hDLng)
			* Math.sin(hDLng) * Math.cos(lat1) * Math.cos(lat2);
	var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	var d = R * c;

	var marker = new google.maps.Marker({
		map : map,
		position : new google.maps.LatLng(lat, lng)
	});
	var circle = new google.maps.Circle({
		map : map,
		radius : d * 1000,
		fillColor : color || '#AA0000'
	});
	circle.bindTo('center', marker, 'position');
}

function drawStartRec() {
	if (!regionStart) {
		region = 'start';
		drawingManager.setMap(map);
		drawingManager.setOptions({
			drawingControl : false,
			rectangleOptions : {
				fillColor : '#FFFFFF'
			}
		});
	}
}

function drawEndRec() {
	if (!regionEnd) {
		region = 'end';
		drawingManager.setMap(map);
		drawingManager.setOptions({
			drawingControl : false,
			rectangleOptions : {
				fillColor : '#000000'
			}
		});
	}
}

function clearMap() {
	for (var i = 0; i < all_overlays.length; i++) {
		all_overlays[i].overlay.setMap(null);
	}
	for (var i = 0; i < polylines.length; i++) {
		polylines[i].setMap(null);
	}
	polylines = [];
	all_overlays = [];
	regionStart = undefined;
	regionEnd = undefined;
	region = undefined;
}

function getTrajectories() {
	for (var i = 0; i < polylines.length; i++) {
		polylines[i].setMap(null);
	}
	polylines = [];
	var req = {
		country : $('#country').val(),
		state : $('#state').val(),
		city : $('#city').val(),
		startHour : $('#startHour').val(),
		endHour : $('#endHour').val(),
		distance : $('#distance').val(),
		angle : $('#angle').val(),
		kStandard : $('#kStandard').val(),
		startGrid : regionStart,
		endGrid : regionEnd,
		interpolation: $('#interpolation').val(),
		sigma: $('#sigma').val(),
		sd: $('#sd').val()
	};
	$.ajax({
		type : "POST",
		url : "/trajectory_outlier_detection_app/resources/trajectory/process",
		dataType : "json",
		contentType : 'application/json',
		data : JSON.stringify(req)
	}).done(function(data) {
		if (data.standards) {
			data.standards.forEach(function(group) {
				if (group.trajectories) {
					group.trajectories.forEach(function(trajectory) {
						drawPoints(trajectory.points, '#FF0000');
					});
				}
			});
		}
		if (data.routes) {
			data.routes.forEach(function(route) {
				if (route.standards) {
					route.standards.forEach(function(trajectory) {
						drawPoints(trajectory.points, '#FF0000');
					});
				}
				if (route.notStandards) {
					route.notStandards.forEach(function(trajectory) {
						drawPoints(trajectory.points, '#0000FF');
					});
				}
			});
		}
	});
}

$('#endHour').val("23");
$('#distance').val("0.0003");
$('#angle').val("30.0");
$('#kStandard').val("1");
$('#interpolation').val("0.0003");
$('#sd').val("0.001");
$('#sigma').val("0.0015");