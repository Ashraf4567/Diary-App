package com.example.diaryapp.util

import io.realm.kotlin.types.RealmInstant
import org.mongodb.kbson.ObjectId
import java.time.Instant

fun RealmInstant.toInstant(): Instant {

    val sec: Long = this.epochSeconds
    val nano: Int = this.nanosecondsOfSecond
    return if (sec >= 0){
        Instant.ofEpochSecond(sec, nano.toLong())
    } else {
        Instant.ofEpochSecond(sec, nano.toLong())

    }
}
