package edu.wku.toppernav.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onEnter: (String) -> Unit
) {
    // Placeholder: results will come from a ViewModel (repository + use case) later.
    // Stand‑in static data removed for academic handoff.
    val results = emptyList<String>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Find a room", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Building and room (e.g., CODE 202)") },
            placeholder = { Text("Type building or room number") },
        )

        Button(
            onClick = { onEnter(query) },
            enabled = query.isNotBlank()
        ) {
            Text("Enter")
        }

        if (results.isEmpty()) {
            Text(
                text = "No search results yet. Data layer integration pending.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEnter(item) }
                    ) {
                        ListItem(
                            headlineContent = { Text(item) },
                            supportingContent = { Text("Tap to navigate") }
                        )
                    }
                }
            }
        }
    }
}
