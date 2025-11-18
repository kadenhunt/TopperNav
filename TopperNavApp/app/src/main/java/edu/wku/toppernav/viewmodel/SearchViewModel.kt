package edu.wku.toppernav.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import edu.wku.toppernav.domain.usecase.SearchRoomsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchViewModel(private val searchRooms: SearchRoomsUseCase) : ViewModel() {
    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results: StateFlow<List<String>> = _results.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun search(query: String) {
        _loading.value = true
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { searchRooms(query) }
            _results.value = list
            _loading.value = false
        }
    }
}
