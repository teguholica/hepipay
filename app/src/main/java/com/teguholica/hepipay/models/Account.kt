package com.teguholica.hepipay.models

data class Account(
    val accountId: String,
    val secretKey: String,
    var lastPagingToken: String? = null,
    private var _balanceXLM: String? = null,
    private var _balanceIDR: String? = null
) {

    val balanceXLM = _balanceXLM
    val balanceIDR = _balanceIDR

    fun setBalance(xlm: String, idr: String) {
        _balanceXLM = xlm
        _balanceIDR = idr
    }

}