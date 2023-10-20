package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import com.onandor.notemanager.navigation.INavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val navigationManager: INavigationManager
) : ViewModel() {}