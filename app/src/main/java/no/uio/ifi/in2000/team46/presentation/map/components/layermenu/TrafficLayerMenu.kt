package no.uio.ifi.in2000.team46.presentation.map.components.layermenu

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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

// summary: shows traffic layer controls with toggleable list of vessel types, refresh and navigation
// main function: allow user to enable boat traffic layer, expand type filters, refresh data, and navigate back

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
    // state for expansion of vessel type list
    var expanded by remember { mutableStateOf(false) }
    // animate arrow rotation based on expansion state
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f)

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // back button to close menu
        TextButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Tilbake")
        }

        // row with master toggle, dropdown arrow, and refresh indicator or button
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
                // dropdown arrow icon rotates
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(arrowRotation)
                )
                if (isChecked) {
                    // show loading spinner or refresh button
                    if (isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp).padding(start = 8.dp)
                        )
                    } else {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "refresh")
                        }
                    }
                }
            }
        )

        // expandable list of vessel types
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                HorizontalDivider()
                // scrollable list with height limit
                LazyColumn(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(VesselTypes.ALL_TYPES.toList()) { (name, type) ->
                        // toggle row for each vessel type
                        LayerToggleRow(
                            label = name,
                            checked = isChecked && selectedTypes.contains(type),
                            onCheckedChange = {
                                // ensure layer is enabled when toggling individual type
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
