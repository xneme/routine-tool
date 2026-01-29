package com.routinetool.domain.model

data class FilterState(
    val showActive: Boolean = true,
    val showOverdue: Boolean = true,
    val showDone: Boolean = true,
    val showSoftDeadline: Boolean = true,
    val showHardDeadline: Boolean = true,
    val showNoDeadline: Boolean = true
) {
    val isDefault: Boolean
        get() = showActive && showOverdue && showDone && showSoftDeadline && showHardDeadline && showNoDeadline
}
