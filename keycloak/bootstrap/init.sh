#!/bin/sh
set -eu

KCADM="/opt/keycloak/bin/kcadm.sh"
SERVICE_CLIENT_ID="auth-service"

until "$KCADM" config credentials \
  --server "$KEYCLOAK_URL" \
  --realm master \
  --user "$KEYCLOAK_ADMIN_USERNAME" \
  --password "$KEYCLOAK_ADMIN_PASSWORD" >/dev/null 2>&1
do
  sleep 2
done

CLIENT_UUID=$(
  "$KCADM" get clients \
    -r "$KEYCLOAK_REALM" \
    -q "clientId=$SERVICE_CLIENT_ID" |
  sed -n 's/.*"id" *: *"\([^"]*\)".*/\1/p' |
  head -n 1
)

if [ -z "$CLIENT_UUID" ]; then
  echo "No se encontró el cliente $SERVICE_CLIENT_ID" >&2
  exit 1
fi

"$KCADM" update "clients/$CLIENT_UUID" \
  -r "$KEYCLOAK_REALM" \
  -s "secret=$KEYCLOAK_AUTH_SERVICE_SECRET"

echo "Cliente técnico de Keycloak configurado."