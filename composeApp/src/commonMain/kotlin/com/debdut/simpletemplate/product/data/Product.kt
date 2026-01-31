package com.debdut.simpletemplate.product.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Product model matching DummyJSON API response.
 * Used for both list and details screens.
 */
@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    @SerialName("discountPercentage") val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val tags: List<String> = emptyList(),
    val brand: String = "",
    val sku: String? = null,
    val weight: Int? = null,
    val dimensions: ProductDimensions? = null,
    @SerialName("warrantyInformation") val warrantyInformation: String? = null,
    @SerialName("shippingInformation") val shippingInformation: String? = null,
    @SerialName("availabilityStatus") val availabilityStatus: String? = null,
    val reviews: List<ProductReview> = emptyList(),
    @SerialName("returnPolicy") val returnPolicy: String? = null,
    @SerialName("minimumOrderQuantity") val minimumOrderQuantity: Int? = null,
    val meta: ProductMeta? = null,
    val images: List<String> = emptyList(),
    val thumbnail: String? = null,
)

@Serializable
data class ProductDimensions(
    val width: Double,
    val height: Double,
    val depth: Double,
)

@Serializable
data class ProductReview(
    val rating: Int,
    val comment: String,
    val date: String,
    @SerialName("reviewerName") val reviewerName: String,
    @SerialName("reviewerEmail") val reviewerEmail: String,
)

@Serializable
data class ProductMeta(
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    val barcode: String? = null,
    @SerialName("qrCode") val qrCode: String? = null,
)
