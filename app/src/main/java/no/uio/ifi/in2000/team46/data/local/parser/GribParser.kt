package no.uio.ifi.in2000.team46.data.local.parser

import android.util.Log
import ucar.ma2.ArrayFloat
import ucar.ma2.Index4D
import ucar.nc2.NetcdfFile
import kotlin.math.atan2
import kotlin.math.sqrt
import java.io.File

class GribParser {

    fun parseGribFile(file: File): List<WindVector> {
        val vectors = mutableListOf<WindVector>()
        val ncfile = NetcdfFile.open(file.absolutePath)

        val uVar = ncfile.findVariable("u-component_of_wind_height_above_ground")
            ?: throw IllegalArgumentException("Fant ikke u-component_of_wind_height_above_ground")
        val vVar = ncfile.findVariable("v-component_of_wind_height_above_ground")
            ?: throw IllegalArgumentException("Fant ikke v-component_of_wind_height_above_ground")
        val latVar = ncfile.findVariable("lat")
            ?: throw IllegalArgumentException("Fant ikke lat")
        val lonVar = ncfile.findVariable("lon")
            ?: throw IllegalArgumentException("Fant ikke lon")

        val uData = uVar.read() as ArrayFloat.D4
        val vData = vVar.read() as ArrayFloat.D4

        val lats = latVar.read().reduce().storage as FloatArray
        val lons = lonVar.read().reduce().storage as FloatArray

        val timeIndex = 0
        val heightIndex = 0

        val latSize = lats.size
        val lonSize = lons.size

        val index = uData.index as Index4D

        //  Debug: Print de første 5 koordinatene for å verifisere
        for (testLat in 0 until minOf(5, latSize)) {
            Log.d("GribParser", "DEBUG lat[$testLat]: ${lats[testLat]}")
        }
        for (testLon in 0 until minOf(5, lonSize)) {
            Log.d("GribParser", "DEBUG lon[$testLon]: ${lons[testLon]}")
        }

        for (iLat in 0 until latSize) {
            for (iLon in 0 until lonSize) {
                index.set(timeIndex, heightIndex, iLat, iLon)

                val u = uData.getFloat(index)
                val v = vData.getFloat(index)

                val speed = sqrt(u * u + v * v).toDouble()
                val direction = (Math.toDegrees(atan2(u.toDouble(), v.toDouble())) + 360) % 360

                // 🟢 HER kan du bytte lat/lon hvis det viser seg at de er snudd
                vectors.add(
                    WindVector(
                        lon = lons[iLon].toDouble(),   // ← Bytt disse hvis de er feil!
                        lat = lats[iLat].toDouble(),
                        speed = speed,
                        direction = direction
                    )
                )
            }
        }

        ncfile.close()
        return vectors
    }

    /** Debug: Lister variabler i GRIB-filen */
    fun listVariablesInGrib(file: File) {
        val ncfile = NetcdfFile.open(file.absolutePath)
        Log.d("GribParser", "=== Variabler i GRIB-filen ===")
        ncfile.variables.forEach { variable ->
            Log.d("GribParser", "Navn: ${variable.fullName}, Dimensions: ${variable.dimensionsString}")
        }
        ncfile.close()
    }
}
