package l192.aakarsh.pocketops.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import l192.aakarsh.pocketops.R
import l192.aakarsh.pocketops.ui.WidgetQrActivity

class QuickUpiWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier.fillMaxSize()
                    .clickable(actionStartActivity<WidgetQrActivity>())
                    .background(ColorProvider(Color.Transparent))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_qr_code),
                    contentDescription = "Show QR",
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
        }
    }
}


