package com.koinwarga.client.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.koinwarga.client.R
import com.koinwarga.client.commons.BaseActivity
import com.koinwarga.client.models.Account
import com.koinwarga.client.repositories.Repository
import com.koinwarga.client.ui.send.SendActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode


class MainActivity : BaseActivity() {

    private val repository by lazy { Repository(this, this) }
    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSend.setOnClickListener {
            goToSendPage()
        }

        btnTopup.setOnClickListener {
//            topupIDR()
        }

        btnCopyAccount.setOnClickListener {
            copyAccountToClipboard()
        }

        btnClearAccount.setOnClickListener {
            clearAccount()
        }

        btnTrust.setOnClickListener {
            trustIDR()
        }
    }

    override fun onResume() {
        super.onResume()

        getAccountInfo()
//        getIDRBalance()
    }

    private fun goToSendPage() {
        Intent(this, SendActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun showAccountId() {
        txtAccountId.text = account.accountId
    }

    private fun showXLMBalance(balance: String) {
        txtXLMBalance.text = balance
    }

    private fun showIDRBalance(balance: String) {
        txtIDRBalance.text = balance
    }

    private fun generateQRCode() {
        val myBitmap = QRCode.from(account.accountId).bitmap()
        vBarcode.setImageBitmap(myBitmap)
    }

    private fun trustIDR() {
        launch(Dispatchers.Main) {
            repository.trustIDR(account)
            getAccountInfo()
            getXLMBalance()
            getIDRBalance()
            showDialogMessage("Account trust IDR")
        }
    }

    private fun clearAccount() {
        launch(Dispatchers.Main) {
//            repository.clearAccount()
            showDialogMessage("Account cleared")
        }
    }

//    private fun topupIDR() {
//        launch(Dispatchers.Main) {
//            val isTopupSuccess = repository.topupIDR()
//            if (isTopupSuccess) {
//                showDialogMessage("Topup success")
//                getXLMBalance()
//                getIDRBalance()
//            } else {
//                showDialogMessage("Topup failed")
//            }
//        }
//    }

    private fun getAccountInfo() {
        launch(Dispatchers.Main) {
            val accountResult = repository.getAccount() ?: return@launch

            account = accountResult
            showAccountId()
            getXLMBalance()
            generateQRCode()
        }
    }

    private fun getXLMBalance() {
        launch(Dispatchers.Main) {
            val balance = repository.getXLM(account)

            if (balance == null) {
                showToast("Can not get xlm balance")
                return@launch
            }


            showXLMBalance(balance)
        }
    }

    private fun getIDRBalance() {
        launch(Dispatchers.Main) {
            val balance = repository.getIDR(account)

            if (balance == null) {
                showToast("Can not get idr balance")
                return@launch
            }


            showIDRBalance(balance)
        }
    }

    private fun copyAccountToClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Source Text", account.accountId)
        clipboardManager.primaryClip = clipData
        showToast("Copy Account ID")
    }
}
