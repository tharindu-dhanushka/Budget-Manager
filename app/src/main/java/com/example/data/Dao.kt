package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // Bank Accounts
    @Query("SELECT * FROM bank_accounts ORDER BY name ASC")
    fun getAllBankAccounts(): Flow<List<BankAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccount(account: BankAccount)

    @Delete
    suspend fun deleteBankAccount(account: BankAccount)

    // Incomes
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<Income>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: Income)

    @Delete
    suspend fun deleteIncome(income: Income)

    // Costs / Expenses
    @Query("SELECT * FROM costs ORDER BY date DESC")
    fun getAllCosts(): Flow<List<Cost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCost(cost: Cost)

    @Delete
    suspend fun deleteCost(cost: Cost)

    // ATM Withdrawals
    @Query("SELECT * FROM atm_withdrawals ORDER BY date DESC")
    fun getAllAtmWithdrawals(): Flow<List<AtmWithdrawal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtmWithdrawal(withdrawal: AtmWithdrawal)

    @Delete
    suspend fun deleteAtmWithdrawal(withdrawal: AtmWithdrawal)

    // Savings deposits / additions
    @Query("SELECT * FROM savings ORDER BY date DESC")
    fun getAllSavings(): Flow<List<Saving>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaving(saving: Saving)

    @Delete
    suspend fun deleteSaving(saving: Saving)

    // Loans
    @Query("SELECT * FROM loans ORDER BY date DESC")
    fun getAllLoans(): Flow<List<Loan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)
}
