package com.teguholica.hepipay.datasources

import android.content.Context
import androidx.core.content.edit
import com.teguholica.hepipay.models.Account

class PrefDataSource(private val context: Context) {

    private val pref by lazy { context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE) }

    fun saveAccount(account: Account) {
        pref.edit {
            putString("accountId", account.id)
            putString("key", account.key)
        }
    }

    fun getAccount(): Account? {
        val accountId = pref.getString("accountId", "")
        val key = pref.getString("key", "")

        if (accountId.isNullOrEmpty() || key.isNullOrEmpty()) {
            return null
        }

        return Account(accountId, key)
    }

    fun clearAccount() {
        pref.edit {
            putString("accountId", "")
            putString("key", "")
        }
    }

}