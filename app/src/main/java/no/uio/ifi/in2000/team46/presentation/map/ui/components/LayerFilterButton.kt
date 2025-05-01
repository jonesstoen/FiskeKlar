package no.uio.ifi.in2000.team46.presentation.map.ui.components


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
import androidx.compose.material.icons.filled.DirectionsBoatFilled
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.domain.model.ais.VesselTypes
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import androidx.compose.ui.text.font.FontWeight
import no.uio.ifi.in2000.team46.presentation.grib.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel


@Composable
fun LayerFilterButton(
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel,
    modifier: Modifier = Modifier
) {
    val TAG = "LayerFilterButton"
    var expanded by remember { mutableStateOf(false) }
    var vesselTypesExpanded by remember { mutableStateOf(false) }

    val isAisLayerVisible by aisViewModel.isLayerVisible.collectAsState()
    val isMetAlertsLayerVisible by metAlertsViewModel.isLayerVisible.collectAsState()
    val isForbudLayerVisible by forbudViewModel.isLayerVisible.collectAsState()
    val isDriftLayerVisible by driftViewModel.isLayerVisible.collectAsState()
    val windResult by gribViewModel.windData.collectAsState(initial = null)
    val isWindLayerVisible by gribViewModel.isLayerVisible.collectAsState()
    val isCurrentLayerVisible by currentViewModel.isLayerVisible.collectAsState()
    val isLoading by aisViewModel.isLoading.collectAsState()
    val error by aisViewModel.error.collectAsState()
    val selectedVesselTypes by aisViewModel.selectedVesselTypes.collectAsState()


    //animation for arrowrotation
    val vesselTypesArrowRotation by animateFloatAsState(if (vesselTypesExpanded) 180f else 0f)

    // wrapping the content in a box
    Box(modifier = modifier) {

        // when expanded, show a transparent overlay to cover the rest of the screen, in order
        // to close the filter panel when clicking outside of it
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Bruk en gjennomsiktig bakgrunn for å sikre at området tar opp plassen, men er "usynlig"
                    .background(Color.Transparent)
                    // clickable() for å lukke panelet ved trykk utenfor
                    .clickable(
                        // Bruker en tom interactionSource og ingen indikasjon slik at overlayet ikke viser visuell feedback
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        expanded = false
                    }
            )
        }

        // FloatingActionButton has a higher zIndex than the overlay, so it is clickable
        FloatingActionButton(
            onClick = {
                // Toggle expanded state
                expanded = !expanded
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .zIndex(2f)
        ) {
            Icon(imageVector = Icons.Default.Layers, contentDescription = "Filter Layers")
        }


        // Expanded filter panel with higher zIndex so it is interactive and does not receive "close" clicks from the overlay
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 70.dp, start = 8.dp)
                .zIndex(3f)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(16.dp)
                    .width(300.dp)
            ) {

                Row {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Filter Layers",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Kartlag", fontWeight = FontWeight.Bold)
                }


                // ais main row with arrow for expanding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Switch(
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
                        modifier = Modifier.scale(0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AIS Fartøyposisjoner",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { vesselTypesExpanded = !vesselTypesExpanded }
                    )


                    // Arrow for expanding vessel types
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand vessel types",
                        modifier = Modifier
                            .clickable { vesselTypesExpanded = !vesselTypesExpanded }
                            .rotate(vesselTypesArrowRotation)
                    )

                    if (isAisLayerVisible) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                                    .padding(start = 8.dp)
                            )
                        } else {
                            IconButton(onClick = { aisViewModel.refreshVesselPositions() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Oppdater"
                                )
                            }
                        }
                    }
                }


                // Shows vessel type filters if AIS layer is expanded
                AnimatedVisibility(visible = vesselTypesExpanded) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .padding(start = 32.dp)
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        item {
                            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsBoatFilled,
                                    contentDescription = "Filter vessel types",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "FartøyTyper",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        items(VesselTypes.ALL_TYPES.toList()) { (name, type) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(name)
                                Spacer(modifier = Modifier.weight(1f))
                                Switch(
                                    modifier = Modifier.scale(0.7f),
                                    checked = isAisLayerVisible && selectedVesselTypes.contains(type),
                                    onCheckedChange = { isChecked ->
                                        if (!isAisLayerVisible) {
                                            aisViewModel.activateLayer()
                                            aisViewModel.clearSelectedVesselTypes() // clears all other types first
                                        }
                                        aisViewModel.toggleVesselType(type)
                                    }
                                )
                            }
                        }
                    }
                }

                // MetAlerts-layer main row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Switch(
                        modifier = Modifier.scale(0.7f),
                        checked = isMetAlertsLayerVisible,
                        onCheckedChange = {
                            Log.d(TAG, "MetAlerts Switch clicked, new state will be: ${!isMetAlertsLayerVisible}")
                            metAlertsViewModel.toggleLayerVisibility()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "MetAlerts",
                        modifier = Modifier.weight(1f)
                    )
                }

                // forbud-layer main row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Switch(
                        modifier = Modifier.scale(0.7f),
                        checked = isForbudLayerVisible,
                        onCheckedChange = {
                            forbudViewModel.toggleLayerVisibility()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Fiskeforbudsoner",
                        modifier = Modifier.weight(1f)
                    )
                }
                // Wind Layer row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Switch(
                        checked = isWindLayerVisible,
                        onCheckedChange = { checked ->
                            if (checked) gribViewModel.activateLayer() else gribViewModel.deactivateLayer()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vindvektorer", modifier = Modifier.weight(1f))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Switch(
                        checked = isCurrentLayerVisible,
                        onCheckedChange = { checked ->
                            if (checked) currentViewModel.toggleLayerVisibility() else currentViewModel.toggleLayerVisibility()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Strømvektorer", modifier = Modifier.weight(1f))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Switch(
                        modifier = Modifier.scale(0.7f),
                        checked = isDriftLayerVisible,
                        onCheckedChange = {
                            driftViewModel.toggleLayerVisibility()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Driftvektorer (vind + strøm)", modifier = Modifier.weight(1f))
                }

                // error messages for ais
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
