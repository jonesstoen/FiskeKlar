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

// summary: provides a dropdown menu for selecting a fish type and offers a dialog to add a new type
// main function: displays current selection, handles expanding list of options, and shows input dialog for adding types

//WARNINGS: the warning in this fole is beause of a dprecated modifier, which we didnt realize. and refactoring at this point would be hard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishTypeDropdown(
    fishTypes: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onAddNew: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // this state controls whether the dropdown menu is open
    var expanded by remember { mutableStateOf(false) }
    // this state controls whether the add-new dialog is visible
    var showDialog by remember { mutableStateOf(false) }
    // this state holds the user input for a new fish type
    var newFishType by remember { mutableStateOf("") }

    if (showDialog) {
        // show dialog for entering a new fish type
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    // only add if input is not blank
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
                // cancel adding new type
                TextButton(onClick = { showDialog = false }) {
                    Text("Avbryt")
                }
            },
            title = { Text("Ny fisketype") },
            text = {
                // input field for new fish type name
                OutlinedTextField(
                    value = newFishType,
                    onValueChange = { newFishType = it },
                    label = { Text("Navn på fisketype") }
                )
            }
        )
    }

    // container that ties text field and dropdown menu together
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        // read-only text field showing current selection
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fisketype") },
            trailingIcon = {
                // rotate icon based on expansion state
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

        // dropdown list of existing fish types and option to add new
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
                        // display each fish type option
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    },
                    onClick = {
                        // handle selection and close menu
                        onSelect(type)
                        expanded = false
                    }
                )
            }
            // divider between options and add-new action
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Legg til ny fisketype...") },
                onClick = {
                    // open dialog for adding new type
                    expanded = false
                    showDialog = true
                }
            )
        }
    }
}
