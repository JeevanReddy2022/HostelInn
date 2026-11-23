package com.example.hostelmanager.domain.validation

enum class IdType(val label: String, val pattern: Regex) {
    AADHAAR("Aadhaar", Patterns.Aadhaar),
    VOTER_ID("Voter ID", Patterns.VoterId),
    DRIVING_LICENSE("Driving License", Patterns.DrivingLic);

    companion object {
        fun detect(value: String): IdType? {
            val clean = value.trim()
            return entries.firstOrNull { it.pattern.matches(clean) }
        }

        fun validateGovtId(value: String): String? {
            val clean = value.trim()
            if (clean.isEmpty()) return "Govt ID number is required."
            if (detect(clean) == null) {
                return "Enter a valid Aadhaar (12 digits), Voter ID (ABC1234567), or DL."
            }
            return null
        }
    }
}
