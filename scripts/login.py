#  This file is part of CustomLauncherRewrite.
#
#  CustomLauncherRewrite is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  CustomLauncherRewrite is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with CustomLauncherRewrite.  If not, see <https://www.gnu.org/licenses/>.

import urllib
import urllib.parse
import urllib.request
import sys
import os
import json
import time
import platform


class TTRQuickLauncher:
    login_url = 'https://www.toontownrewritten.com/api/login?format=json'
    username = sys.argv[1]
    password = sys.argv[2]

    def __init__(self):
        login = urllib.parse.urlencode({'username': self.username, 'password': self.password})
        self.postrequest(login)

    def postrequest(self, request):
        user_agent = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) ' \
                     'Chrome/35.0.1916.47 Safari/537.36 '
        req = urllib.request.Request(self.login_url, request)
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        req.add_header('User-Agent', user_agent)
        post = urllib.request.urlopen(req, request.encode('utf-8'))
        resp = json.loads(post.read())

        success = resp.get('success', 'false')

        if success == 'true':
            os.environ['TTR_PLAYCOOKIE'] = resp.get('cookie', 'CookieNotFound')
            os.environ['TTR_GAMESERVER'] = resp.get('gameserver', 'ServerNotFound')
            system = platform.system()
            if system == 'Windows':
                os.chdir("F:\\Program Files (x86)\\Toontown Rewritten")
                os.system("TTREngine.exe")
            elif system == 'Linux':
                os.system('./TTREngine')
            else:
                print('Platform %s isn\' supported yet.' % system)

        elif success == 'delayed':
            token = resp.get('queueToken', None)

            if token is None:
                print("No queue token was given. This is not supposed to be possible!")
            else:
                print("Login successful. Game is launching.")
                time.sleep(1)
                queue = urllib.parse.urlencode({'queueToken': token})
                self.postrequest(queue)

        elif success == 'partial':
            banner = resp.get('banner', 'Please enter an authenticator token')
            token = resp.get('responseToken', None)

            if token is None:
                print("A auth token for response couldn't be found.")
            else:
                app = raw_input(banner + '\n')

                authrequest = urllib.parse.urlencode({'appToken': app, 'authToken': token})
                self.postRequest(authrequest)

        elif success == 'false':
            banner = resp.get('banner', "Login have failed, but the reason why was not given. Try again later.")
            print(banner)


TTRQuickLauncher = TTRQuickLauncher()
