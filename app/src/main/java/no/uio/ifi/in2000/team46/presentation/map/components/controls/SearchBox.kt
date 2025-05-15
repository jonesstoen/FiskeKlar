package no.uio.ifi.in2000.team46.presentation.map.components.controls


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import no.uio.ifi.in2000.team46.data.remote.api.Feature
import org.maplibre.android.maps.MapLibreMap
import androidx.compose.ui.Alignment

/**
 * this  composable component implements stadia maps search box for maplibre
 * this component adds a search box in the top-left corner of the map
 * and handles the search functionality
 */
@Composable
fun SearchBox(
    map: MapLibreMap,
    searchResults: List<Feature>,
    onSearch: (String) -> Unit,
    onResultSelected: (Feature) -> Unit,
    isSearching: Boolean = false,
    isShowingHistory: Boolean = false,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit

) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var searchText by remember { mutableStateOf("") }
    var isInSearchMode by remember { mutableStateOf(false) }
    var shouldShowHistory by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) } // new state variable

    // state that indicates if the search box is currently focused.
    var isSearchBoxFocused by remember { mutableStateOf(false) }

    // update map interaction based on focus state
    LaunchedEffect(isSearchBoxFocused) {
        map.uiSettings.setAllGesturesEnabled(!isSearchBoxFocused)
    }

    // show search history when the text field is focused and no text is present
    LaunchedEffect(isSearchBoxFocused, searchText) {
        if (isSearchBoxFocused && searchText.isEmpty() && (isShowingHistory || hasSearched)) {
            shouldShowHistory = true
            onSearch("")
        } else {
            shouldShowHistory = false
        }
    }

    val showResults = (searchText.isNotEmpty() && searchResults.isNotEmpty()) ||
            (isSearchBoxFocused && searchText.isEmpty() && shouldShowHistory)

    val outerBoxModifier = if (isInSearchMode) {
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isInSearchMode = false
                        focusManager.clearFocus()
                        isSearchBoxFocused = false
                    }
                )
            }
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = outerBoxModifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // stop event propagation
                }
        ) {
            Surface(
                shadowElevation = 8.dp,
                shape = if (showResults) RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ) else RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // enter search mode when clicking on the search box
                    if (!isInSearchMode) {
                        isInSearchMode = true
                        focusRequester.requestFocus()
                        isSearchBoxFocused = true // update the focus state here
                    }
                }
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = searchText,
                            onValueChange = { newText ->
                                searchText = newText
                                if (newText.isEmpty()) {
                                    if (isSearchBoxFocused && (isShowingHistory || hasSearched)) {
                                        shouldShowHistory = true
                                        onSearch("")
                                    } else {
                                        shouldShowHistory = false
                                    }
                                } else {
                                    shouldShowHistory = false
                                    onSearch(newText)
                                    hasSearched = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .focusRequester(focusRequester)
                                // updated focus change handler to show history when focused
                                .onFocusChanged { focusState ->
                                    isSearchBoxFocused = focusState.isFocused // track focus state
                                    isInSearchMode = focusState.isFocused
                                    if (focusState.isFocused && searchText.isEmpty() && (isShowingHistory || hasSearched)) {
                                        shouldShowHistory = true
                                    }
                                },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchText.isEmpty()) {
                                        Text(
                                            text = "Søk etter sted...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (searchText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        searchText = ""
                                        onSearch("")
                                        shouldShowHistory = true
                                        hasSearched = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Tøm søk",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Søk",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    isInSearchMode = false
                                    isSearchBoxFocused = false
                                    onDismissRequest()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Lukk søkeboks",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                    }

                    // Show a thin line only when results are visible
                    if (showResults) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showResults,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    ),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        if (shouldShowHistory && searchText.isEmpty()) {
                            // Show "Recent Searches" title
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,


                                ) {
                                    Text(
                                        text = "Nylige søk",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        items(searchResults) { feature ->
                            val icon = if (shouldShowHistory && searchText.isEmpty()) {
                                Icons.Default.LocationOn
                            } else if (feature.properties.name.contains("vei", ignoreCase = true) ||
                                feature.properties.name.contains("gate", ignoreCase = true)) {
                                Icons.Default.LocationOn
                            } else {
                                Icons.Default.LocationOn
                            }

                            SearchResultItem(
                                feature = feature,
                                icon = icon,
                                onClick = {
                                    onResultSelected(feature)
                                    hasSearched = true  // Mark that a search has been executed
                                    searchText = ""
                                    isInSearchMode = false
                                    focusManager.clearFocus()
                                    isSearchBoxFocused = false // Also update the focus state here
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    feature: Feature,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.LocationOn,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp, top = 2.dp)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = feature.properties.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                // location information
                val location = buildString {
                    feature.properties.locality?.let { locality ->
                        append(locality)
                    }
                    feature.properties.region?.let { region ->
                        if (isNotEmpty()) append(", ")
                        append(region)
                    }
                }

                if (location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Divider between items
        HorizontalDivider(
            modifier = Modifier.padding(start = 52.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

