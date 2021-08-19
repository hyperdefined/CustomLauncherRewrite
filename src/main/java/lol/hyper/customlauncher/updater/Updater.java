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

package lol.hyper.customlauncher.updater;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
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

public class Updater extends JFrame {

    public final String PATCHES_URL = "https://cdn.toontownrewritten.com/content/patchmanifest.txt";
    public final String PATCHES_URL_DL = "https://download.toontownrewritten.com/patches/";
    public final Logger logger = LogManager.getLogger(this);

    public Updater(String title, Path installLocation) {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error(e);
        }

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

        boolean canWeUpdate = true;

        if (!installLocation.toFile().exists()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Unable to check for TTR updates. The install location cannot be found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            canWeUpdate = false;
        }

        if (canWeUpdate) {
            logger.info("We are checking for TTR updates!");
            String patchesJSONRaw = null;
            URL patchesURL = null;
            try {
                patchesURL = new URL(PATCHES_URL);
            } catch (MalformedURLException e) {
                logger.error(e);
            }

            try (InputStream in = patchesURL.openStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                patchesJSONRaw = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                reader.close();
            } catch (IOException e) {
                logger.error(e);
                JOptionPane.showMessageDialog(
                        frame,
                        "There was an error checking files. Please check your log file for more information.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                frame.dispose();
            }

            if (patchesJSONRaw == null) {
                JOptionPane.showMessageDialog(
                        frame,
                        "There was an error checking files. Please check your log file for more information.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                frame.dispose();
                logger.error("patchesJSONRaw returned null. We weren't able to read the contents of the patches list.");
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
                        logger.error(e);
                        continue;
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
                    JOptionPane.showMessageDialog(
                            frame,
                            "Unable to create folder " + tempFolder.getAbsolutePath(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
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
                        FileUtils.copyURLToFile(
                                new URL(PATCHES_URL_DL + dl), new File(tempFolder + File.separator + dl));
                        logger.info("Finished downloading " + dl);
                        updateStatus.setText("Finished downloading " + dl);
                    } catch (IOException e) {
                        logger.error(e);
                        JOptionPane.showMessageDialog(
                                frame,
                                "There was an error saving file " + PATCHES_URL_DL + dl,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        frame.dispose();
                    }
                    long startTime = System.nanoTime();
                    logger.info("Extracting file " + dl);
                    updateStatus.setText("Extracting file " + dl);
                    try {
                        extractFile(
                                Paths.get(dl).toFile(),
                                Paths.get(fileToDownload).toFile(),
                                installLocation);
                    } catch (IOException e) {
                        logger.error(e);
                        JOptionPane.showMessageDialog(
                                frame,
                                "There was an error extracting file " + fileToDownload,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
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
                            logger.error(e);
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "There was an error deleting" + currentFile + ".",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            frame.dispose();
                        }
                    }
                }
                try {
                    Files.delete(Paths.get(System.getProperty("user.dir") + File.separator + "temp"));
                } catch (IOException e) {
                    logger.error(e);
                    JOptionPane.showMessageDialog(
                            frame, "There was an error deleting \"temp\" folder.", "Error", JOptionPane.ERROR_MESSAGE);
                    frame.dispose();
                }
            }
            JOptionPane.showMessageDialog(
                    frame, "Finished checking for TTR updates!", "Done", JOptionPane.INFORMATION_MESSAGE);
        }
        frame.dispose();
    }

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

    private static void extractFile(File tempFile, File outputFile, Path location) throws IOException {
        FileInputStream in = new FileInputStream("temp" + File.separator + tempFile);
        FileOutputStream out = new FileOutputStream(location + File.separator + outputFile);
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
