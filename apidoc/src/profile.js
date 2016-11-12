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
 * @apiSuccess (Profile) {String} version         Profile current version (UUID).
 * @apiSuccess (Profile) {String} application     Identifier of the application to which the user has been authenticated. Valid values are <code>HOME</code>, <code>UTILITY</code> and <code>MOBILE</code>. Users can override the application by adding any of the aformentioned values to the query request parameter <code>application</code>. If not application is set, users with role <code>ROLE_ADMIN</code> are automatically authenticated to the <code>UTILITY</code> application. If users have no sufficient permissions, authentication is performed against the <code>MOBILE</code> application.
 * @apiSuccess (Profile) {String} username        Authenticated user name.
 * @apiSuccess (Profile) {String} firstname       First name.
 * @apiSuccess (Profile) {String} lastname        Last name
 * @apiSuccess (Profile) {String} address         Address.
 * @apiSuccess (Profile) {Number} birthdate       Birthdate.
 * @apiSuccess (Profile) {String} gender          Gender. Valid values are <code>MALE</code>, <code>FEMALE</code> and <code>UNDEFINED</code>.
 * @apiSuccess (Profile) {String} zip             Postal code.
 * @apiSuccess (Profile) {String} email           Email address.
 * @apiSuccess (Profile) {String} photo           Base64 encoded user image.
 * @apiSuccess (Profile) {String} locale          Locale.
 * @apiSuccess (Profile) {String} timezone        Preferred time zone.
 * @apiSuccess (Profile) {String} country         Country.
 * @apiSuccess (Profile) {Number} mode            Application mode. Each application has different modes.
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
 * @apiSuccess (Profile)   {Boolean}    social                <code>true</code> if social features are enabled; Otherwise <code>false</code>.
 * @apiSuccess (Profile)   {String}     unit                  Measurement unit system. Valid values are:
 * <br/><br/><code>UNDEFINED</code>: Measurement unit system is not set.
 * <br/><code>METRIC</code>: Metric system
 * <br/><code>IMPERIAL</code>:  Imperial system
 * @apiSuccess (Profile)   {Boolean}    garden                <code>true</code> if the household has a garden; Otherwise <code>false</code>. The value may not be initialized. <code>null</code> may be returned.
 * @apiSuccess (Profile)   {Number}     dailyMeterBudget      Daily smart water meter water consumption budget.
 * @apiSuccess (Profile)   {Number}     dailyMeterBudget      Daily smart water meter water consumption budget.
 * @apiSuccess (Profile)   {Number}     dailyAmphiroBudget    Daily Amphiro B1 water consumption budget.
 * @apiSuccess (Profile)   {String}     configuration         Application configuration serialized as a JSON object.
 * @apiSuccess (Profile)   {Object[]}   devices               Array of <code>DeviceRegistration</code> objects representing the Amphiro or Smart Water Meter devices registered to the authenticated user. Instances are implemented by classes <code>WaterMeterDeviceRegistration</code> and <code>AmphiroDeviceRegistration</code>.
 * @apiSuccess (Profile)   {Object}     utility               Utility information.
 * @apiSuccess (Profile)   {Object}     household             Household information.
 *
 * @apiSuccess (DeviceRegistration) {String}     type              Device type. Valid values are <code>METER</code> and <code>AMPHIRO</code>.
 * @apiSuccess (DeviceRegistration) {String}     deviceKey         Unique device id (UUID).
 * @apiSuccess (DeviceRegistration) {Object[]}   properties        Array of <code>KeyValuePair</code> objects. representing device properties.
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
 * @apiSuccess (Utility)    {String}     id                  Utility unique numerical identifier.
 * @apiSuccess (Utility)    {String}     key                 Utility unique key (UUID).
 * @apiSuccess (Utility)    {String}     name                Name.
 * @apiSuccess (Utility)    {String}     country             Country.
 * @apiSuccess (Utility)    {String}     timezone            Time zone.
 * @apiSuccess (Utility)    {String}     locale              Locale.
 * @apiSuccess (Utility)    {String}     city                City.
 *
 * @apiSuccess (Household)  {Number}     createdOn           Creation time stamp.
 * @apiSuccess (Household)  {Number}     updatedOn           Last update time stamp.
 * @apiSuccess (Household)  {Number}     femaleMembers       Total number of female members.
 * @apiSuccess (Household)  {Number}     totalMembers        Total number of members.
 * @apiSuccess (Household)  {Number}     maleMembers         Total number of male members.
 * @apiSuccess (Household)  {Object[]}   members             Array of <code>Member</code> objects. The default member is not included in this collection.
 *
 * @apiSuccess (Member)     {Number}     index               Unique index.
 * @apiSuccess (Member)     {Boolean}    active              <code>true</code> if the member is not delete; Otherwise <code>false</code>.
 * @apiSuccess (Member)     {String}     name                Name.
 * @apiSuccess (Member)     {Number}     age                 Age.
 * @apiSuccess (Member)     {String}     gender              Gender. Valid values are <code>MALE</code> and <code>FEMALE</code>.
 * @apiSuccess (Member)     {String}     photo               Base64 encoded member image.
 * @apiSuccess (Member)     {Number}     createdOn           Creation time stamp.
 * @apiSuccess (Member)     {Number}     updatedOn           Last update time stamp.
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
 *     "social": true,
 *     "garden": true,
 *     "unit" : "METRIC",
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
 *     }],
 *     "utility":       {
 *       "id": 1,
 *       "key": "2b480134-6f05-488f-9f9b-99607a93c6c4",
 *       "name": "DAIAD",
 *       "country": "Greece",
 *       "timezone": "Europe/Athens",
 *       "locale": "el",
 *       "city": "Athens"
 *     },
 *     "household":{
 *       "members": [{
 *         "index": 14,
 *         "active": true,
 *         "name": "George",
 *         "age": 34,
 *         "gender": "MALE",
 *         "photo": null,
 *         "createdOn": 1474162191385,
 *         "updatedOn": 1474162451597
 *       }],
 *       "createdOn": 1474162191385,
 *       "updatedOn": 1474162191385,
 *       "femaleMembers": 0,
 *       "totalMembers": 1,
 *       "maleMembers": 1
 *     }
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
 * @apiParam (UpdateProfileRequest) {String}     [firstname]                 First name.
 * @apiParam (UpdateProfileRequest) {String}     [lastname]                  Last name
 * @apiParam (UpdateProfileRequest) {String}     address                     Address.
 * @apiParam (UpdateProfileRequest) {Number}     birthdate                   Birthdate.
 * @apiParam (UpdateProfileRequest) {String}     gender                      Gender. Valid values are <code>MALE</code>, <code>FEMALE</code> and <code>UNDEFINED</code>.
 * @apiParam (UpdateProfileRequest) {String}     country                     Country.
 * @apiParam (UpdateProfileRequest) {String}     zip                         Postal code.
 * @apiParam (UpdateProfileRequest) {String}     [locale]                    Locale.
 * @apiParam (UpdateProfileRequest) {String}     [timezone]                  Preferred time zone.
 * @apiParam (UpdateProfileRequest) {String}     photo                       Base64 encoded user image.
 * @apiParam (UpdateProfileRequest) {String}     unit                        Measurement unit system. Valid values are:
 * <br/><br/><code>UNDEFINED</code>: Measurement unit system is not set.
 * <br/><code>METRIC</code>: Metric system
 * <br/><code>IMPERIAL</code>:  Imperial system
 * @apiParam (UpdateProfileRequest) {Boolean}    garden                      <code>true</code> if the household has a garden; Otherwise <code>false</code>.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "configuration": "{\"property1\":1,\"property2\":\"value2\"}"}",
 *   "dailyMeterBudget": 300,
 *   "dailyAmphiroBudget": 80,
 *   "unit": "IMPERIAL",
 *   "garden": false
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

