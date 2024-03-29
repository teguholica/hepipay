package com.koinwarga.client.ui.send

import android.content.Intent
import android.os.Bundle
import com.koinwarga.client.R
import com.koinwarga.client.commons.BaseActivity
import com.koinwarga.client.models.Account
import com.koinwarga.client.repositories.Repository
import com.koinwarga.client.ui.scanner.ScannerActivity
import kotlinx.android.synthetic.main.activity_send.*

class SendActivity : BaseActivity() {

    private val repository by lazy { Repository(this, this) }

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
//        launch(Dispatchers.Main) {
//            val isSent = repository.send(to, amount)
//
//            if (isSent) {
//                showToast("IDR sent")
//                repository.sendNotification("emulator@gmail.com", "")
//                onBackPressed()
//            } else {
//                showDialogMessage("failed to send IDR")
//            }
//        }
    }
}
