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
 * @apiDescription Loads all available messages. All messages returned derive from  <code>Message</code>.
 *
 * @apiParam                            {Object}   credentials            User credentials used for authentication.
 * @apiParam                            {String}   credentials.username   User name
 * @apiParam                            {String}   credentials.password   User password
 * @apiParam                            {Object[]} pagination             Array of <code>DataPagingOptions</code> objects. Each data paging options object contains information about fetching data for a specific type of messages. If more than one option objects are found for a single message type, the first overrides the others.
 *
 * @apiParam (DataPagingOptions)   {Number}   type                   Message type. Valid values are <code>ALERT</code>, <code>RECOMMENDATION_STATIC</code>, <code>RECOMMENDATION_DYNAMIC</code> and <code>ANNOUNCEMENT</code>. This parameter is not case sensitive.
 * @apiParam (DataPagingOptions)   {Number}   [index]                Data paging starts from this index. If not set, data paging starts from the first record.
 * @apiParam (DataPagingOptions)   {Number}   [size]                 Number of records to return. If not set, all records are returned.
 * @apiParam (DataPagingOptions)   {Boolean}  [ascending]            <code>true</code> for ascending sorting; Otherwise <code>false</code>. Sorting is performed on the message id field. Sorting on message id field is similar to sorting over creation date. Default value is <code>true</code>.
 * @apiParam (DataPagingOptions)   {Number}   [minMessageId]         Filter data by minimum message id. This option is most useful to the <code>MOBILE</code> client if only the messages after a specific message are required.
 *
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   },
 *   "pagination": [{
 *     "type": "ALERT",
 *     "index": 1,
 *     "size": 3,
 *     "ascending": true,
 *     "minMessageId": 1453
 *   }]
 * }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 * @apiSuccess {Object[]} alerts                   Array of <code>Alert</code> objects.
 * @apiSuccess {Object[]} recommendations          Array of <code>DynamicRecommendation</code> objects.
 * @apiSuccess {Object[]} tips                     Array of <code>StaticRecommendation</code> objects.
 * @apiSuccess {Object[]} announcements            Array of <code>Announcement</code> objects.
 *
 *
 * @apiSuccess (Message) {String} type             Message type. Valid values are <code>ALERT</code>, <code>RECOMMENDATION_STATIC</code>, <code>RECOMMENDATION_DYNAMIC</code> and <code>ANNOUNCEMENT</code>.
 *
 * @apiSuccess (Alert extends Message) {Number} id        Message unique id.
 * @apiSuccess (Alert extends Message) {String} alert     Alert type. Valid values are:
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
 * @apiSuccess (Alert extends Message) {Number} priority       Message priority.
 * @apiSuccess (Alert extends Message) {String} title          Short description.
 * @apiSuccess (Alert extends Message) {String} description    Long description.
 * @apiSuccess (Alert extends Message) {String} imageLink      Image resource link.
 * @apiSuccess (Alert extends Message) {Number} createdOn      Date created time stamp.
 * @apiSuccess (Alert extends Message) {Number} acknowledgedOn Date acknoledged time stamp. This is the date the user has marked the message as read from the mobile/web application.
 *
 * @apiSuccess (StaticRecommendation extends Message) {Number}  id                 Message unique id.
 * @apiSuccess (StaticRecommendation extends Message) {Number}  index              Message index.
 * @apiSuccess (StaticRecommendation extends Message) {String}  title              Short description.
 * @apiSuccess (StaticRecommendation extends Message) {String}  description        Long description.
 * @apiSuccess (StaticRecommendation extends Message) {String}  imageEncoded       Base64 encoded image.
 * @apiSuccess (StaticRecommendation extends Message) {String}  imageLink          Image resource link.
 * @apiSuccess (StaticRecommendation extends Message) {String}  prompt             Prompt message.
 * @apiSuccess (StaticRecommendation extends Message) {String}  externalLink       External link.
 * @apiSuccess (StaticRecommendation extends Message) {String}  source             Message source.
 * @apiSuccess (StaticRecommendation extends Message) {Number}  createdOn          Date created time stamp.
 * @apiSuccess (StaticRecommendation extends Message) {Number}  modifiedOn         Date modified time stamp.
 * @apiSuccess (StaticRecommendation extends Message) {Boolean} active             <code>true</code> if message is acrive; Otherwise <code>false</code>.
 * @apiSuccess (StaticRecommendation extends Message) {Number}  acknowledgedOn     Date acknoledged time stamp. This is the date the user has marked the message as read from the mobile/web application.
 *
 * @apiSuccess (DynamicRecommendation extends Message) {Number} id                Message unique id.
 * @apiSuccess (DynamicRecommendation extends Message) {String} recommendation    Recommendation type. Valid values are:
 * <br/><code>LESS_SHOWER_TIME</code>
 * <br/><code>LOWER_TEMPERATURE</code>
 * <br/><code>LOWER_FLOW</code>
 * <br/><code>CHANGE_SHOWERHEAD</code>
 * <br/><code>SHAMPOO_CHANGE</code>
 * <br/><code>REDUCE_FLOW_WHEN_NOT_NEEDED</code>
 * @apiSuccess (DynamicRecommendation extends Message) {Number} priority          Message priority.
 * @apiSuccess (DynamicRecommendation extends Message) {String} title             Short description.
 * @apiSuccess (DynamicRecommendation extends Message) {String} description       Long description.
 * @apiSuccess (DynamicRecommendation extends Message) {String} imageLink         Image resource link.
 * @apiSuccess (DynamicRecommendation extends Message) {Number} createdOn         Date created time stamp.
 * @apiSuccess (DynamicRecommendation extends Message) {Number} acknowledgedOn    Date acknoledged time stamp. This is the date the user has marked the message as read from the mobile/web application.
 *
 * @apiSuccess (Announcement extends Message) {Number} id               Message unique id.
 * @apiSuccess (Announcement extends Message) {Number} priority         Message priority.
 * @apiSuccess (Announcement extends Message) {String} title            Short description.
 * @apiSuccess (Announcement extends Message) {String} content          Announcement details.
 * @apiSuccess (Announcement extends Message) {String} link             Image resource link.
 * @apiSuccess (Announcement extends Message) {Number} createdOn        Date created time stamp.
 * @apiSuccess (Announcement extends Message) {Number} acknowledgedOn   Date acknoledged time stamp. This is the date the user has marked the message as read from the mobile/web application.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "alerts": [{
 *     "id": 3439,
 *     "alert": "NEAR_WEEKLY_WATER_BUDGET",
 *     "priority": 2,
 *     "title": "Ya has consumido el 80% de lo que sueles consumir normalmente a la semana",
 *     "description": "Ya has consumo 297 litros, y normalmente gastas 350 a la semana. ¿Quieres algunas recomendaciones para ahorrar agua?",
 *     "imageLink": null,
 *     "createdOn": 1463086877922,
 *     "acknowledgedOn": null,
 *     "type": "ALERT"
 *   }],
 *   "recommendations": [],
 *   "tips": [{
 *     "id": 120,
 *     "index": 60,
 *     "title": "Refréscate de forma inteligente durante el verano",
 *     "description": "Cuando usas juguetes acuáticos como piscinas, toboganes acuáticos… piensa que éstos no consumen tanta agua como parece.",
 *     "imageEncoded": null,
 *     "imageLink": null,
 *     "prompt": null,
 *     "externalLink": null,
 *     "source": "http://www.50plus-treff.de/magazin/leben/wasser-sparen-im-haushalt-10-tipps-63.html ",
 *     "createdOn": null,
 *     "modifiedOn": null,
 *     "acknowledgedOn": null,
 *     "active": true,
 *     "type": "RECOMMENDATION_STATIC"
 *   }],
 *   "announcements": [],
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
 */
