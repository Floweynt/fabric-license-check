package com.floweytf.absolutely_proprietary;

import java.lang.reflect.InvocationTargetException;

public class Log {
    private static LogImpl IMPL = null;

    private Log() {
    }

    private static LogImpl tryGetImpl() {
        final var log4j = ReflectUtil.getClass("org.apache.logging.log4j.LogManager")
            .fMap(clazz -> ReflectUtil.getMethod(clazz, "getLogger", String.class))
            .fMap(method -> ReflectUtil.invokeStatic(method, "AbsolutelyProprietary"));

        if (log4j.present()) {
            final var logMethod = ReflectUtil.getClass("org.apache.logging.log4j.Logger")
                .fMap(clazz -> ReflectUtil.getMethod(clazz, "info", String.class));

            final var loggerInst = log4j.get();

            if (!logMethod.present()) {
                throw new RuntimeException(logMethod.error());
            }

            LogImpl impl = text -> {
                try {
                    logMethod.get().invoke(loggerInst, text);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
            impl.logLine("Using log4j");
            return impl;
        }

        return System.out::println;
    }

    public static void logLine(String text) {
        if (IMPL == null) {
            IMPL = tryGetImpl();
        }
        IMPL.logLine(text);
    }

    private interface LogImpl {
        void logLine(String text);
    }
}