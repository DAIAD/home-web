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
 * @apiParam {String} [account.photo]         User phote as a base64 encoded image.
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

/**
 * @api {post} /v1/user/password/change Change a user's password
 * @apiVersion 0.0.1
 * @apiName PasswordChange
 * @apiGroup User
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Changes the password of a user. If the authenticated user is an administrator, he can change the passwords of other users of the same utility by setting the <code>username</code> property.
 *
 * @apiParam {Object} credentials             User credentials
 * @apiParam {String} credentials.username    User name.
 * @apiParam {String} credentials.password    Password.
 * @apiParam {String} [username]        The name of the user for which the password is being changed.If the user does not have the <code>ROLE_ADMIN</code> role, this parameter is ignored and an error is returned.
 * @apiParam {String} password        The new password.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials" : {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****"
 *   },
 *   "password":"****"
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
 *     code: "UserErrorCode.USERNANE_NOT_FOUND",
 *     description: "Account demo@daiad.eu was not found."
 *   }],
 *   success: false
 * }
 *
 */
function changePassword() { return; }

/**
 * @api {post} /v1/user/password/reset/token/create Request a password reset token
 * @apiVersion 0.0.1
 * @apiName ResetPasswordTokenRequest
 * @apiGroup User
 *
 * @apiDescription Requests a token for resetting a user's password. A token is generated only if the user has the <code>ROLE_USER</code>. If the user is an administrator an error is returned.
 *
 * @apiParam {String} username    The name of the user that requests the password reset token.
 * @apiParam {String} application The application requesting the password reset token. Valid values are:</br>
 * <code>HOME</code></br>
 * <code>UTILITY</code></br>
 * <code>MOBILE</code></br>
 * Application controls the template used for rendering the mail content. <code>HOME</code> and <code>UTILITY</code> return a reset URL and a PIN. <code>MOBILE</code> returns only the PIN.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "username":"george.papadopoulos@daiad.eu",
 *   "application":"MOBILE"
 * }
 *
 * @apiSuccess {Boolean}  success           Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Array of <code>Error</code>
 * @apiSuccess {String}   token             The password reset token.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true,
 *   "token": "8232ac30-212b-48a7-a322-e348b3199f8d"
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
 *     code: "SharedErrorCode.UNKNOWN",
 *     description: "Internal server error has occurred."
 *   }],
 *   success: false
 * }
 *
 */
function passwordResetTokenCreate() { return; }

/**
 * @api {post} /v1/user/password/reset/token/redeem Redeem a password reset token
 * @apiVersion 0.0.1
 * @apiName ResetPasswordTokenRedeem
 * @apiGroup User
 *
 * @apiDescription Resets a user's password given a valid token and password.
 *
 * @apiParam {String} token     A valid password reset token.
 * @apiParam {String} password  The new password.
 * @apiParam {String} pin       Four digit PIN required for reseting the password. 
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "token":"ff9496fa-db91-4c7c-8d8d-9f41a140f553",
 *   "pin":"3904",
 *   "password":"****"
 * }
 *
 * @apiSuccess {Boolean}  success           Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Array of <code>Error</code>
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true,
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
 *     code: "SharedErrorCode.UNKNOWN",
 *     description: "Internal server error has occurred."
 *   }],
 *   success: false
 * }
 *
 */
function passwordResetTokenRedeem() { return; }

/**
 * @api {put} /v1/user/role Grant a role
 * @apiVersion 0.0.1
 * @apiName GrantRole
 * @apiGroup User
 * @apiPermission ROLE_ADMIN
 *
 * @apiDescription Grants a role to a user.
 *
 * @apiParam {Object} credentials             User credentials
 * @apiParam {String} credentials.username    User name.
 * @apiParam {String} credentials.password    Password.
 * @apiParam {String} username                The of the user to which the role is granted to.
 * @apiParam {String} role                    The role name. Valid values are:<br />
 *                                            <code>ROLE_USER</code>
 *                                            <code>ROLE_SUPERUSER</code>
 *                                            <code>ROLE_ADMIN</code>
 *
 * @apiParamExample {json} Request Example
 * {
 *   "account" : {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****"
 *   },
 *   "username":"user@daiad.eu",
 *   "role":"ROLE_SUPERUSER"
 * }
 *
 * @apiSuccess {Boolean}  success           Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Array of <code>Error</code>
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true,
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
 *     code: "SharedErrorCode.UNKNOWN",
 *     description: "Internal server error has occurred."
 *   }],
 *   success: false
 * }
 *
 */
function grantRole() { return; }

/**
 * @api {delete} /v1/user/role Revoke a role
 * @apiVersion 0.0.1
 * @apiName RevokeRole
 * @apiGroup User
 * @apiPermission ROLE_ADMIN
 *
 * @apiDescription Revokes a role from a user.
 *
 * @apiParam {Object} credentials             User credentials
 * @apiParam {String} credentials.username    User name.
 * @apiParam {String} credentials.password    Password.
 * @apiParam {String} username                The of the user to which the role is granted to.
 * @apiParam {String} role                    The role name. Valid values are:<br />
 *                                            <code>ROLE_USER</code>
 *                                            <code>ROLE_SUPERUSER</code>
 *                                            <code>ROLE_ADMIN</code>
 *
 * @apiParamExample {json} Request Example
 * {
 *   "account" : {
 *     "username":"george.papadopoulos@daiad.eu",
 *     "password":"****"
 *   },
 *   "username":"user@daiad.eu",
 *   "role":"ROLE_ADMIN"
 * }
 *
 * @apiSuccess {Boolean}  success           Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Array of <code>Error</code>
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true,
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
 *     code: "SharedErrorCode.UNKNOWN",
 *     description: "Internal server error has occurred."
 *   }],
 *   success: false
 * }
 *
 */
function revokeRole() { return; }
