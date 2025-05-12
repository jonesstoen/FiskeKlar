package no.uio.ifi.in2000.team46

import org.junit.Test
import org.junit.Assert.*
import no.uio.ifi.in2000.team46.presentation.sos.screens.haversine


class SosScreenUnitTest {
    @Test
    fun haversine_distance_equales_0_for_same_location() {
        val distance = haversine(60.0, 10.0, 60.0, 10.0)
        assertEquals(0.0, distance, 0.0001)
    }

    @Test
    // 1° change in lat is 111,19 km ≈ 60,04 NM
    fun haversine_one_degree_latitude_is_ca_60_nm() {
        val distance = haversine(0.0, 0.0, 1.0, 0.0)
        assertEquals(60.04, distance, 0.1)
    }


    @Test
    // Lindesnes Fyr coordinates: 57.98260097218856, 7.046976548537053
    // IFI coordinates: 59.943725658920606, 10.718340174388437
    // Distance between IFI and Lindesnes Fyr: 303,08 km = 163.65 nautical miles
    fun returns_correct_distance_between_IFI_and_Lindesnes() {
        val ifiLat = 59.9437
        val ifiLon = 10.7183
        val lindesnesLat = 57.9826
        val lindesnesLon = 7.0470
        val distance = haversine(ifiLat, ifiLon, lindesnesLat, lindesnesLon)
        assertEquals(163.65, distance, 0.1)
    }

    @Test
    fun does_not_throw_for_extreme_coordinate_values() {
        try {
            // Utenfor normale koordinatgrenser, men skal ikke kaste unntak
            haversine(1000.0, -1000.0, 5000.0, 2000.0)
        } catch (e: Exception) {
            fail("haversine gives error because unrealistic coordinates: ${e.message}")
        }
    }
}

