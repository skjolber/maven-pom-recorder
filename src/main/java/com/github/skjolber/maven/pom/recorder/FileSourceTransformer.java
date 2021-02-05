package com.github.skjolber.maven.pom.recorder;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM8;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class FileSourceTransformer implements ClassFileTransformer {

	private final String targetClassName;

	public FileSourceTransformer() {
		this.targetClassName = "org/apache/maven/building/FileSource";
	}

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] byteCode) throws IllegalClassFormatException {
		if (!className.equals(targetClassName)) {
			return byteCode;
		}

		ClassReader classReader = new ClassReader(byteCode);
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		InputStreamClassVisitor exceptionThrower = new InputStreamClassVisitor(classWriter);

		classReader.accept(exceptionThrower, 0);

		return classWriter.toByteArray();
	}

	public static class InputStreamClassVisitor extends ClassVisitor {
		public InputStreamClassVisitor(ClassVisitor classVisitor) {
			super(ASM8, classVisitor);
		}

		@Override
		public MethodVisitor visitMethod(
				int access, String name, String desc, String signature,
				String[] exceptions) {

			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

			if(name.equals("<init>")) {
				return new ConstructorMethodVisitor(mv);
			}

			return mv;
		}
	}

	public static class ConstructorMethodVisitor extends MethodVisitor {

		public ConstructorMethodVisitor(MethodVisitor methodVisitor) {
			super(ASM8, methodVisitor);
		}

		@Override
		public void visitCode() {
			super.visitCode();
		}

		@Override
		public void visitInsn(int opcode) {
			// append at end of method
			if(opcode == Opcodes.RETURN) {
				mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKESTATIC, "com/github/skjolber/maven/pom/recorder/PomRecorder", "record", "(Ljava/io/File;)V", false);	    	
			}
			super.visitInsn(opcode);
		}

	}	
}
