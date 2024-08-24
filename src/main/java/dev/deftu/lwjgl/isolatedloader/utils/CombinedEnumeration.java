package dev.deftu.lwjgl.isolatedloader.utils;

import java.util.Enumeration;

/**
 * Combines multiple enumerations into one.
 * The enumerations provided are prioritized in the order they are given to the constructor.
 *
 * @param <E> The type of elements in the enumeration.
 */
public class CombinedEnumeration<E> implements Enumeration<E> {

    private final Enumeration<E>[] enumerations;

    @SafeVarargs
    public CombinedEnumeration(Enumeration<E>... enumerations) {
        this.enumerations = enumerations;
    }

    public boolean hasMoreElements() {
        for (Enumeration<E> enumeration : enumerations) {
            if (enumeration.hasMoreElements()) {
                return true;
            }
        }

        return false;
    }

    public E nextElement() {
        for (Enumeration<E> enumeration : enumerations) {
            if (enumeration.hasMoreElements()) {
                return enumeration.nextElement();
            }
        }

        return null;
    }

}
