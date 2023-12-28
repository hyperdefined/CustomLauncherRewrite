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

read -r -p "Before we uninstall, would you like to keep your TTR files? These include screenshots and resource packs. Enter 'Y' to keep them, 'N' to wipe them.: " input

if [ "$input" != "Y" ]; then
  keep_ttr_files=false
else
  keep_ttr_files=true
fi

if $keep_ttr_files; then
  echo "Removing /opt/CustomLauncherRewrite but keeping TTR files..."
  # remove everything but ttr-files folder
  sudo find /opt/CustomLauncherRewrite/* -type d -not -name 'ttr-files' -exec rm -rf {} +
  sudo rm /opt/CustomLauncherRewrite/CustomLauncherRewrite*
  sudo rm /opt/CustomLauncherRewrite/run.sh
else 
  echo "Removing /opt/CustomLauncherRewrite..."
  sudo rm -r /opt/CustomLauncherRewrite
fi

echo "Removing the desktop entry..."
sudo rm ~/Desktop/customlauncherrewrite.desktop
sudo rm /usr/share/applications/customlauncherrewrite.desktop

echo "CustomLauncherRewrite has been uninstalled!"
