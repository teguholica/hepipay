package com.teguholica.hebapay.ui.send

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.teguholica.hebapay.R
import com.teguholica.hebapay.commons.BaseActivity
import com.teguholica.hebapay.datasources.PrefDataSource
import com.teguholica.hebapay.models.Account
import com.teguholica.hebapay.repositories.Repository
import com.teguholica.hebapay.ui.scanner.ScannerActivity
import kotlinx.android.synthetic.main.activity_send.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendActivity : BaseActivity() {

    private val repository by lazy { Repository(this, PrefDataSource(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        btnSend.setOnClickListener {
            val to = Account(txtTo.text.toString(), "")
            sendIDR(to, txtAmount.text.toString().toInt())
        }

        btnScan.setOnClickListener {
            goToScannerPage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 300 && resultCode == 200 && data != null) {
            setAccountIdToForm(data.getStringExtra("accountId"))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setAccountIdToForm(accountId: String) {
        txtTo.setText(accountId)
    }

    private fun goToScannerPage() {
        Intent(this, ScannerActivity::class.java).apply {
            startActivityForResult(this, 300)
        }
    }

    private fun sendIDR(to: Account, amount: Int) {
        launch(Dispatchers.Main) {
            val isSent = repository.send(to, amount)

            if (isSent) {
                showToast("IDR sent")
                onBackPressed()
            } else {
                showDialogMessage("failed to send IDR")
            }
        }
    }
}
