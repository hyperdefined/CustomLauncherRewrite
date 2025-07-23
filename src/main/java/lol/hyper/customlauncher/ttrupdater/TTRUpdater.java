/*
 * This file is part of CustomLauncherRewrite.
 *
 * CustomLauncherRewrite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CustomLauncherRewrite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CustomLauncherRewrite.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.customlauncher.ttrupdater;

import lol.hyper.customlauncher.ConfigHandler;
import lol.hyper.customlauncher.CustomLauncherRewrite;
import lol.hyper.customlauncher.tools.ExceptionWindow;
import lol.hyper.customlauncher.tools.JSONUtils;
import lol.hyper.customlauncher.tools.OSDetection;
import lol.hyper.customlauncher.tools.PopUpWindow;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TTRUpdater extends JFrame {

    /**
     * The TTRUpdater logger.
     */
    private final Logger logger = LogManager.getLogger(this);
    /**
     * The main progress bar on the window.
     */
    private final JProgressBar progressBar;
    /**
     * The current file status text.
     */
    private final JLabel updateStatus;
    /**
     * The total file status.
     */
    private final JLabel totalUpdateStatus;
    /**
     * TTR install path.
     */
    private File installPath;
    /**
     * Used to determine if the game was updated successfully.
     */
    private boolean status = false;
    /**
     * HttpClient for requests.
     */
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Creates the TTR updater window.
     */
    public TTRUpdater() {
        // set up the window elements
        setTitle("TTR Updater");
        setSize(370, 150);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            logger.error(exception);
        }

        setIconImage(CustomLauncherRewrite.getIcon());

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        updateStatus = new JLabel("Checking files...");
        updateStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        totalUpdateStatus = new JLabel();
        totalUpdateStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar = new JProgressBar(0, 0);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(updateStatus);
        panel.add(progressBar);
        panel.add(totalUpdateStatus);

        progressBar.setBounds(150, 100, 100, 30);
        updateStatus.setBounds(70, 25, 370, 40);

        add(panel);
        setLocationRelativeTo(null);
    }

    /**
     * Check for updates!
     */
    public void checkUpdates(String manifest) {
        ConfigHandler configHandler = new ConfigHandler();
        installPath = configHandler.getInstallPath();
        // don't run the updater if the folder doesn't exist
        if (!installPath.exists()) {
            JOptionPane.showMessageDialog(this, "Unable to check for TTR updates. We are unable to find your TTR install directory.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            logger.warn("Can't find current install directory. Skipping updates.");
        }

        logger.info("Starting TTRUpdater");
        // read the patches
        String patchesManifestRootUrl = "https://cdn.toontownrewritten.com";
        String patchManifest = patchesManifestRootUrl + manifest;
        JSONObject patches = JSONUtils.requestJSON(patchManifest);
        if (patches == null) {
            logger.error("patchesmanifest.txt returned null!");
            dispose();
            return;
        }
        progressBar.setMaximum(patches.length());

        ArrayList<String> filesToDownload = new ArrayList<>();

        // this loops through the JSON
        // key is the file name
        for (String key : patches.keySet()) {
            progressBar.setValue(progressBar.getValue() + 1);
            JSONObject currentFile = patches.getJSONObject(key);
            String onlineHash = currentFile.getString("hash");
            // get the list of OS's the file is for
            List<String> only = currentFile.getJSONArray("only").toList().stream().map(object -> Objects.toString(object, null)).toList();
            // if we are running the OS the file is for, check it
            if (only.contains(OSDetection.getOsType())) {
                File localFile = new File(installPath, key);
                updateStatus.setText("Checking file " + localFile.getName());
                if (!localFile.exists()) {
                    logger.info("-----------------------------------------------------------------------");
                    logger.info("{}{}{}", installPath.getAbsolutePath(), File.separator, key);
                    logger.info("This file is missing and will be downloaded.");
                    filesToDownload.add(key);
                    continue;
                }

                // the file exists locally, check the SHA1 and compare it to TTR's
                String localHash;
                try {
                    localHash = calcSHA1(localFile);
                } catch (Exception exception) {
                    logger.error("Unable to calculate SHA1 hash for file {}", localFile.getAbsolutePath(), exception);
                    new ExceptionWindow(exception);
                    dispose();
                    return;
                }
                logger.info("-----------------------------------------------------------------------");
                logger.info("{}{}{}", installPath.getAbsolutePath(), File.separator, key);
                logger.info("Local hash: {}", localHash.toLowerCase(Locale.ENGLISH));
                logger.info("Expected hash: {}", onlineHash);
                logger.info("Type: {}", OSDetection.getOsType());
                if (!localHash.equalsIgnoreCase(onlineHash)) {
                    filesToDownload.add(key);
                }
            }
        }

        logger.info("-----------------------------------------------------------------------");

        // we store files we need to download in filesToDownload
        // if there are files in that list, download them
        int currentProgress = 0;
        if (!filesToDownload.isEmpty()) {
            totalUpdateStatus.setText(String.format("Progress: %d / %d", currentProgress, filesToDownload.size()));
            File tempFolder = new File("temp");
            if (!tempFolder.exists() && !tempFolder.mkdirs()) {
                logger.error("Unable to create temp folder!");
                new PopUpWindow(this, "Unable to create temp folder!");
                dispose();
                return;
            }

            logger.info("{} file(s) are going to be downloaded.", filesToDownload.size());
            logger.info(filesToDownload);

            progressBar.setValue(0); // reset

            // download each file
            for (String fileToDownload : filesToDownload) {
                // set the progress
                progressBar.setMaximum(fileToDownload.length());
                // get the file name from TTR to download
                JSONObject file = patches.getJSONObject(fileToDownload);
                String downloadName = file.getString("dl");

                String patchesRootUrl = "https://download.toontownrewritten.com/patches/";
                logger.info("Downloading {}{}", patchesRootUrl, downloadName);
                updateStatus.setText("Downloading " + downloadName);
                progressBar.setVisible(true);
                progressBar.setValue(progressBar.getValue() + 1);

                // build the download URL
                URL downloadURL;
                try {
                    downloadURL = new URI(patchesRootUrl + downloadName).toURL();
                } catch (Exception exception) {
                    logger.error("Invalid URL " + patchesRootUrl + "{}", downloadName);
                    new ExceptionWindow(exception);
                    dispose();
                    return;
                }

                // set the output to be in the temp folder
                File downloadOutput = new File(tempFolder + File.separator + downloadName);
                long downloadStart = System.nanoTime();
                // download the file
                if (!saveFile(downloadURL, downloadOutput)) {
                    logger.error("Unable to download file {}", downloadName);
                    new PopUpWindow(this, "Unable to download file " + downloadName + ".");
                    dispose();
                    return;
                }
                long downloadTime = TimeUnit.MILLISECONDS.convert(System.nanoTime() - downloadStart, TimeUnit.NANOSECONDS);
                logger.info("Finished downloading {}. Took {}ms.", downloadOutput.getAbsolutePath(), downloadTime);
                updateStatus.setText("Finished downloading " + downloadName);

                long startTime = System.nanoTime();
                logger.info("Extracting {} to {}{}{}", downloadOutput.getAbsolutePath(), installPath, File.separator, fileToDownload);
                updateStatus.setText("Extracting " + downloadOutput + " to " + fileToDownload);
                try {
                    // extract the file to the new location
                    decompressBz2(downloadName, fileToDownload);
                } catch (IOException exception) {
                    logger.error("Unable to extract file {}", downloadName, exception);
                    new ExceptionWindow(exception);
                    dispose();
                    return;
                }
                updateStatus.setText("Finished extracting file " + fileToDownload);
                long extractedTime = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
                logger.info("Finished extracting file {}. Took {}ms.", downloadName, extractedTime);
                currentProgress++;
                totalUpdateStatus.setText(String.format("Progress: %d / %d", currentProgress, filesToDownload.size()));
            }
        } else {
            logger.info("No files need downloaded, we are up to date.");
        }
        logger.info("Finished checking for TTR updates!");
        status = true;
        dispose();
    }

    /**
     * Returns the status of the updater.
     *
     * @return True if it was successful, false if it failed.
     */
    public boolean status() {
        return status;
    }

    /**
     * Calculates the SHA1 of a file.
     *
     * @param file The file to calculate.
     * @return String representing the SHA1.
     */
    private static String calcSHA1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }
            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }

    /**
     * Extract the compressed bzip2 files to their output file.
     *
     * @param temp       The temp file's name that was downloaded.
     * @param outputName The file's output name.
     */
    private void decompressBz2(String temp, String outputName) throws IOException {
        File tempFile = new File("temp" + File.separator + temp);
        File output = new File(installPath, outputName);

        long totalBytes = tempFile.length();
        long bytesRead = 0;
        byte[] buffer = new byte[1024];
        int len;

        try (BZip2CompressorInputStream in = new BZip2CompressorInputStream(new BufferedInputStream(new FileInputStream(tempFile))); FileOutputStream out = new FileOutputStream(output)) {
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                bytesRead += len;
                int progress = (int) ((bytesRead * 100) / totalBytes);
                progressBar.setValue(progress);
            }
        }
    }

    /**
     * Downloads TTR file and saves it to the temp folder.
     *
     * @param downloadURL    The URL to download.
     * @param downloadOutput The file to save to.
     * @return True if successful, false if not.
     */
    private boolean saveFile(URL downloadURL, File downloadOutput) {
        boolean isSucceed = true;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(downloadURL.toString()))
                .header("User-Agent", CustomLauncherRewrite.getUserAgent())
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            long contentLength = response.headers()
                    .firstValueAsLong("Content-Length")
                    .orElse(-1);

            try (InputStream input = response.body();
                 FileOutputStream output = new FileOutputStream(downloadOutput)) {

                byte[] buffer = new byte[4096];
                long count = 0;
                int n;

                progressBar.setMaximum(100);

                while ((n = input.read(buffer)) != -1) {
                    output.write(buffer, 0, n);
                    count += n;
                    if (contentLength > 0) {
                        int percent = (int) (count * 100 / contentLength);
                        progressBar.setValue(percent);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Unable to save file!", e);
            isSucceed = false;
        }

        return isSucceed;
    }
}
