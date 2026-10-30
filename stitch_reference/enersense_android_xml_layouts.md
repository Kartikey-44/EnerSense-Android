# EnerSense Android Architecture & XML Layout Specification

This document provides production-ready Android XML layouts, resource definitions (`colors.xml`, `dimens.xml`, `themes.xml`, `strings.xml`), and custom drawable drawables for the **EnerSense** residential energy application, built for standard Android Jetpack architecture (Kotlin, ViewBinding, Material Components 3, and MPAndroidChart).

---

## 1. Resource Files (`res/values/`)

### `res/values/colors.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Brand Primary & Secondary -->
    <color name="brand_primary">#0B533E</color>
    <color name="brand_primary_dark">#083E2E</color>
    <color name="brand_primary_light">#147558</color>
    <color name="brand_mint_surface">#E2F3EC</color>
    <color name="brand_mint_surface_subtle">#F0F8F4</color>
    <color name="brand_mint_accent">#34D399</color>
    
    <!-- Forecast Accent (Blue) -->
    <color name="forecast_blue">#2563EB</color>
    <color name="forecast_blue_light">#DBEAFE</color>
    <color name="forecast_blue_subtle">#EFF6FF</color>
    
    <!-- Neutrals & Surfaces -->
    <color name="background_light">#F8F9FA</color>
    <color name="surface_white">#FFFFFF</color>
    <color name="surface_card_border">#EDEFF2</color>
    <color name="surface_divider">#E2E8F0</color>
    
    <!-- Typography -->
    <color name="text_primary">#111827</color>
    <color name="text_secondary">#6B7280</color>
    <color name="text_muted">#9CA3AF</color>
    
    <!-- Status & Alerts -->
    <color name="status_active_dot">#10B981</color>
    <color name="status_error_red">#DC2626</color>
    <color name="status_error_bg">#FEE2E2</color>
</resources>
```

### `res/values/dimens.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="margin_normal">16dp</dimen>
    <dimen name="margin_small">8dp</dimen>
    <dimen name="margin_large">24dp</dimen>
    <dimen name="margin_tiny">4dp</dimen>
    
    <!-- Card Corners -->
    <dimen name="card_corner_radius">18dp</dimen>
    <dimen name="card_corner_small">12dp</dimen>
    <dimen name="card_elevation">1dp</dimen>
    
    <!-- Touch Targets & Heights -->
    <dimen name="min_touch_target">48dp</dimen>
    <dimen name="bottom_nav_height">64dp</dimen>
    <dimen name="button_height">48dp</dimen>
</resources>
```

### `res/values/themes.xml`
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.EnerSense" parent="Theme.Material3.Light.NoActionBar">
        <item name="colorPrimary">@color/brand_primary</item>
        <item name="colorSecondary">@color/brand_mint_accent</item>
        <item name="android:statusBarColor">@color/surface_white</item>
        <item name="android:windowLightStatusBar">true</item>
        <item name="android:colorBackground">@color/background_light</item>
    </style>
</resources>
```

---

## 2. Activity Root Shell & Navigation

### `res/layout/activity_main.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_light">

    <!-- Top App Bar / Compact Header -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/topAppBar"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:background="@color/surface_white"
        android:elevation="1dp"
        app:title="EnerSense"
        app:titleTextColor="@color/brand_primary"
        app:titleTextAppearance="@style/TextAppearance.Material3.TitleMedium">

        <ImageView
            android:id="@+id/ivProfileHeader"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:layout_gravity="end"
            android:layout_marginEnd="16dp"
            android:src="@drawable/ic_avatar_default"
            android:contentDescription="Profile icon" />
    </com.google.android.material.appbar.MaterialToolbar>

    <!-- NavHost Fragment for Screen Routing -->
    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="56dp"
        android:layout_marginBottom="64dp"
        app:defaultNavHost="true"
        app:navGraph="@navigation/nav_graph" />

    <!-- Fixed Bottom Navigation Bar -->
    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottomNavigationView"
        android:layout_width="match_parent"
        android:layout_height="64dp"
        android:layout_gravity="bottom"
        android:background="@color/surface_white"
        app:elevation="8dp"
        app:itemIconTint="@color/selector_nav_color"
        app:itemTextColor="@color/selector_nav_color"
        app:labelVisibilityMode="labeled"
        app:menu="@menu/bottom_nav_menu" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### `res/menu/bottom_nav_menu.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_home"
        android:icon="@drawable/ic_home"
        android:title="Home" />
    <item
        android:id="@+id/nav_history"
        android:icon="@drawable/ic_history"
        android:title="History" />
    <item
        android:id="@+id/nav_analytics"
        android:icon="@drawable/ic_analytics"
        android:title="Analytics" />
    <item
        android:id="@+id/nav_forecast"
        android:icon="@drawable/ic_trending_up"
        android:title="Forecast" />
    <item
        android:id="@+id/nav_profile"
        android:icon="@drawable/ic_person"
        android:title="Profile" />