/**
 * @api {post} /v1/household Save household
 * @apiVersion 0.0.1
 * @apiName HouseholdSave
 * @apiGroup Profile
 * @apiPermission ROLE_USER
 *
 * @apiDescription Updates household member information. If an index is missing a new member is created. Existing indexes result in updating the existing members. Any indexes that exist in the database but are not included in the request, are deleted.
 *
 * @apiParam (UpdateHouseholdRequest) {Object}     credentials                 User credentials
 * @apiParam (UpdateHouseholdRequest) {String}     credentials.username        User name.
 * @apiParam (UpdateHouseholdRequest) {String}     credentials.password        Password.
 * @apiParam (UpdateHouseholdRequest) {Object[]}   members                     Array of <code>Member</code> objects.
 *
 * @apiParam (Member)     {Number}     index               Unique index.
 * @apiParam (Member)     {Boolean}    active              <code>true</code> if the member is not delete; Otherwise <code>false</code>.
 * @apiParam (Member)     {String}     name                Name.
 * @apiParam (Member)     {Number}     age                 Age.
 * @apiParam (Member)     {String}     gender              Gender. Valid values are <code>MALE</code> and <code>FEMALE</code>.
 * @apiParam (Member)     {String}     photo               Base64 encoded member image.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *   },
 *   "members": [{
 *     "index": 14,
 *     "active": true,
 *     "name": "George",
 *     "age": 34,
 *     "gender": "MALE",
 *     "photo": null
 *   }]
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
function saveHousehold() { return; }
