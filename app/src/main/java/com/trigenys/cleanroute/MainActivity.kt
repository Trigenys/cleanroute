package com.trigenys.cleanroute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.data.transfer.CustomerSpreadsheetService
import com.trigenys.cleanroute.ui.App
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: CleanRouteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = CleanRouteDatabase.open(applicationContext)
        val customerRepository = RoomCustomerRepository(database)
        val spreadsheetService = CustomerSpreadsheetService(database)

        setContent {
            CleanRouteTheme {
                App(
                    customerRepository = customerRepository,
                    spreadsheetService = spreadsheetService
                )
            }
        }
    }

    override fun onDestroy() {
        if (::database.isInitialized) {
            database.close()
        }
        super.onDestroy()
    }
}