</menu>
```

---

## 3. Screen Layouts

### 1. Home Dashboard (`res/layout/fragment_home.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    android:clipToPadding="false"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Greeting Header -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="16dp">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/tvGreeting"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Good morning!"
                    android:textSize="22sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Your energy overview is looking steady."
                    android:textSize="14sp"
                    android:textColor="@color/text_secondary" />
            </LinearLayout>

            <com.google.android.material.chip.Chip
                android:id="@+id/chipDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Tue, Oct 24"
                app:chipIcon="@drawable/ic_calendar"
                app:chipBackgroundColor="@color/surface_white"
                app:chipStrokeColor="@color/surface_card_border" />
        </LinearLayout>

        <!-- 1. Hero Card: Today's Consumption -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardElevation="@dimen/card_elevation"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Today's Consumption"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary" />

                    <ImageView
                        android:layout_width="22dp"
                        android:layout_height="22dp"
                        android:src="@drawable/ic_bolt"
                        app:tint="@color/brand_primary"
                        android:contentDescription="Energy Bolt" />
                </LinearLayout>

                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="baseline"
                    android:layout_marginTop="8dp">

                    <TextView
                        android:id="@+id/tvTodayValue"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="12.4"
                        android:textSize="36sp"
                        android:textStyle="bold"
                        android:textColor="@color/brand_primary" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="6dp"
                        android:text="kWh"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>

                <TextView
                    android:id="@+id/tvLastSyncTime"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="• Recorded through 10:15 AM today"
                    android:textSize="13sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="4dp" />

                <!-- MPAndroidChart Sparkline Curve -->
                <com.github.mikephil.charting.charts.LineChart
                    android:id="@+id/chartTodayTrend"
                    android:layout_width="match_parent"
                    android:layout_height="110dp"
                    android:layout_marginTop="12dp" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- 2. Tomorrow's Forecast Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardElevation="@dimen/card_elevation"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Tomorrow's Forecast"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/bg_forecast_badge"
                        android:paddingHorizontal="10dp"
                        android:paddingVertical="4dp"
                        android:text="7-Day Moving Avg"
                        android:textColor="@color/forecast_blue"
                        android:textSize="11sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="baseline"
                    android:layout_marginTop="8dp">

                    <TextView
                        android:id="@+id/tvForecastValue"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="14.2"
                        android:textSize="32sp"
                        android:textStyle="bold"
                        android:textColor="@color/forecast_blue" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="6dp"
                        android:text="kWh"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Wednesday, Oct 25 • Baseline moving average"
                    android:textSize="13sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="4dp" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- 3. Live Simulation Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="24dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardElevation="@dimen/card_elevation"
            app:cardBackgroundColor="@color/brand_mint_surface_subtle"
            app:strokeColor="@color/brand_mint_surface"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <ImageView
                        android:layout_width="12dp"
                        android:layout_height="12dp"
                        android:src="@drawable/ic_dot_active"
                        app:tint="@color/brand_primary"
                        android:contentDescription="Status active" />

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginStart="8dp"
                        android:text="Live Simulation"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:textColor="@color/brand_primary" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Ready to Replay"
                        android:textSize="12sp"
                        android:textStyle="bold"
                        android:textColor="@color/brand_primary" />
                </LinearLayout>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Historical Data Replay from Sep 14, 2023 • 08:30:00 AM"
                    android:textSize="13sp"
                    android:textColor="@color/text_primary"
                    android:layout_marginTop="8dp" />

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnLaunchSimulation"
                    android:layout_width="match_parent"
                    android:layout_height="52dp"
                    android:layout_marginTop="16dp"
                    android:text="Launch Simulation"
                    android:textAllCaps="false"
                    android:textSize="15sp"
                    app:cornerRadius="12dp"
                    app:backgroundTint="@color/brand_primary" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_horizontal"
                    android:text="Replaying recorded residential meter data — not a live grid connection."
                    android:textSize="11sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="8dp" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- 4. Quick Actions (2x2 Grid) -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Quick Actions"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
            android:layout_marginBottom="12dp" />

        <GridLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:columnCount="2"
            android:alignmentMode="alignMargins"
            android:useDefaultMargins="true">

            <!-- History Action -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardActionHistory"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="@dimen/card_corner_small"
                app:cardBackgroundColor="@color/surface_white"
                app:strokeColor="@color/surface_card_border"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">
                    <ImageView
                        android:layout_width="28dp"
                        android:layout_height="28dp"
                        android:src="@drawable/ic_history"
                        app:tint="@color/brand_primary" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="History"
                        android:textStyle="bold"
                        android:textSize="14sp"
                        android:layout_marginTop="10dp" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Review past logs"
                        android:textSize="11sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Analytics Action -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardActionAnalytics"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="@dimen/card_corner_small"
                app:cardBackgroundColor="@color/surface_white"
                app:strokeColor="@color/surface_card_border"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">
                    <ImageView
                        android:layout_width="28dp"
                        android:layout_height="28dp"
                        android:src="@drawable/ic_analytics"
                        app:tint="@color/brand_primary" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Analytics"
                        android:textStyle="bold"
                        android:textSize="14sp"
                        android:layout_marginTop="10dp" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Period breakdowns"
                        android:textSize="11sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Forecast Action -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardActionForecast"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="@dimen/card_corner_small"
                app:cardBackgroundColor="@color/surface_white"
                app:strokeColor="@color/surface_card_border"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">
                    <ImageView
                        android:layout_width="28dp"
                        android:layout_height="28dp"
                        android:src="@drawable/ic_trending_up"
                        app:tint="@color/forecast_blue" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Forecast"
                        android:textStyle="bold"
                        android:textSize="14sp"
                        android:layout_marginTop="10dp" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="7-day average"
                        android:textSize="11sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Simulation Action -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardActionSimulation"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="@dimen/card_corner_small"
                app:cardBackgroundColor="@color/surface_white"
                app:strokeColor="@color/surface_card_border"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">
                    <ImageView
                        android:layout_width="28dp"
                        android:layout_height="28dp"
                        android:src="@drawable/ic_play_circle"
                        app:tint="@color/brand_primary" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Live Simulation"
                        android:textStyle="bold"
                        android:textSize="14sp"
                        android:layout_marginTop="10dp" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Replay sessions"
                        android:textSize="11sp"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </GridLayout>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

