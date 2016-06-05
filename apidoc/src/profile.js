/**
 * @api {post} /v1/profile/load Load profile
 * @apiVersion 0.0.1
 * @apiName ProfileLoad
 * @apiGroup Profile
 * @apiPermission ROLE_USER
 *
 * @apiDescription Loads user profile as an instance of <code>ProfileResponse</code> with information about the system runtime, application configuration and user devices.
 *
 * @apiParam {String} username User name
 * @apiParam {String} password User password
 *
 * @apiParamExample {json} Request Example
 * {
 *   username: "user@daiad.eu",
 *   password: "****"
 * }
 *
 * @apiSuccess (ProfileResponse) {Boolean}  success                 Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (ProfileResponse) {Object[]} errors                  Array of <code>Error</code>.
 * @apiSuccess (ProfileResponse) {Object} runtime                   Information about the application runtime i.e. active application profile. If <code>success</code> is <code>false</code> this property will be <code>undefined</code>.
 * @apiSuccess (ProfileResponse) {Object} profile                   Authenticated user information. If <code>success</code> is <code>false</code> this property will be <code>undefined</code>.
 *
 * @apiSuccess (Runtime) {String[]} profile       Active application profiles i.e. <code>development</code> or <code>production</code>.
 *
 * @apiSuccess (Profile) {String} key             User unique identifier (UUID).
 * @apiSuccess (Profile) {String} version         Profie current version (UUID).
 * @apiSuccess (Profile) {String} application     Identifier of the application to which the user has been authenticated. Valid values are <code>HOME</code>, <code>UTILITY</code> and <code>MOBILE</code>. Users can override the application by adding any of the aformentioned values to the query request parameter <code>application</code>. If not application is set, users with role <code>ROLE_ADMIN</code> are automatically authenticated to the <code>UTILITY</code> application. If users have no sufficient permissions, authentication is performed against the <code>MOBILE</code> application.
 * @apiSuccess (Profile) {String} username        Authenticated user name.
 * @apiSuccess (Profile) {String} firstname       First name.
 * @apiSuccess (Profile) {String} lastname        Last name
 * @apiSuccess (Profile) {String} email           Email address.
 * @apiSuccess (Profile) {String} photo           Base64 encoded user image.
 * @apiSuccess (Profile) {String} locale          Locale.
 * @apiSuccess (Profile) {String} timezone        Preferred time zone.
 * @apiSuccess (Profile) {String} country         Country.
 * @apiSuccess (Profile) {Number} mode            Application mode. Each application have different modes.
 * <br/><br/>Application <code>HOME</code> modes:
 * <br/>1: <code>ACTIVE</code>
 * <br/>2: <code>INACTIVE</code>
 * <br/><br/>Application <code>UTILITY</code> modes:
 * <br/>1: <code>ACTIVE</code>
 * <br/>2: <code>INACTIVE</code>
 * <br/><br/>Application <code>MOBILE</code> modes:
 * <br/>1: <code>ACTIVE</code>
 * <br/>2: <code>INACTIVE</code>
 * <br/>3: <code>LEARNING</code>
 * <br/>4: <code>BLOCK</code>
 * @apiSuccess (Profile) {Number}     dailyMeterBudget            Daily smart water meter water consumption budget.
 * @apiSuccess (Profile) {Number}     dailyAmphiroBudget          Daily Amphiro B1 water consumption budget.
 * @apiSuccess (Profile) {String} configuration   Application configuration serialized as a JSON object.
 * @apiSuccess (Profile) {Object[]} devices       Array of <code>DeviceRegistration</code> objects representing the Amphiro or Smart Water Meter devices registered to the authenticated user. Instances are implemented by classes <code>WaterMeterDeviceRegistration</code> and <code>AmphiroDeviceRegistration</code>.
 *
 * @apiSuccess (DeviceRegistration) {String}     type              Device type. Valid values are <code>METER</code> and <code>AMPHIRO</code>.
 * @apiSuccess (DeviceRegistration) {String}     deviceKey         Unique device id (UUID).
 * @apiSuccess (DeviceRegistration) {Object[]}   properties        Array of <code>KeyValuePair</code> objects representing device properties.
 * @apiSuccess (DeviceRegistration) {Number}     registeredOn      Device registration time stamp.
 *
 * @apiSuccess (KeyValuePair) {String}           key               Key.
 * @apiSuccess (KeyValuePair) {String}           value             Value.
 *
 * @apiSuccess (WaterMeterDeviceRegistration extends DeviceRegistration) {String}     serial     Smart Water Meter unique serial Id.
 *
 * @apiSuccess (AmphiroDeviceRegistration extends DeviceRegistration)    {String}     name       User friendly name for the device i.e. Shower #1.
 * @apiSuccess (AmphiroDeviceRegistration extends DeviceRegistration)    {String}     macAddress Device unique MAC address.
 * @apiSuccess (AmphiroDeviceRegistration extends DeviceRegistration)    {String}     aesKey     Device AES key.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "runtime": {
 *     "profile": ["development"]
 *   },
 *   "profile": {
 *     "key": "4e0a0f34-06ad-4c49-a935-89eceea56f27",
 *     "version": "fd13abbe-1c1b-4571-b030-702fb5dcdc9a",
 *     "application": "MOBILE",
 *     "username": "george.papadopoulos@daiad.eu",
 *     "firstname": "George",
 *     "lastname": "Papadopoulos",
 *     "email": "george.papadopoulos@daiad.eu",
 *     "photo": null,
 *     "locale": "el",
 *     "timezone": "Europe/Athens",
 *     "country": "Greece",
 *     "mode": 1,
 *     "dailyMeterBudget": null,
 *     "dailyAmphiroBudget": 80,
 *     "configuration": null,
 *     "devices": [ {
 *       "type": "AMPHIRO",
 *       "deviceKey": "730c2e7a-fe22-4ca0-ac5b-500f97d2cf82",
 *       "name": "Amphiro #1",
 *       "macAddress": "9a:94:69:12:a1:f0",
 *       "aesKey": "Qz9-tZUDRQSHBEV7S50V3zjlH7lDL840QLJop-TgEVA",
 *       "registeredOn": 1461152246546,
 *       "properties": [ {
 *         "key": "debug.autogenerate",
 *         "value": "2016-04-20T11:36:50.111Z"
 *       }]
 *     }, {
 *       "type": "METER",
 *       "deviceKey": "48944e9f-471d-4a9a-a926-f23dee64ae6a",
 *       "serial": "C12FA154674",
 *       "registeredOn": 1461152246546,
 *       "properties": [ {
 *         "key": "import.date",
 *         "value": "2016-04-20T11:37:33.589Z"
 *       }, {
 *         "key": "import.file",
 *         "value": "3d0acf5c-e85b-4885-9476-77475913295d.xlsx"
 *       }]
 *     }]
 *   },
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
function loadProfile() { return; }

/**
 * @api {post} /v1/profile/save Save profile
 * @apiVersion 0.0.1
 * @apiName ProfileSave
 * @apiGroup Profile
 * @apiPermission ROLE_USER
 *
 * @apiDescription Saves profile information. This operation allows users to update their profile. Among other fields such as daily meter water consumption budget, the operation supports persistence of arbitrary client state data. For the latter, the operation is schema agnostic and expects a simple string. By convention this string is a serialized JSON object. The saved profile is loaded by <code>/api/v1/profile/load</code> operation in property <code>profile</code>.<code>configuration</code>. When API endpoint is used, profile is always assumed to be referring the <code>MOBILE</code> application. Updating <code>HOME</code> or <code>UTILITY</code> profile configuration requires using the corresponding action endpoint.
 *
 * @apiParam (UpdateProfileRequest) {Object}     credentials                 User credentials
 * @apiParam (UpdateProfileRequest) {String}     credentials.username        User name.
 * @apiParam (UpdateProfileRequest) {String}     credentials.password        Password.
 * @apiParam (UpdateProfileRequest) {String}     configuration               Client state expressed as a JSON serialized object.
 * @apiParam (UpdateProfileRequest) {Number}     dailyMeterBudget            Daily smart water meter water consumption budget.
 * @apiParam (UpdateProfileRequest) {Number}     dailyAmphiroBudget          Daily Amphiro B1 water consumption budget.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "configuration": "{\"property1\":1,\"property2\":\"value2\"}"}",
 *   "dailyMeterBudget": 300,
 *   "dailyAmphiroBudget": 80
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
function saveProfile() { return; }

/**
 * @api {post} /v1/profile/notify Notify mode update
 * @apiVersion 0.0.1
 * @apiName ProfileNotify
 * @apiGroup Profile
 * @apiPermission ROLE_USER
 *
 * @apiDescription Notifies server that an application profile has been applied. This operation is called by the mobile application when application mode has been updated.
 *
 * @apiParam {Object}     credentials                 User credentials
 * @apiParam {String}     credentials.username        User name.
 * @apiParam {String}     credentials.password        Password.
 * @apiParam {String}     updatedOn                   Date time stamp of the configuration update.
 * @apiParam {String}     version                     Configuration unique version (UUID).
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "updatedOn": 1457365253000,
 *   "version": "aeea36cc-cefb-47db-ac0f-8c5dbc95b0f9",
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
function notifyProfile() { return; }
