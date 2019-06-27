package com.teguholica.hepipay.ui.signup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.teguholica.hepipay.R
import com.teguholica.hepipay.commons.BaseActivity
import com.teguholica.hepipay.datasources.PrefDataSource
import com.teguholica.hepipay.repositories.Repository
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpActivity : BaseActivity() {

    private val repository by lazy { Repository(this, PrefDataSource(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnSignUp.setOnClickListener {
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()
            signUp(email, password)
        }
    }

    private fun signUp(email: String, password: String) {
        launch(Dispatchers.Main) {
            val isSuccess = repository.signup(email, password)
            if (isSuccess) {
                showDialogMessage("Signup success")
            } else {
                showDialogMessage("Signup failed")
            }
        }
    }
}
