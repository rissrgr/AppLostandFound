package com.ifs21004.lostandfound.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ifs21004.lostandfound.data.local.entity.DelcomLostandFoundEntity
import com.ifs21004.lostandfound.data.local.room.DelcomLostandFoundDatabase
import com.ifs21004.lostandfound.data.local.room.IDelcomLostandFoundDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocalLostandFoundRepository(context: Context) {
    private val mDelcomLostandFoundDao: IDelcomLostandFoundDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = DelcomLostandFoundDatabase.getInstance(context)
        mDelcomLostandFoundDao = db.delcomLostandFoundDao()
    }

    fun getAllLostandFounds(): LiveData<List<DelcomLostandFoundEntity>?> = mDelcomLostandFoundDao.getAllLostandFounds()

    fun get(todoId: Int): LiveData<DelcomLostandFoundEntity?> = mDelcomLostandFoundDao.get(todoId)

    fun insert(todo: DelcomLostandFoundEntity) {
        executorService.execute { mDelcomLostandFoundDao.insert(todo) }
    }

    fun delete(todo: DelcomLostandFoundEntity) {
        executorService.execute { mDelcomLostandFoundDao.delete(todo) }
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalLostandFoundRepository? = null

        fun getInstance(
            context: Context
        ): LocalLostandFoundRepository {
            synchronized(LocalLostandFoundRepository::class.java) {
                INSTANCE = LocalLostandFoundRepository(
                    context
                )
            }
            return INSTANCE as LocalLostandFoundRepository
        }
    }
}
