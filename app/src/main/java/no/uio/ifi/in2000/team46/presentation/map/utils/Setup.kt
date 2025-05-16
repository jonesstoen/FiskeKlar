package no.uio.ifi.in2000.team46.presentation.map.utils


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapUiEvent
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.domain.metalerts.Feature as DomainFeature


/**
 * Shows a snackbar for MapUiEvent.ShowAlertSnackbar and triggers callback on action.
 */
@Composable
fun SetupSnackbar(
    uiEvents: Flow<MapUiEvent>,
    snackbarHostState: SnackbarHostState,
    onShowFeature: (DomainFeature) -> Unit
) {
    LaunchedEffect(uiEvents) {
        uiEvents.collect { event ->
            if (event is MapUiEvent.ShowAlertSnackbar) {
                val result = snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = "Vis mer"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onShowFeature(event.feature)
                }
            }
        }
    }
}

/**
 * Resets the selected MetAlert when the bottom sheet is hidden.
 */
@ExperimentalMaterial3Api
@Composable
fun SetupBottomSheetReset(
    scaffoldState: BottomSheetScaffoldState,
    metAlertsViewModel: MetAlertsViewModel
) {
    LaunchedEffect(scaffoldState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .distinctUntilChanged()
            .collect { state ->
                if (state != SheetValue.Expanded) {
                    metAlertsViewModel.selectFeature(null)
                }
            }
    }
}




