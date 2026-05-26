package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // ---- Converstation Logs queries ----
    @Query("SELECT * FROM jarvis_logs ORDER BY timestamp DESC LIMIT 100")
    fun getConversationLogsFlow(): Flow<List<JarvisLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: JarvisLog)

    @Query("DELETE FROM jarvis_logs")
    suspend fun clearLogs()

    // ---- Local Calendar queries ----
    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun getAllEventsFlow(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    suspend fun getAllEvents(): List<CalendarEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)

    // ---- Smart Home devices queries ----
    @Query("SELECT * FROM smart_devices")
    fun getAllDevicesFlow(): Flow<List<SmartDevice>>

    @Query("SELECT * FROM smart_devices")
    suspend fun getAllDevices(): List<SmartDevice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: SmartDevice)

    @Query("SELECT * FROM smart_devices WHERE deviceId = :id")
    suspend fun getDeviceById(id: String): SmartDevice?

    // ---- Jarvis Task queries ----
    @Query("SELECT * FROM jarvis_tasks ORDER BY dueDate ASC")
    fun getAllTasksFlow(): Flow<List<JarvisTask>>

    @Query("SELECT * FROM jarvis_tasks ORDER BY dueDate ASC")
    suspend fun getAllTasks(): List<JarvisTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: JarvisTask)

    @Update
    suspend fun updateTask(task: JarvisTask)

    @Query("DELETE FROM jarvis_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)
}
