package com.example.studentaccomodation

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF = "sn_session"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun save(ctx: Context, uid: String, name: String, email: String, role: String) =
        prefs(ctx).edit()
            .putString("uid", uid).putString("name", name)
            .putString("email", email).putString("role", role)
            .putBoolean("logged_in", true).apply()

    fun isLoggedIn(ctx: Context) = prefs(ctx).getBoolean("logged_in", false)
    fun uid(ctx: Context)   = prefs(ctx).getString("uid", "")   ?: ""
    fun name(ctx: Context)  = prefs(ctx).getString("name", "")  ?: ""
    fun email(ctx: Context) = prefs(ctx).getString("email", "") ?: ""
    fun role(ctx: Context)  = prefs(ctx).getString("role", "STUDENT") ?: "STUDENT"
    fun clear(ctx: Context) = prefs(ctx).edit().clear().apply()
}
