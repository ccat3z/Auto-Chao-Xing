package xyz.ccat3z.chaoxingviewer.extensions

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception
import java.util.concurrent.TimeUnit

fun Observable<*>.idle(time: Long, unit: TimeUnit): Observable<*> {
    return this
            .take(1)
            .timeout(time, unit, Schedulers.newThread())
            .onErrorComplete()
            .map { throw Exception("on next") }
}

