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

    val chatHistory: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[CHAT_HISTORY_KEY]
        if (!raw.isNullOrBlank()) {
            raw.split(";").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    val chatPauseHistory: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CHAT_PAUSE_HISTORY_KEY] ?: false
    }

    suspend fun saveChatDefaultCountry(code: String, iso: String) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_DEFAULT_CODE_KEY] = code
            preferences[CHAT_DEFAULT_ISO_KEY] = iso
        }
    }

    suspend fun saveChatNumberToHistory(number: String, flag: String) {
        context.dataStore.edit { preferences ->
            val paused = preferences[CHAT_PAUSE_HISTORY_KEY] ?: false
            if (!paused) {
                val current = (preferences[CHAT_HISTORY_KEY] ?: "")
                    .split(";")
                    .filter { it.isNotBlank() }
                    .toMutableList()
                val entry = "$number:$flag"
                current.remove(entry)
                current.add(0, entry)
                preferences[CHAT_HISTORY_KEY] = current.take(20).joinToString(";")
            }
        }
    }

    suspend fun clearChatHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(CHAT_HISTORY_KEY)
        }
    }

    suspend fun saveChatPauseHistory(pause: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_PAUSE_HISTORY_KEY] = pause
        }
    }

    suspend fun saveDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
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
        prefs[CHAT_HISTORY_KEY]?.let { json.put("chat_history", it) }
        prefs[CHAT_PAUSE_HISTORY_KEY]?.let { json.put("chat_pause_history", it) }
        
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
                if (json.has("chat_history")) preferences[CHAT_HISTORY_KEY] = json.getString("chat_history")
                if (json.has("chat_pause_history")) preferences[CHAT_PAUSE_HISTORY_KEY] = json.getBoolean("chat_pause_history")
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
