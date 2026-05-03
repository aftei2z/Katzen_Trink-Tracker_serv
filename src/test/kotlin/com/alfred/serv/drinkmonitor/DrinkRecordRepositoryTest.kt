package com.alfred.serv.drinkmonitor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DrinkRecordRepositoryTest {

    @Autowired
    lateinit var repository: DrinkRecordRepository

    @Test
    fun `test create, update, and delete DrinkRecord`() {
        // 1. Create
        val initialTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val initialWeight = 5.2
        
        val record = DrinkRecord(
            timestamp = initialTime,
            weight = initialWeight
        )
        
        val savedRecord = repository.save(record)
        
        assertNotNull(savedRecord.id)
        assertEquals(initialWeight, savedRecord.weight)
        assertEquals(initialTime, savedRecord.timestamp.truncatedTo(ChronoUnit.MILLIS))
        
        // 2. Read (Verify creation)
        val foundRecord = repository.findById(savedRecord.id!!).orElse(null)
        assertNotNull(foundRecord)
        assertEquals(initialWeight, foundRecord?.weight)

        // 3. Update
        val updatedWeight = 6.0
        foundRecord!!.weight = updatedWeight
        val updatedRecord = repository.save(foundRecord)
        
        assertEquals(updatedWeight, updatedRecord.weight)
        
        // Verify update in DB
        val reFetchedRecord = repository.findById(savedRecord.id!!).orElse(null)
        assertNotNull(reFetchedRecord)
        assertEquals(updatedWeight, reFetchedRecord?.weight)

        // 4. Delete
        repository.delete(reFetchedRecord!!)
        
        // Verify deletion
        val deletedRecord = repository.findById(savedRecord.id!!)
        assertTrue(deletedRecord.isEmpty)
    }
}
