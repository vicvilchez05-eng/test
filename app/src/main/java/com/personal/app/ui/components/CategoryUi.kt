package com.personal.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.personal.app.R
import com.personal.app.data.model.AccountType
import com.personal.app.data.model.Category

/** Icon + label per category; the data layer stays free of UI. */
val Category.icon: ImageVector
    get() = when (this) {
        Category.HOUSING -> Icons.Outlined.Home
        Category.FOOD -> Icons.Outlined.Restaurant
        Category.TRANSPORT -> Icons.Outlined.DirectionsCar
        Category.LEISURE -> Icons.Outlined.SportsEsports
        Category.SHOPPING -> Icons.Outlined.ShoppingBag
        Category.HEALTH -> Icons.Outlined.LocalHospital
        Category.BILLS -> Icons.Outlined.Receipt
        Category.SUBSCRIPTIONS -> Icons.Outlined.Subscriptions
        Category.OTHER -> Icons.Outlined.MoreHoriz
        Category.SALARY -> Icons.Outlined.Work
        Category.TRANSFER -> Icons.Outlined.SwapHoriz
        Category.OTHER_INCOME -> Icons.Outlined.TrendingUp
    }

val Category.labelRes: Int
    @StringRes get() = when (this) {
        Category.HOUSING -> R.string.cat_housing
        Category.FOOD -> R.string.cat_food
        Category.TRANSPORT -> R.string.cat_transport
        Category.LEISURE -> R.string.cat_leisure
        Category.SHOPPING -> R.string.cat_shopping
        Category.HEALTH -> R.string.cat_health
        Category.BILLS -> R.string.cat_bills
        Category.SUBSCRIPTIONS -> R.string.cat_subscriptions
        Category.OTHER -> R.string.cat_other
        Category.SALARY -> R.string.cat_salary
        Category.TRANSFER -> R.string.cat_transfer
        Category.OTHER_INCOME -> R.string.cat_other_income
    }

val AccountType.icon: ImageVector
    get() = when (this) {
        AccountType.CHECKING -> Icons.Outlined.AccountBalance
        AccountType.SAVINGS -> Icons.Outlined.Savings
        AccountType.CREDIT -> Icons.Outlined.CreditCard
        AccountType.CASH -> Icons.Outlined.Payments
        AccountType.INVESTMENT -> Icons.Outlined.CurrencyBitcoin
    }

val AccountType.labelRes: Int
    @StringRes get() = when (this) {
        AccountType.CHECKING -> R.string.acc_checking
        AccountType.SAVINGS -> R.string.acc_savings
        AccountType.CREDIT -> R.string.acc_credit
        AccountType.CASH -> R.string.acc_cash
        AccountType.INVESTMENT -> R.string.acc_investment
    }
