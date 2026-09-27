package com.trigenys.cleanroute.ui.customer

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.trigenys.cleanroute.domain.CustomerDirectoryEntry
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp")
class CustomerVisualTest {
    @Test
    fun emptyDirectory() {
        captureRoboImage {
            CleanRouteTheme {
                CustomerDirectoryScreen(
                    entries = emptyList(),
                    query = "",
                    loading = false,
                    innerPadding = PaddingValues(),
                    onQueryChange = {},
                    onAddCustomer = {},
                    onCustomerSelected = {}
                )
            }
        }
    }

    @Test
    fun populatedDirectory() {
        val profile = sampleCustomerProfile()
        captureRoboImage {
            CleanRouteTheme {
                CustomerDirectoryScreen(
                    entries = listOf(
                        CustomerDirectoryEntry(profile.customer, profile.zone.name),
                        CustomerDirectoryEntry(
                            profile.customer.copy(
                                id = CustomerId("customer-2"),
                                name = "M. Ewane",
                                status = CustomerStatus.SUSPENDED
                            ),
                            "Makepe"
                        )
                    ),
                    query = "",
                    loading = false,
                    innerPadding = PaddingValues(),
                    onQueryChange = {},
                    onAddCustomer = {},
                    onCustomerSelected = {}
                )
            }
        }
    }

    @Test
    fun suspendedCustomerDetail() {
        captureRoboImage {
            CleanRouteTheme {
                CustomerDetailScreen(
                    profile = sampleCustomerProfile(CustomerStatus.SUSPENDED),
                    innerPadding = PaddingValues(),
                    onBack = {},
                    onEdit = {}
                )
            }
        }
    }
}
