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

echo "Removing /opt/CustomLauncherRewrite..."

sudo rm -r /opt/CustomLauncherRewrite

echo "Removing the desktop entry..."
sudo rm ~/Desktop/customlauncherrewrite.desktop
sudo rm /usr/share/applications/customlauncherrewrite.desktop

echo "CustomLauncherRewrite has been uninstalled!"
