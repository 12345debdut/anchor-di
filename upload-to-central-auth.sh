#!/usr/bin/env bash
set -e

# Upload staging deployment to Central Publisher Portal using token auth only.
# Usage: ./upload-to-central-auth.sh
# Requires: MAVEN_CENTRAL_USERNAME, MAVEN_CENTRAL_PASSWORD, and NAMESPACE (e.g. io.github.yourusername)
# Or pass them: MAVEN_CENTRAL_USERNAME=u MAVEN_CENTRAL_PASSWORD=p NAMESPACE=io.github.you ./upload-to-central-auth.sh

USERNAME="${MAVEN_CENTRAL_USERNAME:?Set MAVEN_CENTRAL_USERNAME (Central Portal token username)}"
PASSWORD="${MAVEN_CENTRAL_PASSWORD:?Set MAVEN_CENTRAL_PASSWORD (Central Portal token password)}"
NAMESPACE="${NAMESPACE:?Set NAMESPACE (e.g. io.github.yourusername)}"

AUTH=$(echo -n "${USERNAME}:${PASSWORD}" | base64)
API="https://ossrh-staging-api.central.sonatype.com"

curl -sSf -X POST \
  -H "Authorization: Bearer $AUTH" \
  -H "Content-Type: application/json" \
  "$API/manual/upload/defaultRepository/$NAMESPACE?publishing_type=user_managed"

echo ""
echo "Upload requested. Check https://central.sonatype.com/publishing for the deployment."
