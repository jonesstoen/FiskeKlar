package no.uio.ifi.in2000.team46.data.local.parser

import android.util.Log
import com.google.common.collect.ImmutableList
import io.mockk.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import ucar.nc2.NetcdfFile
import ucar.nc2.NetcdfFiles
import ucar.nc2.Variable
import ucar.nc2.Dimension
import ucar.ma2.Array
import ucar.ma2.ArrayFloat
import ucar.ma2.Index4D
import no.uio.ifi.in2000.team46.domain.grib.*
import java.io.File

// DISCLAIMER: ChatGPT-4o was used to create this test class.
class GribParserUnitTest {

    private val dummy = File("dummy.grib")
    private lateinit var ncfile: NetcdfFile

    @Before
    fun setUp() {
        // Stub out Android Log.d
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // Stub NetcdfFiles.open(...)
        mockkStatic(NetcdfFiles::class)
        ncfile = mockk(relaxed = true)
        every { NetcdfFiles.open(dummy.absolutePath) } returns ncfile
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // 1) parseVectorFile if missing the u-component
    @Test
    fun parseVectorFile_missing_uVar_throws() {
        every { ncfile.findVariable("uComp") } returns null

        try {
            GribParser().parseVectorFile(dummy, "uComp", "vComp", VectorType.WIND)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Fant ikke uComp"))
        }
    }

    // 2) parseVectorFile with a point(u=3, v=4) → speed=5
    @Test
    fun parseVectorFile_singlePoint() {
        // Mock alle variabler
        val vars = listOf("uComp","vComp","lat","lon","time")
            .associateWith { mockk<Variable>() }
        vars.forEach { (name, v) ->
            every { ncfile.findVariable(name) } returns v
        }

        // lat=[10f], lon=[20f]
        listOf("lat" to 10f, "lon" to 20f).forEach { (name, f) ->
            val arr = mockk<Array>()
            every { vars[name]!!.read() } returns arr
            every { arr.reduce() } returns arr
            every { arr.storage } returns floatArrayOf(f)
        }

        // time=[0f], units=epoch
        val tArr = mockk<Array>()
        every { vars["time"]!!.read() } returns tArr
        every { tArr.reduce() } returns tArr
        every { tArr.storage } returns floatArrayOf(0f)
        every { vars["time"]!!.getUnitsString() } returns "hours since 1970-01-01T00:00:00Z"

        // u=3, v=4
        val u4 = mockk<ArrayFloat.D4>()
        val v4 = mockk<ArrayFloat.D4>()
        val idx = mockk<Index4D>(relaxed = true)
        every { u4.index } returns idx
        every { v4.index } returns idx
        every { u4.getFloat(idx) } returns 3f
        every { v4.getFloat(idx) } returns 4f
        every { vars["uComp"]!!.read() } returns u4
        every { vars["vComp"]!!.read() } returns v4

        // run parser
        val list = GribParser().parseVectorFile(dummy, "uComp", "vComp", VectorType.WIND)

        assertEquals(1, list.size)
        val w = list[0] as WindVector
        assertEquals(5.0, w.speed, 1e-6)  // sqrt(3^2+4^2)
        val expectedDir = (Math.toDegrees(Math.atan2(3.0,4.0)) + 360) % 360
        assertEquals(expectedDir, w.direction, 1e-6)
        assertEquals(20.0, w.lon, 1e-6)
        assertEquals(10.0, w.lat, 1e-6)
        assertEquals(0L, w.timestamp)

        verify { ncfile.close() }
    }

    // 3) parseWaveFile with a point (height=2.5, fromDir=45 → toDir=225)
    @Test
    fun parseWaveFile_singlePoint() {
        val swh = mockk<Variable>()
        val mwd = mockk<Variable>()
        listOf(
            "Significant_height_of_combined_wind_waves_and_swell_height_above_ground" to swh,
            "VAR88-0-140-230_height_above_ground" to mwd,
            "lat" to mockk<Variable>(),
            "lon" to mockk<Variable>(),
            "time" to mockk<Variable>()
        ).forEach { (name, v) ->
            every { ncfile.findVariable(name) } returns v
        }

        // lat=3, lon=4
        listOf("lat" to 3f, "lon" to 4f).forEach { (name, f) ->
            val arr = mockk<Array>()
            every { (ncfile.findVariable(name) as Variable).read() } returns arr
            every { arr.reduce() } returns arr
            every { arr.storage } returns floatArrayOf(f)
        }

        // time=0
        val timeVar = ncfile.findVariable("time")!!
        val tArr = mockk<Array>()
        every { timeVar.read() } returns tArr
        every { tArr.reduce() } returns tArr
        every { tArr.storage } returns floatArrayOf(0f)
        every { timeVar.unitsString } returns "hours since 1970-01-01T00:00:00Z"

        // data: height=2.5, fromDir=45
        val s4 = mockk<ArrayFloat.D4>()
        val m4 = mockk<ArrayFloat.D4>()
        val idx = mockk<Index4D>(relaxed = true)
        every { s4.index } returns idx
        every { m4.index } returns idx
        every { s4.getFloat(idx) } returns 2.5f
        every { m4.getFloat(idx) } returns 45f
        every { swh.read() } returns s4
        every { mwd.read() } returns m4

        // runs parser
        val waves = GribParser().parseWaveFile(dummy)

        assertEquals(1, waves.size)
        with(waves[0]) {
            assertEquals(3.0, lat, 1e-6)
            assertEquals(4.0, lon, 1e-6)
            assertEquals(2.5, height, 1e-6)
            assertEquals((45.0 + 180) % 360, direction, 1e-6)
            assertEquals(0L, timestamp)
        }
    }

    // 4) parsePrecipitationFile kg m^-2 → mm
    @Test
    fun parsePrecipitationFile_kgm2_to_mm() {
        val pDummy = File("p.grib")
        val nc2 = mockk<NetcdfFile>(relaxed = true)
        mockkStatic(NetcdfFiles::class)
        every { NetcdfFiles.open(pDummy.absolutePath) } returns nc2

        val pVar = mockk<Variable>()
        val latVar = mockk<Variable>()
        val lonVar = mockk<Variable>()
        every { nc2.findVariable("Total_precipitation_height_above_ground") } returns pVar
        every { nc2.findVariable("lat") } returns latVar
        every { nc2.findVariable("lon") } returns lonVar

        every { pVar.getDimensions() }   returns ImmutableList.of<Dimension>()
        every { latVar.getDimensions() } returns ImmutableList.of<Dimension>()
        every { lonVar.getDimensions() } returns ImmutableList.of<Dimension>()


        every { pVar.getUnitsString() } returns "kg m^-2"

        // lat=1, lon=2
        listOf(latVar to 1f, lonVar to 2f).forEach { (v, f) ->
            val arr = mockk<Array>()
            every { v.read() } returns arr
            every { arr.reduce() } returns arr
            every { arr.storage } returns floatArrayOf(f)
        }

        // cum=5 @t=1, prev=2 @t=0 → delta=3
        val data4d = mockk<ArrayFloat.D4>()
        val idx4 = mockk<Index4D>(relaxed = true)
        every { data4d.index } returns idx4
        every { data4d.getFloat(idx4) } returnsMany listOf(5f, 2f)
        every { pVar.read() } returns data4d

        // run parser
        val pts = GribParser().parsePrecipitationFile(pDummy, timeIndex = 1, levelIndex = 0)

        assertEquals(1, pts.size)
        val pt = pts[0]
        assertEquals(1.0, pt.lat, 1e-6)
        assertEquals(2.0, pt.lon, 1e-6)
        assertEquals(3.0, pt.precipitation, 1e-6)
    }
}
