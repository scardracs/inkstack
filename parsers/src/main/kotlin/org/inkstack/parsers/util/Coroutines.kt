package org.inkstack.parsers.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

public fun Iterable<Job>.cancelAll(cause: CancellationException? = null) {
	forEach { it.cancel(cause) }
}

public suspend fun <T> Iterable<Deferred<T>>.awaitFirst(): T {
	return channelFlow {
		for (deferred in this@awaitFirst) {
			launch {
				send(deferred.await())
			}
		}
	}.first().also { this@awaitFirst.cancelAll() }
}

public suspend fun <T> Collection<Deferred<T>>.awaitFirst(condition: (T) -> Boolean): T? {
	if (isEmpty()) {
		return null
	}
	return coroutineScope {
		val mutex = Mutex()
		var matched: T? = null
		map { deferred ->
			async {
				val result = deferred.await()
				if (condition(result)) {
					mutex.withLock {
						if (matched == null) {
							matched = result
						}
					}
				}
			}
		}.awaitAll()
		matched
	}.also { this@awaitFirst.cancelAll() }
}
