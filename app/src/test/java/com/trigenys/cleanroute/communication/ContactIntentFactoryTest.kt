package com.trigenys.cleanroute.communication

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContactIntentFactoryTest {
    private val factory = ContactIntentFactory(
        PhoneNumberNormalizer("237")
    )

    @Test
    fun dialIntentUsesNormalizedE164Number() {
        val intent = factory.dial("690 00 00 01")

        assertEquals(Intent.ACTION_DIAL, intent?.action)
        assertEquals("tel", intent?.data?.scheme)
        assertEquals("+237690000001", intent?.data?.schemeSpecificPart)
    }

    @Test
    fun whatsAppIntentTargetsNormalizedConversationAndEncodesMessage() {
        val message = "Bonjour Mme Mballa, passage demain."
        val intent = factory.whatsApp(
            rawPhone = "+237 690 00 00 01",
            message = message,
            packageName = "com.whatsapp"
        )

        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertEquals("com.whatsapp", intent?.`package`)
        assertEquals("wa.me", intent?.data?.host)
        assertEquals("/237690000001", intent?.data?.path)
        assertEquals(message, intent?.data?.getQueryParameter("text"))
    }

    @Test
    fun invalidPhoneDoesNotProduceAnIntent() {
        assertNull(factory.dial("12"))
        assertNull(
            factory.whatsApp(
                rawPhone = "12",
                message = "Bonjour",
                packageName = "com.whatsapp"
            )
        )
    }
}
