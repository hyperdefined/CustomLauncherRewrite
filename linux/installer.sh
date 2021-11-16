#!/bin/bash
echo Dowloading latest version...
# taken from https://gist.github.com/steinwaywhw/a4cd19cda655b8249d908261a62687f8
curl -s hhttps://api.github.com/repos/hyperdefined/CustomLauncherRewrite/releases/latest \
| grep "browser_download_url.*tar.gz" \
| cut -d : -f 2,3 \
| tr -d \" \
| wget -qi -