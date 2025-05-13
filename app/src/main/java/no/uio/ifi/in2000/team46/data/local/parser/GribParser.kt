package no.uio.ifi.in2000.team46.data.local.parser

import android.util.Log
import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.domain.grib.PrecipitationPoint
import no.uio.ifi.in2000.team46.domain.grib.Vector
import no.uio.ifi.in2000.team46.domain.grib.VectorType
import no.uio.ifi.in2000.team46.domain.grib.WaveVector
import no.uio.ifi.in2000.team46.domain.grib.WindVector
import ucar.ma2.ArrayFloat
import ucar.ma2.Index4D
import ucar.nc2.NetcdfFile
import ucar.nc2.NetcdfFiles
import ucar.nc2.time.CalendarDateUnit
import kotlin.math.atan2
import kotlin.math.sqrt
import java.io.File
import ucar.nc2.time.Calendar

// gribparser parses grib-based netcdf files and extracts vector data for wind, current, wave and precipitation
// it handles coordinate iteration, time resolution, and unit conversion for each data type

class GribParser {
    companion object {
        private const val TAG = "GribParser"
    }

    // parses wind or current vector data from a grib file given u and v component variable names
    fun parseVectorFile(
        file: File,
        uComponentName: String,
        vComponentName: String,
        vectorType: VectorType
    ): List<Vector> {
        val vectors = mutableListOf<Vector>()
        val ncfile = NetcdfFiles.open(file.absolutePath)

        val uVar = ncfile.findVariable(uComponentName) ?: error("Fant ikke $uComponentName")
        val vVar = ncfile.findVariable(vComponentName) ?: error("Fant ikke $vComponentName")
        val latVar = ncfile.findVariable("lat") ?: error("Fant ikke lat")
        val lonVar = ncfile.findVariable("lon") ?: error("Fant ikke lon")
        val timeVar = ncfile.findVariable("time") ?: error("Fant ikke time")

        val uData = uVar.read() as ArrayFloat.D4
        val vData = vVar.read() as ArrayFloat.D4
        val lats = latVar.read().reduce().storage as FloatArray
        val lons = lonVar.read().reduce().storage as FloatArray
        val timeArray = timeVar.read().reduce().storage
        val timeUnits = timeVar.getUnitsString()
        val calendarDateUnit = CalendarDateUnit.of(Calendar.gregorian.toString(), timeUnits)

        val index = uData.index as Index4D
        val heightIndex = 0

        // converts time values to epoch millis
        val timeSteps = when (timeArray) {
            is FloatArray -> timeArray.map { calendarDateUnit.makeCalendarDate(it.toDouble()).millis }
            is DoubleArray -> timeArray.map { calendarDateUnit.makeCalendarDate(it).millis }
            else -> throw IllegalArgumentException("Ukjent time-array-type")
        }

        for ((timeIndex, timestamp) in timeSteps.withIndex()) {
            for (iLat in lats.indices) {
                for (iLon in lons.indices) {
                    index.set(timeIndex, heightIndex, iLat, iLon)
                    val u = uData.getFloat(index)
                    val v = vData.getFloat(index)
                    val speed = sqrt(u * u + v * v).toDouble()
                    val direction = (Math.toDegrees(atan2(u.toDouble(), v.toDouble())) + 360) % 360

                    if (speed.isFinite() && direction.isFinite()) {
                        val vector = when (vectorType) {
                            VectorType.WIND -> WindVector(lons[iLon].toDouble(), lats[iLat].toDouble(), speed, direction, timestamp)
                            VectorType.CURRENT -> CurrentVector(lons[iLon].toDouble(), lats[iLat].toDouble(), speed, direction, timestamp)
                        }
                        vectors += vector
                    }
                }
            }
        }

        ncfile.close()
        return vectors
    }

    // parses wave vector data (height and direction) from a grib file
    fun parseWaveFile(file: File): List<WaveVector> {
        val ncfile = NetcdfFiles.open(file.absolutePath)

        val swhName = "Significant_height_of_combined_wind_waves_and_swell_height_above_ground"
        val dirName = "VAR88-0-140-230_height_above_ground"

        val swhVar = ncfile.findVariable(swhName)
            ?: error("Fant ikke $swhName i GRIB-filen")
        val mwdVar = ncfile.findVariable(dirName)
            ?: error("Fant ikke $dirName i GRIB-filen")
        val latVar = ncfile.findVariable("lat") ?: error("Fant ikke lat")
        val lonVar = ncfile.findVariable("lon") ?: error("Fant ikke lon")
        val timeVar = ncfile.findVariable("time") ?: error("Fant ikke time")

        val swhData = swhVar.read() as ArrayFloat.D4
        val mwdData = mwdVar.read() as ArrayFloat.D4
        val lats = latVar.read().reduce().storage as FloatArray
        val lons = lonVar.read().reduce().storage as FloatArray

        val timeUnits = timeVar.unitsString
        val calendarDateUnit = CalendarDateUnit.of(Calendar.gregorian.toString(), timeUnits)
        val timeArray = timeVar.read().reduce().storage

        // converts time values to epoch millis
        val timeSteps = when (timeArray) {
            is FloatArray -> timeArray.map { calendarDateUnit.makeCalendarDate(it.toDouble()).millis }
            is DoubleArray -> timeArray.map { calendarDateUnit.makeCalendarDate(it).millis }
            else -> throw IllegalArgumentException("Ukjent time-array-type: ${timeArray::class.java}")
        }

        val idx = swhData.index as Index4D
        val levelIndex = 0

        val waves = mutableListOf<WaveVector>()
        for ((timeIndex, timestamp) in timeSteps.withIndex()) {
            for (iLat in lats.indices) {
                for (iLon in lons.indices) {
                    idx.set(timeIndex, levelIndex, iLat, iLon)
                    val height = swhData.getFloat(idx).toDouble()
                    val fromDir = mwdData.getFloat(idx).toDouble()
                    val toDir = (fromDir + 180) % 360

                    if (height.isFinite() && toDir.isFinite()) {
                        waves += WaveVector(
                            lon = lons[iLon].toDouble(),
                            lat = lats[iLat].toDouble(),
                            height = height,
                            direction = toDir,
                            timestamp = timestamp
                        )
                    }
                }
            }
        }

        ncfile.close()
        return waves
    }

