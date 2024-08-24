package dev.deftu.lwjgl.isolatedloader;

import cc.polyfrost.polyio.util.PolyHashing;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unchecked")
public class Lwjgl3Transformer {

    private static final Map<String, String> remappingMap = new HashMap<>();
    private static final Class<? extends ClassVisitor> remapperAdapter;

    private Lwjgl3Transformer() {
    }

    /**
     * Transforms the given JAR file to use the utilities we provide for it's FunctionProvider
     *
     * @param path The path to the JAR file
     * @return The path to the transformed JAR file
     */
    public static Path maybeTransform(Path path) {
        File file = path.toFile();
        String filename = file.getName();
        String name = filename.lastIndexOf('.') > 0
                ? filename.substring(0, filename.lastIndexOf('.'))
                : filename;
        String ext = filename.lastIndexOf('.') > 0
                ? filename.substring(filename.lastIndexOf('.'))
                : "";
        Path target = path.getParent().resolve(name + "-patched.tmp" + ext);
        Path targetFinal = path.getParent().resolve(name + "-patched" + ext);
        if (Files.exists(target)) {
            try {
                Files.delete(target);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Couldn't delete target file " + target,
                        e
                );
            }
        }

        final boolean[] modified = {false};
        try (ZipFile zipFile = new ZipFile(path.toFile());
             ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(target))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                byte[] buffer;
                if (entryName.endsWith(".class") &&
                        !entryName.startsWith("META-INF/") &&
                        !entryName.contains("actually3") &&
                        !entryName.endsWith("-info.class")) {

                    ClassReader classReader = new ClassReader(zipFile.getInputStream(entry));
                    ClassNode dummyNode = new ClassNode();
                    classReader.accept(dummyNode, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
                    Remapper remapper = new Remapper() {
                        @Override
                        public String map(String desc) {
                            if (remappingMap.containsKey(desc)) {
                                modified[0] = true;
                                return remappingMap.get(desc);
                            }
                            return desc;
                        }
                    };
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);

                    Constructor<? extends ClassVisitor> constructor =
                            remapperAdapter.getConstructor(ClassVisitor.class, Remapper.class);
                    ClassVisitor remapperInstance = constructor.newInstance(classWriter, remapper);
                    classReader.accept(remapperInstance, ClassReader.EXPAND_FRAMES);
                    byte[] tempBuf = classWriter.toByteArray();

                    ClassNode node = new ClassNode();
                    new ClassReader(tempBuf).accept(node, ClassReader.EXPAND_FRAMES);

                    boolean isModified = transformGLConfig(node);

                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    node.accept(writer);
                    buffer = writer.toByteArray();

                    if (isModified && System.getProperty("isolatedlwjgl3loader.debug") != null) {
                        // Write the class to it's class simple name in the current directory
                        String className = node.name.replace('/', '.');
                        try (FileOutputStream fos = new FileOutputStream(className + ".class")) {
                            byte[] copiedBuffer = new byte[buffer.length];
                            System.arraycopy(buffer, 0, copiedBuffer, 0, buffer.length);

                            fos.write(copiedBuffer);
                        }
                    }

                    zos.putNextEntry(new ZipEntry(entryName.replace(dummyNode.name, node.name)));
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    transferTo(zipFile.getInputStream(entry), baos);

                    buffer = baos.toByteArray();
                    zos.putNextEntry(new ZipEntry(entry.getName()));
                }
                zos.write(buffer);
                zos.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // if the target already exists, compare the two, if they're not the same,
        // delete the old one and move the new one to the old one
        if (Files.exists(targetFinal)) {
            try {
                String currentHash = PolyHashing.hash(target, PolyHashing.MD5);
                String oldHash = PolyHashing.hash(targetFinal, PolyHashing.MD5);
                if (!currentHash.equals(oldHash)) {
                    try {
                        Files.delete(targetFinal);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Couldn't delete target file " + targetFinal,
                                e
                        );
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(
                        "Couldn't hash target file " + target,
                        e
                );
            }
        }

        try {
            Files.move(target, targetFinal);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Couldn't move target file " + target + " to " + targetFinal,
                    e
            );
        }

        if (modified[0]) {
            return targetFinal;
        }

        try {
            Files.delete(targetFinal);
        } catch (IOException ignored) {
        }

        return path;
    }

    private static void transferTo(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer, 0, 1024)) >= 0) {
            out.write(buffer, 0, read);
        }
    }

    private static boolean transformGLConfig(ClassNode node) {
        if (node.name.equalsIgnoreCase("org/lwjgl/nanovg/NanoVGGLConfig")) {
            for (MethodNode method : node.methods) {
                if (method.name.equals("configGL")) {
                    for (AbstractInsnNode insn : method.instructions.toArray()) {
                        if (insn instanceof LdcInsnNode) {
                            LdcInsnNode ldc = (LdcInsnNode) insn;
                            if (ldc.cst.equals("org.lwjgl.opengl.GL")) {
                                ldc.cst = "dev.deftu.lwjgl.isolatedloader.utils.LwjglFunctionProviderFactory";
                                break;
                            }
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

    static {
        remappingMap.put("org/lwjgl/BufferUtils", "org/lwjgl/actually3/BufferUtils");
        remappingMap.put("org/lwjgl/PointerBuffer", "org/lwjgl/actually3/PointerBuffer");
        remappingMap.put("org/lwjgl/CLongBuffer", "org/lwjgl/actually3/CLongBuffer");

        boolean asm5 = false;
        try {
            Class.forName("net.minecraftforge.common.ForgeVersion");
            asm5 = true;
        } catch (Throwable ignored) {
        }

        try {
            if (asm5) {
                remapperAdapter = (Class<? extends ClassVisitor>) Class.forName("org.objectweb.asm.commons.RemappingClassAdapter");
            } else {
                remapperAdapter = (Class<? extends ClassVisitor>) Class.forName("org.objectweb.asm.commons.ClassRemapper");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
