#!/bin/bash

echo "Removing /opt/CustomLauncherRewrite..."

sudo rm -r /opt/CustomLauncherRewrite

echo "Removing the desktop entry..."
sudo rm /usr/share/applications/customlauncherrewrite.desktop

echo "CustomLauncherRewrite has been uninstalled!"