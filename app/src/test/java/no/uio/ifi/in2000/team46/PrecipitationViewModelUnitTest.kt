package no.uio.ifi.in2000.team46

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import no.uio.ifi.in2000.team46.domain.grib.PrecipitationPoint
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.repository.Result
import org.junit.*
import org.junit.Assert.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel

// DISCLAIMER: ChatGPT-4o was used to create this test class.
@OptIn(ExperimentalCoroutinesApi::class)
class PrecipitationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: GribRepository
    private lateinit var viewModel: PrecipitationViewModel

    @Before
    fun setup() {
        // main dispatcher for coroutine testing
        Dispatchers.setMain(testDispatcher)
        // mock repository and initialize ViewMode
        repo = mockk()
        viewModel = PrecipitationViewModel(repo)
    }

    @Before
    fun mockAndroidLog() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @After
    fun unmockAndroidLog() {
        unmockkStatic(Log::class)
    }


    @Test
    fun `setPrecipThreshold updates threshold value`() {
        // Act: set a new precipitation threshold
        viewModel.setPrecipThreshold(10.0)
        // Assert: threshold state should reflect the new value
        assertEquals(10.0, viewModel.precipThreshold.value, 0.01)
    }

    @Test
    fun `toggleLayerVisibility activates layer and calls fetch`() = runTest {
        // Arrange: stub repository to return empty data
        coEvery { repo.getPrecipitationData() } returns Result.Success(emptyList())

        // Precondition: layer should start hidden
        assertFalse(viewModel.isLayerVisible.value)
        // Act: toggle visibility
        viewModel.toggleLayerVisibility()

        // Assert: layer is visible and data fetch is invoked
        assertTrue(viewModel.isLayerVisible.value)
        coVerify { repo.getPrecipitationData() }
    }

    @Test
    fun `deactivateLayer turns off layer and clears data`() {
        // Act: deactivate the precipitation layer
        viewModel.deactivateLayer()
        // Assert: layer visibility is false and data is cleared
        assertFalse(viewModel.isLayerVisible.value)
        assertNull(viewModel.data.value)
    }

    @Test
    fun `fetch stores successful result in data`() = runTest {
        // Arrange: sample precipitation data
        val sampleData = listOf(PrecipitationPoint(60.0, 10.0, 3.0, 10))
        coEvery { repo.getPrecipitationData() } returns Result.Success(sampleData)

        // Act: fetch data by toggling layer visibility
        viewModel.toggleLayerVisibility()

        // Assert: data state should hold the fetched result
        assertTrue(viewModel.data.value is Result.Success)
        assertEquals(sampleData, (viewModel.data.value as Result.Success).data)
    }
}
