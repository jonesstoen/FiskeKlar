package no.uio.ifi.in2000.team46.data.repository

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.uio.ifi.in2000.team46.domain.model.fishlog.FishingData
import java.time.LocalDate
import java.time.LocalTime

class FishLogRepository(private val context: Context) {
    private val _entries = MutableStateFlow<List<FishingData>>(emptyList())
    private val entries: StateFlow<List<FishingData>> = _entries.asStateFlow()
    private val sharedPreferences = context.getSharedPreferences("fishing_log", Context.MODE_PRIVATE)
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, object : TypeAdapter<LocalDate>() {
            override fun write(out: JsonWriter, value: LocalDate?) {
                if (value == null) {
                    out.nullValue()
                } else {
                    out.value(value.toString())
                }
            }

            override fun read(input: JsonReader): LocalDate? {
                val dateStr = input.nextString()
                return LocalDate.parse(dateStr)
            }
        })
        .registerTypeAdapter(LocalTime::class.java, object : TypeAdapter<LocalTime>() {
            override fun write(out: JsonWriter, value: LocalTime?) {
                if (value == null) {
                    out.nullValue()
                } else {
                    out.value(value.toString())
                }
            }

            override fun read(input: JsonReader): LocalTime? {
                val timeStr = input.nextString()
                return LocalTime.parse(timeStr)
            }
        })
        .create()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        val json = sharedPreferences.getString("entries", "[]")
        val type = object : TypeToken<List<FishingData>>() {}.type
        val loadedEntries = gson.fromJson<List<FishingData>>(json, type) ?: emptyList()
        _entries.value = loadedEntries
    }

    private fun saveEntries() {
        val json = gson.toJson(_entries.value)
        sharedPreferences.edit().putString("entries", json).apply()
    }

    fun getAllEntries(): StateFlow<List<FishingData>> {
        return entries
    }

    fun addEntry(entry: FishingData) {
        _entries.value += entry
        saveEntries()
    }

    fun removeEntry(id: Long) {
        _entries.value = _entries.value.filter { it.id != id }
        saveEntries()
    }
}