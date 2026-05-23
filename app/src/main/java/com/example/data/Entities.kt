package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val initialBalance: Double = 0.0
)

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val date: Long, // Timestamp
    val bankId: Int? = null // Optional bank account to deposit into
)

@Entity(tableName = "costs")
data class Cost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // Rent, Food, Groceries, Stationery, Phone Charges, Transport, Others
    val subCategory: String? = null, // Breakfast, Lunch, Dinner, Other (for Food)
    val description: String,
    val amount: Double,
    val date: Long, // Timestamp
    val bankId: Int? = null // Bank paid from (null means Cash pocket)
)

@Entity(tableName = "atm_withdrawals")
data class AtmWithdrawal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bankId: Int, // Decreases bank savings
    val amount: Double,
    val date: Long, // Timestamp
    val description: String = "ATM Withdrawal"
)

@Entity(tableName = "savings")
data class Saving(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bankId: Int, // Decreases Cash or increases Savings
    val description: String,
    val amount: Double,
    val date: Long // Timestamp
)

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "TAKEN" or "PROVIDED"
    val name: String, // Person name
    val description: String,
    val amount: Double,
    val interestRate: Double = 0.0, // interest rate (annual % e.g., 5.0)
    val date: Long, // Start Date of loan
    val isSettled: Boolean = false
)

@Entity(tableName = "loan_installments")
data class LoanInstallment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanId: Int,
    val amount: Double,
    val date: Long,
    val bankId: Int?, // null means Cash pocket
    val note: String = ""
)
