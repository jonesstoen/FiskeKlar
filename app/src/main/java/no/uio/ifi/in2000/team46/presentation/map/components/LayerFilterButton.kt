package no.uio.ifi.in2000.team46.presentation.map.components

import android.util.Log
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.domain.ais.VesselTypes
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel

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
    val TAG = "LayerFilterButton"
    var expanded by remember { mutableStateOf(false) }
    var vesselTypesExpanded by remember { mutableStateOf(false) }

    val isAisLayerVisible by aisViewModel.isLayerVisible.collectAsState()
    val isMetAlertsLayerVisible by metAlertsViewModel.isLayerVisible.collectAsState()
    val isForbudLayerVisible by forbudViewModel.isLayerVisible.collectAsState()
    val isWindLayerVisible by gribViewModel.isLayerVisible.collectAsState()
    val isCurrentLayerVisible by currentViewModel.isLayerVisible.collectAsState()
    val isDriftLayerVisible by driftViewModel.isLayerVisible.collectAsState()
    val isWaveLayerVisible by waveViewModel.isLayerVisible.collectAsState()
    val isWaveLoading by waveViewModel.isRasterLoading.collectAsState(initial = false)
    val isLoading by aisViewModel.isLoading.collectAsState()
    val error by aisViewModel.error.collectAsState()
    val selectedVesselTypes by aisViewModel.selectedVesselTypes.collectAsState()
    val isPrecipitationVisible by precipitationViewModel.isLayerVisible.collectAsState()

    // Arrow rotation animation
    val vesselTypesArrowRotation by animateFloatAsState(if (vesselTypesExpanded) 180f else 0f)

    Box(modifier = modifier) {
        // Close overlay
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.0f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { expanded = false }
            )
        }

        // Filter FAB
        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .zIndex(2f)
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
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tonalElevation = 4.dp,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Filter Layers",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Kartlag",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // AIS Layer Row with arrow & refresh
                    LayerToggleRow(
                        label = "AIS Fartøyposisjoner",
                        checked = isAisLayerVisible,
                        onCheckedChange = { checked ->
                            if (checked) {
                                aisViewModel.activateLayer()
                                aisViewModel.selectAllVesselTypes()
                            } else {
                                aisViewModel.deactivateLayer()
                                aisViewModel.clearSelectedVesselTypes()
                            }
                        },
                        modifier = Modifier
                            .clickable { vesselTypesExpanded = !vesselTypesExpanded }
                            .fillMaxWidth(),
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(vesselTypesArrowRotation)
                            )
                            if (isAisLayerVisible) {
                                if (isLoading) CircularProgressIndicator(
                                    Modifier
                                        .size(24.dp)
                                        .padding(start = 8.dp)
                                ) else IconButton(
                                    onClick = { aisViewModel.refreshVesselPositions() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Oppdater"
                                    )
                                }
                            }
                        }
                    )

// Vessel Types list
                    AnimatedVisibility(visible = vesselTypesExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Divider()
                            LazyColumn(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                items(VesselTypes.ALL_TYPES.toList()) { (name, type) ->
                                    LayerToggleRow(
                                        label = name,
                                        checked = isAisLayerVisible && selectedVesselTypes.contains(type),
                                        onCheckedChange = { isChecked ->
                                            if (!isAisLayerVisible) {
                                                aisViewModel.activateLayer()
                                                aisViewModel.clearSelectedVesselTypes()
                                            }
                                            aisViewModel.toggleVesselType(type)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // MetAlerts
                    LayerToggleRow(
                        label = "MetAlerts",
                        checked = isMetAlertsLayerVisible,
                        onCheckedChange = {
                            Log.d(TAG, "MetAlerts Switch clicked, new state=${!isMetAlertsLayerVisible}")
                            metAlertsViewModel.toggleLayerVisibility()
                        }
                    )

                    // Forbudsoner
                    LayerToggleRow(
                        label = "Fiskeforbudsoner",
                        checked = isForbudLayerVisible,
                        onCheckedChange = { forbudViewModel.toggleLayerVisibility() }
                    )

                    // Vind
                    LayerToggleRow(
                        label = "Vindvektorer",
                        checked = isWindLayerVisible,
                        onCheckedChange = { if (it) gribViewModel.activateLayer() else gribViewModel.deactivateLayer() }
                    )

                    // Strøm
                    LayerToggleRow(
                        label = "Strømvektorer",
                        checked = isCurrentLayerVisible,
                        onCheckedChange = { currentViewModel.toggleLayerVisibility() }
                    )

                    // Drift
                    LayerToggleRow(
                        label = "Driftvektorer (vind + strøm)",
                        checked = isDriftLayerVisible,
                        onCheckedChange = { driftViewModel.toggleLayerVisibility() }
                    )
                    LayerToggleRow(
                        label = "Bølgeheatmap",
                        checked = isWaveLayerVisible,
                        onCheckedChange = { waveViewModel.toggleLayer() },
                        trailing = {
                            // Hvis laget er synlig *og* vi laster data → vis spinner
                            if (isWaveLayerVisible && isWaveLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(start = 8.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )

                    // Rain
                    LayerToggleRow(
                        label = "Nedbør",
                        checked = isPrecipitationVisible,
                        onCheckedChange = { precipitationViewModel.toggleLayerVisibility() },
                        trailing = {
                            if (isPrecipitationVisible && precipitationViewModel.isLoading.collectAsState().value) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(start = 8.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )


                    // Error
                    error?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LayerToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.scale(0.7f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}
