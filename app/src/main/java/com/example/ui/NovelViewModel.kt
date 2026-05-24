package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NovelViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NovelRepository

    // Base Database flows
    val characters: StateFlow<List<CharacterEntity>>
    val locations: StateFlow<List<LocationEntity>>
    val events: StateFlow<List<TimelineEventEntity>>
    val chapters: StateFlow<List<ChapterEntity>>

    // ==========================================
    // UI WORKSPACE STATE
    // ==========================================
    var selectedCharacterIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    var selectedLocationIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    var selectedEventIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    // Active Chapter Forms
    var currentChapterTitle by mutableStateOf("Bab 1: Awal Mula Takdir")
    var currentChapterNumber by mutableStateOf(1)
    var currentStylePreference by mutableStateOf("Modern") // Modern, Klasik, Puitis, Aksi, Kolosal
    var currentOutlineText by mutableStateOf("")

    // AI Status Variables
    var isSuggestingTwists by mutableStateOf(false)
        private set
    var suggestedTwistsText by mutableStateOf("")
        private set

    var isGeneratingChapter by mutableStateOf(false)
        private set
    var generatedChapterText by mutableStateOf("")
        private set

    var isSortingLore by mutableStateOf(false)
        private set
    var sortingAgentResult by mutableStateOf("")
        private set

    init {
        val database = NovelDatabase.getDatabase(application)
        repository = NovelRepository(database)

        characters = repository.charactersFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        locations = repository.locationsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        events = repository.eventsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        chapters = repository.chaptersFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate interesting templates if database is fresh to ease user on first launch
        viewModelScope.launch {
            prepopulateSamplesIfNeeded()
        }
    }

    private suspend fun prepopulateSamplesIfNeeded() {
        // We evaluate empty conditions synchronously in background
        repository.charactersFlow.collect { list ->
            if (list.isEmpty()) {
                repository.saveCharacter(
                    CharacterEntity(
                        name = "Eldrian Vane",
                        role = "Protagonis",
                        infoDescription = "Ksatria Kerajaan Aethelgard yang dituduh mengkhianati raja demi menyembunyikan ramalan meteor.",
                        developmentDetails = "Awalnya berintegritas kaku, tapi perlahan belajar menerima daerah abu-abu kehidupan demi keadilan sejati.",
                        physicalTraits = "Rambut perak, jubah bersulam emas yang sedikit robek, memegang pedang pusaka berlapis kuarsa.",
                        backgroundStory = "Yatim piatu dari daerah perbatasan, diangkat menjadi ksatria elite setelah menyelamatkan nyawa panglima.",
                        notes = "Sangat protektif terhadap rahasia adiknya."
                    )
                )
                repository.saveCharacter(
                    CharacterEntity(
                        name = "Lyanna Solis",
                        role = "Antagonis",
                        infoDescription = "Penyihir Cahaya yang memihak Dewan Pemurnian, menggunakan kekuatannya untuk mendominasi pikiran rakyat.",
                        developmentDetails = "Mewakili motif mulia yang berujung tiran. Ia percaya kontrol mutlak adalah satu-satunya jaminan perdamaian.",
                        physicalTraits = "Gaun porselen putih polos, sorot mata keemasan yang dingin tanpa emosi, selalu memegang bola cermin.",
                        backgroundStory = "Mantan guru Eldrian yang terjebak dalam delusi keselamatan kosmik setelah diserang siluman kegelapan.",
                        notes = "Memiliki kelemahan rahasia terhadap elemen air murni."
                    )
                )
            }
            return@collect
        }

        repository.locationsFlow.collect { list ->
            if (list.isEmpty()) {
                repository.saveLocation(
                    LocationEntity(
                        name = "Katedral Obsidian Akasha",
                        type = "Kuil & Tempat Ritual",
                        description = "Katedral melayang di atas kawah vulkanis purba, dibangun dari batu obsidian antipeluru yang memantulkan bayangan rasi bintang.",
                        historicalNotes = "Tempat disembunyikannya Gulungan Rasi Bintang Takdir oleh para rahib seribu tahun lalu.",
                        notes = "Hanya bisa diakses saat gerhana matahari penuh terjadi atau dengan segel kuarsa."
                    )
                )
                repository.saveLocation(
                    LocationEntity(
                        name = "Hutan Bisik Aletheia",
                        type = "Hutan Misterius",
                        description = "Hutan berlumut perak di mana pepohonan mengeluarkan suara bisikan memori pengunjung masa lalu yang dapat menjatuhkan mental pejalan kaki.",
                        historicalNotes = "Medan pertempuran pertama antara Eldrian dan kelompok ksatria dewan.",
                        notes = "Pejalan harus menutup telinga mereka dengan daun lumut perak agar tidak gila."
                    )
                )
            }
            return@collect
        }

        repository.eventsFlow.collect { list ->
            if (list.isEmpty()) {
                repository.saveEvent(
                    TimelineEventEntity(
                        eventTitle = "Konspirasi Gerhana Akasha",
                        eventOrder = 1,
                        description = "Rencana Dewan Pemurnian untuk mengekstraksi gulungan takdir di bawah katedral saat gerhana melayang terjadi.",
                        consequence = "Jika dewan berhasil, ingatan seluruh kerajaan akan diformat ulang secara paksa.",
                        notes = "Eldrian harus menyusup sendirian tanpa ketahuan."
                    )
                )
            }
            return@collect
        }
    }

    // ==========================================
    // SELECTION & SHELF MANAGEMENT (DRAG MODEL)
    // ==========================================
    fun toggleCharacterSelection(id: Long) {
        selectedCharacterIds = if (selectedCharacterIds.contains(id)) {
            selectedCharacterIds - id
        } else {
            selectedCharacterIds + id
        }
    }

    fun toggleLocationSelection(id: Long) {
        selectedLocationIds = if (selectedLocationIds.contains(id)) {
            selectedLocationIds - id
        } else {
            selectedLocationIds + id
        }
    }

    fun toggleEventSelection(id: Long) {
        selectedEventIds = if (selectedEventIds.contains(id)) {
            selectedEventIds - id
        } else {
            selectedEventIds + id
        }
    }

    fun clearWorkspaceSelections() {
        selectedCharacterIds = emptySet()
        selectedLocationIds = emptySet()
        selectedEventIds = emptySet()
        currentOutlineText = ""
        suggestedTwistsText = ""
        generatedChapterText = ""
    }

    // ==========================================
    // DATABASE LORE WRITING
    // ==========================================
    fun saveCharacter(character: CharacterEntity) = viewModelScope.launch {
        repository.saveCharacter(character)
    }

    fun deleteCharacter(character: CharacterEntity) = viewModelScope.launch {
        repository.deleteCharacter(character)
    }

    fun saveLocation(location: LocationEntity) = viewModelScope.launch {
        repository.saveLocation(location)
    }

    fun deleteLocation(location: LocationEntity) = viewModelScope.launch {
        repository.deleteLocation(location)
    }

    fun saveEvent(event: TimelineEventEntity) = viewModelScope.launch {
        repository.saveEvent(event)
    }

    fun deleteEvent(event: TimelineEventEntity) = viewModelScope.launch {
        repository.deleteEvent(event)
    }

    fun deleteChapter(chapter: ChapterEntity) = viewModelScope.launch {
        repository.deleteChapter(chapter)
    }

    // ==========================================
    // AI INTEGRATION TRIGGERS
    // ==========================================

    /**
     * AI Sort Agent helper
     */
    fun triggerSortLoreAgent(rawInput: String) {
        if (rawInput.isBlank()) return
        viewModelScope.launch {
            isSortingLore = true
            sortingAgentResult = ""
            try {
                sortingAgentResult = repository.sortLoreWithAiAgent(rawInput)
            } catch (e: Exception) {
                sortingAgentResult = "Error: ${e.localizedMessage}"
            } finally {
                isSortingLore = false
            }
        }
    }

    fun clearSortingAgentResult() {
        sortingAgentResult = ""
    }

    /**
     * AI Suggest twists
     */
    fun triggerSuggestPlotTwists() {
        viewModelScope.launch {
            isSuggestingTwists = true
            suggestedTwistsText = ""
            try {
                val charsInvolved = characters.value.filter { selectedCharacterIds.contains(it.id) }
                val locsInvolved = locations.value.filter { selectedLocationIds.contains(it.id) }
                val eventsInvolved = events.value.filter { selectedEventIds.contains(it.id) }

                suggestedTwistsText = repository.suggestPlotTwists(
                    characters = charsInvolved,
                    locations = locsInvolved,
                    events = eventsInvolved,
                    outline = currentOutlineText,
                    style = currentStylePreference
                )
            } catch (e: Exception) {
                suggestedTwistsText = "Gagal memproses plot twist: ${e.localizedMessage}"
            } finally {
                isSuggestingTwists = false
            }
        }
    }

    /**
     * AI Chapter generation complete
     */
    fun triggerGenerateChapter(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isGeneratingChapter = true
            generatedChapterText = ""
            try {
                val charsInvolved = characters.value.filter { selectedCharacterIds.contains(it.id) }
                val locsInvolved = locations.value.filter { selectedLocationIds.contains(it.id) }
                val eventsInvolved = events.value.filter { selectedEventIds.contains(it.id) }

                val generatedStory = repository.generateChapterText(
                    chapterTitle = currentChapterTitle,
                    chapterNumber = currentChapterNumber,
                    characters = charsInvolved,
                    locations = locsInvolved,
                    events = eventsInvolved,
                    outline = currentOutlineText,
                    style = currentStylePreference
                )

                generatedChapterText = generatedStory

                if (!generatedStory.startsWith("Error", true) && !generatedStory.startsWith("Gagal", true)) {
                    // Automatically persist the generated chapter to Database History
                    val newChapterEntry = ChapterEntity(
                        chapterTitle = currentChapterTitle,
                        chapterNumber = currentChapterNumber,
                        involvedCharacterIds = NovelSerializer.formatInvolvedIds(selectedCharacterIds.toList()),
                        involvedLocationIds = NovelSerializer.formatInvolvedIds(selectedLocationIds.toList()),
                        involvedEventIds = NovelSerializer.formatInvolvedIds(selectedEventIds.toList()),
                        stylePreference = currentStylePreference,
                        chapterOutline = currentOutlineText,
                        generatedText = generatedStory,
                        plotTwistSuggestion = suggestedTwistsText,
                        savedAt = System.currentTimeMillis()
                    )
                    repository.saveChapter(newChapterEntry)
                    onSuccess()
                }
            } catch (e: Exception) {
                generatedChapterText = "Gagal menyusun tulisan bab: ${e.localizedMessage}"
            } finally {
                isGeneratingChapter = false
            }
        }
    }
}
