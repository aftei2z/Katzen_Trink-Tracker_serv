package com.alfred.serv.drinkmonitor

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

@RestController
@RequestMapping("/api/records")
class DrinkRecordController(
    private val repository: DrinkRecordRepository
) {

    // 1. Create a new record
    @PostMapping
    fun createRecord(@RequestBody request: CreateRecordRequest): ResponseEntity<DrinkRecord> {
        val record = DrinkRecord(
            timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            weight = request.weight
        )
        return ResponseEntity.ok(repository.save(record))
    }

    // 2. Read all records
    @GetMapping("/all")
    fun getAllRecords(): ResponseEntity<List<DrinkRecord>> {
        return ResponseEntity.ok(repository.findAll())
    }

    // 3. Read by Date (All records for a specific day)
    @GetMapping("/by-date")
    fun getByDate(
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<DrinkRecord>> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)
        
        // This could be optimized with a custom repository query, but doing it in memory is simple for SQLite
        val records = repository.findAll().filter { 
            it.timestamp >= startOfDay && it.timestamp <= endOfDay 
        }
        return ResponseEntity.ok(records)
    }

    // 4. Read by Hour (All records within a specific hour on a specific date)
    @GetMapping("/by-hour")
    fun getByHour(
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam("hour") hour: Int // 0-23
    ): ResponseEntity<List<DrinkRecord>> {
        if (hour !in 0..23) {
            return ResponseEntity.badRequest().build()
        }
        
        val startOfHour = date.atTime(hour, 0)
        val endOfHour = date.atTime(hour, 59, 59, 999999999)
        
        val records = repository.findAll().filter { 
            it.timestamp >= startOfHour && it.timestamp <= endOfHour 
        }
        return ResponseEntity.ok(records)
    }

    // 5. Read by Week (All records within a specific week of a given year)
    @GetMapping("/by-week")
    fun getByWeek(
        @RequestParam("year") year: Int,
        @RequestParam("week") week: Int
    ): ResponseEntity<List<DrinkRecord>> {
        val weekFields = WeekFields.ISO
        
        val records = repository.findAll().filter {
            val recordWeek = it.timestamp.get(weekFields.weekOfWeekBasedYear())
            val recordYear = it.timestamp.get(weekFields.weekBasedYear())
            
            recordWeek == week && recordYear == year
        }
        return ResponseEntity.ok(records)
    }

    // 6. Delete all records
    @DeleteMapping("/all")
    fun deleteAllRecords(): ResponseEntity<Map<String, String>> {
        val count = repository.count()
        repository.deleteAll()
        return ResponseEntity.ok(mapOf("message" to "Successfully deleted $count records."))
    }
}

data class CreateRecordRequest(
    val weight: Double
)
