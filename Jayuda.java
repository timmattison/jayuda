package com.timmattison.jayuda;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

// Some guidance from: http://roguebasin.roguelikedevelopment.org/index.php/Java_Curses_Implementation
public class Jayuda {
	// Code and variables necessary to load the shared library
	private static final String LIB_BIN = "/lib/";
	private static final String JNINCURSES = "JNIncurses";

	static {
		try {
			System.loadLibrary(JNINCURSES);
		} catch (UnsatisfiedLinkError e) {
			loadFromJar();
		}
	}

	/**
	 * When packaged into JAR extracts DLLs, places these into
	 */
	private static void loadFromJar() {
		// we need to put both DLLs to temp dir
		String path = "JNINCURSES_" + new Date().getTime();
		loadLib(path, JNINCURSES);
	}

	/**
	 * Puts library to temp dir and loads to memory
	 */
	private static void loadLib(String path, String name) {
		// Some guidance from: http://stackoverflow.com/questions/1611357/how-to-make-a-jar-file-that-include-dll-files
		name = name + ".so";

		try {
			// have to use a stream
			InputStream in = JNIncurses.class.getResourceAsStream(LIB_BIN + name);
			// always write to different location
			File fileOut = new File(System.getProperty("java.io.tmpdir") + "/" + path + LIB_BIN + name);
			OutputStream out = FileUtils.openOutputStream(fileOut);
			IOUtils.copy(in, out);
			in.close();
			out.close();
			System.load(fileOut.toString());
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load required DLL", e);
		}
	}
}
