package com.github.skjolber.maven.pom.recorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PomRecorderAgent {

	public static void premain(String agentArgs, Instrumentation instrumentation) {
		try {
			File f = createJarFile();

			instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(f));
		} catch (Throwable e) {
			System.err.println("Problem appending instrumentation classes to classpath, unable to record POM files");
			e.printStackTrace();

			return;
		}

		transformClass(FileInputStream.class.getName(), instrumentation);
	}

	private static File createJarFile() throws IOException, FileNotFoundException {
		File f = new File(PomRecorder.M2_DIRECTORY, "maven-pom-recorder-agent.jar");

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));

		copy(out, "/com/github/skjolber/maven/pom/recorder/PomRecorder.class");

		out.close();
		return f;
	}

	private static void copy(ZipOutputStream out, String name) throws IOException {
		ZipEntry entry = new ZipEntry(name.substring(1));
		out.putNextEntry(entry);

		InputStream in = FileSourceTransformer.class.getResourceAsStream(name);

		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		out.flush();
		out.closeEntry();
	}

	private static void transformClass(String className, Instrumentation instrumentation) {
		Class<?> targetCls = null;
		ClassLoader targetClassLoader = null;
		// see if we can get the class using forName

		try {
			targetCls = Class.forName(className);
			targetClassLoader = targetCls.getClassLoader();
			transform(targetCls, targetClassLoader, instrumentation);
			return;
		} catch (Exception ex) {
			// ignore
		}
		// otherwise iterate all loaded classes and find what we want
		for(Class<?> clazz: instrumentation.getAllLoadedClasses()) {
			if(clazz.getName().equals(className)) {
				targetCls = clazz;
				targetClassLoader = targetCls.getClassLoader();
				transform(targetCls, targetClassLoader, instrumentation);
				return;
			}
		}
		throw new RuntimeException("Failed to find class [" + className + "]");
	}

	private static void transform(Class<?> clazz, ClassLoader classLoader, Instrumentation instrumentation) {
		FileSourceTransformer dt = new FileSourceTransformer();
		instrumentation.addTransformer(dt, true);
		try {
			instrumentation.retransformClasses(clazz);
		} catch (Exception ex) {
			throw new RuntimeException("Transform failed for class: [" + clazz.getName() + "]", ex);
		}
	}
}
