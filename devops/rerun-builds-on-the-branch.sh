#/bin/bash
# Rerun all the builds that happened in the past on a given branch
# This is useful when we merge code from other branches in a not(devel | master | release-*)
# branch, so that we can check that the builds that where working still work.
# There are some assumptions in this code, described in comment. We are going to document all of them.

# CURRENT LIMITATIONS, OTHER THAN THE ONE MENTIONED BY TODOs:
# We do not currently selectively re-build based on the folders that have been
# actually changed in the merge commits, but just re-build once all the previous builds. This is more safe for now
# in case some code changes in "shared folders", and also so that we get the status updated on GitHub.

set -xv	

max_limit=20
skip=0

# Initialize to max_limit so we enter the while loop
number_of_runs=20

# Contains all the build that we run again
declare -a all_run_url

# While we get the maximum number possible of results
while [ $number_of_runs -ge $max_limit ]; do
	# Save the builds happened on the branch
	curl -Ss -H "Authorization: Bearer $WERCKER_API_AUTH" "https://app.wercker.com/api/v3/runs/?applicationId=$WERCKER_APPLICATION_ID&branch=$WERCKER_GIT_BRANCH&limit=$max_limit&skip=$skip" 2>/dev/null 1>"runs_api_$WERCKER_GIT_BRANCH.txt"	

	# Count the retrieved number of runs
	number_of_runs=$(cat runs_api_$WERCKER_GIT_BRANCH.txt | jq '.[].id' | wc -l | xargs)

	# Determine the workflow/pipelines that had been run, and that we are interested in run again
	# Here the assumption is that project specific workflows are always initialized by pipelines
	# which name starts with: build-, as it is at the time of writing the script
	#
	# Unfortunately using the Workflow API is not suitable because it does not support branch
	# filters in its v3 version - http://devcenter.wercker.com/docs/api/endpoints/workflows
	build_pipelines_ran_urls=$(cat runs_api_$WERCKER_GIT_BRANCH.txt| jq '.[] | select(.pipeline.pipelineName | startswith("build-")) | .pipeline.url' | sort -u)

	build_pipelines_ran_urls_arr=(${build_pipelines_ran_urls//$'\n'/ })

	unset url_to_run
	declare -a url_to_run

	# If it is the first set of run urls, we just store them
	if [ ${#all_run_url[@]} -eq 0 ]; then

		for ran_urls in "${build_pipelines_ran_urls_arr[@]}"; do
			all_run_url+=("${ran_urls}")
			# printf '%s\n' "${all_run_url[@]}"
			# echo ">>>> RUN: $ran_urls"
		done

		# It is all new, so that we need to run
		url_to_run=${all_run_url[@]}

	# We determine the new urls, and we store them in the set of all_run_url
	else

	    # Determine the url that has not already been run
			for i in "${build_pipelines_ran_urls_arr[@]}"; do
				present=
				for j in "${all_run_url[@]}"; do
					[[ $i == $j ]] && { present=1; break; }
				done
				[[ -n $present ]] || all_run_url+=("${i}")
				[[ -n $present ]] || url_to_run+=("${i}")
				# echo ">>>VALUES"
				# printf '%s\n' "${all_run_url[@]}"
				# printf '%s\n' "${url_to_run[@]}"
				# echo "-----------"
			done

			declare -p all_run_url

	fi

	url_to_run_arr=(${url_to_run//$'\n'/ })

	# Run the urls that have not been already run
	for run_url in "${url_to_run_arr[@]}"
	do
		run_url="${run_url//\"/}"
		branch_name_pipeline_id=$(basename $run_url)
	  echo "Triggering build for: $run_url"
	  echo "Build API call response:"
		
		curl -Ss -H "Content-Type: application/json" -H "Authorization: Bearer $WERCKER_API_AUTH" -X POST -d '{"pipelineId": "'"$branch_name_pipeline_id"'", "branch": "'"$WERCKER_GIT_BRANCH"'", "commitHash": "'"$WERCKER_GIT_COMMIT"'"}' https://app.wercker.com/api/v3/runs/ | jq .
		
		echo ""
	done

	# Move to the next bunch of runs, if there are
	skip=$((skip+max_limit))
	
	# Clean up: delete the temporary file we created
	rm "runs_api_$WERCKER_GIT_BRANCH.txt"

done