---

### 2. Consumption History (`res/layout/fragment_history.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/swipeRefresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- Title & Subtitle -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Consumption History"
                android:textSize="24sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Historical smart meter daily logs"
                android:textSize="14sp"
                android:textColor="@color/text_secondary"
                android:layout_marginTop="2dp"
                android:layout_marginBottom="16dp" />

            <!-- Filter Chips -->
            <HorizontalScrollView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:scrollbars="none"
                android:layout_marginBottom="16dp">

                <com.google.android.material.chip.ChipGroup
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    app:singleSelection="true">

                    <com.google.android.material.chip.Chip
                        android:id="@+id/chip7Days"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Last 7 Days"
                        android:checked="true"
                        style="@style/Widget.Material3.Chip.Filter" />

                    <com.google.android.material.chip.Chip
                        android:id="@+id/chipCustomRange"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Oct 18 – Oct 24, 2023"
                        style="@style/Widget.Material3.Chip.Filter" />
                </com.google.android.material.chip.ChipGroup>
            </HorizontalScrollView>

            <!-- 7-Day Aggregated Summary -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="@dimen/card_corner_radius"
                app:cardBackgroundColor="@color/surface_white"
                app:strokeColor="@color/surface_card_border"
                app:strokeWidth="1dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:baselineAligned="false"
                    android:padding="16dp">

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="7-Day Total"
                            android:textSize="12sp"
                            android:textColor="@color/text_secondary" />

                        <TextView
                            android:id="@+id/tv7DayTotal"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="88.6 kWh"
                            android:textSize="22sp"
                            android:textStyle="bold"
                            android:textColor="@color/brand_primary" />
                    </LinearLayout>

                    <View
                        android:layout_width="1dp"
                        android:layout_height="match_parent"
                        android:background="@color/surface_divider"
                        android:layout_marginHorizontal="12dp" />

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Daily Average"
                            android:textSize="12sp"
                            android:textColor="@color/text_secondary" />

                        <TextView
                            android:id="@+id/tv7DayAvg"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="12.6 kWh"
                            android:textSize="22sp"
                            android:textStyle="bold"
                            android:textColor="@color/brand_primary" />
                    </LinearLayout>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- RecyclerView for History Records -->
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/rvHistoryItems"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:nestedScrollingEnabled="false" />

            <!-- Empty State Container (Gone by default) -->
            <LinearLayout
                android:id="@+id/layoutEmptyState"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:padding="32dp"
                android:visibility="gone">

                <ImageView
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:src="@drawable/ic_history"
                    app:tint="@color/text_muted" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="No consumption data available for this period."
                    android:textSize="14sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="12dp" />
            </LinearLayout>

        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

