package com.koinwarga.client.ui.landing

import android.content.Intent
import android.os.Bundle
import com.koinwarga.client.R
import com.koinwarga.client.commons.BaseActivity
import com.koinwarga.client.repositories.Repository
import com.koinwarga.client.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LandingActivity : BaseActivity() {

    private val repository by lazy { Repository(this, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        btnNewAccount.setOnClickListener { createNewAccount() }

        checkAccountAvaibility()
    }

    private fun goToMainPage() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun checkAccountAvaibility() {
        launch(Dispatchers.Main) {
            val account = repository.getAccount()

            if (account != null) {
                goToMainPage()
            }
        }
    }

    private fun createNewAccount() {
        launch(Dispatchers.Main) {
            repository.createAccount(true)
            goToMainPage()
        }
    }
}
