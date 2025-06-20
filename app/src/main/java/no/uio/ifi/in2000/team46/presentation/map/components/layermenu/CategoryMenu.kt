package no.uio.ifi.in2000.team46.presentation.map.components.layermenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// summary: provides a menu for choosing layer categories and disabling all layers
// main function: display category list items and handle selection or disable all action

@Composable
fun CategoryMenu(
    onCategorySelected: (LayerCategory) -> Unit,
    onDisableAll: () -> Unit
) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "KartLag",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            // button to disable all layers
            TextButton(
                onClick = onDisableAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.VisibilityOff, contentDescription = "disable all", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Skru av alle lag", style = MaterialTheme.typography.bodySmall)
            }
        }

        // list item for the different layer categories
        ListItem(
            headlineContent = { Text("Båttrafikk") },
            leadingContent = { Icon(Icons.Default.DirectionsBoat, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable { onCategorySelected(LayerCategory.TRAFFIC) }
        )
        ListItem(
            headlineContent = { Text("Farevarsler") },
            leadingContent = { Icon(Icons.Default.Warning, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable { onCategorySelected(LayerCategory.WARNINGS) }
        )
        ListItem(
            headlineContent = { Text("Vær Data") },
            leadingContent = { Icon(Icons.Default.Waves, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable { onCategorySelected(LayerCategory.GRIB) }
        )
        ListItem(
            headlineContent = { Text("Favoritter") },
            leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable { onCategorySelected(LayerCategory.FAVORITES) }
        )
    }
}
