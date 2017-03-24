changed_folders=$(curl -sS https://api.github.com/repos/$WERCKER_GIT_OWNER/$WERCKER_GIT_REPOSITORY/commits/$WERCKER_GIT_COMMIT | jq '.files[] | select(.filename | split ("/") | length > 1 ) | .filename | split ("/") | .[0]' | uniq)

changed_folders_arr=(${changed_folders// / })

for folder in "${changed_folders_arr[@]}"
do
  echo "$folder"
done