package cc.polyfrost.lwjgl.bootstrap;

import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
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

/**
 * @author xtrm
 */
public enum Lwjgl3PreProcessor {
    INSTANCE;

    private static final String LWJGL2_FUNCTION_PROVIDER_FQDN =
            "cc/polyfrost/lwjgl/bootstrap/Lwjgl2FunctionProvider";
    private final Map<String, String> remappingMap = new HashMap<>();
    private final Class<? extends ClassVisitor> remapperAdapter;

    @SuppressWarnings("unchecked")
    @SneakyThrows
    Lwjgl3PreProcessor() {
        remappingMap.put("org/lwjgl/BufferUtils", "org/lwjgl/actually3/BufferUtils");
        remappingMap.put("org/lwjgl/PointerBuffer", "org/lwjgl/actually3/PointerBuffer");
        remappingMap.put("org/lwjgl/CLongBuffer", "org/lwjgl/actually3/CLongBuffer");

        boolean asm5 = false;
        try {
            Class.forName("net.minecraftforge.common.ForgeVersion");
            asm5 = true;
        } catch (Throwable ignored) {
        }

        if (asm5) {
            remapperAdapter = (Class<? extends ClassVisitor>) Class.forName("org.objectweb.asm.commons.RemappingClassAdapter");
        } else {
            remapperAdapter = (Class<? extends ClassVisitor>) Class.forName("org.objectweb.asm.commons.ClassRemapper");
        }
    }

    @SneakyThrows
    public Path process(Path path) {
        System.out.println("Processing " + path);
        File file = path.toFile();
        String filename = file.getName();
        String name = filename.lastIndexOf('.') > 0
                ? filename.substring(0, filename.lastIndexOf('.'))
                : filename;
        String ext = filename.lastIndexOf('.') > 0
                ? filename.substring(filename.lastIndexOf('.'))
                : "";
        Path target = path.getParent().resolve(name + "-patched" + ext);
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
//                    System.out.println("Transforming: " + entryName);

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

                    transformGLConfig(node);

                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    node.accept(writer);
                    buffer = writer.toByteArray();
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (modified[0]) {
            return target;
        }
        try {
            Files.delete(target);
        } catch (IOException ignored) {
        }
        return path;
    }

    private void transferTo(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer, 0, 1024)) >= 0) {
            out.write(buffer, 0, read);
        }
    }

    private void transformGLConfig(ClassNode node) {
        if (node.name.equalsIgnoreCase("org/lwjgl/nanovg/NanoVGGLConfig")) {
            System.out.println(">>> Transforming " + node.name);
            for (MethodNode method : node.methods) {
                if (method.name.equals("configGL")) {
                    InsnList list = new InsnList();

                    list.add(new VarInsnNode(Opcodes.LLOAD, 0));
                    list.add(new TypeInsnNode(Opcodes.NEW, LWJGL2_FUNCTION_PROVIDER_FQDN));
                    list.add(new InsnNode(Opcodes.DUP));
                    list.add(new MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            LWJGL2_FUNCTION_PROVIDER_FQDN,
                            "<init>",
                            "()V",
                            false
                    ));
                    list.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "org/lwjgl/nanovg/NanoVGGLConfig",
                            "config",
                            "(JLorg/lwjgl/system/FunctionProvider;)V",
                            false
                    ));
                    list.add(new InsnNode(Opcodes.RETURN));

                    method.instructions.clear();
                    method.instructions.insert(list);
                }
            }
        }
    }
}