function message() { return; }

/**
 * @api {post} /v1/message/acknowledge Acknowledge messages
 * @apiVersion 0.0.1
 * @apiName MessageAcknowledge
 * @apiGroup Message
 * @apiPermission ROLE_USER
 *
 * @apiDescription Marks one or more messages as acknowledged.
 *
 * @apiParam                            {Object}   credentials            User credentials used for authentication.
 * @apiParam                            {String}   credentials.username   User name
 * @apiParam                            {String}   credentials.password   User password
 * @apiParam                            {Object[]} messages               Array of <code>MessageAcknowledgement</code>.
 *
 * @apiParam (MessageAcknowledgement)   {Number}   type                   Message type. Valid values are <code>ALERT</code>, <code>RECOMMENDATION_STATIC</code>, <code>RECOMMENDATION_DYNAMIC</code> and <code>ANNOUNCEMENT</code>. This parameter is not case sensitive.
 * @apiParam (MessageAcknowledgement)   {Number}   id                     Unique message id. This id is unique per message type.
 * @apiParam (MessageAcknowledgement)   {Number}   timestamp              Time stamp the message was read by the user i.e. the time stamp at the mobile device.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   },
 *   "messages": [{
 *     "id": 1625,
 *     "timestamp": 1463667895000,
 *     "type": "ALERT"
 *   }, {
 *     "id": 1797,
 *     "timestamp": 1463667895000,
 *     "type": "ALERT"
 *   }]
 * }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   success: true
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
 *     description: "Unknown message acknowledgement type."
 *   }],
 *   success: false
 * }
 */
function acknowledge() { return; }
