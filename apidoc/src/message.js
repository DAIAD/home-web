/**
 * @apiDefine Credentials User credentials used for authentication
 * @apiParam {String} username User name
 * @apiParam {String} password User password
 */

/**
 * @api {post} /v1/message Load messages
 * @apiVersion 0.0.1
 * @apiName Message
 * @apiGroup Message
 * @apiPermission ROLE_USER
 *
 * @apiDescription Loads all available messages
 *
 * @apiUse Credentials
 * 
 * @apiParamExample {json} Request Example
 * {
 *   username: "user@daiad.eu",
 *   password: "****"
 * }
 * 
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 * @apiSuccess {Object[]} messages                 Collection of <code>Message</code> objects. Valid messages types are <code>Alert</code>, <code>StaticRecommendation</code>, <code>DynamicRecommendation</code> and <code>Announcement</code>.
 * 
 * @apiSuccess (Message) {String} type             Message type. Valid values are <code>ALERT</code>, <code>RECOMMENDATION_STATIC</code>, <code>RECOMMENDATION_DYNAMIC</code> and <code>ANNOUNCEMENT</code>.
 * 
 * @apiSuccess (Alert extends Message) {Number} id Message unique id.
 * @apiSuccess (Alert extends Message) {String} alert Alert type. Valid values are:
 * <br/><code>WATER_LEAK</code>
 * <br/><code>SHOWER_ON</code>
 * <br/><code>WATER_FIXTURES</code>
 * <br/><code>UNUSUAL_ACTIVITY</code>
 * <br/><code>WATER_QUALITY</code>
 * <br/><code>HOT_TEMPERATURE</code>
 * <br/><code>NEAR_DAILY_WATER_BUDGET</code>
 * <br/><code>NEAR_WEEKLY_WATER_BUDGET</code>
 * <br/><code>NEAR_DAILY_SHOWER_BUDGET</code>
 * <br/><code>NEAR_WEEKLY_SHOWER_BUDGET</code>
 * <br/><code>REACHED_DAILY_WATER_BUDGET</code>
 * <br/><code>REACHED_DAILY_SHOWER_BUDGET</code>
 * <br/><code>WATER_CHAMPION</code>
 * <br/><code>SHOWER_CHAMPION</code>
 * <br/><code>TOO_MUCH_WATER_SWM</code>
 * <br/><code>TOO_MUCH_WATER_AMPHIRO</code>
 * <br/><code>TOO_MUCH_ENERGY</code>
 * <br/><code>REDUCED_WATER_USE</code>
 * <br/><code>IMPROVED_SHOWER_EFFICIENCY</code>
 * <br/><code>WATER_EFFICIENCY_LEADER</code>
 * <br/><code>KEEP_UP_SAVING_WATER</code>
 * <br/><code>GOOD_JOB_MONTHLY</code>
 * <br/><code>LITERS_ALREADY_SAVED</code>
 * <br/><code>TOP_25_PERCENT_OF_SAVERS</code>
 * <br/><code>TOP_10_PERCENT_OF_SAVERS</code>
 * <br/><code>DID_YOU_KNOW2</code>
 * <br/><code>DID_YOU_KNOW3</code>
 * @apiSuccess (Alert extends Message) {Number} priority Message priority.
 * @apiSuccess (Alert extends Message) {String} title Short description.
 * @apiSuccess (Alert extends Message) {String} description Long description.
 * @apiSuccess (Alert extends Message) {String} imageLink Image resource link.
 * @apiSuccess (Alert extends Message) {Number} createdOn Date created time stamp.
 * 
 * @apiSuccess (StaticRecommendation extends Message) {Number} id Message unique id.
 * @apiSuccess (StaticRecommendation extends Message) {Number} index Message index.
 * @apiSuccess (StaticRecommendation extends Message) {String} title Short description.
 * @apiSuccess (StaticRecommendation extends Message) {String} description Long description.
 * @apiSuccess (StaticRecommendation extends Message) {String} imageEncoded Base64 encoded image.
 * @apiSuccess (StaticRecommendation extends Message) {String} imageLink Image resource link.
 * @apiSuccess (StaticRecommendation extends Message) {String} prompt Prompt message.
 * @apiSuccess (StaticRecommendation extends Message) {String} externaLink External link.
 * @apiSuccess (StaticRecommendation extends Message) {String} source Message source.
 * @apiSuccess (StaticRecommendation extends Message) {Number} createdOn Date created time stamp.
 * @apiSuccess (StaticRecommendation extends Message) {Number} modifiedOn Date modified time stamp.
 * @apiSuccess (StaticRecommendation extends Message) {Boolean} active <code>true</code> if message is acrive; Otherwise <code>false</code>.
 * 
 * @apiSuccess (DynamicRecommendation extends Message) {Number} id Message unique id.
 * @apiSuccess (DynamicRecommendation extends Message) {String} recommendation Recommendation type. Valid values are:
 * <br/><code>LESS_SHOWER_TIME</code>
 * <br/><code>LOWER_TEMPERATURE</code>
 * <br/><code>LOWER_FLOW</code>
 * <br/><code>CHANGE_SHOWERHEAD</code>
 * <br/><code>SHAMPOO_CHANGE</code>
 * <br/><code>REDUCE_FLOW_WHEN_NOT_NEEDED</code>
 * @apiSuccess (DynamicRecommendation extends Message) {Number} priority Message priority.
 * @apiSuccess (DynamicRecommendation extends Message) {String} title Short description.
 * @apiSuccess (DynamicRecommendation extends Message) {String} description Long description.
 * @apiSuccess (DynamicRecommendation extends Message) {String} imageLink Image resource link.
 * @apiSuccess (DynamicRecommendation extends Message) {Number} createdOn Date created time stamp.
 * 
 * @apiSuccess (Announcement extends Message) {Number} id Message unique id.
 * @apiSuccess (Announcement extends Message) {Number} priority Message priority.
 * @apiSuccess (Announcement extends Message) {String} title Short description.
 * @apiSuccess (Announcement extends Message) {String} content Announcement details.
 * @apiSuccess (Announcement extends Message) {String} link Image resource link.
 * @apiSuccess (Announcement extends Message) {Number} createdOn Date created time stamp.
 * 
 * @apiSuccessExample {json} Message response
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "messages": [ {
 *     "id": 3439,
 *     "alert": "NEAR_WEEKLY_WATER_BUDGET",
 *     "priority": 2,
 *     "title": "Ya has consumido el 80% de lo que sueles consumir normalmente a la semana",
 *     "description": "Ya has consumo 297 litros, y normalmente gastas 350 a la semana. ¿Quieres algunas recomendaciones para ahorrar agua?",
 *     "imageLink": null,
 *     "createdOn": 1463086877922,
 *     "type": "ALERT"
 *   }, {
 *     "id": 120,
 *     "index": 60,
 *     "title": "Refréscate de forma inteligente durante el verano",
 *     "description": "Cuando usas juguetes acuáticos como piscinas, toboganes acuáticos… piensa que éstos no consumen tanta agua como parece.",
 *     "imageEncoded": null,
 *     "imageLink": null,
 *     "prompt": null,
 *     "externaLink": null,
 *     "source": "http://www.50plus-treff.de/magazin/leben/wasser-sparen-im-haushalt-10-tipps-63.html ",
 *     "createdOn": null,
 *     "modifiedOn": null,
 *     "active": true,
 *     "type": "RECOMMENDATION_STATIC"
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
 * @apiErrorExample Response (example):
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USERNANE_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 */
function message() { return; }

/**
 * @api {post} /v1/message/acknowledge/:type/:id Acknowledge message
 * @apiVersion 0.0.1
 * @apiName MessageAcknowledge
 * @apiGroup Message
 * @apiPermission ROLE_USER
 *
 * @apiDescription Marks a message as acknowledged.
 *
 * @apiUse Credentials
 * @apiParam (URL Parameter) {String} type Message type. Valid values are <code>ALERT</code>, <code>RECOMMENDATION_STATIC</code>, <code>RECOMMENDATION_DYNAMIC</code> and <code>ANNOUNCEMENT</code>. This parameter is not case sensitive.
 * @apiParam (URL Parameter) {Number} id Message unique id
 * 
 * @apiParamExample {json} Request Example
 * POST /v1/message/acknowledge/alert/12 HTTP/1.1
 * {
 *   username: "user@daiad.eu",
 *   password: "****"
 * }
 * 
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Response (example):
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "SharedErrorCode.UNKNOWN",
 *     description: "Unknown message acknowledgement type."
 *   }],
 *   success: false
 * }
 */
function acknowledge() { return; }