#### History Row Item (`res/layout/item_history_record.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp"
    app:cardCornerRadius="@dimen/card_corner_small"
    app:cardBackgroundColor="@color/surface_white"
    app:strokeColor="@color/surface_card_border"
    app:strokeWidth="1dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:padding="16dp">

        <ImageView
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:src="@drawable/ic_calendar"
            app:tint="@color/text_secondary" />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:layout_marginStart="14dp">

            <TextView
                android:id="@+id/tvRecordDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Saturday, Oct 21"
                android:textSize="15sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary" />

            <TextView
                android:id="@+id/tvRecordSubtext"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Recorded daily total"
                android:textSize="12sp"
                android:textColor="@color/text_secondary" />
        </LinearLayout>

        <TextView
            android:id="@+id/tvRecordKwh"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="14.6 kWh"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@color/brand_primary" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

---

### 3. Analytics Dashboard (`res/layout/fragment_analytics.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Title -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Analytics"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Aggregated smart meter consumption analysis"
            android:textSize="14sp"
            android:textColor="@color/text_secondary"
            android:layout_marginBottom="16dp" />

        <!-- Granularity Selector: Hourly | Daily | Weekly | Monthly | Yearly -->
        <com.google.android.material.tabs.TabLayout
            android:id="@+id/tabPeriodSelector"
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:background="@drawable/bg_tab_segmented"
            app:tabIndicator="@drawable/bg_tab_indicator"
            app:tabIndicatorColor="@color/brand_primary"
            app:tabIndicatorGravity="stretch"
            app:tabSelectedTextColor="@color/surface_white"
            app:tabTextColor="@color/text_secondary"
            app:tabMode="fixed"
            app:tabGravity="fill"
            android:layout_marginBottom="16dp">

            <com.google.android.material.tabs.TabItem
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Hourly" />
            <com.google.android.material.tabs.TabItem
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Daily" />
            <com.google.android.material.tabs.TabItem
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Weekly" />
            <com.google.android.material.tabs.TabItem
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Monthly" />
            <com.google.android.material.tabs.TabItem
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Yearly" />
        </com.google.android.material.tabs.TabLayout>

        <!-- Summary Metrics: Total, Peak Load, Avg / Day -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="16dp">

            <!-- Total Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="6dp"
                app:cardCornerRadius="@dimen/card_corner_small"
                app:cardBackgroundColor="@color/surface_white"
                app:strokeColor="@color/surface_card_border"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="12dp">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Total"
                        android:textSize="12sp"
                        android:textColor="@color/text_secondary" />
                    <TextView
                        android:id="@+id/tvTotalKwh"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="88.6"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="kWh"
                        android:textSize="11sp"
                        android:textColor="@color/text_muted" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Peak Load Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginHorizontal="3dp"
                app:cardCornerRadius="@dimen/card_corner_small"
                app:cardBackgroundColor="@color/surface_white"
                app:strokeColor="@color/surface_card_border"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="12dp">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Peak Load"
                        android:textSize="12sp"
                        android:textColor="@color/text_secondary" />
                    <TextView
                        android:id="@+id/tvPeakLoad"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="2.1"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="kW"
                        android:textSize="11sp"
                        android:textColor="@color/text_muted" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Avg / Day Card -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="6dp"
                app:cardCornerRadius="@dimen/card_corner_small"
                app:cardBackgroundColor="@color/surface_white"
                app:strokeColor="@color/surface_card_border"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="12dp">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Avg / Day"
                        android:textSize="12sp"
                        android:textColor="@color/text_secondary" />
                    <TextView
                        android:id="@+id/tvAvgDay"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="12.6"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary" />
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="kWh/day"
                        android:textSize="11sp"
                        android:textColor="@color/text_muted" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

        <!-- Chart Card (MPAndroidChart) -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:id="@+id/tvChartHeaderTitle"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Daily Consumption Trend"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Oct 18 – Oct 24 • Aggregated Meter Telemetry"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginBottom="12dp" />

                <!-- MPAndroidChart LineChart view -->
                <com.github.mikephil.charting.charts.LineChart
                    android:id="@+id/chartAnalyticsMain"
                    android:layout_width="match_parent"
                    android:layout_height="220dp" />

                <!-- MPAndroidChart BarChart view (toggled based on selected tab) -->
                <com.github.mikephil.charting.charts.BarChart
                    android:id="@+id/chartAnalyticsBar"
                    android:layout_width="match_parent"
                    android:layout_height="220dp"
                    android:visibility="gone" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

