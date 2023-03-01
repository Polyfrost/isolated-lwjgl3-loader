package cc.polyfrost.lwjgl.bootstrap.hook;

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.*;

/**
 * @author xtrm
 */
public interface LoaderHook {
    boolean canApply();

    void addURL(@NotNull URL url);

    default boolean canEval(ThrowingSupplier<Object> supplier) {
        try {
            return Objects.nonNull(supplier.get());
        } catch (LinkageError | NullPointerException | ClassNotFoundException e) {
            return false;
        } catch (Throwable e) {
            LogManager.getLogger("LWJGLBootstrap")
                    .error("Error while evaluating loader hook", e);
            return false;
        }
    }

    @FunctionalInterface
    interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    class All {
        private static final ServiceLoader<LoaderHook> serviceLoader =
                ServiceLoader.load(LoaderHook.class);

        private All() {
        }

        public static LoaderHook findAppropriate() {
            Iterator<LoaderHook> iterator = serviceLoader.iterator();
            // iterator to list
            List<LoaderHook> list = new ArrayList<>();
            iterator.forEachRemaining(list::add);

            return list.stream()
                    .filter(LoaderHook::canApply)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No loader hook found"));
        }
    }
}