package com.shsshobuj.shstube.config

import com.shsshobuj.shstube.BuildConfig

/**
 * SecretConfig - Centralized configuration fetched from BuildConfig (injected at build time from local.properties).
 * No secrets are hardcoded. All values come from build-time injection.
 */
object SecretConfig {
    val DEV_NAME: String get() = BuildConfig.DEV_NAME
    val BKASH_PAYMENT_NUMBER: String get() = BuildConfig.BKASH_PAYMENT_NUMBER
    val TELEGRAM_CHANNEL_URL: String get() = BuildConfig.TELEGRAM_CHANNEL_URL
    val FACEBOOK_PROFILE_URL: String get() = BuildConfig.FACEBOOK_PROFILE_URL
    val SUPPORT_EMAIL: String get() = BuildConfig.SUPPORT_EMAIL

    const val APP_NAME = "SHS Tube"
    const val APP_VERSION = "1.0.0"
    const val PACKAGE_NAME = "com.shsshobuj.shstube"
}
