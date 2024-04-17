package net.nymtech.nymvpn.ui.common.textbox

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import net.nymtech.nymvpn.R
import net.nymtech.nymvpn.ui.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(value: String, onValueChange: (value : String) -> Unit, modifier: Modifier, singleLine: Boolean = false, supportingText: @Composable (() -> Unit)? = null, leading: @Composable (() -> Unit)? = null, isError: Boolean = false) {
	val interactionSource = remember { MutableInteractionSource() }
	BasicTextField(
		value = value,
		textStyle = MaterialTheme.typography.bodyMedium.copy(
			color = MaterialTheme.colorScheme.onSurface,
		),
		onValueChange = {
			onValueChange(it)
		},
		cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
		modifier = modifier,
		interactionSource = interactionSource,
		enabled = true,
		singleLine = singleLine,
	) {
		OutlinedTextFieldDefaults.DecorationBox(
			value = value,
			innerTextField = it,
			leadingIcon = leading,
			singleLine = singleLine,
			supportingText = supportingText,
			colors = TextFieldDefaults.colors().copy(
				focusedIndicatorColor = CustomColors.outlineVariant,
				disabledIndicatorColor = CustomColors.outlineVariant,
				unfocusedIndicatorColor = CustomColors.outlineVariant,
				focusedLabelColor = MaterialTheme.colorScheme.onSurface,
				focusedContainerColor = MaterialTheme.colorScheme.background,
				unfocusedContainerColor = MaterialTheme.colorScheme.background,
				focusedTextColor = MaterialTheme.colorScheme.onSurface,
				cursorColor = MaterialTheme.colorScheme.onSurface,
			),
			enabled = true,
			label = { Text(text = stringResource(id = R.string.credential_label)) },
			visualTransformation = VisualTransformation.None,
			interactionSource = interactionSource,
			container = {
				OutlinedTextFieldDefaults.ContainerBox(
					true,
					isError = isError,
					interactionSource,
					colors = TextFieldDefaults.colors().copy(
						focusedIndicatorColor = CustomColors.outlineVariant,
						disabledIndicatorColor = CustomColors.outlineVariant,
						unfocusedIndicatorColor = CustomColors.outlineVariant,
						focusedLabelColor = MaterialTheme.colorScheme.onSurface,
						focusedContainerColor = MaterialTheme.colorScheme.background,
						unfocusedContainerColor = MaterialTheme.colorScheme.background,
						focusedTextColor = MaterialTheme.colorScheme.onSurface,
						cursorColor = MaterialTheme.colorScheme.onSurface,
					),
					shape = RoundedCornerShape(4.dp),
					focusedBorderThickness = 0.5.dp,
					unfocusedBorderThickness = 0.5.dp
				)
			}
		)
	}
}
