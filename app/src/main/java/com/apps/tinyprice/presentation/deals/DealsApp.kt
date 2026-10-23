package com.apps.tinyprice.presentation.deals

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apps.tinyprice.data.model.ProductOffer
import com.apps.tinyprice.ui.theme.TinyPriceTheme

@Composable
fun DealsApp(state: DealsUiState, onEvent: (DealsEvent) -> Unit) {
    BackHandler(enabled = state.selectedOffer != null) { onEvent(DealsEvent.Back) }
    AnimatedContent(
        targetState = state.selectedOffer,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
        label = "screen",
    ) { offer ->
        if (offer == null) DealsSearchScreen(state, onEvent)
        else DealDetailScreen(offer, onBack = { onEvent(DealsEvent.Back) })
    }
}

@Composable
private fun DealsSearchScreen(state: DealsUiState, onEvent: (DealsEvent) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FishMark()
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("TinyPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text(
                            "Búsqueda web inteligente",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onEvent(DealsEvent.QueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("¿Qué quieres comparar?") },
                    leadingIcon = {
                        Text(
                            "⌕",
                            fontSize = 26.sp,
                            modifier = Modifier.semantics { contentDescription = "Buscar productos" },
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = { onEvent(DealsEvent.SubmitSearch) }) {
                            Text("Buscar", fontWeight = FontWeight.Bold)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onEvent(DealsEvent.SubmitSearch) }),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }
        },
    ) { padding ->
        if (state.hasSearched) {
            SearchResults(
                offers = state.offers,
                query = state.query,
                isLoading = state.isLoading,
                errorMessage = state.errorMessage,
                isPreviewData = state.isPreviewData,
                topPadding = padding.calculateTopPadding(),
                onOpenOffer = { onEvent(DealsEvent.OpenOffer(it)) },
                onRetry = { onEvent(DealsEvent.SubmitSearch) },
            )
        } else {
            DiscoveryHome(
                topPadding = padding.calculateTopPadding(),
                onSuggestion = { onEvent(DealsEvent.UseSuggestion(it)) },
            )
        }
    }
}

@Composable
private fun DiscoveryHome(topPadding: Dp, onSuggestion: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = topPadding + 8.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { HeroCard() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Prueba una búsqueda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Nintendo Switch OLED", "Audífonos", "Laptop gamer").forEach { suggestion ->
                        AssistChip(
                            onClick = { onSuggestion(suggestion) },
                            label = { Text(suggestion) },
                            leadingIcon = { Text("✦") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        )
                    }
                }
            }
        }
        item { HowItWorksCard() }
    }
}

@Composable
private fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            Modifier
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)))
                .padding(24.dp),
        ) {
            Surface(color = Color.White.copy(alpha = .18f), shape = RoundedCornerShape(99.dp)) {
                Text(
                    "DEMO · BÚSQUEDA DE OFERTAS",
                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = .8.sp,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Encuentra la mejor compra sin abrir diez pestañas.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 30.sp,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "TinyPrice compara ofertas, entrega y valor para ayudarte a decidir.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = .9f),
            )
        }
    }
}

@Composable
private fun HowItWorksCard() {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Cómo funcionará", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            val steps = listOf(
                "Busca en la web en tiempo real",
                "Compara precio, envío y disponibilidad",
                "Conserva siempre el enlace a la fuente",
            )
            steps.forEachIndexed { index, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(28.dp), CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
                if (index < steps.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SearchResults(
    offers: List<ProductOffer>,
    query: String,
    isLoading: Boolean,
    errorMessage: String?,
    isPreviewData: Boolean,
    topPadding: Dp,
    onOpenOffer: (ProductOffer) -> Unit,
    onRetry: () -> Unit,
) {
    if (isLoading) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("loading")
                .semantics { liveRegion = LiveRegionMode.Polite }
                .padding(top = topPadding, start = 28.dp, end = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Buscando ofertas…", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Consultando fuentes web.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else if (offers.isEmpty()) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("empty-or-error")
                .semantics { liveRegion = LiveRegionMode.Polite }
                .padding(top = topPadding, start = 28.dp, end = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("◌", fontSize = 42.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(errorMessage ?: "No encontramos ofertas para “$query”", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                if (isPreviewData) "Prueba con Nintendo Switch OLED para recorrer la demo."
                else "Prueba con otro producto o vuelve a intentarlo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = topPadding + 6.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Text("Resultados para “$query”", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "${offers.size} resultados ${if (isPreviewData) "de demostración" else "encontrados en la web"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(offers, key = ProductOffer::id) { offer ->
                OfferCard(offer) { onOpenOffer(offer) }
            }
        }
    }
}

@Composable
private fun OfferCard(offer: ProductOffer, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        offer.store,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(offer.productName, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(offer.price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    offer.originalPrice?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                offer.tags.take(2).forEach { Tag(it, offer.isBestValue && it == "Mejor precio") }
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(offer.shipping, style = MaterialTheme.typography.bodySmall)
                Text("★ ${offer.rating}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(offer.delivery, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Tag(text: String, highlighted: Boolean) {
    Surface(
        shape = RoundedCornerShape(99.dp),
        color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DealDetailScreen(offer: ProductOffer, onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("‹  Volver", fontSize = 16.sp) }
                Text("Detalle de oferta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(offer.store, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(offer.productName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(22.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text(
                        if (offer.price == "Ver en fuente") "Precio por verificar" else "Precio encontrado",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(offer.price, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                    offer.originalPrice?.let {
                        Text("Antes $it", textDecoration = TextDecoration.LineThrough, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        offer.tags.forEach { Tag(it, it == "Mejor precio") }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Por qué aparece como opción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(offer.summary, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
            Spacer(Modifier.height(24.dp))
            DetailLine("Envío", offer.shipping)
            DetailLine("Entrega", offer.delivery)
            DetailLine("Valoración", "★ ${offer.rating} / 5")
            Spacer(Modifier.height(20.dp))
            Text("Fuente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(offer.sourceUrl, color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
            Spacer(Modifier.height(8.dp))
            Text(
                "Cada resultado conserva su fuente para que puedas verificar la oferta directamente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (offer.sourceUrl.startsWith("http://") || offer.sourceUrl.startsWith("https://")) {
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(offer.sourceUrl)))
                    },
                ) {
                    Text("Abrir fuente web")
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun FishMark() {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Text("◒", color = MaterialTheme.colorScheme.onPrimary, fontSize = 25.sp, fontWeight = FontWeight.Black)
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryPreview() {
    TinyPriceTheme { DealsApp(DealsUiState(), {}) }
}
