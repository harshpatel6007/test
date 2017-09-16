app.service('ajaxService', ['$http', 'toaster', 'modalUtil', function ($http, toaster, modalUtil) {
	
	var createCustomToaster = function (type, title, text) {
		console.log("add toaster");
		toaster.clear();
    	toaster.pop(type, title, text);
	};
	
	var displayMessage = function (data) {
		if(data["message"] && data["message"] != '') {
			if(data["type"] == 'success') {
				createCustomToaster('success', '', data["message"]);
			} else {
				createCustomToaster('error', '', data["message"]);
			}					
		}
	};
	
	var handleSuccessResponse = function (response, successCallback, errorCallback) {
		var data = response["data"];
		if(data["code"] == "750") {
			displayException();
		} else {
			displayMessage(data);
			if(data["type"] == "success") {
				if(successCallback) {
					successCallback(data);
				}
			} else {
				if(errorCallback) {
					errorCallback(data);
				}
			}			
		}
	};
	
	var handleErrorResponse = function () {
		console.log("handle error");
		modalUtil.createDefaultErrorModal('Error', 'Something Goes Wrong, Contact Admin', 'lg');		
	};
	
	var displayException = function () {
		console.log("handle exception");
		modalUtil.createDefaultErrorModal('Error', 'Your session is no longer valid. Click Ok button for goto login page', 'lg');		
	};
	
	return {
		
		fireGetRequest : function (url, successCallback, errorCallback) {
			console.log("fire get request");
			$http({
				  method: 'GET',
				  url: url			 
				})
			.then(
				function success(response) {
//					console.log("get success response from server, move to success callback");
					successCallback(response);
				},
				function error(response) {
//					console.log("get error response");
//					console.log("object : " + angular.toJson(response));					
				}
			);
		},
		
		/**
		 * 	fire post request to server.
		 * 	user can define their success and error callback
		 */
		firePostRequest : function (url, data, successCallback, errorCallback) {
			console.log("fire post request");
			$http({
				  method: 'POST',
				  url: contextPath + url,
				  headers: { 
					  'Content-Type' : 'application/json' 
				  },
				  data: JSON.stringify(data)
				})
			.then(
				function success(response) {					
//					console.log("post success response from server, move to success callback");			
					handleSuccessResponse(response, successCallback, errorCallback);					
				},
				function error(response) {					
//					console.log("post error response from server, move to default error handler");
//					console.log("error : " + angular.toJson(response));
					handleErrorResponse();
				}
			);
		},
		
		/**
		 * 	fire post request to server.
		 * 	return promise of http post
		 */
		getDataFromServer : function (url, data) {			
			return $http({
						  method: 'POST',
						  url: contextPath + url,
						  headers: { 
							  'Content-Type' : 'application/json' 
						  },
						  data: JSON.stringify(data)
					});
		}
		
	};
	
}]);
