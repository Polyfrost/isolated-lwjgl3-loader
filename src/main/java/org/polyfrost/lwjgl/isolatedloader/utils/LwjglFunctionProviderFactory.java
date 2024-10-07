package org.polyfrost.lwjgl.isolatedloader.utils;

import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

@SuppressWarnings("ALL")
public class LwjglFunctionProviderFactory {

    public static void getCapabilities() {
        // no-ip - This method is empty because it is not needed for our LWJGL2 function provider.
    }

    public static FunctionProvider getFunctionProvider() {
        ClassLoader classLoader = LwjglFunctionProviderFactory.class.getClassLoader().getClass().getClassLoader();

        try {
            // Attempt for LWJGL2

            // We need to create our own our own implementation of FunctionProvider (which is required by NanoVG) because LWJGL2 does not provide one.
            // Thankfully, this is incredibly easy to do as LWJGL2 provides a method to get the function address of a function name, we just need to wrap it in a FunctionProvider.

            Class<?> clz = Class.forName("org.lwjgl.opengl.GLContext", true, classLoader);
            Method method = clz.getDeclaredMethod("ngetFunctionAddress", long.class);
            method.setAccessible(true);

            return new FunctionProvider() {

                public long getFunctionAddress(ByteBuffer functionName) {
                    try {
                        return (long) method.invoke(null, MemoryUtil.memAddress(functionName));
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }

            };
        } catch (Throwable t) {
            try {
                // Attempt for LWJGL3

                // When we load LWJGL3 in our isolated class loader, any modules which require a FunctionProvider (namely, NanoVG) will attempt to load the default provided by LWJGL3.
                // But, because it's already loaded in the game's class loader, it will fail.
                // To fix this, we need to create our own implementation of FunctionProvider which wraps the default one.

                Class<?> clz = Class.forName("org.lwjgl.opengl.GL", true, classLoader);
                Method method = clz.getDeclaredMethod("getFunctionProvider");
                method.setAccessible(true);

                Object wrappingProvider = method.invoke(null);
                Class<?> providerClass = Class.forName("org.lwjgl.system.FunctionProvider", true, classLoader);
                Method getFunctionAddress = providerClass.getDeclaredMethod("getFunctionAddress", ByteBuffer.class);
                getFunctionAddress.setAccessible(true);

                return new FunctionProvider() {

                    public long getFunctionAddress(ByteBuffer functionName) {
                        try {
                            return (long) getFunctionAddress.invoke(wrappingProvider, functionName);
                        } catch (Throwable t1) {
                            throw new RuntimeException(t1);
                        }
                    }

                };
            } catch (Throwable t1) {
                t1.addSuppressed(t);
                throw new RuntimeException(t1);
            }
        }
    }

}