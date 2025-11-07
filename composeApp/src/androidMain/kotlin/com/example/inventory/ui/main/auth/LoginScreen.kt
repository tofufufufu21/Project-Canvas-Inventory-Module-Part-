package com.example.inventory.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.R
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.shadow

@Composable
fun LoginScreen(
    viewModel: DemoLoginViewModel,
    onLoginSuccess: (accessToken: String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    // when login completes, notify parent
    LaunchedEffect(viewModel.isLoggedIn, viewModel.accessToken) {
        if (viewModel.isLoggedIn && viewModel.accessToken != null) {
            onLoginSuccess(viewModel.accessToken!!)
        }
    }

    // colors tuned to match the sample
    val leftBlue = Color(0xFF223859)
    val accentRed = Color(0xFFE04E4C)
    val pageBg = Color(0xFFF2F6FB)
    val cardBg = Color(0xFFF6F9FC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.84f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(6.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left panel
            Column(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .background(leftBlue)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Beanity Cafe",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )

                // cashier image (vector drawable)
                Image(
                    painter = painterResource(id = R.drawable.ic_cashier),
                    contentDescription = "Cashier Icon",
                    modifier = Modifier.size(130.dp),
                    contentScale = ContentScale.Fit
                )

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // RIGHT panel: center the card both vertically and horizontally
            Box(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight()
                    .background(cardBg)
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "POINT OF SALE",
                            color = accentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sign in",
                            color = leftBlue,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = viewModel.email,
                            onValueChange = { viewModel.email = it },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.password,
                            onValueChange = { viewModel.password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!viewModel.errorMessage.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = viewModel.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // call suspend signIn inside coroutine scope
                                scope.launch { viewModel.signIn() }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(6.dp),
                            enabled = !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Log In", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
