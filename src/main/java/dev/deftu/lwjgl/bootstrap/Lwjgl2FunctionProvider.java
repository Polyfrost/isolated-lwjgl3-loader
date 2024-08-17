package dev.deftu.lwjgl.bootstrap;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.FunctionProvider;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class Lwjgl2FunctionProvider implements FunctionProvider {

    private static final Class<?> GL_CONTEXT;
    private final Method getFunctionAddress;

    static {
        try {
            GL_CONTEXT = Class.forName("org.lwjgl.opengl.GLContext");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public Lwjgl2FunctionProvider() {
        try {
            getFunctionAddress = GL_CONTEXT.getDeclaredMethod("getFunctionAddress", String.class);
            getFunctionAddress.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Error initializing LWJGL2FunctionProvider", exception);
        }
    }

    @Override
    public long getFunctionAddress(CharSequence functionName) {
        try {
            return (long) getFunctionAddress.invoke(null, functionName.toString());
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public long getFunctionAddress(@NotNull ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException("LWJGL 2 does not support this method");
    }

}