package org.example.balogserver.global.util

import java.text.NumberFormat
import java.util.Locale

object MoneyFormatter { private val format = NumberFormat.getNumberInstance(Locale.KOREA); @JvmStatic fun format(amount: Long?) = amount?.let(format::format) ?: "0" }
