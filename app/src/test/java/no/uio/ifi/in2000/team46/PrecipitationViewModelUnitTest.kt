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
        Dispatchers.setMain(testDispatcher)
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
        viewModel.setPrecipThreshold(10.0)
        assertEquals(10.0, viewModel.precipThreshold.value, 0.01)
    }

    @Test
    fun `toggleLayerVisibility activates layer and calls fetch`() = runTest {
        coEvery { repo.getPrecipitationData() } returns Result.Success(emptyList())

        assertFalse(viewModel.isLayerVisible.value)
        viewModel.toggleLayerVisibility()
        assertTrue(viewModel.isLayerVisible.value)
        coVerify { repo.getPrecipitationData() }
    }

    @Test
    fun `deactivateLayer turns off layer and clears data`() {
        viewModel.deactivateLayer()
        assertFalse(viewModel.isLayerVisible.value)
        assertNull(viewModel.data.value)
    }

    @Test
    fun `fetch stores successful result in data`() = runTest {
        val sampleData = listOf(PrecipitationPoint(60.0, 10.0, 3.0, 10))
        coEvery { repo.getPrecipitationData() } returns Result.Success(sampleData)

        viewModel.toggleLayerVisibility()
        assertTrue(viewModel.data.value is Result.Success)
        assertEquals(sampleData, (viewModel.data.value as Result.Success).data)
    }
}
