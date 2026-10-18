package org.inkstack.core.util

interface CloseableSequence<T> : Sequence<T>, AutoCloseable
