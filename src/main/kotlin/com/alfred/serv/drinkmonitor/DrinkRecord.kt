package com.alfred.serv.drinkmonitor

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class DrinkRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    var timestamp: LocalDateTime = LocalDateTime.now(),
    
    var weight: Double = 0.0
)
