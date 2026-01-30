# Quick Task 001: Fix Keyboard Hiding Subtask Input

## Problem

When adding more than two subtasks in AddTaskScreen, the input field travels behind the keyboard and users cannot scroll to bring it back into view.

## Root Cause

The AddTaskScreen uses a scrollable Column but doesn't handle IME (keyboard) window insets. When the keyboard appears, the content doesn't resize/pad to accommodate it.

## Solution

1. **Enable edge-to-edge display** in MainActivity using `enableEdgeToEdge()` - this allows the app to properly respond to window insets including the keyboard.

2. **Add `imePadding()` modifier** to the scrollable Column in AddTaskScreen - this automatically adds bottom padding equal to the keyboard height when visible.

## Tasks

- [x] Task 1: Add `enableEdgeToEdge()` call in MainActivity.onCreate()
- [x] Task 2: Add `imePadding()` import and modifier to AddTaskScreen Column

## Files Changed

1. `app/src/main/java/com/routinetool/MainActivity.kt`
2. `app/src/main/java/com/routinetool/ui/screens/addtask/AddTaskScreen.kt`
