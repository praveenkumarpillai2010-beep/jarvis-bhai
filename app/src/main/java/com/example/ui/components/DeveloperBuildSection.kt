package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.ui.theme.JarvisAqua
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepNavy
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Composable
fun DeveloperBuildSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var showInstructions by remember { mutableStateOf(false) }
    var githubRepoUrl by remember { mutableStateOf("https://github.com/shaikhayman411/jarvis-ai/releases") }

    var verifiedApkFile by remember { mutableStateOf<File?>(null) }
    var apkFileSizeMb by remember { mutableStateOf<String?>(null) }
    var verificationStatus by remember { mutableStateOf<String>("Not verified yet. Tap 'VERIFY & PREPARE APK' to scan filesystem.") }
    var isVerifying by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }

    fun checkAndPrepareApk() {
        isVerifying = true
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    // Check candidates:
                    // 1. Export cache directory
                    val cacheApk = File(context.cacheDir, "JARVIS-AI-v1.0.0.apk")
                    // 2. External files dir
                    val extApk = File(context.getExternalFilesDir(null), "app-debug.apk")
                    // 3. Installed base APK (real APK running on device)
                    val baseApkPath = context.applicationInfo.sourceDir
                    val baseApkFile = if (!baseApkPath.isNullOrBlank()) File(baseApkPath) else null

                    if (cacheApk.exists() && cacheApk.length() > 0) {
                        cacheApk
                    } else if (extApk.exists() && extApk.length() > 0) {
                        extApk
                    } else if (baseApkFile != null && baseApkFile.exists() && baseApkFile.length() > 0) {
                        // Prepare by copying the running APK to cache with standardized release asset name
                        try {
                            FileInputStream(baseApkFile).use { input ->
                                FileOutputStream(cacheApk).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            if (cacheApk.exists() && cacheApk.length() > 0) cacheApk else baseApkFile
                        } catch (e: Exception) {
                            baseApkFile
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            isVerifying = false
            if (result != null && result.exists() && result.length() > 0) {
                verifiedApkFile = result
                val mb = String.format("%.2f", result.length().toDouble() / (1024.0 * 1024.0))
                apkFileSizeMb = mb
                isVerified = true
                verificationStatus = "Verified APK accessible at: ${result.name} ($mb MB). Ready for export or share."
            } else {
                verifiedApkFile = null
                isVerified = false
                verificationStatus = "Error: APK file not found at accessible path. Run 'gradle :app:assembleDebug' in AI Studio."
            }
        }
    }

    // Auto-check on first launch
    LaunchedEffect(Unit) {
        checkAndPrepareApk()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisDeepNavy, RoundedCornerShape(12.dp))
            .border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("developer_build_section")
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Developer Build",
                        tint = JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DEVELOPER BUILD & GITHUB RELEASES",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "APK Packaging, Local Verification & GitHub v1.0.0",
                            color = JarvisTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (BuildConfig.DEBUG) JarvisElectricBlue.copy(alpha = 0.2f) else JarvisSuccess.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (BuildConfig.DEBUG) "DEBUG BUILD" else "RELEASE",
                        color = if (BuildConfig.DEBUG) JarvisElectricBlue else JarvisSuccess,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Output Path & Release Metadata Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "EXPECTED BUILD OUTPUT PATH",
                        color = JarvisTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "app/build/outputs/apk/debug/app-debug.apk",
                            color = JarvisAqua,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("app/build/outputs/apk/debug/app-debug.apk"))
                                Toast.makeText(context, "Output path copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Path",
                                tint = JarvisCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = JarvisBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("RELEASE TAG", color = JarvisTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("v1.0.0", color = JarvisTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("RELEASE TITLE", color = JarvisTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("JARVIS AI v1.0.0", color = JarvisTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("ASSET FILENAME", color = JarvisTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("JARVIS-AI-v1.0.0.apk", color = JarvisSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Real APK Verification Status & Download
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisSurface, RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isVerified) JarvisSuccess.copy(alpha = 0.5f) else JarvisWarning.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = "Status",
                                tint = if (isVerified) JarvisSuccess else JarvisWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVerified) "APK VERIFIED & ACCESSIBLE" else "VERIFICATION PENDING",
                                color = if (isVerified) JarvisSuccess else JarvisWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        OutlinedButton(
                            onClick = { checkAndPrepareApk() },
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("verify_apk_button"),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = JarvisCyan,
                                    strokeWidth = 1.5.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Rescan", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RESCAN", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = verificationStatus,
                        color = JarvisTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real Download / Share Button - strictly active only when APK file exists!
                    Button(
                        onClick = {
                            val apkFile = verifiedApkFile
                            if (apkFile != null && apkFile.exists()) {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        apkFile
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/vnd.android.package-archive"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "JARVIS AI v1.0.0 APK")
                                        putExtra(Intent.EXTRA_TEXT, "JARVIS AI Android debug package (v1.0.0)")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Download / Share JARVIS APK"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = isVerified && verifiedApkFile != null && verifiedApkFile?.exists() == true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_apk_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = Color(0xFF001E2E),
                            disabledContainerColor = JarvisSurface,
                            disabledContentColor = JarvisTextMuted
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.Share else Icons.Default.CloudDownload,
                            contentDescription = "Export APK",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isVerified) "DOWNLOAD / SHARE APK (${apkFileSizeMb ?: ""} MB)" else "APK NOT ACCESSIBLE (RUN GRADLE BUILD)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (!isVerified) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Download disabled until an APK file is generated and detected on storage.",
                            color = JarvisError,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. GitHub Releases Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "GitHub",
                            tint = JarvisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GITHUB RELEASES PAGE",
                            color = JarvisTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = githubRepoUrl,
                        onValueChange = { githubRepoUrl = it },
                        label = { Text("GitHub Releases URL", fontSize = 10.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_releases_url_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = JarvisTextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedLabelColor = JarvisCyan,
                            unfocusedLabelColor = JarvisTextMuted,
                            cursorColor = JarvisCyan
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubRepoUrl.trim()))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_github_releases_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisElectricBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OPEN GITHUB RELEASES PAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Instructions Accordion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInstructions = !showInstructions }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Build Instructions",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BUILD & RELEASE INSTRUCTIONS",
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Icon(
                    imageVector = if (showInstructions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (showInstructions) "Collapse" else "Expand",
                    tint = JarvisCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = showInstructions) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(JarvisSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "1. LOCAL GRADLE BUILD:",
                        color = JarvisCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Run: gradle :app:assembleDebug\nOutput: app/build/outputs/apk/debug/app-debug.apk",
                        color = JarvisTextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "2. GITHUB ACTIONS AUTOMATION:",
                        color = JarvisCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Workflow: .github/workflows/android-build-release.yml\nPush tag 'v1.0.0' to build artifact & draft release:\n  git tag v1.0.0 && git push origin v1.0.0",
                        color = JarvisTextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "3. SECURITY & SIGNING NOTICE:",
                        color = JarvisWarning,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Debug APKs are signed with the default debug key for testing/sideloading. For Google Play production distribution, sign with a secure release keystore.",
                        color = JarvisTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
