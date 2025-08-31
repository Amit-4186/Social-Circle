import android.util.Log
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.socialcircle.navigation.AppScreens
import com.example.socialcircle.viewModels.ProfileSetupViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameScreen(
    viewModel: ProfileSetupViewModel,
    navController: NavController
) {
    val user = viewModel.user
    var username by remember { mutableStateOf(user.value.userName) }
    var isChecking by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploaded by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()


    LaunchedEffect(isUploaded) {
        if(isUploaded == true){
            navController.navigate(AppScreens.RootNav.route)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Choose Username") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                text = "Create a userName",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add a unike username for the account.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    isAvailable = null // reset when typing
                },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isAvailable == false
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isChecking -> {
                    Text("Checking availability...", color = Color.Gray)
                }
                isAvailable == true -> {
                    Text("Username is available ✅", color = Color.Green)
                }
                isAvailable == false -> {
                    Text("Username already taken ❌", color = Color.Red)
                }
            }

            if(isUploaded == false){
                Text("some error occurs, your profile not created", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }, enabled = !isLoading) {
                    Text("Back")
                }
                Button(
                    onClick = {
                        isChecking = true
                        isLoading = true;
                        viewModel.checkIfUsernameExists(username) {
                            isAvailable = !it
                            viewModel.updateUsername(username)
                            isChecking = false

                            if (isAvailable == true) {
                                scope.launch {
                                    isUploaded = viewModel.uploadOnFireStore()
                                }
                            }
                        }
                    },
                    enabled = username.isNotBlank() && !isChecking && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    else {
                        Text("Next")
                    }
                }
            }
        }
    }
}