---

### 4. Consumption Forecast (`res/layout/fragment_forecast.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Title -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Consumption Forecast"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Target: Tomorrow, Wed, Oct 25, 2023"
            android:textSize="14sp"
            android:textColor="@color/text_secondary"
            android:layout_marginTop="2dp"
            android:layout_marginBottom="16dp" />

        <!-- Hero Forecast Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardBackgroundColor="@color/forecast_blue_subtle"
            app:strokeColor="@color/forecast_blue_light"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="PROJECTED NEXT DAY USAGE"
                    android:textSize="12sp"
                    android:textStyle="bold"
                    android:textColor="@color/forecast_blue" />

                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="baseline"
                    android:layout_marginTop="6dp">

                    <TextView
                        android:id="@+id/tvForecastHeroNumber"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="14.2"
                        android:textSize="40sp"
                        android:textStyle="bold"
                        android:textColor="@color/forecast_blue" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="8dp"
                        android:text="kWh"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Calculated using a 7-day rolling moving average baseline."
                    android:textSize="13sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="8dp" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- 7-Day Trajectory Bar Chart -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="7-Day Trajectory"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Recorded Actuals vs Rolling Forecast"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginBottom="12dp" />

                <com.github.mikephil.charting.charts.BarChart
                    android:id="@+id/chartForecastTrajectory"
                    android:layout_width="match_parent"
                    android:layout_height="180dp" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Method Transparency Note -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="@dimen/card_corner_small"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="How the Forecast Works"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Projections calculate the rolling 7-day average of your past residential smart meter readings to estimate upcoming daily demand without complex assumptions."
                    android:textSize="13sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="6dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Method: 7-Day Simple Moving Average (SMA)"
                    android:textSize="12sp"
                    android:textStyle="bold"
                    android:textColor="@color/brand_primary"
                    android:layout_marginTop="10dp" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

---

