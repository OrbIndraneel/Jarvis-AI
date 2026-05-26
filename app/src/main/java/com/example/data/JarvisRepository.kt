package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class JarvisRepository(private val dao: JarvisDao) {

    val logsFlow: Flow<List<JarvisLog>> = dao.getConversationLogsFlow()
    val eventsFlow: Flow<List<CalendarEvent>> = dao.getAllEventsFlow()
    val devicesFlow: Flow<List<SmartDevice>> = dao.getAllDevicesFlow()
    val tasksFlow: Flow<List<JarvisTask>> = dao.getAllTasksFlow()

    suspend fun insertLog(log: JarvisLog) {
        dao.insertLog(log)
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    suspend fun insertEvent(event: CalendarEvent) {
        dao.insertEvent(event)
    }

    suspend fun deleteEvent(eventId: Long) {
        dao.deleteEvent(eventId)
    }

    suspend fun insertTask(task: JarvisTask) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: JarvisTask) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTask(taskId)
    }

    suspend fun updateDevice(device: SmartDevice) {
        dao.insertOrUpdateDevice(device)
    }

    suspend fun toggleDevice(deviceId: String): SmartDevice? {
        val device = dao.getDeviceById(deviceId) ?: return null
        val updated = when (device.deviceType) {
            "LIGHT" -> {
                val nextStatus = if (device.status == "ON") "OFF" else "ON"
                device.copy(status = nextStatus)
            }
            "LOCK" -> {
                val nextStatus = if (device.status == "LOCKED") "UNLOCKED" else "LOCKED"
                device.copy(status = nextStatus)
            }
            "MEDIA" -> {
                val nextStatus = if (device.status == "ON") "OFF" else "ON"
                device.copy(status = nextStatus)
            }
            "THERMOSTAT" -> {
                // cycle or toggle heating or cooling or on/off
                val nextStatus = if (device.status == "ON") "OFF" else "ON"
                device.copy(status = nextStatus)
            }
            else -> device
        }
        dao.insertOrUpdateDevice(updated)
        return updated
    }

    // Prepopulate default items if empty
    suspend fun prepopulateIfNeeded() {
        // Pre-propulate Smart Home Devices
        val currentDevices = dao.getAllDevices()
        if (currentDevices.isEmpty()) {
            val defaults = listOf(
                SmartDevice("light_living", "Living Room Chandelier", "LIGHT", "OFF", 80.0, "Living Room"),
                SmartDevice("thermostat_main", "A.C. Climate Control", "THERMOSTAT", "ON", 71.5, "Hallway"),
                SmartDevice("lock_front", "Penthouse Security Lock", "LOCK", "LOCKED", 0.0, "Entrance"),
                SmartDevice("media_theater", "Matrix Media Display", "MEDIA", "OFF", 0.0, "Media Room")
            )
            defaults.forEach { dao.insertOrUpdateDevice(it) }
        }

        // Pre-populate Events
        val currentEvents = dao.getAllEvents()
        if (currentEvents.isEmpty()) {
            val cal = Calendar.getInstance()
            
            // Event 1: Stark Board Meeting (In 2 hours)
            cal.add(Calendar.HOUR, 2)
            val event1Start = cal.timeInMillis
            cal.add(Calendar.HOUR, 1)
            val event1End = cal.timeInMillis
            
            // Event 2: Tech Briefing (Tomorrow at 10 AM)
            val cal2 = Calendar.getInstance()
            cal2.add(Calendar.DAY_OF_YEAR, 1)
            cal2.set(Calendar.HOUR_OF_DAY, 10)
            cal2.set(Calendar.MINUTE, 0)
            val event2Start = cal2.timeInMillis
            cal2.add(Calendar.HOUR, 1)
            val event2End = cal2.timeInMillis

            // Event 3: Daily Sync
            val cal3 = Calendar.getInstance()
            cal3.add(Calendar.MINUTE, 15) // In 15 minutes!
            val event3Start = cal3.timeInMillis
            cal3.add(Calendar.MINUTE, 30)
            val event3End = cal3.timeInMillis

            dao.insertEvent(CalendarEvent(
                title = "Jarvis Deep Diagnostics",
                description = "Running complete core diagnostic and firmware alignment with Stark mainframe.",
                startTime = event3Start,
                endTime = event3End,
                location = "Stark Penthouse Server"
            ))

            dao.insertEvent(CalendarEvent(
                title = "Board of Directors Briefing",
                description = "Review the energy outputs of the second-gen Arc Reactor modules.",
                startTime = event1Start,
                endTime = event1End,
                location = "Stark Headquarters Hall A"
            ))

            dao.insertEvent(CalendarEvent(
                title = "Avenger Protocol Sync",
                description = "Discuss security shield calibration and threat assessment telemetry indexes.",
                startTime = event2Start,
                endTime = event2End,
                location = "S.H.I.E.L.D. Helicarrier Complex"
            ))
        }

        // Pre-populate Tasks
        val currentTasks = dao.getAllTasks()
        if (currentTasks.isEmpty()) {
            val defaults = listOf(
                JarvisTask(
                    title = "Primary Arc Reactor Thermography",
                    completed = false,
                    priority = "HIGH",
                    category = "Stark Labs",
                    dueDate = System.currentTimeMillis() + (4 * 60 * 60 * 1000)
                ),
                JarvisTask(
                    title = "Mark LXXXV Chassis Resonance Check",
                    completed = false,
                    priority = "MEDIUM",
                    category = "Defense",
                    dueDate = System.currentTimeMillis() + (12 * 60 * 60 * 1000)
                ),
                JarvisTask(
                    title = "Integrate Flight Telemetry Decoders",
                    completed = true,
                    priority = "LOW",
                    category = "System",
                    dueDate = System.currentTimeMillis() - (8 * 60 * 60 * 1000)
                )
            )
            defaults.forEach { dao.insertTask(it) }
        }
    }
}
