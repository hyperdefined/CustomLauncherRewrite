![owo](https://legoshi.moe/img/background.png)

<h1 align="center">CustomLauncherRewrite</h1>

CustomLauncherRewrite is a brand new custom launcher for Toontown Rewritten. This launcher is based on my old one, [Multitoon Helper](https://github.com/hyperdefined/multitoon-helper). This one is built from the ground up and has many more features than the previous one. This launcher is designed for players who are sick of the current official launcher.

## Features
* Ability to save logins for multiple accounts. Sick of typing it in *every* single time?
* Encrypt login data with a passphrase. You data is encrypted using [AES Encryption](https://searchsecurity.techtarget.com/definition/Advanced-Encryption-Standard).
* Launch the game directly from the launcher, no external scripts needed. Supports 2FA and ToonGuard.
* Check for TTR updates.

## Setup
* You need Java 8 or above installed.

After you have these things, download the latest [release](https://github.com/hyperdefined/CustomLauncherRewrite/releases). Throw the contents of the zip into a seperate folder and run CustomLauncherRewrite to open. To add a new account, simply click "Add Account" and follow the steps. Double click the account to launch the game.

## Troubleshooting
### It says that it can't find my TTR installation.
If you have installed TTR in a different location, copy that installation path and change the path in the `config.json` file.

### It says my passphrase is wrong.
You passphrase was typed in wrong. You either typed it incorrectly when you added an account, or you keep typing it incorrectly when you login. Remove the account and re-add it back.

### I have an issue that is not listed here.
No problem! Create a new [issue](https://github.com/hyperdefined/CustomLauncherRewrite/issues) and include as much detail as possible so I can help you out.

## License
This program is released under GNU General Public License v3. See [LICENSE](https://github.com/hyperdefined/CustomLauncherRewrite/blob/master/LICENSE).

| Licenses |
| ----------- |
| [org.json](https://github.com/stleary/JSON-java) |
| [commons-io](https://github.com/apache/commons-io/blob/master/LICENSE.txt) |
| [commons-compress](https://github.com/apache/commons-compress/blob/master/LICENSE.txt) |
| [httpcomponents-client](https://github.com/apache/httpcomponents-client/blob/master/LICENSE.txt) |