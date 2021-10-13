<h1 align="center">CustomLauncherRewrite</h1>

![GitHub release (latest by date)](https://img.shields.io/github/v/release/hyperdefined/CustomLauncherRewrite) [![Downloads](https://img.shields.io/github/downloads/hyperdefined/CustomLauncherRewrite/total?logo=github)](https://github.com/hyperdefined/CustomLauncherRewrite/releases) [![Donate with Bitcoin](https://en.cryptobadges.io/badge/micro/1F29aNKQzci3ga5LDcHHawYzFPXvELTFoL)](https://en.cryptobadges.io/donate/1F29aNKQzci3ga5LDcHHawYzFPXvELTFoL) [![Donate with Ethereum](https://en.cryptobadges.io/badge/micro/0x0f58B66993a315dbCc102b4276298B5Ff8895F41)](https://en.cryptobadges.io/donate/0x0f58B66993a315dbCc102b4276298B5Ff8895F41) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

CustomLauncherRewrite is a brand-new custom launcher for Toontown Rewritten. This launcher is based on my old one, [Multitoon Helper](https://github.com/hyperdefined/multitoon-helper). This one is built from the ground up and has many more features than the previous one. This launcher is designed for players who are annoyed of the current official launcher.

## Features
* Ability to save logins for multiple accounts. Sick of typing it in *every* single time?
* Add accounts right from the launcher. Don't need to mess around with files. 
* Encrypt login data with a passphrase. Your data is encrypted using [AES Encryption](https://searchsecurity.techtarget.com/definition/Advanced-Encryption-Standard).
* Launch the game directly from the launcher, no external scripts needed. Supports 2FA and ToonGuard.
* Update your TTR game files.
* Built in invasion tracker.

![Screenshot1](https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/images/image.png)
![Screenshot2](https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/images/image2.png)
![Screenshot3](https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/images/image3.png)
![Screenshot4](https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/images/image4.png)

## Setup
* You need Java 8 or above installed.

Simply download the latest [release](https://github.com/hyperdefined/CustomLauncherRewrite/releases). Throw the `exe` file into a new folder and run it. To add a new account, simply click "Add Account" and follow the steps. Double click the account to launch the game.

## Updating
To update to a new release, simply [download](https://github.com/hyperdefined/CustomLauncherRewrite/releases) the new release and put the new `exe` file in the same folder as the old one. Run the new `exe` file instead of the old one. You're all set.

## Troubleshooting
### It says that it can't find my TTR installation.
The program tries to search for common install locations. If it can't find yours, then you need to change the path in the options' menu.

To find your current install location, search for "Toontown Rewritten" on the start menu and click "Open File Location".
![Help1](https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/images/findInstallLocation2.png)

Then click the path at the top and copy it. This is what you want to put into the options menu of the program.

![Help2](https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/images/findInstallLocation.png)
![Help3](https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/images/findInstallLocation3.png)

### It says my passphrase is wrong.
Your passphrase was typed in wrong. You either typed it incorrectly when you added an account, or you keep typing it incorrectly when you login. Remove the account and re-add it back if you can't remember it.

### I have an issue that is not listed here.
No problem! Create a new [issue](https://github.com/hyperdefined/CustomLauncherRewrite/issues) and include as much detail as possible, so I can help you out.

## Known Issues
None currently.

## Contributing
The code for this program could definitely be improved. Feel free to fork and make and changes/improvements to it.

## License
This program is released under GNU General Public License v3. See [LICENSE](https://github.com/hyperdefined/CustomLauncherRewrite/blob/master/LICENSE).

| Licenses |
| ----------- |
| [org.json](https://github.com/stleary/JSON-java/blob/master/LICENSE) |
| [commons-io](https://github.com/apache/commons-io/blob/master/LICENSE.txt) |
| [commons-compress](https://github.com/apache/commons-compress/blob/master/LICENSE.txt) |
| [httpcomponents-client](https://github.com/apache/httpcomponents-client/blob/master/LICENSE.txt) |
| [launch4j](https://github.com/mirror/launch4j/blob/master/LICENSE.txt) |
| [log4j2](https://github.com/apache/logging-log4j2/blob/master/LICENSE.txt) |
