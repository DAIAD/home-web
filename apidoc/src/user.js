/**
 * @api {post} /v1/user/register Register a new user
 * @apiVersion 0.0.1
 * @apiName UserRegister
 * @apiGroup User
 * 
 * @apiDescription Registers a new user.  <b><span class="note">If white list constraint is not enabled, all properties are required</span></b>.
 *
 * @apiParam {Object} account                 Account information
 * @apiParam {String} account.username        Unique user name.
 * @apiParam {String} account.password        Password.
 * @apiParam {String} [account.firstname]     First name.
 * @apiParam {String} [account.lastname]      Last name.
 * @apiParam {String} [account.gender]        Gender. Valid values are <code>MALE</code> and <code>FEMALE</code>.
 * @apiParam {String} [account.birthdate]     Birth date in ISO8601 format i.e. 2016-05-13T16:55:47+00:00
 * @apiParam {String} [account.country]       Country.
 * @apiParam {String} [account.city]          City.
 * @apiParam {String} [account.address]       Address.
 * @apiParam {String} [account.timezone]      Preferred time zone.
 * @apiParam {String} [account.zip]           Postal code.
 * @apiParam {String} [account.locale]        Locale. Valid values are <code>en</code>, <code>el</code>, <code>es</code> and <code>de</code>
 * @apiParam {Object} [account.location]      Location expressed using GeoJSON.
 * 
 * @apiParamExample {json} Request Example (White List Filtering)
 * {
 *   "account" : {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****"
 *   }
 * }
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "account" : {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****",
 *     "firstname":"George",
 *     "lastname":"Papadopoulos",
 *     "gender": "MALE",
 *     "birthdate":"2016-05-13T16:55:47+02:00",
 *     "country":"Greece",
 *     "zip":null,
 *     "timezone":"Europe/Athens",
 *     "locale":"el"
 *   }
 * }
 *
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>
 * @apiSuccess {String}   userKey             Unique user key (UUID).
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
 *     code: "UserErrorCode.USERNANE_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function registerUser() { return; }
