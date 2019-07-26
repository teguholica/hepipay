package com.teguholica.hepipay.repositories

import android.content.Context
import com.teguholica.hepipay.datasources.local_database.LocalDatabase
import com.teguholica.hepipay.models.Account
import kotlinx.coroutines.*
import org.stellar.sdk.*


class Repository(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private val issuerKey = "SCZ6PTG3RQ5TPYDB4SI5PKU3KPGO2QWBKXQMBPYAEXF5PGTZ46E76LQ2"

    suspend fun createAccount(asDefault: Boolean = false): Account =
        withContext(scope.coroutineContext + Dispatchers.IO) {
            val db = LocalDatabase.connect(context)

            val newAccountPair = KeyPair.random()
            val newAccount = Account(newAccountPair.accountId, String(newAccountPair.secretSeed))

            db.accountDao().insertAll(
                com.teguholica.hepipay.datasources.local_database.Account(
                    accountId = newAccount.accountId,
                    secretKey = newAccount.secretKey,
                    isDefault = asDefault
                )
            )

            db.close()

            return@withContext newAccount
        }

    suspend fun trustIDR(account: Account) = withContext(scope.coroutineContext + Dispatchers.IO) {
        val accountPair = KeyPair.fromSecretSeed(account.secretKey)
        val issuerPair = KeyPair.fromSecretSeed(issuerKey)
        val asset = Asset.createNonNativeAsset("IDR", issuerPair)

        val server = Server("https://horizon-testnet.stellar.org")

        val newAccount = server.accounts().account(accountPair)

        val changeTrustTransaction = Transaction.Builder(newAccount, Network.TESTNET)
            .addOperation(ChangeTrustOperation.Builder(asset, "20000000").build())
            .setTimeout(180)
            .build()
        changeTrustTransaction.sign(accountPair)

        try {
            server.submitTransaction(changeTrustTransaction)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAccount(): Account? = withContext(scope.coroutineContext + Dispatchers.IO) {
        val db = LocalDatabase.connect(context)

        val account = db.accountDao().getDefault()

        if (account == null) {
            db.close()
            return@withContext null
        }

        db.close()

        return@withContext Account(account.accountId, account.secretKey)
    }

    suspend fun getXLM(account: Account): String? = withContext(scope.coroutineContext + Dispatchers.IO) {
        val pair = KeyPair.fromAccountId(account.accountId)

        val server = Server("https://horizon-testnet.stellar.org")
        val serverAccount = server.accounts().account(pair)
        val balanceInfo = serverAccount.balances.firstOrNull { it.assetType == "native" } ?: return@withContext null

        balanceInfo.balance
    }

    suspend fun getIDR(account: Account): String? = withContext(scope.coroutineContext + Dispatchers.IO) {
        val pair = KeyPair.fromAccountId(account.accountId)

        val server = Server("https://horizon-testnet.stellar.org")
        val serverAccount = server.accounts().account(pair)
        val balanceInfo = serverAccount.balances.firstOrNull { it.assetCode == "IDR" } ?: return@withContext null

        balanceInfo.balance
    }

    suspend fun send(from: Account, to: Account, amount: Int): Boolean = withContext(scope.coroutineContext + Dispatchers.IO) {
        val accountPair = KeyPair.fromSecretSeed(from.secretKey)
        val toPair = KeyPair.fromAccountId(to.accountId)
        val issuerPair = KeyPair.fromSecretSeed(issuerKey)
        val asset = Asset.createNonNativeAsset("IDR", issuerPair)

        val server = Server("https://horizon-testnet.stellar.org")

        val ownServerAccount = server.accounts().account(accountPair)

        val transaction = Transaction.Builder(ownServerAccount, Network.TESTNET)
            .addOperation(PaymentOperation.Builder(toPair, asset, amount.toString()).build())
            .setTimeout(180)
            .build()
        transaction.sign(accountPair)

        try {
            server.submitTransaction(transaction)

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendNotification(username: String, msg: String): Boolean =
        withContext(scope.coroutineContext + Dispatchers.IO) {
            suspendCancellableCoroutine<Boolean> {
                it.invokeOnCancellation {
                    cancel()
                }

                val parameters = HashMap<String, String>()
                parameters["username"] = username
                parameters["msg"] = msg
            }
        }
}