    // -------------------------------------------------------------
    // 2. UPDATE 4x1 WIDGET (¼ Clock + Balance 3/4 Area)
    // Row 1 = Gregorian Date (optional style) + Tamil Weekday
    // Row 2 = Tamil Month & Tamil Date + Converted Paksham & Thithi
    // -------------------------------------------------------------
    @JvmOverloads
    fun update4x1(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {
        val layoutId = context.resources.getIdentifier("widget_layout_4x1", "layout", context.packageName)
        if (layoutId == 0) return
        val views = RemoteViews(context.packageName, layoutId)

        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 140)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context)

        // 1. Clock at 1/4 Area (Analog vs Digital with half-size AM/PM)
        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "tv_digital_clock", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
            views.safeSetText(context, "tv_digital_clock", formatDigitalTimeWithHalfAmPm(cal))
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
        }

        // 2. Balance Area - Row 1: Gregorian Date (Type Optional via Settings) + Tamil Weekday
        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        val weekdayStr = if (data.weekdayTa.isNotBlank()) data.weekdayTa else "---"
        views.safeSetText(context, "tv_widget_weekday", weekdayStr)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentGold)

        // 3. Balance Area - Row 2: Tamil Month & Date + Paksham & Thithi
        val tMonth = if (data.tamilMonth.isNotBlank()) data.tamilMonth else "---"
        val tDay = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_date", "$tMonth $tDay")
        views.safeSetTextColor(context, "tv_widget_tamil_date", colors.secondaryText)

        val convertedPaksham = convertPakshamName(data.paksha)
        val thithiStr = if (data.tithiName.isNotBlank()) data.tithiName else "---"
        views.safeSetText(context, "tv_widget_paksham_thithi", "$convertedPaksham • $thithiStr")
        views.safeSetTextColor(context, "tv_widget_paksham_thithi", colors.primaryText)

        // Pending Intent
        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider4x1::class.java))
        }

        if (targetIds != null && targetIds.isNotEmpty()) {
            manager.updateAppWidget(targetIds, views)
        }
    }
