package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.roundToInt
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.viewmodel.FinancialSummary
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

// Styling Color Tokens matching design guidelines
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

object ThemeToggle {
    var isDark by mutableStateOf(true)
}

val LocalCurrencySymbol = staticCompositionLocalOf { "$" }

private val SlateDark: Color
    get() = if (ThemeToggle.isDark) Color(0xFF1C1B1F) else Color(0xFFFAF9FD)

private val SlateSurface: Color
    get() = if (ThemeToggle.isDark) Color(0xFF2B2930) else Color(0xFFFFFFFF)

private val SlateBorder: Color
    get() = if (ThemeToggle.isDark) Color(0xFF49454F) else Color(0xFFE5E2EB)

private val EmeraldAccent: Color
    get() = if (ThemeToggle.isDark) Color(0xFFB5F2B8) else Color(0xFF2E7D32)

private val EmeraldSurface: Color
    get() = if (ThemeToggle.isDark) Color(0xFF113111) else Color(0xFFE8F5E9)

private val CoralAccent: Color
    get() = if (ThemeToggle.isDark) Color(0xFFF2B8B5) else Color(0xFFC62828)

private val CoralSurface: Color
    get() = if (ThemeToggle.isDark) Color(0xFF311111) else Color(0xFFFFEBEE)

private val AmberAccent: Color
    get() = if (ThemeToggle.isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)

private val ElegantPurpleAccent: Color
    get() = if (ThemeToggle.isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)

private val ElegantPurpleOnAccent: Color
    get() = if (ThemeToggle.isDark) Color(0xFF381E72) else Color(0xFFFFFFFF)

private val ElegantPurpleDeep: Color
    get() = if (ThemeToggle.isDark) Color(0xFF4F378B) else Color(0xFFEADDFF)

private val TextPrimary: Color
    get() = if (ThemeToggle.isDark) Color.White else Color(0xFF1C1B1F)

private val TextSecondary: Color
    get() = if (ThemeToggle.isDark) Color.LightGray else Color(0xFF5E5C64)

