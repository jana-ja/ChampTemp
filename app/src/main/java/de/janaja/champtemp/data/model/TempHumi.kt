package de.janaja.champtemp.data.model

import java.time.LocalDateTime


class TempHumi(
    val id: String,
    val temp: Int,
    val humi: Int,
    val timestamp: LocalDateTime
)