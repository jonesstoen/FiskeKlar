package no.uio.ifi.in2000.team46.presentation.ui.components


import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel
import androidx.compose.ui.text.font.FontWeight
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud.ForbudViewModel


@Composable
fun LayerFilterButton(
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
    modifier: Modifier = Modifier
) {
    val TAG = "LayerFilterButton"
    var expanded by remember { mutableStateOf(false) }
    var vesselTypesExpanded by remember { mutableStateOf(false) }

    val isAisLayerVisible by aisViewModel.isLayerVisible.collectAsState()
    val isMetAlertsLayerVisible by metAlertsViewModel.isLayerVisible.collectAsState()
    val isForbudLayerVisible by forbudViewModel.isLayerVisible.collectAsState()
    val isLoading by aisViewModel.isLoading.collectAsState()
    val error by aisViewModel.error.collectAsState()
    val selectedVesselTypes by aisViewModel.selectedVesselTypes.collectAsState()

    // Animert rotasjon for pilen
    val vesselTypesArrowRotation by animateFloatAsState(if (vesselTypesExpanded) 180f else 0f)

    Box(modifier = modifier) {
        // Filter-knapp
        FloatingActionButton(
            onClick = {
                expanded = !expanded
            },
            modifier =
            Modifier.align(Alignment.BottomStart)
        ) {
            Icon(imageVector = Icons.Default.Layers , contentDescription = "Filter Layers")
        }

        // Ekspandert filter-panel
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 70.dp, start = 8.dp)
                .zIndex(1f)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(16.dp)
                    .width(300.dp)
            ) {
                Row () {
                    Icon(
                        imageVector = Icons.Default.Layers ,
                        contentDescription = "Filter Layers",
                        modifier = Modifier.padding(end=8.dp)
                    )
                    Text("Kartlag", modifier = Modifier.padding(bottom = 8.dp),fontWeight = FontWeight.Bold)
                }


                // AIS-lag hovedrad med pil for ekspandering
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

                    // Pil for ekspandering av fartøytyper
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
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Oppdater")
                            }
                        }
                    }
                }

                // Vis fartøytype-filtre basert på ekspanderingsstatus, uavhengig av om AIS-laget er aktivert
                AnimatedVisibility(visible = vesselTypesExpanded) {
                    HorizontalDivider(modifier = Modifier.padding(bottom=8.dp))

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
                                    checked = isAisLayerVisible && aisViewModel.isVesselTypeSelected(type),
                                    onCheckedChange = { isChecked ->
                                        if (!isAisLayerVisible) {
                                            aisViewModel.activateLayer()
                                            aisViewModel.clearSelectedVesselTypes() // Clear all first
                                        }
                                        aisViewModel.toggleVesselType(type)
                                    }
                                )
                            }
                        }
                    }
                }
                // MetAlerts-lag hovedrad
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
// Forbudsområder-lag hovedrad
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

                // Vis eventuelle feilmeldinger
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