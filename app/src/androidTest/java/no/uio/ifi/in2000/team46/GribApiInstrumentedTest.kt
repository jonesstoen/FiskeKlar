// File: app/src/androidTest/java/no/uio/ifi/in2000/team46/GribApiInstrumentedTest.kt
// Language: Kotlin
package no.uio.ifi.in2000.team46

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.team46.data.repository.GribFilesRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class GribApiInstrumentedTest {

    private val TAG = "GribApiTest"

    @Test
    fun testGribApiReachability() {
        runBlocking {
            val repository = GribFilesRepository()
            val gribData = repository.getCurrentGribFile()
            // Assert that gribData is not null when API is reachable
            assertNotNull("API not reachable or no data received.", gribData)
            Log.i(TAG, "API reachable. Received ${gribData?.size} bytes.")
        }
    }
}