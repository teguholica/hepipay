package com.teguholica.hepipay.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.teguholica.hepipay.R
import com.teguholica.hepipay.commons.BaseActivity
import com.teguholica.hepipay.datasources.PrefDataSource
import com.teguholica.hepipay.repositories.Repository
import com.teguholica.hepipay.ui.new_account.NewAccountActivity
import com.teguholica.hepipay.ui.signup.SignUpActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private val repository by lazy { Repository(this, PrefDataSource(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()
            login(email, password)
        }

        btnSignUp.setOnClickListener {
            goToSignUpPage()
        }

        checkIsAuthenticated()
    }

    private fun goToSignUpPage() {
        Intent(this, SignUpActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun goToCreateAccountPage() {
        Intent(this, NewAccountActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun checkIsAuthenticated() {
        launch(Dispatchers.Main) {
            val isLogin = repository.isLogin()
            if (isLogin) {
                goToCreateAccountPage()
            }
        }
    }

    private fun login(email: String, password: String) {
        launch(Dispatchers.Main) {
            val isSuccess = repository.login(email, password)
            if (isSuccess) {
                goToCreateAccountPage()
            }else {
                showToast("failed login")
            }
        }
    }
}
