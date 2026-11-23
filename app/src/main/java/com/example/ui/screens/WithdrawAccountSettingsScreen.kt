package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SisaEnakUiState
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.BgLightGray
import com.example.ui.theme.BgWhite
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenBorder
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.FreshGreenSoft
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextGray

private val presetProviders = listOf(
    "BCA", "BRI", "Mandiri", "BNI", "BSI", "CIMB Niaga",
    "GoPay", "OVO", "DANA", "ShopeePay"
)

/**
 * Sub-screen Pengaturan: Rekening Penarikan (Bank / E-Wallet)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WithdrawAccountSettingsScreen(
    uiState: SisaEnakUiState,
    onBack: () -> Unit,
    onSaveAccount: (bankName: String, accountNumber: String, accountHolder: String) -> Unit,
    onClearAccount: () -> Unit
) {
    val prefs = uiState.userPrefs
    val savedBank = prefs.withdrawBankName
    val isSavedCustomBank = savedBank.isNotBlank() && savedBank !in presetProviders

    var selectedProvider by remember(savedBank) {
        mutableStateOf(
            when {
                savedBank.isBlank() -> "BCA"
                isSavedCustomBank -> "Lainnya"
                else -> savedBank
            }
        )
    }
    var customBankName by remember(savedBank) {
        mutableStateOf(if (isSavedCustomBank) savedBank else "")
    }
    var accountNumber by remember(prefs.withdrawAccountNumber) {
        mutableStateOf(prefs.withdrawAccountNumber)
    }
    var accountHolder by remember(prefs.withdrawAccountHolder, prefs.userName) {
        mutableStateOf(
            prefs.withdrawAccountHolder.ifBlank { prefs.userName }
        )
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val effectiveBankName = if (selectedProvider == "Lainnya") {
        customBankName.trim()
    } else {
        selectedProvider
    }

    val isBankValid = effectiveBankName.length >= 2
    val isNumberValid = accountNumber.length >= 8
    val isHolderValid = accountHolder.trim().length >= 2
    val canSave = isBankValid && isNumberValid && isHolderValid

    val isEWallet = selectedProvider in listOf("GoPay", "OVO", "DANA", "ShopeePay")

    Scaffold(
        containerColor = BgLightGray,
        topBar = {
            SubScreenTopBar(
                title = "Rekening Penarikan",
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(
                color = BgWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(0.5.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            onSaveAccount(effectiveBankName, accountNumber, accountHolder.trim())
                            onBack()
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FreshGreenDark,
                            contentColor = Color.White,
                            disabledContainerColor = SurfaceBorder,
                            disabledContentColor = TextGray
                        ),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("save_withdraw_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan Rekening Penarikan",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Status Rekening Tersimpan Saat Ini
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgWhite,
                    border = BorderStroke(
                        1.dp,
                        if (prefs.hasSavedWithdrawAccount) FreshGreenBorder else SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (prefs.hasSavedWithdrawAccount) FreshGreenSoft else BgLightGray,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                    tint = if (prefs.hasSavedWithdrawAccount) FreshGreenDark else TextGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (prefs.hasSavedWithdrawAccount) {
                                    "Rekening Utama Tersimpan"
                                } else {
                                    "Belum Ada Rekening Tersimpan"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextBlack
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (prefs.hasSavedWithdrawAccount) {
                                    prefs.formattedWithdrawAccount()
                                } else {
                                    "Simpan nomor rekening bank atau e-wallet agar pencairan Dompet LUDES langsung siap dipakai."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (prefs.hasSavedWithdrawAccount) FreshGreenDark else TextGray
                            )
                        }
                    }
                }

                // Form Rekening Penarikan
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Pilih Bank atau E-Wallet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextBlack
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (presetProviders + "Lainnya").forEach { provider ->
                                val isSelected = selectedProvider == provider
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) FreshGreenSoft else BgLightGray,
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) FreshGreen else SurfaceBorder
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedProvider = provider }
                                        .testTag("withdraw_provider_${provider.lowercase().replace(" ", "_")}")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = provider,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) FreshGreenDark else TextBlack
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedProvider == "Lainnya") {
                            OutlinedTextField(
                                value = customBankName,
                                onValueChange = { customBankName = it.take(30) },
                                label = { Text("Nama Bank / E-Wallet") },
                                placeholder = { Text("Contoh: Bank Jago, SeaBank, LinkAja") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FreshGreen,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedLabelColor = FreshGreenDark
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_custom_bank_input")
                            )
                        }

                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = { input ->
                                accountNumber = input.filter { it.isDigit() }.take(20)
                            },
                            label = {
                                Text(if (isEWallet) "Nomor HP E-Wallet ($selectedProvider)" else "Nomor Rekening")
                            },
                            placeholder = {
                                Text(if (isEWallet) "Contoh: 081234567890" else "Contoh: 1234567890")
                            },
                            singleLine = true,
                            isError = accountNumber.isNotEmpty() && !isNumberValid,
                            supportingText = {
                                Text(
                                    text = if (accountNumber.isNotEmpty() && !isNumberValid) {
                                        "Minimal 8 digit angka"
                                    } else {
                                        "Pastikan nomor aktif dan sesuai buku tabungan / akun e-wallet"
                                    },
                                    color = if (accountNumber.isNotEmpty() && !isNumberValid) LiveRed else TextGray
                                )
                            },
                            trailingIcon = {
                                if (accountNumber.isNotEmpty()) {
                                    IconButton(onClick = { accountNumber = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Hapus nomor rekening",
                                            tint = TextGray
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FreshGreen,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedLabelColor = FreshGreenDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_account_number_input")
                        )

                        OutlinedTextField(
                            value = accountHolder,
                            onValueChange = { accountHolder = it.take(50) },
                            label = { Text("Nama Pemilik Rekening") },
                            placeholder = { Text("Sesuai nama pada rekening") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = TextGray
                                )
                            },
                            singleLine = true,
                            isError = accountHolder.isNotEmpty() && !isHolderValid,
                            supportingText = {
                                if (accountHolder.isNotEmpty() && !isHolderValid) {
                                    Text("Masukkan nama pemilik minimal 2 karakter", color = LiveRed)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FreshGreen,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedLabelColor = FreshGreenDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_account_holder_input")
                        )
                    }
                }

                // Catatan Keamanan
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FreshGreenSoft,
                    border = BorderStroke(1.dp, FreshGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = FreshGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Data rekening tersimpan aman di perangkatmu dan hanya digunakan untuk pencairan saldo Dompet LUDES.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FreshGreenDark
                        )
                    }
                }

                if (prefs.hasSavedWithdrawAccount) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, LiveRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("delete_withdraw_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hapus Rekening Tersimpan", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = BgWhite,
            title = { Text("Hapus Rekening Tersimpan?") },
            text = {
                Text("Rekening ${prefs.formattedWithdrawAccount()} akan dihapus dari daftar tujuan penarikan tersimpan.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onClearAccount()
                        onBack()
                    }
                ) {
                    Text("Ya, Hapus", color = LiveRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = TextGray)
                }
            }
        )
    }
}
