package l192.aakarsh.pocketops.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserStore(private val context: Context) {

    companion object {
        val UPI_ID_KEY = stringPreferencesKey("upi_id") // Legacy
        val UPI_IDS_KEY = stringPreferencesKey("upi_ids") // New: Comma separated list
        val DEFAULT_UPI_ID_KEY = stringPreferencesKey("default_upi_id") // New default selection
        val PAYEE_NAME_KEY = stringPreferencesKey("payee_name")
        val RECENT_AMOUNTS_KEY = stringPreferencesKey("recent_amounts")
        val SHOW_UPI_ID_KEY = booleanPreferencesKey("show_upi_id")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        
        // PayPal Additions
        val PAYPAL_IDS_KEY = stringPreferencesKey("paypal_ids")
        val DEFAULT_PAYPAL_ID_KEY = stringPreferencesKey("default_paypal_id")
        val USE_PAYPAL_KEY = booleanPreferencesKey("use_paypal")

        // Quick Chat Additions
        val CHAT_DEFAULT_CODE_KEY = stringPreferencesKey("chat_default_code")
        val CHAT_DEFAULT_ISO_KEY = stringPreferencesKey("chat_default_iso")
        val CHAT_HISTORY_KEY = stringPreferencesKey("chat_history")
        val CHAT_PAUSE_HISTORY_KEY = booleanPreferencesKey("chat_pause_history")

        // Quick Clip Additions
        val CLIPBOARD_PAUSE_KEY = booleanPreferencesKey("clipboard_pause")
        val QUICK_LINKS_KEY = stringPreferencesKey("quick_links")
    }

    val upiIds: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val rawIds = preferences[UPI_IDS_KEY]
        if (!rawIds.isNullOrBlank()) {
            rawIds.split(",").filter { it.isNotBlank() }
        } else {
            val legacyId = preferences[UPI_ID_KEY]
            if (!legacyId.isNullOrBlank()) listOf(legacyId) else emptyList()
        }
    }

    val defaultUpiId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_UPI_ID_KEY]
    }

    val upiId: Flow<String?> = upiIds.map { it.firstOrNull() }

    val payeeName: Flow<String?> =
        context.dataStore.data.map { preferences -> preferences[PAYEE_NAME_KEY] }

    val recentAmounts: Flow<List<String>> =
        context.dataStore.data.map { preferences ->
            val serialized = preferences[RECENT_AMOUNTS_KEY] ?: "100,200,500"
            serialized.split(",").filter { it.isNotBlank() }
        }

    val showUpiId: Flow<Boolean> =
        context.dataStore.data.map { preferences -> preferences[SHOW_UPI_ID_KEY] ?: true }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "SYSTEM"
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: false
    }

    // PayPal flow readers
    val paypalIds: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val rawIds = preferences[PAYPAL_IDS_KEY]
        if (!rawIds.isNullOrBlank()) {
            rawIds.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    val defaultPaypalId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_PAYPAL_ID_KEY]
    }

    val usePaypal: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_PAYPAL_KEY] ?: false
    }

    suspend fun saveUpiIds(ids: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[UPI_IDS_KEY] = ids.joinToString(",")
            if (ids.isNotEmpty()) {
                preferences[UPI_ID_KEY] = ids.first()
                val currentDefault = preferences[DEFAULT_UPI_ID_KEY]
                if (currentDefault == null || !ids.contains(currentDefault)) {
                    preferences[DEFAULT_UPI_ID_KEY] = ids.first()
                }
            } else {
                preferences.remove(UPI_ID_KEY)
                preferences.remove(DEFAULT_UPI_ID_KEY)
            }
        }
    }

    suspend fun saveDefaultUpiId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_UPI_ID_KEY] = id
        }
    }

    suspend fun saveUpiId(id: String) {
        saveUpiIds(listOf(id))
    }

    suspend fun savePayeeName(name: String) {
        context.dataStore.edit { preferences -> preferences[PAYEE_NAME_KEY] = name }
    }

    suspend fun saveShowUpiId(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_UPI_ID_KEY] = show }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    // PayPal save functions
    suspend fun savePaypalIds(ids: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PAYPAL_IDS_KEY] = ids.joinToString(",")
            if (ids.isNotEmpty()) {
                val currentDefault = preferences[DEFAULT_PAYPAL_ID_KEY]
                if (currentDefault == null || !ids.contains(currentDefault)) {
                    preferences[DEFAULT_PAYPAL_ID_KEY] = ids.first()
                }
            } else {
                preferences.remove(DEFAULT_PAYPAL_ID_KEY)
            }
        }
    }

    suspend fun saveDefaultPaypalId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_PAYPAL_ID_KEY] = id
        }
    }

    suspend fun saveUsePaypal(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_PAYPAL_KEY] = use
        }
    }

    suspend fun saveRecentAmount(amount: String) {
        if (amount.isBlank()) return
        context.dataStore.edit { preferences ->
            val currentList =
                (preferences[RECENT_AMOUNTS_KEY] ?: "100,200,500")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .toMutableList()
            currentList.remove(amount)
            currentList.add(0, amount)
            val newList = currentList.take(3)
            preferences[RECENT_AMOUNTS_KEY] = newList.joinToString(",")
        }
    }

    // Quick Chat flows & methods
    val chatDefaultCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CHAT_DEFAULT_CODE_KEY] ?: "91"
    }

    val chatDefaultIso: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CHAT_DEFAULT_ISO_KEY] ?: "IN"
    }

    private val quickChatDir = java.io.File(context.filesDir, "QuickChat")
    private val quickChatFile = java.io.File(quickChatDir, "history.txt")

    private val _chatHistoryFlow = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(emptyList())
    val chatHistory: Flow<List<String>> = _chatHistoryFlow

    private val clipboardDir = java.io.File(context.filesDir, "Clipboard")
    private val clipboardMetadataFile = java.io.File(clipboardDir, "metadata.json")

    data class ClipItem(
        val id: String,
        val type: String, // "text" or "image"
        val content: String, // text content or filename
        val timestamp: Long
    )

    data class LinkItem(
        val id: String,
        val title: String,
        val url: String,
        val iconUrl: String? = null,
        val timestamp: Long
    )

    private val _clipItemsFlow = kotlinx.coroutines.flow.MutableStateFlow<List<ClipItem>>(emptyList())
    val clipItems: Flow<List<ClipItem>> = _clipItemsFlow

    private val _quickLinksFlow = kotlinx.coroutines.flow.MutableStateFlow<List<LinkItem>>(emptyList())
    val quickLinks: Flow<List<LinkItem>> = _quickLinksFlow

    init {
        loadChatHistoryFromFile()
        loadClipboardItems()
        loadQuickLinks()

        // Migrate from dataStore if needed
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val legacy = context.dataStore.data.first()[CHAT_HISTORY_KEY]
                if (!legacy.isNullOrBlank() && !quickChatFile.exists()) {
                    if (!quickChatDir.exists()) quickChatDir.mkdirs()
                    quickChatFile.writeText(legacy)
                    val list = legacy.split(";").filter { it.isNotBlank() }
                    _chatHistoryFlow.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadChatHistoryFromFile() {
        try {
            if (!quickChatDir.exists()) {
                quickChatDir.mkdirs()
            }
            if (quickChatFile.exists()) {
                val lines = quickChatFile.readText()
                val list = lines.split(";").filter { it.isNotBlank() }
                _chatHistoryFlow.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val chatPauseHistory: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CHAT_PAUSE_HISTORY_KEY] ?: false
    }

    val clipboardPause: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLIPBOARD_PAUSE_KEY] ?: false
    }

    suspend fun saveChatDefaultCountry(code: String, iso: String) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_DEFAULT_CODE_KEY] = code
            preferences[CHAT_DEFAULT_ISO_KEY] = iso
        }
    }

    suspend fun saveChatNumberToHistory(number: String, flag: String) {
        val paused = context.dataStore.data.first()[CHAT_PAUSE_HISTORY_KEY] ?: false
        if (!paused) {
            val current = _chatHistoryFlow.value.toMutableList()
            val entry = "$number:$flag"
            current.remove(entry)
            current.add(0, entry)
            val updated = current.take(20)
            _chatHistoryFlow.value = updated

            try {
                if (!quickChatDir.exists()) quickChatDir.mkdirs()
                quickChatFile.writeText(updated.joinToString(";"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun clearChatHistory() {
        _chatHistoryFlow.value = emptyList()
        try {
            if (quickChatFile.exists()) {
                quickChatFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveChatPauseHistory(pause: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_PAUSE_HISTORY_KEY] = pause
        }
    }

    suspend fun saveClipboardPause(pause: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLIPBOARD_PAUSE_KEY] = pause
        }
    }

    suspend fun saveDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    fun loadClipboardItems() {
        try {
            if (!clipboardDir.exists()) {
                clipboardDir.mkdirs()
            }
            if (clipboardMetadataFile.exists()) {
                val jsonStr = clipboardMetadataFile.readText()
                val jsonArray = org.json.JSONArray(jsonStr)
                val list = mutableListOf<ClipItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        ClipItem(
                            id = obj.getString("id"),
                            type = obj.getString("type"),
                            content = obj.getString("content"),
                            timestamp = obj.getLong("timestamp")
                        )
                    )
                }
                _clipItemsFlow.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveClipboardItems(list: List<ClipItem>) {
        _clipItemsFlow.value = list
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            if (!clipboardDir.exists()) {
                clipboardDir.mkdirs()
            }
            val jsonArray = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject()
                obj.put("id", item.id)
                obj.put("type", item.type)
                obj.put("content", item.content)
                obj.put("timestamp", item.timestamp)
                jsonArray.put(obj)
            }
            clipboardMetadataFile.writeText(jsonArray.toString(4))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        }
    }

    fun addTextToClipboardHistory(text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return
        val currentList = _clipItemsFlow.value.toMutableList()

        val existing = currentList.find { it.type == "text" && it.content == cleanText }
        if (existing != null) {
            currentList.remove(existing)
            currentList.add(0, existing.copy(timestamp = System.currentTimeMillis()))
        } else {
            currentList.add(
                0,
                ClipItem(
                    id = System.currentTimeMillis().toString(),
                    type = "text",
                    content = cleanText,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        saveClipboardItems(currentList)
    }

    fun addImageToClipboardHistory(uri: android.net.Uri) {
        try {
            val contentResolver = context.contentResolver
            val id = System.currentTimeMillis().toString()
            val filename = "img_$id.png"
            val destFile = java.io.File(clipboardDir, filename)

            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val currentList = _clipItemsFlow.value.toMutableList()
            currentList.add(
                0,
                ClipItem(
                    id = id,
                    type = "image",
                    content = filename,
                    timestamp = System.currentTimeMillis()
                )
            )
            saveClipboardItems(currentList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addImageStreamToClipboardHistory(inputStream: java.io.InputStream) {
        val id = System.currentTimeMillis().toString()
        val filename = "img_$id.png"
        val newItem = ClipItem(id = id, type = "image", content = filename, timestamp = System.currentTimeMillis())
        val currentList = _clipItemsFlow.value.toMutableList()
        currentList.add(0, newItem)
        saveClipboardItems(currentList)

        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (!clipboardDir.exists()) clipboardDir.mkdirs()
                val destFile = java.io.File(clipboardDir, filename)
                inputStream.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                if (_clipItemsFlow.value.none { it.id == id }) {
                    destFile.delete()
                } else {
                    _clipItemsFlow.value = _clipItemsFlow.value.toList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun deleteClipItem(item: ClipItem) {
        val currentList = _clipItemsFlow.value.toMutableList()
        currentList.remove(item)
        if (item.type == "image") {
            try {
                val file = java.io.File(clipboardDir, item.content)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        saveClipboardItems(currentList)
    }

    fun clearAllClipItems() {
        val currentList = _clipItemsFlow.value.toList()
        for (item in currentList) {
            if (item.type == "image") {
                try {
                    val file = java.io.File(clipboardDir, item.content)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        saveClipboardItems(emptyList())
        try {
            if (clipboardMetadataFile.exists()) {
                clipboardMetadataFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyImageToClipboard(filename: String) {
        try {
            val file = java.io.File(clipboardDir, filename)
            if (file.exists()) {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData(
                    "Image",
                    arrayOf("image/png"),
                    android.content.ClipData.Item(uri)
                )
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(context, "Image copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun loadQuickLinks() {
        try {
            val raw = kotlinx.coroutines.runBlocking { context.dataStore.data.first()[QUICK_LINKS_KEY] } ?: return
            val jsonArray = org.json.JSONArray(raw)
            val list = mutableListOf<LinkItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    LinkItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        url = obj.getString("url"),
                        iconUrl = obj.optString("iconUrl").takeIf { it.isNotBlank() },
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
            _quickLinksFlow.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveQuickLinks(list: List<LinkItem>) {
        _quickLinksFlow.value = list
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val jsonArray = org.json.JSONArray()
                for (item in list) {
                    val obj = org.json.JSONObject()
                    obj.put("id", item.id)
                    obj.put("title", item.title)
                    obj.put("url", item.url)
                    obj.put("iconUrl", item.iconUrl ?: "")
                    obj.put("timestamp", item.timestamp)
                    jsonArray.put(obj)
                }
                context.dataStore.edit { preferences ->
                    preferences[QUICK_LINKS_KEY] = jsonArray.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveQuickLink(title: String, url: String, iconUrl: String?) {
        val cleanUrl = url.trim()
        if (cleanUrl.isEmpty()) return
        val cleanTitle = title.trim().ifEmpty { cleanUrl }
        val current = _quickLinksFlow.value.toMutableList()
        current.removeAll { it.url == cleanUrl }
        current.add(0, LinkItem(System.currentTimeMillis().toString(), cleanTitle, cleanUrl, iconUrl, System.currentTimeMillis()))
        saveQuickLinks(current)
    }

    fun deleteQuickLink(item: LinkItem) {
        val current = _quickLinksFlow.value.toMutableList()
        current.remove(item)
        saveQuickLinks(current)
    }

    suspend fun exportToJson(): String {
        val json = org.json.JSONObject()
        json.put("magic", "PocketOps by Aakarsh(IIXII-L192)")

        val prefs = context.dataStore.data.first()

        prefs[UPI_IDS_KEY]?.let { json.put("upi_ids", it) }
        prefs[UPI_ID_KEY]?.let { json.put("upi_id", it) }
        prefs[DEFAULT_UPI_ID_KEY]?.let { json.put("default_upi_id", it) }
        prefs[PAYEE_NAME_KEY]?.let { json.put("payee_name", it) }
        prefs[RECENT_AMOUNTS_KEY]?.let { json.put("recent_amounts", it) }
        prefs[SHOW_UPI_ID_KEY]?.let { json.put("show_upi_id", it) }
        prefs[THEME_MODE_KEY]?.let { json.put("theme_mode", it) }
        prefs[DYNAMIC_COLOR_KEY]?.let { json.put("dynamic_color", it) }
        prefs[PAYPAL_IDS_KEY]?.let { json.put("paypal_ids", it) }
        prefs[DEFAULT_PAYPAL_ID_KEY]?.let { json.put("default_paypal_id", it) }
        prefs[USE_PAYPAL_KEY]?.let { json.put("use_paypal", it) }
        prefs[CHAT_DEFAULT_CODE_KEY]?.let { json.put("chat_default_code", it) }
        prefs[CHAT_DEFAULT_ISO_KEY]?.let { json.put("chat_default_iso", it) }
        prefs[CHAT_PAUSE_HISTORY_KEY]?.let { json.put("chat_pause_history", it) }
        prefs[CLIPBOARD_PAUSE_KEY]?.let { json.put("clipboard_pause", it) }
        prefs[QUICK_LINKS_KEY]?.let { json.put("quick_links", it) }

        return json.toString(4)
    }

    suspend fun importFromJson(jsonString: String): Boolean {
        try {
            val json = org.json.JSONObject(jsonString)
            if (json.optString("magic") != "PocketOps by Aakarsh(IIXII-L192)") {
                return false
            }
            context.dataStore.edit { preferences ->
                if (json.has("upi_ids")) preferences[UPI_IDS_KEY] = json.getString("upi_ids")
                if (json.has("upi_id")) preferences[UPI_ID_KEY] = json.getString("upi_id")
                if (json.has("default_upi_id")) preferences[DEFAULT_UPI_ID_KEY] = json.getString("default_upi_id")
                if (json.has("payee_name")) preferences[PAYEE_NAME_KEY] = json.getString("payee_name")
                if (json.has("recent_amounts")) preferences[RECENT_AMOUNTS_KEY] = json.getString("recent_amounts")
                if (json.has("show_upi_id")) preferences[SHOW_UPI_ID_KEY] = json.getBoolean("show_upi_id")
                if (json.has("theme_mode")) preferences[THEME_MODE_KEY] = json.getString("theme_mode")
                if (json.has("dynamic_color")) preferences[DYNAMIC_COLOR_KEY] = json.getBoolean("dynamic_color")
                if (json.has("paypal_ids")) preferences[PAYPAL_IDS_KEY] = json.getString("paypal_ids")
                if (json.has("default_paypal_id")) preferences[DEFAULT_PAYPAL_ID_KEY] = json.getString("default_paypal_id")
                if (json.has("use_paypal")) preferences[USE_PAYPAL_KEY] = json.getBoolean("use_paypal")
                if (json.has("chat_default_code")) preferences[CHAT_DEFAULT_CODE_KEY] = json.getString("chat_default_code")
                if (json.has("chat_default_iso")) preferences[CHAT_DEFAULT_ISO_KEY] = json.getString("chat_default_iso")
                if (json.has("chat_pause_history")) preferences[CHAT_PAUSE_HISTORY_KEY] = json.getBoolean("chat_pause_history")
                if (json.has("clipboard_pause")) preferences[CLIPBOARD_PAUSE_KEY] = json.getBoolean("clipboard_pause")
                if (json.has("quick_links")) preferences[QUICK_LINKS_KEY] = json.getString("quick_links")
            }
            // Reload clipboard from disk if imported
            loadClipboardItems()
        loadQuickLinks()
            loadChatHistoryFromFile()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}






