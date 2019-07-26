package com.teguholica.hepipay.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.teguholica.hepipay.R
import com.teguholica.hepipay.datasources.local_database.LocalDatabase
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import java.lang.Exception


class ReceiverWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("test", "Check payment")

        val db = LocalDatabase.connect(context)
        val activeAccount = db.accountDao().getDefault()

        if (activeAccount == null) {
            Log.d("test", "failed")
            return Result.failure()
        }

        val server = Server("https://horizon-testnet.stellar.org")
        val account = KeyPair.fromAccountId(activeAccount.accountId)

        Log.d("test", "get from server")
        val paymentsRequest = server.payments().forAccount(account).cursor(activeAccount.lastPagingToken)

        try {
            val response = paymentsRequest.execute()

            Log.d("test", "last paging : " + activeAccount.lastPagingToken)
            if (activeAccount.lastPagingToken == null) {
                notification("Update token")
                activeAccount.lastPagingToken = response.records[0].pagingToken
                db.accountDao().update(activeAccount)

                return Result.retry()
            }

            if (activeAccount.lastPagingToken != response.records[0].pagingToken) {
                response.records.forEach {
                    val paymentResponse = it as PaymentOperationResponse
                    notification("""Anda mendapat ${paymentResponse.amount}""")
                }
                activeAccount.lastPagingToken = response.records[0].pagingToken
                db.accountDao().update(activeAccount)
                return Result.retry()
            }

            return Result.retry()
        } catch (e: Exception) {
            Log.d("test", "Error dari server")
            return Result.retry()
        } finally {
            db.close()
        }
    }

    private fun notification(msg: String) {
        Log.d("test", "kirim notif")
        val builder = NotificationCompat.Builder(context, "koinwarga")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(msg)
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Much longer text that cannot fit one line..."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "koinwarga"
            val descriptionText = "koin dari kita, untuk kita, oleh kita"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("koinwarga", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId("koinwarga")
        }

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }

//    private fun receivePayment(): Result {
//        val db = LocalDatabase.connect(context)
//        val activeAccount = db.accountDao().getDefault() ?: return Result.failure()
//
//        val server = Server("https://horizon-testnet.stellar.org")
//        val account = KeyPair.fromAccountId(activeAccount.accountId)
//
//        val paymentsRequest = server.payments().forAccount(account).
//
//        val lastToken = "0"
//        if (lastToken != null) {
//            paymentsRequest.cursor(lastToken)
//        }
//
//        paymentsRequest.
//        paymentsRequest.stream(object : EventListener<OperationResponse> {
//            override fun onFailure(p0: Optional<Throwable>?, p1: Optional<Int>?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onEvent(payment: OperationResponse) {
//                // Record the paging token so we can start from here next time.
////                savePagingToken(payment.pagingToken)
//
//                // The payments stream includes both sent and received payments. We only
//                // want to process received payments here.
//                if (payment is PaymentOperationResponse) {
//                    if (payment.to == account) {
//                        return
//                    }
//
//                    val amount = payment.amount
//
//                    val asset = payment.asset
//                    val assetName: String
//                    if (asset.equals(AssetTypeNative())) {
//                        assetName = "lumens"
//                    } else {
//                        val assetNameBuilder = StringBuilder()
//                        assetNameBuilder.append((asset as AssetTypeCreditAlphaNum).code)
//                        assetNameBuilder.append(":")
//                        assetNameBuilder.append(asset.issuer.accountId)
//                        assetName = assetNameBuilder.toString()
//                    }
//
//                    val output = StringBuilder()
//                    output.append(amount)
//                    output.append(" ")
//                    output.append(assetName)
//                    output.append(" from ")
//                    output.append(payment.from.accountId)
//                    println(output.toString())
//                }
//            }
//        })
//    }
//
//    private fun loadLastPagingToken(): String? {
//        val pair = KeyPair.fromAccountId(account.accountId)
//
//        val server = Server("https://horizon-testnet.stellar.org")
//        val serverAccount = server.accounts().account(pair)
//        val balanceInfo = serverAccount.balances.firstOrNull { it.assetType == "native" } ?: return@withContext null
//
//        balanceInfo.balance
//    }
}