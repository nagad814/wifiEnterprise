var exec = require('cordova/exec');

var formatWifiString = function (ssid) {

    if (ssid === undefined || ssid === null) {
        ssid = "";
    }
    ssid = ssid.trim();

    if (ssid.charAt(0) != '"') {
        ssid = '"' + ssid;
    }

    if (ssid.charAt(ssid.length - 1) != '"') {
        ssid = ssid + '"';
    }

    return ssid;
};

exports.getMacAddress = function(success, error){
 		cordova.exec(success, error, 'WifiEnterprise', 'getMacAddress', []);
};

exports.getWifiMacAddress = function(success, error){
 		cordova.exec(success, error, 'WifiEnterprise', 'getWifiMacAddress', []);
};

/**
     *  Gets the currently connected wifi SSID
     * @param 	win	callback function
     * @param 	fail	callback function if error
     */
exports.getCurrentSSID = function(success, error) {
    // if (typeof win != "function") {
    //     console.log("getCurrentSSID first parameter must be a function to handle SSID.");
    //     return;
    // }
    cordova.exec(success, error, 'WifiEnterprise', 'getConnectedSSID', []);
};

exports.getSupplicantState = function(success, error) {
    
    cordova.exec(success, error, 'WifiEnterprise', 'getSupplicantState', []);
};

/**
 *  Gets 'true' or 'false' if WiFi is enabled or disabled
 * @param 	success	callback function
 * @param 	fail
 */
 exports.isWifiEnabled = function (success, error) {
      if (typeof success != "function") {
          console.log("isWifiEnabled first parameter must be a function to handle wifi status.");
          return;
      }
      cordova.exec(
          // Cordova can only return strings to JS, and the underlying plugin
          // sends a "1" for true and "0" for false.
          success,
          error, 'WifiEnterprise', 'isWifiEnabled', []
      );
};

  /**
   *  Gets '1' if WiFi is enabled
   * @param   enabled	callback function
   * @param 	success	callback function
   * @param 	fail	callback function if wifi is disabled
   */
  exports.setWifiEnabled = function (enabled, success, error) {
      if (typeof success != "function") {
          console.log("WifiEnterprise second parameter must be a function to handle enable result.");
          return;
      }
      cordova.exec(success, error, 'WifiEnterprise', 'setWifiEnabled', [enabled]);
};

/**
 *  Start scanning wifi.
 * @param 	success	callback function
 * @param 	error	callback function if error
 */
exports.startScan = function (success, error) {

    if (typeof success != "function") {
        console.log("startScan first parameter must be a function to handle.");
        return;
    }
    cordova.exec(success, error, 'WifiEnterprise', 'startScan', []);
};

/**
     * Scan WiFi networks and return results
     * @param success callback function
     * @param error callback function if error
     */
exports.scanWifi = function(options, success, error) {

   if (typeof options === 'function') {
       error = success;
       success = options;
       options = {};
   }

   if (typeof success != 'function' ) {
       console.log("scan first parameters must be a function to handle list.");
       return;
   }

   cordova.exec(success, error, 'WifiEnterprise', 'scanWifi', [options]);
};

/**
 *	Hands the list of previously used and configured networks to the `success` success callback function.
 * @param 	success	callback function that receives list of networks
 * @param 	error	callback function if error
 * @return	a list of networks
 */
exports.listNetworks = function (success, error) {
    if (typeof success != "function") {
        console.log("listNetworks first parameter must be a function to handle list.");
        return;
    }
    cordova.exec(success, error, 'WifiEnterprise', 'listNetworks', []);
};

/**
 *  Hands the list of scanned  networks to the `success` success callback function.
 * @param   opts optional json object of options
 * @param 	success	callback function that receives list of networks
 * @param 	error	callback function if error
 * @return	a list of networks
 */
exports.getScanResults = function (options, success, error) {
    if (typeof options === 'function') {
        error   = success;
        success = options;
        options = {};
    }

    if (typeof success !== "function") {
        console.log("getScanResults first parameter must be a function to handle list.");
        return;
    }

    cordova.exec(success, error, 'WifiEnterprise', 'getScanResults', [options]);
};

exports.setWifiEap = function(wifi, success, error) {

      console.log("Invoked WifiEnterprise: setWifiEap");

      if (wifi !== null && typeof wifi === 'object') {
          // Ok to proceed!
      } else {
          console.log('WifiEnterprise: Invalid parameter. parameter is not an object.');
          return false;
      }

      //var networkInformation = [];
      var ssid = "";
      if(typeof wifi === 'object') {

          ssid = formatWifiString(wifi.SSID);
          console.log("Formatted Wifi string : " + ssid);
            //networkInformation.push(ssid);

          } else {
              // i dunno, like, reject the call or something? what are you even doing?
              console.log('WifiEnterprise: No SSID given.');
              return false;
          }

          //networkInformation.push(wifi.identity);
          //networkInformation.push(wifi.password);

      exec(success, error, "WifiEnterprise", "setWifiEap", [ssid, wifi.identity, wifi.password]);
      // exec(success, error, "WifiEnterprise", "setWifiEap", networkInformation);
};

/**
 *	This method removes a given network from the list of configured networks.
 *	@param	SSID	of the network to remove
 *	@param	success	function to handle successful callback
 *	@param	fail		function to handle error callback
 */
exports.removeWifiEap = function (wifi, success, error) {

    console.log("Invoked WifiEnterprise: removeWifiEap");
    var ssid = formatWifiString(wifi.SSID);
    console.log("Formatted Wifi string : " + ssid);

    exec(success, error, 'WifiEnterprise', 'removeWifiEap', [ssid]);
};

/**
 *	This method connects a network if it is configured.
 *	@param	SSID	the network to connect
 *	@param	success		function that is called if successful
 * @param	error		function that is called to handle errors
 */
exports.connectNetwork = function (wifi, success, error) {

  console.log("Invoked WifiEnterprise: connectNetwork");
  var ssid = formatWifiString(wifi.SSID);
    exec(success, error, 'WifiEnterprise', 'connectNetwork', [ssid]);
};

/**
 *	This method disconnects a network if it is configured.
 *	@param	SSID	the network to disconnect
 *	@param	success		function that is called if successful
 * @param	error		function that is called to handle errors
 */
exports.disconnectNetwork = function (wifi, success, error) {

  console.log("Invoked WifiEnterprise: disconnectNetwork");
  var ssid = formatWifiString(wifi.SSID);
    exec(success, error, 'WifiEnterprise', 'disconnectNetwork', [ssid]);
};
