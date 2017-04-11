/**
 * @api {post} action/user/password/change Change password
 * @apiVersion 0.0.1
 * @apiName PasswordChange
 * @apiGroup User
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Changes the password of a user. If the authenticated user is an administrator, he can change the passwords of other users by setting the <code>username</code> property. The administrator must have access to the utility of the user.
 *
 * @apiParam {String} [username]      The name of the user for which the password is being changed.If the user does not have the <code>ROLE_SYSTEM_ADMIN</code> or <code>ROLE_UTILITY_ADMIN</code> role, this parameter is ignored and an error is returned.
 * @apiParam {String} password        The new password.
 * @apiParam {String} captcha         The reCAPTCHA challenge token.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "password":"****",
 *   "captcha":""
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
 *     code: "CaptchaErrorCode.CAPTCHA_VERIFICATION_ERROR",
 *     description: "CAPTCHA verification has failed."
 *   }],
 *   success: false
 * }
 *
 */
function changePassword() { return; }

/**
 * @api {post} action/user/password/reset/token/create Request password reset token
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
 *   "username":"user@daiad.eu",
 *   "application":"HOME"
 * }
 *
 * @apiSuccess {Boolean}  success           Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Array of <code>Error</code>
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
 *     code: "UserErrorCode.PASSWORD_RESET_APPLICATION_NOT_SUPPORTED",
 *     description: "Application is not supported."
 *   }],
 *   success: false
 * }
 *
 */
function passwordResetTokenCreate() { return; }

/**
 * @api {post} action/user/password/reset/token/redeem Redeem password reset token
 * @apiVersion 0.0.1
 * @apiName ResetPasswordTokenRedeem
 * @apiGroup User
 *
 * @apiDescription Resets a user's password given a valid token and password.
 *
 * @apiParam {String} token     A valid password reset token.
 * @apiParam {String} password  The new password.
 * @apiParam {String} pin       Four digit PIN required for reseting the password.
 * @apiParam {String} captcha   The reCAPTCHA challenge token.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "token":"ff9496fa-db91-4c7c-8d8d-9f41a140f553",
 *   "pin":"3904",
 *   "password":"****",
 *   "captcha":""
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
 *     code: "PasswordErrorCode.WEAK_PASSWORD",
 *     description: "Password is weak."
 *   }],
 *   success: false
 * }
 *
 */
function passwordResetTokenRedeem() { return; }
