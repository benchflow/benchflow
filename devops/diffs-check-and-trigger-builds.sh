#/bin/bash
# Detects the changes in the build commit, and triggers a build of the corresponding projects

# TODO: modularize in functions and multiple files
# TODO: improve the code to dynamically retrieve the needed IDs available through the APIs
# TODO: improve string to array splitting

# CURRENT LIMITATIONS, OTHER THAN THE ONE MENTIONED BY TODOs:
# - There is currently no handling for API Rate limits
# - There is currently no handling fro GitHub 250 records limit for the commit API
# - Possible impacts of more builds happening concurrently is currently not taken into account. The 
#   current assumption is that all the builds have to finish, and we add a bit of overhead because we
#   check new builds to be triggered against the latest successful build of the current branch. 
#
# - Currently we do not deal with versioning, because we build against each branch and each commit and
#   we use branch-name_commit-hash as build identifier
#
# - Currently we do not deal with changes to generic/shared folders, such as the test folder or in the root of the repo.
#   A stronger implementation has to determine what to build also when shared folder are updated.

# TODO: remove, when the code become stable
set -xv	

echo "Current commit: $WERCKER_GIT_COMMIT"

# Determine the last built commit on the current branch
last_built_commit_sha=$(curl -Ss -H "Content-Type: application/json" -H "Authorization: Bearer $WERCKER_API_AUTH" -X GET "https://app.wercker.com/api/v3/runs/?applicationId=$WERCKER_APPLICATION_ID&branch=$WERCKER_GIT_BRANCH&status=finished&result=passed&sort=creationDateDesc&limit=1" | jq '.[0].commitHash')
# TODO rm, test code
# last_built_commit_sha=$(curl -Ss -H "Content-Type: application/json" -H "Authorization: Bearer $WERCKER_API_AUTH" -X GET "https://app.wercker.com/api/v3/runs/?applicationId=$WERCKER_APPLICATION_ID&branch=$WERCKER_GIT_BRANCH&status=finished&result=passed&sort=creationDateDesc" | jq '.[5].commitHash')

echo "Last built commit in the current branch: ${last_built_commit_sha//\"/}"

