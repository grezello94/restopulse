package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import com.redlantern.restopulse.workers.WorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorkSchedulerViewModel @Inject constructor(
    val scheduler: WorkScheduler
) : ViewModel()
