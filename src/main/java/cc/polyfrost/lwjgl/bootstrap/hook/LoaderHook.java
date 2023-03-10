package cc.polyfrost.lwjgl.bootstrap.hook;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * @author xtrm
 */
public interface LoaderHook {
    boolean canApply();

    void addURL(@NotNull URL url);

    void provideClassloader(@NotNull Consumer<ClassLoader> consumer);

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
            list.removeIf(hook -> !hook.canApply());

            if (list.size() != 1) {
                list.forEach(System.err::println);
                throw new RuntimeException("No singular loader hook found");
            }

            return list.get(0);
        }
    }
}