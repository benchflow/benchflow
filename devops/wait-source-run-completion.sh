#/bin/bash
# Waits for source run to complete

#TODO: reuse functions already defined

# Keep waiting until the dependent build is successfully completed, or kills the build if the dependent build fails
WERCKER_RUN_ID=$(echo "$WERCKER_RUN_URL" | sed -e 's/.*build-and-deploy-benchflow-dsl\/\(.*\).*/\1/')
echo "Checking source run result..."
sourcerun_result=""
while true; do
  sleep 2
  sourcerun_result=$(curl -Ss -H "Content-Type: application/json" -H "Authorization: Bearer $WERCKER_API_AUTH" -X GET "https://app.wercker.com/api/v3/runs/$WERCKER_RUN_ID" | jq '.sourceRun.result')
  echo "Source run result: $sourcerun_result"
  
  if [ "$sourcerun_result" == "passed" ]; then 
  	break;
  # The source run result is aborted or failed
  # Reference: http://devcenter.wercker.com/docs/api/endpoints/runs#get-all-runs
  elif [ "$sourcerun_result" != "unknown" ]; then 
  	exit 1;
  fi
done
echo ""