package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.AppUser
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginDialog(
    currentUser: AppUser?,
    isFirebaseConnected: Boolean = true,
    onDismiss: () -> Unit,
    onLogin: (UserRole, String, String) -> Unit,
    onGoogleSignInSuccess: (name: String, email: String, photoUrl: String) -> Unit = { _, _, _ -> },
    onLogout: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentUser?.role ?: UserRole.CUSTOMER) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ChanWarmSurface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand Header
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(ChanOrangePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "CC+", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Masuk ke ChanChan+",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )

                Text(
                    text = "Pilih peran akun untuk melanjutkan",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Firebase Status Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFirebaseConnected) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (isFirebaseConnected) "🔥" else "⚠️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFirebaseConnected) "Firebase Firestore Database Aktif" else "Menyambungkan ke Cloud Database...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFirebaseConnected) ChanGreenSuccess else Color(0xFFE65100)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role Selector Tabs (Customer, Employee, Owner)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChanSurfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RoleSelectorPill(
                        label = "Pelanggan",
                        emoji = "👤",
                        isSelected = selectedRole == UserRole.CUSTOMER,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedRole = UserRole.CUSTOMER }
                    )
                    RoleSelectorPill(
                        label = "Karyawan",
                        emoji = "☕",
                        isSelected = selectedRole == UserRole.EMPLOYEE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedRole = UserRole.EMPLOYEE }
                    )
                    RoleSelectorPill(
                        label = "Owner",
                        emoji = "👑",
                        isSelected = selectedRole == UserRole.OWNER,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedRole = UserRole.OWNER }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role Permissions Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (selectedRole) {
                        UserRole.CUSTOMER -> ChanOrangeContainer
                        UserRole.EMPLOYEE -> Color(0xFFE3F2FD)
                        UserRole.OWNER -> Color(0xFFF0EAE1)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (selectedRole) {
                                UserRole.CUSTOMER -> Icons.Default.Person
                                UserRole.EMPLOYEE -> Icons.Default.Badge
                                UserRole.OWNER -> Icons.Default.VerifiedUser
                            },
                            contentDescription = null,
                            tint = Color(selectedRole.badgeColorHex),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hak Akses: ${selectedRole.displayName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(selectedRole.badgeColorHex)
                            )
                            Text(
                                text = when (selectedRole) {
                                    UserRole.CUSTOMER -> "Beli makanan, lacak pesanan, tukar koin ChanPoints."
                                    UserRole.EMPLOYEE -> "Kelola stok, ubah menu, dan proses pesanan dapur."
                                    UserRole.OWNER -> "Akses penuh: CRUD semua fitur, laporan omset, stok, dan data pelanggan."
                                },
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Google Sign-In with Credential Manager Button
                OutlinedButton(
                    onClick = {
                        isGoogleLoading = true
                        authError = null
                        val clientId = try {
                            context.getString(R.string.default_web_client_id)
                        } catch (e: Exception) {
                            null
                        }

                        if (clientId.isNullOrEmpty()) {
                            authError = "Google Web Client ID belum terkonfigurasi"
                            isGoogleLoading = false
                            return@OutlinedButton
                        }

                        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                        coroutineScope.launch {
                            try {
                                val result = credentialManager.getCredential(context as Activity, request)
                                val credential = result.credential
                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                    val authResult = FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                                    val fbUser = authResult.user
                                    val displayName = fbUser?.displayName ?: "Pengguna Google"
                                    val email = fbUser?.email ?: "user@gmail.com"
                                    val photo = fbUser?.photoUrl?.toString() ?: ""
                                    onGoogleSignInSuccess(displayName, email, photo)
                                } else {
                                    authError = "Kredensial tidak dikenali"
                                }
                            } catch (e: GetCredentialCancellationException) {
                                Log.w("Auth", "Google sign in cancelled: ${e.message}")
                            } catch (e: Exception) {
                                Log.e("Auth", "Google sign in failed: ${e.message}", e)
                                authError = "Gagal login Google: ${e.localizedMessage ?: "Coba lagi"}"
                            } finally {
                                isGoogleLoading = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("google_signin_button"),
                    enabled = !isGoogleLoading
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌐", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Masuk dengan Akun Google",
                                color = Color(0xFF3C4043),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (authError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = authError!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fast 1-Click Demo Login
                Text(
                    text = "Akses Cepat (Demo Akun)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { onLogin(selectedRole, "", "") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(selectedRole.badgeColorHex)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demo_login_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = ChanGoldPoints,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedRole) {
                            UserRole.CUSTOMER -> "Login Cepat: Dimas (Pelanggan)"
                            UserRole.EMPLOYEE -> "Login Cepat: Siti (Karyawan / Barista)"
                            UserRole.OWNER -> "Login Cepat: Pak Hendra (Owner)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ChanBorder)
                    Text(
                        text = " atau login manual ",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ChanBorder)
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Form Fields
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email atau Username", fontSize = 12.sp) },
                    placeholder = {
                        Text(
                            when (selectedRole) {
                                UserRole.CUSTOMER -> "customer@chanchan.com"
                                UserRole.EMPLOYEE -> "staff@chanchan.com"
                                UserRole.OWNER -> "owner@chanchan.com"
                            },
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Kata Sandi", fontSize = 12.sp) },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val email = emailInput.ifEmpty { "user@chanchan.com" }
                        onLogin(selectedRole, email, "")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("submit_login_btn")
                ) {
                    Text(
                        text = "Masuk sebagai ${selectedRole.displayName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (currentUser != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Keluar dari Akun (${currentUser.name})",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onDismiss) {
                    Text(text = "Tutup", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RoleSelectorPill(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ChanCoffeeBrown else TextSecondary
            )
        }
    }
}
