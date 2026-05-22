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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
private val SlateDark = Color(0xFF1C1B1F)
private val SlateSurface = Color(0xFF2B2930)
private val SlateBorder = Color(0xFF49454F)
private val EmeraldAccent = Color(0xFFB5F2B8)
private val EmeraldSurface = Color(0xFF113111)
private val CoralAccent = Color(0xFFF2B8B5)
private val CoralSurface = Color(0xFF311111)
private val AmberAccent = Color(0xFFD0BCFF)

private val ElegantPurpleAccent = Color(0xFFD0BCFF)
private val ElegantPurpleOnAccent = Color(0xFF381E72)
private val ElegantPurpleDeep = Color(0xFF4F378B)

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
    var showProfileDrawer by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(FinanceTab.OVERVIEW) }
    val summary by viewModel.financialSummaryState.collectAsState()
    val banks by viewModel.bankAccountsState.collectAsState()
    val incomes by viewModel.incomesState.collectAsState()
    val costs by viewModel.costsState.collectAsState()
    val withdrawals by viewModel.atmWithdrawalsState.collectAsState()
    val savings by viewModel.savingsState.collectAsState()
    val loans by viewModel.loansState.collectAsState()

    // Dialog trigger states
    var showAddBank by remember { mutableStateOf(false) }
    var showAddIncome by remember { mutableStateOf(false) }
    var showAddCost by remember { mutableStateOf(false) }
    var showAddAtm by remember { mutableStateOf(false) }
    var showAddSaving by remember { mutableStateOf(false) }
    var showAddLoan by remember { mutableStateOf(false) }

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
                            color = Color(0xFFE6E1E5),
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDark,
                    titleContentColor = Color.White
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
                        label = { Text(tab.title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantPurpleAccent,
                            selectedTextColor = ElegantPurpleAccent,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
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
                        onAddLoan = { showAddLoan = true },
                        onSettledToggle = { viewModel.toggleLoanSettled(it) },
                        onDeleteLoan = { viewModel.deleteLoan(it) }
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
                        color = Color.White
                    )
                    IconButton(
                        onClick = { showProfileDrawer = false }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close drawer",
                            tint = Color.LightGray
                        )
                    }
                }

                // Profile card details (JD logo design)
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
                                text = "JD",
                                color = ElegantPurpleAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "John Doe",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Premium Member",
                            fontSize = 11.sp,
                            color = ElegantPurpleAccent
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
                            color = Color.LightGray.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                    
                    // Google Sign-In style button
                    Button(
                        onClick = {
                            Toast.makeText(context, "Google Sign-In initialized", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
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
                }

                Divider(color = SlateBorder.copy(alpha = 0.4f))

                // --- SETTINGS SECTION ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SETTINGS",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                    
                    val settingsList = listOf(
                        Triple(Icons.Default.Notifications, "Notifications", "Active"),
                        Triple(Icons.Default.Language, "Language", "English"),
                        Triple(Icons.Default.Lock, "Passcode lock", "Disabled")
                    )
                    
                    settingsList.forEach { (icon, title, status) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    Toast.makeText(context, "$title setting", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = Color.LightGray.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = status,
                                color = ElegantPurpleAccent.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Divider(color = SlateBorder.copy(alpha = 0.4f))

                // --- ABOUT US ---
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ABOUT US",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "T-Desk Solutions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "All right received.",
                        fontSize = 11.sp,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )
                }

                Divider(color = SlateBorder.copy(alpha = 0.4f))

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
                            color = Color.White
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
                                            color = Color.LightGray.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "thdhanushka31@gmail.com",
                                            fontSize = 12.sp,
                                            color = Color.White,
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
                                            color = Color.LightGray.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "+94 78 97 28 396",
                                            fontSize = 12.sp,
                                            color = Color.White,
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
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Tap on nodes to view precise logs",
                        color = Color.LightGray.copy(alpha = 0.6f),
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
                    Text("Savings", color = Color.LightGray, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 3.dp).background(CoralAccent, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text("Costs", color = Color.LightGray, fontSize = 11.sp)
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
                            tint = Color.LightGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "No matching financial logs for this interval",
                            color = Color.LightGray.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val paddingLeft = 45.dp
                    val paddingRight = 10.dp
                    val paddingTop = 15.dp
                    val paddingBottom = 25.dp
                    
                    val textPaintY = remember {
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#938F99")
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    }
                    val textPaintX = remember {
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#938F99")
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
                                    val stepX = workableWidth / (points.size - 1)
                                    
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
                                String.format(Locale.US, "$%.0f", gridVal),
                                pLeft - 10f,
                                gridY + 8f,
                                textPaintY
                            )
                        }
                        
                        val stepX = chartW / (points.size - 1)
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
                        Text(text = "Period Label", color = Color.LightGray, fontSize = 9.sp)
                        Text(text = point.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(EmeraldAccent))
                            Spacer(Modifier.width(4.dp))
                            Text(text = "Savings Added", color = Color.LightGray, fontSize = 9.sp)
                        }
                        Text(text = String.format(Locale.getDefault(), "$%.2f", point.savings), color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(CoralAccent))
                            Spacer(Modifier.width(4.dp))
                            Text(text = "Costs Logged", color = Color.LightGray, fontSize = 9.sp)
                        }
                        Text(text = String.format(Locale.getDefault(), "$%.2f", point.costs), color = CoralAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(banks) { bank ->
                        val balance = summary.bankBalances[bank.id] ?: bank.initialBalance
                        Card(
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
                                        text = String.format(Locale.getDefault(), "$%.2f", balance),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = EmeraldAccent
                                    )
                                }
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
                            text = String.format(Locale.getDefault(), "-$%.2f", total),
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
                text = String.format(Locale.getDefault(), "$%.2f", summary.cashInHand),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = ElegantPurpleOnAccent
            )

            Spacer(Modifier.height(16.dp))
            Divider(color = ElegantPurpleOnAccent.copy(alpha = 0.2f))
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
                        text = String.format(Locale.getDefault(), "$%.2f", summary.totalIncomeThisMonth),
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
                        text = String.format(Locale.getDefault(), "$%.2f", summary.totalCostThisMonth),
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
                    text = String.format(Locale.getDefault(), "$%.2f", summary.totalBankSavings),
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
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (filterBySavingsTab) {
                        String.format(Locale.getDefault(), "$%.2f", savings.sumOf { it.amount })
                    } else {
                        String.format(Locale.getDefault(), "$%.2f", incomes.sumOf { it.amount })
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
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Button(
                onClick = { if (filterBySavingsTab) onAddSaving() else onAddIncome() },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag(if (filterBySavingsTab) "add_saving_btn" else "add_income_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (filterBySavingsTab) "Add Saving" else "Add Income",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
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
                    Text("No incomes registered yet. Tap 'Add Income'.", color = Color.LightGray)
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
                                        color = Color.White
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
                                            color = Color.LightGray
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "• ${formatDate(income.date)}",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "+$%.2f", income.amount),
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
            if (savings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No manual savings recorded yet.", color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savings) { saving ->
                        val bankName = banks.find { it.id == saving.bankId }?.name ?: "Unknown Bank"
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
                                        text = saving.description,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
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
                                            color = Color.LightGray
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "• ${formatDate(saving.date)}",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "+$%.2f", saving.amount),
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldAccent,
                                        fontSize = 16.sp
                                    )
                                    IconButton(onClick = { onDeleteSaving(saving) }) {
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
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f", displayedCosts.sumOf { it.amount }),
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
                        color = if (isSelected) Color.Black else Color.White,
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
                color = Color.White,
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
                Text("No expenditures recorded in this group.", color = Color.LightGray)
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
                                    color = Color.White
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Paid from: $bankName",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "• ${formatDate(cost.date)}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.getDefault(), "-$%.2f", cost.amount),
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
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f", withdrawals.sumOf { it.amount }),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "ATM withdrawals subtract from the bank's savings and insert cash into your hand.",
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.8f)
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
                color = Color.White,
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
                Text("No ATM operations logged.", color = Color.LightGray)
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
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Source: $bankName", color = Color.LightGray, fontSize = 11.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("• ${formatDate(atm.date)}", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.getDefault(), "$%.2f", atm.amount),
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
    onAddLoan: () -> Unit,
    onSettledToggle: (Loan) -> Unit,
    onDeleteLoan: (Loan) -> Unit
) {
    var filterType by rememberSaveable { mutableStateOf("TAKEN") } // "TAKEN" vs "PROVIDED"

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
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val activeLoans = loans.filter { !it.isSettled }
                val totalTakenPrincipal = activeLoans.filter { it.type == "TAKEN" }.sumOf { it.amount }
                val totalProvidedPrincipal = activeLoans.filter { it.type == "PROVIDED" }.sumOf { it.amount }

                // Accrued interest calculates in real-time
                val totalTakenInterest = activeLoans.filter { it.type == "TAKEN" }.sumOf { loan ->
                    val elapsedMs = System.currentTimeMillis() - loan.date
                    val elapsedDays = elapsedMs / (1000 * 60 * 60 * 24)
                    val elapsedYears = elapsedDays.toDouble() / 365.25
                    loan.amount * (loan.interestRate / 100.0) * elapsedYears
                }
                val totalProvidedInterest = activeLoans.filter { it.type == "PROVIDED" }.sumOf { loan ->
                    val elapsedMs = System.currentTimeMillis() - loan.date
                    val elapsedDays = elapsedMs / (1000 * 60 * 60 * 24)
                    val elapsedYears = elapsedDays.toDouble() / 365.25
                    loan.amount * (loan.interestRate / 100.0) * elapsedYears
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Loans Taken (Owed)", color = Color.LightGray, fontSize = 11.sp)
                        Text(
                            text = String.format(Locale.getDefault(), "$%.2f", totalTakenPrincipal + totalTakenInterest),
                            fontWeight = FontWeight.Bold,
                            color = CoralAccent,
                            fontSize = 16.sp
                        )
                        if (totalTakenInterest > 0) {
                            Text(
                                text = String.format(Locale.getDefault(), "Incl. $%.2f accrued int.", totalTakenInterest),
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Loans Provided (Collectible)", color = Color.LightGray, fontSize = 11.sp)
                        Text(
                            text = String.format(Locale.getDefault(), "$%.2f", totalProvidedPrincipal + totalProvidedInterest),
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent,
                            fontSize = 16.sp
                        )
                        if (totalProvidedInterest > 0) {
                            Text(
                                text = String.format(Locale.getDefault(), "Incl. $%.2f accrued int.", totalProvidedInterest),
                                fontSize = 10.sp,
                                color = Color.LightGray
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
                color = Color.White,
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
                Text("No items recorded in this section.", color = Color.LightGray)
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
                    val interestAccrued = if (loan.isSettled) 0.0 else loan.amount * (loan.interestRate / 100.0) * elapsedYears
                    val netTotal = loan.amount + interestAccrued

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
                                            color = if (loan.isSettled) Color.LightGray else accentColor,
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
                                        color = Color.White
                                    )
                                    Text(
                                        text = loan.description,
                                        fontSize = 13.sp,
                                        color = Color.LightGray.copy(alpha = 0.9f)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "$%.2f", netTotal),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = if (loan.isSettled) Color.LightGray else accentColor
                                    )
                                    Text(
                                        text = "Start: ${formatDate(loan.date)}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            if (!loan.isSettled && loan.interestRate > 0.0) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SlateDark, RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Interest accumulated (${elapsedDays} days)", color = Color.LightGray, fontSize = 11.sp)
                                    Text(
                                        text = String.format(Locale.getDefault(), "+$%.2f", interestAccrued),
                                        color = AmberAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Divider(color = SlateBorder)
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
}

// ------------------------------------------------------------------------------------------------
// DIALOG OVERLAY COMPONENTS & HELPERS
// ------------------------------------------------------------------------------------------------

@Composable
fun AddBankDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var initialBalanceStr by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bank Wallet", fontWeight = FontWeight.Bold, color = Color.White) },
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
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = initialBalanceStr,
                    onValueChange = { initialBalanceStr = it },
                    label = { Text("Initial Balance ($)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)) {
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
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedBankId by remember { mutableStateOf<Int?>(null) }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf("") }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Income Stream", fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = SlateSurface,
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Custom drop selector styled precisely
                Text("Deposit Wallet Destination", color = Color.LightGray, fontSize = 12.sp)
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
                        Text(activeBankName, color = Color.White)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
                    }

                    DropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cash on Hand", color = Color.White) },
                            onClick = {
                                selectedBankId = null
                                bankDropdownExpanded = false
                            }
                        )
                        banks.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.name, color = Color.White) },
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
                Text("Date Selection", color = Color.LightGray, fontSize = 12.sp)
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
                        color = Color.LightGray
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
                    if (description.isBlank()) {
                        error = "Description is blank"
                        return@Button
                    }
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        error = "Amount must be greater than zero."
                        return@Button
                    }
                    onConfirm(description, amt, selectedBankId, dateMs)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Register Stream", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)) {
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
        title = { Text("Log Cost Expenditure", fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = SlateSurface,
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                // Category drop selection
                Text("Main Category", color = Color.LightGray, fontSize = 12.sp)
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
                        Text(category, color = Color.White)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
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
                                text = { Text(cat, color = Color.White) },
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
                    Text("Food Meal Type", color = Color.LightGray, fontSize = 12.sp)
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
                            Text(subCategory ?: "Breakfast", color = Color.White)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
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
                                    text = { Text(sub, color = Color.White) },
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
                    label = { Text("Short Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = CoralAccent,
                        focusedBorderColor = CoralAccent,
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = CoralAccent,
                        focusedBorderColor = CoralAccent,
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Payment Wallet Source (Cash pocket or specific bank account)
                Text("Payment Wallet Source", color = Color.LightGray, fontSize = 12.sp)
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
                        Text(walletName, color = Color.White)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
                    }

                    DropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cash on Hand", color = Color.White) },
                            onClick = {
                                selectedBankId = null
                                bankDropdownExpanded = false
                            }
                        )
                        banks.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.name, color = Color.White) },
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
                    if (description.isBlank()) {
                        error = "Description cannot be blank"
                        return@Button
                    }
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        error = "Amount must be greater than zero."
                        return@Button
                    }
                    onConfirm(category, subCategory, description, amt, selectedBankId, dateMs)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
            ) {
                Text("Log Cost", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)) {
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
    if (banks.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = SlateSurface,
            title = { Text("Action Blocked", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Please create a Bank Wallet first to add savings separately.", color = Color.LightGray) },
            confirmButton = {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)) {
                    Text("OK", color = Color.Black)
                }
            }
        )
        return
    }

    var selectedBankId by remember { mutableStateOf(banks.first().id) }
    var description by remember { mutableStateOf("Monthly Savings Deposit") }
    var amountStr by remember { mutableStateOf("") }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Savings Addition", fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = SlateSurface,
        text = {
            Column {
                Text("Savings Bank Account", color = Color.LightGray, fontSize = 12.sp)
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
                        Text(bankName, color = Color.White)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
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
                                text = { Text(b.name, color = Color.White) },
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
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Transfer Amount ($)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddAtmDialog(
    banks: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, Long, String) -> Unit
) {
    if (banks.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = SlateSurface,
            title = { Text("Action Blocked", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("You must register a Bank Wallet first to log withdrawals.", color = Color.LightGray) },
            confirmButton = {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)) {
                    Text("OK", color = Color.Black)
                }
            }
        )
        return
    }

    var selectedBankId by remember { mutableStateOf(banks.first().id) }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("ATM Cash Withdrawal") }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure ATM Withdrawal", fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = SlateSurface,
        text = {
            Column {
                Text("Source Bank Wallet", color = Color.LightGray, fontSize = 12.sp)
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
                        Text(bankName, color = Color.White)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
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
                                text = { Text(b.name, color = Color.White) },
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
                    label = { Text("Withdrawal Amount ($)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = EmeraldAccent,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
        title = { Text("Log New Debt/Loan Arrangement", fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = SlateSurface,
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Text("Arrangement Type", color = Color.LightGray, fontSize = 12.sp)
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
                        Text(if (type == "TAKEN") "Loan I Borrowed (Taken)" else "Loan I Provided (Asset)", color = Color.White)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
                    }

                    DropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(SlateSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Loan I Borrowed (Taken)", color = Color.White) },
                            onClick = {
                                type = "TAKEN"
                                typeDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Loan I Lended (Provided)", color = Color.White) },
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
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Loan Principal Amount ($)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = AmberAccent,
                        focusedBorderColor = AmberAccent,
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
                        unfocusedLabelColor = Color.LightGray,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)) {
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
