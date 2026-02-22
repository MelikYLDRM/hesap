package com.melikyldrm.hesap.ui.screens.converter

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melikyldrm.hesap.domain.model.UnitCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Birim Dönüştürücü") },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "Geçmiş")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category selection
            ScrollableTabRow(
                selectedTabIndex = UnitCategory.entries.indexOf(state.selectedCategory),
                edgePadding = 8.dp
            ) {
                UnitCategory.entries.forEach { category ->
                    Tab(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        text = { Text(category.displayName, maxLines = 1) },
                        icon = {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            // Converter content - weight ile kalan alanı kapla
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input
                OutlinedTextField(
                    value = state.inputValue,
                    onValueChange = viewModel::updateInputValue,
                    label = { Text("Değer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 24.sp)
                )

                // From unit selector
                UnitSelector(
                    label = "Kaynak Birim",
                    selectedUnit = state.fromUnit,
                    availableUnits = state.availableUnits,
                    onUnitSelected = viewModel::updateFromUnit
                )

                // Swap button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FilledIconButton(
                        onClick = viewModel::swapUnits,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Birimleri değiştir"
                        )
                    }
                }

                // To unit selector
                UnitSelector(
                    label = "Hedef Birim",
                    selectedUnit = state.toUnit,
                    availableUnits = state.availableUnits,
                    onUnitSelected = viewModel::updateToUnit
                )

                // Result display
                AnimatedVisibility(
                    visible = state.result.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sonuç",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${state.result} ${state.toUnit}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = viewModel::saveConversion
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Geçmişe Kaydet")
                            }
                        }
                    }
                }

                // Error message
                state.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Quick conversion hints
                if (state.inputValue.isEmpty()) {
                    QuickConversionHints(
                        category = state.selectedCategory,
                        onHintClick = { value -> viewModel.updateInputValue(value) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitSelector(
    label: String,
    selectedUnit: String,
    availableUnits: List<Pair<String, String>>,
    onUnitSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedUnitDisplay = availableUnits.find { it.first == selectedUnit }?.second ?: selectedUnit

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = "$selectedUnitDisplay ($selectedUnit)",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableUnits.forEach { (symbol, name) ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name)
                            Text(
                                text = symbol,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onUnitSelected(symbol)
                        expanded = false
                    },
                    leadingIcon = if (symbol == selectedUnit) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun QuickConversionHints(
    category: UnitCategory,
    onHintClick: (String) -> Unit
) {
    val hints = when (category) {
        UnitCategory.LENGTH -> listOf("1", "10", "100", "1000")
        UnitCategory.WEIGHT -> listOf("1", "100", "500", "1000")
        UnitCategory.TEMPERATURE -> listOf("0", "20", "37", "100")
        UnitCategory.DATA -> listOf("1", "100", "1024", "2048")
        else -> listOf("1", "10", "100", "1000")
    }

    Column {
        Text(
            text = "Hızlı Değerler",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hints.forEach { hint ->
                SuggestionChip(
                    onClick = { onHintClick(hint) },
                    label = { Text(hint) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun getCategoryIcon(category: UnitCategory) = when (category) {
    UnitCategory.LENGTH -> Icons.Default.Straighten
    UnitCategory.WEIGHT -> Icons.Default.Scale
    UnitCategory.AREA -> Icons.Default.Square
    UnitCategory.VOLUME -> Icons.Default.LocalDrink
    UnitCategory.TEMPERATURE -> Icons.Default.Thermostat
    UnitCategory.DATA -> Icons.Default.Storage
    UnitCategory.TIME -> Icons.Default.Schedule
    UnitCategory.SPEED -> Icons.Default.Speed
}

