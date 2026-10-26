package org.caresphere.android

import android.app.*
import android.os.*
import android.content.*
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.*
import java.util.Locale
import java.time.format.DateTimeFormatter


class MainActivity : ProtectedActivity() {
    private fun tx(value:String)=AppLanguage.text(this,value)
    private lateinit var data: CareData
    private lateinit var monitor: HeartRateMonitor
    private lateinit var page: LinearLayout
    private lateinit var root: LinearLayout
    private var tab = 0
    private var settings = false
    private var activityIndex = -1
    private var activityQuery = ""
    private var noteQuery = ""
    private var favoritesOnly = false
    private var fewerChoices = true
    private var round = 0
    private var feedback = ""
    private var firstPair = -1
    private var secondPair = -1
    private val matched = mutableSetOf<Int>()
    private var memoryHidden = false
    private var memoryPosition = 0
    private var gameLevel = "gentle"
    private var gameFocus = "all"
    private var gamePreferencesSet = false
    private var memoryPairCount = 2
    private var memoryTheme = "Nature"
    private val pictureThemes = mapOf(
        "Nature" to listOf("Leaf", "Flower", "Bird", "Sun", "Cloud", "River", "Acorn", "Mushroom"),
        "Home" to listOf("Cup", "Book", "Lamp", "Chair", "Key", "Clock", "Plate", "House"),
        "Food" to listOf("Apple", "Bread", "Pear", "Soup", "Rice", "Banana", "Carrot", "Cheese")
    )
    private var memoryCards = emptyList<String>()
    private var pairsRevealAll = false
    private var bingoPlayerCount = 2
    private var bingoCards = LocalGameRules.bingoCards(2)
    private var bingoCalls = LocalGameRules.bingoCalls().toMutableList()
    private val bingoCalled = mutableListOf<Int>()
    private val bingoMarks = mutableListOf<MutableSet<Int>>()
    private var bingoActivePlayer = 0
    private var solitaire = LocalGameRules.newSolitaire()
    private var solitaireSelection: Pair<Boolean, Pair<Int, Int>>? = null
    private var solitaireDrawCount = 1
    private val solitaireHistory = mutableListOf<LocalGameRules.SolitaireState>()
    private var wordTheme = "Nature"
    private var wordSize = 8
    private var wordPuzzle = LocalGameRules.wordPuzzle(listOf("FLOWER", "LEAF", "TREE", "BIRD", "CLOUD"), 8)
    private var wordStart: Pair<Int, Int>? = null
    private val foundWords = mutableSetOf<String>()
    private val foundCells = mutableSetOf<Pair<Int, Int>>()
    private var breathing = false
    private var breathSecond = 0
    private var playing = false
    private val sound = CalmAudio()
    private val handler = Handler(Looper.getMainLooper())
    private var breathLabel: TextView? = null
    private var hrLabel: TextView? = null
    private var hrStatus: TextView? = null
    private var hrTimestamp: TextView? = null
    private var deviceList: LinearLayout? = null
    private var hrControl: Button? = null
    private var displayedDevices = ""
    private val dark: Boolean get() = when (if (::data.isInitialized) data.json.optString("appearance", "system") else "system") {
        "dark" -> true; "light" -> false
        else -> resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    private val green get() = if (dark) Color.rgb(168,211,178) else Color.rgb(55,111,76)
    private val fill get() = Color.rgb(55,111,76)
    private val ink get() = if (dark) Color.rgb(237,244,237) else Color.rgb(28,46,36)
    private val muted get() = if (dark) Color.rgb(189,205,190) else Color.rgb(80,98,87)
    private val bg get() = if (dark) Color.rgb(20,29,23) else Color.rgb(246,248,244)
    private val surface get() = if (dark) Color.rgb(31,43,34) else Color.WHITE
    private val soft get() = if (dark) Color.rgb(45,62,49) else Color.rgb(233,240,232)
    private fun dialogBuilder() = AlertDialog.Builder(ContextThemeWrapper(this,
        if (dark) android.R.style.Theme_Material_Dialog_Alert else android.R.style.Theme_Material_Light_Dialog_Alert))
    private val pairWords = listOf("Flower", "Star", "Leaf", "Star", "Flower", "Leaf")
    private val memory = listOf("Circle", "Star", "Leaf")
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = CareData(this)
        monitor = HeartRateMonitor(this) { updateHealth() }
        savedInstanceState?.let {
            activityQuery = it.getString("activityQuery", ""); noteQuery = it.getString("noteQuery", ""); favoritesOnly = it.getBoolean("favoritesOnly")
            tab = it.getInt("tab"); settings = it.getBoolean("settings")
            activityIndex = it.getInt("activity", -1); round = it.getInt("round"); fewerChoices = it.getBoolean("fewerChoices", true)
            feedback = it.getString("feedback", ""); memoryHidden = it.getBoolean("memoryHidden")
            memoryPosition = it.getInt("memoryPosition"); matched.addAll(it.getIntArray("matched")?.toList() ?: emptyList())
            firstPair = it.getInt("first", -1); secondPair = it.getInt("second", -1)
        }
        render()
    }
    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putString("activityQuery", activityQuery); out.putString("noteQuery", noteQuery); out.putBoolean("favoritesOnly", favoritesOnly)
        out.putInt("tab", tab); out.putBoolean("settings", settings); out.putInt("activity", activityIndex); out.putInt("round", round); out.putBoolean("fewerChoices", fewerChoices)
        out.putString("feedback", feedback); out.putBoolean("memoryHidden", memoryHidden); out.putInt("memoryPosition", memoryPosition)
        out.putIntArray("matched", matched.toIntArray()); out.putInt("first", firstPair); out.putInt("second", secondPair)
    }
    override fun onResume() { super.onResume(); if (::data.isInitialized) { data = CareData(this); data.refreshDay(); if(data.json.optJSONObject("tools")?.optBoolean("simple") == true && tab > 1) tab = 0; render() } }
    override fun onStop() {
        stopComfort()
        if (::monitor.isInitialized) monitor.disconnect("Monitoring stopped when CareSphere left the foreground. Reconnect when you're ready.")
        super.onStop()
    }
    override fun onDestroy() { if (::monitor.isInitialized) monitor.close(); handler.removeCallbacksAndMessages(null); super.onDestroy() }
    @Deprecated("Uses the platform back dispatcher for compatibility")
    override fun onBackPressed() {
        if (settings || activityIndex != -1) { settings = false; activityIndex = -1; stopComfort(); render() }
        else if (tab != 0) { tab = 0; render() } else super.onBackPressed()
    }
    private fun stopComfort() { breathing = false; playing = false; sound.stop(); handler.removeCallbacks(breathTick) }
    private fun shape(color: Int, radius: Int = 20) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun column(parent: LinearLayout = page) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        parent.addView(this, LinearLayout.LayoutParams(-1, -2))
    }
    private fun text(value: String, size: Float = 17f, parent: LinearLayout = page, bold: Boolean = false): TextView = TextView(this).apply {
        text = value; textLocale=Locale.forLanguageTag(AppLanguage.current(this@MainActivity)); textSize = size; setTextColor(if (bold) ink else muted)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(5), 0, dp(7)); setTextIsSelectable(true)
        parent.addView(this, LinearLayout.LayoutParams(-1, -2))
    }
    private fun copy(value: String, size: Float = 17f, parent: LinearLayout = page) { if (!DisplayMode.enabled(this)) text(value, size, parent) }
    private fun button(label: String, parent: LinearLayout = page, primary: Boolean = false, action: () -> Unit): Button = Button(this).apply {
        text = label; textSize = 17f; isAllCaps = false; minHeight = dp(52)
        setPadding(dp(12), dp(10), dp(12), dp(10)); setTextColor(if(primary) Color.WHITE else green)
        background = shape(if (primary) fill else soft, 14)
        parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6); bottomMargin = dp(6) })
        setOnClickListener { action() }
    }
    private fun card(title: String, body: (LinearLayout) -> Unit) {
        val c = column(); c.background = shape(surface); c.setPadding(dp(18), dp(14), dp(18), dp(16))
        (c.layoutParams as LinearLayout.LayoutParams).apply { topMargin = dp(10); bottomMargin = dp(6) }
        text(title, 21f, c, true).apply { if (Build.VERSION.SDK_INT >= 28) isAccessibilityHeading = true }
        body(c)
    }
    private fun render() {
        @Suppress("DEPRECATION")
        window.statusBarColor = bg
        @Suppress("DEPRECATION")
        window.navigationBarColor = bg
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = if(dark) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        hrLabel = null; hrStatus = null; hrTimestamp = null; deviceList = null; hrControl = null; breathLabel = null; displayedDevices = ""
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        root.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                v.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        setContentView(root)
        val scroll = ScrollView(this).apply { isFillViewport = true }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(18), dp(20), dp(24)) }
        scroll.addView(page)
        text(tx("CARE / SPHERE"), 13f, bold = true)
        data.error?.let { text(it, 16f).setTextColor(Color.rgb(130,55,20)); button(tx("Retry secure save")) { data.save(); render() } }
        when {
            settings -> settingsPage()
            activityIndex == -2 -> researchPage()
            activityIndex >= 0 -> activityPage()
            tab == 0 -> today()
            tab == 1 -> activities()
            tab == 2 -> care()
            else -> health()
        }
        val largeText = resources.configuration.fontScale > 1.3f
        val nav = LinearLayout(this).apply {
            orientation = if(largeText) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }
        root.addView(nav, LinearLayout.LayoutParams(-1, -2))
        var navRow = nav
        (if(data.json.optJSONObject("tools")?.optBoolean("simple") == true) listOf("My Day", "Activities", "My People") else listOf("Today", "Activities", "Care", "Health")).forEachIndexed { i, label ->
            val b = Button(this).apply {
                text = tx(label); textLocale=Locale.forLanguageTag(AppLanguage.current(this@MainActivity)); textSize = 15f; isAllCaps = false; minHeight = dp(56)
                setTextColor(if (i == tab && !settings && activityIndex < 0) Color.WHITE else green)
                background = shape(if (i == tab && !settings && activityIndex < 0) fill else bg, 14)
                isSelected = i == tab && !settings && activityIndex < 0
                setOnClickListener { if(i == 2 && data.json.optJSONObject("tools")?.optBoolean("simple") == true) { openInternal(Intent(this@MainActivity, SupportActivity::class.java)); return@setOnClickListener }; stopComfort(); tab = i; settings = false; activityIndex = -1; render() }
            }
            if(largeText && i % 2 == 0) {
                navRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                nav.addView(navRow, LinearLayout.LayoutParams(-1, -2))
            }
            navRow.addView(b, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(3) })
        }
        root.requestApplyInsets()
    }
    private fun today() {
        val name = data.json.optString("name")
        text(if (name.isBlank()) tx("Your day, at your pace.") else AppLanguage.format(this,"Hello, {0}.",name), 30f, bold = true).apply { if (Build.VERSION.SDK_INT >= 28) isAccessibilityHeading = true }
        text(LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.FULL).withLocale(Locale.forLanguageTag(AppLanguage.current(this)))), 16f)
        button(tx("My support tools")) { openInternal(Intent(this, SupportActivity::class.java)) }
        button(tx("Medication reminders")) { openInternal(Intent(this,MedicationActivity::class.java)) }
        button(tx("Settings & privacy")) { settings = true; render() }
        card(tx("A small moment for you")) { c ->
            copy(tx("Choose an activity, check in with yourself, or simply take a break. Everything here is optional."), parent = c)
            button(tx("Explore activities"), c, true) { tab = 1; render() }
        }
        card(tx("Saved activities")) { c ->
            val favorites = data.favorites()
            val savedIndices = LibraryPreferences.ids.indices.filter { LibraryPreferences.ids[it] in favorites }.take(4)
            if (savedIndices.isEmpty()) {
                copy(tx("Save a favorite in Activities and it will appear here for one-tap access."), parent = c)
                button(tx("Browse activities"), c, true) { tab = 1; render() }
            } else {
                savedIndices.forEach { index ->
                    button(AppLanguage.format(this, "Start {0}", tx(ActivityRules.titles[index])), c, true) { openActivity(index) }
                }
                button(tx("Browse activities"), c) { tab = 1; render() }
            }
        }
        card(tx("My routine today")) { c ->
            val routines = data.list("routines")
            if (routines.length() == 0) text(tx("No routines yet. Add something that matters to you."), parent = c)
            val done = data.list("done")
            for (i in 0 until routines.length()) {
                val item = routines.getJSONObject(i)
                val check = CheckBox(this).apply {
                    text = item.getString("title"); textSize = 18f; minHeight = dp(52); setTextColor(ink)
                    isChecked = (0 until done.length()).any { done.optString(it) == item.getString("id") }
                    isEnabled = data.canWrite
                    setOnCheckedChangeListener { _, checked ->
                        val updated = JSONArray()
                        for (j in 0 until done.length()) if (done.optString(j) != item.getString("id")) updated.put(done.optString(j))
                        if (checked) updated.put(item.getString("id"))
                        data.put("done", updated); render()
                    }
                }; c.addView(check)
            }
            button(tx("Add a routine"), c) {
                if (routines.length() >= 24) { message(tx("You can keep up to 24 routines. Remove one in Settings first.")); return@button }
                input(tx("Add your routine"), tx("A short reminder"), 80) { title ->
                    val updatedRoutines = JSONArray(routines.toString())
                    updatedRoutines.put(JSONObject().put("id", java.util.UUID.randomUUID().toString()).put("title", title))
                    data.put("routines", updatedRoutines); render()
                }
            }.isEnabled = data.canWrite
        }
        if(data.json.optJSONObject("routineUndo")?.optString("day") == LocalDate.now().toString()) button(tx("Undo last routine change")) { data.undoRoutine(); render() }
        card(tx("How are you feeling?")) { c ->
            val current = data.json.optString("mood")
            text(if(current.isBlank()) tx("No check-in today. Choose a word if you want to.") else AppLanguage.format(this,"Today's self-report: {0}",AppLanguage.moodLabel(current,::tx)), parent = c)
            listOf("Low", "Okay", "Good", "Prefer not to say").forEach { mood ->
                button(tx(mood), c) {
                    data.put("mood", mood)
                    data.put("moods", MoodHistory.record(data.list("moods"), java.time.LocalDate.now().toString(), mood, java.time.Instant.now().toString()))
                    render()
                }.isEnabled = data.canWrite
            }
            text(tx("Mood history"), 20f, c)
            val history = MoodHistory.recent(data.list("moods"), 5)
            if (history.isEmpty()) text(tx("No check-ins yet"), parent = c)
            else history.forEach { (day, label) -> text("$day · ${AppLanguage.moodLabel(label, ::tx)}", 15f, c) }
            button(tx("I'm stressed"), c) { openInternal(Intent(this, CheckInActivity::class.java)) }
            copy(tx("A personal check-in, not a health assessment."), 14f, c)
        }
    }
    private fun searchField(label: String, value: String, changed: (String) -> Unit) {
        text(label, 15f, bold = true)
        val field = EditText(this).apply {
            hint = tx(label); contentDescription = tx(label); setText(value); textSize = 18f
            isSingleLine = true; setTextColor(ink); setHintTextColor(muted)
            setPadding(dp(14), dp(10), dp(14), dp(10)); minHeight = dp(52); background = shape(soft, 14)
            filters = arrayOf(android.text.InputFilter.LengthFilter(160))
        }
        page.addView(field, LinearLayout.LayoutParams(-1, -2))
        field.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { changed(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun openActivity(i: Int) {
        activityIndex = i; round = 0; fewerChoices = true; feedback = ""; firstPair = -1; secondPair = -1; matched.clear(); memoryPosition = 0; memoryHidden = false
        gameLevel = data.json.optString("gameLevel", "gentle").takeIf { it in listOf("gentle", "steady", "challenge") } ?: "gentle"
        memoryPairCount = when (gameLevel) { "challenge" -> 6; "steady" -> 4; else -> 2 }
        memoryTheme = "Nature"
        memoryCards = pictureThemes.getValue(memoryTheme).take(memoryPairCount).flatMap { listOf(it, it) }.shuffled()
        if (i == 12) resetBingo(if (gameLevel == "challenge") 4 else if (gameLevel == "steady") 3 else 2)
        if (i == 13) resetSolitaire()
        if (i == 14) { wordTheme = "Nature"; wordSize = when (gameLevel) { "challenge" -> 10; "steady" -> 8; else -> 6 }; resetWordPuzzle() }
        (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(root.windowToken, 0)
        render()
    }
    private fun activities() {
        gameLevel = data.json.optString("gameLevel", "gentle").takeIf { it in listOf("gentle", "steady", "challenge") } ?: "gentle"
        gameFocus = data.json.optString("gameFocus", "all").takeIf { it in listOf("all", "thinking", "familiar", "connection") } ?: "all"
        gamePreferencesSet = data.json.optBoolean("gamePreferencesSet", false)
        text(tx("Make room for a pause."), 30f, bold = true)
        copy(tx("Find something you enjoy. Save a favorite for next time. No scores, streaks, or pressure to finish."))
        card(tx("Activity preferences")) { c ->
            copy(tx("Optional. No diagnosis is requested. These choices only adjust difficulty and activity order; they do not indicate treatment or clinical benefit for autism or dementia."), parent = c)
            button(tx(if (gamePreferencesSet) "Edit preferences" else "Use these preferences"), c) { chooseGamePreferences() }
            text("${tx("Pace")}: ${tx(when(gameLevel) { "challenge" -> "More challenge"; "steady" -> "Steady"; else -> "Gentle" })} · ${tx(when(gameFocus) { "thinking" -> "Thinking and patterns"; "familiar" -> "Familiar everyday themes"; "connection" -> "Connection and expression"; else -> "A little of everything" })}", 14f, c)
        }
        button(tx("Research & how CareSphere works")) { activityIndex = -2; render() }
        lateinit var results: LinearLayout
        searchField("Find an activity", activityQuery) { activityQuery = it; activityResults(results) }
        val only = CheckBox(this).apply {
            text = tx("Favorites only"); textSize = 18f; minHeight = dp(52); setTextColor(ink); isChecked = favoritesOnly
            setOnCheckedChangeListener { _, checked -> favoritesOnly = checked; activityResults(results) }
        }; page.addView(only)
        results = column(); activityResults(results)
        card(tx("Why these activities?")) { c ->
            copy(tx("Predictable steps, personal choice, and reduced sensory pressure draw on accessibility and wellbeing guidance. These individual games are not clinically validated dementia or autism treatments, cognitive stimulation therapy, or diagnostic tests."), parent = c)
            button(tx("Read NICE dementia guidance"), c) { openURL("https://www.nice.org.uk/guidance/ng97/chapter/recommendations") }
            button(tx("Read NICE autistic-adult guidance"), c) { openURL("https://www.nice.org.uk/guidance/cg142/chapter/recommendations") }
        }
    }
    private fun chooseGamePreferences() {
        val levels = arrayOf(tx("Gentle"), tx("Steady"), tx("More challenge"))
        dialogBuilder().setTitle(tx("Pace")).setItems(levels) { _, selected ->
            gameLevel = listOf("gentle", "steady", "challenge")[selected]
            val focuses = arrayOf(tx("A little of everything"), tx("Thinking and patterns"), tx("Familiar everyday themes"), tx("Connection and expression"))
            dialogBuilder().setTitle(tx("Show these first")).setItems(focuses) { _, focus ->
                gameFocus = listOf("all", "thinking", "familiar", "connection")[focus]
                data.put("gameLevel", gameLevel)
                data.put("gameFocus", gameFocus)
                data.put("gamePreferencesSet", true)
                gamePreferencesSet = true
                render()
            }.showProtected()
        }.showProtected()
    }
    private fun researchPage() {
        button(tx("Back to activities")) { activityIndex = -1; render() }
        text(tx("Research & how CareSphere works"), 29f, bold = true)
        text(tx("What two reviews say, what CareSphere borrows, and what it does not claim."))
        card(tx("Serious games for social skills in autistic children and adolescents (2024)")) { c ->
            text(tx("Nine studies reported a positive result in at least one social-skill outcome, but the review found substantial risk of bias, varied interventions, and a small evidence base. The evidence concerns studied programs for children and adolescents, not CareSphere or adults."), parent = c)
            button(tx("Read the open-access systematic review"), c) { openURL("https://doi.org/10.3390/healthcare12050508") }
        }
        card(tx("Digital cognitive interventions for MCI and dementia (2020)")) { c ->
            text(tx("This review described 19 studies and 11 programs for adults over 60 with mild cognitive impairment or dementia. It distinguishes stimulation, training, and individualized rehabilitation; programs and evidence differed, and more clinical research was needed. Standalone app activities are not equivalent to structured therapist-led programs."), parent = c)
            button(tx("Read the open-access systematic review"), c) { openURL("https://doi.org/10.3389/fpsyg.2020.00648") }
        }
        card(tx("How CareSphere applies this")) { c ->
            text(tx("Social moment cards are optional everyday scenarios with several reasonable responses. Choose, discuss another option, or skip. There is no right-answer score; no response is saved or sent to a server."), parent = c)
            text(tx("Familiar steps, optional hints, fewer choices, and clear stop controls support choice and predictability. Activities run offline. Favorites stay on this device."), parent = c)
            text(tx("Choice reduction is user-controlled for comfort; the app does not automatically adjust difficulty from answers."), parent = c)
            text(tx("The activity does not ask for eye contact or expression imitation. Speaking, pointing, reading, or skipping are equally welcome."), parent = c)
        }
        card(tx("How the app works")) { c ->
            text(tx("CareSphere activities run on this device. Choose an activity, go at your own pace, and leave whenever you want. Activity answers and scores are not collected. Other features request permissions separately only when you choose them."), parent = c)
        }
        card(tx("Important limits")) { c ->
            text(tx("Evidence boundary: CareSphere activities have not been clinically tested and do not diagnose, prevent, slow, or treat dementia or autism. Use them for optional enjoyment or conversation, not therapy or assessment."), parent = c)
        }
        card(tx("Sources you shared")) { c ->
            text(tx("The Google Scholar Labs links require sign-in and did not expose their result lists. They are links only; their contents have not been summarized or treated as verified evidence."), parent = c)
            button(tx("Open Google Scholar Labs search 1"), c) { openURL("https://scholar.google.com/scholar_labs/search/session/8600305665779058312?hl=en") }
            button(tx("Open Google Scholar Labs search 2"), c) { openURL("https://scholar.google.com/scholar_labs/search/session/6581218369437547131?hl=en") }
        }
        button(tx("Back to activities")) { activityIndex = -1; render() }
    }
    private fun activityResults(parent: LinearLayout) {
        parent.removeAllViews()
        val favorites = data.favorites()
        val filtered = ActivityRules.titles.indices.filter {
            (!favoritesOnly || LibraryPreferences.ids[it] in favorites) && LibraryPreferences.matches(ActivityRules.titles[it] + " " + LibraryPreferences.descriptions[it] + " " + tx(ActivityRules.titles[it]) + " " + tx(LibraryPreferences.descriptions[it]), activityQuery)
        }
        val focusFirst = when (gameFocus) {
            "thinking" -> setOf(0, 1, 2, 5, 8, 13, 14)
            "familiar" -> setOf(3, 7, 4, 5)
            "connection" -> setOf(4, 6, 11, 12)
            else -> emptySet()
        }
        val visible = filtered.sortedWith(compareBy<Int> { if (it in focusFirst) 0 else 1 }.thenBy { it })
        text("${visible.size} ${if(visible.size == 1) "activity" else "activities"} · available offline", 15f, parent)
        if(visible.isEmpty()) {
            text(tx("No matching activities. Try a different word, or show everything."), parent = parent)
            button(tx("Show all activities"), parent) { activityQuery = ""; favoritesOnly = false; render() }
        }
        for (i in visible) {
            val c = column(parent); c.background = shape(surface); c.setPadding(dp(16), dp(12), dp(16), dp(14))
            (c.layoutParams as LinearLayout.LayoutParams).bottomMargin = dp(12)
            button(tx(ActivityRules.titles[i]), c, true) { openActivity(i) }
            text(tx(LibraryPreferences.descriptions[i]), 16f, c)
            val saved = LibraryPreferences.ids[i] in favorites
            button(tx(if(saved) "Remove from favorites" else "Save to favorites"), c) {
                data.toggleFavorite(LibraryPreferences.ids[i]); activityResults(parent)
                parent.announceForAccessibility(if(saved) "Removed from favorites" else "Saved to favorites")
            }.apply { contentDescription = "${if(saved) "Remove" else "Save"} ${ActivityRules.titles[i]} ${if(saved) "from" else "to"} favorites"; isEnabled = data.canWrite }
        }
    }
    private fun activityPage() {
        button(tx("Back to activities")) { activityIndex = -1; stopComfort(); render() }
        text(tx(ActivityRules.titles[activityIndex]), 29f, bold = true)
        text(tx("Take your time. You can skip, ask for a hint, or stop."), 16f)
        when(activityIndex) {
            in 0..3 -> {
                val r = ActivityRules.round(activityIndex, round)
                card(tx(r.prompt)) { c ->
                    val comfort = CheckBox(this).apply {
                        text = tx("Fewer choices"); textLocale = Locale.forLanguageTag(AppLanguage.current(this@MainActivity))
                        textSize = 17f; minHeight = dp(48); setTextColor(ink); isChecked = fewerChoices
                        setOnCheckedChangeListener { _, checked -> fewerChoices = checked; render() }
                    }
                    c.addView(comfort)
                    text(tx("You can change this anytime. Hints and skipping are always okay."), 14f, c)
                    r.optionIndices(fewerChoices).forEach { i ->
                        val option = r.options[i]
                        button(tx(option), c) {
                        feedback = if(i == r.answer) "That fits. Continue when you're ready." else "Let's look together. You can try another choice or use a hint."
                        render()
                    } }
                    button(tx("Show a hint"), c) { feedback = r.hint; render() }
                }
            }
            4 -> card(tx("A conversation, not a test")) { c ->
                val prompts = listOf("What music do you enjoy?", "Would you like to describe a place where you feel comfortable?", "Is there a food, texture, or scent you enjoy?", "What would make today more comfortable?")
                text(tx(prompts[round % prompts.size]), 24f, c, true)
                copy(tx("Speak, point, write elsewhere, listen, or skip. There is no right answer and nothing is recorded."), parent = c)
            }
            5 -> pairs()
            6 -> card(tx("Name a feeling, if you want")) { c ->
                copy(tx("People express feelings differently. These words are choices, not a judgment about someone's face or behavior."), parent = c)
                listOf("Comfortable", "Excited", "Tired", "Overwhelmed", "Not sure", "Something else").forEach { feeling ->
                    button(tx(feeling), c) { feedback = "$feeling. Would a quiet moment, a change of activity, or company feel helpful? This choice is not saved."; render() }
                }
            }
            7 -> card(tx("Tell the story your way")) { c ->
                val scenes = listOf("☀  A sunny window", "🌱  A small plant", "☕  A cup on the table")
                text(tx(scenes[round % scenes.size]), 25f, c, true)
                copy(tx("Imagine this scene. What might happen next? You can describe, point, or simply look. This is a fictional prompt, not a memory test."), parent = c)
            }
            8 -> card(tx("Remember a short pattern")) { c ->
                if (!memoryHidden) {
                    text(memory.joinToString(" → "), 25f, c, true)
                    text(tx("Look for as long as you like. You can reveal the pattern again at any time."), parent = c)
                    button(tx("I'm ready to try"), c, true) { memoryHidden = true; memoryPosition = 0; feedback = ""; render() }
                } else if (memoryPosition < memory.size) {
                    text("Choose item ${memoryPosition + 1} of ${memory.size}", parent = c)
                    memory.forEach { word -> button(word, c) {
                        if (word == memory[memoryPosition]) { memoryPosition++; feedback = if(memoryPosition == memory.size) "Pattern complete. Take a break or try again." else "Continue when you're ready." }
                        else feedback = "Try another choice, or reveal the pattern."
                        render()
                    } }
                    button(tx("Reveal the pattern"), c) { memoryHidden = false; render() }
                } else text(tx("You reached the end of this pattern."), parent = c)
            }
            9 -> card(tx("A gentle tone")) { c ->
                copy(tx("Optional synthesized sound. Start with a comfortable device volume. It stops when you leave the app. Silence is always a good choice."), parent = c)
                button(if(playing) "Stop sound" else "Play gentle tone", c, true) {
                    if (playing) { sound.stop(); playing = false } else { playing = sound.play(); if(!playing) feedback = "Audio couldn't start. Check your sound output and try again." }
                    render()
                }
            }
            10 -> card(tx("Breathe comfortably")) { c ->
                copy(tx("Optional pace: in for 4 seconds, out for 6. No breath hold. Follow your own comfortable rhythm instead if you prefer. Stop if uncomfortable or dizzy."), parent = c)
                breathLabel = text(if(breathing) phase() else "Ready when you are", 30f, c, true)
                button(if(breathing) "Pause breathing guide" else "Start breathing guide", c, true) {
                    breathing = !breathing
                    handler.removeCallbacks(breathTick)
                    if(breathing) { breathSecond = 0; handler.postDelayed(breathTick, 1000) }
                    render()
                }
            }
            11 -> card(tx("Several answers may fit. Choose, reflect, or skip. Nothing is recorded.")) { c ->
                val moments = listOf(
                    "A room feels noisy. What might feel helpful?" to listOf("Move somewhere quieter", "Ask for a quieter moment", "Take a break outside"),
                    "Someone says hello. What feels comfortable for you?" to listOf("Say hello", "Wave or smile", "Take time before responding"),
                    "A plan changes. What could help right now?" to listOf("Ask what happens next", "Look at the new plan", "Pause before deciding")
                )
                val moment = moments[Math.floorMod(round, moments.size)]
                text(tx(moment.first), 24f, c, true)
                moment.second.forEach { option -> button(tx(option), c) {
                    feedback = "That is one option. Your own preference matters; you can also choose differently or skip."
                    render()
                } }
                text(tx("No answer is scored as correct. Stop whenever you like."), parent = c)
            }
            12 -> bingo()
            13 -> solitaireGame()
            14 -> wordSearchGame()
        }
        if (feedback.isNotBlank()) text(tx(feedback), 19f, bold = true).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        if (activityIndex < 9 || activityIndex == 11) button(tx("Next / start again")) {
            round++; feedback = ""; firstPair = -1; secondPair = -1; matched.clear(); memoryHidden = false; memoryPosition = 0
            if (activityIndex == 5) resetMemoryPairs()
            render()
        }
        button(tx("Take a break")) { activityIndex = -1; stopComfort(); render() }
    }
    private fun bingo() = card(tx("Bingo together")) { c ->
        text(tx("Pass the device between players. No account, internet, or server."), parent = c)
        button(tx("Number of players") + ": $bingoPlayerCount", c) {
            dialogBuilder().setTitle(tx("Number of players")).setItems((2..6).map(Int::toString).toTypedArray()) { _, which -> resetBingo(which + 2); render() }.showProtected()
        }
        button(tx("Call next number"), c, true) {
            if (bingoCalls.isNotEmpty() && bingoWinner() == null) { bingoCalled += bingoCalls.removeAt(0); feedback = tx("Called {0}. Mark this card if it matches.").let { AppLanguage.format(this, "Called {0}. Mark this card if it matches.", bingoLabel(bingoCalled.last())) }; render() }
        }.isEnabled = bingoCalls.isNotEmpty() && bingoWinner() == null
        text(if (bingoCalled.isEmpty()) tx("Ready when you are") else bingoLabel(bingoCalled.last()), 27f, c, true).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        bingoCards.indices.forEach { player ->
            button(AppLanguage.format(this, "Player {0}", player + 1) + if (player == bingoActivePlayer) " · ✓" else "", c) { bingoActivePlayer = player; render() }
        }
        val card = bingoCards[bingoActivePlayer]
        for (row in 0 until 5) {
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            c.addView(line, LinearLayout.LayoutParams(-1, -2))
            for (column in 0 until 5) {
                val value = card[row * 5 + column]
                val free = value == 0
                val marked = free || value in bingoMarks[bingoActivePlayer]
                val cell = Button(this).apply {
                    text = if (free) tx("Free") else value.toString()
                    textSize = 14f; isAllCaps = false; minHeight = dp(48); setTextColor(if(marked) Color.WHITE else ink)
                    background = shape(if(marked) fill else soft, 8)
                    contentDescription = if (free) tx("Free space") else bingoLabel(value)
                    isEnabled = !free && value in bingoCalled && bingoWinner() == null
                    setOnClickListener { if (value in bingoMarks[bingoActivePlayer]) bingoMarks[bingoActivePlayer].remove(value) else bingoMarks[bingoActivePlayer].add(value); render() }
                }
                line.addView(cell, LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginEnd = dp(3) })
            }
        }
        bingoWinner()?.let { text(AppLanguage.format(this, "Bingo! Player {0} has a line.", it + 1), 20f, c, true).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE } }
        text(bingoCalled.takeLast(12).asReversed().joinToString(" · ", transform = ::bingoLabel), 14f, c)
        button(tx("New game"), c) { resetBingo(bingoPlayerCount); render() }
    }
    private fun bingoLabel(number:Int):String = "${listOf("B", "I", "N", "G", "O")[(number - 1) / 15]} $number"
    private fun bingoWinner():Int? = bingoCards.indices.firstOrNull { LocalGameRules.hasBingo(bingoCards[it], bingoMarks[it]) }
    private fun resetBingo(count:Int) {
        bingoPlayerCount = count.coerceIn(2, 6)
        bingoCards = LocalGameRules.bingoCards(bingoPlayerCount)
        bingoCalls = LocalGameRules.bingoCalls().toMutableList()
        bingoCalled.clear(); bingoMarks.clear(); repeat(bingoPlayerCount) { bingoMarks.add(mutableSetOf()) }
        bingoActivePlayer = 0; feedback = ""
    }
    private fun solitaireGame() = card(tx("Solitaire")) { c ->
        text(tx("No timer"), parent = c)
        button(if (solitaireDrawCount == 1) tx("Draw one") else tx("Draw three"), c) { solitaireDrawCount = if(solitaireDrawCount == 1) 3 else 1; render() }
        button(tx("Undo"), c) { solitaireUndo(); render() }.isEnabled = solitaireHistory.isNotEmpty()
        button(tx("New game"), c) { resetSolitaire(); render() }
        button(if(solitaire.stock.isEmpty()) tx("Return waste to stock") else AppLanguage.format(this, "Stock: {0} cards. Draw cards", solitaire.stock.size), c, true) { solitaireDraw(); render() }
        button(tx("Waste pile") + ": " + (solitaire.waste.lastOrNull()?.let(::solitaireCardLabel) ?: tx("Waste")), c) { solitaireSelection = if(solitaireSelection?.first == true) null else if(solitaire.waste.isEmpty()) null else true to (0 to 0); render() }
        solitaire.foundations.forEachIndexed { index, pile ->
            val suit = listOf("clubs", "diamonds", "hearts", "spades")[index]
            val symbol = listOf("♣", "♦", "♥", "♠")[index]
            button(AppLanguage.format(this, "Foundation {0}", tx(suit)) + ": " + (pile.lastOrNull()?.let(::solitaireCardLabel) ?: symbol), c) { moveSolitaireFoundation(index); render() }
        }
        val tableauRow = HorizontalScrollView(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tableauRow.addView(row)
        c.addView(tableauRow, LinearLayout.LayoutParams(-1, -2))
        solitaire.tableau.forEachIndexed { pileIndex, pile ->
            val pileView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(3), 0, dp(3), 0) }
            row.addView(pileView, LinearLayout.LayoutParams(dp(88), -2))
            if (pile.isEmpty()) {
                val empty = Button(this).apply { text = "K"; minHeight = dp(50); contentDescription = AppLanguage.format(this@MainActivity, "Empty pile {0}", pileIndex + 1); setOnClickListener { moveSolitaireTableau(pileIndex); render() } }
                pileView.addView(empty, LinearLayout.LayoutParams(-1, -2))
            }
            pile.forEachIndexed { cardIndex, card ->
                val label = if(card.faceUp) solitaireCardLabel(card) else AppLanguage.format(this, "Pile {0}, face-down card", pileIndex + 1)
                val cardButton = Button(this).apply {
                    text = if(card.faceUp) card.name else "✦"
                    textSize = 14f; isAllCaps = false; minHeight = dp(48); setTextColor(if(card.faceUp && card.red) Color.rgb(180,55,55) else ink)
                    background = shape(if(card.faceUp) surface else soft, 8); contentDescription = label
                    setOnClickListener { selectSolitaireTableau(pileIndex, cardIndex); render() }
                    isEnabled = card.faceUp || cardIndex == pile.lastIndex
                }
                pileView.addView(cardButton, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
            }
        }
        text(tx("Select a card, then choose a legal pile. The exposed card turns over automatically."), 14f, c)
    }
    private fun solitaireCardLabel(card:LocalGameRules.Card):String = AppLanguage.format(this, "{0} of {1}", tx(when(card.rank) { 1 -> "Ace"; 11 -> "Jack"; 12 -> "Queen"; 13 -> "King"; else -> card.rankLabel }), tx(card.suit))
    private fun resetSolitaire() { solitaire = LocalGameRules.newSolitaire(); solitaireSelection = null; solitaireHistory.clear() }
    private fun solitaireCommit(next:LocalGameRules.SolitaireState) { solitaireHistory.add(solitaire); solitaire = next; solitaireSelection = null }
    private fun solitaireUndo() { if(solitaireHistory.isNotEmpty()) { solitaire = solitaireHistory.removeAt(solitaireHistory.lastIndex); solitaireSelection = null } }
    private fun solitaireDraw() {
        if(solitaire.stock.isNotEmpty()) {
            val count = minOf(solitaireDrawCount, solitaire.stock.size)
            val draw = solitaire.stock.takeLast(count).asReversed().map { it.flipped(true) }
            solitaireCommit(solitaire.copy(stock = solitaire.stock.dropLast(count), waste = solitaire.waste + draw))
        } else if(solitaire.waste.isNotEmpty()) solitaireCommit(solitaire.copy(stock = solitaire.waste.asReversed().map { it.flipped(false) }, waste = emptyList()))
    }
    private fun selectSolitaireTableau(pile:Int, index:Int) {
        val card = solitaire.tableau[pile][index]
        if(!card.faceUp) {
            val piles = solitaire.tableau.map { it.toMutableList() }
            piles[pile][index] = card.flipped(true)
            solitaireCommit(solitaire.copy(tableau = piles))
            return
        }
        val selected = solitaireSelection
        if(selected != null && !(selected.first == false && selected.second.first == pile) && moveSolitaireTableau(pile)) return
        solitaireSelection = if(selected == (false to (pile to index))) null else false to (pile to index)
    }
    private fun selectedSolitaireCards():List<LocalGameRules.Card> {
        val selected = solitaireSelection ?: return emptyList()
        return if(selected.first) solitaire.waste.takeLast(1) else solitaire.tableau[selected.second.first].drop(selected.second.second)
    }
    private fun moveSolitaireTableau(target:Int):Boolean {
        val selected = solitaireSelection ?: return false
        if(!selected.first && selected.second.first == target) return false
        val moving = selectedSolitaireCards(); val first = moving.firstOrNull() ?: return false
        val targetTop = solitaire.tableau[target].lastOrNull()
        if(!LocalGameRules.canPlaceTableau(first, targetTop)) return false
        val piles = solitaire.tableau.map { it.toMutableList() }
        val waste = if(selected.first) solitaire.waste.dropLast(1) else solitaire.waste
        if(!selected.first) {
            val sourcePile = piles[selected.second.first]
            repeat(sourcePile.size - selected.second.second) { sourcePile.removeAt(sourcePile.lastIndex) }
            if(sourcePile.isNotEmpty() && !sourcePile.last().faceUp) sourcePile[sourcePile.lastIndex] = sourcePile.last().flipped(true)
        }
        piles[target].addAll(moving)
        solitaireCommit(solitaire.copy(waste = waste, tableau = piles))
        return true
    }
    private fun moveSolitaireFoundation(target:Int) {
        val selected = solitaireSelection ?: return
        val moving = selectedSolitaireCards()
        if(moving.size != 1 || !LocalGameRules.canPlaceFoundation(moving[0], solitaire.foundations[target])) return
        val piles = solitaire.tableau.map { it.toMutableList() }
        val waste = if(selected.first) solitaire.waste.dropLast(1) else solitaire.waste
        if(!selected.first) {
            val sourcePile = piles[selected.second.first]
            sourcePile.removeAt(selected.second.second)
            if(sourcePile.isNotEmpty() && !sourcePile.last().faceUp) sourcePile[sourcePile.lastIndex] = sourcePile.last().flipped(true)
        }
        val foundations = solitaire.foundations.map { it.toMutableList() }
        foundations[target].add(moving[0])
        solitaireCommit(solitaire.copy(waste = waste, tableau = piles, foundations = foundations))
    }
    private fun gameWords():List<String> = when {
        wordTheme == "Nature" && AppLanguage.current(this) == "es" -> listOf("FLOR", "HOJA", "ARBOL", "AVE", "NUBE")
        wordTheme == "Nature" && AppLanguage.current(this) == "zh-Hans" -> listOf("花朵", "树叶", "树木", "小鸟", "云朵")
        wordTheme == "Nature" && AppLanguage.current(this) == "fil" -> listOf("BULAKLAK", "DAHON", "PUNO", "IBON", "ULAP")
        wordTheme == "Nature" && AppLanguage.current(this) == "vi" -> listOf("HOA", "LÁ", "CÂY", "CHIM", "MÂY")
        wordTheme == "Home" && AppLanguage.current(this) == "es" -> listOf("TAZA", "LIBRO", "LUZ", "SILLA", "PUERTA")
        wordTheme == "Home" && AppLanguage.current(this) == "zh-Hans" -> listOf("杯子", "书本", "台灯", "椅子", "房门")
        wordTheme == "Home" && AppLanguage.current(this) == "fil" -> listOf("BASO", "AKLAT", "ILAW", "SILYA", "PINTO")
        wordTheme == "Home" && AppLanguage.current(this) == "vi" -> listOf("CỐC", "SÁCH", "ĐÈN", "GHẾ", "CỬA")
        wordTheme == "Food" && AppLanguage.current(this) == "es" -> listOf("MANZANA", "PAN", "PERA", "SOPA", "ARROZ")
        wordTheme == "Food" && AppLanguage.current(this) == "zh-Hans" -> listOf("苹果", "面包", "梨子", "米饭", "汤")
        wordTheme == "Food" && AppLanguage.current(this) == "fil" -> listOf("MANSANAS", "TINAPAY", "PERAS", "SABAW", "BIGAS")
        wordTheme == "Food" && AppLanguage.current(this) == "vi" -> listOf("TÁO", "BÁNH MÌ", "LÊ", "SÚP", "CƠM")
        wordTheme == "Home" -> listOf("CUP", "BOOK", "LAMP", "CHAIR", "DOOR")
        wordTheme == "Food" -> listOf("APPLE", "BREAD", "PEAR", "SOUP", "RICE")
        else -> listOf("FLOWER", "LEAF", "TREE", "BIRD", "CLOUD")
    }
    private fun resetWordPuzzle() { wordPuzzle = LocalGameRules.wordPuzzle(gameWords(), wordSize); wordStart = null; foundWords.clear(); foundCells.clear(); feedback = "Choose the first and last letter of a word in the same row or column." }
    private fun wordSearchGame() = card(tx("Word search")) { c ->
        copy(tx("Find each word in a straight line across or down. Tap its first and last letter; reverse direction is fine."), parent = c)
        button(tx("Word theme") + ": " + tx(wordTheme), c) { dialogBuilder().setTitle(tx("Word theme")).setItems(arrayOf(tx("Nature"), tx("Home"), tx("Food"))) { _, which -> wordTheme = listOf("Nature", "Home", "Food")[which]; resetWordPuzzle(); render() }.showProtected() }
        button(AppLanguage.format(this, "{0} by {0}", wordSize), c) { dialogBuilder().setTitle(tx("Grid size")).setItems(arrayOf("6 × 6", "8 × 8", "10 × 10", "12 × 12")) { _, which -> wordSize = listOf(6, 8, 10, 12)[which]; resetWordPuzzle(); render() }.showProtected() }
        for (row in 0 until wordSize) {
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            c.addView(line, LinearLayout.LayoutParams(-1, -2))
            for (column in 0 until wordSize) {
                val key = row to column
                val cell = Button(this).apply {
                    text = wordPuzzle.grid[row][column]; textSize = 11f; isAllCaps = false; minHeight = dp(38); setPadding(0, 0, 0, 0)
                    val selected = key == wordStart
                    val found = key in foundCells
                    background = shape(if(found) fill else if(selected) Color.rgb(186,151,56) else soft, 5)
                    setTextColor(if(found) Color.WHITE else ink)
                    contentDescription = "${tx("Row")} ${row + 1}, ${tx("column")} ${column + 1}, ${wordPuzzle.grid[row][column]}"
                    setOnClickListener { chooseWordCell(row, column); render() }
                }
                line.addView(cell, LinearLayout.LayoutParams(0, dp(42), 1f).apply { marginEnd = dp(2) })
            }
        }
        text(tx("Words to find"), 19f, c, true)
        wordPuzzle.words.forEach { word -> text((if(word in foundWords) "✓ " else "• ") + word, 16f, c) }
        text(tx("Find each word in a straight line across or down. Tap its first and last letter; reverse direction is fine."), 14f, c)
        button(tx("New puzzle"), c) { resetWordPuzzle(); render() }
    }
    private fun chooseWordCell(row:Int, column:Int) {
        val start = wordStart
        if(start == null) { wordStart = row to column; feedback = "Choose the last letter of that word."; return }
        val found = LocalGameRules.selectedWord(wordPuzzle, start.first, start.second, row, column)
        if(found == null) {
            wordStart = row to column
            feedback = if(start.first != row && start.second != column) "Choose cells in the same row or column. Try again from this letter." else "No listed word there. Choose another first letter."
            return
        }
        wordStart = null
        if(found in foundWords) { feedback = "That word is already found. Choose another one."; return }
        foundWords.add(found)
        val rowStep = when { row > start.first -> 1; row < start.first -> -1; else -> 0 }
        val columnStep = when { column > start.second -> 1; column < start.second -> -1; else -> 0 }
        val length = maxOf(kotlin.math.abs(row - start.first), kotlin.math.abs(column - start.second)) + 1
        repeat(length) { offset -> foundCells.add((start.first + rowStep * offset) to (start.second + columnStep * offset)) }
        feedback = AppLanguage.format(this, "Found: {0}", found)
        if(foundWords.size == wordPuzzle.words.size) feedback = "All words found. You can start a new puzzle whenever you like."
    }
    private fun pairs() = card(tx("Picture pairs")) { c ->
        copy(tx("Optional picture matching for enjoyment. This is not a memory test or a treatment. Unmatched cards stay visible until you choose to hide them."), parent = c)
        button(tx("Picture theme") + ": " + tx(memoryTheme), c) {
            dialogBuilder().setTitle(tx("Picture theme")).setItems(arrayOf(tx("Nature"), tx("Home"), tx("Food"))) { _, which -> memoryTheme = listOf("Nature", "Home", "Food")[which]; resetMemoryPairs(); render() }.showProtected()
        }
        button(AppLanguage.format(this, "{0} pairs", memoryPairCount), c) {
            val options = arrayOf(2, 4, 6, 8)
            dialogBuilder().setTitle(tx("Number of pairs")).setItems(options.map { "$it" }.toTypedArray()) { _, which -> memoryPairCount = options[which]; resetMemoryPairs(); render() }.showProtected()
        }
        button(tx(if(pairsRevealAll) "Hide all cards" else "Keep all pictures visible"), c) { pairsRevealAll = !pairsRevealAll; render() }
        memoryCards.forEachIndexed { i, word ->
            val isMatched = i in matched
            val revealed = pairsRevealAll || i == firstPair || i == secondPair || isMatched
            val display = if (revealed) "${pictureEmoji(word)} ${tx(word)}${if(isMatched) " · ${tx("pair found")}" else ""}" else "▢ ${AppLanguage.format(this, "Card {0}", i + 1)}"
            button(display, c) {
                if (isMatched || i == firstPair || secondPair != -1) return@button
                if (firstPair == -1) firstPair = i else {
                    secondPair = i
                    if (memoryCards[firstPair] == word) {
                        matched.add(firstPair); matched.add(i); firstPair = -1; secondPair = -1
                        feedback = if (matched.size == memoryCards.size) "All pairs are found. Start over or choose another theme." else "A pair matches. Continue whenever you like."
                    } else feedback = "These pictures are different. They will stay open until you choose to hide them."
                }
                render()
            }.isEnabled = !isMatched && secondPair == -1 && !pairsRevealAll
        }
        if (secondPair >= 0) button(tx("Hide cards"), c) { firstPair = -1; secondPair = -1; feedback = "Cards hidden. Choose two whenever you are ready."; render() }
        button(tx("Start over"), c) { resetMemoryPairs(); render() }
    }
    private fun resetMemoryPairs() { memoryCards = pictureThemes.getValue(memoryTheme).take(memoryPairCount).flatMap { listOf(it, it) }.shuffled(); firstPair = -1; secondPair = -1; matched.clear(); pairsRevealAll = false; feedback = "Choose two cards. You can take your time." }
    private fun pictureEmoji(word:String):String = when(word) {
        "Leaf" -> "🍃"; "Flower" -> "🌼"; "Bird" -> "🐦"; "Sun" -> "☀️"; "Cloud" -> "☁️"; "River" -> "🌊"; "Acorn" -> "🌰"; "Mushroom" -> "🍄"
        "Cup" -> "☕"; "Book" -> "📘"; "Lamp" -> "💡"; "Chair" -> "🪑"; "Key" -> "🔑"; "Clock" -> "🕰️"; "Plate" -> "🍽️"; "House" -> "🏠"
        "Apple" -> "🍎"; "Bread" -> "🍞"; "Pear" -> "🍐"; "Soup" -> "🥣"; "Rice" -> "🍚"; "Banana" -> "🍌"; "Carrot" -> "🥕"; "Cheese" -> "🧀"; else -> "✧"
    }
    private fun phase() = if (breathSecond % 10 < 4) "Breathe in gently" else "Breathe out gently"
    private val breathTick = object : Runnable {
        override fun run() {
            if(!breathing) return
            breathSecond++
            val next = phase()
            if (breathLabel?.text != next) { breathLabel?.text = next; breathLabel?.announceForAccessibility(next) }
            handler.postDelayed(this, 1000)
        }
    }
    private fun care() {
        text(tx("Keep what matters."), 30f, bold = true)
        copy(tx("Private notes on this device. Search, edit, or choose exactly what to share. No family sync or clinician portal is connected."))
        button(tx("Write a care note"), primary = true) {
            if (data.list("notes").length() >= 100) { message(tx("You have 100 notes. Remove a note before adding another.")); return@button }
            input(tx("New care note"), tx("What would you like to remember?"), 4000) { note ->
                val notes = data.list("notes")
                notes.put(JSONObject().put("id", java.util.UUID.randomUUID().toString()).put("body", note).put("at", Instant.now().toString()))
                data.put("notes", notes); noteQuery = ""; render()
            }
        }.isEnabled = data.canWrite
        lateinit var results: LinearLayout
        searchField("Search care notes", noteQuery) { noteQuery = it; noteResults(results) }
        results = column(); noteResults(results)
        card(tx("Connect with someone you trust")) { c ->
            copy(tx("Create a room link, share it yourself, and open the real video provider. Nobody is invited automatically. The provider may require sign-in and handles its own privacy and permissions."), parent = c)
            button(tx("Create a video invitation"), c) {
                val url = "https://meet.jit.si/CareSphere-${java.util.UUID.randomUUID()}"
                dialogBuilder().setTitle(tx("Your room invitation")).setMessage(url)
                    .setPositiveButton(tx("Open provider")) { _, _ -> openURL(url) }
                    .setNeutralButton(tx("Share link")) { _, _ -> share(url) }.setNegativeButton(tx("Cancel"), null).showProtected()
            }
        }
    }
    private fun noteResults(parent: LinearLayout) {
        parent.removeAllViews()
        val notes = data.list("notes")
        val visible = (notes.length()-1 downTo 0).filter { LibraryPreferences.matches(notes.getJSONObject(it).optString("body"), noteQuery) }
        if(notes.length() == 0) { text(tx("No notes yet"), 22f, parent, true); copy(tx("Only notes you write will appear here."), parent = parent) }
        else if(visible.isEmpty()) { text(tx("No notes match your search."), parent = parent); button(tx("Clear search"), parent) { noteQuery = ""; render() } }
        else text("${visible.size} ${if(visible.size == 1) "note" else "notes"}", 15f, parent)
        for(i in visible) {
            val note = notes.getJSONObject(i)
            val c = column(parent); c.background = shape(surface); c.setPadding(dp(16), dp(14), dp(16), dp(14))
            (c.layoutParams as LinearLayout.LayoutParams).topMargin = dp(12)
            text(note.getString("body"), 18f, c)
            val date = runCatching { Instant.parse(note.getString("at")).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM,java.time.format.FormatStyle.SHORT).withLocale(Locale.forLanguageTag(AppLanguage.current(this)))) }.getOrDefault("Date not recorded")
            text("Created $date${if(note.has("editedAt")) " · edited" else ""}", 14f, c)
            button(tx("Edit note"), c) { input(tx("Edit care note"), tx("Your note"), 4000, initial = note.getString("body")) { body ->
                note.put("body", body).put("editedAt", Instant.now().toString()); data.put("notes", notes); render()
            } }.isEnabled = data.canWrite
            button(tx("Review & share this note"), c) { confirm(tx("Share this note?"), "Review the recipient before sending.\n\n${note.getString("body")}") { share(note.getString("body")) } }
            button(tx("Delete note"), c) { confirm(tx("Delete this note?"), "Move this note to Recently deleted in My support tools for 30 days? Previously shared copies cannot be removed.") { val trash = data.list("trash"); trash.put(JSONObject(note.toString()).put("deletedAt", System.currentTimeMillis())); while(trash.length() > 100) trash.remove(0); data.put("trash", trash); notes.remove(i); data.put("notes", notes); render() } }.isEnabled = data.canWrite
        }
    }
    private fun health() {
        text(tx("Real readings only."), 30f, bold = true)
        copy(tx("Connect a monitor that broadcasts the standard Bluetooth Heart Rate service. A phone camera is not used to estimate your pulse."))
        card(tx("Heart rate")) { c ->
            hrLabel = text(monitor.bpm?.let { "$it bpm" } ?: "— bpm", 40f, c, true)
            c.addView(SparklineView(this, !dark).apply { show(monitor.samples) }, LinearLayout.LayoutParams(-1, dp(44)))
            if (monitor.samples.size < 2) text(tx("Awaiting real readings…"), 14f, c)
            hrStatus = text(monitor.status, parent = c)
            hrTimestamp = text("", 14f, c)
            hrControl = button(tx("Pair a heart-rate monitor"), c, true) {
                if (monitor.busy || monitor.connected) monitor.disconnect()
                else if (!monitor.permitted()) requestPermissions(monitor.permissions, 18)
                else monitor.scan()
            }
            deviceList = column(c)
            button(tx("Bluetooth settings"), c) {
                runCatching { startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)) }
                    .onFailure { message(tx("Open Bluetooth in your device Settings.")) }
            }
            button(tx("App permissions"), c) {
                runCatching { startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) }
                    .onFailure { message(tx("Open CareSphere's permissions in Android Settings.")) }
            }
            text(tx("Readings expire after 15 seconds without an update. Monitoring stops when the app leaves the foreground. This is not a medical device or an emergency monitor."), 15f, c)
        }
        card(tx("Other health data")) { c ->
            button(tx("Health history"),c){openInternal(Intent(this,HealthHistoryActivity::class.java))}
            button(tx("Health references"),c){openInternal(Intent(this,ReferencesActivity::class.java))}
            button(tx("Emergency SOS"),c){openInternal(Intent(this,SosActivity::class.java))}
            copy(tx("Read permitted Health Connect history or enter your own blood pressure and blood oxygen records. No smartwatch-account sync or generated readings."),parent=c)
            text(tx("For symptoms or health concerns, contact a qualified clinician. In an emergency, use your phone to contact local emergency services."), parent = c)
        }
        updateHealth()
    }
    private fun updateHealth() {
        if (!::monitor.isInitialized) return
        hrLabel?.text = monitor.bpm?.let { "$it bpm" } ?: "— bpm"
        hrStatus?.text = monitor.status
        hrTimestamp?.text = monitor.measuredAt?.let { "Received ${Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm:ss a"))} · Bluetooth monitor" } ?: "No current measurement"
        hrControl?.text = if(monitor.busy) "Cancel pairing" else if(monitor.connected) "Disconnect monitor" else "Pair a heart-rate monitor"
        val signature = monitor.devices.keys.joinToString() + monitor.busy + monitor.connected
        if (displayedDevices != signature) {
            displayedDevices = signature
            deviceList?.let { c ->
                c.removeAllViews()
                if (!monitor.connected && (!monitor.busy || monitor.scanning)) monitor.devices.values.forEach { device ->
                    val name = try { device.name ?: "Heart-rate monitor" } catch (_: SecurityException) { "Heart-rate monitor" }
                    button("$name · ${device.address.takeLast(5)}", c) { monitor.connect(device) }
                }
            }
        }
    }
    override fun onRequestPermissionsResult(code: Int, permissions: Array<String>, results: IntArray) {
        if(holdUntilUnlocked{onRequestPermissionsResult(code,permissions,results)})return
        super.onRequestPermissionsResult(code, permissions, results)
        if(code == 18) {
            if (results.isNotEmpty() && monitor.permitted()) monitor.scan()
            else message(tx("Permission was not granted. No reading is shown. You can allow Nearby devices (or Location on older Android) in Android Settings when ready."))
        }
    }
    private fun settingsPage() {
        card(tx("App language")) { c ->
            button("🌐 "+tx("App language"),c) {
                val options=AppLanguage.options
                dialogBuilder().setTitle(tx("App language")).setSingleChoiceItems(options.map{if(it.first=="auto")tx(it.second) else it.second}.toTypedArray(),options.indexOfFirst{it.first==AppLanguage.preference(this)}){d,i->
                    if(AppLanguage.set(this,options[i].first)){stopComfort();d.dismiss();render()}else message(tx("Language preference could not be saved."))
                }.setNegativeButton(tx("Cancel"),null).showProtected()
            }
            text(tx("Your notes and care records stay in their original language."),parent=c)
            text(tx("Some advanced instructions and external content remain in English."),parent=c)
        }

        button(tx("Back to Today")) { settings = false; tab = 0; render() }
        text(tx("Your app. Your choices."), 29f, bold = true)
        card(tx("App lock")) { c ->
            copy(tx("Lock private screens when CareSphere leaves the foreground. Android 11+ and a secure device screen lock are required. Screenshots are blocked while enabled."),parent=c)
            button(tx(if(AppLock.enabled(this))"Disable app lock" else "Enable app lock"),c){configureAppLock(!AppLock.enabled(this)){render()}}.isEnabled=Build.VERSION.SDK_INT>=30
        }
        card(tx("Profile")) { c ->
            text("Display name: ${data.json.optString("name").ifBlank { "Not entered" }}", parent = c)
            button(tx("Set display name"), c) { input(tx("Your name"), tx("Optional display name"), 60) { data.put("name", it); render() } }.isEnabled = data.canWrite
            button(tx("Clear display name"), c) { data.put("name", ""); render() }.isEnabled = data.canWrite
            val contactName = data.json.optString("contactName")
            val contactPhone = data.json.optString("contactPhone")
            text(tx("Emergency contact") + ": " + if (EmergencyContact.usable(contactName, contactPhone)) "$contactName · $contactPhone" else tx("Not set"), parent = c)
            button(tx("Set emergency contact"), c) { contactDialog() }.isEnabled = data.canWrite
            if (contactName.isNotBlank() || contactPhone.isNotBlank()) button(tx("Clear emergency contact"), c) {
                data.put("contactName", ""); data.put("contactPhone", ""); render()
            }.isEnabled = data.canWrite
            copy(tx("Only used when you choose to call or message them. CareSphere never contacts anyone automatically."), parent = c)
        }
        card(tx("Routine management")) { c ->
            val routines = data.list("routines")
            if(routines.length() == 0) copy(tx("Add routines from Today."), parent = c)
            for(i in 0 until routines.length()) {
                button("Remove ${routines.getJSONObject(i).getString("title")}", c) { confirm(tx("Remove routine?"), "Your other routines will remain.") { val updated = JSONArray(routines.toString()); updated.remove(i); data.put("routines", updated); render() } }.isEnabled = data.canWrite
            }
        }
        card(tx("Appearance")) { c ->
            copy(tx("Choose what feels comfortable. System follows your device's light or dark setting."), parent = c)
            val current = data.json.optString("appearance", "system")
            listOf("system", "light", "dark").forEach { mode ->
                button("${mode.replaceFirstChar { it.uppercase() }}${if(current == mode) " · selected" else ""}", c) { data.put("appearance", mode); render() }
                    .apply { isSelected = current == mode; isEnabled = data.canWrite }
            }
            button(tx(if(DisplayMode.enabled(this))"Bare minimum mode · On" else "Bare minimum mode · Off"), c) { DisplayMode.set(this, !DisplayMode.enabled(this)); render() }
        }
        card(tx("Accessibility")) { c ->
            copy(tx("Text follows Android's font-size setting. Buttons have text labels and generous touch targets. Activities don't auto-advance; no flashing, points, or timed tests. TalkBack uses the native controls."), parent = c)
            button(tx("Open Android accessibility settings"), c) { runCatching { startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)) }.onFailure { message(tx("Open Accessibility in Android Settings.")) } }
        }
        card(tx("Privacy & local data")) { c ->
            copy(tx("Care notes, routines, name, and today's mood are encrypted with AES-GCM using a key in Android Keystore. System backup is disabled. No analytics, ad SDKs, account, or background health monitoring. Shared exports are no longer protected by CareSphere."), parent = c)
            button(tx("Review & export basic care data"), c) {
                val basic = JSONObject()
                listOf("name", "routines", "done", "day", "mood", "notes").forEach { key -> if(data.json.has(key)) basic.put(key, data.json.get(key)) }
                val exported = basic.toString(2)
                if(exported.length > 150000) {
                    message(tx("This text export is too large for the Android share sheet. Use the encrypted backup in My support tools to save your full data, or Device sharing for selected notes and routines."))
                } else confirm(tx("Export unencrypted basic data?"), "Includes your name, daily routines, mood and notes. Personal photos, contacts, other support tools and recently deleted notes are excluded; use an encrypted backup for those. Share only with someone you trust. Preview (first 10,000 characters):\n\n${exported.take(10000)}") { share(exported) }
            }.isEnabled = data.canWrite
            button(tx("Erase all local data"), c) { confirm(tx("Erase everything?"), tx("This permanently removes your saved care information and encryption key from this app. Copies you previously shared cannot be removed.")) { monitor.disconnect(); data.erase(); settings = false; tab = 0; render() } }
        }
        card(tx("About this Android release")) { c ->
            text("CareSphere "+packageManager.getPackageInfo(packageName,0).versionName+" · "+tx("Native Android"),parent=c)
            copy(tx("Apple Intelligence is an Apple-only capability and is not available in this Android app. No AI answers are fabricated or substituted. This is a first Android release, not full iOS feature parity."), parent = c)
        }
    }
    private fun input(title: String, hint: String, limit: Int, initial: String = "", save: (String) -> Unit) {
        val input = EditText(ContextThemeWrapper(this, if(dark) android.R.style.Theme_Material else android.R.style.Theme_Material_Light)).apply { this.hint = hint; setText(initial); setSelection(text.length); minLines = 2; maxLines = 7; filters = arrayOf(android.text.InputFilter.LengthFilter(limit)); setPadding(dp(22), dp(14), dp(22), dp(14)) }
        val dialog = dialogBuilder().setTitle(title).setView(input).setPositiveButton(tx("Save"), null).setNegativeButton(tx("Cancel"), null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val value = input.text.toString().trim()
            if(value.isBlank()) input.error = "Enter some text, or cancel." else { save(value); dialog.dismiss() }
        } }; dialog.showProtected()
    }
    private fun contactDialog() {
        val name = EditText(ContextThemeWrapper(this, if(dark) android.R.style.Theme_Material else android.R.style.Theme_Material_Light)).apply {
            hint = tx("Name"); contentDescription = tx("Emergency contact name")
            setText(data.json.optString("contactName")); filters = arrayOf(android.text.InputFilter.LengthFilter(EmergencyContact.NAME_MAX))
        }
        val phone = EditText(ContextThemeWrapper(this, if(dark) android.R.style.Theme_Material else android.R.style.Theme_Material_Light)).apply {
            hint = tx("Phone"); contentDescription = tx("Emergency contact phone")
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setText(data.json.optString("contactPhone")); filters = arrayOf(android.text.InputFilter.LengthFilter(30))
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(20), 0, dp(20), 0); addView(name); addView(phone)
        }
        val dialog = dialogBuilder().setTitle(tx("Emergency contact")).setView(box)
            .setPositiveButton(tx("Save"), null).setNegativeButton(tx("Cancel"), null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val cleanName = EmergencyContact.cleanName(name.text.toString())
                val cleanPhone = EmergencyContact.cleanPhone(phone.text.toString())
                if (!EmergencyContact.usable(cleanName, cleanPhone)) {
                    phone.error = tx("Enter a name and a phone number with at least 7 digits, or cancel.")
                } else {
                    data.put("contactName", cleanName); data.put("contactPhone", cleanPhone)
                    dialog.dismiss(); render()
                }
            }
        }
        dialog.showProtected()
    }

    private fun confirm(title: String, body: String, action: () -> Unit) {
        dialogBuilder().setTitle(title).setMessage(body).setPositiveButton(tx("Continue")) { _, _ -> action() }.setNegativeButton(tx("Cancel"), null).showProtected()
    }
    private fun message(value: String) { dialogBuilder().setMessage(value).setPositiveButton(tx("OK"), null).showProtected() }
    private fun share(value: String) {
        runCatching { startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, value), "Choose who receives this")) }
            .onFailure { message(tx("No compatible sharing app is available. Nothing was sent.")) }
    }
    private fun openURL(url: String) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }.onFailure { message("No browser is available. Open this link on a device with a browser: $url") }
    }
}
