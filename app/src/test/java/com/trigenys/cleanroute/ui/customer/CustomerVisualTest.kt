package com.trigenys.cleanroute.ui.customer

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.material3.Surface
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
                    onCustomerSelected = {},
                    totalCount = 2,
                    outstandingByCustomer = mapOf(profile.customer.id.value to 7_500L),
                    sectors = listOf("Bonamoussadi", "Makepe"),
                    onSectorSelected = {},
                    onImportCustomers = {},
                    onOpenCollection = {}
                )
            }
        }
    }

    @Test
    fun newCustomerForm() {
        captureRoboImage {
            CleanRouteTheme {
                Surface {
                    CustomerFormContent(
                        initialProfile = null,
                        errorMessage = null,
                        onDismiss = {},
                        onSave = {},
                        suggestedZones = listOf("Bonamoussadi", "Makepe", "Bonapriso")
                    )
                }
            }
        }
    }

    @Test
    fun activeCustomerDetailTimeline() {
        val profile = sampleCustomerProfile().copy(
            paidThisPeriodXaf = 0,
            outstandingThisPeriodXaf = 5_000
        )
        captureRoboImage {
            CleanRouteTheme {
                CustomerDetailScreen(
                    profile = profile,
                    innerPadding = PaddingValues(),
                    onBack = {},
                    onEdit = {},
                    onRecordPayment = {},
                    onReversePayment = {}
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