# If the current commit is the last with a successful build, we just exit (this situation should not happen)
if [ ""${last_built_commit_sha//\"/}"" == "${WERCKER_GIT_COMMIT}" ]; then
	exit
fi

echo ""

# Collect all the commits happened on the current branch, since the last built commit
# NOTE: We need to actually iterate over the commits, because the GitHub APIs do not
# support branch specific commits diff.

# We need to iterate the commits from the newest to the oldest. The API returns them in the reverse order. 
# Source: http://unix.stackexchange.com/a/9443
reverse ()
{
    local line
    if IFS= read -r line
    then
        reverse
        printf '%s\n' "$line"
    fi
}

# IF the branch is one of the following, we need to look at the history of the branch directly
# because no direct commits can happen, and the history is exclusive to the branch with commits
# coming from other branches
if [[ "$WERCKER_GIT_BRANCH" = "master" || "$WERCKER_GIT_BRANCH" = "devel" || "$WERCKER_GIT_BRANCH" = "release-"* ]]; then
  last_commits_on_branch=$(curl -sS "https://api.github.com/repos/$WERCKER_GIT_OWNER/$WERCKER_GIT_REPOSITORY/commits?sha=$WERCKER_GIT_BRANCH&client_id=$GITHUB_API_CLIENT_ID&client_secret=$GITHUB_API_CLIENT_SECRET" | jq '.[].sha')
# IF we are in a feature branch, we need to look at the diff of the feature branch and devel
else
	# NOTE: we currently assume the main branch is always devel   
	last_commits_on_branch=$(curl -sS "https://api.github.com/repos/$WERCKER_GIT_OWNER/$WERCKER_GIT_REPOSITORY/compare/devel...$WERCKER_GIT_BRANCH?client_id=$GITHUB_API_CLIENT_ID&client_secret=$GITHUB_API_CLIENT_SECRET" | jq '.commits | .[].sha' | reverse)
fi

last_commits_on_branch_arr=(${last_commits_on_branch//$'\n'/ })

last_built_commit_index=null

# If the last_built_commit_sha is not empty or null, there was a previous build
if [[ "${last_built_commit_sha//\"/}" != "null" ]]; then

	index=0

	for commit in "${last_commits_on_branch_arr[@]}"
	do
	  # echo "Commit: $commit"
	  if [ ""${last_built_commit_sha}"" == "${commit}" ]; then
			# echo $index
			last_built_commit_index=$index
			break
	  fi
	  index=$((index+1))
	done

	# IF we find the delta in the commits history
	if [[ "$last_built_commit_index" != "null" ]]; then

		echo "Commits to evaluate for build: $last_built_commit_index"

		echo ""

		new_commits_to_build=("${last_commits_on_branch_arr[@]:0:$last_built_commit_index}")

	# IF we do not find the delta in the commit history.
	# NOTE: Sometimes it might happen that the history of a branch changes (e.g, because of a pull request or
	# more people working on the same branch doing merges instead of rebases, or rebases of a branch on top of another branch).
	# It happened to us that User A was working on a branch, and did not push all commits, meanwhile User B worked on the same branch by first rebasing it
	# on top of another one and then pushing all commits, then User A did a merge instead of a rebase. This is mainly because we try to avoid rebases due to
	# https://www.atlassian.com/git/articles/git-team-workflows-merge-or-rebase, https://www.derekgourlay.com/blog/git-when-to-merge-vs-when-to-rebase/
	else

		# We consider the list of all the commits to the current branch as all commits to build,
		# The assumption is that is better to build more than less.
		echo "The last built commit was not found. Building all commits."

		echo ""
		new_commits_to_build=("${last_commits_on_branch_arr[@]}")

	fi

# This is the first build for this branch
else
	# We need to build for all the commits
	echo "First build for the branch. Building all commits."

	echo ""

	new_commits_to_build=("${last_commits_on_branch_arr[@]}")
fi

declare -a all_changed_folders

for commit in "${new_commits_to_build[@]}"
do

  commit="${commit//\"/}"

  echo "Commit $commit"

  # Stores the commit data on file
	# TODO: store them in memory
	curl -sS "https://api.github.com/repos/$WERCKER_GIT_OWNER/$WERCKER_GIT_REPOSITORY/commits/$commit?client_id=$GITHUB_API_CLIENT_ID&client_secret=$GITHUB_API_CLIENT_SECRET" 2>/dev/null 1>"commit_api_$commit.txt"

	# Determines if it is a merge commit
	# TODO: make the detection and the subsequent decision stronger, after applying to real development

	# A merge commit can be either a Pull Request merge, or a merge of another branch in the current one
	merge_grep_matching="Merge pull request\|Merge branch\|Merge remote-tracking"
	lines_containing_merge=$(jq '.commit.message' < "commit_api_$commit.txt" | grep "$merge_grep_matching" | wc -l | xargs)
	# committer=$(jq '.committer | .login'  < "commit_api_$commit.txt")

	# If we match "merge_grep_matching" then is a merge commit
	is_merge_commit=false

	if [ "$lines_containing_merge" -gt "0" ]; then
	   is_merge_commit=true
	fi

	echo "Is a merge commit? $is_merge_commit"

	# IF we are in devel, master or release-* branch:
		# WE build the merge commit, because it merges code from other branches and the releases have to be updated.
	# IF we are in other branches, we evaluate the build history of the branch  to evaluate what to build 
	# again due to the merge commits. We do not currently selectively re-build based on the folders that have been
	# actually changed in the merge commits, but just re-build once all the previous builds. This is more safe for now
	# in case some code changes in "shared folders", and also so that we get the status updated on GitHub.

	if [[ "$is_merge_commit" = false ]] ||
		 [[ "$is_merge_commit" = true && ("$WERCKER_GIT_BRANCH" = "master" || "$WERCKER_GIT_BRANCH" = "devel" || "$WERCKER_GIT_BRANCH" = "release-"*) ]]; then
     
      changed_folders=$(cat "commit_api_$commit.txt" | jq '.files[] | select(.filename | split ("/") | length > 1 ) | .filename | split ("/") | .[0]' | uniq)

			changed_folders_arr=(${changed_folders// / })

			all_changed_folders=("${all_changed_folders[@]}" "${changed_folders_arr[@]}")

			echo "Changed Folders:"

			for folder in "${changed_folders_arr[@]}"
			do
			  echo "$folder"
			done

			# Determine the folder in which content has been changed since the last build of this branch
			# TODO: fine tune, by removing the folders changed in merge commits, because they might be dependencies (need to be evaluate with actual usage)

			echo "Trigger builds for ALL Changed Folders of interest"

			# Determine the source pipeline id
			# TODO: monitor Wercker and check if they add this ID as part of the available Envs
			#       since it is the current run ID.
			# NOTE: currently we assume the url structure does not change
			WERCKER_RUN_ID=$(echo "$WERCKER_RUN_URL" | sed -e 's/.*what-to-build\/\(.*\).*/\1/')


			all_changed_folders=($(for v in "${all_changed_folders[@]}"; do echo "$v";done| sort -u))

			# TODO: improve the way we handle the triggers, as well as the retrieval of the pipeline id to be more dynamic
			for folder in "${all_changed_folders[@]}"
			do
				folder="${folder//\"/}"
				branch_name_pipeline_id=""
			  echo "Triggering build for: $folder"
			  case $folder in
				     "benchflow-dsl")
				     branch_name_pipeline_id=$WERCKER_BENCHFLOW_DSL_PIPELINE_ID
				     ;;
				     *)
				     echo "No build pipeline defined for the current folder"
				     ;;
				esac

				if [ -n "${branch_name_pipeline_id}" ]; then
					echo "Build API call response:"
					curl -Ss -H "Content-Type: application/json" -H "Authorization: Bearer $WERCKER_API_AUTH" -X POST -d '{"pipelineId": "'"$branch_name_pipeline_id"'", "branch": "'"$WERCKER_GIT_BRANCH"'", "commitHash": "'"$WERCKER_GIT_COMMIT"'"}' https://app.wercker.com/api/v3/runs/ | jq .
				fi
				
				echo ""
			done
			
	else
	    echo "Determine what to build, using the build history of the branch, and build, for: $commit"
	    source ./rerun-builds-on-the-branch.sh
	fi

	echo ""

	# Deletes the temporary file
	rm "commit_api_$commit.txt"
done