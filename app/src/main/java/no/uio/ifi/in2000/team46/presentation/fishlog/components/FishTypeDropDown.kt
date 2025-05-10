package no.uio.ifi.in2000.team46.presentation.fishlog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishTypeDropdown(
    fishTypes: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onAddNew: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var newFishType by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (newFishType.isNotBlank()) {
                        onAddNew(newFishType.trim())
                        showDialog = false
                        newFishType = ""
                    }
                }) {
                    Text("Legg til")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Avbryt")
                }
            },
            title = { Text("Ny fisketype") },
            text = {
                OutlinedTextField(
                    value = newFishType,
                    onValueChange = { newFishType = it },
                    label = { Text("Navn på fisketype") }
                )
            }
        )
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fisketype") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded,
                    Modifier.rotate(if (expanded) 180f else 0f)
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            fishTypes.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    }
                )
            }
            Divider()
            DropdownMenuItem(
                text = { Text("Legg til ny fisketype...") },
                onClick = {
                    expanded = false
                    showDialog = true
                }
            )
        }
    }
}
