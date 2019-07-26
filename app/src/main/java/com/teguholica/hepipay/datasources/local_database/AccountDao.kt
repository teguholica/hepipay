package com.teguholica.hepipay.datasources.local_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AccountDao {

    @Query("SELECT * FROM ACCOUNT WHERE is_default IS 1")
    fun getDefault(): Account?

    @Insert
    fun insertAll(vararg accounts: Account)

    @Update
    fun update(vararg accounts: Account)

}