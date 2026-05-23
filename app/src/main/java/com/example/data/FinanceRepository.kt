package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {
    val allBankAccounts: Flow<List<BankAccount>> = financeDao.getAllBankAccounts()
    val allIncomes: Flow<List<Income>> = financeDao.getAllIncomes()
    val allCosts: Flow<List<Cost>> = financeDao.getAllCosts()
    val allAtmWithdrawals: Flow<List<AtmWithdrawal>> = financeDao.getAllAtmWithdrawals()
    val allSavings: Flow<List<Saving>> = financeDao.getAllSavings()
    val allLoans: Flow<List<Loan>> = financeDao.getAllLoans()
    val allLoanInstallments: Flow<List<LoanInstallment>> = financeDao.getAllLoanInstallments()

    suspend fun insertBankAccount(account: BankAccount) = financeDao.insertBankAccount(account)
    suspend fun deleteBankAccount(account: BankAccount) = financeDao.deleteBankAccount(account)

    suspend fun insertIncome(income: Income) = financeDao.insertIncome(income)
    suspend fun deleteIncome(income: Income) = financeDao.deleteIncome(income)

    suspend fun insertCost(cost: Cost) = financeDao.insertCost(cost)
    suspend fun deleteCost(cost: Cost) = financeDao.deleteCost(cost)

    suspend fun insertAtmWithdrawal(withdrawal: AtmWithdrawal) = financeDao.insertAtmWithdrawal(withdrawal)
    suspend fun deleteAtmWithdrawal(withdrawal: AtmWithdrawal) = financeDao.deleteAtmWithdrawal(withdrawal)

    suspend fun insertSaving(saving: Saving) = financeDao.insertSaving(saving)
    suspend fun deleteSaving(saving: Saving) = financeDao.deleteSaving(saving)

    suspend fun insertLoan(loan: Loan) = financeDao.insertLoan(loan)
    suspend fun deleteLoan(loan: Loan) = financeDao.deleteLoan(loan)

    suspend fun insertLoanInstallment(installment: LoanInstallment) = financeDao.insertLoanInstallment(installment)
    suspend fun deleteLoanInstallment(installment: LoanInstallment) = financeDao.deleteLoanInstallment(installment)
}
