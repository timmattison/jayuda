package com.timmattison.jayuda.jarloader.exceptions;

public class DeletionFailedException extends Exception {
	private String absolutePath = null;

	public DeletionFailedException(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}
}
