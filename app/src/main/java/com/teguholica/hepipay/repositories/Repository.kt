package com.teguholica.hepipay.repositories

import android.util.Log
import com.teguholica.hepipay.datasources.PrefDataSource
import com.teguholica.hepipay.models.Account
import kotlinx.coroutines.*
import org.stellar.sdk.*
import kotlin.coroutines.resume
import android.widget.Toast
import com.parse.*
import com.teguholica.hepipay.ui.main.MainActivity


class Repository(
    private val scope: CoroutineScope,
    private val prefDataSource: PrefDataSource
) {

    private val issuerAccountId = "GDOVFMOPIN3XGEB25G75OOYH7POUHLNAHBIOI52MAE7JKNF46ZFOR2QT"
    private val issuerKey = "SAM2I5OREQUFKYRF6FUCV36REAVNTK5MKMDTDMSXYMY4DHKH6RTQAJVP"

    private val distributionAccountId = "GBKPPKPGCTJGRU7PFJZBIKCJ3HRNRZYL7UAYADAB35NJ2AK3ISDEFFIO"
    private val distributionKey = "SCYKDBUPE56PAVR55ZIRGJ4PK4ASTQWMIS6PIJK7BDQKJJ6ZW54WRKA5"

    private val parseInstallation by lazy { ParseInstallation.getCurrentInstallation() }
    private val parseUser by lazy { ParseUser.getCurrentUser() }

    suspend fun signup(email: String, password: String): Boolean = withContext(scope.coroutineContext + Dispatchers.IO) {
        val user = ParseUser()
        user.username = email
        user.email = email
        user.setPassword(password)
        suspendCancellableCoroutine<Boolean> { resolve ->
            user.signUpInBackground {
                resolve.invokeOnCancellation {
                    cancel()
                }
                if (it == null) {
                    resolve.resume(true)
                } else {
                    ParseUser.logOut()
                    resolve.resume(false)
                }
            }
        }
    }

    suspend fun login(email: String, password: String): Boolean = withContext(scope.coroutineContext + Dispatchers.IO) {
        suspendCancellableCoroutine<Boolean> { resolve ->
            resolve.invokeOnCancellation {
                cancel()
            }
            ParseUser.logInInBackground(email, password) { parseUser, e ->
                if (parseUser != null) {
                    parseInstallation.put("user", parseUser)
                    parseInstallation.save()
                    resolve.resume(true)
                } else {
                    ParseUser.logOut()
                    resolve.resume(false)
                }
            }
        }
    }

    suspend fun isLogin(): Boolean = withContext(scope.coroutineContext + Dispatchers.IO) {
        if (parseUser == null) {
            false
        } else {
            parseInstallation.put("user", parseUser)
            parseInstallation.save()
            parseUser.isAuthenticated
        }
    }

    suspend fun createAccount(): Boolean = withContext(scope.coroutineContext + Dispatchers.IO) {
        val distributionPair = KeyPair.fromSecretSeed(distributionKey)
        val newAccountPair = KeyPair.random()

        val server = Server("https://horizon-testnet.stellar.org")

        val distributionAccount = server.accounts().account(distributionPair)
        val createAccountTransaction = Transaction.Builder(distributionAccount, Network.TESTNET)
            .addOperation(CreateAccountOperation.Builder(newAccountPair, "5").build())
            .addMemo(Memo.text("Create new account from HEBA"))
            .setTimeout(180)
            .build()
        createAccountTransaction.sign(distributionPair)

        try {
            server.submitTransaction(createAccountTransaction)

            prefDataSource.saveAccount(Account(newAccountPair.accountId, String(newAccountPair.secretSeed)))

            Log.d("test", newAccountPair.accountId)
            Log.d("test", String(newAccountPair.secretSeed))

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun issuerTrust() = withContext(scope.coroutineContext + Dispatchers.IO) {
        val account = prefDataSource.getAccount() ?: return@withContext null
        val accountPair = KeyPair.fromSecretSeed(account.key)
        val issuerPair = KeyPair.fromSecretSeed(issuerKey)
        val asset = Asset.createNonNativeAsset("IDR", issuerPair)

        val server = Server("https://horizon-testnet.stellar.org")

        val newAccount = server.accounts().account(accountPair)

        val changeTrustTransaction = Transaction.Builder(newAccount, Network.TESTNET)
            .addOperation(ChangeTrustOperation.Builder(asset, "20000000").build())
            .addOperation(ManageDataOperation.Builder("hepiId", parseUser.username.toByteArray()).build())
            .addMemo(Memo.text("Trust IDR from HEBA"))
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
        prefDataSource.getAccount()
    }

    suspend fun clearAccount() = withContext(scope.coroutineContext + Dispatchers.IO) {
        prefDataSource.clearAccount()
    }

    suspend fun topupIDR(): Boolean = withContext(scope.coroutineContext + Dispatchers.IO) {
        val account = prefDataSource.getAccount() ?: return@withContext false
        val accountPair = KeyPair.fromSecretSeed(account.key)
        val issuerPair = KeyPair.fromSecretSeed(issuerKey)
        val asset = Asset.createNonNativeAsset("IDR", issuerPair)

        val server = Server("https://horizon-testnet.stellar.org")

        val issuerAccount = server.accounts().account(issuerPair)

        val changeTrustTransaction = Transaction.Builder(issuerAccount, Network.TESTNET)
            .addOperation(PaymentOperation.Builder(accountPair, asset, "100000").build())
            .addMemo(Memo.text("Topup IDR from HEBA"))
            .setTimeout(180)
            .build()
        changeTrustTransaction.sign(issuerPair)

        try {
            server.submitTransaction(changeTrustTransaction)

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getXLM(): String? = withContext(scope.coroutineContext + Dispatchers.IO) {
        val account = prefDataSource.getAccount() ?: return@withContext null

        val pair = KeyPair.fromAccountId(account.id)

        val server = Server("https://horizon-testnet.stellar.org")
        val serverAccount = server.accounts().account(pair)
        val balanceInfo = serverAccount.balances.firstOrNull { it.assetType == "native" } ?: return@withContext null

        balanceInfo.balance
    }

    suspend fun getIDR(): String? = withContext(scope.coroutineContext + Dispatchers.IO) {
        val account = prefDataSource.getAccount() ?: return@withContext null

        val pair = KeyPair.fromAccountId(account.id)

        val server = Server("https://horizon-testnet.stellar.org")
        val serverAccount = server.accounts().account(pair)
        val balanceInfo = serverAccount.balances.firstOrNull { it.assetCode == "IDR" } ?: return@withContext null

        balanceInfo.balance
    }

    suspend fun send(to: Account, amount: Int): Boolean = withContext(scope.coroutineContext + Dispatchers.IO) {
        val account = prefDataSource.getAccount() ?: return@withContext false
        val accountPair = KeyPair.fromSecretSeed(account.key)
        val toPair = KeyPair.fromAccountId(to.id)
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

    suspend fun sendNotification(username: String, msg: String): Boolean = withContext(scope.coroutineContext + Dispatchers.IO) {
        suspendCancellableCoroutine<Boolean> {
            it.invokeOnCancellation {
                cancel()
            }

            val parameters = HashMap<String, String>()
            parameters["username"] = username
            parameters["msg"] = msg

            ParseCloud.callFunctionInBackground("sendnotification", parameters,
                FunctionCallback<Map<String, Any>> { _, e ->
                    if (e == null) {
                        it.resume(true)
                    } else {
                        it.resume(false)
                    }
                }
            )
        }
    }
}