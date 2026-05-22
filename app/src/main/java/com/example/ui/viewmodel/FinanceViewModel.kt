package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // Expose streams from Database
    val bankAccountsState = repository.allBankAccounts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val incomesState = repository.allIncomes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val costsState = repository.allCosts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val atmWithdrawalsState = repository.allAtmWithdrawals.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val savingsState = repository.allSavings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val loansState = repository.allLoans.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Dynamic Calculations
    val financialSummaryState = combine(
        combine(bankAccountsState, incomesState, costsState) { banks, incomes, costs ->
            Triple(banks, incomes, costs)
        },
        combine(atmWithdrawalsState, savingsState, loansState) { withdrawals, savings, loans ->
            Triple(withdrawals, savings, loans)
        }
    ) { first, second ->
        val (banks, incomes, costs) = first
        val (withdrawals, savings, loans) = second
        calculateSummary(banks, incomes, costs, withdrawals, savings, loans)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Database Actions
    fun addBankAccount(name: String, initialBalance: Double) {
        viewModelScope.launch {
            repository.insertBankAccount(BankAccount(name = name, initialBalance = initialBalance))
        }
    }

    fun deleteBankAccount(bankAccount: BankAccount) {
        viewModelScope.launch {
            repository.deleteBankAccount(bankAccount)
        }
    }

    fun addIncome(description: String, amount: Double, bankId: Int?, date: Long) {
        viewModelScope.launch {
            repository.insertIncome(Income(description = description, amount = amount, bankId = bankId, date = date))
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    fun addCost(category: String, subCategory: String?, description: String, amount: Double, bankId: Int?, date: Long) {
        viewModelScope.launch {
            repository.insertCost(
                Cost(category = category, subCategory = subCategory, description = description, amount = amount, bankId = bankId, date = date)
            )
        }
    }

    fun deleteCost(cost: Cost) {
        viewModelScope.launch {
            repository.deleteCost(cost)
        }
    }

    fun addAtmWithdrawal(bankId: Int, amount: Double, date: Long, description: String) {
        viewModelScope.launch {
            repository.insertAtmWithdrawal(AtmWithdrawal(bankId = bankId, amount = amount, date = date, description = description))
        }
    }

    fun deleteAtmWithdrawal(withdrawal: AtmWithdrawal) {
        viewModelScope.launch {
            repository.deleteAtmWithdrawal(withdrawal)
        }
    }

    fun addSaving(bankId: Int, description: String, amount: Double, date: Long) {
        viewModelScope.launch {
            repository.insertSaving(Saving(bankId = bankId, description = description, amount = amount, date = date))
        }
    }

    fun deleteSaving(saving: Saving) {
        viewModelScope.launch {
            repository.deleteSaving(saving)
        }
    }

    fun addLoan(type: String, name: String, description: String, amount: Double, interestRate: Double, date: Long) {
        viewModelScope.launch {
            repository.insertLoan(
                Loan(type = type, name = name, description = description, amount = amount, interestRate = interestRate, date = date)
            )
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }

    fun toggleLoanSettled(loan: Loan) {
        viewModelScope.launch {
            repository.insertLoan(loan.copy(isSettled = !loan.isSettled))
        }
    }
}

// Data holder class for dynamic metrics
data class FinancialSummary(
    val cashInHand: Double = 0.0,
    val bankBalances: Map<Int, Double> = emptyMap(), // Bank ID -> Net Balance
    val totalBankSavings: Double = 0.0,
    val totalIncomeThisMonth: Double = 0.0,
    val totalCostThisMonth: Double = 0.0,
    val dailyCosts: Map<String, Double> = emptyMap(), // Date string (yyyy-MM-dd) -> Total
    val loansTakenTotal: Double = 0.0,
    val loansProvidedTotal: Double = 0.0,
    val totalAccruedInterestTaken: Double = 0.0,
    val totalAccruedInterestProvided: Double = 0.0
)

// Helper logic to compute bank states, cash pockets, and totals on aggregate change
private fun calculateSummary(
    banks: List<BankAccount>,
    incomes: List<Income>,
    costs: List<Cost>,
    withdrawals: List<AtmWithdrawal>,
    savings: List<Saving>,
    loans: List<Loan>
): FinancialSummary {
    val bankBalances = banks.associate { it.id to it.initialBalance }.toMutableMap()

    // 1. Process Income added to bank accounts or pocket Cash
    var totalCashInHand = 0.0
    incomes.forEach { inc ->
        if (inc.bankId != null) {
            val cur = bankBalances[inc.bankId] ?: 0.0
            bankBalances[inc.bankId] = cur + inc.amount
        } else {
            totalCashInHand += inc.amount
        }
    }

    // 2. Process Savings (Depositing cash into bank accounts)
    savings.forEach { sav ->
        totalCashInHand -= sav.amount
        val cur = bankBalances[sav.bankId] ?: 0.0
        bankBalances[sav.bankId] = cur + sav.amount
    }

    // 3. Process ATM Withdrawals
    withdrawals.forEach { w ->
        val cur = bankBalances[w.bankId] ?: 0.0
        bankBalances[w.bankId] = cur - w.amount
        totalCashInHand += w.amount
    }

    // 4. Process Costs
    costs.forEach { c ->
        if (c.bankId != null) {
            val cur = bankBalances[c.bankId] ?: 0.0
            bankBalances[c.bankId] = cur - c.amount
        } else {
            totalCashInHand -= c.amount
        }
    }

    // 5. Aggregate dynamic date values
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH)
    val currentYear = now.get(Calendar.YEAR)

    var totalIncomeThisMonth = 0.0
    var totalCostThisMonth = 0.0
    val dailyCostsMap = mutableMapOf<String, Double>()

    incomes.forEach { inc ->
        val cal = Calendar.getInstance().apply { timeInMillis = inc.date }
        if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
            totalIncomeThisMonth += inc.amount
        }
    }

    costs.forEach { c ->
        val cal = Calendar.getInstance().apply { timeInMillis = c.date }
        if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
            totalCostThisMonth += c.amount
        }

        val dateStr = String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dailyCostsMap[dateStr] = (dailyCostsMap[dateStr] ?: 0.0) + c.amount
    }

    // 6. Assess unpaid loans and calculate compounding/simple values
    var loansTakenTotal = 0.0
    var loansProvidedTotal = 0.0
    var totalAccruedInterestTaken = 0.0
    var totalAccruedInterestProvided = 0.0

    loans.forEach { loan ->
        if (!loan.isSettled) {
            val elapsedMs = System.currentTimeMillis() - loan.date
            val elapsedDays = elapsedMs / (1000 * 60 * 60 * 24)
            val elapsedYears = elapsedDays.toDouble() / 365.25
            val interest = loan.amount * (loan.interestRate / 100.0) * elapsedYears

            if (loan.type == "TAKEN") {
                loansTakenTotal += loan.amount
                totalAccruedInterestTaken += interest
            } else {
                loansProvidedTotal += loan.amount
                totalAccruedInterestProvided += interest
            }
        }
    }

    return FinancialSummary(
        cashInHand = totalCashInHand,
        bankBalances = bankBalances,
        totalBankSavings = bankBalances.values.sum(),
        totalIncomeThisMonth = totalIncomeThisMonth,
        totalCostThisMonth = totalCostThisMonth,
        dailyCosts = dailyCostsMap,
        loansTakenTotal = loansTakenTotal,
        loansProvidedTotal = loansProvidedTotal,
        totalAccruedInterestTaken = totalAccruedInterestTaken,
        totalAccruedInterestProvided = totalAccruedInterestProvided
    )
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
