This document describes the DAIAD Action API Interface.

All Action API requests must set the HTTP header `X-CSRF-TOKEN` to the value of the CSRF token. The latter is initially stored in the HTML <meta> element named `_csrf`. The application is responsible for extracting and storing the CSRF value received by the response in the `X-CSRF-TOKEN` response header to be used in subsequent requests.
