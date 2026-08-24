package com.example.data.model

data class GroupItem(
    val id: String = "",
    val name: String = "",
    val privacy: String = "Public",
    val description: String = "",
    val coverUrl: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val category: String = "General",
    val membersCount: Int = 1,
    val isBlocked: Boolean = false,
    val isVerified: Boolean = false,
    val badgeType: String = "BLUE", // BLUE or GREEN
    val badgeExpiresAt: Long = 0L, // 0L = Lifetime / No expiry, >0 timestamp
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
        "privacy" to privacy,
        "description" to description,
        "coverUrl" to coverUrl,
        "creatorId" to creatorId,
        "creatorName" to creatorName,
        "category" to category,
        "membersCount" to membersCount,
        "isBlocked" to isBlocked,
        "isVerified" to isVerified,
        "badgeType" to badgeType,
        "badgeExpiresAt" to badgeExpiresAt,
        "createdAt" to createdAt
    )
}
