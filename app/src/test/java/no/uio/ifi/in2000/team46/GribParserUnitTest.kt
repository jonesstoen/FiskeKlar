package no.uio.ifi.in2000.team46

import android.util.Log
import io.mockk.*
import no.uio.ifi.in2000.team46.data.local.parser.GribParser
import org.junit.Before
import org.junit.After
import org.junit.Test
import ucar.nc2.NetcdfFile
import ucar.nc2.NetcdfFiles
import ucar.nc2.Variable
import ucar.ma2.Array
import ucar.ma2.ArrayFloat
import ucar.ma2.Index4D
import no.uio.ifi.in2000.team46.domain.grib.*
import java.io.File
import ucar.nc2.time.CalendarDateUnit
import kotlin.test.assertFailsWith
import kotlin.math.atan2
import org.junit.Assert.assertEquals

// DISCLAIMER: ChatGPT-4o was used to create this test class.

class GribParserUnitTest {

    private val dummy = File("dummy.grib")
    private lateinit var ncfile: NetcdfFile
    private lateinit var calendarUnit: CalendarDateUnit

    @Before
    fun setUp() {
        // stub Android Log to prevent crashes in JVM tests
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // stub NetcdfFiles.open to return our mocked NetcdfFile
        mockkStatic(NetcdfFiles::class)
        ncfile = mockk(relaxed = true)
        every { NetcdfFiles.open(dummy.absolutePath) } returns ncfile

        // stub CalendarDateUnit to convert hour offsets to timestamps
        mockkStatic(CalendarDateUnit::class)
        calendarUnit = mockk()
        every { CalendarDateUnit.of(any(), any()) } returns calendarUnit
        every { calendarUnit.makeCalendarDate(any<Double>()) } answers {
            val millis = (firstArg<Double>() * 3600 * 1000).toLong()
            mockk {
                every { this@mockk.millis } returns millis
            }
        }
    }

    @After
    fun tearDown() {
        // reset all mocks
        unmockkAll()
    }

    @Test
    fun parseVectorFile_missing_uVar_throws() {
        //  simulate missing u component variable
        every { ncfile.findVariable("uComp") } returns null
        // act & assert: parsing should fail with IllegalStateException
        assertFailsWith<IllegalStateException> {
            GribParser().parseVectorFile(dummy, "uComp", "vComp", VectorType.WIND)
        }
    }

    @Test
    fun parseVectorFile_singlePoint() {
        //  mock required variables
        val uVar = mockk<Variable>()
        val vVar = mockk<Variable>()
        val latVar = mockk<Variable>()
        val lonVar = mockk<Variable>()
        val timeVar = mockk<Variable>()

        every { ncfile.findVariable("uComp") } returns uVar
        every { ncfile.findVariable("vComp") } returns vVar
        every { ncfile.findVariable("lat") } returns latVar
        every { ncfile.findVariable("lon") } returns lonVar
        every { ncfile.findVariable("time") } returns timeVar

        // lat/lon arrays
        listOf(latVar to 10f, lonVar to 20f).forEach { (v, value) ->
            val arr = mockk<Array>()
            every { v.read() } returns arr
            every { arr.reduce() } returns arr
            every { arr.storage } returns floatArrayOf(value)
        }

        // time array
        val timeArr = mockk<Array>()
        every { timeVar.read() } returns timeArr
        every { timeArr.reduce() } returns timeArr
        every { timeArr.storage } returns floatArrayOf(0f)
        every { timeVar.getUnitsString() } returns "hours since 1970-01-01T00:00:00Z"

        // mock vector data
        val uData = mockk<ArrayFloat.D4>()
        val vData = mockk<ArrayFloat.D4>()
        val idx = mockk<Index4D>(relaxed = true)

        every { uData.index } returns idx
        every { vData.index } returns idx
        every { uData.getFloat(ofType(Index4D::class)) } returns 3f
        every { vData.getFloat(ofType(Index4D::class)) } returns 4f
        every { uVar.read() } returns uData
        every { vVar.read() } returns vData

        // parse vector file
        val result = GribParser().parseVectorFile(dummy, "uComp", "vComp", VectorType.WIND)

        // verify single WindVector with correct properties
        assertEquals(1, result.size)
        val vec = result[0] as WindVector

        assertEquals(10.0, vec.lat, 1e-6)
        assertEquals(20.0, vec.lon, 1e-6)
        assertEquals(5.0, vec.speed, 1e-6)
        assertEquals((Math.toDegrees(atan2(3.0, 4.0)) + 360) % 360, vec.direction, 1e-6)
        assertEquals(0L, vec.timestamp)

        // Verify the file was closed
        verify { ncfile.close() }
    }
}




