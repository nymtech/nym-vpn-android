package net.nymtech.nymvpn.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.text.buildAnnotatedString
import net.nymtech.nymvpn.R
import net.nymtech.vpn.model.Hop

object StringUtils {
    fun buildCountryNameString(country : Hop.Country, context : Context) : String {
        return buildAnnotatedString {
            if(country.isFastest) {
                append(context.getString(R.string.fastest))
                append(" (")
                append(country.name)
                append(")")}
            else append(country.name)
        }.text
    }
    @SuppressLint("DiscouragedApi")
    fun getFlagImageVectorByName(context: Context, name: String): Int {
        val flagAssetName = "flag_%S".format(name).lowercase()
        return context.resources.getIdentifier(flagAssetName, "drawable", context.packageName)
    }
}