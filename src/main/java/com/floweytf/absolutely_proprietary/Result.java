package com.floweytf.absolutely_proprietary;

import java.util.function.Function;
import java.util.function.Supplier;

public sealed abstract class Result<V, E> {
    public static <V, E> Result<V, E> of(V value) {
        return new Value<>(value);
    }

    public static <V, E> Result<V, E> error(E value) {
        return new Error<>(value);
    }

    public abstract V get();

    public abstract V getOr(V other);

    public abstract V getLazy(Supplier<V> supplier);

    public abstract E error();

    public abstract boolean present();

    public abstract <U> Result<U, E> map(Function<V, U> mapper);

    public abstract <U> Result<U, E> fMap(Function<V, Result<U, E>> mapper);

    public abstract <Ep> Result<V, Ep> mapError(Function<E, Ep> mapper);

    private static final class Value<V, E> extends Result<V, E> {
        private final V value;

        private Value(V value) {
            this.value = value;
        }

        @Override
        public V get() {
            return value;
        }

        @Override
        public V getOr(V other) {
            return value;
        }

        @Override
        public V getLazy(Supplier<V> supplier) {
            return value;
        }

        @Override
        public E error() {
            throw new IllegalStateException("cannot call error() on result with value");
        }

        @Override
        public boolean present() {
            return true;
        }

        @Override
        public <U> Result<U, E> map(Function<V, U> mapper) {
            return new Value<>(mapper.apply(value));
        }

        @Override
        public <U> Result<U, E> fMap(Function<V, Result<U, E>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public <Ep> Result<V, Ep> mapError(Function<E, Ep> mapper) {
            return (Value<V, Ep>) this;
        }
    }

    private static final class Error<V, E> extends Result<V, E> {
        private final E error;

        private Error(E error) {
            this.error = error;
        }

        @Override
        public V get() {
            throw new IllegalStateException("cannot call get() on result with error");
        }

        @Override
        public V getOr(V other) {
            return other;
        }

        @Override
        public V getLazy(Supplier<V> supplier) {
            return supplier.get();
        }

        @Override
        public E error() {
            return error;
        }

        @Override
        public boolean present() {
            return false;
        }

        @Override
        public <U> Result<U, E> map(Function<V, U> mapper) {
            return (Error<U, E>) this;
        }

        @Override
        public <U> Result<U, E> fMap(Function<V, Result<U, E>> mapper) {
            return (Error<U, E>) this;
        }

        @Override
        public <Ep> Result<V, Ep> mapError(Function<E, Ep> mapper) {
            return new Error<>(mapper.apply(error));
        }
    }
}
