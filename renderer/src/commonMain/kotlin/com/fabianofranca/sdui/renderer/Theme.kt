package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fabianofranca.sdui.contract.BoxAlignment
import com.fabianofranca.sdui.contract.ColorRole
import com.fabianofranca.sdui.contract.HorizontalAlignment
import com.fabianofranca.sdui.contract.HorizontalArrangement
import com.fabianofranca.sdui.contract.ImageScale
import com.fabianofranca.sdui.contract.ImeActionKind
import com.fabianofranca.sdui.contract.KeyboardKind
import com.fabianofranca.sdui.contract.Padding
import com.fabianofranca.sdui.contract.SnackbarLength
import com.fabianofranca.sdui.contract.TextAlignment
import com.fabianofranca.sdui.contract.TypographyRole
import com.fabianofranca.sdui.contract.VerticalAlignment

// O JSON usa os papéis do Material 3; as cores, a tipografia e as formas vêm do MaterialTheme do app.

@Composable
@ReadOnlyComposable
internal fun ColorRole.toColor(): Color {
    val c = MaterialTheme.colorScheme
    return when (this) {
        ColorRole.Primary -> c.primary
        ColorRole.OnPrimary -> c.onPrimary
        ColorRole.PrimaryContainer -> c.primaryContainer
        ColorRole.OnPrimaryContainer -> c.onPrimaryContainer
        ColorRole.Secondary -> c.secondary
        ColorRole.OnSecondary -> c.onSecondary
        ColorRole.SecondaryContainer -> c.secondaryContainer
        ColorRole.OnSecondaryContainer -> c.onSecondaryContainer
        ColorRole.Tertiary -> c.tertiary
        ColorRole.OnTertiary -> c.onTertiary
        ColorRole.TertiaryContainer -> c.tertiaryContainer
        ColorRole.OnTertiaryContainer -> c.onTertiaryContainer
        ColorRole.Error -> c.error
        ColorRole.OnError -> c.onError
        ColorRole.ErrorContainer -> c.errorContainer
        ColorRole.OnErrorContainer -> c.onErrorContainer
        ColorRole.Background -> c.background
        ColorRole.OnBackground -> c.onBackground
        ColorRole.Surface -> c.surface
        ColorRole.OnSurface -> c.onSurface
        ColorRole.SurfaceVariant -> c.surfaceVariant
        ColorRole.OnSurfaceVariant -> c.onSurfaceVariant
        ColorRole.SurfaceContainerLowest -> c.surfaceContainerLowest
        ColorRole.SurfaceContainerLow -> c.surfaceContainerLow
        ColorRole.SurfaceContainer -> c.surfaceContainer
        ColorRole.SurfaceContainerHigh -> c.surfaceContainerHigh
        ColorRole.SurfaceContainerHighest -> c.surfaceContainerHighest
        ColorRole.InverseSurface -> c.inverseSurface
        ColorRole.InverseOnSurface -> c.inverseOnSurface
        ColorRole.InversePrimary -> c.inversePrimary
        ColorRole.Outline -> c.outline
        ColorRole.OutlineVariant -> c.outlineVariant
    }
}

@Composable
@ReadOnlyComposable
internal fun TypographyRole.toTextStyle(): TextStyle {
    val t = MaterialTheme.typography
    return when (this) {
        TypographyRole.DisplayLarge -> t.displayLarge
        TypographyRole.DisplayMedium -> t.displayMedium
        TypographyRole.DisplaySmall -> t.displaySmall
        TypographyRole.HeadlineLarge -> t.headlineLarge
        TypographyRole.HeadlineMedium -> t.headlineMedium
        TypographyRole.HeadlineSmall -> t.headlineSmall
        TypographyRole.TitleLarge -> t.titleLarge
        TypographyRole.TitleMedium -> t.titleMedium
        TypographyRole.TitleSmall -> t.titleSmall
        TypographyRole.BodyLarge -> t.bodyLarge
        TypographyRole.BodyMedium -> t.bodyMedium
        TypographyRole.BodySmall -> t.bodySmall
        TypographyRole.LabelLarge -> t.labelLarge
        TypographyRole.LabelMedium -> t.labelMedium
        TypographyRole.LabelSmall -> t.labelSmall
    }
}

