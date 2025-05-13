package no.uio.ifi.in2000.team46

import android.util.Log
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import no.uio.ifi.in2000.team46.data.local.database.entities.FishType
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.data.repository.FishTypeRepository
import no.uio.ifi.in2000.team46.presentation.fishlog.viewmodel.FishingLogViewModel
import org.junit.*
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.*

// DISCLAIMER: ChatGPT-4o was used to create this test class.
@OptIn(ExperimentalCoroutinesApi::class)
class FishingLogViewModelTest {

    // test-dispatcher for running coroutines in the test
    private val testDispatcher = UnconfinedTestDispatcher()

    // mocks for repositories
    private lateinit var fishLogRepo: FishLogRepository
    private lateinit var fishTypeRepo: FishTypeRepository

    // viewmodel for the test
    private lateinit var vm: FishingLogViewModel

    @Before
    fun setUp() {
        // Setting the main dispatcher to the test dispatcher
        Dispatchers.setMain(testDispatcher)

        // mock repositories
        fishLogRepo = mockk()
        fishTypeRepo = mockk()

        // mock static Log so we don't get logcat errors
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // prepare repositories' flows
        every { fishLogRepo.getAllLogsFlow() } returns MutableStateFlow(emptyList())
        every { fishTypeRepo.allTypes } returns MutableStateFlow(emptyList())

        // init ViewModel
        vm = FishingLogViewModel(fishLogRepo, fishTypeRepo)
    }

    @After
    fun tearDown() {
        // resetting the main dispatcher
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }


    @Test
    fun `entries initial value is empty list`() {
        assertTrue(vm.entries.value.isEmpty())
    }

    @Test
    fun `fishTypes initial value is empty list`() {
        assertTrue(vm.fishTypes.value.isEmpty())
    }

    @Test
    fun `addEntry calls repository insert with correct FishingLog`() = runTest {
        coEvery { fishLogRepo.insert(any()) } just Runs

        // sample input
        val date = LocalDate.of(2025,5,11)
        val time = LocalTime.of(8,30)
        val loc = "Oslofjorden"
        val type = "Torsk"
        val weight = 2.5
        val notes = "Stor fisk"
        val uri = "content://img/1"
        val lat = 59.9
        val lon = 10.8
        val count = 1

        // adding the sample entry
        vm.addEntry(date, time, loc, type, weight, notes, uri, lat, lon, count)
        advanceUntilIdle()


        // verifying that the repository's insert method was called with the correct FishingLog
        val slot = slot<FishingLog>()
        coVerify(exactly = 1) { fishLogRepo.insert(capture(slot)) }

        val entry = slot.captured
        assertEquals(date.toString(), entry.date)
        assertEquals(time.toString(), entry.time)
        assertEquals(loc, entry.location)
        assertEquals(type, entry.fishType)
        assertEquals(weight, entry.weight, 0.0001)
        assertEquals(notes, entry.notes)
        assertEquals(uri, entry.imageUri)
        assertEquals(lat, entry.latitude, 0.0001)
        assertEquals(lon, entry.longitude, 0.0001)
        assertEquals(count, entry.count)
    }

    @Test
    fun `removeEntry calls repository delete`() = runTest {
        val entry = FishingLog(
            date = "2025-05-11",
            time = "08:30",
            location = "X",
            fishType = "Y",
            weight = 1.0,
            notes = null,
            imageUri = null,
            latitude = 0.0,
            longitude = 0.0,
            count = 1
        )
        coEvery { fishLogRepo.delete(entry) } just Runs

        vm.removeEntry(entry)
        advanceUntilIdle()

        coVerify(exactly = 1) { fishLogRepo.delete(entry) }
    }

    @Test
    fun `addFishType calls repository insert`() = runTest {
        coEvery { fishTypeRepo.insert(any()) } just Runs

        vm.addFishType("Sild")
        advanceUntilIdle()

        val slot = slot<FishType>()
        coVerify { fishTypeRepo.insert(capture(slot)) }
        assertEquals("Sild", slot.captured.name)
    }

    @Test
    fun `deleteAllLogs calls repository deleteAllLogs`() = runTest {
        coEvery { fishLogRepo.deleteAllLogs() } just Runs

        vm.deleteAllLogs()
        advanceUntilIdle()

        coVerify { fishLogRepo.deleteAllLogs() }
    }
}
