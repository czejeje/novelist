package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

// ==========================================
// ROOM ENTITIES
// ==========================================

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String, // e.g., Protagonis, Antagonis, Pendukung
    val infoDescription: String, // Karakter Info
    val developmentDetails: String, // Karakter Development
    val physicalTraits: String = "",
    val backgroundStory: String = "",
    val notes: String = ""
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // Nama tempat imajiner
    val type: String, // e.g., Kerajaan, Kota, Hutan, Planet
    val description: String, // Suasana & Geografi
    val historicalNotes: String = "",
    val notes: String = ""
)

@Entity(tableName = "timeline_events")
data class TimelineEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventTitle: String,
    val eventOrder: Int = 1, // Urutan kejadian
    val description: String, // Apa yang terjadi?
    val consequence: String = "", // Dampak event pada plot
    val notes: String = ""
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterTitle: String,
    val chapterNumber: Int,
    val involvedCharacterIds: String = "", // Comma-separated list e.g. "1,2"
    val involvedLocationIds: String = "", // e.g. "1"
    val involvedEventIds: String = "", // e.g. "3"
    val stylePreference: String = "Modern", // Modern, Klasik, Formal, Puitis, Aksi
    val chapterOutline: String = "", // Outline dari user
    val generatedText: String = "", // Hasil generate tulisan lengkap
    val plotTwistSuggestion: String = "", // Saran plot twist dari AI
    val savedAt: Long = System.currentTimeMillis()
)

// Helper objects to parse/convert serialization safely
object NovelSerializer {
    fun parseInvolvedIds(serialized: String): List<Long> {
        if (serialized.isBlank()) return emptyList()
        return serialized.split(",").mapNotNull { it.trim().toLongOrNull() }
    }
    fun formatInvolvedIds(ids: List<Long>): String {
        return ids.filter { it > 0 }.joinToString(",")
    }
}

// ==========================================
// ROOM DAO
// ==========================================

@Dao
interface NovelDao {
    // Characters
    @Query("SELECT * FROM characters ORDER BY name ASC")
    fun getAllCharactersFlow(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    // Locations
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocationsFlow(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    // Timeline Events
    @Query("SELECT * FROM timeline_events ORDER BY eventOrder ASC")
    fun getAllEventsFlow(): Flow<List<TimelineEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TimelineEventEntity): Long

    @Delete
    suspend fun deleteEvent(event: TimelineEventEntity)

    // Chapters
    @Query("SELECT * FROM chapters ORDER BY chapterNumber ASC")
    fun getAllChaptersFlow(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)
}

// ==========================================
// ROOM DATABASE
// ==========================================

@Database(
    entities = [
        CharacterEntity::class,
        LocationEntity::class,
        TimelineEventEntity::class,
        ChapterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovelDatabase : RoomDatabase() {
    abstract val novelDao: NovelDao

    companion object {
        @Volatile
        private var INSTANCE: NovelDatabase? = null

        fun getDatabase(context: Context): NovelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovelDatabase::class.java,
                    "novel_assistant_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
