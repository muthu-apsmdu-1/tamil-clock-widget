<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/widget_root"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- 1. Dynamic Background Bitmap Layer (Theme color + Opacity + Border) -->
    <ImageView
        android:id="@+id/widget_bg_image"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="fitXY"
        android:contentDescription="Widget Background" />

    <!-- 2. Main Vertical Distribution: Clock (Top 2/3) + Elements (Bottom 1/3) -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:paddingHorizontal="12dp"
        android:paddingVertical="8dp"
        android:weightSum="3"
        android:gravity="center_horizontal">

        <!-- TOP 2/3: Clock Container (Centered) -->
        <FrameLayout
            android:id="@+id/container_clock_top"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="2"
            android:layout_gravity="center"
            android:layout_marginBottom="4dp">

            <!-- Classic Vintage Analog Clock (Direct on theme background) -->
            <AnalogClock
                android:id="@+id/analog_clock_view"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_gravity="center"
                android:dial="@drawable/classic_clock_dial"
                android:hand_hour="@drawable/classic_clock_hour"
                android:hand_minute="@drawable/classic_clock_minute"
                android:visibility="gone" />

            <!-- Digital Clock: Big time font with half-size AM/PM -->
            <TextView
                android:id="@+id/tv_digital_clock"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:gravity="center"
                android:textSize="36sp"
                android:textStyle="bold"
                android:includeFontPadding="false"
                android:visibility="visible" />
        </FrameLayout>

        <!-- Horizontal Divider Line -->
        <View
            android:id="@+id/view_center_divider"
            android:layout_width="match_parent"
            android:layout_height="1.5dp"
            android:layout_marginVertical="4dp"
            android:layout_marginHorizontal="8dp"
            android:background="#44FFDF00" />

        <!-- BOTTOM 1/3: Elements Container in Rows -->
        <LinearLayout
            android:id="@+id/container_elements_bottom"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:orientation="vertical"
            android:weightSum="4"
            android:gravity="center_vertical"
            android:paddingHorizontal="4dp">

            <!-- Row 1 (Fixed): Gregorian Date + Tamil Weekday + Tamil Month & Date -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:orientation="horizontal"
                android:gravity="center">

                <TextView
                    android:id="@+id/tv_widget_greg_date"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textSize="12.5sp"
                    android:textStyle="bold"
                    android:maxLines="1"
                    android:includeFontPadding="false" />

                <TextView
                    android:id="@+id/tv_widget_fixed_sep1"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text=" • "
                    android:textSize="12.5sp"
                    android:textStyle="bold"
                    android:includeFontPadding="false" />

                <TextView
                    android:id="@+id/tv_widget_weekday"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textSize="12.5sp"
                    android:textStyle="bold"
                    android:maxLines="1"
                    android:includeFontPadding="false" />

                <TextView
                    android:id="@+id/tv_widget_fixed_sep2"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text=" • "
                    android:textSize="12.5sp"
                    android:textStyle="bold"
                    android:includeFontPadding="false" />

                <TextView
                    android:id="@+id/tv_widget_tamil_full"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textSize="12.5sp"
                    android:textStyle="bold"
                    android:maxLines="1"
                    android:includeFontPadding="false" />
            </LinearLayout>

            <!-- Row 2: Selected Options Paragraph Part 1 -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:gravity="center">

                <TextView
                    android:id="@+id/tv_widget_row2_paragraph"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textSize="11.5sp"
                    android:textStyle="bold"
                    android:maxLines="1"
                    android:gravity="center"
                    android:includeFontPadding="false" />
            </LinearLayout>

            <!-- Row 3: Selected Options Paragraph Part 2 -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:gravity="center">

                <TextView
                    android:id="@+id/tv_widget_row3_paragraph"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textSize="11.5sp"
                    android:textStyle="bold"
                    android:maxLines="1"
                    android:gravity="center"
                    android:includeFontPadding="false" />
            </LinearLayout>

            <!-- Row 4: Selected Options Paragraph Part 3 -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:gravity="center">

                <TextView
                    android:id="@+id/tv_widget_row4_paragraph"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textSize="11.5sp"
                    android:textStyle="bold"
                    android:maxLines="1"
                    android:gravity="center"
                    android:includeFontPadding="false" />
            </LinearLayout>

        </LinearLayout>

    </LinearLayout>

</FrameLayout>
