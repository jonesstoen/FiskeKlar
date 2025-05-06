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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.domain.ais.VesselTypes
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel

@Composable
fun LayerFilterButton(
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel,
    waveViewModel: WaveViewModel,
    precipitationViewModel: PrecipitationViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var aisExpanded by remember { mutableStateOf(false) }
    var weatherExpanded by remember { mutableStateOf(false) }

    val isAisLayerVisible by aisViewModel.isLayerVisible.collectAsState()
    val isMetAlertsLayerVisible by metAlertsViewModel.isLayerVisible.collectAsState()
    val isWindLayerVisible by gribViewModel.isLayerVisible.collectAsState()
    val isCurrentLayerVisible by currentViewModel.isLayerVisible.collectAsState()
    val isDriftLayerVisible by driftViewModel.isLayerVisible.collectAsState()
    val isWaveLayerVisible by waveViewModel.isLayerVisible.collectAsState()
    val isPrecipitationVisible by precipitationViewModel.isLayerVisible.collectAsState()

    val selectedVesselTypes by aisViewModel.selectedVesselTypes.collectAsState()
    val isAisLoading by aisViewModel.isLoading.collectAsState()

    val isAnyWeatherOn = listOf(
        isWindLayerVisible,
        isCurrentLayerVisible,
        isDriftLayerVisible,
        isWaveLayerVisible,
        isPrecipitationVisible
    ).any { it }

    Box(modifier = modifier) {
        // Dismiss overlay
        if (expanded) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expanded = false
                        aisExpanded = false
                        weatherExpanded = false
                    }
            )
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .zIndex(2f)
        ) {
            Icon(Icons.Default.Layers, contentDescription = "Filter Layers")
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 80.dp)
                .zIndex(3f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = {
                        metAlertsViewModel.toggleLayerVisibility()
                        aisExpanded = false
                        weatherExpanded = false
                    }) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = "MetAlerts",
                            tint = if (isMetAlertsLayerVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        aisExpanded = !aisExpanded
                        if (aisExpanded) weatherExpanded = false
                    }) {
                        Icon(
                            Icons.Default.DirectionsBoat,
                            contentDescription = "AIS Fartøyposisjoner",
                            tint = if (isAisLayerVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        weatherExpanded = !weatherExpanded
                        if (weatherExpanded) aisExpanded = false
                    }) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = "Værfilter",
                            tint = if (isAnyWeatherOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // AIS-submeny med velg/fjern alle og indikator for scroll, utransparent
        AnimatedVisibility(
            visible = aisExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 80.dp, bottom = 140.dp)
                .width(200.dp)
                .heightIn(max = 300.dp)
                .zIndex(4f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                val listState = rememberLazyListState()
                Box {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            // Hvis ingen er valgt → Velg alle, ellers Fjern alle
                            val anySelected = selectedVesselTypes.isNotEmpty()
                            TextButton(
                                onClick = {
                                    if (anySelected) {
                                        aisViewModel.clearSelectedVesselTypes()
                                    } else {
                                        aisViewModel.activateLayer()
                                        aisViewModel.selectAllVesselTypes()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (anySelected) "Fjern alle" else "Velg alle")
                            }
                            Divider()
                        }
                        items(VesselTypes.ALL_TYPES.toList()) { (name, type) ->
                            LayerToggleRow(
                                label = name,
                                checked = isAisLayerVisible && selectedVesselTypes.contains(type),
                                onCheckedChange = { checked ->
                                    if (!isAisLayerVisible) {
                                        aisViewModel.activateLayer()
                                        aisViewModel.clearSelectedVesselTypes()
                                    }
                                    aisViewModel.toggleVesselType(type)
                                }
                            )
                        }
                        item {
                            if (isAisLayerVisible) {
                                if (isAisLoading) CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(top = 8.dp)
                                ) else TextButton(
                                    onClick = { aisViewModel.refreshVesselPositions() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Oppdater posisjoner")
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.surface, Color.Transparent)
                                )
                            )
                    )
                }
            }
        }

        // Vær-submeny scrollable, utransparent
        AnimatedVisibility(
            visible = weatherExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 144.dp, bottom = 140.dp)
                .width(220.dp)
                .heightIn(max = 300.dp)
                .zIndex(4f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                val weatherState = rememberLazyListState()
                Box {
                    LazyColumn(
                        state = weatherState,
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            LayerToggleRow(
                                label = "Vind",
                                checked = isWindLayerVisible,
                                onCheckedChange = { checked -> if (checked) gribViewModel.activateLayer() else gribViewModel.deactivateLayer() }
                            )
                        }
                        item {
                            LayerToggleRow(
                                label = "Strømmer",
                                checked = isCurrentLayerVisible,
                                onCheckedChange = { checked -> currentViewModel.toggleLayerVisibility() }
                            )
                        }
                        item {
                            LayerToggleRow(
                                label = "Drift",
                                checked = isDriftLayerVisible,
                                onCheckedChange = { checked -> driftViewModel.toggleLayerVisibility() }
                            )
                        }
                        item {
                            LayerToggleRow(
                                label = "Bølger",
                                checked = isWaveLayerVisible,
                                onCheckedChange = { checked -> waveViewModel.toggleLayer() }
                            )
                        }
                        item {
                            LayerToggleRow(
                                label = "Nedbør",
                                checked = isPrecipitationVisible,
                                onCheckedChange = { checked -> precipitationViewModel.toggleLayerVisibility() }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.surface, Color.Transparent)
                                )
                            )
                    )
                }
            }
        }
    }
}