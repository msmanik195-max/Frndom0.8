package com.example.data.model

data class PageItem(
    val id: String = "",
    val name: String = "",
    val category: String = "Creator",
    val description: String = "",
    val coverUrl: String = "",
    val avatarUrl: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val followersCount: Int = 1,
    val likesCount: Int = 1,
    val isBlocked: Boolean = false,
    val isVerified: Boolean = false,
    val badgeType: String = "BLUE", // BLUE or GREEN
    val badgeExpiresAt: Long = 0L, // 0L = Lifetime / No expiry, >0 timestamp
    val website: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isBadgeActive(): Boolean {
        if (!isVerified) return false
        if (badgeExpiresAt == 0L) return true
        return System.currentTimeMillis() <= badgeExpiresAt
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "category" to category,
        "description" to description,
        "coverUrl" to coverUrl,
        "avatarUrl" to avatarUrl,
        "creatorId" to creatorId,
        "creatorName" to creatorName,
        "followersCount" to followersCount,
        "likesCount" to likesCount,
        "isBlocked" to isBlocked,
        "isVerified" to isVerified,
        "badgeType" to badgeType,
        "badgeExpiresAt" to badgeExpiresAt,
        "website" to website,
        "email" to email,
        "phone" to phone,
        "createdAt" to createdAt
    )
}
