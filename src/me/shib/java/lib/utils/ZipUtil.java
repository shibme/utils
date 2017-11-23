package me.shib.java.lib.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtil {

    private static Logger logger = Logger.getLogger(ZipUtil.class.getName());

    public File zipContent(File sourceContent) {
        return zipContent(sourceContent, true);
    }

    public File zipContent(File sourceContent, boolean includeSourceDir) {
        return zipContent(sourceContent, null, includeSourceDir);
    }

    public File zipContent(File sourceContent, File zipFile) {
        return zipContent(sourceContent, zipFile, true);
    }

    public File zipContent(File sourceContent, File zipFile, boolean includeSourceDir) {
        if (!sourceContent.exists()) {
            return null;
        }
        if (zipFile == null) {
            zipFile = new File(sourceContent.getName() + ".zip");
        }
        List<String> fileList = new ArrayList<>();
        generateFileList(fileList, sourceContent, sourceContent, includeSourceDir);
        byte[] buffer = new byte[1024];
        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (String file : fileList) {
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);
                FileInputStream in;
                if (sourceContent.isDirectory()) {
                    if (includeSourceDir) {
                        in = new FileInputStream(sourceContent.getAbsoluteFile().getParentFile().getAbsolutePath() + File.separator + file);
                    } else {
                        in = new FileInputStream(sourceContent.getAbsolutePath() + File.separator + file);
                    }
                } else {
                    in = new FileInputStream(sourceContent);
                }
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
            }
            zos.closeEntry();
            zos.close();
            return zipFile;
        } catch (IOException e) {
            logger.throwing(this.getClass().getName(), "zipContent", e);
            return null;
        }
    }

    private void generateFileList(List<String> fileList, File sourceContent, File node, boolean includeSourceDir) {
        if (node.isFile()) {
            if (sourceContent.isDirectory()) {
                fileList.add(generateZipEntry(sourceContent, node, includeSourceDir));
            } else {
                fileList.add(generateZipEntry(null, node, includeSourceDir));
            }
        }
        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(fileList, sourceContent, new File(node, filename), includeSourceDir);
            }
        }
    }

    private String generateZipEntry(File sourceContent, File file, boolean includeSourceDir) {
        if (sourceContent != null) {
            if (includeSourceDir) {
                return file.getAbsolutePath().substring(sourceContent.getAbsoluteFile().getParentFile().getAbsolutePath().length() + 1, file.getAbsolutePath().length());
            } else {
                return file.getAbsolutePath().substring(sourceContent.getAbsolutePath().length() + 1, file.getAbsolutePath().length());
            }
        }
        return file.getName();
    }
}