### 5. Live Simulation (`res/layout/fragment_live_simulation.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Disclaimer Banner -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="@dimen/card_corner_small"
            app:cardBackgroundColor="@color/brand_mint_surface"
            app:strokeColor="@color/brand_mint_accent"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="14dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="HISTORICAL DATA REPLAY"
                    android:textSize="11sp"
                    android:textStyle="bold"
                    android:textColor="@color/brand_primary" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Replaying recorded smart-meter readings from Sep 14, 2023. This is a simulated replay, not a live physical grid connection."
                    android:textSize="12sp"
                    android:textColor="@color/brand_primary_dark"
                    android:layout_marginTop="4dp" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Simulated Meter Clock & Speed -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="SIMULATED METER TIMESTAMP"
                    android:textSize="11sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_secondary" />

                <TextView
                    android:id="@+id/tvSimClock"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="14:34:35 EDT"
                    android:textSize="32sp"
                    android:textStyle="bold"
                    android:textColor="@color/brand_primary"
                    android:layout_marginTop="4dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Recorded on Thursday, September 14, 2023"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="2dp" />

                <!-- Replay Tempo Radio Group -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginTop="16dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="REPLAY TEMPO"
                        android:textSize="11sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_secondary" />

                    <com.google.android.material.button.MaterialButtonToggleGroup
                        android:id="@+id/toggleTempo"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        app:singleSelection="true"
                        app:checkedButton="@id/btnSpeed1x">

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnSpeed1x"
                            style="?attr/materialButtonOutlinedStyle"
                            android:layout_width="wrap_content"
                            android:layout_height="36dp"
                            android:text="1x"
                            android:textSize="12sp" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnSpeed5x"
                            style="?attr/materialButtonOutlinedStyle"
                            android:layout_width="wrap_content"
                            android:layout_height="36dp"
                            android:text="5x"
                            android:textSize="12sp" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnSpeed10x"
                            style="?attr/materialButtonOutlinedStyle"
                            android:layout_width="wrap_content"
                            android:layout_height="36dp"
                            android:text="10x"
                            android:textSize="12sp" />
                    </com.google.android.material.button.MaterialButtonToggleGroup>
                </LinearLayout>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Demand Rate Telemetry -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Instantaneous Demand Rate"
                    android:textSize="15sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary" />

                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="baseline"
                    android:layout_marginTop="6dp">

                    <TextView
                        android:id="@+id/tvDemandRate"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="1.81"
                        android:textSize="36sp"
                        android:textStyle="bold"
                        android:textColor="@color/brand_primary" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="6dp"
                        android:text="kW"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_secondary" />
                </LinearLayout>

                <TextView
                    android:id="@+id/tv15MinAccumulation"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="15-min Block Accumulation: 0.45 kWh"
                    android:textSize="13sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="8dp" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Playback Deck Controls -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Playback Deck"
                    android:textSize="15sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary" />

                <com.google.android.material.slider.Slider
                    android:id="@+id/sliderPlaybackProgress"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:valueFrom="0.0"
                    android:valueTo="100.0"
                    android:value="60.0"
                    app:thumbColor="@color/brand_primary"
                    app:trackColorActive="@color/brand_primary"
                    app:trackColorInactive="@color/surface_divider" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center"
                    android:layout_marginTop="12dp">

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnReset"
                        style="?attr/materialButtonOutlinedStyle"
                        android:layout_width="0dp"
                        android:layout_height="48dp"
                        android:layout_weight="1"
                        android:layout_marginEnd="8dp"
                        android:text="Reset"
                        android:textAllCaps="false" />

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnTogglePlayPause"
                        android:layout_width="0dp"
                        android:layout_height="48dp"
                        android:layout_weight="2"
                        android:text="Pause Simulation"
                        android:textAllCaps="false"
                        app:backgroundTint="@color/brand_primary" />

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnStepForward"
                        style="?attr/materialButtonOutlinedStyle"
                        android:layout_width="0dp"
                        android:layout_height="48dp"
                        android:layout_weight="1"
                        android:layout_marginStart="8dp"
                        android:text="+15 min"
                        android:textAllCaps="false" />
                </LinearLayout>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

---