internal fun HorizontalAlignment.toCompose(): Alignment.Horizontal = when (this) {
    HorizontalAlignment.Start -> Alignment.Start
    HorizontalAlignment.Center -> Alignment.CenterHorizontally
    HorizontalAlignment.End -> Alignment.End
}

internal fun VerticalAlignment.toCompose(): Alignment.Vertical = when (this) {
    VerticalAlignment.Top -> Alignment.Top
    VerticalAlignment.Center -> Alignment.CenterVertically
    VerticalAlignment.Bottom -> Alignment.Bottom
}

internal fun HorizontalArrangement.toCompose(spacing: Int): Arrangement.Horizontal = when (this) {
    HorizontalArrangement.Start -> Arrangement.spacedBy(spacing.dp, Alignment.Start)
    HorizontalArrangement.Center -> Arrangement.spacedBy(spacing.dp, Alignment.CenterHorizontally)
    HorizontalArrangement.End -> Arrangement.spacedBy(spacing.dp, Alignment.End)
    HorizontalArrangement.SpaceBetween -> Arrangement.SpaceBetween
    HorizontalArrangement.SpaceAround -> Arrangement.SpaceAround
    HorizontalArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
}

internal fun BoxAlignment.toCompose(): Alignment = when (this) {
    BoxAlignment.TopStart -> Alignment.TopStart
    BoxAlignment.TopCenter -> Alignment.TopCenter
    BoxAlignment.TopEnd -> Alignment.TopEnd
    BoxAlignment.CenterStart -> Alignment.CenterStart
    BoxAlignment.Center -> Alignment.Center
    BoxAlignment.CenterEnd -> Alignment.CenterEnd
    BoxAlignment.BottomStart -> Alignment.BottomStart
    BoxAlignment.BottomCenter -> Alignment.BottomCenter
    BoxAlignment.BottomEnd -> Alignment.BottomEnd
}

internal fun TextAlignment.toCompose(): TextAlign = when (this) {
    TextAlignment.Start -> TextAlign.Start
    TextAlignment.Center -> TextAlign.Center
    TextAlignment.End -> TextAlign.End
    TextAlignment.Justify -> TextAlign.Justify
}

internal fun ImageScale.toCompose(): ContentScale = when (this) {
    ImageScale.Crop -> ContentScale.Crop
    ImageScale.Fit -> ContentScale.Fit
    ImageScale.FillBounds -> ContentScale.FillBounds
    ImageScale.FillWidth -> ContentScale.FillWidth
    ImageScale.FillHeight -> ContentScale.FillHeight
    ImageScale.Inside -> ContentScale.Inside
    ImageScale.None -> ContentScale.None
}

internal fun Padding?.toPaddingValues(): PaddingValues =
    if (this == null) PaddingValues(0.dp) else PaddingValues(start = start.dp, top = top.dp, end = end.dp, bottom = bottom.dp)

internal fun KeyboardKind.toKeyboardType(): KeyboardType = when (this) {
    KeyboardKind.Text -> KeyboardType.Text
    KeyboardKind.Email -> KeyboardType.Email
    KeyboardKind.Number -> KeyboardType.Number
    KeyboardKind.Phone -> KeyboardType.Phone
    KeyboardKind.Password -> KeyboardType.Password
}

internal fun ImeActionKind.toImeAction(): ImeAction = when (this) {
    ImeActionKind.Default -> ImeAction.Default
    ImeActionKind.Done -> ImeAction.Done
    ImeActionKind.Next -> ImeAction.Next
    ImeActionKind.Search -> ImeAction.Search
    ImeActionKind.Send -> ImeAction.Send
    ImeActionKind.Go -> ImeAction.Go
}

internal fun SnackbarLength.toCompose(): SnackbarDuration = when (this) {
    SnackbarLength.Short -> SnackbarDuration.Short
    SnackbarLength.Long -> SnackbarDuration.Long
    SnackbarLength.Indefinite -> SnackbarDuration.Indefinite
}
