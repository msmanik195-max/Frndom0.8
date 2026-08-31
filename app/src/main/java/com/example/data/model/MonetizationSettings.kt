package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MonetizationSettings(
    val reelRatePer1000: Double = 0.5,
    val imageRatePer1000: Double = 0.2,
    val textRatePer1000: Double = 0.1,
    val minTransferAmount: Double = 5.0,
    
    // Creator Fund Requirements
    val reqTotalViews: Int = 500,
    val reqTotalFollowers: Int = 100,
    val reqTotalPosts: Int = 10,
    val reqTotalReels: Int = 5,
    val reqAccountAgeDays: Int = 7,
    
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "reelRatePer1000" to reelRatePer1000,
            "imageRatePer1000" to imageRatePer1000,
            "textRatePer1000" to textRatePer1000,
            "minTransferAmount" to minTransferAmount,
            "reqTotalViews" to reqTotalViews,
            "reqTotalFollowers" to reqTotalFollowers,
            "reqTotalPosts" to reqTotalPosts,
            "reqTotalReels" to reqTotalReels,
            "reqAccountAgeDays" to reqAccountAgeDays,
            "updatedAt" to updatedAt
        )
    }
}

