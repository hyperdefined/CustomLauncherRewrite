# CustomLauncherRewrite To-do List
1. Better Windows setup (using the Inno Setup)
    * Figure out a solution that will not break old installs.

# Finished
1. Better account encryption.
   * Version accounts in the file. 0 is plaintext, 1 is legacy, 2 is new.
   * Convert accounts when user logins to new version.
   * If there is no version, assume it's the old one.
2. First launch setup
   * Look for a current TTR install location.
   * Ask if the user wants to copy over resource packs, settings, and screenshots.