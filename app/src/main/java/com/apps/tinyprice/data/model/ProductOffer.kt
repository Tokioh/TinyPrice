package com.apps.tinyprice.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProductOffer(
    val id: String,
    val store: String,
    val productName: String,
    val price: String,
    val originalPrice: String? = null,
    val shipping: String,
    val delivery: String,
    val rating: String,
    val sourceUrl: String,
    val summary: String,
    val tags: List<String>,
    val isBestValue: Boolean = false,
)
