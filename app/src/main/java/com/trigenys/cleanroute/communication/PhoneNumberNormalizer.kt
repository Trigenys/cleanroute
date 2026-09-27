package com.trigenys.cleanroute.communication

data class NormalizedPhoneNumber(
    val e164: String,
    val internationalDigits: String
)

class PhoneNumberNormalizer(
    defaultCountryCallingCode: String
) {
    private val countryCode = defaultCountryCallingCode.filter(Char::isDigit).also {
        require(it.isNotBlank()) { "Default country calling code must contain digits" }
    }

    fun normalize(raw: String): NormalizedPhoneNumber? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        val explicitInternational = trimmed.startsWith("+") || trimmed.startsWith("00")
        var digits = trimmed.filter(Char::isDigit)
        if (digits.isEmpty()) return null

        if (trimmed.startsWith("00")) {
            digits = digits.removePrefix("00")
        }

        val internationalDigits = when {
            explicitInternational -> digits
            digits.startsWith(countryCode) && digits.length > countryCode.length + 6 -> digits
            else -> countryCode + digits.removePrefix("0")
        }

        if (internationalDigits.length !in 8..15) return null

        return NormalizedPhoneNumber(
            e164 = "+$internationalDigits",
            internationalDigits = internationalDigits
        )
    }
}
