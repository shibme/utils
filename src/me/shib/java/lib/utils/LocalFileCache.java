package me.shib.java.lib.utils;


import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LocalFileCache {

    private static final String defaultLocalCacheDirectory = "LocalFileCacheData";

    private static Logger logger = Logger.getLogger(LocalFileCache.class.getName());

    private File localCacheDirectory;
    private long localCacheRenewalInterval;
    private ZipUtil zipUtil;
    private boolean applyKeyEncoding;

    public LocalFileCache(long localCacheRenewalIntervalInMinutes, String localCacheDirectoryName, boolean applyKeyEncoding) {
        initializeLocalCacheManager(localCacheRenewalIntervalInMinutes, localCacheDirectoryName, applyKeyEncoding);
    }

    public LocalFileCache(String localCacheDirectoryName, boolean applyKeyEncoding) {
        initializeLocalCacheManager(-1, localCacheDirectoryName, applyKeyEncoding);
    }

    public LocalFileCache(long localCacheRenewalIntervalInMinutes, boolean applyKeyEncoding) {
        initializeLocalCacheManager(localCacheRenewalIntervalInMinutes, null, applyKeyEncoding);
    }

    public LocalFileCache(boolean applyKeyEncoding) {
        initializeLocalCacheManager(-1, null, applyKeyEncoding);
    }

    private void initializeLocalCacheManager(long localCacheRenewalIntervalInMinutes, String localCacheDirectoryName, boolean applyKeyEncoding) {
        this.applyKeyEncoding = applyKeyEncoding;
        if ((localCacheDirectoryName == null) || (localCacheDirectoryName.isEmpty())) {
            this.localCacheDirectory = new File(defaultLocalCacheDirectory);
        } else {
            this.localCacheDirectory = new File(localCacheDirectoryName);
        }
        if ((!this.localCacheDirectory.exists()) || (!this.localCacheDirectory.isDirectory())) {
            if (!this.localCacheDirectory.mkdirs()) {
                logger.log(Level.WARNING, localCacheDirectory.getAbsolutePath() + " was not created.");
            }
        }
        zipUtil = new ZipUtil();
        this.localCacheRenewalInterval = localCacheRenewalIntervalInMinutes * 60000;
    }

    private String getEncodedName(String name) {
        if (!applyKeyEncoding) {
            return name;
        }
        try {
            return String.format("%x", new BigInteger(1, name.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            logger.throwing(this.getClass().getName(), "getEncodedName", e);
            return null;
        }
    }

    private String decodeKeyToName(String key) {
        if (!applyKeyEncoding) {
            return key;
        }
        try {
            String[] byteStrings = key.split("(?<=\\G.{2})");
            StringBuilder nameBuilder = new StringBuilder();
            for (String byteStr : byteStrings) {
                nameBuilder.append((char) (Byte.parseByte(byteStr, 16)));
            }
            return nameBuilder.toString();
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "decodeKeyToName", e);
            return null;
        }
    }

    public String[] getTypes() {
        String[] typeDirs = localCacheDirectory.list();
        if ((typeDirs != null) && (typeDirs.length > 0)) {
            ArrayList<String> typeList = new ArrayList<>();
            for (String typeFile : typeDirs) {
                String typeName = decodeKeyToName(typeFile);
                if (typeName != null) {
                    typeList.add(typeName);
                }
            }
            if (typeList.size() > 0) {
                String[] typeArr = new String[typeList.size()];
                typeArr = typeList.toArray(typeArr);
                return typeArr;
            }
        }
        return null;
    }

    public String[] getKeys(String type) {
        File keyDir = new File(localCacheDirectory.getPath() + File.separator + getEncodedName(type));
        if (keyDir.exists()) {
            String[] encodedKeys = keyDir.list();
            ArrayList<String> keyList = new ArrayList<>();
            if(encodedKeys != null) {
                for (String enKey : encodedKeys) {
                    String key = decodeKeyToName(enKey.replace(".json", ""));
                    if (key != null) {
                        keyList.add(key);
                    }
                }
            }
            if (keyList.size() > 0) {
                String[] keyArr = new String[keyList.size()];
                keyArr = keyList.toArray(keyArr);
                return keyArr;
            }
        }
        keyDir.mkdirs();
        return new String[0];
    }

    public String getDataforKey(String type, String key) {
        try {
            File storeDir = new File(localCacheDirectory.getPath()
                    + File.separator + getEncodedName(type));
            if ((!storeDir.exists()) || (!storeDir.isDirectory())) {
                if (!storeDir.mkdirs()) {
                    return null;
                }
            }
            File dataFile = new File(storeDir.getPath()
                    + File.separator + getEncodedName(key) + ".json");
            if (dataFile.exists()) {
                long diffTime = (new Date().getTime()) - dataFile.lastModified();
                if ((diffTime < localCacheRenewalInterval) || (localCacheRenewalInterval < 0)) {
                    StringBuilder contentBuilder = new StringBuilder();
                    BufferedReader br = new BufferedReader(new FileReader(dataFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        contentBuilder.append(line).append("\n");
                    }
                    br.close();
                    if (!contentBuilder.toString().isEmpty()) {
                        return contentBuilder.toString();
                    }
                }
            }
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "getDataforKey", e);
        }
        return null;
    }

    public boolean putDataForKey(String type, String key, String content) {
        try {
            File storeDir = new File(localCacheDirectory.getPath()
                    + File.separator + getEncodedName(type));
            if ((!storeDir.exists()) || (!storeDir.isDirectory())) {
                if (!storeDir.mkdirs()) {
                    return false;
                }
            }
            File dataFile = new File(storeDir.getPath()
                    + File.separator + getEncodedName(key) + ".json");
            if (dataFile.exists()) {
                if (!dataFile.delete()) {
                    logger.warning("Failed to delete " + dataFile.getAbsolutePath());
                }
            }
            PrintWriter pw = new PrintWriter(dataFile);
            pw.append(content);
            pw.close();
            return true;
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "putDataForKey", e);
            return false;
        }
    }

    public boolean deleteData(String type, String key) {
        try {
            File storeDir = new File(localCacheDirectory.getPath()
                    + File.separator + getEncodedName(type));
            File dataFile = new File(storeDir.getPath()
                    + File.separator + getEncodedName(key) + ".json");
            if (dataFile.exists()) {
                return dataFile.delete();
            }
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "deleteData", e);
        }
        return false;
    }

    public File getLocalCacheBackup() {
        return zipUtil.zipContent(localCacheDirectory);
    }

    public File getLocalCacheBackup(File zipFile) {
        return zipUtil.zipContent(localCacheDirectory, zipFile);
    }

}
