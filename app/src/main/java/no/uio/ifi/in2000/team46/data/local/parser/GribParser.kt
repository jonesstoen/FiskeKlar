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

                // Sjekk om verdiene er gyldige
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



    /** Debug: Lister variabler i GRIB-filen */
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
