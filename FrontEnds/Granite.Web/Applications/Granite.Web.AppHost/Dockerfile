FROM nginx:alpine

RUN apk add --no-cache openssl gettext \
    && mkdir -p /etc/nginx/ssl \
    && openssl req -x509 -nodes -days 365 \
       -newkey rsa:2048 \
       -keyout /etc/nginx/ssl/selfsigned.key \
       -out /etc/nginx/ssl/selfsigned.crt \
       -subj "/CN=localhost"

COPY dist/ /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY config.js.template /etc/nginx/templates/config.js.template
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENV API_BASE_URL=""

EXPOSE 443
ENTRYPOINT ["/docker-entrypoint.sh"]
