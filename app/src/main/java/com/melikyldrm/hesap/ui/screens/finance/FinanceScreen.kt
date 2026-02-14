package com.melikyldrm.hesap.ui.screens.finance

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melikyldrm.hesap.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finansal Hesaplamalar") },
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
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                edgePadding = 16.dp
            ) {
                FinanceTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.title) }
                    )
                }
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (state.selectedTab) {
                    FinanceTab.KDV -> KdvContent(state, viewModel)
                    FinanceTab.TEVKIFAT -> TevkifatContent(state, viewModel)
                    FinanceTab.FAIZ -> FaizContent(state, viewModel)
                    FinanceTab.KAR_ZARAR -> KarZararContent(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun KdvContent(state: FinanceUiState, viewModel: FinanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.kdvAmount,
            onValueChange = viewModel::updateKdvAmount,
            label = { Text("Tutar (₺)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // KDV Rate selection
        Text("KDV Oranı", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KdvRate.entries.forEach { rate ->
                FilterChip(
                    selected = state.kdvRate == rate,
                    onClick = { viewModel.updateKdvRate(rate) },
                    label = { Text(rate.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // KDV Included toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.isKdvIncluded,
                onCheckedChange = { viewModel.toggleKdvIncluded() }
            )
            Text("Girilen tutar KDV dahil")
        }

        Button(
            onClick = viewModel::calculateKdv,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hesapla")
        }

        // Error message
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        // Result
        state.kdvResult?.let { result ->
            KdvResultCard(result)
        }
    }
}

@Composable
private fun KdvResultCard(result: KdvResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Sonuç", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            ResultRow("KDV Hariç Tutar:", String.format("%,.2f ₺", result.baseAmount))
            ResultRow("KDV Oranı:", "%${(result.kdvRate * 100).toInt()}")
            ResultRow("KDV Tutarı:", String.format("%,.2f ₺", result.kdvAmount))
            HorizontalDivider()
            ResultRow(
                "Toplam (KDV Dahil):",
                String.format("%,.2f ₺", result.totalAmount),
                highlight = true
            )
        }
    }
}

@Composable
private fun TevkifatContent(state: FinanceUiState, viewModel: FinanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.tevkifatAmount,
            onValueChange = viewModel::updateTevkifatAmount,
            label = { Text("Matrah (KDV Hariç Tutar) (₺)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("KDV Oranı", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(KdvRate.RATE_8, KdvRate.RATE_18, KdvRate.RATE_20).forEach { rate ->
                FilterChip(
                    selected = state.tevkifatKdvRate == rate,
                    onClick = { viewModel.updateTevkifatKdvRate(rate) },
                    label = { Text(rate.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text("Tevkifat Oranı", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TevkifatRate.entries.forEach { rate ->
                FilterChip(
                    selected = state.tevkifatRate == rate,
                    onClick = { viewModel.updateTevkifatRate(rate) },
                    label = { Text(rate.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = viewModel::calculateTevkifat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hesapla")
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        state.tevkifatResult?.let { result ->
            TevkifatResultCard(result)
        }
    }
}

@Composable
private fun TevkifatResultCard(result: TevkifatResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Sonuç", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            ResultRow("Matrah:", String.format("%,.2f ₺", result.baseAmount))
            ResultRow("Hesaplanan KDV:", String.format("%,.2f ₺", result.kdvAmount))
            ResultRow("Tevkifat Oranı:", result.tevkifatRate)
            HorizontalDivider()
            ResultRow("Tevkifat Tutarı (Alıcı Öder):", String.format("%,.2f ₺", result.tevkifatAmount))
            ResultRow("Satıcının Tahsil Edeceği KDV:", String.format("%,.2f ₺", result.sellerKdv))
            HorizontalDivider()
            ResultRow("Toplam Tutar:", String.format("%,.2f ₺", result.totalAmount), highlight = true)
        }
    }
}

@Composable
private fun FaizContent(state: FinanceUiState, viewModel: FinanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.faizPrincipal,
            onValueChange = viewModel::updateFaizPrincipal,
            label = { Text("Ana Para (₺)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.faizRate,
            onValueChange = viewModel::updateFaizRate,
            label = { Text("Yıllık Faiz Oranı (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.faizTime,
            onValueChange = viewModel::updateFaizTime,
            label = { Text("Süre (Yıl)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.isCompoundInterest,
                onCheckedChange = { viewModel.toggleCompoundInterest() }
            )
            Text("Bileşik Faiz")
        }

        AnimatedVisibility(visible = state.isCompoundInterest) {
            Column {
                Text("Faiz Periyodu", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompoundFrequency.entries.forEach { freq ->
                        FilterChip(
                            selected = state.faizFrequency == freq,
                            onClick = { viewModel.updateFaizFrequency(freq) },
                            label = { Text(freq.displayName) }
                        )
                    }
                }
            }
        }

        Button(
            onClick = viewModel::calculateFaiz,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hesapla")
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        state.faizResult?.let { result ->
            FaizResultCard(result)
        }
    }
}

@Composable
private fun FaizResultCard(result: FaizResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Sonuç", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            ResultRow("Ana Para:", String.format("%,.2f ₺", result.principal))
            ResultRow("Faiz Oranı:", "%${result.rate}")
            ResultRow("Süre:", "${result.time} Yıl")
            ResultRow("Faiz Türü:", if (result.isCompound) "Bileşik" else "Basit")
            HorizontalDivider()
            ResultRow("Faiz Tutarı:", String.format("%,.2f ₺", result.interest))
            ResultRow("Toplam Tutar:", String.format("%,.2f ₺", result.totalAmount), highlight = true)
        }
    }
}

@Composable
private fun KarZararContent(state: FinanceUiState, viewModel: FinanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.karCostPrice,
            onValueChange = viewModel::updateKarCostPrice,
            label = { Text("Maliyet Fiyatı (₺)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.karSellingPrice,
            onValueChange = viewModel::updateKarSellingPrice,
            label = { Text("Satış Fiyatı (₺)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = viewModel::calculateKarZarar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hesapla")
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        state.karResult?.let { result ->
            KarZararResultCard(result)
        }
    }
}

@Composable
private fun KarZararResultCard(result: KarZararResult) {
    val isProfit = result.isProfit
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isProfit) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                if (isProfit) "KÂR" else "ZARAR",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isProfit) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            HorizontalDivider()
            ResultRow("Maliyet:", String.format("%,.2f ₺", result.costPrice))
            ResultRow("Satış:", String.format("%,.2f ₺", result.sellingPrice))
            HorizontalDivider()
            ResultRow(
                if (isProfit) "Kâr Tutarı:" else "Zarar Tutarı:",
                String.format("%,.2f ₺", kotlin.math.abs(result.profitOrLoss))
            )
            ResultRow(
                if (isProfit) "Kâr Oranı:" else "Zarar Oranı:",
                String.format("%.2f%%", kotlin.math.abs(result.percentage)),
                highlight = true
            )
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
        )
    }
}

