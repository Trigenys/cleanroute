package com.trigenys.cleanroute.mapping

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle

private const val PROTOTYPE_STYLE =
    "https://tiles.openfreemap.org/styles/liberty"

internal object MapLibreCustomerMapAdapter {
    @Composable
    fun Render(
        model: CustomerMapModel,
        modifier: Modifier = Modifier
    ) {
    val geoJson = remember(model) { model.toGeoJson() }
    val primary = MaterialTheme.colorScheme.primary

    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(PROTOTYPE_STYLE)
    ) {
        val source = rememberGeoJsonSource(
            GeoJsonData.JsonString(geoJson)
        )
        CircleLayer(
            id = "cleanroute-customer-points",
            source = source,
            radius = const(7.dp),
            color = const(primary),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp)
        )
    }

        MaplibreMap(
            state = mapState,
            modifier = modifier.fillMaxSize()
        )
    }
}

private fun CustomerMapModel.toGeoJson(): String =
    buildString {
        append("{\"type\":\"FeatureCollection\",\"features\":[")
        customers.forEachIndexed { index, customer ->
            if (index > 0) append(',')
            append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[")
            append(customer.coordinate.longitude)
            append(',')
            append(customer.coordinate.latitude)
            append("]},\"properties\":{\"customerId\":")
            append(jsonString(customer.customerId))
            append(",\"label\":")
            append(jsonString(customer.label))
            append("}}")
        }
        append("]}")
    }

private fun jsonString(value: String): String =
    buildString {
        append('"')
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
