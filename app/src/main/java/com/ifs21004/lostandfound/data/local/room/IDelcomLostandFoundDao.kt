package com.ifs21004.lostandfound.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ifs21004.lostandfound.data.local.entity.DelcomLostandFoundEntity

@Dao
interface IDelcomLostandFoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(delcomLostandFound: DelcomLostandFoundEntity)

    @Delete
    fun delete(delcomLostandFound: DelcomLostandFoundEntity)

    @Query("SELECT * FROM delcom_lostandfounds WHERE id = :id LIMIT 1")
    fun get(id: Int): LiveData<DelcomLostandFoundEntity?>

    @Query("SELECT * FROM delcom_lostandfounds ORDER BY created_at DESC")
    fun getAllLostandFounds(): LiveData<List<DelcomLostandFoundEntity>?>
}