package no.uio.ifi.in2000.team46.presentation.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.domain.ais.VesselTypes

@Composable
fun TrafficLayerMenu(
    isChecked: Boolean,
    selectedTypes: Set<Int>,
    isLoading: Boolean,
    onToggleLayer: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onToggleType: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f)

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Tilbake")
        }

        LayerToggleRow(
            label = "All Båttraffik",
            checked = isChecked,
            onCheckedChange = {
                onToggleLayer(it)
                if (it) onSelectAll() else onClearAll()
            },
            modifier = Modifier
                .clickable { expanded = !expanded }
                .fillMaxWidth(),
            trailing = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(arrowRotation)
                )
                if (isChecked) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp).padding(start = 8.dp)
                        )
                    } else {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            }
        )

        AnimatedVisibility(visible = expanded) {
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
                            checked = isChecked && selectedTypes.contains(type),
                            onCheckedChange = {
                                if (!isChecked) {
                                    onToggleLayer(true)
                                    onClearAll()
                                }
                                onToggleType(type)
                            }
                        )
                    }
                }
            }
        }
    }
}