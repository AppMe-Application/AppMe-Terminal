package com.appme.story.engine.app.commons.downloader.database;

import java.util.List;

import com.appme.story.engine.app.commons.downloader.models.DownloadModel;

public interface DbHelper {

    DownloadModel find(int id);

    void insert(DownloadModel model);

    void update(DownloadModel model);

    void updateProgress(int id, long downloadedBytes, long lastModifiedAt);

    void remove(int id);

    List<DownloadModel> getUnwantedModels(int days);

    void clear();

}
