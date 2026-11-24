package com.example.hostelmanager.data

import com.example.hostelmanager.util.toIsoMonth
import java.time.YearMonth

/** Formats a YearMonth to a SQL LIKE pattern, e.g., "2026-03%". */
fun YearMonth.monthLikePattern(): String = "${toIsoMonth()}%"
