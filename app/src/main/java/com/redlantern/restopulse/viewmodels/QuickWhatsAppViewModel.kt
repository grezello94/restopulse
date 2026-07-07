package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.services.WhatsAppChecker
import com.redlantern.restopulse.utils.PhoneNumberNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class QuickWhatsAppUiState(
    val input: String = "",
    val searchedNumber: String = "",
    val personalAvailable: Boolean = false,
    val businessAvailable: Boolean = false,
    val checking: Boolean = false,
    val message: String = "Enter a mobile number to open WhatsApp or Business WhatsApp."
) {
    val hasSearch: Boolean get() = searchedNumber.isNotBlank()
    val canOpenAny: Boolean get() = personalAvailable || businessAvailable
}

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuickWhatsAppViewModel @Inject constructor(
    private val normalizer: PhoneNumberNormalizer,
    private val whatsAppChecker: WhatsAppChecker
) : ViewModel() {
    private val input = MutableStateFlow("")

    val state: StateFlow<QuickWhatsAppUiState> = input
        .debounce(120)
        .distinctUntilChanged()
        .flatMapLatest { raw ->
            flow {
                val normalized = normalizer.normalize(raw)
                if (raw.isBlank()) {
                    emit(QuickWhatsAppUiState(input = raw))
                    return@flow
                }
                if (normalized.length != 10) {
                    emit(
                        QuickWhatsAppUiState(
                            input = raw,
                            message = "Type a valid 10 digit mobile number."
                        )
                    )
                    return@flow
                }

                emit(
                    QuickWhatsAppUiState(
                        input = raw,
                        searchedNumber = normalized,
                        checking = true,
                        message = "Checking WhatsApp apps..."
                    )
                )
                val result = whatsAppChecker.chatAvailability(normalized)
                emit(
                    QuickWhatsAppUiState(
                        input = normalized,
                        searchedNumber = normalized,
                        personalAvailable = result.personal,
                        businessAvailable = result.business,
                        message = if (result.any) {
                            "Ready to open +91 $normalized."
                        } else {
                            "WhatsApp is not available on this phone for +91 $normalized."
                        }
                    )
                )
            }.flowOn(Dispatchers.IO)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuickWhatsAppUiState())

    fun setInput(value: String) {
        input.update { value }
    }

    fun search() {
        input.update { normalizer.normalize(it) }
    }

    fun openPersonal() {
        val number = state.value.searchedNumber
        if (number.isNotBlank()) whatsAppChecker.openChat(number, WhatsAppChecker.WhatsAppApp.PERSONAL)
    }

    fun openBusiness() {
        val number = state.value.searchedNumber
        if (number.isNotBlank()) whatsAppChecker.openChat(number, WhatsAppChecker.WhatsAppApp.BUSINESS)
    }
}
