package me.shib.java.lib.utils;

public class LocalObjectStore {

    private static final String storeType = "LocalObjectStore";
    private static final LocalFileCache localFileCache = new LocalFileCache(true);
    private static final JsonUtil jsonUtil = new JsonUtil();

    public static synchronized void storeObject(String key, Object object) {
        localFileCache.putDataForKey(storeType, key, jsonUtil.toJson(object));
    }

    public <T> T getObjectForKey(String key, Class<T> classOfT) {
        return jsonUtil.fromJson(localFileCache.getDataforKey(storeType, key), classOfT);
    }

    public void deleteStoredObject(String key) {
        localFileCache.deleteData(storeType, key);
    }

}
