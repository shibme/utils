package me.shib.java.lib.utils;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    public static boolean unZip(File zipFile, File outputDirectory) {
        byte[] buffer = new byte[1024];
        try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdir();
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputDirectory.getAbsolutePath() + File.separator + fileName);
                System.out.println("Extracting : " + newFile.getAbsoluteFile());
                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            System.out.println("Extracted " + zipFile.getName() + " successfully!");
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean unZip(File zipFile, String fileToExtract, File outputDirectory) {
        List<String> filesToExtract = new ArrayList<>();
        filesToExtract.add(fileToExtract);
        return unZip(zipFile, filesToExtract, outputDirectory);
    }

    public static boolean unZip(File zipFile, List<String> filesToExtract, File outputDirectory) {
        byte[] buffer = new byte[1024];
        try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdir();
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputDirectory.getAbsolutePath() + File.separator + fileName);
                if (filesToExtract.contains(newFile.getName())) {
                    System.out.println("Extracting : " + newFile.getAbsoluteFile());
                    if (ze.isDirectory()) {
                        newFile.mkdirs();
                    } else {
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            System.out.println("Extracted " + zipFile.getName() + " successfully!");
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static String calculateChecksum(File file, String hashType) {
        InputStream is = null;
        try {
            BigInteger checkSumInt = null;
            MessageDigest digest = null;
            digest = MessageDigest.getInstance(hashType);
            is = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            checkSumInt = new BigInteger(1, md5sum);
            is.close();
            return checkSumInt.toString(16);
        } catch (NoSuchAlgorithmException | IOException e) {
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                return "";
            }
        }
    }

    /**
     * Calculates the MD5 checksum of the file in the provided file path.
     *
     * @param file the file to calculate checksum
     * @return the MD5 checksum as a String. In case of an error, will return an empty string.
     */
    public static String calculateMD5(File file) {
        return calculateChecksum(file, "MD5");
    }

    /**
     * Calculates the SHA1 checksum of the file in the provided file path.
     *
     * @param file the file to calculate checksum
     * @return the SHA1 checksum as a String. In case of an error, will return an empty string.
     */
    public static String calculateSHA1(File file) {
        return calculateChecksum(file, "SHA1");
    }

}