### 6. Profile & Settings (`res/layout/fragment_profile.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Account Info Header -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="20dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:padding="20dp">

                <ImageView
                    android:id="@+id/ivProfileAvatar"
                    android:layout_width="64dp"
                    android:layout_height="64dp"
                    android:src="@drawable/ic_avatar_default"
                    android:contentDescription="User Avatar" />

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical"
                    android:layout_marginStart="16dp">

                    <TextView
                        android:id="@+id/tvAccountNumber"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Residential Account"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/brand_primary" />

                    <TextView
                        android:id="@+id/tvMeterHardwareId"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Meter ID: SM-4819-TX"
                        android:textSize="13sp"
                        android:textColor="@color/text_secondary"
                        android:layout_marginTop="4dp" />
                </LinearLayout>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Account Settings & Preferences -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="PREFERENCES &amp; ACCOUNT SETTINGS"
            android:textSize="12sp"
            android:textStyle="bold"
            android:textColor="@color/text_secondary"
            android:layout_marginBottom="10dp" />

        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="24dp"
            app:cardCornerRadius="@dimen/card_corner_radius"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <!-- Data Sync Toggle -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:paddingVertical="8dp">

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Data Synchronization"
                            android:textSize="15sp"
                            android:textStyle="bold"
                            android:textColor="@color/text_primary" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Auto-sync every 15 min"
                            android:textSize="12sp"
                            android:textColor="@color/text_secondary" />
                    </LinearLayout>

                    <com.google.android.material.materialswitch.MaterialSwitch
                        android:id="@+id/switchDataSync"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:checked="true"
                        app:thumbTint="@color/surface_white"
                        app:trackTint="@color/brand_primary" />
                </LinearLayout>

                <View
                    android:layout_width="match_parent"
                    android:layout_height="1dp"
                    android:background="@color/surface_divider"
                    android:layout_marginVertical="8dp" />

                <!-- Cache Management -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:paddingVertical="8dp">

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Offline Data Cache"
                            android:textSize="15sp"
                            android:textStyle="bold"
                            android:textColor="@color/text_primary" />

                        <TextView
                            android:id="@+id/tvCacheSize"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="14.2 MB stored"
                            android:textSize="12sp"
                            android:textColor="@color/text_secondary" />
                    </LinearLayout>

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnClearCache"
                        style="?attr/materialButtonOutlinedStyle"
                        android:layout_width="wrap_content"
                        android:layout_height="36dp"
                        android:text="Clear"
                        android:textAllCaps="false"
                        android:textSize="12sp" />
                </LinearLayout>

                <View
                    android:layout_width="match_parent"
                    android:layout_height="1dp"
                    android:background="@color/surface_divider"
                    android:layout_marginVertical="8dp" />

                <!-- Privacy Disclosure -->
                <LinearLayout
                    android:id="@+id/rowPrivacy"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:paddingVertical="8dp">

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Privacy &amp; Data Disclosure"
                            android:textSize="15sp"
                            android:textStyle="bold"
                            android:textColor="@color/text_primary" />

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Consumer smart meter privacy"
                            android:textSize="12sp"
                            android:textColor="@color/text_secondary" />
                    </LinearLayout>

                    <ImageView
                        android:layout_width="20dp"
                        android:layout_height="20dp"
                        android:src="@drawable/ic_chevron_right"
                        app:tint="@color/text_secondary" />
                </LinearLayout>

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Logout Action -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/cardSignOut"
            android:layout_width="match_parent"
            android:layout_height="52dp"
            app:cardCornerRadius="@dimen/card_corner_small"
            app:cardBackgroundColor="@color/surface_white"
            app:strokeColor="@color/surface_card_border"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:gravity="center"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@drawable/ic_logout"
                    app:tint="@color/status_error_red" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="8dp"
                    android:text="Sign Out of Account"
                    android:textSize="15sp"
                    android:textStyle="bold"
                    android:textColor="@color/status_error_red" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:text="Session Token Expiring in 24 hrs • Encrypted AES-256"
            android:textSize="11sp"
            android:textColor="@color/text_muted"
            android:layout_marginTop="12dp" />

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```
