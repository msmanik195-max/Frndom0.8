package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class AuthRepository(
    private val context: Context? = null
) {
    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("frndom_auth_prefs", Context.MODE_PRIVATE)
    }

    private fun ensureFirebaseInitialized() {
        try {
            if (FirebaseApp.getApps(context ?: com.google.firebase.FirebaseApp.getInstance().applicationContext).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDbcKf04i6AshXBO0kpmeCNBbkytEo-KwU")
                    .setApplicationId("1:426440213847:android:4f04cda3bbfddf3bb56a12")
                    .setProjectId("frndom-e3f3b")
                    .setDatabaseUrl("https://frndom-e3f3b-default-rtdb.firebaseio.com")
                    .setStorageBucket("frndom-e3f3b.firebasestorage.app")
                    .build()
                if (context != null) {
                    FirebaseApp.initializeApp(context, options)
                }
            }
        } catch (_: Throwable) {}
    }

    private val auth: FirebaseAuth?
        get() {
            return try {
                ensureFirebaseInitialized()
                FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                Log.w("AuthRepository", "FirebaseAuth instance initialization warning: ${e.message}")
                null
            }
        }

    private val realtimeDb: FirebaseDatabase?
        get() {
            return try {
                ensureFirebaseInitialized()
                FirebaseDatabase.getInstance("https://frndom-e3f3b-default-rtdb.firebaseio.com")
            } catch (e: Throwable) {
                try {
                    FirebaseDatabase.getInstance()
                } catch (ex: Throwable) {
                    Log.w("AuthRepository", "FirebaseDatabase instance initialization warning: ${ex.message}")
                    null
                }
            }
        }

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (e: Throwable) {
            null
        }

    /**
     * Sanitizes and normalizes phone numbers
     */
    fun sanitizePhoneNumber(raw: String): String {
        return raw.trim().replace(" ", "").replace("-", "")
    }

    /**
     * Sanitizes email addresses
     */
    fun sanitizeEmail(raw: String): String {
        return raw.trim().lowercase(Locale.getDefault())
    }

    /**
     * Generates an internal email address for phone-based auth
     */
    private fun getInternalAuthEmailForPhone(phone: String): String {
        val cleanPhone = sanitizePhoneNumber(phone).replace("+", "p")
        return "user_${cleanPhone}@frndom.internal"
    }

    /**
     * Registers a new user with either Email or Phone, syncing with Firebase Auth,
     * Realtime Database, and Local storage.
     */
    suspend fun registerUser(
        firstName: String,
        lastName: String,
        identifierType: String,
        identifierValue: String,
        gender: String,
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        password: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val sanitizedFirstName = firstName.trim()
            val sanitizedLastName = lastName.trim()
            val fullName = "$sanitizedFirstName $sanitizedLastName".trim()
            val formattedBirthDate = String.format(Locale.US, "%02d/%02d/%04d", birthDay, birthMonth, birthYear)

            val authEmail: String
            val userEmail: String
            val userPhone: String

            if (identifierType == "phone") {
                userPhone = sanitizePhoneNumber(identifierValue)
                userEmail = ""
                authEmail = getInternalAuthEmailForPhone(userPhone)
            } else {
                userEmail = sanitizeEmail(identifierValue)
                userPhone = ""
                authEmail = userEmail
            }

            var uid: String = ""

            // Try Firebase Auth
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth.createUserWithEmailAndPassword(authEmail, password).await()
                    val user = authResult.user
                    if (user != null) {
                        uid = user.uid
                    } else {
                        throw Exception("Firebase Auth did not return a user.")
                    }
                } catch (e: Throwable) {
                    if (e.message?.contains("already in use", ignoreCase = true) == true) {
                        throw Exception("An account already exists with this email/phone.")
                    }
                    throw Exception("Firebase Auth error: ${e.message}")
                }
            } else {
                throw Exception("Firebase is not configured. Please check your google-services.json.")
            }

            val profile = UserProfile(
                uid = uid,
                firstName = sanitizedFirstName,
                lastName = sanitizedLastName,
                fullName = fullName,
                identifierType = identifierType,
                email = userEmail,
                phoneNumber = userPhone,
                gender = gender,
                birthDay = birthDay,
                birthMonth = birthMonth,
                birthYear = birthYear,
                formattedBirthDate = formattedBirthDate,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )

            // Sync to Realtime Database (both users and admin_users nodes)
            try {
                realtimeDb?.getReference("users")
                    ?.child(uid)
                    ?.setValue(profile.toMap())
                    ?.await()
                realtimeDb?.getReference("admin_users")
                    ?.child(uid)
                    ?.setValue(profile.toMap())
                Log.d("AuthRepository", "Profile synced to Realtime Database: $uid")
            } catch (e: Throwable) {
                Log.w("AuthRepository", "Realtime DB write notice: ${e.message}")
            }

            Result.success(profile)
        } catch (e: Throwable) {
            Log.e("AuthRepository", "Registration error", e)
            Result.failure(Exception(e.message ?: "Registration failed. Please try again."))
        }
    }

    /**
     * Logs in a user using Email or Phone Number and password.
     */
    suspend fun loginUser(
        identifier: String,
        password: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val cleanIdentifier = identifier.trim()
            val isEmail = cleanIdentifier.contains("@")

            val authEmail = if (isEmail) {
                sanitizeEmail(cleanIdentifier)
            } else {
                getInternalAuthEmailForPhone(cleanIdentifier)
            }

            var profile: UserProfile? = null
            var uid: String? = null

            // 1. Try Firebase Auth
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth.signInWithEmailAndPassword(authEmail, password).await()
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        uid = firebaseUser.uid
                    } else {
                        throw Exception("Firebase Auth did not return a user.")
                    }
                } catch (e: Throwable) {
                    if (e.message?.contains("invalid-credential", ignoreCase = true) == true) {
                        throw Exception("Invalid email/phone or password.")
                    }
                    throw Exception("Firebase Auth error: ${e.message}")
                }
            } else {
                throw Exception("Firebase is not configured. Please check your google-services.json.")
            }

            // 2. Fetch from Realtime Database
            if (uid != null) {
                try {
                    val snapshot = realtimeDb?.getReference("users")?.child(uid)?.get()?.await()
                    if (snapshot != null && snapshot.exists()) {
                        profile = snapshot.getValue(UserProfile::class.java)
                    }
                } catch (e: Throwable) {
                    Log.w("AuthRepository", "Realtime DB fetch notice: ${e.message}")
                }

                if (profile == null) {
                    try {
                        val adminSnap = realtimeDb?.getReference("admin_users")?.child(uid)?.get()?.await()
                        if (adminSnap != null && adminSnap.exists()) {
                            profile = adminSnap.getValue(UserProfile::class.java)
                        }
                    } catch (_: Throwable) {}
                }
            }

            if (profile == null) {
                // Fallback to local profile if available
                if (context != null && uid != null) {
                    profile = UserRepository(context).getLocalUserProfile(uid)
                }
            }

            if (profile == null) {
                throw Exception("Account not found or incorrect credentials.")
            }

            val finalProfile = if (context != null) {
                val userRepo = UserRepository(context)
                val enriched = userRepo.enrichProfileWithVerification(profile)
                userRepo.saveLocalUserProfile(enriched)
                enriched
            } else {
                profile
            }

            Result.success(finalProfile)
        } catch (e: Throwable) {
            Log.e("AuthRepository", "Login error", e)
            Result.failure(Exception(e.message ?: "Login failed. Please check your credentials."))
        }
    }

    /**
     * Fetches current user profile from remote
     */
    suspend fun fetchCurrentUserProfile(): UserProfile? = withContext(Dispatchers.IO) {
        val uid = currentUser?.uid
        if (uid != null) {
            var profile: UserProfile? = null
            try {
                val snapshot = realtimeDb?.getReference("users")?.child(uid)?.get()?.await()
                if (snapshot != null && snapshot.exists()) {
                    profile = snapshot.getValue(UserProfile::class.java)
                }
            } catch (_: Throwable) {}

            if (profile == null) {
                try {
                    val adminSnap = realtimeDb?.getReference("admin_users")?.child(uid)?.get()?.await()
                    if (adminSnap != null && adminSnap.exists()) {
                        profile = adminSnap.getValue(UserProfile::class.java)
                    }
                } catch (_: Throwable) {}
            }

            if (profile == null && context != null) {
                profile = UserRepository(context).getLocalUserProfile(uid)
            }

            if (profile != null && context != null) {
                val userRepo = UserRepository(context)
                val enriched = userRepo.enrichProfileWithVerification(profile)
                userRepo.saveLocalUserProfile(enriched)
                return@withContext enriched
            }

            return@withContext profile
        }
        null
    }

    /**
     * Signs out the user
     */
    fun logout() {
        try {
            auth?.signOut()
        } catch (_: Throwable) {}
    }
}
