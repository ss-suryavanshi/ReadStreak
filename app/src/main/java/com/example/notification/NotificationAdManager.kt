package com.example.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

data class SponsoredOffer(
    val id: String,
    val brandName: String,
    val headline: String,
    val notificationActionTitle: String,
    val badgeText: String,
    val dealDescription: String,
    val discountTag: String,
    val actionUrl: String
)

object NotificationAdManager {

    const val PREFS_NAME = "notification_ad_prefs"
    const val KEY_NOTIFICATION_ADS_ENABLED = "notification_ads_enabled"

    val SPONSORED_OFFERS = listOf(
        SponsoredOffer(
            id = "audible_trial",
            brandName = "Audible",
            headline = "Listen to 'Atomic Habits' Free for 30 Days 🎧",
            notificationActionTitle = "🎁 Claim Audible Free Trial",
            badgeText = "SPONSORED DEAL",
            dealDescription = "Get 30 days free access to 100,000+ audiobooks including bestsellers on productivity, fiction, and self-growth.",
            discountTag = "100% FREE TRIAL",
            actionUrl = "https://www.audible.com"
        ),
        SponsoredOffer(
            id = "kindle_unlimited",
            brandName = "Kindle Unlimited",
            headline = "3 Months of Unlimited Reading for $0.99 📚",
            notificationActionTitle = "📚 Claim 3 Months $0.99",
            badgeText = "SPECIAL OFFER",
            dealDescription = "Unlimited reading on any device. Over 4 million digital titles, magazines, and audio companion books.",
            discountTag = "95% OFF",
            actionUrl = "https://www.amazon.com/kindle-dbs/hz/signup"
        ),
        SponsoredOffer(
            id = "blinkist_summaries",
            brandName = "Blinkist",
            headline = "Key Insights from 6,500+ Non-Fiction Books in 15 Mins ⚡",
            notificationActionTitle = "⚡ Get 7-Day Free Pass",
            badgeText = "SPONSORED TIP",
            dealDescription = "Read or listen to key takeaways from top non-fiction bestsellers while maintaining your daily reading streak.",
            discountTag = "FREE PASS",
            actionUrl = "https://www.blinkist.com"
        ),
        SponsoredOffer(
            id = "headspace_reader",
            brandName = "Headspace",
            headline = "Mindful Reading & Deep Focus Soundscapes 🧘",
            notificationActionTitle = "🧘 Start Mindful Session",
            badgeText = "FEATURED PARTNER",
            dealDescription = "Enhance your reading comprehension with ambient background soundscapes and guided deep-focus audio.",
            discountTag = "50% OFF ANNUAL",
            actionUrl = "https://www.headspace.com"
        )
    )

    fun isNotificationAdsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATION_ADS_ENABLED, true)
    }

    fun setNotificationAdsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATION_ADS_ENABLED, enabled).apply()
    }

    fun getRandomOffer(): SponsoredOffer {
        return SPONSORED_OFFERS.random()
    }

    fun getOfferById(id: String): SponsoredOffer {
        return SPONSORED_OFFERS.find { it.id == id } ?: SPONSORED_OFFERS.first()
    }

    fun attachSponsoredAdToNotification(
        context: Context,
        builder: NotificationCompat.Builder,
        offer: SponsoredOffer
    ) {
        if (!isNotificationAdsEnabled(context)) return

        // PendingIntent when user clicks the notification action button
        val adIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SPONSORED_OFFER_ID", offer.id)
            putExtra("SPONSORED_SOURCE", "NOTIFICATION_ACTION")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            offer.id.hashCode(),
            adIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action button inside push notification (Truecaller style!)
        builder.addAction(
            R.mipmap.ic_launcher,
            offer.notificationActionTitle,
            pendingIntent
        )
    }

    fun showTruecallerStyleAdNotification(context: Context) {
        DailyReminderReceiver.createNotificationChannel(context)

        val offer = getRandomOffer()

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SPONSORED_OFFER_ID", offer.id)
        }

        val mainPendingIntent = PendingIntent.getActivity(
            context,
            7771,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val adActionIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SPONSORED_OFFER_ID", offer.id)
            putExtra("SPONSORED_SOURCE", "TRUECALLER_NOTIFICATION_ACTION")
        }

        val adActionPendingIntent = PendingIntent.getActivity(
            context,
            7772,
            adActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, DailyReminderReceiver.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Protect Your Daily Reading Streak! 🔥")
            .setContentText("Complete 15 mins today • [Sponsored] ${offer.brandName}: ${offer.headline}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You haven't logged reading today! Log a quick session now to protect your streak.\n\n✨ [Sponsored Deal] ${offer.brandName}: ${offer.headline} - Tap below to claim!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_agenda,
                offer.notificationActionTitle,
                adActionPendingIntent
            )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(DailyReminderReceiver.NOTIFICATION_ID, builder.build())
    }
}