enum class FinanceTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    INCOME("Income/Savings", Icons.Default.AccountBalance),
    COSTS("Costs", Icons.Default.ReceiptLong),
    ATM("ATM Logs", Icons.Default.LocalAtm),
    LOANS("Loans", Icons.Default.Handshake)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val savedGoogleEmails by viewModel.savedGoogleEmails.collectAsState()

    LaunchedEffect(isDarkTheme) {
        ThemeToggle.isDark = isDarkTheme
    }

    var showGoogleSignInDialog by remember { mutableStateOf(false) }
    var showProfileDrawer by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(FinanceTab.OVERVIEW) }
    val summary by viewModel.financialSummaryState.collectAsState()
    val banks by viewModel.bankAccountsState.collectAsState()
    val incomes by viewModel.incomesState.collectAsState()
    val costs by viewModel.costsState.collectAsState()
    val withdrawals by viewModel.atmWithdrawalsState.collectAsState()
    val savings by viewModel.savingsState.collectAsState()
    val loans by viewModel.loansState.collectAsState()
    val loanInstallments by viewModel.loanInstallmentsState.collectAsState()

    // Dialog trigger states
    var showAddBank by remember { mutableStateOf(false) }
    var showAddIncome by remember { mutableStateOf(false) }
    var showAddCost by remember { mutableStateOf(false) }
    var showAddAtm by remember { mutableStateOf(false) }
    var showAddSaving by remember { mutableStateOf(false) }
    var showAddLoan by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalCurrencySymbol provides selectedCurrency.symbol) {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Welcome back,",
                            color = Color(0xFF938F99),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Finance Overview",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDark,
                    titleContentColor = TextPrimary
                ),
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "May 2026",
                            modifier = Modifier
                                .background(SlateSurface, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            color = ElegantPurpleAccent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElegantPurpleDeep)
                                .border(1.dp, ElegantPurpleAccent.copy(alpha = 0.2f), CircleShape)
                                .clickable { showProfileDrawer = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                color = ElegantPurpleAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                FinanceTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 9.sp,
                                maxLines = 1,
                                softWrap = false,
                                letterSpacing = (-0.3).sp,
                                overflow = TextOverflow.Clip
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantPurpleAccent,
                            selectedTextColor = ElegantPurpleAccent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = ElegantPurpleDeep
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = SlateDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                FinanceTab.OVERVIEW -> {
                    OverviewScreen(
                        summary = summary,
                        banks = banks,
                        costs = costs,
                        savings = savings,
                        onAddBank = { showAddBank = true },
                        onDeleteBank = { viewModel.deleteBankAccount(it) }
                    )
                }
                FinanceTab.INCOME -> {
                    IncomeScreen(
                        incomes = incomes,
                        savings = savings,
                        banks = banks,
                        summary = summary,
                        onAddIncome = { showAddIncome = true },
                        onAddSaving = { showAddSaving = true },
                        onAddBank = { showAddBank = true },
                        onDeleteIncome = { viewModel.deleteIncome(it) },
                        onDeleteSaving = { viewModel.deleteSaving(it) }
                    )
                }
                FinanceTab.COSTS -> {
                    CostsScreen(
                        costs = costs,
                        banks = banks,
                        summary = summary,
                        onAddCost = { showAddCost = true },
                        onDeleteCost = { viewModel.deleteCost(it) }
                    )
                }
                FinanceTab.ATM -> {
                    AtmScreen(
                        withdrawals = withdrawals,
                        banks = banks,
                        onAddWithdrawal = { showAddAtm = true },
                        onDeleteWithdrawal = { viewModel.deleteAtmWithdrawal(it) }
                    )
                }
                FinanceTab.LOANS -> {
                    LoansScreen(
                        loans = loans,
                        loanInstallments = loanInstallments,
                        banks = banks,
                        onAddLoan = { showAddLoan = true },
                        onSettledToggle = { viewModel.toggleLoanSettled(it) },
                        onDeleteLoan = { viewModel.deleteLoan(it) },
                        onAddInstallment = { loanId, amt, bankId, dt, label ->
                            viewModel.addLoanInstallment(loanId, amt, bankId, dt, label)
                        },
                        onDeleteInstallment = { viewModel.deleteLoanInstallment(it) }
                    )
                }
            }

            // --- ALL ACTION DIALOG OVERLAYS ---
            if (showAddBank) {
                AddBankDialog(
                    onDismiss = { showAddBank = false },
                    onConfirm = { name, initial ->
                        viewModel.addBankAccount(name, initial)
                        showAddBank = false
                    }
                )
            }

            if (showAddIncome) {
                AddIncomeDialog(
                    banks = banks,
                    onDismiss = { showAddIncome = false },
                    onConfirm = { desc, amount, bankId, date ->
                        viewModel.addIncome(desc, amount, bankId, date)
                        showAddIncome = false
                    }
                )
            }

            if (showAddCost) {
                AddCostDialog(
                    banks = banks,
                    onDismiss = { showAddCost = false },
                    onConfirm = { category, subCategory, desc, amount, bankId, date ->
                        viewModel.addCost(category, subCategory, desc, amount, bankId, date)
                        showAddCost = false
                    }
                )
            }

            if (showAddSaving) {
                AddSavingDialog(
                    banks = banks,
                    onDismiss = { showAddSaving = false },
                    onConfirm = { bankId, desc, amount, date ->
                        viewModel.addSaving(bankId, desc, amount, date)
                        showAddSaving = false
                    }
                )
            }

            if (showAddAtm) {
                AddAtmDialog(
                    banks = banks,
                    onDismiss = { showAddAtm = false },
                    onConfirm = { bankId, amount, date, description ->
                        viewModel.addAtmWithdrawal(bankId, amount, date, description)
                        showAddAtm = false
                    }
                )
            }

            if (showAddLoan) {
                AddLoanDialog(
                    onDismiss = { showAddLoan = false },
                    onConfirm = { type, name, desc, amount, rate, date ->
                        viewModel.addLoan(type, name, desc, amount, rate, date)
                        showAddLoan = false
                    }
                )
            }

            if (showGoogleSignInDialog) {
                GoogleSignInDialog(
                    savedEmails = savedGoogleEmails,
                    onDismiss = { showGoogleSignInDialog = false },
                    onLogin = { email ->
                        viewModel.loginWithEmail(email)
                        showGoogleSignInDialog = false
                    },
                    onRemoveSavedEmail = { email ->
                        viewModel.removeSavedEmail(email)
                        if (currentUserEmail == email) {
                            viewModel.logout()
                        }
                    }
                )
            }

            if (showCurrencyDialog) {
                AlertDialog(
                    onDismissRequest = { showCurrencyDialog = false },
                    title = { Text("Choose Default Currency", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    containerColor = SlateSurface,
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.example.ui.viewmodel.currencyOptions.forEach { curr ->
                                val isSelected = selectedCurrency.code == curr.code
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ElegantPurpleDeep.copy(alpha = 0.4f) else Color.Transparent)
                                        .clickable {
                                            viewModel.selectCurrency(curr)
                                            showCurrencyDialog = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = curr.name,
                                            color = TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = ElegantPurpleAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCurrencyDialog = false }) {
                            Text("Cancel", color = ElegantPurpleAccent)
                        }
                    }
                )
            }
        }
    }

        // --- BACKGROUND DIM BACKDROP ---
        if (showProfileDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        showProfileDrawer = false
                    }
            )
        }

        // --- SLIDING PROFILE DRAWER ---
        AnimatedVisibility(
            visible = showProfileDrawer,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 300.dp)
                    .fillMaxWidth(0.85f)
                    .background(SlateDark)
                    .border(
                        width = 1.dp,
                        color = SlateBorder,
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header of Drawer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = { showProfileDrawer = false }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close drawer",
                            tint = TextSecondary
                        )
                    }
                }

                // Profile card details (JD logo design)
                val initials = if (currentUserEmail != null) currentUserEmail!!.take(2).uppercase() else "JD"
                val displayName = if (currentUserEmail != null) currentUserEmail!!.substringBefore("@") else "John Doe"
                val displaySub = if (currentUserEmail != null) currentUserEmail!! else "Premium Member"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ElegantPurpleDeep)
                                .border(2.dp, ElegantPurpleAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = ElegantPurpleAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = displaySub,
                            fontSize = 11.sp,
                            color = ElegantPurpleAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // --- SIGN IN OPTIONS ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SIGN IN OPTIONS",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                    
                    if (currentUserEmail == null) {
                        // Google Sign-In style button
                        Button(
                            onClick = {
                                showGoogleSignInDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(1.dp, SlateBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google Logo",
                                    tint = Color(0xFF4285F4),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Sign in with Google",
                                    color = Color(0xFF1F1F1F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        // Signed in options: Logged in profile view & Sign Out / Change Options
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateDark),
                                modifier = Modifier.fillMaxWidth(),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(SlateBorder.copy(alpha = 0.3f))
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF34A853)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Connected Account",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(
                                    onClick = { showGoogleSignInDialog = true }
                                ) {
                                    Text("Switch Account", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.logout()
                                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Sign Out", color = CoralAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = SlateBorder.copy(alpha = 0.4f))

                // --- SETTINGS SECTION ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SETTINGS",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                    
                    // Theme Switch option row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.setDarkTheme(!isDarkTheme)
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Theme",
                                tint = TextSecondary.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Dark Theme",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElegantPurpleAccent,
                                checkedTrackColor = ElegantPurpleDeep,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }

                    // Default Currency selector option row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showCurrencyDialog = true
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Paid, 
                                contentDescription = "Currency",
                                tint = TextSecondary.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Default Currency",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = selectedCurrency.code,
                            color = ElegantPurpleAccent.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = SlateBorder.copy(alpha = 0.4f))

                // --- ABOUT US ---
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ABOUT US",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "T-Desk Solutions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "All right received.",
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                }

                HorizontalDivider(color = SlateBorder.copy(alpha = 0.4f))

                // --- CONTACTS ---
                var contactDetailsExpanded by rememberSaveable { mutableStateOf(false) }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { contactDetailsExpanded = !contactDetailsExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contact us:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = if (contactDetailsExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Toggle Contacts",
                            tint = ElegantPurpleAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = contactDetailsExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Email Redirect Clickable Box
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:thdhanushka31@gmail.com")
                                                putExtra(Intent.EXTRA_SUBJECT, "Related to Finance Management App")
                                                // Default Text as requested
                                                putExtra(Intent.EXTRA_TEXT, "Related to Finance Management App\n\nHi T-Desk Solutions,\n")
                                                `package` = "com.google.android.gm"
                                            }
                                            context.startActivity(emailIntent)
                                        } catch (e: Exception) {
                                            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:thdhanushka31@gmail.com")
                                                putExtra(Intent.EXTRA_SUBJECT, "Related to Finance Management App")
                                                putExtra(Intent.EXTRA_TEXT, "Related to Finance Management App\n\nHi T-Desk Solutions,\n")
                                            }
                                            try {
                                                context.startActivity(fallbackIntent)
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, "Could not open email app.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Gmail icon",
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Send Email",
                                            fontSize = 10.sp,
                                            color = TextSecondary.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "thdhanushka31@gmail.com",
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // WhatsApp Clickable Box
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val waUrl = "https://api.whatsapp.com/send?phone=94789728396&text=Related%20to%20Finance%20management%20app"
                                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
                                                setPackage("com.whatsapp")
                                            }
                                            context.startActivity(waIntent)
                                        } catch (e: Exception) {
                                            val backupUrl = "https://api.whatsapp.com/send?phone=94789728396&text=Related%20to%20Finance%20management%20app"
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(backupUrl))
                                            try {
                                                context.startActivity(webIntent)
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, "Could not open WhatsApp.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "WhatsApp icon",
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "WhatsApp message",
                                            fontSize = 10.sp,
                                            color = TextSecondary.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "+94 78 97 28 396",
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
      }
    }
}

@Composable
fun formatPrice(amount: Double): String {
    val symbol = LocalCurrencySymbol.current
    return String.format(Locale.getDefault(), "%s%.2f", symbol, amount)
}

@Composable
fun formatPrice(amount: Float): String {
    val symbol = LocalCurrencySymbol.current
    return String.format(Locale.getDefault(), "%s%.2f", symbol, amount)
}

