#!/bin/bash
# taken from https://gist.github.com/steinwaywhw/a4cd19cda655b8249d908261a62687f8
DOWNLOADURL=$(curl -s https://api.github.com/repos/hyperdefined/CustomLauncherRewrite/releases/latest | grep "browser_download_url"  | grep "tar.gz" | cut -d '"' -f 4)
OUTPUTFILE=${DOWNLOADURL##*/}
INSTALLDIR=/opt/CustomLauncherRewrite #In case support for a custom install directory is added

echo "Downloading latest version from ""$DOWNLOADURL"""

wget -q -P /tmp/ "$DOWNLOADURL"

echo "Creating $INSTALLDIR...".
sudo mkdir -p $INSTALLDIR

echo "Extracting ""$OUTPUTFILE""..."
sudo tar -xf /tmp/"$OUTPUTFILE" -C $INSTALLDIR

echo "Downloading desktop entry..."
wget -q -O ~/.local/share/icons/custom-rewrite-launcher.png https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/src/main/resources/icon.png
# create the icons directory just in case it doesn't exist
mkdir -p ~/.local/share/icons/
sudo wget -q -P /usr/share/applications https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/linux/customlauncherrewrite.desktop

# Symlink on desktop (e.g. desktop shortcut)
ln -s /usr/share/applications/customlauncherrewrite.desktop ~/Desktop/customlauncherrewrite.desktop 
sudo chmod +x ~/Desktop/customlauncherrewrite.desktop
sudo chown "$USER":"$USER" ~/Desktop/customlauncherrewrite.desktop

echo "Setting correct perms to install location..."
sudo chown -R "$USER":"$USER" $INSTALLDIR
sudo chmod -R 755 $INSTALLDIR

rm -rf /tmp/CustomLauncherRewrite*

echo "CustomLauncherRewrite has been installed!"
