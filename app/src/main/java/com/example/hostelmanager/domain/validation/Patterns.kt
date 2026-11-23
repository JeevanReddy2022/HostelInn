package com.example.hostelmanager.domain.validation

object Patterns {
    val Phone = Regex("^[6-9]\\d{9}$")
    val Aadhaar = Regex("^[2-9][0-9]{11}$")
    val VoterId = Regex("^[A-Z]{3}[0-9]{7}$")
    val DrivingLic = Regex("^(([A-Z]{2}[0-9]{2})( )?|([A-Z]{2}-[0-9]{2}))((19|20)[0-9]{2})[0-9]{7}$")
    val IsoDate = Regex("^\\d{4}-\\d{2}-\\d{2}$")
}
