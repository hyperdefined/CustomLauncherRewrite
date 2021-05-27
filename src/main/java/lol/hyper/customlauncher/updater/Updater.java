package lol.hyper.customlauncher.updater;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Updater extends JFrame {

    public final String PATCHES_URL = "https://cdn.toontownrewritten.com/content/patchmanifest.txt";
    public final String PATCHES_URL_DL = "https://download.toontownrewritten.com/patches/";

    public Updater(String title, Path installLocation) {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel updateStatus = new JLabel("Checking files...");
        JProgressBar progressBar = new JProgressBar(0, 22);
        frame.add(updateStatus);
        frame.add(progressBar);

        progressBar.setBounds(150,100,100,30);
        updateStatus.setBounds(100, 25, 370, 40);

        frame.setVisible(true);

        String patchesJSONRaw = null;
        URL patchesURL = null;
        try {
            patchesURL = new URL(PATCHES_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try (InputStream in = patchesURL.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            patchesJSONRaw = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "There was an error checking files.", "Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
        }

        if (patchesJSONRaw == null) {
            JOptionPane.showMessageDialog(frame, "There was an error checking files.", "Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
        }

        JSONObject patches = new JSONObject(patchesJSONRaw);
        ArrayList<String> filesToDownload = new ArrayList<>();

        for (String key : patches.keySet()) {
            progressBar.setValue(progressBar.getValue() + 1);
            JSONObject currentFile = (JSONObject)patches.get(key);
            String onlineHash = currentFile.getString("hash");
            JSONArray only = currentFile.getJSONArray("only");
            List<String> list = new ArrayList<>();
            for (Object value : only) {
                list.add((String)value);
            }

            if (list.contains("win32") || list.contains("win64")) {
                String localHash;
                File localFile = new File(installLocation + File.separator + key);
                updateStatus.setText("Checking file " + localFile.getName());
                if (!localFile.exists()) {
                    System.out.println("-----------------------------------------------------------------------");
                    System.out.println(installLocation + File.separator + key);
                    System.out.println("This file is missing and will be downloaded.");
                    System.out.println("-----------------------------------------------------------------------");
                    filesToDownload.add(key);
                    continue;
                }

                try {
                    localHash = calcSHA1(localFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("This shouldn't happen...");
                    continue;
                }
                System.out.println("-----------------------------------------------------------------------");
                System.out.println(installLocation + File.separator + key);
                System.out.println("Local: " + localHash.toLowerCase());
                System.out.println("Expected: " + onlineHash);
                if (localHash.equalsIgnoreCase(onlineHash)) {
                    System.out.println("File is good!");
                } else {
                    System.out.println("File is outdated! Will be downloaded.");
                    filesToDownload.add(key);
                }
                System.out.println("-----------------------------------------------------------------------");
            }
        }
        if (filesToDownload.size() > 0) {
            File tempFolder = new File("temp");
            if (!tempFolder.exists() && !tempFolder.mkdirs()) {
                JOptionPane.showMessageDialog(frame, "Unable to create folder " + tempFolder.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
                frame.dispose();
            }

            System.out.println(filesToDownload.size() + " file(s) are going to be downloaded.");
            System.out.println(filesToDownload.toString());

            for (String fileToDownload : filesToDownload) {
                JSONObject file = patches.getJSONObject(fileToDownload);
                String dl = file.getString("dl");
                try {
                    System.out.println("Downloading " + PATCHES_URL_DL + dl);
                    updateStatus.setText("Downloading " + dl);
                    FileUtils.copyURLToFile(new URL(PATCHES_URL_DL + dl), new File(tempFolder + File.separator + dl));
                    System.out.println("Done downloading " + dl);
                    updateStatus.setText("Finished downloading " + dl);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "There was an error saving file " + PATCHES_URL_DL + dl, "Error", JOptionPane.ERROR_MESSAGE);
                    frame.dispose();
                }
                long startTime = System.nanoTime();
                System.out.println("Extracting...");
                updateStatus.setText("Extracting file " + dl);
                try {
                    extractFile(Paths.get(dl).toFile(), Paths.get(fileToDownload).toFile(), installLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "There was an error extracing file " + fileToDownload, "Error", JOptionPane.ERROR_MESSAGE);
                    frame.dispose();
                }
                updateStatus.setText("Finished extracting file " + dl);
                System.out.println("Done, took " + TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS) + " seconds.");
            }
            try {
                Files.delete(Paths.get("temp"));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "There was an error deleting \"temp\" folder.", "Error", JOptionPane.ERROR_MESSAGE);
                frame.dispose();
            }
        }
        JOptionPane.showMessageDialog(frame, "Finished checking for TTR updates!", "Done", JOptionPane.INFORMATION_MESSAGE);
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
