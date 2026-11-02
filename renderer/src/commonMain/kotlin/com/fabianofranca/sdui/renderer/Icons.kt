package com.fabianofranca.sdui.renderer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Resolve o nome de um ícone do JSON (nomes dos Material Symbols, em snake_case) para um
 * [ImageVector]. O padrão cobre os ícones do `material-icons-core`; o app pode encadear o seu:
 * `SduiIcons { name -> meusIcones[name] } orElse SduiIcons.Default`.
 */
fun interface SduiIcons {
    fun resolve(name: String): ImageVector?

    infix fun orElse(other: SduiIcons): SduiIcons = SduiIcons { name -> resolve(name) ?: other.resolve(name) }

    companion object {
        val Default: SduiIcons = SduiIcons { name -> DefaultIcons[name] }
    }
}

private val DefaultIcons: Map<String, ImageVector> by lazy {
    mapOf(
        "account_box" to Icons.Filled.AccountBox,
        "account_circle" to Icons.Filled.AccountCircle,
        "add" to Icons.Filled.Add,
        "add_circle" to Icons.Filled.AddCircle,
        "arrow_back" to Icons.AutoMirrored.Filled.ArrowBack,
        "arrow_drop_down" to Icons.Filled.ArrowDropDown,
        "arrow_forward" to Icons.AutoMirrored.Filled.ArrowForward,
        "build" to Icons.Filled.Build,
        "call" to Icons.Filled.Call,
        "check" to Icons.Filled.Check,
        "check_circle" to Icons.Filled.CheckCircle,
        "clear" to Icons.Filled.Clear,
        "close" to Icons.Filled.Close,
        "create" to Icons.Filled.Create,
        "date_range" to Icons.Filled.DateRange,
        "delete" to Icons.Filled.Delete,
        "done" to Icons.Filled.Done,
        "edit" to Icons.Filled.Edit,
        "email" to Icons.Filled.Email,
        "mail" to Icons.Filled.Email,
        "exit_to_app" to Icons.AutoMirrored.Filled.ExitToApp,
        "logout" to Icons.AutoMirrored.Filled.ExitToApp,
        "face" to Icons.Filled.Face,
        "favorite" to Icons.Filled.Favorite,
        "favorite_border" to Icons.Filled.FavoriteBorder,
        "home" to Icons.Filled.Home,
        "info" to Icons.Filled.Info,
        "keyboard_arrow_down" to Icons.Filled.KeyboardArrowDown,
        "keyboard_arrow_left" to Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        "keyboard_arrow_right" to Icons.AutoMirrored.Filled.KeyboardArrowRight,
        "chevron_right" to Icons.AutoMirrored.Filled.KeyboardArrowRight,
        "keyboard_arrow_up" to Icons.Filled.KeyboardArrowUp,
        "list" to Icons.AutoMirrored.Filled.List,
        "location_on" to Icons.Filled.LocationOn,
        "lock" to Icons.Filled.Lock,
        "mail_outline" to Icons.Filled.MailOutline,
        "menu" to Icons.Filled.Menu,
        "more_vert" to Icons.Filled.MoreVert,
        "notifications" to Icons.Filled.Notifications,
        "person" to Icons.Filled.Person,
        "phone" to Icons.Filled.Phone,
        "place" to Icons.Filled.Place,
        "play_arrow" to Icons.Filled.PlayArrow,
        "refresh" to Icons.Filled.Refresh,
        "search" to Icons.Filled.Search,
        "send" to Icons.AutoMirrored.Filled.Send,
        "settings" to Icons.Filled.Settings,
        "share" to Icons.Filled.Share,
        "shopping_cart" to Icons.Filled.ShoppingCart,
        "star" to Icons.Filled.Star,
        "thumb_up" to Icons.Filled.ThumbUp,
        "warning" to Icons.Filled.Warning,
    )
}
