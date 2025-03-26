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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.domain.model.ais.VesselTypes
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel


@Composable
fun LayerFilterButton(
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    modifier: Modifier = Modifier
) {
    val TAG = "LayerFilterButton"
    var expanded by remember { mutableStateOf(false) }
    var vesselTypesExpanded by remember { mutableStateOf(false) }

    val isAisLayerVisible by aisViewModel.isLayerVisible.collectAsState()
    val isMetAlertsLayerVisible by metAlertsViewModel.isLayerVisible.collectAsState()
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
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter Layers")
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
                Text("Kartlag", modifier = Modifier.padding(bottom = 8.dp))

                // AIS-lag hovedrad med pil for ekspandering
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Switch(
                        checked = isAisLayerVisible,
                        onCheckedChange = {
                            Log.d(TAG, "Switch clicked, new state will be: ${!isAisLayerVisible}")
                            aisViewModel.toggleLayerVisibility()
                        }
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
                    LazyColumn(
                        modifier = Modifier
                            .padding(start = 32.dp)
                            .fillMaxWidth()
                    ) {
                        items(VesselTypes.ALL_TYPES.toList()) { (name, type) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Switch(
                                    checked = isAisLayerVisible && aisViewModel.isVesselTypeSelected(type),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            // Hvis vi aktiverer en fartøystype og AIS-laget er av,
                                            // aktiver AIS-laget først med bare denne fartøystypen
                                            if (!isAisLayerVisible) {
                                                aisViewModel.selectOnlyVesselType(type)
                                                aisViewModel.activateLayer()
                                            } else {
                                                // Hvis AIS-laget allerede er på, bare toggle denne typen
                                                aisViewModel.toggleVesselType(type)
                                            }
                                        } else {
                                            // Fjern denne fartøystypen
                                            aisViewModel.deselectVesselType(type)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name)
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