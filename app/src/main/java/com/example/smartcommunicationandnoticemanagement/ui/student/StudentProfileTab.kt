package com.example.smartcommunicationandnoticemanagement.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel

/**
 * A simple placeholder tab for the student profile screen.
 * It receives the same callbacks that the dashboard expects.
 */
@Composable
fun StudentProfileTab(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenFaq: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Student Profile", modifier = Modifier.padding(bottom = 8.dp))
        Button(onClick = onEditProfile, modifier = Modifier.padding(bottom = 8.dp)) {
            Text(text = "Edit Profile")
        }
        Button(onClick = onOpenFaq, modifier = Modifier.padding(bottom = 8.dp)) {
            Text(text = "FAQ")
        }
        Button(onClick = {
            // Trigger logout via view model and then callback
            authViewModel.logout()
            onLogout()
        }) {
            Text(text = "Logout")
        }
    }
}
