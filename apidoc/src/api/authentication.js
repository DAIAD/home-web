/**
 * @api {post} /v1/auth/login Login
 * @apiVersion 0.0.1
 * @apiName Login
 * @apiGroup Authentication
 *
 * @apiDescription Authenticate user credentials.
 *
 * @apiParam {String} username User name
 * @apiParam {String} password User password
 * @apiParam {String} version  DAIAD@home mobile application version.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   username: "user@daiad.eu",
 *   password: "****",
 *   version: "1.5.0"
 * }
 *
 * @apiSuccess {Boolean}  success                 Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                  Array of <code>Error</code>.
 * @apiSuccess {Object} runtime                   Information about the application runtime i.e. active application profile. If <code>success</code> is <code>false</code> this property will be <code>undefined</code>.
 * @apiSuccess {Object} profile                   Authenticated user information. If <code>success</code> is <code>false</code> this property will be <code>undefined</code>.
 * 
 * @apiSuccess (Runtime) {String[]} profile       Active application profiles i.e. <code>development</code> or <code>production</code>.
 * 
 * @apiSuccess (Profile) {String} key             User unique identifier (UUID).
 * @apiSuccess (Profile) {String} version         Profile current version (UUID).
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
 * @apiSuccess (Profile) {String} configuration   Application configuration serialized as a JSON object.
 * @apiSuccess (Profile) {Object[]} devices       Array of <code>DeviceRegistration</code> objects representing the Amphiro or Smart Water Meter devices registered to the authenticated user. Instances are implemented by classes <code>WaterMeterDeviceRegistration</code> and <code>AmphiroDeviceRegistration</code>.
 * @apiSuccess (Profile) {Number}     dailyMeterBudget            Daily smart water meter water consumption budget.
 * @apiSuccess (Profile) {Number}     dailyAmphiroBudget          Daily Amphiro B1 water consumption budget
 * @apiSuccess (Profile) {String}     configuration         Application configuration serialized as a JSON object.
 * @apiSuccess (Profile) {Object[]}   devices               Array of <code>DeviceRegistration</code> objects representing the Amphiro or Smart Water Meter devices registered to the authenticated user. Instances are implemented by classes <code>WaterMeterDeviceRegistration</code> and <code>AmphiroDeviceRegistration</code>.
 * @apiSuccess (Profile) {Object}     utility               Utility information.
 * @apiSuccess (Profile) {Object}     household             Household information.
 * 
 * @apiSuccess (DeviceRegistration) {String}     type              Device type. Valid values are <code>METER</code> and <code>AMPHIRO</code>.
 * @apiSuccess (DeviceRegistration) {String}     deviceKey         Unique device id (UUID).
 * @apiSuccess (DeviceRegistration) {Object[]}   properties        Array of <code>KeyValuePair</code> objects representing device properties.
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
 *     "username": "user@daiad.eu",
 *     "firstname": "George",
 *     "lastname": "Papadopoulos",
 *     "email": "user@daiad.eu",
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
 *       "properties": [ {
 *         "key": "debug.autogenerate",
 *         "value": "2016-04-20T11:36:50.111Z"
 *       }]
 *     }, {
 *       "type": "METER",
 *       "deviceKey": "48944e9f-471d-4a9a-a926-f23dee64ae6a",
 *       "serial": "C12FA154674"
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
function login() { return; }
