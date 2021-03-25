object AndroidSdk {
    const val compileSdk = 29
    const val minSdk = 21
    const val targetSdk = 29

    const val versionCode = 16
    const val versionName = "7.0"
    const val buildToolsVersion = "29.0.3"
    const val DalalAppId = "org.pragyan.dalal18"
}

object Versions {
    const val Gradle = "4.0.1"
    const val Kotlin = "1.3.61"
    const val CoreKtx = "1.3.1"
    const val Coroutine = "1.3.3"
    const val Appcompat = "1.1.0"
    const val ConstraintLayout = "2.0.0-beta4"
    const val Lifecycle = "2.2.0"
    const val Navigation = "1.0.0"
    const val Material = "1.2.0-alpha04"
    const val Security = "1.1.0-alpha02"
    const val Picasso = "2.71828"
    const val Browser = "1.0.0"
    const val Preference = "1.1.0"
    const val Anko = "0.10.5"
    const val JavaxAnnotation = "1.2"

    // dagger dependency versions
    const val Dagger = "2.24"
    const val DaggerCompiler = "2.21"

    // google services versions
    const val Gson = "2.8.5"
    const val GoogleServices = "4.3.4"
    const val FirebaseBom = "26.2.0"
    const val FirebaseAnalytics = "18.0.0"
    const val FirebaseCrashanalyticsGradle = "2.4.1"
    const val Crashlytics = "17.3.0"
    const val GmsServiceAuth = "17.0.0"
    const val PlayCore = "1.9.0"
    const val PlayCoreKtx = "1.8.1"

    // gRPC dependencies version
    const val Okhttp = "1.24.0"
    const val ProtobufLite = "1.24.0"
    const val ProtobufGradlePlugin = "0.8.8"
    const val GrpcStub = "1.24.0"

    // external libraries versions
    const val BetterSpinner = "1.1.0"
    const val MpAndroidChartLibrary = "v3.1.0"
    const val ExpandableTextView = "0.1.4"
    const val TapTargetSafeView = "1.11.0"

    // test dependency versions
    const val Junit = "4.12"
    const val JunitExt = "1.1.2"
    const val EspressoCore = "3.3.0"

}

object BuildPlugins {
    const val navigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.Navigation}"
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.Gradle}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin}"
    const val protobufGradlePlugin = "com.google.protobuf:protobuf-gradle-plugin:${Versions.ProtobufGradlePlugin}"
    const val googleServicesGradlePlugin = "com.google.gms:google-services:${Versions.GoogleServices}"
    const val firebaseCrashlyticsGradle = "com.google.firebase:firebase-crashlytics-gradle:${Versions.FirebaseCrashanalyticsGradle}"

    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val kotlinKapt = "kotlin-kapt"
    const val navigationSafeArgsPlugin = "androidx.navigation.safeargs.kotlin"
    const val googleServicesPlugin = "com.google.gms.google-services"
    const val crashAnalyticsGradlePlugin = "com.google.firebase.crashlytics"
}

object TestLibs {
    const val junit = "junit:junit:${Versions.Junit}"
    const val junitExt = "androidx.test.ext:junit:${Versions.JunitExt}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.EspressoCore}"
    const val junitRunner = "androidx.test.runner.AndroidJUnitRunner"
}


object Libs {
    const val anko = "org.jetbrains.anko:anko:${Versions.Anko}"

    const val kotlinStd = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.Kotlin}"
    const val kotlinCoroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Coroutine}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.CoreKtx}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.Appcompat}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.ConstraintLayout}"
    const val androidMaterialLibrary = "com.google.android.material:material:${Versions.Material}"

    const val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.Lifecycle}"
    const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.Lifecycle}"
    const val lifecycleLiveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.Lifecycle}"
    const val lifecycleCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.Lifecycle}"
    const val lifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:${Versions.Lifecycle}"
    const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.Lifecycle}"

    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.Navigation}"
    const val navigationUIKtx = "androidx.navigation:navigation-ui-ktx:${Versions.Navigation}"
    const val navigationArchFragmentKtx = "android.arch.navigation:navigation-fragment-ktx:${Versions.Navigation}"
    const val navigationArchUIKtx = "android.arch.navigation:navigation-ui-ktx:${Versions.Navigation}"

    const val playCore = "com.google.android.play:core:${Versions.PlayCore}"
    const val playCoreKtx = "com.google.android.play:core-ktx:${Versions.PlayCoreKtx}"

    const val security = "androidx.security:security-crypto:${Versions.Security}"

    const val picasso = "com.squareup.picasso:picasso:${Versions.Picasso}"

    const val grpcOkHttp = "io.grpc:grpc-okhttp:${Versions.Okhttp}"
    const val grpcProtobufLite = "io.grpc:grpc-protobuf-lite:${Versions.ProtobufLite}"
    const val grpcStub = "io.grpc:grpc-stub:${Versions.GrpcStub}"

    const val gson = "com.google.code.gson:gson:${Versions.Gson}"

    const val javaxAnnotation = "javax.annotation:javax.annotation-api:${Versions.JavaxAnnotation}"

    const val browser = "androidx.browser:browser:${Versions.Browser}"

    const val preference = "androidx.preference:preference:${Versions.Preference}"

    const val betterSpinnerLibrary = "com.weiwangcn.betterspinner:library-material:${Versions.BetterSpinner}"

    const val mpAndroidChartLibrary = "com.github.PhilJay:MPAndroidChart:${Versions.MpAndroidChartLibrary}"

    const val expandableTextView = "com.ms-square:expandableTextView:${Versions.ExpandableTextView}"

    const val tapTargetSafeView = "com.getkeepsafe.taptargetview:taptargetview:${Versions.TapTargetSafeView}"

    const val dagger = "com.google.dagger:dagger:${Versions.Dagger}"
    const val daggerAndroid = "com.google.dagger:dagger-android:${Versions.Dagger}"
    const val daggerAndroidAndroidProcessor = "com.google.dagger:dagger-android-processor:${Versions.Dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.DaggerCompiler}"

    const val firebaseBom = "com.google.firebase:firebase-bom:${Versions.FirebaseBom}"
    const val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"
    const val crashAnalytics = "com.google.firebase:firebase-crashlytics-ktx"
    const val firebasecloudmessaging = "com.google.firebase:firebase-messaging-ktx"

    const val gmsServiceAuth = "com.google.android.gms:play-services-auth:${Versions.GmsServiceAuth}"

}
