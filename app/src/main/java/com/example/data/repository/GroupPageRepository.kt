package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.GroupItem
import com.example.data.model.PageItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class GroupPageRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("frndom_groups_pages", Context.MODE_PRIVATE)

    private val dbRef: DatabaseReference? by lazy {
        try {
            FirebaseDatabase.getInstance().reference
        } catch (e: Throwable) {
            Log.w("GroupPageRepository", "Firebase Database unavailable: ${e.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            null
        }
    }

    private val _pagesFlow = MutableStateFlow<List<PageItem>>(emptyList())
    val pagesFlow: StateFlow<List<PageItem>> = _pagesFlow.asStateFlow()

    private val _groupsFlow = MutableStateFlow<List<GroupItem>>(emptyList())
    val groupsFlow: StateFlow<List<GroupItem>> = _groupsFlow.asStateFlow()

    init {
        loadLocalData()
        listenToFirebase()
    }

    private fun loadLocalData() {
        val pagesJson = prefs.getString("saved_pages", null)
        val defaultPages = if (!pagesJson.isNullOrBlank()) {
            parsePages(pagesJson).filterNot {
                it.id in listOf("page_tech", "page_travel", "page_fitness")
            }
        } else {
            emptyList()
        }
        _pagesFlow.value = defaultPages

        val groupsJson = prefs.getString("saved_groups", null)
        val defaultGroups = if (!groupsJson.isNullOrBlank()) {
            parseGroups(groupsJson).filterNot {
                it.id in listOf("grp_android", "grp_photo", "grp_travel")
            }
        } else {
            emptyList()
        }
        _groupsFlow.value = defaultGroups
    }

    private fun listenToFirebase() {
        // Listen to admin_pages (primary node in Firebase Realtime Database)
        dbRef?.child("admin_pages")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    val list = parsePagesSnapshot(snapshot)
                    _pagesFlow.value = list
                    savePagesLocally(list)
                } else {
                    // Check legacy "pages" node for one-time migration to admin_pages
                    checkAndMigrateLegacyPages()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Listen to admin_groups (primary node in Firebase Realtime Database)
        dbRef?.child("admin_groups")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    val list = parseGroupsSnapshot(snapshot)
                    _groupsFlow.value = list
                    saveGroupsLocally(list)
                } else {
                    // Check legacy "groups" node for one-time migration to admin_groups
                    checkAndMigrateLegacyGroups()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun parsePagesSnapshot(snapshot: DataSnapshot): List<PageItem> {
        val list = mutableListOf<PageItem>()
        for (child in snapshot.children) {
            val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
            if (id.isBlank() || id in listOf("page_tech", "page_travel", "page_fitness")) continue
            val name = child.child("name").getValue(String::class.java) ?: ""
            val category = child.child("category").getValue(String::class.java) ?: "Creator"
            val desc = child.child("description").getValue(String::class.java) ?: ""
            val cover = child.child("coverUrl").getValue(String::class.java) ?: ""
            val avatar = child.child("avatarUrl").getValue(String::class.java) ?: ""
            val creatorId = child.child("creatorId").getValue(String::class.java) ?: ""
            val creatorName = child.child("creatorName").getValue(String::class.java) ?: ""
            val followers = child.child("followersCount").getValue(Int::class.java) ?: 1
            val likes = child.child("likesCount").getValue(Int::class.java) ?: 1
            val isBlocked = child.child("isBlocked").getValue(Boolean::class.java) ?: false
            val isVerified = child.child("isVerified").getValue(Boolean::class.java) ?: false
            val badgeType = child.child("badgeType").getValue(String::class.java) ?: "BLUE"
            val badgeExpiresAt = child.child("badgeExpiresAt").getValue(Long::class.java) ?: 0L
            val website = child.child("website").getValue(String::class.java) ?: ""
            val email = child.child("email").getValue(String::class.java) ?: ""
            val phone = child.child("phone").getValue(String::class.java) ?: ""
            val created = child.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
            list.add(
                PageItem(
                    id = id,
                    name = name,
                    category = category,
                    description = desc,
                    coverUrl = cover,
                    avatarUrl = avatar,
                    creatorId = creatorId,
                    creatorName = creatorName,
                    followersCount = followers,
                    likesCount = likes,
                    isBlocked = isBlocked,
                    isVerified = isVerified,
                    badgeType = badgeType,
                    badgeExpiresAt = badgeExpiresAt,
                    website = website,
                    email = email,
                    phone = phone,
                    createdAt = created
                )
            )
        }
        return list
    }

    private fun parseGroupsSnapshot(snapshot: DataSnapshot): List<GroupItem> {
        val list = mutableListOf<GroupItem>()
        for (child in snapshot.children) {
            val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
            if (id.isBlank() || id in listOf("grp_android", "grp_photo", "grp_travel")) continue
            val name = child.child("name").getValue(String::class.java) ?: ""
            val privacy = child.child("privacy").getValue(String::class.java) ?: "Public"
            val desc = child.child("description").getValue(String::class.java) ?: ""
            val cover = child.child("coverUrl").getValue(String::class.java) ?: ""
            val creatorId = child.child("creatorId").getValue(String::class.java) ?: ""
            val creatorName = child.child("creatorName").getValue(String::class.java) ?: ""
            val category = child.child("category").getValue(String::class.java) ?: "General"
            val members = child.child("membersCount").getValue(Int::class.java) ?: 1
            val isBlocked = child.child("isBlocked").getValue(Boolean::class.java) ?: false
            val isVerified = child.child("isVerified").getValue(Boolean::class.java) ?: false
            val badgeType = child.child("badgeType").getValue(String::class.java) ?: "BLUE"
            val badgeExpiresAt = child.child("badgeExpiresAt").getValue(Long::class.java) ?: 0L
            val created = child.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
            list.add(
                GroupItem(
                    id = id,
                    name = name,
                    privacy = privacy,
                    description = desc,
                    coverUrl = cover,
                    creatorId = creatorId,
                    creatorName = creatorName,
                    category = category,
                    membersCount = members,
                    isBlocked = isBlocked,
                    isVerified = isVerified,
                    badgeType = badgeType,
                    badgeExpiresAt = badgeExpiresAt,
                    createdAt = created
                )
            )
        }
        return list
    }

    private fun checkAndMigrateLegacyPages() {
        try {
            dbRef?.child("pages")?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        val legacyList = parsePagesSnapshot(snapshot)
                        if (legacyList.isNotEmpty()) {
                            _pagesFlow.value = legacyList
                            savePagesLocally(legacyList)
                            // Push each into admin_pages
                            for (page in legacyList) {
                                dbRef?.child("admin_pages")?.child(page.id)?.setValue(page.toMap())
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (_: Exception) {}
    }

    private fun checkAndMigrateLegacyGroups() {
        try {
            dbRef?.child("groups")?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        val legacyList = parseGroupsSnapshot(snapshot)
                        if (legacyList.isNotEmpty()) {
                            _groupsFlow.value = legacyList
                            saveGroupsLocally(legacyList)
                            // Push each into admin_groups
                            for (group in legacyList) {
                                dbRef?.child("admin_groups")?.child(group.id)?.setValue(group.toMap())
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (_: Exception) {}
    }

    fun createPage(page: PageItem) {
        val newPage = if (page.id.isBlank()) page.copy(id = "page_" + UUID.randomUUID().toString().take(8)) else page
        val updated = listOf(newPage) + _pagesFlow.value.filter { it.id != newPage.id }
        _pagesFlow.value = updated
        savePagesLocally(updated)
        try {
            // Write to admin_pages in Firebase Realtime Database
            dbRef?.child("admin_pages")?.child(newPage.id)?.setValue(newPage.toMap())
            // Also write to Firestore admin_pages
            firestore?.collection("admin_pages")?.document(newPage.id)?.set(newPage.toMap(), SetOptions.merge())
        } catch (_: Exception) {}
    }

    fun updatePage(page: PageItem) {
        val updated = _pagesFlow.value.map { if (it.id == page.id) page else it }
        _pagesFlow.value = updated
        savePagesLocally(updated)
        try {
            // Write to admin_pages in Firebase Realtime Database
            dbRef?.child("admin_pages")?.child(page.id)?.setValue(page.toMap())
            // Also write to Firestore admin_pages
            firestore?.collection("admin_pages")?.document(page.id)?.set(page.toMap(), SetOptions.merge())
        } catch (_: Exception) {}
    }

    fun deletePage(pageId: String) {
        val updated = _pagesFlow.value.filter { it.id != pageId }
        _pagesFlow.value = updated
        savePagesLocally(updated)
        try {
            // Remove from admin_pages in Firebase Realtime Database
            dbRef?.child("admin_pages")?.child(pageId)?.removeValue()
            dbRef?.child("pages")?.child(pageId)?.removeValue()
            // Also remove from Firestore admin_pages
            firestore?.collection("admin_pages")?.document(pageId)?.delete()
        } catch (_: Exception) {}
    }

    fun createGroup(group: GroupItem) {
        val newGroup = if (group.id.isBlank()) group.copy(id = "grp_" + UUID.randomUUID().toString().take(8)) else group
        val updated = listOf(newGroup) + _groupsFlow.value.filter { it.id != newGroup.id }
        _groupsFlow.value = updated
        saveGroupsLocally(updated)
        try {
            // Write to admin_groups in Firebase Realtime Database
            dbRef?.child("admin_groups")?.child(newGroup.id)?.setValue(newGroup.toMap())
            // Also write to Firestore admin_groups
            firestore?.collection("admin_groups")?.document(newGroup.id)?.set(newGroup.toMap(), SetOptions.merge())
        } catch (_: Exception) {}
    }

    fun updateGroup(group: GroupItem) {
        val updated = _groupsFlow.value.map { if (it.id == group.id) group else it }
        _groupsFlow.value = updated
        saveGroupsLocally(updated)
        try {
            // Write to admin_groups in Firebase Realtime Database
            dbRef?.child("admin_groups")?.child(group.id)?.setValue(group.toMap())
            // Also write to Firestore admin_groups
            firestore?.collection("admin_groups")?.document(group.id)?.set(group.toMap(), SetOptions.merge())
        } catch (_: Exception) {}
    }

    fun deleteGroup(groupId: String) {
        val updated = _groupsFlow.value.filter { it.id != groupId }
        _groupsFlow.value = updated
        saveGroupsLocally(updated)
        try {
            // Remove from admin_groups in Firebase Realtime Database
            dbRef?.child("admin_groups")?.child(groupId)?.removeValue()
            dbRef?.child("groups")?.child(groupId)?.removeValue()
            // Also remove from Firestore admin_groups
            firestore?.collection("admin_groups")?.document(groupId)?.delete()
        } catch (_: Exception) {}
    }

    // Admin Actions for Groups
    fun setGroupBlocked(groupId: String, isBlocked: Boolean) {
        val current = _groupsFlow.value.find { it.id == groupId } ?: return
        val updated = current.copy(isBlocked = isBlocked)
        updateGroup(updated)
    }

    fun setGroupVerification(groupId: String, isVerified: Boolean, badgeType: String = "BLUE", expiresAt: Long = 0L) {
        val current = _groupsFlow.value.find { it.id == groupId } ?: return
        val updated = current.copy(
            isVerified = isVerified,
            badgeType = badgeType,
            badgeExpiresAt = if (isVerified) expiresAt else 0L
        )
        updateGroup(updated)
    }

    fun adjustGroupBadgeExpiry(groupId: String, addDays: Int) {
        val current = _groupsFlow.value.find { it.id == groupId } ?: return
        val baseTime = if (current.badgeExpiresAt > System.currentTimeMillis()) current.badgeExpiresAt else System.currentTimeMillis()
        val newExpiry = if (addDays == 0) 0L else (baseTime + addDays.toLong() * 24 * 60 * 60 * 1000L).coerceAtLeast(System.currentTimeMillis() + 60000)
        val updated = current.copy(
            isVerified = true,
            badgeExpiresAt = newExpiry
        )
        updateGroup(updated)
    }

    // Admin Actions for Pages
    fun setPageBlocked(pageId: String, isBlocked: Boolean) {
        val current = _pagesFlow.value.find { it.id == pageId } ?: return
        val updated = current.copy(isBlocked = isBlocked)
        updatePage(updated)
    }

    fun setPageVerification(pageId: String, isVerified: Boolean, badgeType: String = "BLUE", expiresAt: Long = 0L) {
        val current = _pagesFlow.value.find { it.id == pageId } ?: return
        val updated = current.copy(
            isVerified = isVerified,
            badgeType = badgeType,
            badgeExpiresAt = if (isVerified) expiresAt else 0L
        )
        updatePage(updated)
    }

    fun adjustPageBadgeExpiry(pageId: String, addDays: Int) {
        val current = _pagesFlow.value.find { it.id == pageId } ?: return
        val baseTime = if (current.badgeExpiresAt > System.currentTimeMillis()) current.badgeExpiresAt else System.currentTimeMillis()
        val newExpiry = if (addDays == 0) 0L else (baseTime + addDays.toLong() * 24 * 60 * 60 * 1000L).coerceAtLeast(System.currentTimeMillis() + 60000)
        val updated = current.copy(
            isVerified = true,
            badgeExpiresAt = newExpiry
        )
        updatePage(updated)
    }

    private fun savePagesLocally(pages: List<PageItem>) {
        val arr = JSONArray()
        for (p in pages) {
            arr.put(JSONObject(p.toMap()))
        }
        prefs.edit().putString("saved_pages", arr.toString()).apply()
    }

    private fun saveGroupsLocally(groups: List<GroupItem>) {
        val arr = JSONArray()
        for (g in groups) {
            arr.put(JSONObject(g.toMap()))
        }
        prefs.edit().putString("saved_groups", arr.toString()).apply()
    }

    private fun parsePages(json: String): List<PageItem> {
        val list = mutableListOf<PageItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PageItem(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", ""),
                        category = obj.optString("category", "Creator"),
                        description = obj.optString("description", ""),
                        coverUrl = obj.optString("coverUrl", ""),
                        avatarUrl = obj.optString("avatarUrl", ""),
                        creatorId = obj.optString("creatorId", ""),
                        creatorName = obj.optString("creatorName", ""),
                        followersCount = obj.optInt("followersCount", 1),
                        likesCount = obj.optInt("likesCount", 1),
                        isBlocked = obj.optBoolean("isBlocked", false),
                        isVerified = obj.optBoolean("isVerified", false),
                        badgeType = obj.optString("badgeType", "BLUE"),
                        badgeExpiresAt = obj.optLong("badgeExpiresAt", 0L),
                        website = obj.optString("website", ""),
                        email = obj.optString("email", ""),
                        phone = obj.optString("phone", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseGroups(json: String): List<GroupItem> {
        val list = mutableListOf<GroupItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    GroupItem(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", ""),
                        privacy = obj.optString("privacy", "Public"),
                        description = obj.optString("description", ""),
                        coverUrl = obj.optString("coverUrl", ""),
                        creatorId = obj.optString("creatorId", ""),
                        creatorName = obj.optString("creatorName", ""),
                        category = obj.optString("category", "General"),
                        membersCount = obj.optInt("membersCount", 1),
                        isBlocked = obj.optBoolean("isBlocked", false),
                        isVerified = obj.optBoolean("isVerified", false),
                        badgeType = obj.optString("badgeType", "BLUE"),
                        badgeExpiresAt = obj.optLong("badgeExpiresAt", 0L),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
