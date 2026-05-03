package com.alfred.serv.drinkmonitor

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.WeekFields

@SpringBootTest
@AutoConfigureMockMvc
class DrinkRecordControllerTest {

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repository: DrinkRecordRepository

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    lateinit var objectMapper: ObjectMapper

    @Value("\${app.api.key}")
    lateinit var apiKey: String

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    fun `test create record without api key fails`() {
        val request = CreateRecordRequest(weight = 5.0)

        mockMvc.perform(
            post("/api/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `test create record with valid api key succeeds`() {
        val request = CreateRecordRequest(weight = 5.0)

        mockMvc.perform(
            post("/api/records")
                .header("X-API-KEY", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.weight").value(5.0))

        assertEquals(1, repository.count())
    }

    @Test
    fun `test get all records`() {
        // Save dummy data
        repository.save(DrinkRecord(weight = 1.0))
        repository.save(DrinkRecord(weight = 2.0))
        repository.save(DrinkRecord(weight = 3.0))
        
        mockMvc.perform(
            get("/api/records/all")
                .header("X-API-KEY", apiKey)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    fun `test get by date`() {
        val today = LocalDate.now()
        
        // Record for today
        repository.save(DrinkRecord(weight = 1.0, timestamp = today.atTime(10, 0)))
        repository.save(DrinkRecord(weight = 2.0, timestamp = today.atTime(15, 0)))
        
        // Record for yesterday
        repository.save(DrinkRecord(weight = 3.0, timestamp = today.minusDays(1).atTime(10, 0)))

        mockMvc.perform(
            get("/api/records/by-date")
                .param("date", today.toString())
                .param("key", apiKey) // Testing api key via param
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `test get by hour`() {
        val today = LocalDate.now()
        
        // Records at 14:00 (2 PM)
        repository.save(DrinkRecord(weight = 1.0, timestamp = today.atTime(14, 15)))
        repository.save(DrinkRecord(weight = 2.0, timestamp = today.atTime(14, 45)))
        
        // Record at 15:00 (3 PM)
        repository.save(DrinkRecord(weight = 3.0, timestamp = today.atTime(15, 10)))

        mockMvc.perform(
            get("/api/records/by-hour")
                .header("X-API-KEY", apiKey)
                .param("date", today.toString())
                .param("hour", "14")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }
    
    @Test
    fun `test get by hour with invalid hour returns bad request`() {
        val today = LocalDate.now()
        
        mockMvc.perform(
            get("/api/records/by-hour")
                .header("X-API-KEY", apiKey)
                .param("date", today.toString())
                .param("hour", "25")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test get by week`() {
        val now = LocalDateTime.now()
        val weekFields = WeekFields.ISO
        val currentWeek = now.get(weekFields.weekOfWeekBasedYear())
        val currentYear = now.get(weekFields.weekBasedYear())
        
        // Records for current week
        repository.save(DrinkRecord(weight = 1.0, timestamp = now))
        repository.save(DrinkRecord(weight = 2.0, timestamp = now.plusDays(1)))
        
        // Record for a totally different week (subtracting 2 weeks)
        repository.save(DrinkRecord(weight = 3.0, timestamp = now.minusWeeks(2)))

        mockMvc.perform(
            get("/api/records/by-week")
                .header("X-API-KEY", apiKey)
                .param("year", currentYear.toString())
                .param("week", currentWeek.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2)) // Should find 2 records in the current week
    }

    @Test
    fun `test delete all records`() {
        repository.save(DrinkRecord(weight = 1.0))
        repository.save(DrinkRecord(weight = 2.0))
        repository.save(DrinkRecord(weight = 3.0))

        assertEquals(3, repository.count())

        mockMvc.perform(
            delete("/api/records/all")
                .header("X-API-KEY", apiKey)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Successfully deleted 3 records."))

        assertEquals(0, repository.count())
    }
}
