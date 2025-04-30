package no.uio.ifi.in2000.team46.data.local.parser

import android.util.Log
import ucar.ma2.ArrayFloat
import ucar.ma2.Index4D
import ucar.nc2.NetcdfFile
import ucar.nc2.time.CalendarDateUnit
import kotlin.math.atan2
import kotlin.math.sqrt
import java.io.File
import ucar.nc2.time.Calendar

class GribParser {

    fun parseVectorFile(
        file: File,
        uComponentName: String,
        vComponentName: String,
        vectorType: VectorType
    ): List<Vector> {
        val vectors = mutableListOf<Vector>()
        val ncfile = NetcdfFile.open(file.absolutePath)

        val uVar = ncfile.findVariable(uComponentName)
            ?: throw IllegalArgumentException("Fant ikke $uComponentName")
        val vVar = ncfile.findVariable(vComponentName)
            ?: throw IllegalArgumentException("Fant ikke $vComponentName")
        val latVar = ncfile.findVariable("lat")
            ?: throw IllegalArgumentException("Fant ikke lat")
        val lonVar = ncfile.findVariable("lon")
            ?: throw IllegalArgumentException("Fant ikke lon")

        val uData = uVar.read() as ArrayFloat.D4
        val vData = vVar.read() as ArrayFloat.D4
        val lats = latVar.read().reduce().storage as FloatArray
        val lons = lonVar.read().reduce().storage as FloatArray

        val index = uData.index as Index4D
        val timeIndex = 0
        val heightIndex = 0

        for (iLat in 0 until lats.size) {
            for (iLon in 0 until lons.size) {
                index.set(timeIndex, heightIndex, iLat, iLon)
                val u = uData.getFloat(index)
                val v = vData.getFloat(index)
                val speed = sqrt(u * u + v * v).toDouble()
                val direction = (Math.toDegrees(atan2(u.toDouble(), v.toDouble())) + 360) % 360

                // check if the variables are correctly defined
                if (speed.isFinite() && direction.isFinite()) {
                    val vector = when (vectorType) {
                        VectorType.WIND -> WindVector(lons[iLon].toDouble(), lats[iLat].toDouble(), speed, direction)
                        VectorType.CURRENT -> CurrentVector(lons[iLon].toDouble(), lats[iLat].toDouble(), speed, direction)
                    }
                    vectors.add(vector)
                }
            }
        }
        ncfile.close()
        return vectors
    }

    // for å parse nedbør
    fun parsePrecipitationFile(
        file: File,
        precVarName: String = "Total_precipitation_height_above_ground",
        timeIndex: Int = 0,
        levelIndex: Int = 0
    ): List<PrecipitationPoint> {
        val ncfile = NetcdfFile.open(file.absolutePath)
        val precVar = ncfile.findVariable(precVarName)
            ?: throw IllegalArgumentException("Fant ikke $precVarName")
        val latVar = ncfile.findVariable("lat")
            ?: throw IllegalArgumentException("Fant ikke lat")
        val lonVar = ncfile.findVariable("lon")
            ?: throw IllegalArgumentException("Fant ikke lon")

        // Optional: log units so you know whether to convert
        Log.d("GribParser", "Precip units = ${precVar.getUnitsString()}")

        @Suppress("UNCHECKED_CAST")
        val precData = precVar.read() as ArrayFloat.D4
        val lats = (latVar.read().reduce().storage as FloatArray)
        val lons = (lonVar.read().reduce().storage as FloatArray)
        val idx = precData.index as Index4D

        val points = mutableListOf<PrecipitationPoint>()
        for (iLat in lats.indices) {
            for (iLon in lons.indices) {
                idx.set(timeIndex, levelIndex, iLat, iLon)
                val raw = precData.getFloat(idx).toDouble()
                if (raw.isFinite()) {
                    // if units are “m”, convert to mm:
                    val inMm = raw * 1000.0
                    points += PrecipitationPoint(
                        lon = lons[iLon].toDouble(),
                        lat = lats[iLat].toDouble(),
                        precipitation = inMm
                    )
                }
            }
        }

        ncfile.close()
        return points
    }


    // Debug: lists the variables in the file
    fun listVariablesInGrib(file: File) {
        val ncfile = NetcdfFile.open(file.absolutePath)
        Log.d("GribParser", "=== Variabler i GRIB-filen ===")
        ncfile.variables.forEach { variable ->
            Log.d("GribParser", "Navn: ${variable.fullName}, Dimensions: ${variable.dimensionsString}")
        }
        val timeVar = ncfile.findVariable("time")
        if (timeVar != null) {
            val timeArray = timeVar.read().reduce().storage
            val timeUnits = timeVar.getUnitsString()
            val calendarDateUnit = CalendarDateUnit.of(Calendar.gregorian.toString(), timeUnits)


            val timeValue = when (timeArray) {
                is FloatArray -> timeArray[0].toDouble()
                is DoubleArray -> timeArray[0]
                else -> throw IllegalArgumentException("Ukjent type for time-array: ${timeArray::class.java}")
            }
            val timestamp = calendarDateUnit.makeCalendarDate(timeValue).millis
            Log.d("GribParser", "Første timestamp: $timestamp ms ($timeUnits)")
        }
        ncfile.close()
    }
}
