#!/bin/bash

# taken from https://gist.github.com/steinwaywhw/a4cd19cda655b8249d908261a62687f8
DOWNLOADURL=$(curl -s https://api.github.com/repos/hyperdefined/CustomLauncherRewrite/releases/latest | grep "browser_download_url"  | grep "tar.gz" | cut -d '"' -f 4)
OUTPUTFILE=${DOWNLOADURL##*/}

echo Dowloading latest version from "$DOWNLOADURL"

wget -q -P /tmp/ "$DOWNLOADURL"

echo "We need to run -> 'sudo mkdir -p /opt/CustomLauncherRewrite'".
sudo mkdir -p /opt/CustomLauncherRewrite

echo Extracting "$OUTPUTFILE"...
sudo tar -xf /tmp/"$OUTPUTFILE" -C /opt/CustomLauncherRewrite

echo Dowloading desktop entry...
sudo wget -q -P /usr/share/applications https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/linux/customlauncherrewrite.desktop

echo Setting correct perms to install location...
sudo chown -R "$USER":"$USER" /opt/CustomLauncherRewrite
sudo chmod -R 755 /opt/CustomLauncherRewrite
sudo rm -rf /tmp/CustomLauncherRewrite*

echo CustomLauncherRewrite has been installed!