package com.appme.story.engine.app.folders.clipboard;


public interface FileOperationListener
{
	void onFileProcessed(String filename);
	boolean isOperationCancelled();
}
