package org.caresphere.android


object EmergencyContact {
    const val NAME_MAX = 40
    private const val PHONE_MAX_DIGITS = 24

    fun cleanName(raw: String): String =
        raw.replace(Regex("\\s+"), " ").trim().take(NAME_MAX)

    fun cleanPhone(raw: String): String {
        val text = raw.trim()
        val digits = text.filter { it.isDigit() }.take(PHONE_MAX_DIGITS)
        return if (text.startsWith("+") && digits.isNotEmpty()) "+$digits" else digits
    }

    fun usable(name: String, phone: String): Boolean =
        cleanName(name).isNotEmpty() && cleanPhone(phone).count { it.isDigit() } >= 7

    
    fun smsUri(phone: String): String = "smsto:" + cleanPhone(phone)
}
