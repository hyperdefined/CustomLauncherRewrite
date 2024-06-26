#!/bin/bash
#
# This file is part of CustomLauncherRewrite.
#
# CustomLauncherRewrite is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# CustomLauncherRewrite is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with CustomLauncherRewrite.  If not, see <https://www.gnu.org/licenses/>.
#

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
# Create the icons directory just in case it doesn't exist
# Save the icon for the launcher
mkdir -p ~/.local/share/icons/
wget -q -O ~/.local/share/icons/customlauncherrewrite-icon.png https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/src/main/resources/icon.png
sudo wget -q -P /usr/share/applications https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/linux/customlauncherrewrite.desktop

# Find the user's desktop
if command -v xdg-user-dir >/dev/null 2>&1; then
    DESKTOP_DIR=$(xdg-user-dir DESKTOP)
else
    # If xdg-user-dir is not installed, just try to guess it
    DESKTOP_DIR=~/Desktop
fi

# Symlink on desktop (e.g. desktop shortcut)
ln -s /usr/share/applications/customlauncherrewrite.desktop "$DESKTOP_DIR/customlauncherrewrite.desktop"
sudo chown "$USER":"$USER" "$DESKTOP_DIR/customlauncherrewrite.desktop"
chmod +x "$DESKTOP_DIR/customlauncherrewrite.desktop"

# Make sure the user owns the folder to run
echo "Setting correct perms to install location..."
sudo chown -R "$USER":"$USER" $INSTALLDIR
sudo chmod -R 755 $INSTALLDIR

# Delete any temp files
rm -rf /tmp/CustomLauncherRewrite*

echo "CustomLauncherRewrite has been installed!"