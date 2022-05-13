package com.appme.story.engine.app.commons.downloader.listeners;

import com.appme.story.engine.app.commons.downloader.models.Error;

public interface OnDownloadListener {

    void onDownloadComplete();

    void onError(Error error);

}
