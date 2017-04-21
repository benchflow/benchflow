#/bin/bash
# Deliver branch and commit specific artifacts on Bintray

# TODO: remove, when the code become stable
set -xv	

# Creates the version (nothing happens if already exists)
echo "Creating version for commit..."
curl -v -u$BINTRAY_ADMIN_USERNAME:$BINTRAY_API_KEY -H "Content-Type: application/json" -d '{"name": "'"$WERCKER_GIT_BRANCH_$CUSTOM_VERSION_TAG"'", "desc": "'"$WERCKER_GIT_BRANCH_$CUSTOM_VERSION_TAG"'"}' "https://api.bintray.com/packages/$BINTRAY_USERNAME/$BINTRAY_USERNAME-maven/$ARTIFACT_NAME/versions" | jq .
echo ""
echo "Creating version for branch..."
curl -v -u$BINTRAY_ADMIN_USERNAME:$BINTRAY_API_KEY -H "Content-Type: application/json" -d '{"name": "'"$WERCKER_GIT_BRANCH"'", "desc": "'"$WERCKER_GIT_BRANCH"'"}' "https://api.bintray.com/packages/$BINTRAY_USERNAME/$BINTRAY_USERNAME-maven/$ARTIFACT_NAME/versions" | jq .
echo ""
echo "Uploading artefact for commit..."
curl -T $WERCKER_OUTPUT_DIR/$ARTIFACT_NAME.jar -u$BINTRAY_ADMIN_USERNAME:$BINTRAY_API_KEY "https://api.bintray.com/content/$BINTRAY_USERNAME/$BINTRAY_USERNAME-maven/$ARTIFACT_NAME/$WERCKER_GIT_BRANCH_$CUSTOM_VERSION_TAG/$ARTIFACT_NAME_$WERCKER_GIT_BRANCH_$CUSTOM_VERSION_TAG.jar?publish=1&override=1" | jq .
echo ""
echo "Uploading artefact for branch..."
curl -T $WERCKER_OUTPUT_DIR/$ARTIFACT_NAME.jar -u$BINTRAY_ADMIN_USERNAME:$BINTRAY_API_KEY "https://api.bintray.com/content/$BINTRAY_USERNAME/$BINTRAY_USERNAME-maven/$ARTIFACT_NAME/$WERCKER_GIT_BRANCH/$ARTIFACT_NAME_$WERCKER_GIT_BRANCH.jar?publish=1&override=1" | jq .