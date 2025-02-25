package com.github.skjolber.maven.pom.recorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PomRecorder extends Thread {

	public static final String M2_DIRECTORY_PROPERTY = "POM_RECORDER_M2_DIR";

	public static final PomRecorder instance = new PomRecorder();

	protected static final String M2_DIRECTORY = System.getProperty("user.home") + "/.m2";
	protected static final String M2_REPOSITORY_DIRECTORY = "/repository";

	public static void record(File file) {
		String name = file.getAbsolutePath();
		if(name.endsWith(".pom") && name.startsWith(getMavenRepository())) {
			instance.add(name);
		}
	}

	public static String getM2Directory() {
		String fromProperties = System.getProperty(M2_DIRECTORY_PROPERTY);
		if(fromProperties != null) {
			return fromProperties;
		}
		String fromEnv = System.getenv(M2_DIRECTORY_PROPERTY);
		if (fromEnv != null) { return fromEnv; }
		return M2_DIRECTORY;
	}

	public static String getMavenRepository() {
		return getM2Directory() + M2_REPOSITORY_DIRECTORY;
	}

	private final Set<String> poms = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public PomRecorder() {
		Runtime.getRuntime().addShutdownHook(this);
	}

	public void add(String name) {
		poms.add(name);
	}

	public void close() {
		File file;
		do {
			file = new File(getM2Directory(), "maven-pom-recorder-poms-" + randomId() + ".txt");
		} while(file.exists());

		FileOutputStream fout = null;
		PrintWriter writer = null;
		try {
			fout = new FileOutputStream(file, true); // i.e. append
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fout)));

			synchronized(poms) {
				for(String pom : poms) {
					writer.println(pom);
				}
			}
		} catch (IOException e) {
			System.err.println("Instrumentation problem writing POM files to " + file + " : " + e);
		} finally {
			if(writer != null) {
				writer.close();
			}
			if(fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static String randomId() {
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 16;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();

		return generatedString;
	}

	@Override
	public void run() {
		close();
	}
}
