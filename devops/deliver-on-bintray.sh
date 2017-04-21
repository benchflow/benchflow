#/bin/bash
# Deliver branch and commit specific artifacts on Bintray

# TODO: remove, when the code become stable
set -xv

# TODO: Solve the following issues, so that we avoid code duplication
# Because now the same code needs to be duplicated to generate the same tag for docker images
# Integrated with deliver-on-bintray.sh, because it returns a strange errors, without
    # more details. See: https://app.wercker.com/BenchFlow/benchflow/runs/deploy-benchflow-dsl/58f9b393789b8d01004c0520?step=58f9b3a68380e70001b221ef
    # - script:
    #     name: Generate the Image Tag
    #     cwd: ./devops
    #     code: ./generate-commit-specific-tag.sh

echo "Delivering on Bintray"
 
# Generate the tag used to deploy commit specific version
CUSTOM_VERSION_TAG=${WERCKER_GIT_BRANCH}"_"${WERCKER_GIT_COMMIT:0:6}

# Creates the version (nothing happens if already exists)
# echo "Creating version for commit..."
# curl -v -u$BINTRAY_ADMIN_USERNAME:$BINTRAY_API_KEY -H "Content-Type: application/json" -d '{"name": "'"$WERCKER_GIT_BRANCH_$CUSTOM_VERSION_TAG"'", "desc": "'"$WERCKER_GIT_BRANCH_$CUSTOM_VERSION_TAG"'"}' "https://api.bintray.com/packages/$BINTRAY_USERNAME/$BINTRAY_USERNAME-maven/$ARTIFACT_NAME/versions" | jq .
# echo ""
echo "Creating version for branch..."
curl -v -u$BINTRAY_ADMIN_USERNAME:$BINTRAY_API_KEY -H "Content-Type: application/json" -d '{"name": "'"$WERCKER_GIT_BRANCH"'", "desc": "'"$WERCKER_GIT_BRANCH"'"}' "https://api.bintray.com/packages/$BINTRAY_USERNAME/$BINTRAY_USERNAME-maven/$ARTIFACT_NAME/versions" | jq .
echo ""

# Check if the expected file exists
if [ ! -f $WERCKER_SOURCE_DIR/$ARTIFACT_NAME.jar ]; then
    echo "File not found: $WERCKER_SOURCE_DIR/$ARTIFACT_NAME.jar"
    exit 1
fi

#NOTE: The output of the previous pipeline is in the source of the next pipeline
echo "Uploading artefact for commit..."
curl -v -T $WERCKER_SOURCE_DIR/$ARTIFACT_NAME.jar -u$BINTRAY_ADMIN_USERNAME:$BINTRAY_API_KEY "https://api.bintray.com/content/$BINTRAY_USERNAME/$BINTRAY_USERNAME-maven/$ARTIFACT_NAME/$WERCKER_GIT_BRANCH/$ARTIFACT_NAME_$WERCKER_GIT_BRANCH_$CUSTOM_VERSION_TAG.jar?publish=1&override=1" | jq .
echo ""
echo "Uploading artefact for branch..."
curl -v -T $WERCKER_SOURCE_DIR/$ARTIFACT_NAME.jar -u$BINTRAY_ADMIN_USERNAME:$BINTRAY_API_KEY "https://api.bintray.com/content/$BINTRAY_USERNAME/$BINTRAY_USERNAME-maven/$ARTIFACT_NAME/$WERCKER_GIT_BRANCH/$ARTIFACT_NAME_$WERCKER_GIT_BRANCH.jar?publish=1&override=1" | jq .