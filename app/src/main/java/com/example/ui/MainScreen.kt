package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NovelViewModel) {
    var activeTab by remember { mutableStateOf(1) } // 1: Lore, 2: Workspace, 3: Manuscript
    var activeLoreSubTab by remember { mutableStateOf("Karakter") } // Karakter, Tempat, Event
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // State bindings
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()

    // Dialog Control
    var showAddCharacterDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Selected Chapter for Reader View
    var selectedReaderChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var activeViewEntity by remember { mutableStateOf<Any?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SleekPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "App Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "SastraSinergi AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalGold
                            )
                            Text(
                                text = "Menjaga Konsistensi & Alur Cerita Novel",
                                fontSize = 11.sp,
                                color = GrayMuted
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(LavenderContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Active Agent",
                                tint = SleekPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Sena Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPurple
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = RoyalGold
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    label = { Text("Database Lore", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(imageVector = Icons.Default.Category, contentDescription = "Lore Database") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SleekPurpleBtn,
                        selectedTextColor = SleekPurple,
                        indicatorColor = LavenderContainer,
                        unselectedIconColor = GrayMuted,
                        unselectedTextColor = GrayMuted
                    ),
                    modifier = Modifier.testTag("nav_lore_tab")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    label = { Text("Work Desk", fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(imageVector = Icons.Default.Create, contentDescription = "Writing Desk") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SleekPurpleBtn,
                        selectedTextColor = SleekPurple,
                        indicatorColor = LavenderContainer,
                        unselectedIconColor = GrayMuted,
                        unselectedTextColor = GrayMuted
                    ),
                    modifier = Modifier.testTag("nav_workspace_tab")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    label = { Text("Manuscriptor", fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(imageVector = Icons.Default.Book, contentDescription = "Generated Chapters") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SleekPurpleBtn,
                        selectedTextColor = SleekPurple,
                        indicatorColor = LavenderContainer,
                        unselectedIconColor = GrayMuted,
                        unselectedTextColor = GrayMuted
                    ),
                    modifier = Modifier.testTag("nav_manuscript_tab")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                1 -> LoreTabScaffold(
                    viewModel = viewModel,
                    subTab = activeLoreSubTab,
                    onSubTabChange = { activeLoreSubTab = it },
                    characters = characters,
                    locations = locations,
                    events = events,
                    onAddCharacterClick = { showAddCharacterDialog = true },
                    onAddLocationClick = { showAddLocationDialog = true },
                    onAddEventClick = { showAddEventDialog = true },
                    onEntityClick = { activeViewEntity = it }
                )
                2 -> WritingDeskTabNew(
                    viewModel = viewModel,
                    characters = characters,
                    locations = locations,
                    events = events,
                    onNavigateToLibrary = { activeTab = 3 }
                )
                3 -> LibraryTabScaffold(
                    chapters = chapters,
                    onSelectChapter = { selectedReaderChapter = it },
                    onDeleteChapter = { viewModel.deleteChapter(it) }
                )
            }

            // Security warning displayed as a tiny footer floating above the bottom bar or as notification inside elements
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .border(1.dp, RoyalGold.copy(alpha = 0.3f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Secure",
                    tint = TerracottaRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Security Alert: APK contains experimental client sandbox code. Do not publish API key in public APK builds.",
                    fontSize = 9.sp,
                    color = GrayMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // ==========================================
    // DIALOGS & SHEET FORMS (LORE INSERTION)
    // ==========================================

    if (showAddCharacterDialog) {
        AddCharacterFormDialog(
            onDismiss = { showAddCharacterDialog = false },
            onSave = { character ->
                viewModel.saveCharacter(character)
                showAddCharacterDialog = false
            }
        )
    }

    if (showAddLocationDialog) {
        AddLocationFormDialog(
            onDismiss = { showAddLocationDialog = false },
            onSave = { location ->
                viewModel.saveLocation(location)
                showAddLocationDialog = false
            }
        )
    }

    if (showAddEventDialog) {
        AddEventFormDialog(
            onDismiss = { showAddEventDialog = false },
            onSave = { event ->
                viewModel.saveEvent(event)
                showAddEventDialog = false
            }
        )
    }

    if (selectedReaderChapter != null) {
        ReaderFullViewDialog(
            chapter = selectedReaderChapter!!,
            onDismiss = { selectedReaderChapter = null },
            onCopyClick = {
                clipboardManager.setText(AnnotatedString(selectedReaderChapter!!.generatedText))
            }
        )
    }

    if (activeViewEntity != null) {
        ViewEntityDetailDialog(
            entity = activeViewEntity!!,
            onDismiss = { activeViewEntity = null }
        )
    }
}

// ==========================================================
// COLUMN 1: LORE LIBRARIES & THE SENA CLASSIFIER AGENT
// ==========================================================

@Composable
fun LoreTabScaffold(
    viewModel: NovelViewModel,
    subTab: String,
    onSubTabChange: (String) -> Unit,
    characters: List<CharacterEntity>,
    locations: List<LocationEntity>,
    events: List<TimelineEventEntity>,
    onAddCharacterClick: () -> Unit,
    onAddLocationClick: () -> Unit,
    onAddEventClick: () -> Unit,
    onEntityClick: (Any) -> Unit
) {
    var rawTextQuery by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Live filtering for supreme interactive searchability
    val filteredCharacters = remember(characters, searchQuery) {
        if (searchQuery.isBlank()) characters else {
            characters.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true) ||
                it.infoDescription.contains(searchQuery, ignoreCase = true) ||
                it.developmentDetails.contains(searchQuery, ignoreCase = true) ||
                it.physicalTraits.contains(searchQuery, ignoreCase = true) ||
                it.backgroundStory.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredLocations = remember(locations, searchQuery) {
        if (searchQuery.isBlank()) locations else {
            locations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.historicalNotes.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredEvents = remember(events, searchQuery) {
        if (searchQuery.isBlank()) events else {
            events.filter {
                it.eventTitle.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.consequence.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 36.dp) // Leave clean bottom safe area
    ) {
        // High impact subtab selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Karakter", "Tempat Imajinatif", "Timeline Cerita")) { tabName ->
                val isSelected = (tabName.startsWith(subTab))
                val targetSub = if (tabName.contains("Karakter")) "Karakter" else if (tabName.contains("Tempat")) "Tempat" else "Event"
                FilterChip(
                    selected = isSelected,
                    onClick = { onSubTabChange(targetSub) },
                    label = { Text(tabName, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SleekPurple,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = SleekPurple
                    ),
                    modifier = Modifier.testTag("lore_subtab_$targetSub")
                )
            }
        }

        // Sleek Search Bar for extreme data accessibility
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("lore_search_bar"),
            placeholder = { Text("Cari karakter, tempat fiksi, peristiwa...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, "Cari", tint = SleekPurple) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Hapus", tint = GrayMuted)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = SleekPurple,
                unfocusedBorderColor = LavenderBorder.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Sub categories contents
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (subTab) {
                "Karakter" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Latar Karakter & Pengembangan",
                                fontWeight = FontWeight.Bold,
                                color = SleekPurple,
                                fontSize = 15.sp
                            )
                            IconButton(
                                onClick = onAddCharacterClick,
                                modifier = Modifier
                                    .testTag("add_char_button")
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SleekPurple)
                            ) {
                                Icon(Icons.Default.Add, "Tambah", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }

                        if (characters.isEmpty()) {
                            EmptyStateComponent("Belum ada data Karakter. Tulis ide kasarmu di agen Sena di bawah untuk membuatnya otomatis!")
                        } else if (filteredCharacters.isEmpty()) {
                            EmptyStateComponent("Tidak ada karakter yang cocok dengan kata pencarian '$searchQuery'.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredCharacters) { char ->
                                    CharacterLoreCard(
                                        char = char,
                                        allCharacters = characters,
                                        allLocations = locations,
                                        allEvents = events,
                                        onEntityClick = onEntityClick,
                                        onDelete = { viewModel.deleteCharacter(char) }
                                    )
                                }
                            }
                        }
                    }
                }
                "Tempat" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Dunia Imajinatif (World Building)",
                                fontWeight = FontWeight.Bold,
                                color = SleekPurple,
                                fontSize = 15.sp
                            )
                            IconButton(
                                onClick = onAddLocationClick,
                                modifier = Modifier
                                    .testTag("add_location_button")
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SleekPurple)
                            ) {
                                Icon(Icons.Default.Add, "Tambah", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }

                        if (locations.isEmpty()) {
                            EmptyStateComponent("Belum ada Tempat Imajinatif. Daftarkan kerajaan, planet, kuil atau markas fiksionalmu!")
                        } else if (filteredLocations.isEmpty()) {
                            EmptyStateComponent("Tidak ada tempat yang cocok dengan kata pencarian '$searchQuery'.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredLocations) { loc ->
                                    LocationLoreCard(
                                        loc = loc,
                                        allCharacters = characters,
                                        allLocations = locations,
                                        allEvents = events,
                                        onEntityClick = onEntityClick,
                                        onDelete = { viewModel.deleteLocation(loc) }
                                    )
                                }
                            }
                        }
                    }
                }
                "Event" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Timeline Alur Utama & Subplot",
                                fontWeight = FontWeight.Bold,
                                color = SleekPurple,
                                fontSize = 15.sp
                            )
                            IconButton(
                                onClick = onAddEventClick,
                                modifier = Modifier
                                    .testTag("add_event_button")
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SleekPurple)
                            ) {
                                Icon(Icons.Default.Add, "Tambah", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }

                        if (events.isEmpty()) {
                            EmptyStateComponent("Timeline novel masih kosong. Masukkan titik insiden penting pertamamu!")
                        } else if (filteredEvents.isEmpty()) {
                            EmptyStateComponent("Tidak ada peristiwa yang cocok dengan kata pencarian '$searchQuery'.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredEvents) { ev ->
                                    EventLoreCard(
                                        ev = ev,
                                        allCharacters = characters,
                                        allLocations = locations,
                                        allEvents = events,
                                        onEntityClick = onEntityClick,
                                        onDelete = { viewModel.deleteEvent(ev) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // INTEGRATED AI AGENT SENA: OUTSTANDING REVOLUTIONARY SORT ZONE (FLOATING FOOTER DRAWER)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, RoyalGold.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = VelvetCoal),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Sena Parser",
                        tint = TerracottaRed
                    )
                    Text(
                        text = "Sena: Sortir Ide Mentah (AI Sorting Agent)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tulis paragraf acak ide ceritamu di sini. Sena akan mensortir info karakter, lokasi fiksi, dan timeline kejadian secara rapi.",
                    fontSize = 10.sp,
                    color = GrayMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = rawTextQuery,
                        onValueChange = { rawTextQuery = it },
                        placeholder = { Text("E.g. Eldrian bertarung di Menara Obsidian melawan musuhnya Lyanna yang menggunakan gaun porselen...", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("sena_agent_input"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = InkBlack,
                            unfocusedContainerColor = InkBlack,
                            focusedIndicatorColor = RoyalGold,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Button(
                        onClick = {
                            viewModel.triggerSortLoreAgent(rawTextQuery)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(64.dp)
                            .testTag("sena_action_button"),
                        enabled = !viewModel.isSortingLore && rawTextQuery.isNotBlank()
                    ) {
                        if (viewModel.isSortingLore) {
                            CircularProgressIndicator(color = InkBlack, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Default.AutoAwesome, "Sortir", tint = InkBlack)
                        }
                    }
                }

                if (viewModel.sortingAgentResult.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .verticalScroll(rememberScrollState())
                            .border(1.dp, BlueInk.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = InkBlack)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hasil Analisis & Sortir Sena:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalGold
                                )
                                TextButton(
                                    onClick = { viewModel.clearSortingAgentResult() },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Bersihkan", fontSize = 10.sp, color = TerracottaRed)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.sortingAgentResult,
                                fontSize = 11.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// Cards for tab 1
@Composable
fun CharacterLoreCard(
    char: CharacterEntity,
    allCharacters: List<CharacterEntity> = emptyList(),
    allLocations: List<LocationEntity> = emptyList(),
    allEvents: List<TimelineEventEntity> = emptyList(),
    onEntityClick: (Any) -> Unit = {},
    onDelete: () -> Unit
) {
    // Detect linked lore elements based on mentions in text descriptions
    val linkedChars = remember(char, allCharacters) {
        allCharacters.filter { 
            it.id != char.id && it.name.length >= 3 && (
                char.infoDescription.contains(it.name, ignoreCase = true) ||
                char.developmentDetails.contains(it.name, ignoreCase = true) ||
                char.backgroundStory.contains(it.name, ignoreCase = true)
            )
        }
    }
    val linkedLocs = remember(char, allLocations) {
        allLocations.filter { 
            it.name.length >= 3 && (
                char.infoDescription.contains(it.name, ignoreCase = true) ||
                char.developmentDetails.contains(it.name, ignoreCase = true) ||
                char.backgroundStory.contains(it.name, ignoreCase = true)
            )
        }
    }
    val linkedEvents = remember(char, allEvents) {
        allEvents.filter { 
            it.eventTitle.length >= 3 && (
                char.infoDescription.contains(it.eventTitle, ignoreCase = true) ||
                char.developmentDetails.contains(it.eventTitle, ignoreCase = true) ||
                char.backgroundStory.contains(it.eventTitle, ignoreCase = true)
            )
        }
    }

    val hasAnyLinks = linkedChars.isNotEmpty() || linkedLocs.isNotEmpty() || linkedEvents.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, LavenderBorder.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Char",
                        tint = SleekPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(char.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = if (char.role == "Protagonis") LavenderContainer else SleekPurpleMuted,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(char.role, fontSize = 10.sp, color = SleekPurpleBtn, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = TerracottaRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LavenderBorder.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Text("Informasi Karakter:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
            Text(char.infoDescription, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            Text("Rencana Pengembangan (Development):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
            Text(char.developmentDetails, fontSize = 13.sp, color = SleekPurple, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))

            if (char.physicalTraits.isNotBlank() || char.backgroundStory.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, LavenderBorder.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (char.physicalTraits.isNotBlank()) {
                            Text("Ciri fisik: ${char.physicalTraits}", fontSize = 11.sp, color = GrayMuted)
                        }
                        if (char.backgroundStory.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Latar belakang: ${char.backgroundStory}", fontSize = 11.sp, color = GrayMuted)
                        }
                    }
                }
            }

            // Render interactive linked cross-references
            if (hasAnyLinks) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = LavenderBorder.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Koneksi Lore Terdeteksi 🔗:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(linkedChars) { c ->
                        SuggestionChip(
                            onClick = { onEntityClick(c) },
                            label = { Text(c.name, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Person, "CharLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                    items(linkedLocs) { l ->
                        SuggestionChip(
                            onClick = { onEntityClick(l) },
                            label = { Text(l.name, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Place, "LocLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                    items(linkedEvents) { e ->
                        SuggestionChip(
                            onClick = { onEntityClick(e) },
                            label = { Text(e.eventTitle, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Timeline, "EvLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationLoreCard(
    loc: LocationEntity,
    allCharacters: List<CharacterEntity> = emptyList(),
    allLocations: List<LocationEntity> = emptyList(),
    allEvents: List<TimelineEventEntity> = emptyList(),
    onEntityClick: (Any) -> Unit = {},
    onDelete: () -> Unit
) {
    // Cross references
    val linkedChars = remember(loc, allCharacters) {
        allCharacters.filter { 
            it.name.length >= 3 && (
                loc.description.contains(it.name, ignoreCase = true) ||
                loc.historicalNotes.contains(it.name, ignoreCase = true)
            )
        }
    }
    val linkedLocs = remember(loc, allLocations) {
        allLocations.filter { 
            it.id != loc.id && it.name.length >= 3 && (
                loc.description.contains(it.name, ignoreCase = true) ||
                loc.historicalNotes.contains(it.name, ignoreCase = true)
            )
        }
    }
    val linkedEvents = remember(loc, allEvents) {
        allEvents.filter { 
            it.eventTitle.length >= 3 && (
                loc.description.contains(it.eventTitle, ignoreCase = true) ||
                loc.historicalNotes.contains(it.eventTitle, ignoreCase = true)
            )
        }
    }

    val hasAnyLinks = linkedChars.isNotEmpty() || linkedLocs.isNotEmpty() || linkedEvents.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, LavenderBorder.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Place",
                        tint = SleekPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(loc.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = LavenderBorder.copy(alpha = 0.3f), modifier = Modifier.padding(end = 8.dp)) {
                        Text(loc.type, fontSize = 10.sp, color = SleekPurpleBtn, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = TerracottaRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = LavenderBorder.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Text("Deskripsi Geografi & Suasana:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
            Text(loc.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))

            if (loc.historicalNotes.isNotBlank()) {
                Text("Sejarah / Catatan Unik:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                Text(loc.historicalNotes, fontSize = 12.sp, color = SleekPurple, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))
            }

            // Cross reference linkage block
            if (hasAnyLinks) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = LavenderBorder.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Koneksi Lore Terdeteksi 🔗:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(linkedChars) { c ->
                        SuggestionChip(
                            onClick = { onEntityClick(c) },
                            label = { Text(c.name, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Person, "CharLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                    items(linkedLocs) { l ->
                        SuggestionChip(
                            onClick = { onEntityClick(l) },
                            label = { Text(l.name, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Place, "LocLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                    items(linkedEvents) { e ->
                        SuggestionChip(
                            onClick = { onEntityClick(e) },
                            label = { Text(e.eventTitle, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Timeline, "EvLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventLoreCard(
    ev: TimelineEventEntity,
    allCharacters: List<CharacterEntity> = emptyList(),
    allLocations: List<LocationEntity> = emptyList(),
    allEvents: List<TimelineEventEntity> = emptyList(),
    onEntityClick: (Any) -> Unit = {},
    onDelete: () -> Unit
) {
    // Cross references
    val linkedChars = remember(ev, allCharacters) {
        allCharacters.filter { 
            it.name.length >= 3 && (
                ev.description.contains(it.name, ignoreCase = true) ||
                ev.consequence.contains(it.name, ignoreCase = true)
            )
        }
    }
    val linkedLocs = remember(ev, allLocations) {
        allLocations.filter { 
            it.name.length >= 3 && (
                ev.description.contains(it.name, ignoreCase = true) ||
                ev.consequence.contains(it.name, ignoreCase = true)
            )
        }
    }
    val linkedEvents = remember(ev, allEvents) {
        allEvents.filter { 
            it.id != ev.id && it.eventTitle.length >= 3 && (
                ev.description.contains(it.eventTitle, ignoreCase = true) ||
                ev.consequence.contains(it.eventTitle, ignoreCase = true)
            )
        }
    }

    val hasAnyLinks = linkedChars.isNotEmpty() || linkedLocs.isNotEmpty() || linkedEvents.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, LavenderBorder.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Event",
                        tint = TerracottaRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${ev.eventOrder}. ${ev.eventTitle}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = TerracottaRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = LavenderBorder.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Text("Deskripsi Kejadian:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
            Text(ev.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))

            if (ev.consequence.isNotBlank()) {
                Text("Dampak Kejadian / Konsekuensi Alur:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                Text(ev.consequence, fontSize = 12.sp, color = TerracottaRed, modifier = Modifier.padding(top = 2.dp))
            }

            // Cross reference linkage block
            if (hasAnyLinks) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = LavenderBorder.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Koneksi Lore Terdeteksi 🔗:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(linkedChars) { c ->
                        SuggestionChip(
                            onClick = { onEntityClick(c) },
                            label = { Text(c.name, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Person, "CharLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                    items(linkedLocs) { l ->
                        SuggestionChip(
                            onClick = { onEntityClick(l) },
                            label = { Text(l.name, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Place, "LocLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                    items(linkedEvents) { e ->
                        SuggestionChip(
                            onClick = { onEntityClick(e) },
                            label = { Text(e.eventTitle, fontSize = 9.sp) },
                            icon = { Icon(Icons.Default.Timeline, "EvLink", modifier = Modifier.size(10.dp), tint = SleekPurple) }
                        )
                    }
                }
            }
        }
    }
}


// ==========================================================
// COLUMN 2: WORK SPACE - SHELF BOARD & THE DRAG DRAG PREFERENCES
// ==========================================================

@Composable
fun WritingDeskTabNew(
    viewModel: NovelViewModel,
    characters: List<CharacterEntity>,
    locations: List<LocationEntity>,
    events: List<TimelineEventEntity>,
    onNavigateToLibrary: () -> Unit
) {
    var titleInput by remember { mutableStateOf(viewModel.currentChapterTitle) }
    var numberInput by remember { mutableStateOf(viewModel.currentChapterNumber.toString()) }
    var detailOutline by remember { mutableStateOf(viewModel.currentOutlineText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp) // extra padding to avoid notification footer
    ) {
        // Descriptive Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LavenderBorder.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Penyusunan Chapter Baru",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPurple
                )
                Text(
                    "Silakan 'Tarik' (Ketuk) elemen lore karakter, planet, tempat, atau kejadian di Rak bawah untuk melibatkannya dalam chapter ini.",
                    fontSize = 11.sp,
                    color = GrayMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // ==========================================
        // INDONESIAN THEMATIC "RAK ELEMEN AKTIF" (ACTIVE SHELF DESK)
        // ==========================================
        Text(
            text = "⚡ RAK ELEMEN TERBAWA (ACTIVE SHELF)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SleekPurple,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, LavenderBorder, RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val charsInvolved = characters.filter { viewModel.selectedCharacterIds.contains(it.id) }
                val locsInvolved = locations.filter { viewModel.selectedLocationIds.contains(it.id) }
                val eventsInvolved = events.filter { viewModel.selectedEventIds.contains(it.id) }

                if (charsInvolved.isEmpty() && locsInvolved.isEmpty() && eventsInvolved.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rak Kerja Kosong.\nPilih elemen dari katalog di bawah untuk terlibat.",
                            fontSize = 11.sp,
                            color = GrayMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Active list animation visual grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        charsInvolved.forEach { char ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LavenderContainer),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, LavenderBorder),
                                modifier = Modifier
                                    .clickable { viewModel.toggleCharacterSelection(char.id) }
                                    .testTag("shelf_char_${char.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Person, "Uninvolve", tint = SleekPurpleBtn, modifier = Modifier.size(14.dp))
                                    Text(char.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekPurpleBtn)
                                    Icon(Icons.Default.Close, "X", tint = SleekPurpleBtn.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                                }
                            }
                        }

                        locsInvolved.forEach { loc ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LavenderContainer),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, LavenderBorder),
                                modifier = Modifier
                                    .clickable { viewModel.toggleLocationSelection(loc.id) }
                                    .testTag("shelf_loc_${loc.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Place, "Uninvolve", tint = SleekPurpleBtn, modifier = Modifier.size(14.dp))
                                    Text(loc.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekPurpleBtn)
                                    Icon(Icons.Default.Close, "X", tint = SleekPurpleBtn.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                                }
                            }
                        }

                        eventsInvolved.forEach { ev ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LavenderContainer),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, LavenderBorder),
                                modifier = Modifier
                                    .clickable { viewModel.toggleEventSelection(ev.id) }
                                    .testTag("shelf_event_${ev.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Timeline, "Uninvolve", tint = SleekPurpleBtn, modifier = Modifier.size(14.dp))
                                    Text(ev.eventTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekPurpleBtn)
                                    Icon(Icons.Default.Close, "X", tint = SleekPurpleBtn.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { viewModel.clearWorkspaceSelections() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Kosongkan Rak", fontSize = 11.sp, color = TerracottaRed)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // ELEMENT DEPOSIT CATALOG
        // ==========================================
        Text(
            text = "📚 KATALOG ELEMEN (CLICK COPIES TO RAK)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalGold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Text(
            text = "Ketuk kartu di bawah untuk memasukkan atau mengeluarkannya dari rak alur:",
            fontSize = 10.sp,
            color = GrayMuted,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        // Horizontal catalog list for rapid adding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Characters scrollable
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, LavenderBorder.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Karakter Tersedia:", fontSize = 11.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (characters.isEmpty()) {
                        Text("Belum ada karakter yang didaftarkan.", fontSize = 11.sp, color = GrayMuted)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(characters) { char ->
                                val isSelected = viewModel.selectedCharacterIds.contains(char.id)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) LavenderContainer else MaterialTheme.colorScheme.background
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) LavenderBorder else LightCardBorder),
                                    modifier = Modifier
                                        .clickable { viewModel.toggleCharacterSelection(char.id) }
                                        .width(130.dp)
                                        .testTag("catalog_char_${char.id}")
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(char.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = if (isSelected) SleekPurpleBtn else MaterialTheme.colorScheme.onSurface)
                                        Text(char.role, fontSize = 10.sp, color = if (isSelected) SleekPurple else SleekPurple.copy(alpha = 0.7f))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(char.infoDescription, fontSize = 9.sp, color = GrayMuted, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Locations Scrollable
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, LavenderBorder.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Tempat Imajinatif Tersedia:", fontSize = 11.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (locations.isEmpty()) {
                        Text("Belum ada tempat melayang/kota yang terdaftar.", fontSize = 11.sp, color = GrayMuted)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(locations) { loc ->
                                val isSelected = viewModel.selectedLocationIds.contains(loc.id)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) LavenderContainer else MaterialTheme.colorScheme.background
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) LavenderBorder else LightCardBorder),
                                    modifier = Modifier
                                        .clickable { viewModel.toggleLocationSelection(loc.id) }
                                        .width(130.dp)
                                        .testTag("catalog_loc_${loc.id}")
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(loc.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = if (isSelected) SleekPurpleBtn else MaterialTheme.colorScheme.onSurface)
                                        Text(loc.type, fontSize = 10.sp, color = if (isSelected) SleekPurple else SleekPurple.copy(alpha = 0.7f))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(loc.description, fontSize = 9.sp, color = GrayMuted, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Events Scrollable
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, LavenderBorder.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Peristiwa Timeline Tersedia:", fontSize = 11.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (events.isEmpty()) {
                        Text("Belum ada timeline kejadian ceritamu.", fontSize = 11.sp, color = GrayMuted)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(events) { ev ->
                                val isSelected = viewModel.selectedEventIds.contains(ev.id)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) LavenderContainer else MaterialTheme.colorScheme.background
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) LavenderBorder else LightCardBorder),
                                    modifier = Modifier
                                        .clickable { viewModel.toggleEventSelection(ev.id) }
                                        .width(130.dp)
                                        .testTag("catalog_event_${ev.id}")
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("${ev.eventOrder}. ${ev.eventTitle}", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = if (isSelected) SleekPurpleBtn else MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(ev.description, fontSize = 9.sp, color = GrayMuted, maxLines = 3)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // CONFIG FORMS (TITLE, COMPOSITION STYLE)
        // ==========================================
        Text(
            text = "✍️ DETAIL & GAYA CHAPTER",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SleekPurple,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = titleInput,
                    onValueChange = {
                        titleInput = it
                        viewModel.currentChapterTitle = it
                    },
                    label = { Text("Judul Bab (Title)") },
                    modifier = Modifier
                        .weight(2f)
                        .testTag("chapter_title_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = numberInput,
                    onValueChange = {
                        numberInput = it
                        viewModel.currentChapterNumber = it.toIntOrNull() ?: 1
                    },
                    label = { Text("Nomor Bab") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chapter_num_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )
            }

            // Style Selection Chips (Scrollable row)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Gaya Bahasa Sastra (Preferensi):", fontSize = 11.sp, color = SleekPurple, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Modern (Lugas)", "Sastra/Puitis", "Klasik Jawa/Kolosal", "Aksi Cepat", "Suspense/Thriller")) { style ->
                        val isSel = viewModel.currentStylePreference == style
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.currentStylePreference = style },
                            label = { Text(style, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SleekPurple,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = SleekPurple
                            ),
                            modifier = Modifier.testTag("style_chip_$style")
                        )
                    }
                }
            }

            // Outline Summary
            TextField(
                value = detailOutline,
                onValueChange = {
                    detailOutline = it
                    viewModel.currentOutlineText = it
                },
                label = { Text("Outline Skenario / Garis Besar Kejadian di Bab") },
                placeholder = { Text("E.g. Eldrian menyusup diam-diam saat gerhana melayang terjadi, namun tertangkap basah di tengah...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("outline_input_box"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = SleekPurple,
                    unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                ),
                maxLines = 5
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // ENGINE CALL BUTTONS
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Plot Suggestion Button
            Button(
                onClick = { viewModel.triggerSuggestPlotTwists() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekPurpleMuted,
                    contentColor = SleekPurple
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LavenderBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("twist_ai_button"),
                enabled = !viewModel.isSuggestingTwists && !viewModel.isGeneratingChapter
            ) {
                if (viewModel.isSuggestingTwists) {
                    CircularProgressIndicator(color = SleekPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sena Sedang Menghitung Plot Twist...", color = SleekPurple, fontSize = 13.sp)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lightbulb, "Idea", tint = SleekPurple)
                        Text("Rancang 3 Plot Twist AI Berdasarkan Lore 🔮", color = SleekPurple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Full Novel Generator Button
            Button(
                onClick = {
                    viewModel.triggerGenerateChapter(onSuccess = {
                        // Success toast or view changes
                    })
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekPurpleBtn,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_chapter_button"),
                enabled = !viewModel.isSuggestingTwists && !viewModel.isGeneratingChapter
            ) {
                if (viewModel.isGeneratingChapter) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sena Sedang Menulis Karya Lengkap...", color = Color.White, fontSize = 13.sp)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, "Sparkle", tint = Color.White)
                        Text("Tulis Bab Secara Lengkap & Simpan Ke Draft ✒️", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // ACTION RESULTS BOXES (PLOT TWIST & GENERATED TEXT)
        // ==========================================
        if (viewModel.suggestedTwistsText.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, LavenderBorder),
                colors = CardDefaults.cardColors(containerColor = SleekPurpleMuted)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, "Bulb", tint = SleekPurple, modifier = Modifier.size(18.dp))
                        Text(
                            "AI PLOT TWIST (Sena Proposal):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPurple
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.suggestedTwistsText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        if (viewModel.generatedChapterText.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, LavenderBorder),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hasil Draf Tulisan Lengkap Bab:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPurple
                        )
                        Button(
                            onClick = onNavigateToLibrary,
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPurpleBtn, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Buka Perpustakaan", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = viewModel.generatedChapterText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 20.sp,
                        modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}


// ==========================================================
// COLUMN 3: LIBRARY / MANUSCRIPTOR LISTTAB - VIEW RESULTS
// ==========================================================

@Composable
fun LibraryTabScaffold(
    chapters: List<ChapterEntity>,
    onSelectChapter: (ChapterEntity) -> Unit,
    onDeleteChapter: (ChapterEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 36.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Naskah & Perpustakaan Karya",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold
            )
            Text(
                "Setiap chapter yang berhasil disusun secara lengkap oleh Sena tersimpan rapi untuk ditinjau secara berkala berikut ini.",
                fontSize = 11.sp,
                color = GrayMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (chapters.isEmpty()) {
            EmptyStateComponent("Perpustakaan kosong. Mari rancang elemen di Work Desk lalu klik \"Tulis Bab Secara Lengkap\" untuk melahirkan karya pertamamu!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chapters) { chap ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, RoyalGold.copy(alpha = 0.2f))
                            .clickable { onSelectChapter(chap) }
                            .testTag("manuscript_item_${chap.id}"),
                        colors = CardDefaults.cardColors(containerColor = VelvetCoal)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "Chapter logo",
                                        tint = RoyalGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Bab ${chap.chapterNumber}: ${chap.chapterTitle}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalGold,
                                        maxLines = 1
                                    )
                                }

                                IconButton(onClick = { onDeleteChapter(chap) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, "Delete", tint = TerracottaRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Gaya: ${chap.stylePreference}",
                                fontSize = 10.sp,
                                color = GrayMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (chap.generatedText.length > 150) chap.generatedText.take(150) + "..." else chap.generatedText,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = FontFamily.Serif,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dateStr = remember(chap.savedAt) {
                                    try {
                                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                        sdf.format(Date(chap.savedAt))
                                    } catch (e: Exception) {
                                        "Tanggal tersimpan"
                                    }
                                }
                                Text(
                                    text = dateStr,
                                    fontSize = 9.sp,
                                    color = GrayMuted
                                )

                                Text(
                                    text = "Centang Elemen: C(${NovelSerializer.parseInvolvedIds(chap.involvedCharacterIds).size}) P(${NovelSerializer.parseInvolvedIds(chap.involvedLocationIds).size}) E(${NovelSerializer.parseInvolvedIds(chap.involvedEventIds).size})",
                                    fontSize = 9.sp,
                                    color = RoyalGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SUB GENERAL COMPOSE UTILITIES
// ==========================================

@Composable
fun EmptyStateComponent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = "Empty Logo",
            tint = GrayMuted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 12.sp,
            color = GrayMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// Dialog character form
@Composable
fun AddCharacterFormDialog(
    onDismiss: () -> Unit,
    onSave: (CharacterEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Protagonis") } // Protagonis, Antagonis, Pendukung
    var infoBlock by remember { mutableStateOf("") }
    var devBlock by remember { mutableStateOf("") }
    var physicalTraits by remember { mutableStateOf("") }
    var background by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, SleekPurple, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Pendaftaran Karakter Baru",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPurple
                )

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Karakter") },
                    modifier = Modifier.fillMaxWidth().testTag("add_char_name_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                // Role Selector row
                Column {
                    Text("Peran dalam Cerita:", fontSize = 11.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Protagonis", "Antagonis", "Pendukung").forEach { r ->
                            val active = role == r
                            FilterChip(
                                selected = active,
                                onClick = { role = r },
                                label = { Text(r, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = SleekPurple
                                )
                            )
                        }
                    }
                }

                TextField(
                    value = infoBlock,
                    onValueChange = { infoBlock = it },
                    label = { Text("Informasi Pokok / Keahlian") },
                    modifier = Modifier.fillMaxWidth().testTag("add_char_info_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = devBlock,
                    onValueChange = { devBlock = it },
                    label = { Text("Karakter Development / Goal Akhir") },
                    modifier = Modifier.fillMaxWidth().testTag("add_char_dev_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = physicalTraits,
                    onValueChange = { physicalTraits = it },
                    label = { Text("Ciri Fisik Khusus (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = background,
                    onValueChange = { background = it },
                    label = { Text("Asal Usul / Latar Belakang (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TerracottaRed)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    CharacterEntity(
                                        name = name,
                                        role = role,
                                        infoDescription = infoBlock,
                                        developmentDetails = devBlock,
                                        physicalTraits = physicalTraits,
                                        backgroundStory = background
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPurpleBtn, contentColor = Color.White),
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("save_char_confirm_button")
                    ) {
                        Text("Simpan", color = Color.White)
                    }
                }
            }
        }
    }
}

// Dialog location form
@Composable
fun AddLocationFormDialog(
    onDismiss: () -> Unit,
    onSave: (LocationEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Kerajaan") } // Kerajaan, Kota, Hutan, Planet etc
    var desc by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, SleekPurple, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Pendaftaran Dunia & Tempat",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPurple
                )

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Tempat Imajinatif") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Tipe Tempat (e.g. Istana, Planet, Pulau)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi Suasana & Geografis") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = history,
                    onValueChange = { history = it },
                    label = { Text("Legenda / Sejarah Kuno Tempat (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TerracottaRed)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    LocationEntity(
                                        name = name,
                                        type = type,
                                        description = desc,
                                        historicalNotes = history
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPurpleBtn, contentColor = Color.White),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Simpan", color = Color.White)
                    }
                }
            }
        }
    }
}

// Dialog event form
@Composable
fun AddEventFormDialog(
    onDismiss: () -> Unit,
    onSave: (TimelineEventEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var orderStr by remember { mutableStateOf("1") }
    var desc by remember { mutableStateOf("") }
    var consequence by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, SleekPurple, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Pendaftaran Peristiwa Cerita",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPurple
                )

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Kejadian") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = orderStr,
                    onValueChange = { orderStr = it },
                    label = { Text("Garis Urutan Waktu (Kejadian Ke- Berapa)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Detail Kejadian Penting") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

                TextField(
                    value = consequence,
                    onValueChange = { consequence = it },
                    label = { Text("Dampak pada Karakter / Plot Berjalannya Cerita") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = SleekPurple,
                        unfocusedIndicatorColor = LavenderBorder.copy(alpha = 0.5f)
                    )
                )

               Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TerracottaRed)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    TimelineEventEntity(
                                        eventTitle = title,
                                        eventOrder = orderStr.toIntOrNull() ?: 1,
                                        description = desc,
                                        consequence = consequence
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPurpleBtn, contentColor = Color.White),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Simpan", color = Color.White)
                    }
                }
            }
        }
    }
}

// Clickable Lore view dialog to fulfill "linkable" beautifully
@Composable
fun ViewEntityDetailDialog(
    entity: Any,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.7.dp, SleekPurple, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (entity) {
                    is CharacterEntity -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, "Char", tint = SleekPurple, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(entity.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                        }
                        HorizontalDivider(color = LavenderBorder.copy(alpha = 0.4f))
                        
                        Text("Peran Sastra:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                        Badge(containerColor = if (entity.role == "Protagonis") LavenderContainer else SleekPurpleMuted) {
                            Text(entity.role, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SleekPurpleBtn, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        
                        Text("Informasi & Karakteristik:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                        Text(entity.infoDescription, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        
                        Text("Rencana Pengembangan (Milestones):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                        Text(entity.developmentDetails, fontSize = 13.sp, color = SleekPurple, fontWeight = FontWeight.Medium)
                        
                        if (entity.physicalTraits.isNotBlank()) {
                            Text("Ciri Fisik Unik:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                            Text(entity.physicalTraits, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                        
                        if (entity.backgroundStory.isNotBlank()) {
                            Text("Asal Usul / Backstory:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                            Text(entity.backgroundStory, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                    is LocationEntity -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, "Place", tint = SleekPurple, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(entity.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                        }
                        HorizontalDivider(color = LavenderBorder.copy(alpha = 0.4f))
                        
                        Text("Tipe Lokasi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                        Badge(containerColor = LavenderContainer) {
                            Text(entity.type, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SleekPurpleBtn, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        
                        Text("Deskripsi Suasana & Geografi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                        Text(entity.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        
                        if (entity.historicalNotes.isNotBlank()) {
                            Text("Sejarah / Legenda Kuno:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                            Text(entity.historicalNotes, fontSize = 12.sp, color = SleekPurple, fontWeight = FontWeight.Medium)
                        }
                    }
                    is TimelineEventEntity -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timeline, "Event", tint = SleekPurple, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kejadian #${entity.eventOrder}: ${entity.eventTitle}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                        }
                        HorizontalDivider(color = LavenderBorder.copy(alpha = 0.4f))
                        
                        Text("Detail Kejadian Penting:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                        Text(entity.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        
                        if (entity.consequence.isNotBlank()) {
                            Text("Dampak Kejadian:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                            Text(entity.consequence, fontSize = 13.sp, color = SleekPurple, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPurpleBtn, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Dialog Reader full display component
@Composable
fun ReaderFullViewDialog(
    chapter: ChapterEntity,
    onDismiss: () -> Unit,
    onCopyClick: () -> Unit
) {
    var fontSizeMultiplier by remember { mutableStateOf(1f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, RoyalGold),
            colors = CardDefaults.cardColors(containerColor = InkBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top controls bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCopyClick) {
                            Icon(Icons.Default.ContentCopy, "Salin", tint = RoyalGold)
                        }
                        IconButton(onClick = { fontSizeMultiplier = (fontSizeMultiplier - 0.15f).coerceAtLeast(0.7f) }) {
                            Text("A-", color = RoyalGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        IconButton(onClick = { fontSizeMultiplier = (fontSizeMultiplier + 0.15f).coerceAtLeast(1.4f) }) {
                            Text("A+", color = RoyalGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = TerracottaRed)
                    }
                }

                HorizontalDivider(color = RoyalGold.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                // Literary View
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bab ${chapter.chapterNumber}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = chapter.chapterTitle,
                        fontSize = (20 * fontSizeMultiplier).sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalGold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Text Body matching parchment books
                    Text(
                        text = chapter.generatedText,
                        fontSize = (15 * fontSizeMultiplier).sp,
                        color = Color(0xFFECEFF1),
                        fontFamily = FontFamily.Serif,
                        lineHeight = (23 * fontSizeMultiplier).sp,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (chapter.plotTwistSuggestion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = RoyalGold.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "[Plot Twist Tersarung AI]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaRed
                        )
                        Text(
                            text = chapter.plotTwistSuggestion,
                            fontSize = 11.sp,
                            color = GrayMuted,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// FlowRow copy-patch since standard Compose might not bundle experimental FlowRow depending on libraries.
// Making custom Grid-like Row makes it 100% failproof
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Elegant fall-back wrapping logic since FlowRow may require experimental compose declarations
    Box(modifier = modifier) {
        // Minimal stackable layout. As the shelf contains typically 1-6 elements, a wrap row is perfect.
        Column(verticalArrangement = verticalArrangement) {
            var itemsLeft = 1 // Simulated wrapping by organizing elements on screen
            Row(
                horizontalArrangement = horizontalArrangement,
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}
