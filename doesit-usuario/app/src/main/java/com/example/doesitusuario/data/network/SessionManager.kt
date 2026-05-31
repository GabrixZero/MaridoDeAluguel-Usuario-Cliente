package com.example.doesitusuario.data.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SessionManager {
    private const val PREF_NAME = "secure_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_PHONE = "userPhone"
    private const val KEY_USER_CPF = "userCpf"
    private const val KEY_USER_BIRTH_DATE = "userBirthDate"
    private const val KEY_USER_GENDER = "userGender"
    private const val KEY_ADDRESS_CEP = "addressCep"
    private const val KEY_ADDRESS_STREET = "addressStreet"
    private const val KEY_ADDRESS_NUMBER = "addressNumber"
    private const val KEY_ADDRESS_NEIGHBORHOOD = "addressNeighborhood"
    private const val KEY_ADDRESS_CITY = "addressCity"
    private const val KEY_ADDRESS_STATE = "addressState"
    private const val KEY_RATING = "rating"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        prefs = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var token: String
        get() = prefs?.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_TOKEN, value)?.apply() ?: Unit

    var userId: Long
        get() = prefs?.getLong(KEY_USER_ID, 0L) ?: 0L
        set(value) = prefs?.edit()?.putLong(KEY_USER_ID, value)?.apply() ?: Unit

    var userName: String
        get() = prefs?.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_NAME, value)?.apply() ?: Unit

    var userEmail: String
        get() = prefs?.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_EMAIL, value)?.apply() ?: Unit

    var userPhone: String
        get() = prefs?.getString(KEY_USER_PHONE, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_PHONE, value)?.apply() ?: Unit

    var userCpf: String
        get() = prefs?.getString(KEY_USER_CPF, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_CPF, value)?.apply() ?: Unit

    var userBirthDate: String
        get() = prefs?.getString(KEY_USER_BIRTH_DATE, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_BIRTH_DATE, value)?.apply() ?: Unit

    var userGender: String
        get() = prefs?.getString(KEY_USER_GENDER, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_GENDER, value)?.apply() ?: Unit

    var addressCep: String
        get() = prefs?.getString(KEY_ADDRESS_CEP, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_ADDRESS_CEP, value)?.apply() ?: Unit

    var addressStreet: String
        get() = prefs?.getString(KEY_ADDRESS_STREET, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_ADDRESS_STREET, value)?.apply() ?: Unit

    var addressNumber: String
        get() = prefs?.getString(KEY_ADDRESS_NUMBER, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_ADDRESS_NUMBER, value)?.apply() ?: Unit

    var addressNeighborhood: String
        get() = prefs?.getString(KEY_ADDRESS_NEIGHBORHOOD, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_ADDRESS_NEIGHBORHOOD, value)?.apply() ?: Unit

    var addressCity: String
        get() = prefs?.getString(KEY_ADDRESS_CITY, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_ADDRESS_CITY, value)?.apply() ?: Unit

    var addressState: String
        get() = prefs?.getString(KEY_ADDRESS_STATE, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_ADDRESS_STATE, value)?.apply() ?: Unit

    var rating: Double
        get() = prefs?.getFloat(KEY_RATING, 0.0f)?.toDouble() ?: 0.0
        set(value) = prefs?.edit()?.putFloat(KEY_RATING, value.toFloat())?.apply() ?: Unit

    val ratedRequestIds: MutableSet<Long> = mutableSetOf()
    val dismissedRatingIds: MutableSet<Long> = mutableSetOf()

    fun isLoggedIn() = token.isNotEmpty()
    fun bearerToken() = "Bearer $token"

    fun save(
        token: String, id: Long, name: String, email: String,
        phone: String = "", cpf: String = "", birthDate: String = "",
        gender: String = "",
        addressCep: String = "", addressStreet: String = "", addressNumber: String = "",
        addressNeighborhood: String = "", addressCity: String = "", addressState: String = "",
        rating: Double = 0.0
    ) {
        this.token = token; this.userId = id; this.userName = name; this.userEmail = email
        this.userPhone = phone; this.userCpf = cpf; this.userBirthDate = birthDate
        this.userGender = gender
        this.addressCep = addressCep; this.addressStreet = addressStreet
        this.addressNumber = addressNumber; this.addressNeighborhood = addressNeighborhood
        this.addressCity = addressCity; this.addressState = addressState
        this.rating = rating
    }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
        ratedRequestIds.clear()
        dismissedRatingIds.clear()
    }
}
