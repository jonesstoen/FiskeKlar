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

class GribParser {
    companion object {
        private const val TAG = "GribParser"
    }

    fun parseVectorFile(
        file: File,
        uComponentName: String,
        vComponentName: String,
        vectorType: VectorType
    ): List<Vector> {
        val vectors = mutableListOf<Vector>()
        val ncfile = NetcdfFiles.open(file.absolutePath)

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
    fun parseWaveFile(file: File): List<WaveVector> {
        val ncfile = NetcdfFiles.open(file.absolutePath)

        // bruk de faktiske navnene fra Logcat
        val swhName = "Significant_height_of_combined_wind_waves_and_swell_height_above_ground"
        val dirName = "VAR88-0-140-230_height_above_ground"

        val swhVar = ncfile.findVariable(swhName)
            ?: error("Fant ikke $swhName i GRIB-filen")
        val mwdVar = ncfile.findVariable(dirName)
            ?: error("Fant ikke $dirName i GRIB-filen")
        val latVar = ncfile.findVariable("lat") ?: error("Fant ikke lat")
        val lonVar = ncfile.findVariable("lon") ?: error("Fant ikke lon")

        val swhData = swhVar.read() as ArrayFloat.D4
        val mwdData = mwdVar.read() as ArrayFloat.D4
        val lats    = latVar.read().reduce().storage as FloatArray
        val lons    = lonVar.read().reduce().storage as FloatArray

        val idx = swhData.index as Index4D
        val timeIndex  = 0
        val levelIndex = 0

        val waves = mutableListOf<WaveVector>()
        for (iLat in lats.indices) {
            for (iLon in lons.indices) {
                idx.set(timeIndex, levelIndex, iLat, iLon)
                val height = swhData.getFloat(idx).toDouble()
                val fromDir = mwdData.getFloat(idx).toDouble()
                // meteorologisk konvensjon: retningen bølgene kommer fra
                val toDir = (fromDir + 180) % 360

                if (height.isFinite() && toDir.isFinite()) {
                    waves += WaveVector(
                        lon       = lons[iLon].toDouble(),
                        lat       = lats[iLat].toDouble(),
                        height    = height,
                        direction = toDir
                    )
                }
            }
        }
        ncfile.close()
        return waves
    }



    // for å parse nedbør
    fun parsePrecipitationFile(
        file: File,
        timeIndex: Int = 1,
        levelIndex: Int = 0
    ): List<PrecipitationPoint> {
        Log.d(TAG, "Opening GRIB file: ${file.absolutePath}")
        val ncfile = NetcdfFiles.open(file.absolutePath)
        val precVar = ncfile.findVariable("Total_precipitation_height_above_ground")
            ?: throw IllegalArgumentException("Fant ikke Total_precipitation_height_above_ground")
        val latVar = ncfile.findVariable("lat")
            ?: throw IllegalArgumentException("Fant ikke lat")
        val lonVar = ncfile.findVariable("lon")
            ?: throw IllegalArgumentException("Fant ikke lon")

        // Log dimension info
        val timeDim = precVar.dimensions.find { it.shortName == "time" }?.length ?: -1
        val latLen = latVar.dimensions.firstOrNull()?.length ?: -1
        val lonLen = lonVar.dimensions.firstOrNull()?.length ?: -1
        Log.d(TAG, "Dimensions -> time=$timeDim, lat=$latLen, lon=$lonLen")

        val units = precVar.getUnitsString()
        Log.d(TAG, "Units: $units")

        @Suppress("UNCHECKED_CAST")
        val data = precVar.read() as ArrayFloat.D4
        val lats = (latVar.read().reduce().storage as FloatArray)
        val lons = (lonVar.read().reduce().storage as FloatArray)
        val idx = data.index as Index4D

        // Prepare sampling indices
        val midLat = lats.size / 2
        val midLon = lons.size / 2
        var firstNonZeroLogged = false

        val points = mutableListOf<PrecipitationPoint>()
        for (iLat in lats.indices) {
            for (iLon in lons.indices) {
                // compute cumulative at timeIndex and previous
                idx.set(timeIndex, levelIndex, iLat, iLon)
                val rawCum = data.getFloat(idx).toDouble()
                idx.set(timeIndex - 1, levelIndex, iLat, iLon)
                val rawPrev = data.getFloat(idx).toDouble()
                val rawDelta = (rawCum - rawPrev).coerceAtLeast(0.0)

                // convert units
                val inMm = when {
                    units.contains("kg m^-2") -> rawDelta
                    units.contains("m") -> rawDelta * 1000.0
                    else -> rawDelta
                }

                // Log a few debug samples
                if (!firstNonZeroLogged && inMm > 0) {
                    Log.d(TAG, "First non-zero delta at [$iLat,$iLon]: cum=$rawCum, prev=$rawPrev, delta=$rawDelta, mm=$inMm")
                    firstNonZeroLogged = true
                }
                if (iLat == 0 && iLon < 5) {
                    Log.d(TAG, String.format("Top-row sample [%d,%d]: mm=%.3f", iLat, iLon, inMm))
                }
                if (iLat == midLat && iLon == midLon) {
                    Log.d(TAG, String.format("Mid-grid sample [%d,%d]: mm=%.3f", iLat, iLon, inMm))
                }

                if (inMm.isFinite() && inMm > 0) {
                    points += PrecipitationPoint(
                        lon = lons[iLon].toDouble(),
                        lat = lats[iLat].toDouble(),
                        precipitation = inMm
                    )
                }
            }
        }

        Log.d(TAG, "Parsed points count: ${points.size}")
        ncfile.close()
        return points
    }



    // Debug: lists the variables in the file
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
