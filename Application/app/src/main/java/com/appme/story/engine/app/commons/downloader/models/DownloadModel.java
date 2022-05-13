package com.appme.story.engine.app.commons.downloader.models;

public class DownloadModel {

    public static final String ID = "id";
    public static final String URL = "url";
    public static final String ETAG = "etag";
    public static final String DIR_PATH = "dir_path";
    public static final String FILE_NAME = "file_name";
    public static final String TOTAL_BYTES = "total_bytes";
    public static final String DOWNLOADED_BYTES = "downloaded_bytes";
    public static final String LAST_MODIFIED_AT = "last_modified_at";

    private int id;
    private String url;
    private String eTag;
    private String dirPath;
    private String fileName;
    private long totalBytes;
    private long downloadedBytes;
    private long lastModifiedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public long getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(long lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

}