    // parses cumulative precipitation and converts it to delta between timesteps
    fun parsePrecipitationFile(
        file: File,
        levelIndex: Int = 0
    ): List<PrecipitationPoint> {
        Log.d(TAG, "Opening GRIB file: ${file.absolutePath}")
        val ncfile = NetcdfFiles.open(file.absolutePath)

        val precVar = ncfile.findVariable("Total_precipitation_height_above_ground")
            ?: throw IllegalArgumentException("Fant ikke Total_precipitation_height_above_ground")
        val latVar = ncfile.findVariable("lat") ?: throw IllegalArgumentException("Fant ikke lat")
        val lonVar = ncfile.findVariable("lon") ?: throw IllegalArgumentException("Fant ikke lon")
        val timeVar = ncfile.findVariable("time") ?: throw IllegalArgumentException("Fant ikke time")

        val units = precVar.getUnitsString()
        Log.d(TAG, "Units: $units")

        val data = precVar.read() as ArrayFloat.D4
        val lats = latVar.read().reduce().storage as FloatArray
        val lons = lonVar.read().reduce().storage as FloatArray
        val idx = data.index as Index4D

        val timeArray = timeVar.read().reduce().storage
        val timeUnits = timeVar.getUnitsString()
        val calendarDateUnit = CalendarDateUnit.of(Calendar.gregorian.toString(), timeUnits)
        val timeSteps = when (timeArray) {
            is FloatArray -> timeArray.map { calendarDateUnit.makeCalendarDate(it.toDouble()).millis }
            is DoubleArray -> timeArray.map { calendarDateUnit.makeCalendarDate(it).millis }
            else -> throw IllegalArgumentException("Ukjent time-array-type: ${timeArray::class.java}")
        }

        val points = mutableListOf<PrecipitationPoint>()
        for (timeIndex in 1 until timeSteps.size) {
            val timestamp = timeSteps[timeIndex]

            for (iLat in lats.indices) {
                for (iLon in lons.indices) {
                    idx.set(timeIndex, levelIndex, iLat, iLon)
                    val rawCum = data.getFloat(idx).toDouble()

                    idx.set(timeIndex - 1, levelIndex, iLat, iLon)
                    val rawPrev = data.getFloat(idx).toDouble()

                    val rawDelta = (rawCum - rawPrev).coerceAtLeast(0.0)
                    val inMm = when {
                        units.contains("kg m^-2") -> rawDelta
                        units.contains("m") -> rawDelta * 1000.0
                        else -> rawDelta
                    }

                    if (inMm.isFinite() && inMm > 0) {
                        points += PrecipitationPoint(
                            lon = lons[iLon].toDouble(),
                            lat = lats[iLat].toDouble(),
                            precipitation = inMm,
                            timestamp = timestamp
                        )
                    }
                }
            }
        }

        Log.d(TAG, "Parsed ${points.size} precipitation points across ${timeSteps.size} timesteps.")
        ncfile.close()
        return points
    }

    // utility method for debugging grib files by listing all available variables and time info
    fun listVariablesInGrib(file: File) {
        val ncfile = NetcdfFiles.open(file.absolutePath)
        Log.d("GribParser", "=== Variabler i GRIB-filen ===")
        ncfile.variables.forEach { variable ->
            Log.d("GribParser", "Navn: ${variable.fullName}, Dimensions: ${variable.dimensionsString}")
        }

        val timeVar = ncfile.findVariable("time")
        if (timeVar != null) {
            val timeArray = timeVar.read().reduce().storage
            val timeUnits = timeVar.getUnitsString()
            val calendarDateUnit = CalendarDateUnit.of(Calendar.gregorian.toString(), timeUnits)

            Log.d("GribParser", "Enheter: $timeUnits")
            Log.d("GribParser", "Antall tidspunkter: ${timeVar.shape.joinToString()}")

            when (timeArray) {
                is FloatArray -> timeArray.forEachIndexed { i, v ->
                    val millis = calendarDateUnit.makeCalendarDate(v.toDouble()).millis
                    Log.d("GribParser", "[$i] = $v -> $millis")
                }
                is DoubleArray -> timeArray.forEachIndexed { i, v ->
                    val millis = calendarDateUnit.makeCalendarDate(v).millis
                    Log.d("GribParser", "[$i] = $v -> $millis")
                }
                else -> Log.w("GribParser", "Ukjent type for time-array: ${timeArray::class.java}")
            }
        } else {
            Log.w("GribParser", "Fant ikke variabelen 'time'")
        }

        ncfile.close()
    }
}
