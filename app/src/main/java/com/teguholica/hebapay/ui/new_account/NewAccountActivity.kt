package com.teguholica.hebapay.ui.new_account

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.teguholica.hebapay.R
import com.teguholica.hebapay.commons.BaseActivity
import com.teguholica.hebapay.datasources.PrefDataSource
import com.teguholica.hebapay.repositories.Repository
import com.teguholica.hebapay.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_new_account.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewAccountActivity : BaseActivity() {

    private val repository by lazy { Repository(this, PrefDataSource(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_account)

        btnCreateAccount.visibility = View.INVISIBLE
        btnCreateAccount.setOnClickListener {
            createNewAccount()
        }

        checkAccount()
    }

    private fun goToMainPage() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun showCreateAccountForm() {
        btnCreateAccount.visibility = View.VISIBLE
    }

    private fun createNewAccount() {
        launch(Dispatchers.Main) {
            val result = repository.createAccount()

            if (result) {
                goToMainPage()
            }
        }
    }

    private fun checkAccount() {
        launch(Dispatchers.Main) {
            val account = repository.getAccount()

            if (account != null) {
                goToMainPage()
            } else {
                showCreateAccountForm()
            }
        }
    }
}
