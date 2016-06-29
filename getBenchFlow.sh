#!/bin/sh
set -e
#
# This script is meant for quick & easy install via:
#   'wget -qO- https://github.com/benchflow/client/releases/download/<version>/benchflow | sh'
#
# How to use this script to install BenchFlow:
#   1. Log into your Ubuntu or OSX terminal as a user with `sudo` privileges.
#	2. Verify that you have `wget` installed.

# 		$ which wget

# 	3. If `wget` isn’t installed, install it after updating your package manager (for OSX install brew if not already installed: http://brew.sh):
		
#		Ubuntu:
# 		$ sudo apt-get update

# 		$ sudo apt-get install wget

#		OSX:
# 		$ sudo brew update

# 		$ sudo brew install wget

#
# 	4. Get the latest BenchFlow package.

#		$ wget -qO- https://github.com/benchflow/benchflow/releases/download/<version>/getBenchFlow.sh | sh

# 	   The system prompts you for your sudo password. Then, it downloads and installs BenchFlow and its dependencies.

# 	5. Verify `benchflow` is installed correctly.

#		$ benchflow
# 		Usage: benchflow.py [OPTIONS] COMMAND [ARGS]...
# 	    Options:
#   		--help  Show this message and exit.
#
#
# Inspired from: https://get.docker.com and https://docs.docker.com/linux/step_one/

#-----BenchFlow details-----#
VERSION="v-dev"
#-----BenchFlow details-----#

command_exists() {
	command -v "$@" > /dev/null 2>&1
}

do_install() {

	user="$(id -un 2>/dev/null || true)"

	sh_c='sh -c'
	if [ "$user" != 'root' ]; then
		if command_exists sudo; then
			sh_c='sudo -E sh -c'
		elif command_exists su; then
			sh_c='su -c'
		else
			cat >&2 <<-'EOF'
			Error: this installer needs the ability to run commands as root.
			We are unable to find either "sudo" or "su" available to make this happen.
			EOF
			exit 1
		fi
	fi

	if command_exists benchflow; then

		local skip_warning=false

		#if we want to force the update, we don't warn the user
		if [[ ! -z "$1" ]]
		then
		  if [[ "-f" == "$1" ]]
		  then
		     skip_warning=true
		  fi
		fi

		if [ "$skip_warning" = false ] ; then
		    cat >&2 <<-'EOF'
				Warning: the "benchflow" command appears to already exist on this system.

				You may press Ctrl+C now to abort this script.
			EOF
			( set -x; sleep 20 )
		fi

		# Removing the old version of the BenchFlow command
		cat >&2 <<-'EOF'
			Removing old 'benchflow' command
		EOF

		$sh_c 'rm -f /usr/local/bin/benchflow'
	fi

	#TODO: handle errors
	$sh_c 'wget -qO- https://github.com/benchflow/client/releases/download/'$VERSION'/benchflow > /usr/local/bin/benchflow' 1>&- 2>&-
	$sh_c 'chmod +x /usr/local/bin/benchflow'
	$sh_c 'chown '$user' /usr/local/bin/benchflow'
 
	cat >&2 <<-'EOF'
		Latest version of the "benchflow" command installed
	EOF

	#-----Download the correct tools used by benchflow if we are on OSX-----#
	# DESCRIPTION OF PROBLEM: Implementations of sed, readlink, zcat, etc. are different on OS X and Linux.
	# SOURCE: https://gist.github.com/bittner/5436f3dc011d43ab7551

	# cross-OS compatibility (greadlink, gsed, zcat are GNU implementations for OS X)
	[[ `uname` == 'Darwin' ]] && {
	  which greadlink gsed gzcat > /dev/null || {
		set FORCE_UNSAFE_CONFIGURE=1
		sudo brew install coreutils gnu-sed
	  }
	}
	#-----Download the correct tools used by benchflow if we are on OSX-----#

}

# wrapped up in a function so that we have some protection against only getting
# half the file during "wget | sh"
do_install $@