package com.ifs21004.lostandfound.presentation.lostandfound

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ifs21004.lostandfound.data.local.entity.DelcomLostandFoundEntity
import com.ifs21004.lostandfound.data.remote.MyResult
import com.ifs21004.lostandfound.data.remote.response.DataAddLostandFoundResponse
import com.ifs21004.lostandfound.data.remote.response.DelcomLostandFoundResponse
import com.ifs21004.lostandfound.data.remote.response.DelcomResponse
import com.ifs21004.lostandfound.presentation.ViewModelFactory
import com.ifs21004.lostandfound.data.repository.LocalLostandFoundRepository
import com.ifs21004.lostandfound.data.repository.LostandFoundRepository
import okhttp3.MultipartBody

class LostandFoundViewModel(
    private val lostandFoundRepository: LostandFoundRepository,
    private val localLostFoundRepository: LocalLostandFoundRepository
) : ViewModel() {

    fun getLostandFound(lostandfoundId: Int): LiveData<MyResult<DelcomLostandFoundResponse>> {
        return lostandFoundRepository.getLostandFound(lostandfoundId).asLiveData()
    }

    fun postLostandFound(
        title: String,
        description: String,
        status : String,
    ): LiveData<MyResult<DataAddLostandFoundResponse>> {
        return lostandFoundRepository.postLostandFound(
            title,
            description,
            status
        ).asLiveData()
    }

    fun putLostandFound(
        lostandfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ): LiveData<MyResult<DelcomResponse>> {
        return lostandFoundRepository.putLostandFound(
            lostandfoundId,
            title,
            description,
            status,
            isCompleted,
        ).asLiveData()
    }

    fun deleteLostandFound(todoId: Int): LiveData<MyResult<DelcomResponse>> {
        return lostandFoundRepository.deleteLostandFound(todoId).asLiveData()
    }

    fun getLocalLostFounds(): LiveData<List<DelcomLostandFoundEntity>?> {
        return localLostFoundRepository.getAllLostandFounds()
    }

    fun getLocalLostFound(lostfoundId: Int): LiveData<DelcomLostandFoundEntity?> {
        return localLostFoundRepository.get(lostfoundId)
    }
    fun insertLocalLostFound(lostfound: DelcomLostandFoundEntity) {
        localLostFoundRepository.insert(lostfound)
    }
    fun deleteLocalLostFound(lostfound: DelcomLostandFoundEntity) {
        localLostFoundRepository.delete(lostfound)
    }

    fun addCoverLostandFound(
        todoId: Int,
        cover: MultipartBody.Part,
    ): LiveData<MyResult<DelcomResponse>> {
        return lostandFoundRepository.addCoverLostandFound(todoId, cover).asLiveData()
    }

    companion object {
        @Volatile
        private var INSTANCE: LostandFoundViewModel? = null

        fun getInstance(
            lostandfoundRepository: LostandFoundRepository,
            localLostFoundRepository: LocalLostandFoundRepository,
        ): LostandFoundViewModel {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = LostandFoundViewModel(
                    lostandfoundRepository,
                    localLostFoundRepository
                )
            }
            return INSTANCE as LostandFoundViewModel
        }
    }
}
