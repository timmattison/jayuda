package com.timmattison.jayuda.jarloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.timmattison.jayuda.jarloader.exceptions.DeletionFailedException;

public abstract class LoadLibraryFromJar {
	/**
	 * The directory in which the library files are stored. Typically this is "/lib/".
	 * 
	 * @return
	 */
	protected abstract String getLibraryDirectory();

	/**
	 * The human readable name of the library
	 * 
	 * @return
	 */
	protected abstract String getLibraryReadableName();

	/**
	 * The names of the library files without their extension (ie. "library.so" becomes "library")
	 * 
	 * @return
	 */
	protected abstract String[] getLibraryNames();

	/**
	 * The size of the buffer to use when reading from the JAR
	 * 
	 * @return
	 */
	protected abstract int getReadBufferSize();

	public LoadLibraryFromJar() {
		for (String libraryName : getLibraryNames()) {
			try {
				System.loadLibrary(libraryName);

				// Library loaded successfully, exit the for loop
				break;
			} catch (UnsatisfiedLinkError e1) {
				// Library failed to load (missing file or wrong architecture), see if it can be loaded from the JAR
				try {
					loadFromJar();

					// Library loaded successfully, exit the for loop
					break;
				} catch (UnsatisfiedLinkError e2) {
					// Library failed to load (missing file or wrong architecture), go to the next file
				}
			}
		}
	}

	/**
	 * Loads a library from a JAR
	 */
	private void loadFromJar() {
		for (String libraryName : getLibraryNames()) {
			String path = libraryName + new Date().getTime();
			loadLib(path, libraryName);
		}
	}

	private boolean isWindows() {
		String operatingSystem = System.getProperty("os.name");

		if (operatingSystem.contains("Windows")) {
			return true;
		} else {
			return false;
		}
	}

	private String getSharedObjectExtension() {
		if (!isWindows()) {
			// Linux
			return ".so";
		} else {
			// Windows
			return ".dll";
		}
	}

	private String getSharedObjectName() {
		if (!isWindows()) {
			// Linux
			return "SO";
		} else {
			// Windows
			return "DLL";
		}
	}

	/**
	 * Pulls a library out of the JAR, puts it in a temp directory, loads it into memory, and cleans up after itself
	 */
	private void loadLib(String path, String name) {
		// Get the name of the library with the proper shared object extension
		name = name + getSharedObjectExtension();

		// Determine the names of our parent and library temp directories
		String parentTempDirectoryName = System.getProperty("java.io.tmpdir") + "/" + path;
		String libTempDirectoryName = parentTempDirectoryName + getLibraryDirectory();

		try {
			// Get the library's as a resource from the class loader and convert it to an input stream
			InputStream inputStream = LoadLibraryFromJar.class.getResourceAsStream(getLibraryDirectory() + name);

			if (inputStream == null) {
				throw new IllegalStateException("Couldn't get an input stream for " + getLibraryDirectory() + name);
			}

			// Create the directories necessary for our temp files
			File tempDirectory = new File(libTempDirectoryName);

			// Did we create the necessary temp directories?
			if (!tempDirectory.mkdirs()) {
				// No, throw an exception
				throw new IOException("Couldn't create temp directories");
			}

			// Create an output file in the temp lib directory
			File outputFile = new File(libTempDirectoryName + name);

			// Read the data from the JAR resource and write it into the temp lib directory
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

			// Load the library from the temp lib directory
			System.load(outputFile.toString());
		} catch (UnsatisfiedLinkError e) {
			// Rethrow this so that the caller can decide if they want to try to load another library
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load required " + getSharedObjectName() + " for " + getLibraryReadableName(), e);
		} finally {
			// Does the parent temp directory exist?
			File parentTempDirectory = new File(parentTempDirectoryName);

			if (parentTempDirectory.exists()) {
				// Yes, delete it
				try {
					recursiveDelete(parentTempDirectory, true);
				} catch (DeletionFailedException e) {
					// Ignore failures
				}
			}
		}
	}

	/**
	 * Recursively deletes a directory or deletes a file
	 * 
	 * @param file
	 * @param ignoreFailures
	 *            set to TRUE if DeletionFailedExceptions should be ignored
	 * @throws DeletionFailedException
	 */
	private void recursiveDelete(File file, boolean ignoreFailures) throws DeletionFailedException {
		// Is this a directory?
		if (file.isDirectory()) {
			// Yes, loop through all of its entries and delete them
			for (File innerFile : file.listFiles()) {
				recursiveDelete(innerFile, ignoreFailures);
			}
		}

		// Are we able to delete the file?
		if (!file.delete()) {
			// No, do we care?
			if (ignoreFailures) {
				// No
			} else {
				// Yes, throw an exception
				throw new DeletionFailedException(file.getAbsolutePath());
			}
		}
	}
}
