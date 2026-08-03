package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PersonaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {
    @Query("SELECT * FROM personas ORDER BY isDefault DESC, id ASC")
    fun getAllPersonas(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE isActive = 1 ORDER BY isDefault DESC, id ASC")
    fun getActivePersonas(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    suspend fun getPersonaById(id: Long): PersonaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(personas: List<PersonaEntity>)

    @Update
    suspend fun updatePersona(persona: PersonaEntity)

    @Query("DELETE FROM personas WHERE id = :id")
    suspend fun deletePersona(id: Long)

    @Query("SELECT COUNT(*) FROM personas")
    suspend fun getPersonaCount(): Int
}
