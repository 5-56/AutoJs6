plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.xihe.automation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xihe.automation"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 从 local.properties 读取 AI API 配置
        val properties = java.util.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        
        buildConfigField("String", "AI_API_KEY", "\"${properties.getProperty("ai.api.key", "")}\"")
        buildConfigField("String", "AI_API_URL", "\"${properties.getProperty("ai.api.url", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX 核心库
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    
    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    
    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // JSON 解析
    implementation("com.google.code.gson:gson:2.11.0")
    
    // Rhino JavaScript 引擎 (来自 AutoJs6)
    implementation(files("libs/rhino-1.8.1-SNAPSHOT.jar"))
    
    // OpenCV (用于屏幕分析)
    implementation("org.opencv:opencv:4.8.0")
    
    // MLKit OCR (屏幕文字识别)
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")
    
    // EventBus (事件总线)
    implementation("org.greenrobot:eventbus:3.3.1")
    
    // 日志
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
