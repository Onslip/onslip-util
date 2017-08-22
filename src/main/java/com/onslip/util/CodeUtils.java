package com.onslip.util;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

public abstract class CodeUtils {
    /**
     * Callback used by {@link #toString(Object, Class, FieldValueGetter)}.
     */
    public interface FieldValueGetter {
        /** Return a field's value
         *
         * @param object The object to inspect.
         * @param field  The field to get the value of.
         * @return The field's value.
         * @throws IllegalAccessException If this field should be skipped.
         */
        Object getFieldValue(Object object, Field field) throws IllegalAccessException;
    }

    /**
     * Utility method to dump an object into a human readable format.
     *
     * @param obj     The object to dump.
     * @param getter  A callback that reads the actual field value.
     * @return        A string readable by humans.
     */
    public static String toString(Object obj, FieldValueGetter getter) {
        return toString(obj, null, getter);
    }

    private static String toString(Object obj, Class<?> cls, FieldValueGetter getter) {
        if (toStringVisited.get().contains(obj)) {
            return "<recursion>";
        }

        if (cls == null) {
            cls = obj.getClass();
        }

        String name = cls.getSimpleName();
        StringBuilder result = new StringBuilder("[").append(name.isEmpty() ? obj.getClass().getName() : name);

        if (cls.getSuperclass() != Object.class) {
            result.append(" super=").append(toString(obj, cls.getSuperclass(), getter));
        }

        toStringVisited.get().add(obj);

        try {
            for (Field f : cls.getDeclaredFields()) {
                String value;

                try {
                    value = getter.getFieldValue(obj, f).toString();
                }
                catch (NullPointerException ignored) {
                    value = "<null>";
                }
                catch (IllegalAccessException ignored) {
                    continue;
                }

                result.append(' ').append(f.getName()).append("=").append(value);
            }

            return result.append(']').toString();
        }
        finally {
            toStringVisited.get().remove(obj);
        }
    }

    private static ThreadLocal<Set<Object>> toStringVisited = new ThreadLocal<Set<Object>>() {
        @Override public Set<Object> initialValue() {
            return new HashSet<Object>();
        }
    };

    /** Like a {@link Comparator}, but compares a value with a key instead. */
    public interface KeyComparator<T, K> {
        int compare(T object, K key);
    }

    /**
     * Finds a value in a value array based on a key. The value array must be
     * sorted according to the values' key and the KeyComparator.
     *
     * @param values  An array of values.
     * @param key     The key.
     * @param comp    A KeyComparator that compares a value with a key.
     * @param <T>     The value type.
     * @param <K>     The key type.
     * @return        The value.
     * @throws IllegalArgumentException No value was found.
     */
    public static <T, K> T findValueWithKey(T[] values, K key, KeyComparator<T, K> comp) {
        int low  = 0;
        int high = values.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = comp.compare(values[mid], key);

            if (cmp < 0) {
                low = mid + 1;
            }
            else if (cmp > 0) {
                high = mid - 1;
            }
            else {
                return values[mid];
            }
        }

        throw new IllegalArgumentException(String.format("No %s with key %s",
                                                         values.getClass().getComponentType().getSimpleName(),
                                                         key));
    }

    public static String enumToString(Enum<?> value) {
        return value.toString().toLowerCase(Locale.ENGLISH).replaceAll("_", "-");
    }

    public static <T extends Enum<T>> String enumToString(EnumSet<T> values, String separator) {
        StringBuffer result = new StringBuffer();

        for (T value : values) {
            result.append(result.length() != 0 ? separator : "").append(enumToString(value));
        }

        return result.toString();
    }

    public static <T extends Enum<T>> T stringToEnum(Class<T> cls, String value) {
        return Enum.valueOf(cls, value.toUpperCase(Locale.ENGLISH).replaceAll("-", "_"));
    }
}
