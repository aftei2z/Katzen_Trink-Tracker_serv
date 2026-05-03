package com.alfred.serv.drinkmonitor

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service

@Service
interface DrinkRecordRepository : JpaRepository<DrinkRecord, Long>
