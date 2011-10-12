package com.timmattison.jayuda.jarloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class InstallFromJar {
	/**
	 * The directory in which the library files are stored. Typically this is "/lib/".
	 * 
	 * @return
	 */
	protected abstract String getLibraryDirectory();

	/**
	 * The size of the buffer to use when reading from the JAR
	 * 
	 * @return
	 */
	protected abstract int getReadBufferSize();

	public void install(String destinationPath, String name) {
		// Did they specify the destination path?
		if (destinationPath == null) {
			throw new IllegalStateException("Destination path not specified in InstallFromJar.install");
		}

		// Did they specify the file name?
		if (name == null) {
			throw new IllegalStateException("File name not specified in InstallFromJar.install");
		}
		// Does the destination path end with a slash?
		if (!destinationPath.endsWith("/")) {
			// No, add it
			destinationPath += "/";
		}

		try {
			// Get the library's as a resource from the class loader and convert it to an input stream
			InputStream inputStream = InstallFromJar.class.getResourceAsStream(getLibraryDirectory() + name);

			// Does the destination directory exist?
			File destinationDirectory = new File(destinationPath);

			if (!destinationDirectory.exists()) {
				// No, throw an exception
				throw new IOException("Destination directory doesn't exist [" + destinationPath + "]");
			}

			// Create an output file in the destination directory
			File outputFile = new File(destinationPath + name);

			// Read the data from the JAR resource and write it into the destnation directory
			OutputStream outputStream = new FileOutputStream(outputFile);

			byte[] buffer = new byte[getReadBufferSize()];

			long count = 0;
			int bytesRead = 0;

			// NOTE: This could be an infinite loop if the stream always returns zero bytes.
			while ((bytesRead = inputStream.read(buffer)) >= 0) {
				outputStream.write(buffer, 0, bytesRead);
				count += bytesRead;
			}

			// Close both streams
			inputStream.close();
			outputStream.close();
		} catch (UnsatisfiedLinkError e) {
			// Rethrow this so that the caller can decide if they want to try to load another library
		} catch (Exception e) {
			throw new IllegalStateException("Failed to install required file " + name + " into " + destinationPath);
		}
	}
}