@Composable
fun formatPriceSigned(amount: Double, positive: Boolean): String {
    val symbol = LocalCurrencySymbol.current
    val sign = if (positive) "+" else "-"
    return String.format(Locale.getDefault(), "%s%s%.2f", sign, symbol, amount)
}

// ------------------------------------------------------------------------------------------------
// SCREEN 1: OVERVIEW SCREEN
// ------------------------------------------------------------------------------------------------
enum class GraphFrequency {
    DAILY, WEEKLY, MONTHLY
}

data class ChartPoint(
    val label: String,
    val savings: Float,
    val costs: Float
)

@Composable
fun InteractiveTrendChart(
    costs: List<Cost>,
    savings: List<Saving>,
    modifier: Modifier = Modifier
) {
    var frequency by remember { mutableStateOf(GraphFrequency.DAILY) }
    
    // Prepare the data points
    val points = remember(costs, savings, frequency) {
        val calculated = mutableListOf<ChartPoint>()
        val sdf = SimpleDateFormat("MMM d", Locale.US)
        
        when (frequency) {
            GraphFrequency.DAILY -> {
                for (i in 6 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    
                    val startCal = Calendar.getInstance().apply {
                        timeInMillis = cal.timeInMillis
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val endCal = Calendar.getInstance().apply {
                        timeInMillis = cal.timeInMillis
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    
                    val dayCosts = costs.filter { it.date in startCal.timeInMillis..endCal.timeInMillis }.sumOf { it.amount }
                    val daySavings = savings.filter { it.date in startCal.timeInMillis..endCal.timeInMillis }.sumOf { it.amount }
                    
                    calculated.add(ChartPoint(
                        label = sdf.format(cal.time),
                        savings = daySavings.toFloat(),
                        costs = dayCosts.toFloat()
                    ))
                }
            }
            GraphFrequency.WEEKLY -> {
                for (i in 3 downTo 0) {
                    val calStart = Calendar.getInstance()
                    calStart.add(Calendar.DAY_OF_YEAR, -(i + 1) * 7)
                    val calEnd = Calendar.getInstance()
                    calEnd.add(Calendar.DAY_OF_YEAR, -i * 7)
                    
                    val weekLabel = if (i == 0) "This Wk" else "Wk -${i}"
                    
                    val weekCosts = costs.filter { it.date in calStart.timeInMillis..calEnd.timeInMillis }.sumOf { it.amount }
                    val weekSavings = savings.filter { it.date in calStart.timeInMillis..calEnd.timeInMillis }.sumOf { it.amount }
                    
                    calculated.add(ChartPoint(
                        label = weekLabel,
                        savings = weekSavings.toFloat(),
                        costs = weekCosts.toFloat()
                    ))
                }
            }
            GraphFrequency.MONTHLY -> {
                val monthFormat = SimpleDateFormat("MMM", Locale.US)
                for (i in 5 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, -i)
                    
                    val startCal = Calendar.getInstance().apply {
                        timeInMillis = cal.timeInMillis
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val endCal = Calendar.getInstance().apply {
                        timeInMillis = cal.timeInMillis
                        set(Calendar.DAY_OF_MONTH, startCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    
                    val monthCosts = costs.filter { it.date in startCal.timeInMillis..endCal.timeInMillis }.sumOf { it.amount }
                    val monthSavings = savings.filter { it.date in startCal.timeInMillis..endCal.timeInMillis }.sumOf { it.amount }
                    
                    calculated.add(ChartPoint(
                        label = monthFormat.format(cal.time),
                        savings = monthSavings.toFloat(),
                        costs = monthCosts.toFloat()
                    ))
                }
            }
        }
        calculated
    }
    
    var selectedIndex by remember(points) { mutableStateOf(-1) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(24.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Savings & Costs Analytics",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Tap on nodes to view precise logs",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                
                Row(
                    modifier = Modifier
                        .background(SlateDark, RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GraphFrequency.values().forEach { freq ->
                        val isSelected = frequency == freq
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElegantPurpleAccent else Color.Transparent)
                                .clickable {
                                    frequency = freq
                                    selectedIndex = -1
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = freq.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (isSelected) ElegantPurpleOnAccent else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(6.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 3.dp).background(EmeraldAccent, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text("Savings", color = TextSecondary, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 3.dp).background(CoralAccent, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text("Costs", color = TextSecondary, fontSize = 11.sp)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (points.all { it.savings == 0f && it.costs == 0f }) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "No matching financial logs for this interval",
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val currencySymbol = LocalCurrencySymbol.current
                    val paddingLeft = 45.dp
                    val paddingRight = 10.dp
                    val paddingTop = 15.dp
                    val paddingBottom = 25.dp
                    
                    val textColorVal = run {
                        val a = (TextSecondary.alpha * 255f).toInt()
                        val r = (TextSecondary.red * 255f).toInt()
                        val g = (TextSecondary.green * 255f).toInt()
                        val b = (TextSecondary.blue * 255f).toInt()
                        (a shl 24) or (r shl 16) or (g shl 8) or b
                    }
                    val textPaintY = remember(textColorVal) {
                        android.graphics.Paint().apply {
                            color = textColorVal
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    }
                    val textPaintX = remember(textColorVal) {
                        android.graphics.Paint().apply {
                            color = textColorVal
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    }
                    
                    val pathEffect = remember {
                        androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    }
                    
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures { offset ->
                                    val widthPx = size.width
                                    val pLeftPx = paddingLeft.toPx()
                                    val pRightPx = paddingRight.toPx()
                                    val workableWidth = widthPx - pLeftPx - pRightPx
                                    val stepX = if (points.size > 1) workableWidth / (points.size - 1) else workableWidth
                                    
                                    val relativeX = offset.x - pLeftPx
                                    val index = (relativeX / stepX).roundToInt()
                                    if (index in points.indices) {
                                        selectedIndex = if (selectedIndex == index) -1 else index
                                    }
                                }
                            }
                    ) {
                        val widthVec = size.width
                        val heightVec = size.height
                        
                        val pLeft = paddingLeft.toPx()
                        val pRight = paddingRight.toPx()
                        val pTop = paddingTop.toPx()
                        val pBottom = paddingBottom.toPx()
                        
                        val chartW = widthVec - pLeft - pRight
                        val chartH = heightVec - pTop - pBottom
                        
                        val originalMax = points.maxOf { maxOf(it.savings, it.costs) }
                        val maxAmt = if (originalMax <= 0f) 50f else originalMax * 1.15f
                        
                        val gridLevels = 3
                        for (g in 0..gridLevels) {
                            val ratio = g.toFloat() / gridLevels
                            val gridY = heightVec - pBottom - (ratio * chartH)
                            val gridVal = ratio * maxAmt
                            
                            drawLine(
                                color = SlateBorder.copy(alpha = 0.3f),
                                start = androidx.compose.ui.geometry.Offset(pLeft, gridY),
                                end = androidx.compose.ui.geometry.Offset(widthVec - pRight, gridY),
                                strokeWidth = 1f
                            )
                            
                            drawContext.canvas.nativeCanvas.drawText(
                                String.format(Locale.US, "%s%.0f", currencySymbol, gridVal),
                                pLeft - 10f,
                                gridY + 8f,
                                textPaintY
                            )
                        }
                        
                        val stepX = if (points.size > 1) chartW / (points.size - 1) else chartW
                        val savingsPath = Path()
                        val costsPath = Path()
                        
                        val savingsPoints = mutableListOf<androidx.compose.ui.geometry.Offset>()
                        val costsPoints = mutableListOf<androidx.compose.ui.geometry.Offset>()
                        
                        points.forEachIndexed { idx, pt ->
                            val x = pLeft + idx * stepX
                            
                            val ySav = heightVec - pBottom - ((pt.savings / maxAmt) * chartH)
                            val yCos = heightVec - pBottom - ((pt.costs / maxAmt) * chartH)
                            
                            val savOffset = androidx.compose.ui.geometry.Offset(x, ySav)
                            val cosOffset = androidx.compose.ui.geometry.Offset(x, yCos)
                            
                            savingsPoints.add(savOffset)
                            costsPoints.add(cosOffset)
                            
                            if (idx == 0) {
                                savingsPath.moveTo(x, ySav)
                                costsPath.moveTo(x, yCos)
                            } else {
                                savingsPath.lineTo(x, ySav)
                                costsPath.lineTo(x, yCos)
                            }
                            
                            drawContext.canvas.nativeCanvas.drawText(
                                pt.label,
                                x,
                                heightVec - 4f,
                                textPaintX
                            )
                        }
                        
                        val savingsFillPath = Path().apply {
                            addPath(savingsPath)
                            lineTo(pLeft + (points.size - 1) * stepX, heightVec - pBottom)
                            lineTo(pLeft, heightVec - pBottom)
                            close()
                        }
                        drawPath(
                            path = savingsFillPath,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(EmeraldAccent.copy(alpha = 0.15f), Color.Transparent),
                                startY = pTop,
                                endY = heightVec - pBottom
                            )
                        )
                        
                        val costsFillPath = Path().apply {
                            addPath(costsPath)
                            lineTo(pLeft + (points.size - 1) * stepX, heightVec - pBottom)
                            lineTo(pLeft, heightVec - pBottom)
                            close()
                        }
                        drawPath(
                            path = costsFillPath,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(CoralAccent.copy(alpha = 0.15f), Color.Transparent),
                                startY = pTop,
                                endY = heightVec - pBottom
                            )
                        )
                        
                        drawPath(
                            path = savingsPath,
                            color = EmeraldAccent,
                            style = Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        drawPath(
                            path = costsPath,
                            color = CoralAccent,
                            style = Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        
                        points.forEachIndexed { idx, _ ->
                            drawCircle(
                                color = EmeraldAccent,
                                radius = 6f,
                                center = savingsPoints[idx]
                            )
                            drawCircle(
                                color = SlateSurface,
                                radius = 3f,
                                center = savingsPoints[idx]
                            )
                            
                            drawCircle(
                                color = CoralAccent,
                                radius = 6f,
                                center = costsPoints[idx]
                            )
                            drawCircle(
                                color = SlateSurface,
                                radius = 3f,
                                center = costsPoints[idx]
                            )
                        }
                        
                        if (selectedIndex in points.indices) {
                            val activeX = pLeft + selectedIndex * stepX
                            
                            drawLine(
                                color = ElegantPurpleAccent.copy(alpha = 0.8f),
                                start = androidx.compose.ui.geometry.Offset(activeX, pTop),
                                end = androidx.compose.ui.geometry.Offset(activeX, heightVec - pBottom),
                                strokeWidth = 2f,
                                pathEffect = pathEffect
                            )
                            
                            drawCircle(
                                color = EmeraldAccent,
                                radius = 10f,
                                center = savingsPoints[selectedIndex]
                            )
                            drawCircle(
                                color = SlateDark,
                                radius = 4f,
                                center = savingsPoints[selectedIndex]
                            )
                            
                            drawCircle(
                                color = CoralAccent,
                                radius = 10f,
                                center = costsPoints[selectedIndex]
                            )
                            drawCircle(
                                color = SlateDark,
                                radius = 4f,
                                center = costsPoints[selectedIndex]
                            )
                        }
                    }
                }
            }
            
            if (selectedIndex in points.indices) {
                val point = points[selectedIndex]
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateDark, RoundedCornerShape(12.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Period Label", color = TextSecondary, fontSize = 9.sp)
                        Text(text = point.label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(EmeraldAccent))
                            Spacer(Modifier.width(4.dp))
                            Text(text = "Savings Added", color = TextSecondary, fontSize = 9.sp)
                        }
                        Text(text = formatPrice(point.savings), color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(CoralAccent))
                            Spacer(Modifier.width(4.dp))
                            Text(text = "Costs Logged", color = TextSecondary, fontSize = 9.sp)
                        }
                        Text(text = formatPrice(point.costs), color = CoralAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// SCREEN 1: OVERVIEW SCREEN
// ------------------------------------------------------------------------------------------------
@Composable
fun OverviewScreen(
    summary: FinancialSummary,
    banks: List<BankAccount>,
    costs: List<Cost>,
    savings: List<Saving>,
    onAddBank: () -> Unit,
    onDeleteBank: (BankAccount) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Cash limits layout with bold minimalist styling
        item {
            FinancialSummaryCard(summary)
        }

        // Beautiful Interactive Line Graph Trends
        item {
            InteractiveTrendChart(costs = costs, savings = savings)
        }

        // Bank Wallets Dashboard section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bank Wallets & Savings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Button(
                    onClick = onAddBank,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_bank_button")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Bank",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Add Bank", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (banks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = "",
                            tint = Color.LightGray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No Bank Wallets Added",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Register different banks to track savings separately.",
                            fontSize = 12.sp,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    banks.chunked(2).forEach { rowBanks ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowBanks.forEach { bank ->
                                val balance = summary.bankBalances[bank.id] ?: bank.initialBalance
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(SlateBorder)
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        IconButton(
                                            onClick = { onDeleteBank(bank) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .offset(x = (-4).dp, y = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = CoralAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = EmeraldAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = bank.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 14.sp
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = formatPrice(balance),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = EmeraldAccent
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowBanks.size < 2) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Daily aggregated cost summaries (reduces from Income layout)
        item {
            Text(
                text = "Daily Cost Ledger (Reduces monthly income)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (summary.dailyCosts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No costs recorded yet. Create daily expenses to view tracking.",
                            fontSize = 12.sp,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val sortedDaily = summary.dailyCosts.toList().sortedByDescending { it.first }.take(5)
            items(sortedDaily) { (dateStr, total) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CoralSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = CoralAccent)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = formatDateString(dateStr),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Daily cost aggregation",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                        Text(
                            text = formatPriceSigned(total.toDouble(), false),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = CoralAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialSummaryCard(summary: FinancialSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ElegantPurpleAccent),
        shape = RoundedCornerShape(24.dp),
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "CASH ON HAND (Pocket)",
                color = ElegantPurpleOnAccent.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = formatPrice(summary.cashInHand),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = ElegantPurpleOnAccent
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = ElegantPurpleOnAccent.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElegantPurpleOnAccent)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Monthly Incomes", color = ElegantPurpleOnAccent.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        text = formatPrice(summary.totalIncomeThisMonth),
                        color = ElegantPurpleOnAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElegantPurpleOnAccent.copy(alpha = 0.6f))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Monthly Costs", color = ElegantPurpleOnAccent.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        text = formatPrice(summary.totalCostThisMonth),
                        color = ElegantPurpleOnAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Total Combined Bank Savings Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElegantPurpleOnAccent.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Combined Savings in Banks", color = ElegantPurpleOnAccent.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = formatPrice(summary.totalBankSavings),
                    color = ElegantPurpleOnAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// SCREEN 2: INCOMES & SAVINGS
// ------------------------------------------------------------------------------------------------
@Composable
fun IncomeScreen(
    incomes: List<Income>,
    savings: List<Saving>,
    banks: List<BankAccount>,
    summary: FinancialSummary,
    onAddIncome: () -> Unit,
    onAddSaving: () -> Unit,
    onAddBank: () -> Unit,
    onDeleteIncome: (Income) -> Unit,
    onDeleteSaving: (Saving) -> Unit
) {
    var filterBySavingsTab by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Card summarizing income stream
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (filterBySavingsTab) "SAVINGS DEPOSIT TOTAL" else "TOTAL ACCUMULATED INCOMES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (filterBySavingsTab) {
                        formatPrice(savings.sumOf { it.amount })
                    } else {
                        formatPrice(incomes.sumOf { it.amount })
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldAccent
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Selector buttons to switch between viewing Income and separate Savings deposits
        TabRow(
            selectedTabIndex = if (filterBySavingsTab) 1 else 0,
            containerColor = SlateDark,
            contentColor = EmeraldAccent,
            divider = {}
        ) {
            Tab(
                selected = !filterBySavingsTab,
                onClick = { filterBySavingsTab = false },
                text = { Text("Income Streams", fontSize = 13.sp) }
            )
            Tab(
                selected = filterBySavingsTab,
                onClick = { filterBySavingsTab = true },
                text = { Text("Savings Deposits", fontSize = 13.sp) }
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (!filterBySavingsTab) "Active Income Records" else "Manual Savings Entries",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddBank,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("add_bank_from_income_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Add Bank Wallet",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Add Wallet",
                        color = EmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { if (filterBySavingsTab) onAddSaving() else onAddIncome() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag(if (filterBySavingsTab) "add_saving_btn" else "add_income_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (filterBySavingsTab) "Add Saving" else "Add Income",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (!filterBySavingsTab) {
            if (incomes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No incomes registered yet. Tap 'Add Income'.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(incomes) { income ->
                        val bankName = banks.find { it.id == income.bankId }?.name ?: "Cash on Hand"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateSurface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = income.description,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Wallet: $bankName",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "• ${formatDate(income.date)}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatPriceSigned(income.amount, true),
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldAccent,
                                        fontSize = 16.sp
                                    )
                                    IconButton(onClick = { onDeleteIncome(income) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralAccent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Wallets Section Title
                item {
                    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                        Text(
                            text = "Registered Wallets / Bank Accounts",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (banks.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateSurface.copy(alpha = 0.5f)),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No wallets/bank accounts created yet. Use 'Add Wallet' above.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(banks) { bank ->
                        val currBalance = summary.bankBalances[bank.id] ?: bank.initialBalance
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateDark),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = bank.name,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Initial Deposit: ${formatPrice(bank.initialBalance)}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Current Balance",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = formatPrice(currBalance),
                                        color = if (currBalance >= 0) EmeraldAccent else CoralAccent,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Separator Spacer
                item {
                    Spacer(Modifier.height(8.dp))
                }

                // 2. Manual Savings Section Title
                item {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            text = "Manual Savings Deposits Logs",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (savings.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateSurface.copy(alpha = 0.5f)),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No manual savings records found.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(savings) { saving ->
                        val bankName = banks.find { it.id == saving.bankId }?.name ?: "Unknown Bank"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateSurface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = saving.description,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Bank: $bankName",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "• ${formatDate(saving.date)}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatPriceSigned(saving.amount, true),
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldAccent,
                                        fontSize = 15.sp
                                    )
                                    IconButton(onClick = { onDeleteSaving(saving) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralAccent, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// SCREEN 3: COSTS SCREEN
// ------------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CostsScreen(
    costs: List<Cost>,
    banks: List<BankAccount>,
    summary: FinancialSummary,
    onAddCost: () -> Unit,
    onDeleteCost: (Cost) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Rent", "Food", "Groceries", "Stationery", "Phone Charges", "Transport", "Others")

    val displayedCosts = remember(costs, selectedCategoryFilter) {
        if (selectedCategoryFilter != null && selectedCategoryFilter != "All") {
            costs.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        } else {
            costs
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Card displaying total and categories
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TOTAL MONTHLY COSTS OUTFLOW",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatPrice(displayedCosts.sumOf { it.amount }),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = CoralAccent
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Categories pill container
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = (cat == "All" && selectedCategoryFilter == null) || (selectedCategoryFilter == cat)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CoralAccent else SlateSurface)
                        .clickable {
                            selectedCategoryFilter = if (cat == "All") null else cat
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registered Expenses Log",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Button(
                onClick = onAddCost,
                colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_cost_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Cost", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        if (displayedCosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No expenditures recorded in this group.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedCosts) { cost ->
                    val bankName = banks.find { it.id == cost.bankId }?.name ?: "Cash on Hand"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cost.category,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = CoralAccent,
                                        modifier = Modifier
                                            .background(CoralSurface, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    if (cost.subCategory != null) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = cost.subCategory,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .background(AmberAccent, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = cost.description,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Paid from: $bankName",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "• ${formatDate(cost.date)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatPriceSigned(cost.amount, false),
                                    fontWeight = FontWeight.Black,
                                    color = CoralAccent,
                                    fontSize = 16.sp
                                )
                                IconButton(onClick = { onDeleteCost(cost) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// SCREEN 4: ATM HISTORY Logs
// ------------------------------------------------------------------------------------------------
@Composable
fun AtmScreen(
    withdrawals: List<AtmWithdrawal>,
    banks: List<BankAccount>,
    onAddWithdrawal: () -> Unit,
    onDeleteWithdrawal: (AtmWithdrawal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalAtm, contentDescription = null, tint = EmeraldAccent)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ATM WITHDRAWALS COUNTER",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatPrice(withdrawals.sumOf { it.amount }),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "ATM withdrawals subtract from the bank's savings and insert cash into your hand.",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Atm Transaction history",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Button(
                onClick = onAddWithdrawal,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_atm_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Withdrawal", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        if (withdrawals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No ATM operations logged.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(withdrawals) { atm ->
                    val bankName = banks.find { it.id == atm.bankId }?.name ?: "Unknown Bank"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ATM Cash Withdrawal",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Source: $bankName", color = TextSecondary, fontSize = 11.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("• ${formatDate(atm.date)}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatPrice(atm.amount),
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldAccent,
                                    fontSize = 16.sp
                                )
                                IconButton(onClick = { onDeleteWithdrawal(atm) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// SCREEN 5: LOANS MODULE (Taken & Provided, with accrued interest calculations)
// ------------------------------------------------------------------------------------------------
@Composable
fun LoansScreen(
    loans: List<Loan>,
    loanInstallments: List<LoanInstallment>,
    banks: List<BankAccount>,
    onAddLoan: () -> Unit,
    onSettledToggle: (Loan) -> Unit,
    onDeleteLoan: (Loan) -> Unit,
    onAddInstallment: (Int, Double, Int?, Long, String) -> Unit,
    onDeleteInstallment: (LoanInstallment) -> Unit
) {
    var filterType by rememberSaveable { mutableStateOf("TAKEN") } // "TAKEN" vs "PROVIDED"
    var activeLoanForInstallment by remember { mutableStateOf<Loan?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dynamic Loans Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACTIVE LOANS OUTSTANDING",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val activeLoans = loans.filter { !it.isSettled }
                val totalTakenPrincipal = activeLoans.filter { it.type == "TAKEN" }.sumOf { loan ->
                    val paid = loanInstallments.filter { it.loanId == loan.id }.sumOf { it.amount }
                    maxOf(0.0, loan.amount - paid)
                }
                val totalProvidedPrincipal = activeLoans.filter { it.type == "PROVIDED" }.sumOf { loan ->
                    val paid = loanInstallments.filter { it.loanId == loan.id }.sumOf { it.amount }
                    maxOf(0.0, loan.amount - paid)
                }

                // Accrued interest calculates in real-time
                val totalTakenInterest = activeLoans.filter { it.type == "TAKEN" }.sumOf { loan ->
                    val paid = loanInstallments.filter { it.loanId == loan.id }.sumOf { it.amount }
                    val netAmount = maxOf(0.0, loan.amount - paid)
                    val elapsedMs = System.currentTimeMillis() - loan.date
                    val elapsedDays = elapsedMs / (1000 * 60 * 60 * 24)
                    val elapsedYears = elapsedDays.toDouble() / 365.25
                    netAmount * (loan.interestRate / 100.0) * elapsedYears
                }
                val totalProvidedInterest = activeLoans.filter { it.type == "PROVIDED" }.sumOf { loan ->
                    val paid = loanInstallments.filter { it.loanId == loan.id }.sumOf { it.amount }
                    val netAmount = maxOf(0.0, loan.amount - paid)
                    val elapsedMs = System.currentTimeMillis() - loan.date
                    val elapsedDays = elapsedMs / (1000 * 60 * 60 * 24)
                    val elapsedYears = elapsedDays.toDouble() / 365.25
                    netAmount * (loan.interestRate / 100.0) * elapsedYears
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Loans Taken (Owed)", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = formatPrice(totalTakenPrincipal + totalTakenInterest),
                            fontWeight = FontWeight.Bold,
                            color = CoralAccent,
                            fontSize = 16.sp
                        )
                        if (totalTakenInterest > 0) {
                            Text(
                                text = "Incl. ${formatPrice(totalTakenInterest)} accrued int.",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Loans Provided (Collectible)", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = formatPrice(totalProvidedPrincipal + totalProvidedInterest),
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent,
                            fontSize = 16.sp
                        )
                        if (totalProvidedInterest > 0) {
                            Text(
                                text = "Incl. ${formatPrice(totalProvidedInterest)} accrued int.",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Switcher button for loan types
        TabRow(
            selectedTabIndex = if (filterType == "TAKEN") 0 else 1,
            containerColor = SlateDark,
            contentColor = AmberAccent,
            divider = {}
        ) {
            Tab(
                selected = filterType == "TAKEN",
                onClick = { filterType = "TAKEN" },
                text = { Text("Loans I Owe (Taken)", fontSize = 13.sp) }
            )
            Tab(
                selected = filterType == "PROVIDED",
                onClick = { filterType = "PROVIDED" },
                text = { Text("Loans Owed To Me (Provided)", fontSize = 13.sp) }
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (filterType == "TAKEN") "My Borrowed Debts" else "My Custom Assets",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Button(
                onClick = onAddLoan,
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_loan_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Create Loan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        val currentLoans = loans.filter { it.type == filterType }

        if (currentLoans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No items recorded in this section.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentLoans) { loan ->
                    // Dynamic live interest accrual computation
                    val elapsedMs = System.currentTimeMillis() - loan.date
                    val elapsedDays = elapsedMs / (1000 * 60 * 60 * 24)
                    val elapsedYears = elapsedDays.toDouble() / 365.25

                    val filteredInstallments = loanInstallments.filter { it.loanId == loan.id }
                    val totalPaidInstallments = filteredInstallments.sumOf { it.amount }
                    val netAmount = maxOf(0.0, loan.amount - totalPaidInstallments)

                    val interestAccrued = if (loan.isSettled) 0.0 else netAmount * (loan.interestRate / 100.0) * elapsedYears
                    val netTotal = netAmount + interestAccrued

                    val accentColor = if (filterType == "TAKEN") CoralAccent else EmeraldAccent

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (loan.isSettled) SlateBorder else accentColor.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (loan.isSettled) "SETTLED" else "ACTIVE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (loan.isSettled) TextSecondary else accentColor,
                                            modifier = Modifier
                                                .background(
                                                    if (loan.isSettled) SlateBorder else accentColor.copy(alpha = 0.15f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                        if (loan.interestRate > 0.0) {
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "${loan.interestRate}% Ann. Rate",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = AmberAccent,
                                                modifier = Modifier
                                                    .background(SlateDark, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = loan.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = loan.description,
                                        fontSize = 13.sp,
                                        color = TextSecondary.copy(alpha = 0.9f)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatPrice(netTotal),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = if (loan.isSettled) TextSecondary else accentColor
                                    )
                                    Text(
                                        text = "Start: ${formatDate(loan.date)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (!loan.isSettled && loan.interestRate > 0.0 && netAmount > 0.0) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SlateDark, RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Interest accumulated (${elapsedDays} days)", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = formatPriceSigned(interestAccrued, true),
                                        color = AmberAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Installment Payment History Section inside the Card
                            if (filteredInstallments.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SlateDark, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Installment History", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Total Paid: ${formatPrice(totalPaidInstallments)}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    filteredInstallments.forEach { inst ->
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val bank = banks.find { it.id == inst.bankId }
                                                Text(
                                                    text = "${formatDate(inst.date)} (${bank?.name ?: "Cash"})",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                                if (inst.note.isNotEmpty()) {
                                                    Text(
                                                        text = " - ${inst.note}",
                                                        color = TextSecondary.copy(alpha = 0.7f),
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(start = 4.dp)
                                                    )
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = formatPrice(inst.amount),
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = { onDeleteInstallment(inst) },
                                                    modifier = Modifier.size(18.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Delete Installment",
                                                        tint = CoralAccent,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = SlateBorder)
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!loan.isSettled && netAmount > 0.0) {
                                    Button(
                                        onClick = { activeLoanForInstallment = loan },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = if (loan.type == "TAKEN") "Pay Installment" else "Receive Installment",
                                            fontSize = 11.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                }

                                OutlinedButton(
                                    onClick = { onSettledToggle(loan) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = if (loan.isSettled) "Reactivate" else "Mark Settled",
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                IconButton(
                                    onClick = { onDeleteLoan(loan) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = CoralAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    activeLoanForInstallment?.let { loan ->
        AddInstallmentDialog(
            loan = loan,
            banks = banks,
            onDismiss = { activeLoanForInstallment = null },
            onConfirm = { amount, bankId, date, note ->
                onAddInstallment(loan.id, amount, bankId, date, note)
                activeLoanForInstallment = null
            }
        )
    }
}

// ------------------------------------------------------------------------------------------------
// DIALOG OVERLAY COMPONENTS & HELPERS
// ------------------------------------------------------------------------------------------------

@Composable
fun AddBankDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    val symbol = LocalCurrencySymbol.current
    var name by remember { mutableStateOf("") }
    var initialBalanceStr by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bank Wallet", fontWeight = FontWeight.Bold, color = TextPrimary) },
        containerColor = SlateSurface,
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bank Name (e.g. Chase, Cash)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = initialBalanceStr,
                    onValueChange = { initialBalanceStr = it },
                    label = { Text("Initial Balance ($symbol)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CoralAccent, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Bank name cannot be blank"
                        return@Button
                    }
                    val balance = initialBalanceStr.toDoubleOrNull() ?: 0.0
                    onConfirm(name, balance)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Double Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddIncomeDialog(
    banks: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int?, Long) -> Unit
) {
    val symbol = LocalCurrencySymbol.current
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedBankId by remember { mutableStateOf<Int?>(null) }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf("") }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Income Stream", fontWeight = FontWeight.Bold, color = TextPrimary) },
        containerColor = SlateSurface,
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($symbol)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Custom drop selector styled precisely
                Text("Deposit Wallet Destination", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark)
                        .clickable { bankDropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    val activeBankName = banks.find { it.id == selectedBankId }?.name ?: "Cash on Hand"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(activeBankName, color = TextPrimary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cash on Hand", color = TextPrimary) },
                            onClick = {
                                selectedBankId = null
                                bankDropdownExpanded = false
                            }
                        )
                        banks.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.name, color = TextPrimary) },
                                onClick = {
                                    selectedBankId = b.id
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Simple custom direct numerical date chooser
                Spacer(Modifier.height(12.dp))
                Text("Date Selection", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateDark, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Recording date will defaults to today.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = formatDate(dateMs),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldAccent
                    )
                }

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CoralAccent, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDescription = if (description.isBlank()) "Income Stream" else description
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        error = "Amount must be greater than zero."
                        return@Button
                    }
                    onConfirm(finalDescription, amt, selectedBankId, dateMs)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Register Stream", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddCostDialog(
    banks: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String, Double, Int?, Long) -> Unit
) {
    val symbol = LocalCurrencySymbol.current
    var category by remember { mutableStateOf("Rent") }
    var subCategory by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedBankId by remember { mutableStateOf<Int?>(null) }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf("") }

    var catDropdownExpanded by remember { mutableStateOf(false) }
    var subDropdownExpanded by remember { mutableStateOf(false) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

    val mainCategories = listOf("Rent", "Food", "Groceries", "Stationery", "Phone Charges", "Transport", "Others")
    val foodSubCategories = listOf("Breakfast", "Lunch", "Dinner", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Cost Expenditure", fontWeight = FontWeight.Bold, color = TextPrimary) },
        containerColor = SlateSurface,
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                // Category drop selection
                Text("Main Category", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark)
                        .clickable { catDropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category, color = TextPrimary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = catDropdownExpanded,
                        onDismissRequest = { catDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        mainCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = TextPrimary) },
                                onClick = {
                                    category = cat
                                    // Default subcategory if Food is selected
                                    if (cat == "Food") {
                                        subCategory = "Breakfast"
                                    } else {
                                        subCategory = null
                                    }
                                    catDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // SubCategory Drop Select for Food
                if (category == "Food") {
                    Spacer(Modifier.height(12.dp))
                    Text("Food Meal Type", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateDark)
                            .clickable { subDropdownExpanded = true }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subCategory ?: "Breakfast", color = TextPrimary)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                        }

                        DropdownMenu(
                            expanded = subDropdownExpanded,
                            onDismissRequest = { subDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .background(SlateSurface)
                        ) {
                            foodSubCategories.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub, color = TextPrimary) },
                                    onClick = {
                                        subCategory = sub
                                        subDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = CoralAccent,
                        focusedBorderColor = CoralAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($symbol)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = CoralAccent,
                        focusedBorderColor = CoralAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Payment Wallet Source (Cash pocket or specific bank account)
                Text("Payment Wallet Source", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark)
                        .clickable { bankDropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    val walletName = banks.find { it.id == selectedBankId }?.name ?: "Cash on Hand"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(walletName, color = TextPrimary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cash on Hand", color = TextPrimary) },
                            onClick = {
                                selectedBankId = null
                                bankDropdownExpanded = false
                            }
                        )
                        banks.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.name, color = TextPrimary) },
                                onClick = {
                                    selectedBankId = b.id
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CoralAccent, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDescription = if (description.isBlank()) (subCategory ?: category) else description
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        error = "Amount must be greater than zero."
                        return@Button
                    }
                    onConfirm(category, subCategory, finalDescription, amt, selectedBankId, dateMs)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
            ) {
                Text("Log Cost", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddSavingDialog(
    banks: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, Double, Long) -> Unit
) {
    val symbol = LocalCurrencySymbol.current
    val initialBankId = remember(banks) { banks.firstOrNull()?.id ?: 0 }
    var selectedBankId by remember { mutableStateOf(initialBankId) }

    LaunchedEffect(banks) {
        if (banks.isNotEmpty() && !banks.any { it.id == selectedBankId }) {
            selectedBankId = banks.first().id
        }
    }

    var description by remember { mutableStateOf("Monthly Savings Deposit") }
    var amountStr by remember { mutableStateOf("") }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    if (banks.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = SlateSurface,
            title = { Text("Action Blocked", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Please create a Bank Wallet first to add savings separately.", color = TextSecondary) },
            confirmButton = {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)) {
                    Text("OK", color = Color.Black)
                }
            }
        )
    } else {
        AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Savings Addition", fontWeight = FontWeight.Bold, color = TextPrimary) },
        containerColor = SlateSurface,
        text = {
            Column {
                Text("Savings Bank Account", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark)
                        .clickable { dropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    val bankName = banks.find { it.id == selectedBankId }?.name ?: "Unknown"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(bankName, color = TextPrimary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        banks.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.name, color = TextPrimary) },
                                onClick = {
                                    selectedBankId = b.id
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Transfer Amount ($symbol)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CoralAccent, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isBlank()) {
                        error = "Description cannot be blank"
                        return@Button
                    }
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        error = "Please enter positive currency."
                        return@Button
                    }
                    onConfirm(selectedBankId, description, amt, dateMs)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Process Deposit", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                Text("Cancel")
            }
        }
    )
  }
}

@Composable
fun AddAtmDialog(
    banks: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, Long, String) -> Unit
) {
    val symbol = LocalCurrencySymbol.current
    val initialBankId = remember(banks) { banks.firstOrNull()?.id ?: 0 }
    var selectedBankId by remember { mutableStateOf(initialBankId) }

    LaunchedEffect(banks) {
        if (banks.isNotEmpty() && !banks.any { it.id == selectedBankId }) {
            selectedBankId = banks.first().id
        }
    }

    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("ATM Cash Withdrawal") }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    if (banks.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = SlateSurface,
            title = { Text("Action Blocked", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("You must register a Bank Wallet first to log withdrawals.", color = TextSecondary) },
            confirmButton = {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)) {
                    Text("OK", color = Color.Black)
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Configure ATM Withdrawal", fontWeight = FontWeight.Bold, color = TextPrimary) },
            containerColor = SlateSurface,
            text = {
                Column {
                    Text("Source Bank Wallet", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateDark)
                            .clickable { dropdownExpanded = true }
                            .padding(14.dp)
                    ) {
                        val bankName = banks.find { it.id == selectedBankId }?.name ?: "Unknown"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bankName, color = TextPrimary)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .background(SlateSurface)
                        ) {
                            banks.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b.name, color = TextPrimary) },
                                    onClick = {
                                        selectedBankId = b.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Withdrawal Amount ($symbol)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = EmeraldAccent,
                            focusedBorderColor = EmeraldAccent,
                            unfocusedLabelColor = TextSecondary,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Withdrawal Tag Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = EmeraldAccent,
                            focusedBorderColor = EmeraldAccent,
                            unfocusedLabelColor = TextSecondary,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(error, color = CoralAccent, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt <= 0.0) {
                            error = "Amount must be a valid number"
                            return@Button
                        }
                        onConfirm(selectedBankId, amt, dateMs, description)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("Process ATM Cash", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GoogleSignInDialog(
    savedEmails: Set<String>,
    onDismiss: () -> Unit,
    onLogin: (String) -> Unit,
    onRemoveSavedEmail: (String) -> Unit
) {
    var showEmailForm by remember { mutableStateOf(savedEmails.isEmpty()) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SlateSurface,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Google styled Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 28.sp)
                    Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 24.sp)
                }

                if (!showEmailForm) {
                    Text(
                        text = "Choose an account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "to continue to Finance Tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        savedEmails.forEach { email ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                    .clickable { onLogin(email) },
                                colors = CardDefaults.cardColors(containerColor = SlateDark)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                  ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4285F4)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = email.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = email,
                                            color = Color.White, // Always white on dark SlateDark card back
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRemoveSavedEmail(email) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove account from device",
                                            tint = CoralAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = { showEmailForm = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF4285F4))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Google account", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                    }
                } else {
                    Text(
                        text = "Sign in",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "with your Google Account",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            error = ""
                        },
                        label = { Text("Email or phone") },
                        placeholder = { Text("example@gmail.com") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color(0xFF4285F4),
                            unfocusedBorderColor = SlateBorder,
                            focusedLabelColor = Color(0xFF4285F4),
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            error = ""
                        },
                        label = { Text("Enter your password") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color(0xFF4285F4),
                            unfocusedBorderColor = SlateBorder,
                            focusedLabelColor = Color(0xFF4285F4),
                            unfocusedLabelColor = TextSecondary
                        ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = null, tint = TextSecondary)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(error, color = CoralAccent, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (savedEmails.isNotEmpty()) {
                            TextButton(onClick = { showEmailForm = false }) {
                                Text("Back to accounts", color = TextSecondary)
                            }
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                if (emailInput.isBlank()) {
                                    error = "Please enter your Gmail address."
                                    return@Button
                                }
                                if (!emailInput.contains("@") || !emailInput.endsWith(".com")) {
                                    error = "Please enter a valid Gmail address (e.g. user@gmail.com)."
                                    return@Button
                                }
                                if (passwordInput.isBlank()) {
                                    error = "Please enter your password."
                                    return@Button
                                }
                                if (passwordInput.length < 4) {
                                    error = "Password must be at least 4 characters long."
                                    return@Button
                                }
                                onLogin(emailInput.trim())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddLoanDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Double, Long) -> Unit
) {
    val symbol = LocalCurrencySymbol.current
    var type by remember { mutableStateOf("TAKEN") } // "TAKEN" (Owed) vs "PROVIDED" (Asset)
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var interestRateStr by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }

    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Debt/Loan Arrangement", fontWeight = FontWeight.Bold, color = TextPrimary) },
        containerColor = SlateSurface,
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Text("Arrangement Type", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark)
                        .clickable { typeDropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (type == "TAKEN") "Loan I Borrowed (Taken)" else "Loan I Provided (Asset)", color = TextPrimary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Loan I Borrowed (Taken)", color = TextPrimary) },
                            onClick = {
                                type = "TAKEN"
                                typeDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Loan I Lended (Provided)", color = TextPrimary) },
                            onClick = {
                                type = "PROVIDED"
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (Lender or Borrower)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = AmberAccent,
                        focusedBorderColor = AmberAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Arrangement Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = AmberAccent,
                        focusedBorderColor = AmberAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Loan Principal Amount ($symbol)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = AmberAccent,
                        focusedBorderColor = AmberAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = interestRateStr,
                    onValueChange = { interestRateStr = it },
                    label = { Text("Annual Interest Rate (%) (Defaults to 0)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = AmberAccent,
                        focusedBorderColor = AmberAccent,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CoralAccent, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank()) {
                        error = "Name and description cannot be empty"
                        return@Button
                    }
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val rate = interestRateStr.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        error = "Loan amount must be greater than zero."
                        return@Button
                    }
                    onConfirm(type, name, description, amt, rate, dateMs)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
            ) {
                Text("Process Loan", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddInstallmentDialog(
    loan: Loan,
    banks: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int?, Long, String) -> Unit
) {
    val symbol = LocalCurrencySymbol.current
    var amountStr by remember { mutableStateOf("") }
    var selectedBankId by remember { mutableStateOf<Int?>(null) } // null means Cash pocket
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val dateMs by remember { mutableStateOf(System.currentTimeMillis()) }

    var bankDropdownExpanded by remember { mutableStateOf(false) }

    val accentColor = if (loan.type == "TAKEN") AmberAccent else EmeraldAccent

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (loan.type == "TAKEN") "Pay Loan Installment" else "Receive Loan Installment",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        containerColor = SlateSurface,
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Text(
                    text = "Loan: ${loan.name} (${if (loan.type == "TAKEN") "Borrowed" else "Lended"})",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Installment Amount (${symbol})") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = accentColor,
                        focusedBorderColor = accentColor,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = if (loan.type == "TAKEN") "Pay From (Account/Cash)" else "Receive Into (Account/Cash)",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark)
                        .clickable { bankDropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    val activeBank = banks.find { it.id == selectedBankId }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(activeBank?.name ?: "Cash pocket", color = TextPrimary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cash pocket", color = TextPrimary) },
                            onClick = {
                                selectedBankId = null
                                bankDropdownExpanded = false
                            }
                        )
                        banks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank.name, color = TextPrimary) },
                                onClick = {
                                    selectedBankId = bank.id
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Installment Label / Note (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = accentColor,
                        focusedBorderColor = accentColor,
                        unfocusedLabelColor = TextSecondary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CoralAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        error = "Please enter a valid positive amount."
                    } else {
                        onConfirm(amt, selectedBankId, dateMs, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Log Payment", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}

// ------------------------------------------------------------------------------------------------
// GENERAL DATE TIME PARSERS Reference Formatter utilities
// ------------------------------------------------------------------------------------------------

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatDateString(dateStr: String): String {
    // dateStr is formatted "yyyy-MM-dd"
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return try {
        val d = parser.parse(dateStr)
        if (d != null) formatter.format(d) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}
