package com.onandor.notemanager.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Stack
import javax.inject.Singleton

@Singleton
class NavigationManager : INavigationManager {

    private val _navActions: MutableStateFlow<NavAction?> by lazy {
        MutableStateFlow(null)
    }

    private val backStack: Stack<NavAction> = Stack()

    override val navActions =_navActions.asStateFlow()

    override fun navigateTo(navAction: NavAction?, popCurrent: Boolean) {
        if (_navActions.value != null && !popCurrent)
            backStack.push(_navActions.value)
        _navActions.update { navAction }
    }

    override fun navigateBack() {
        _navActions.update { backStack.pop() }
    }

    override fun setInitialBackStackAction(navAction: NavAction) {
        if (backStack.empty())
            backStack.push(navAction)
    }
}