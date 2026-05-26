package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jarvis_logs")
data class JarvisLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val reply: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean = false,
    val isVoice: Boolean = false
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val location: String = "Stark Headquarters"
)

@Entity(tableName = "jarvis_tasks")
data class JarvisTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val completed: Boolean = false,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val category: String = "Core", // "Stark Labs", "Avengers", "Personal", "System"
    val dueDate: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
)

@Entity(tableName = "smart_devices")
data class SmartDevice(
    @PrimaryKey val deviceId: String,
    val deviceName: String,
    val deviceType: String, // "LIGHT", "THERMOSTAT", "LOCK", "MEDIA"
    val status: String,    // "ON", "OFF", "LOCKED", "UNLOCKED"
    val value: Double = 0.0, // Thermostat temperature, Light level etc.
    val room: String = "Penthouse"
)

