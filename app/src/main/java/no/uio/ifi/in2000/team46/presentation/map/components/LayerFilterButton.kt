package no.uio.ifi.in2000.team46.presentation.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.domain.ais.VesselTypes
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.*
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.components.CategoryMenu
import no.uio.ifi.in2000.team46.presentation.map.components.LayerToggleRow

enum class LayerCategory { NONE, TRAFFIC, WARNINGS, GRIB }


@Composable
fun LayerFilterButton(
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel,
    waveViewModel: WaveViewModel,
    precipitationViewModel: PrecipitationViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(LayerCategory.NONE) }

    val isAisLayerVisible by aisViewModel.isLayerVisible.collectAsState()
    val isMetAlertsLayerVisible by metAlertsViewModel.isLayerVisible.collectAsState()
    val isWindLayerVisible by gribViewModel.isLayerVisible.collectAsState()
    val isCurrentLayerVisible by currentViewModel.isLayerVisible.collectAsState()
    val isDriftLayerVisible by driftViewModel.isLayerVisible.collectAsState()
    val isWaveLayerVisible by waveViewModel.isLayerVisible.collectAsState()
    val isPrecipitationVisible by precipitationViewModel.isLayerVisible.collectAsState()
    val selectedVesselTypes by aisViewModel.selectedVesselTypes.collectAsState()
    val isLoading by aisViewModel.isLoading.collectAsState()

    Box(modifier = modifier) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.0f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        expanded = false
                        selectedCategory = LayerCategory.NONE
                    }
            )
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.align(Alignment.BottomStart).zIndex(2f)
        ) {
            Icon(imageVector = Icons.Default.Layers, contentDescription = "Filter Layers")
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 70.dp, start = 8.dp)
                .zIndex(3f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                modifier = Modifier.width(260.dp)
            ) {
                when (selectedCategory) {
                    LayerCategory.NONE -> CategoryMenu(
                        onCategorySelected = { selectedCategory = it },
                        onDisableAll = {
                            aisViewModel.deactivateLayer()
                            aisViewModel.clearSelectedVesselTypes()
                            metAlertsViewModel.deactivateLayer()
                            gribViewModel.deactivateLayer()
                            currentViewModel.deactivateLayer()
                            driftViewModel.deactivateLayer()
                            waveViewModel.deactivateLayer()
                            precipitationViewModel.deactivateLayer()
                        }
                    )

                    LayerCategory.TRAFFIC -> TrafficLayerMenu(
                        isChecked = isAisLayerVisible,
                        selectedTypes = selectedVesselTypes,
                        isLoading = isLoading,
                        onToggleLayer = {
                            if (it) aisViewModel.activateLayer() else aisViewModel.deactivateLayer()
                        },
                        onRefresh = { aisViewModel.refreshVesselPositions() },
                        onToggleType = { aisViewModel.toggleVesselType(it) },
                        onSelectAll = { aisViewModel.selectAllVesselTypes() },
                        onClearAll = { aisViewModel.clearSelectedVesselTypes() },
                        onBack = { selectedCategory = LayerCategory.NONE }
                    )

                    LayerCategory.WARNINGS -> WarningLayerMenu(
                        isChecked = isMetAlertsLayerVisible,
                        onToggle = { metAlertsViewModel.toggleLayerVisibility() },
                        onBack = { selectedCategory = LayerCategory.NONE }
                    )

                    LayerCategory.GRIB -> GribLayerMenu(
                        isWind = isWindLayerVisible,
                        isCurrent = isCurrentLayerVisible,
                        isDrift = isDriftLayerVisible,
                        isWave = isWaveLayerVisible,
                        isPrecip = isPrecipitationVisible,
                        onToggleWind = { if (it) gribViewModel.activateLayer() else gribViewModel.deactivateLayer() },
                        onToggleCurrent = { currentViewModel.toggleLayerVisibility() },
                        onToggleDrift = { driftViewModel.toggleLayerVisibility() },
                        onToggleWave = { waveViewModel.toggleLayer() },
                        onTogglePrecip = { precipitationViewModel.toggleLayerVisibility() },
                        gribViewModel = gribViewModel,
                        waveViewModel = waveViewModel,
                        onBack = { selectedCategory = LayerCategory.NONE }
                    )
                }
            }
        }
    }
}