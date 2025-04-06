plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.projetws"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.projetws"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.10")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Volley pour les requêtes HTTP
    implementation ("com.android.volley:volley:1.2.1")

    // Gson pour la manipulation JSON
    implementation ("com.google.code.gson:gson:2.8.9")

    // Picasso pour le chargement d'images
    implementation ("com.squareup.picasso:picasso:2.71828")

    // CardView pour les items de la liste
    implementation ("androidx.cardview:cardview:1.0.0")

}