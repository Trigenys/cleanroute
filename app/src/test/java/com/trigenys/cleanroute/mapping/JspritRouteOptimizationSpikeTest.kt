package com.trigenys.cleanroute.mapping

import com.graphhopper.jsprit.core.algorithm.box.Jsprit
import com.graphhopper.jsprit.core.problem.Location
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
import com.graphhopper.jsprit.core.problem.job.Service
import com.graphhopper.jsprit.core.util.Solutions
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
import kotlin.math.cos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JspritRouteOptimizationSpikeTest {
    private val depot = GeoCoordinate(
        latitude = 4.0400,
        longitude = 9.6900
    )

    private val manualStops = listOf(
        customer("A", 4.0400, 9.6945),
        customer("B", 4.0400, 9.6855),
        customer("C", 4.0445, 9.6900),
        customer("D", 4.0355, 9.6900),
        customer("E", 4.0436, 9.6936),
        customer("F", 4.0364, 9.6864)
    )

    @Test
    fun denseUrbanSpikeComparesManualAndJspritOrderWithoutSavingsClaim() {
        val projected = manualStops.associateBy(
            keySelector = GeocodedCustomer::customerId,
            valueTransform = { project(it.coordinate) }
        )
        val depotProjected = project(depot)

        val vehicleType = VehicleTypeImpl.Builder
            .newInstance("collector")
            .addCapacityDimension(0, 100)
            .setCostPerDistance(1.0)
            .build()

        val vehicle = VehicleImpl.Builder
            .newInstance("collector-1")
            .setStartLocation(
                Location.newInstance(depotProjected.first, depotProjected.second)
            )
            .setType(vehicleType)
            .setReturnToDepot(true)
            .build()

        val problemBuilder = VehicleRoutingProblem.Builder
            .newInstance()
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addVehicle(vehicle)

        manualStops.forEach { customer ->
            val point = requireNotNull(projected[customer.customerId])
            problemBuilder.addJob(
                Service.Builder
                    .newInstance(customer.customerId)
                    .addSizeDimension(0, 1)
                    .setLocation(Location.newInstance(point.first, point.second))
                    .build()
            )
        }

        val problem = problemBuilder.build()
        val algorithm = Jsprit.createAlgorithm(problem)
        algorithm.setMaxIterations(200)

        val best = Solutions.bestOf(algorithm.searchSolutions())
        assertTrue(best.unassignedJobs.isEmpty())

        val route = best.routes.single()
        val optimizedIds = route.activities.mapNotNull { activity ->
            (activity as? TourActivity.JobActivity)?.job?.id
        }
        val byId = manualStops.associateBy(GeocodedCustomer::customerId)
        val optimizedStops = optimizedIds.map { requireNotNull(byId[it]) }

        assertEquals(manualStops.map(GeocodedCustomer::customerId).toSet(), optimizedIds.toSet())

        val metrics = HaversineMetricProvider(averageSpeedKph = 18.0)
        val manualMetrics = metrics.closedTour(depot, manualStops)
        val optimizedMetrics = metrics.closedTour(depot, optimizedStops)

        assertTrue(optimizedMetrics.distanceMeters < manualMetrics.distanceMeters)
        assertTrue(optimizedMetrics.durationSeconds < manualMetrics.durationSeconds)

        val comparison = RouteComparison(
            manualOrder = manualStops.map(GeocodedCustomer::customerId),
            optimizedOrder = optimizedIds,
            manual = manualMetrics,
            optimized = optimizedMetrics,
            representativeData = false
        )

        assertFalse(comparison.representativeData)
    }

    @Test
    fun mappingCapabilityIsDisabledByDefault() {
        assertFalse(MappingFeatureConfig().enabled)
    }

    private fun customer(
        id: String,
        latitude: Double,
        longitude: Double
    ) = GeocodedCustomer(
        customerId = id,
        label = "Client $id",
        coordinate = GeoCoordinate(latitude, longitude)
    )

    private fun project(point: GeoCoordinate): Pair<Double, Double> {
        val earthRadiusMeters = 6_371_000.0
        val lat0 = Math.toRadians(depot.latitude)
        val x = earthRadiusMeters *
            Math.toRadians(point.longitude - depot.longitude) *
            cos(lat0)
        val y = earthRadiusMeters *
            Math.toRadians(point.latitude - depot.latitude)
        return x to y
    }
}
