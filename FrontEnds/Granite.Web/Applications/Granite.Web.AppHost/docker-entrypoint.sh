#!/bin/sh
envsubst '${API_BASE_URL} ${GOOGLE_CLIENT_ID}' < /etc/nginx/templates/config.js.template > /usr/share/nginx/html/config.js
envsubst '${GATEWAY_URL}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'
