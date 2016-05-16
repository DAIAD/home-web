/**
 * @api {post} /v1/device/register Register an Amphiro device
 * @apiVersion 0.0.1
 * @apiName AmphiroRegister
 * @apiGroup Device
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Registers an Amphiro device. Expects an instance of <code>AmphiroDeviceRegistrationRequest</code> that extends <code>DeviceRegistrationRequest</code>.
 *
 * @apiParam (DeviceRegistrationRequest) {Object}     credentials                 User credentials
 * @apiParam (DeviceRegistrationRequest) {String}     credentials.username        User name.
 * @apiParam (DeviceRegistrationRequest) {String}     credentials.password        Password.
 * @apiParam (DeviceRegistrationRequest) {String}     type                        Device type. Must be set to <code>AMPHIRO</code>.
 * @apiParam (DeviceRegistrationRequest) {Object[]}   [properties]                Array of <code>KeyValuePair</code> objects representing device properties.
 * 
 * @apiParam (KeyValuePair) {String}           key                         Key.
 * @apiParam (KeyValuePair) {String}           value                       Value.
 * 
 * @apiParam (AmphiroDeviceRegistrationRequest extends DeviceRegistration)    {String}     name       User friendly name for the device i.e. Shower #1.
 * @apiParam (AmphiroDeviceRegistrationRequest extends DeviceRegistration)    {String}     macAddress Device unique MAC address.
 * @apiParam (AmphiroDeviceRegistrationRequest extends DeviceRegistration)    {String}     aesKey     Device AES key.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "type":"AMPHIRO",
 *   "name": "Amphiro #1",
 *   "macAddress": "9a:94:69:12:a1:f0",
 *   "aesKey": "Qz9-tZUDRQSHBEV7S50V3zjlH7lDL840QLJop-TgEVA",
 *   "properties":[{
 *     "key":"manufacturer",
 *     "value":"amphiro"
 *   }, {
 *     "key":"model",
 *     "value":"b1"
 *   }]
 * }
 * 
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>
 * @apiSuccess {String}   deviceKey           Unique device key (UUID).
 * @apiSuccess {Object[]} configurations      Array of <code>DeviceAmphiroConfiguration</code> with device active and not acknowledged configurations.
 *
 * @apiSuccess (DeviceAmphiroConfiguration) {String}           version             Configuration version (UUID).
 * @apiSuccess (DeviceAmphiroConfiguration) {String}           title               Title.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           createdOn           Creation date time stamp.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           acknowledgedOn      Acknowledged date time stamp.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           enabledOn           Date time stamp thet the mobile application has enabled the configuration for the specific device.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number[]}         properties          Amphiro specific parameters
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           block               Block.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           numberOfFrames      Number of frames.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           frameDuration       Frame duration.
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "deviceKey": "75b2a82c-d41e-4a2f-b281-3047073ee474",
 *   "configurations": [{
 *     "version": "4715cf67-127f-40a3-8ddb-46600530628e",
 *     "title": "Off Configuration",
 *     "createdOn": 1463163145701,
 *     "acknowledgedOn": null,
 *     "enabledOn": null,
 *     "properties": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ],
 *     "block": 3,
 *     "numberOfFrames": 0,
 *     "frameDuration": 0
 *   }],
 *   "success": true
 * }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USERNANE_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function registerAmphiro() { return; }

/**
 * @api {post} /v1/meter/register Register Smart Water Meter
 * @apiVersion 0.0.1
 * @apiName MeterRegister
 * @apiGroup Device
 * @apiPermission ROLE_ADMIN
 * 
 * @apiDescription Registers a smart water meter to a user account. Expects an instance of <code>WaterMeterDeviceRegistrationRequest</code> that extends <code>DeviceRegistrationRequest</code>.
 *
 * @apiParam (DeviceRegistrationRequest) {Object}     credentials                 User credentials
 * @apiParam (DeviceRegistrationRequest) {String}     credentials.username        User name.
 * @apiParam (DeviceRegistrationRequest) {String}     credentials.password        Password.
 * @apiParam (DeviceRegistrationRequest) {String}     type                        Device type. Must be set to <code>METER</code>.
 * @apiParam (DeviceRegistrationRequest) {Object[]}   [properties]                Array of <code>KeyValuePair</code> objects representing device properties.
 * 
 * @apiParam (KeyValuePair) {String}           key                         Key.
 * @apiParam (KeyValuePair) {String}           value                       Value.
 * 
 * @apiParam (WaterMeterDeviceRegistrationRequest extends DeviceRegistration)    {String}     userKey       Owner unique id (UUID).
 * @apiParam (WaterMeterDeviceRegistrationRequest extends DeviceRegistration)    {String}     serial 		Device serial number.
 * @apiParam (WaterMeterDeviceRegistrationRequest extends DeviceRegistration)    {Object}     location      Device location as a GeoJSON expression.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "type":"METER",
 *   "userKey":"4e0a0f34-06ad-4c49-a935-89eceea56f27",
 *   "serial":"D14IA023913",
 *   "properties":[],
 *   "location": { 
 *     "type": "Point",
 *     "coordinates": [100.0, 0.0] 
 *   }
 * }
 * 
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>
 * @apiSuccess {String}   deviceKey           Unique device key (UUID).
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "deviceKey": "75b2a82c-d41e-4a2f-b281-3047073ee474",
 *   "success": true
 * }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function registerMeter() { return; }

/**
 * @api {post} /v1/meter/register Query devices
 * @apiVersion 0.0.1
 * @apiName DeviceQuery
 * @apiGroup Device
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Queries user devices.
 *
 * @apiParam (DeviceRegistrationRequest) {Object}     credentials                 User credentials
 * @apiParam (DeviceRegistrationRequest) {String}     credentials.username        User name.
 * @apiParam (DeviceRegistrationRequest) {String}     credentials.password        Password.
 * @apiParam (DeviceRegistrationRequest) {String}     [type]                      Allows filtering devices by type. Valid values are <code>METER</code> and <code>AMPHIRO</code>.
 * 
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "type":"METER"
 * }
 * 
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>
 * @apiSuccess {Object[]} devices             Array of <code>Device</code>. Instances of <code>AmphiroDevice</code> or <code>WaterMeterDevice</code> may be returned.
 * 
 * @apiParam (Device) {String}     key                 Device unique key (UUID).
 * @apiParam (Device) {String}     type                Device type. Valid values are <code>METER</code> and <code>AMPHIRO</code>.
 * @apiParam (Device) {Object[]}   [properties]        Array of <code>KeyValuePair</code> objects representing device properties.
 * 
 * 
 * @apiParam (KeyValuePair) {String}           key                         Key.
 * @apiParam (KeyValuePair) {String}           value                       Value.
 * 
 * @apiParam (WaterMeterDevice extends Device) {String}     serial    Unique serial number.
 * @apiParam (WaterMeterDevice extends Device) {Object}     location  Location in GeoJSON format.
 * 
 * @apiParam (AmphiroDevice extends Device)    {String}     name       User friendly name for the device i.e. Shower #1.
 * @apiParam (AmphiroDevice extends Device)    {String}     macAddress Device unique MAC address.
 * @apiParam (AmphiroDevice extends Device)    {String}     aesKey     Device AES key.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "devices": [ {
 *     "key": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "properties": [ {
 *       "key": "debug.autogenerate",
 *       "value": "2016-04-20T11:36:50.111Z"
 *     }],
 *     "name": "Amphiro #1",
 *     "macAddress": "77:84:bd:b7:74:10",
 *     "aesKey": "YhSHlzInBoA_iVWa88wFFFbdKwbA8EerklKt8SgnVkg",
 *     "type": "AMPHIRO"
 *   }, {
 *     "key": "114e7af3-39af-4a44-ac57-41779689ec39",
 *     "properties": [ {
 *       "key": "import.file",
 *       "value": "3d0acf5c-e85b-4885-9476-77475913295d.xlsx"
 *     }, {
 *       "key": "import.date",
 *       "value": "2016-04-20T11:36:57.231Z"
 *     }],
 *     "serial": "I13FB124680",
 *     "location": {
 *       "type": "Point",
 *       "coordinates": [ 100, 12.04426716 ]
 *     },
 *     "type": "METER"
 *   } ],
 *   "success": true
 * }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function deviceQuery() { return; }

/**
 * @api {post} /v1/device/config Loads device configurations
 * @apiVersion 0.0.1
 * @apiName DeviceConfig
 * @apiGroup Device
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Loads device configurations that have been enabled but are not acknowledged yet.
 *
 * @apiParam {Object}     credentials                 User credentials
 * @apiParam {String}     credentials.username        User name.
 * @apiParam {String}     credentials.password        Password.
 * @apiParam {String[]}   deviceKey                   Array of device unique keys (UUID).
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": [ "aeea36cc-cefb-47db-ac0f-8c5dbc95b0f9" ]
 * }
 * 
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>.
 * @apiSuccess {Object[]} devices             Array of <code>DeviceConfigurationCollection</code>.
 *
 * @apiSuccess (DeviceConfigurationCollection) {String}           key             Device unique key (UUID).
 * @apiSuccess (DeviceConfigurationCollection) {String}           macAddress      Device MAC address.
 * @apiSuccess (DeviceConfigurationCollection) {Object[]}         configurations  Array of <code>DeviceAmphiroConfiguration</code>.
 * 
 * @apiSuccess (DeviceAmphiroConfiguration) {String}           version             Configuration version (UUID).
 * @apiSuccess (DeviceAmphiroConfiguration) {String}           title               Title.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           createdOn           Creation date time stamp.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           acknowledgedOn      Acknowledged date time stamp.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           enabledOn           Date time stamp thet the mobile application has enabled the configuration for the specific device.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number[]}         properties          Amphiro specific parameters
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           block               Block.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           numberOfFrames      Number of frames.
 * @apiSuccess (DeviceAmphiroConfiguration) {Number}           frameDuration       Frame duration.
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "devices": [   {
 *     "key": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "macAddress": "77:84:bd:b7:74:10",
 *     "configurations": [ {
 *       "version": "aeea36cc-cefb-47db-ac0f-8c5dbc95b0f9",
 *       "title": "Off Configuration",
 *       "createdOn": 1461152210531,
 *       "acknowledgedOn": null,
 *       "enabledOn": 1457365253000,
 *       "properties": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
 *       "block": 3,
 *       "numberOfFrames": 0,
 *       "frameDuration": 0
 *     }]
 *   }],
 *   "success": true
 * }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function deviceConfig() { return; }

/**
 * @api {post} /v1/device/notify Notify configuration
 * @apiVersion 0.0.1
 * @apiName DeviceNotify
 * @apiGroup Device
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Notifies server that an Amphiro configuration has been enabled on the device.
 *
 * @apiParam {Object}     credentials                 User credentials
 * @apiParam {String}     credentials.username        User name.
 * @apiParam {String}     credentials.password        Password.
 * @apiParam {String}     updatedOn                   Date time stamp of the configuration update.
 * @apiParam {String}     version                     Configuration unique version (UUID).
 * @apiParam {String}     deviceKey                   Unique device key (UUID).
 * 
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *   "version": "aeea36cc-cefb-47db-ac0f-8c5dbc95b0f9",
 *   "updatedOn": 1457365253000
 * }
 * 
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>
 * @apiSuccess {String}   deviceKey           Unique device key (UUID).
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "deviceKey": "75b2a82c-d41e-4a2f-b281-3047073ee474",
 *   "success": true
 * }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function deviceNotify() { return; }

/**
 * @api {post} /v1/device/reset Deletes a device registration
 * @apiVersion 0.0.1
 * @apiName DeviceReset
 * @apiGroup Device
 * @apiPermission ROLE_ADMIN
 * 
 * @apiDescription Deletes a device registration. After a device is deleted, any data become inaccessible.
 *
 * @apiParam {Object}     credentials                 User credentials
 * @apiParam {String}     credentials.username        User name.
 * @apiParam {String}     credentials.password        Password.
 * @apiParam {String}     deviceKey                   Device unique id (UUID).
 * 
* @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"admin@alicante.daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey":"4e0a0f34-06ad-4c49-a935-89eceea56f27",
 * }
 * 
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true
 * }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function registerRest() { return; }
