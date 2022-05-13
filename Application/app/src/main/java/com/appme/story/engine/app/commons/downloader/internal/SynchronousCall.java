package com.appme.story.engine.app.commons.downloader.internal;

import com.appme.story.engine.app.commons.downloader.models.Response;
import com.appme.story.engine.app.commons.downloader.request.DownloadRequest;

public class SynchronousCall {

    public final DownloadRequest request;

    public SynchronousCall(DownloadRequest request) {
        this.request = request;
    }

    public Response execute() {
        DownloadTask downloadTask = DownloadTask.create(request);
        return downloadTask.run();
    }

}
