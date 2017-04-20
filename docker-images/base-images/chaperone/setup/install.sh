#!bin/sh

# Preamble derived from the prepare.sh script from https://github.com/phusion/baseimage-docker.
# Thank you, phusion, for all the lessons learned.

# open up all sudo commands to all users.  See apps/etc/startup.sh.  If SECURE_ROOT is set, then
# startup will lock down root after the container is initialized and running.

. buildenv.inc

# Root directory (it is the directory where we cwd on the Wercker step calling this script)
root=$(pwd)

echo "ALL ALL=NOPASSWD: ALL" >>/etc/sudoers    

# Install prebuilt binaries
(cd /; tar vxzf $BINARIES_FOLDER/setproctitle-install.tar.gz)

# Build chaperone
$BUILD_CHAPERONE_INSTALL # normally 'pip3 install chaperone' located in buildenv.inc

# get rid of annoying color prompts (some people must like this, but on some color schemes,
# things are unreadable).

rm -rf /etc/profile.d/color_prompt

# Now, just so there is no confusion, create a new, empty /var/log directory so that any logs
# written will obviously be written by the current container software.  Keep the old one so
# it's there for reference so we can see what the distribution did.
cd /var
mv log log-dist
mkdir log
chmod 775 log
chown root log

# Customize some system files
cp $root/dot.bashrc /root/.bashrc

# Allow unfettered root access by users. This is done so that apps/startup.d scripts can
# have unfettered access to root on their first startup to configure userspace files
# if needed (see mysql in chaperone-lamp for an example).  At the end of the first startup
# this is then locked down by apps/etc/init.sh.
passwd -d root

# Create default /apps directory
cp -a $root/apps /
cp $root/version.inc /apps/etc/version.inc

# Create aliases in /usr/local/bin for Chaperone programs.

cp -a /usr/bin/chaperone /usr/bin/telchap /usr/bin/envcp /usr/bin/sdnotify \
     /usr/local/bin

# Set up runapps user

addgroup -g 901 runapps
adduser -G runapps -u 901 -H -h / -D runapps
chown -R runapps: /apps

# Do final cleanups
rm -rf /tmp/* /var/tmp/* /var/cache/apk/*
rm -f `find /apps -name '*~'`

echo done.
