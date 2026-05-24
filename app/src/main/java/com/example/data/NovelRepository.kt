package com.example.data

import android.content.Context
import android.util.Log
import com.example.api.Content
import com.example.api.GeminiRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.api.GenerationConfig
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NovelRepository(private val db: NovelDatabase) {
    val charactersFlow: Flow<List<CharacterEntity>> = db.novelDao.getAllCharactersFlow()
    val locationsFlow: Flow<List<LocationEntity>> = db.novelDao.getAllLocationsFlow()
    val eventsFlow: Flow<List<TimelineEventEntity>> = db.novelDao.getAllEventsFlow()
    val chaptersFlow: Flow<List<ChapterEntity>> = db.novelDao.getAllChaptersFlow()

    // Database Actions
    suspend fun saveCharacter(character: CharacterEntity) = withContext(Dispatchers.IO) {
        db.novelDao.insertCharacter(character)
    }

    suspend fun deleteCharacter(character: CharacterEntity) = withContext(Dispatchers.IO) {
        db.novelDao.deleteCharacter(character)
    }

    suspend fun saveLocation(location: LocationEntity) = withContext(Dispatchers.IO) {
        db.novelDao.insertLocation(location)
    }

    suspend fun deleteLocation(location: LocationEntity) = withContext(Dispatchers.IO) {
        db.novelDao.deleteLocation(location)
    }

    suspend fun saveEvent(event: TimelineEventEntity) = withContext(Dispatchers.IO) {
        db.novelDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: TimelineEventEntity) = withContext(Dispatchers.IO) {
        db.novelDao.deleteEvent(event)
    }

    suspend fun saveChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        db.novelDao.insertChapter(chapter)
    }

    suspend fun deleteChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        db.novelDao.deleteChapter(chapter)
    }

    // ==========================================
    // GEMINI AI SERVICE ACTIONS
    // ==========================================

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key == "MY_GEMINI_API_KEY") "" else key
    }

    /**
     * AI Agent: Sorts a chaotic paragraph of ideas into structured character/location/event suggestions.
     */
    suspend fun sortLoreWithAiAgent(rawInput: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) return@withContext "Error: API Key Gemini belum dikonfigurasi di panel Secrets AI Studio."

        val prompt = """
            Anda adalah AI Sorting Agent khsus untuk penulis novel bernama "Sena".
            Tugas Anda adalah memilah isi paragraf beralur acak berikut dan mengelompokkannya ke dalam tiga kategori:
            1. Karakter (Nama, Peran, Info Detail, Pengembangan/Goal)
            2. Tempat Imajinatiff (Nama, Deskripsi tipe/suasana)
            3. Timeline Cerita (Judul Kejadian, Deskripsi Kejadian)
            
            Silakan buat hasil sortir dengan format yang bersih, interaktif, dan mudah dimengerti penulis dalam Bahasa Indonesia. Harap sajikan hasil berpoin-poin dengan ikon emoji yang sesuai, lalu sertakan rangkuman kesimpulan karakter development di akhir.
            
            Input teks penulis:
            "$rawInput"
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.5f),
            systemInstruction = Content(parts = listOf(Part(text = "Anda adalah Sena, AI Sorting Agent sastra yang membantu merapikan ide acak novelis menjadi database rapi.")))
        )

        try {
            val res = RetrofitClient.geminiService.generateContent(apiKey, request)
            res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "AI tidak dapat memilah teks. Silakan coba lagi."
        } catch (e: Exception) {
            "Kesalahan koneksi AI: ${e.localizedMessage}. Silakan periksa kunci API Anda."
        }
    }

    /**
     * Suggests 3 explosive plot twists based on selected lore.
     */
    suspend fun suggestPlotTwists(
        characters: List<CharacterEntity>,
        locations: List<LocationEntity>,
        events: List<TimelineEventEntity>,
        outline: String,
        style: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) return@withContext "Error: API Key belum dikonfigurasi di Secrets AI Studio."

        val charContext = characters.joinToString("\n") { 
            "- ${it.name} (${it.role}): ${it.infoDescription}. Perkembangan: ${it.developmentDetails}" 
        }
        val locContext = locations.joinToString("\n") { 
            "- ${it.name} (${it.type}): ${it.description}" 
        }
        val eventContext = events.joinToString("\n") { 
            "- ${it.eventTitle}: ${it.description}. Dampak: ${it.consequence}" 
        }

        val prompt = """
            Saya mendaftarkan elemen-elemen berikut untuk terlibat dalam chapter novel yang sedang saya buat:
            
            [KARAKTER TERLIBAT]
            $charContext
            
            [TEMPAT TERLIBAT]
            $locContext
            
            [STORYLINE / TIMELINE TERLIBAT]
            $eventContext
            
            [DESKRIPSI OUTLINE/RENCANA CHAPTER]
            $outline
            
            [PREFERENSI GAYA MENULIS]
            Gaya: $style
            
            Analisis data lore tersebut secara mendalam. Berikan 3 rancangan plot twist tak terduga (explosive/mind-bending) yang konsisten dengan lore dan perkembangan karakter di atas, namun benar-benar mengejutkan pembaca.
            
            Format output rancangan plot twist harus informatif, menarik, dan berakar kuat dari lore yang disediakan dalam Bahasa Indonesia.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.85f),
            systemInstruction = Content(parts = listOf(Part(text = "Anda adalah AI Novelist Master Plotter, ahli membuat plot twist cerdas berkorelasi dengan detail lore.")))
        )

        try {
            val res = RetrofitClient.geminiService.generateContent(apiKey, request)
            res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Gagal merumuskan plot twist. Coba ubah elemen yang terlibat."
        } catch (e: Exception) {
            "Gagal menghubungkan AI: ${e.localizedMessage}"
        }
    }

    /**
     * Generates a complete ready-to-publish chapter using styled preferences and historical consistency.
     */
    suspend fun generateChapterText(
        chapterTitle: String,
        chapterNumber: Int,
        characters: List<CharacterEntity>,
        locations: List<LocationEntity>,
        events: List<TimelineEventEntity>,
        outline: String,
        style: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) return@withContext "Error: API Key belum dikonfigurasi di Secrets AI Studio."

        val charContext = characters.joinToString("\n") { 
            "- ${it.name} (${it.role}): Ciri fisik: ${it.physicalTraits}. Latar belakang: ${it.backgroundStory}. Perkembangan/Goal: ${it.developmentDetails}" 
        }
        val locContext = locations.joinToString("\n") { 
            "- Tempati: ${it.name} (${it.type}) -> Deskirpsi: ${it.description}. Sejarah: ${it.historicalNotes}" 
        }
        val eventContext = events.joinToString("\n") { 
            "- Peristiwa: ${it.eventTitle} -> ${it.description}. Dampak: ${it.consequence}" 
        }

        val prompt = """
            Tulis sebuah chapter novel lengkap dan terperinci bernarasi tinggi berdasarkan detail berikut:
            
            [IDENTITAS CHAPTER]
            Judul: $chapterTitle
            Chapter Ke: $chapterNumber
            
            [KARAKTER YANG TERLIBAT]
            $charContext
            
            [TEMPAT / LOKASI YANG MENJADI LATAR]
            $locContext
            
            [PERISTIWA TIMELINE YANG MEMPENGARUHI]
            $eventContext
            
            [OUTLINE ALUR RENCANA]
            $outline
            
            [GAYA & PREFERENSI PENULISAN]
            Gaya Bahasa: $style (Tulis dengan kosakata yang sangat kaya, deskripsi atmosferik yang hidup, dialog yang alami, dan penjiwaan penuh sesuai style ini)
            
            KETENTUAN KHUSUS PENYUSUNAN NOVEL:
            - Tuliskan chapter dalam Bahasa Indonesia sastra berkualitas tinggi (panjang, mengalir, minim repetisi kata).
            - Pastikan karakter konsisten dengan "Goal & Perkembangan" mereka.
            - Narasi tempat harus memancing panca indera (olfaktori, visual, auditif, taktil).
            - Langsung berikan teks novel novelnya secara utuh (bab novel lengkap) tanpa ramah-tamah/pesan pembuka AI. Mulai langsung dari judul babnya.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.75f),
            systemInstruction = Content(parts = listOf(Part(text = "Anda adalah Penulis Novel Sastra Profesional Indonesia pemenang penghargaan.")))
        )

        try {
            val res = RetrofitClient.geminiService.generateContent(apiKey, request)
            res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Gagal mengeskalasi tulisan novel. Pastikan koneksi dan input lengkap."
        } catch (e: Exception) {
            "Gagal melakukan generasi teks: ${e.localizedMessage}"
        }
    }
}
