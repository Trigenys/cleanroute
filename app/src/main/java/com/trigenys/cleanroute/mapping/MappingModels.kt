package com.trigenys.cleanroute.mapping

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }
}

data class GeocodedCustomer(
    val customerId: String,
    val label: String,
    val coordinate: GeoCoordinate
) {
    init {
        require(customerId.isNotBlank()) { "Customer id must not be blank" }
        require(label.isNotBlank()) { "Customer label must not be blank" }
    }
}

data class CustomerMapModel(
    val customers: List<GeocodedCustomer>
)

data class MappingFeatureConfig(
    val enabled: Boolean = false
)

data class RouteMetrics(
    val distanceMeters: Double,
    val durationSeconds: Long
) {
    init {
        require(distanceMeters >= 0.0) { "Distance must not be negative" }
        require(durationSeconds >= 0L) { "Duration must not be negative" }
    }
}

data class RouteComparison(
    val manualOrder: List<String>,
    val optimizedOrder: List<String>,
    val manual: RouteMetrics,
    val optimized: RouteMetrics,
    val representativeData: Boolean
)

interface RouteOrderOptimizer {
    fun optimize(
        depot: GeoCoordinate,
        stops: List<GeocodedCustomer>
    ): List<String>
}

interface RouteMetricProvider {
    fun closedTour(
        depot: GeoCoordinate,
        orderedStops: List<GeocodedCustomer>
    ): RouteMetrics
}

class HaversineMetricProvider(
    private val averageSpeedKph: Double
) : RouteMetricProvider {
    init {
        require(averageSpeedKph > 0.0) { "Average speed must be positive" }
    }

    override fun closedTour(
        depot: GeoCoordinate,
        orderedStops: List<GeocodedCustomer>
    ): RouteMetrics {
        if (orderedStops.isEmpty()) return RouteMetrics(0.0, 0L)

        val points = buildList {
            add(depot)
            orderedStops.forEach { add(it.coordinate) }
            add(depot)
        }
        val distance = points.zipWithNext().sumOf { (a, b) -> haversineMeters(a, b) }
        val durationSeconds = (
            distance / (averageSpeedKph * 1_000.0 / 3_600.0)
        ).roundToLong()

        return RouteMetrics(
            distanceMeters = distance,
            durationSeconds = durationSeconds
        )
    }

    private fun haversineMeters(
        a: GeoCoordinate,
        b: GeoCoordinate
    ): Double {
        val earthRadiusMeters = 6_371_000.0
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val deltaLat = Math.toRadians(b.latitude - a.latitude)
        val deltaLon = Math.toRadians(b.longitude - a.longitude)

        val h = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(lat1) * cos(lat2) *
            sin(deltaLon / 2) * sin(deltaLon / 2)

        return 2 * earthRadiusMeters * asin(sqrt(h))
    }
}
