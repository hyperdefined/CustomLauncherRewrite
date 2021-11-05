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

import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.accounts.JSONManager;
import lol.hyper.customlauncher.generic.ErrorWindow;
import lol.hyper.customlauncher.generic.InfoWindow;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TTRUpdater extends JFrame {

    public final String PATCHES_URL = "https://cdn.toontownrewritten.com/content/patchmanifest.txt";
    public final String PATCHES_URL_DL = "https://download.toontownrewritten.com/patches/";
    public final Logger logger = LogManager.getLogger(this);

    public TTRUpdater(String title, Path installLocation) throws IOException {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error(e);
        }

        frame.setIconImage(Main.icon);

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel updateStatus = new JLabel("Checking files...");
        JProgressBar progressBar = new JProgressBar(0, 22);
        frame.add(updateStatus);
        frame.add(progressBar);

        progressBar.setBounds(150, 100, 100, 30);
        updateStatus.setBounds(70, 25, 370, 40);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        if (!installLocation.toFile().exists()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Unable to check for TTR updates. We are unable to find your TTR install directory.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            logger.warn("Can't find current install directory. Skipping updates.");
        }

        logger.info("We are checking for TTR updates!");
        String patchesJSONRaw = null;
        URL url = new URL(PATCHES_URL);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty(
                "User-Agent", "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite");
        conn.connect();

        try (InputStream in = conn.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            patchesJSONRaw = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (IOException e) {
            logger.error("Unable to read patchesmanifest.txt!", e);
            JFrame errorWindow = new ErrorWindow(
                    "Unable to read patchesmanifest.txt!\n" + e.getClass().getCanonicalName() + ": " + e.getMessage());
            errorWindow.dispose();
            frame.dispose();
        }

        if (patchesJSONRaw == null) {
            JFrame errorWindow = new ErrorWindow("patchmanifest.txt returned empty.");
            logger.error("patchesJSONRaw returned null. We weren't able to read the contents of the patches list.");
            errorWindow.dispose();
            frame.dispose();
            return;
        }

        JSONObject patches = new JSONObject(patchesJSONRaw);
        ArrayList<String> filesToDownload = new ArrayList<>();

        for (String key : patches.keySet()) {
            progressBar.setValue(progressBar.getValue() + 1);
            JSONObject currentFile = (JSONObject) patches.get(key);
            String onlineHash = currentFile.getString("hash");
            JSONArray only = currentFile.getJSONArray("only");
            List<String> list = new ArrayList<>();
            for (Object value : only) {
                list.add((String) value);
            }

            if (list.contains("win32") || list.contains("win64")) {
                String localHash;
                File localFile = new File(installLocation + File.separator + key);
                updateStatus.setText("Checking file " + localFile.getName());
                if (!localFile.exists()) {
                    logger.info("-----------------------------------------------------------------------");
                    logger.info(installLocation + File.separator + key);
                    logger.info("This file is missing and will be downloaded.");
                    logger.info("-----------------------------------------------------------------------");
                    filesToDownload.add(key);
                    continue;
                }

                try {
                    localHash = calcSHA1(localFile);
                } catch (Exception e) {
                    logger.error("Unable to calculate SHA1 hash for file " + localFile.getAbsolutePath(), e);
                    JFrame errorWindow = new ErrorWindow("Unable to calculate SHA1 hash for file " + localFile.getAbsolutePath() + ".\n" + e.getClass().getCanonicalName() + ": " + e.getMessage());
                    errorWindow.dispose();
                    frame.dispose();
                    return;
                }
                logger.info("-----------------------------------------------------------------------");
                logger.info(installLocation + File.separator + key);
                logger.info("Local hash: " + localHash.toLowerCase(Locale.ENGLISH));
                logger.info("Expected hash: " + onlineHash);
                if (localHash.equalsIgnoreCase(onlineHash)) {
                    logger.info("File is good!");
                } else {
                    logger.info("File is outdated! Will be downloaded.");
                    filesToDownload.add(key);
                }
                logger.info("-----------------------------------------------------------------------");
            }
        }
        if (filesToDownload.size() > 0) {
            File tempFolder = new File("temp");
            if (!tempFolder.exists() && !tempFolder.mkdirs()) {
                logger.error("Unable to create temp folder!");
                JFrame errorWindow = new ErrorWindow("Unable to create temp folder!");
                errorWindow.dispose();
                frame.dispose();
            }

            logger.info(filesToDownload.size() + " file(s) are going to be downloaded.");
            logger.info(filesToDownload);

            for (String fileToDownload : filesToDownload) {
                JSONObject file = patches.getJSONObject(fileToDownload);
                String dl = file.getString("dl");
                try {
                    logger.info("Downloading " + PATCHES_URL_DL + dl);
                    updateStatus.setText("Downloading " + dl);
                    FileUtils.copyURLToFile(new URL(PATCHES_URL_DL + dl), new File(tempFolder + File.separator + dl));
                    logger.info("Finished downloading " + dl);
                    updateStatus.setText("Finished downloading " + dl);
                } catch (IOException e) {
                    logger.error("Unable to download file" + dl, e);
                    JFrame errorWindow = new ErrorWindow("Unable to download file " + dl + ".\n" + e.getClass().getCanonicalName() + ": " + e.getMessage());
                    errorWindow.dispose();
                    frame.dispose();
                }
                long startTime = System.nanoTime();
                logger.info("Extracting file " + dl);
                updateStatus.setText("Extracting file " + dl);
                try {
                    extractFile(dl, fileToDownload);
                } catch (IOException e) {
                    logger.error("Unable to extract file" + dl, e);
                    JFrame errorWindow = new ErrorWindow("Unable to extract file " + dl + ".\n" + e.getClass().getCanonicalName() + ": " + e.getMessage());
                    errorWindow.dispose();
                    frame.dispose();
                }
                updateStatus.setText("Finished extracting file " + dl);
                logger.info("Done, took "
                        + TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
                        + " seconds.");
            }
            File[] tempFolderFiles = tempFolder.listFiles();
            if (tempFolderFiles != null) {
                for (File currentFile : tempFolderFiles) {
                    try {
                        Files.delete(currentFile.toPath());
                    } catch (IOException e) {
                        logger.error("Unable to delete file" + currentFile.getAbsolutePath(), e);
                        JFrame errorWindow = new ErrorWindow("Unable to delete file " + currentFile.getAbsolutePath() + ".\n" + e.getClass().getCanonicalName() + ": " + e.getMessage());
                        errorWindow.dispose();
                        frame.dispose();
                    }
                }
            }
            try {
                Files.delete(Paths.get(System.getProperty("user.dir") + File.separator + "temp"));
            } catch (IOException e) {
                logger.error("Unable to delete temp folder!", e);
                JFrame errorWindow = new ErrorWindow("Unable to delete temp folder!\n" + e.getClass().getCanonicalName() + ": " + e.getMessage());
                errorWindow.dispose();
                frame.dispose();
            }
        }
        JFrame infoWindow = new InfoWindow("Finished checking for TTR updates!");
        infoWindow.dispose();
        frame.dispose();
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
     * @param tempFile The temp file's name that was downloaded.
     * @param outputName The file's output name.
     */
    private static void extractFile(String tempFile, String outputName) throws IOException {
        FileInputStream in = new FileInputStream("temp" + File.separator + tempFile);
        FileOutputStream out = new FileOutputStream(JSONManager.config().getString("ttrInstallLocation") + File.separator + outputName);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = bzIn.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        bzIn.close();
    }
}
