package com.example.socialcircle.screens.profileSetup

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.socialcircle.navigation.AppScreens
import com.example.socialcircle.viewModels.ProfileSetupViewModel
import com.google.firebase.Timestamp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateOfBirthScreen(
    viewModel: ProfileSetupViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val user = viewModel.user
    var selectedDob by remember { mutableStateOf(user.value.birthDate) }
    var isLoading by remember { mutableStateOf(false) }

    // Format DOB for display
    val displayDate = selectedDob?.toDate()?.let {
        android.text.format.DateFormat.format("dd MMM yyyy", it).toString()
    } ?: "Select your Date of Birth"

    val calendar = Calendar.getInstance()

    // Setup initial date (default 18 years old)
    val initYear = calendar.get(Calendar.YEAR) - 18
    val initMonth = calendar.get(Calendar.MONTH)
    val initDay = calendar.get(Calendar.DAY_OF_MONTH)

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val chosenCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDob = Timestamp(chosenCal.time)
        },
        initYear,
        initMonth,
        initDay
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Enter Date of Birth") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "What's your date of birth?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Use your own date of birth even if this account is for a pet or something else. No one can see it unless you choose to share it",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(24.dp))


            OutlinedButton(
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(64.dp),
                onClick = { datePicker.show() }
            ) {
                Text(displayDate)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {navController.popBackStack()},
                    enabled = !isLoading
                ) {
                    Text("Back")
                }

                Button(
                    onClick = {
                        selectedDob?.let {
                            isLoading = true
                            viewModel.updateBirthDate(selectedDob?: Timestamp.now())
                            navController.navigate(AppScreens.GetPhoneNo.route)
                        }
                    },
                    enabled = selectedDob != null && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("Next")
                    }
                }
            }
        }
    }
}

