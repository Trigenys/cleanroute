package com.trigenys.cleanroute.communication

import android.content.Intent
import android.net.Uri

class ContactIntentFactory(
    private val phoneNumberNormalizer: PhoneNumberNormalizer
) {
    fun dial(rawPhone: String): Intent? {
        val phone = phoneNumberNormalizer.normalize(rawPhone) ?: return null
        return Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", phone.e164, null)
        )
    }

    fun whatsApp(
        rawPhone: String,
        message: String?,
        packageName: String
    ): Intent? {
        val phone = phoneNumberNormalizer.normalize(rawPhone) ?: return null
        val uriBuilder = Uri.Builder()
            .scheme("https")
            .authority("wa.me")
            .appendPath(phone.internationalDigits)

        if (!message.isNullOrBlank()) {
            uriBuilder.appendQueryParameter("text", message)
        }

        return Intent(Intent.ACTION_VIEW, uriBuilder.build())
            .setPackage(packageName)
    }
}
