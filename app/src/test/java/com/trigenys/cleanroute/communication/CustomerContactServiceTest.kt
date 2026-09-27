package com.trigenys.cleanroute.communication

import com.trigenys.cleanroute.domain.ContactAction
import com.trigenys.cleanroute.domain.ContactActionId
import com.trigenys.cleanroute.domain.ContactActionRepository
import com.trigenys.cleanroute.domain.ContactChannel
import com.trigenys.cleanroute.domain.CustomerId
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CustomerContactServiceTest {
    @Test
    fun missingWhatsAppReturnsReadableFailureInsteadOfLaunching() = runBlocking {
        val service = CustomerContactService(
            repository = FakeContactActionRepository(),
            intentFactory = ContactIntentFactory(PhoneNumberNormalizer("237")),
            templates = FrenchCustomerMessageTemplates()
        )

        val result = service.whatsApp(
            context = RuntimeEnvironment.getApplication(),
            customerId = CustomerId("customer-1"),
            rawPhone = "690000001",
            kind = CustomerMessageKind.UPCOMING_COLLECTION,
            data = CustomerMessageData(
                customerName = "Mme Mballa",
                collectionDate = LocalDate.of(2026, 9, 28)
            ),
            at = Instant.parse("2026-09-27T11:00:00Z")
        )

        assertTrue(result is ContactLaunchResult.Unavailable)
    }

    private class FakeContactActionRepository : ContactActionRepository {
        override suspend fun record(
            customerId: CustomerId,
            channel: ContactChannel,
            at: Instant
        ): ContactAction =
            ContactAction(
                id = ContactActionId("fake-action"),
                customerId = customerId,
                channel = channel,
                createdAt = at
            )
    }
}
