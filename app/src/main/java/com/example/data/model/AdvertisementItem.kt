package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AdvertisementItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userAvatar: String = "",
    val campaignName: String = "",
    val campaignGoal: String = "Website Visits", // Website Visits, Page Promotion, Messages / Chat, Post Engagement, Brand Awareness
    val headline: String = "",
    val description: String = "",
    val mediaUrl: String = "",
    val destinationUrl: String = "",
    val callToAction: String = "Learn More", // Learn More, Shop Now, Sign Up, Contact Us, Send Message, Visit Page
    val targetLocation: String = "All Bangladesh",
    val targetAgeRange: String = "18 - 65+",
    val targetGender: String = "All", // All, Men, Women
    val dailyBudget: Double = 100.0,
    val durationDays: Int = 5,
    val totalBudget: Double = 500.0,
    val status: String = "PENDING", // PENDING, RUNNING, PAUSED, COMPLETED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long = 0L,
    val adminNote: String = "",
    val estimatedReach: Int = 5000,
    val estimatedClicks: Int = 350,
    val impressions: Int = 0,
    val clicks: Int = 0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "userName" to userName,
        "userEmail" to userEmail,
        "userPhone" to userPhone,
        "userAvatar" to userAvatar,
        "campaignName" to campaignName,
        "campaignGoal" to campaignGoal,
        "headline" to headline,
        "description" to description,
        "mediaUrl" to mediaUrl,
        "destinationUrl" to destinationUrl,
        "callToAction" to callToAction,
        "targetLocation" to targetLocation,
        "targetAgeRange" to targetAgeRange,
        "targetGender" to targetGender,
        "dailyBudget" to dailyBudget,
        "durationDays" to durationDays,
        "totalBudget" to totalBudget,
        "status" to status,
        "createdAt" to createdAt,
        "approvedAt" to approvedAt,
        "adminNote" to adminNote,
        "estimatedReach" to estimatedReach,
        "estimatedClicks" to estimatedClicks,
        "impressions" to impressions,
        "clicks" to clicks
    )
}
