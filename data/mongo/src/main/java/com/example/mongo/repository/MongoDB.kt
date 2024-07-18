package com.example.mongo.repository

import android.util.Log
import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.data.repository.MongoRepository
import com.example.util.model.Diary
import com.example.util.model.RequestState
import com.example.util.Constants.APP_ID
import com.example.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object MongoDB: MongoRepository {

    private lateinit var realm: Realm
    private val app = App.create(APP_ID)
    private val user = app.currentUser

    init {
        configureRealm()
    }

    override fun configureRealm() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user , setOf(Diary::class))
                .initialSubscriptions{sub->
                    add(
                        query = sub.query(Diary::class, "ownerId == $0", user.id),
                        name = "User's Diaries"
                    )
                }
                .log(level = LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        return if (user != null){
            try {
                realm.query<Diary>(query = "ownerId == $0" , user.id)
                    .sort(property = "date" , sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map {res->
                        RequestState.Success(res.list.groupBy {
                            it.date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        })
                    }
            }catch (e: Exception){
                return flow {
                    emit(RequestState.Error(e))
                }
            }
        }else{
            return flow {
                emit(RequestState.Error(UserNotAuthenticatedException("User not logged in")))
            }
        }
    }

    override fun getFilteredDiaries(zonedDateTime: ZonedDateTime): Flow<Diaries> {
        return if (user != null){
            try {
                realm.query<Diary>(
                    "ownerId == $0 AND date < $1 AND date > $2",
                    user.id,
                    RealmInstant.from(
                        LocalDateTime.of(zonedDateTime.toLocalDate().plusDays(1), LocalTime.MIDNIGHT).toEpochSecond(zonedDateTime.offset),
                        0
                    ),
                    RealmInstant.from(
                        LocalDateTime.of(
                            zonedDateTime.toLocalDate(),
                            LocalTime.MIDNIGHT
                        ).toEpochSecond(zonedDateTime.offset),
                        0
                    )
                ).asFlow().map { result->
                    RequestState.Success(data = result.list.groupBy {
                        it.date.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    })
                }
            }catch (e: Exception){
                return flow {
                    emit(RequestState.Error(e))
                }
            }
        }else{
            return flow {
                emit(RequestState.Error(UserNotAuthenticatedException("User not logged in")))
            }
        }
    }

    override fun getSelectedDiary(diaryId: ObjectId): Flow<RequestState<Diary>> {
        return if (user != null) {
            try {
                realm.query<Diary>(query = "_id == $0", diaryId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException("User not logged in"))) }
        }
    }

    override suspend fun addNewDiary(diary: Diary): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                try {
                    val addedDiary = copyToRealm(diary.apply { ownerId = user.id })
                    RequestState.Success( data =  addedDiary)
                }catch (e: Exception){
                    Log.d("MongoDB Error" , e.message.toString())
                    RequestState.Error(e)
                }
            }
        }else{
            return RequestState.Error(UserNotAuthenticatedException("User not logged in"))
        }
    }

    override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
        return if(user != null){
            realm.write {
                val queryDiary = query<Diary>( query = "_id == $0" , diary._id).first().find()
                if (queryDiary != null) {
                    queryDiary.title = diary.title
                    queryDiary.description = diary.description
                    queryDiary.date = diary.date
                    queryDiary.images = diary.images
                    queryDiary.mood = diary.mood
                    RequestState.Success(data = queryDiary)
                }else{
                    RequestState.Error(Exception("Queried diary not found"))
                }
            }

        }else{
            return RequestState.Error(UserNotAuthenticatedException("User not logged in"))
        }
    }

    override suspend fun deleteDiary(diaryId: ObjectId): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                val queryDiary = query<Diary>(query = "_id == $0 AND ownerId == $1" , diaryId , user.id).first().find()
                if (queryDiary != null) {
                    try {

                        delete(queryDiary)
                        RequestState.Success(data = queryDiary)
                    }catch (e: Exception){
                        RequestState.Error(e)
                    }
                }else{
                    RequestState.Error(Exception("Queried diary not found"))
                }

            }
        }else{
            return RequestState.Error(UserNotAuthenticatedException("User not logged in"))
        }
    }

    override suspend fun deleteAllDiaries(): RequestState<Boolean> {
        return if (user != null) {
            realm.write {
                val diaries = query<Diary>(query = "ownerId == $0" , user.id).find()
                try {
                    delete(diaries)
                    RequestState.Success(data = true)
                }catch (e: Exception){
                    RequestState.Error(e)
                }

            }
        }else{
            return RequestState.Error(UserNotAuthenticatedException("User not logged in"))
        }
    }
}

private class UserNotAuthenticatedException(message: String): Exception(message)