package com.pushme.app

/**
 * All messages live in this one file.
 * To add your own: add a line inside the matching m(...) group below.
 *
 * Kinds:  AFFIRM  = affirmation          WHY   = why this matters
 *         STEP    = tiny first step      RESTART = coming back after a break
 *         FAITH   = prayer, scripture, sayings
 * Langs:  EN = English, AM = Amharic, MIX = both in one line
 *
 * NOTE: Scripture lines are written from memory. Please compare the wording
 * with your own Bible and fix anything that differs from the version you use.
 * Amharic lines are written in the first person ("I"), so they suit anyone.
 */
enum class Kind { AFFIRM, WHY, STEP, RESTART, FAITH }

enum class Lang { EN, AM, MIX }

data class Msg(val kind: Kind, val lang: Lang, val text: String)

object Messages {

    private fun m(kind: Kind, lang: Lang, vararg texts: String): List<Msg> =
        texts.map { Msg(kind, lang, it) }

    private val base: List<Msg> =
        // ---------------- AFFIRMATIONS ----------------
        m(
            Kind.AFFIRM, Lang.EN,
            "I have kept habits for months before. I can do it again.",
            "Discipline is already mine. I only need to wake it up.",
            "I don't need to feel ready. I only need to begin.",
            "Starting is the hardest part, and I am strong enough for it.",
            "I finish what I give my attention to.",
            "I am not behind. I am exactly where I can begin.",
            "Small effort, repeated, has always carried me far.",
            "I choose to begin, and the rest will follow."
        ) + m(
            Kind.AFFIRM, Lang.AM,
            "ልማዶችን ለወራት ጠብቄ አውቃለሁ። እንደገና ማድረግ እችላለሁ።",
            "ብርታቱ አለኝ፤ መጀመር ብቻ ነው የሚቀረኝ።",
            "ዝግጁ መሆን አይጠበቅብኝም፤ መጀመር ብቻ በቂ ነው።",
            "መጀመር ከሁሉ የከበደ ክፍል ነው፤ እኔ ደግሞ ለዚህ በቂ ብርቱ ነኝ።",
            "ትኩረት የሰጠሁትን ሥራ እጨርሳለሁ።",
            "አልዘገየሁም፤ አሁን ለመጀመር በሚገባኝ ቦታ ላይ ነኝ።",
            "ትንሽ ጥረት ደጋግሞ ሲደረግ ሩቅ ያደርሳል።",
            "ለመጀመር ወስኛለሁ፤ የቀረው ይከተላል።"
        ) + m(
            Kind.AFFIRM, Lang.MIX,
            "ዲሲፕሊን አለኝ. I have kept habits for months before, and I can do it again.",
            "I don't need to feel ready. መጀመር ብቻ በቂ ነው።",
            "መጀመር is the hardest part, and እኔ በቂ ብርቱ ነኝ።",
            "I am not behind. አሁን ለመጀመር ትክክለኛ ቦታ ላይ ነኝ።",
            "Small effort, ደጋግሞ ሲደረግ, carries me far.",
            "I choose to begin. የቀረው ይከተላል።"
        ) +

        // ---------------- WHY IT MATTERS ----------------
        m(
            Kind.WHY, Lang.EN,
            "This task matters because it moves my life forward, even a little.",
            "Future me is counting on what I do in the next ten minutes.",
            "Doing this now frees my mind for the things I love.",
            "Every task I begin builds the person I am becoming.",
            "The relief of having started is worth more than the comfort of waiting.",
            "This is part of the work I was given to do. I will do it well.",
            "Others count on me to show up.",
            "What I do today is the proof of who I say I am."
        ) + m(
            Kind.WHY, Lang.AM,
            "ይህ ሥራ ሕይወቴን ትንሽም ቢሆን ወደ ፊት ያራምደዋል።",
            "ወደ ፊት የምሆነው እኔ በቀጣዮቹ አሥር ደቂቃዎች በምሠራው ላይ ተስፋ አድርጓል።",
            "አሁን ማድረጌ አእምሮዬን ለምወዳቸው ነገሮች ነጻ ያደርገዋል።",
            "የምጀምረው ሥራ ሁሉ የምሆነውን ሰው ይገነባል።",
            "ከመጀመር የሚገኘው እፎይታ ከመጠበቅ ምቾት ይበልጣል።",
            "ይህ ከተሰጠኝ ሥራ አንዱ ክፍል ነው፤ በሚገባ አደርገዋለሁ።",
            "ሌሎች እንድገኝ ይጠብቁኛል።",
            "ዛሬ የማደርገው እኔ ነኝ የምለውን ሰው ማረጋገጫ ነው።"
        ) + m(
            Kind.WHY, Lang.MIX,
            "This task matters. ሕይወቴን ወደ ፊት ያራምደዋል።",
            "Future me ተስፋ አድርጓል on the next ten minutes.",
            "Doing this now ለምወዳቸው ነገሮች አእምሮዬን ነጻ ያደርጋል።",
            "Every task I begin የምሆነውን ሰው ይገነባል።",
            "The relief of starting ከመጠበቅ ምቾት ይበልጣል።",
            "ዛሬ የማደርገው proves who I say I am."
        ) +

        // ---------------- TINY FIRST STEP ----------------
        m(
            Kind.STEP, Lang.EN,
            "Open the file. Nothing more.",
            "Set a timer for two minutes and begin.",
            "Write one sentence. Just one.",
            "Stand up, drink water, sit down, and start with the smallest piece.",
            "Do the easiest part first.",
            "Put your phone face down. One breath. Begin.",
            "Say a short prayer, then do the first small thing.",
            "Don't finish it. Just start it for five minutes."
        ) + m(
            Kind.STEP, Lang.AM,
            "ፋይሉን እከፍታለሁ፤ ከዚያ በላይ አያስፈልግም።",
            "ለሁለት ደቂቃ ሰዓት አዘጋጅቼ እጀምራለሁ።",
            "አንድ ዓረፍተ ነገር ብቻ እጽፋለሁ።",
            "ተነስቼ ውሃ እጠጣለሁ፤ ተመልሼ ከትንሿ ክፍል እጀምራለሁ።",
            "በጣም ቀላሉን ክፍል መጀመሪያ አደርጋለሁ።",
            "ስልኬን ገልብጬ አስቀምጣለሁ፤ አንድ ትንፋሽ ወስጄ እጀምራለሁ።",
            "አጭር ጸሎት ጸልዬ የመጀመሪያውን ትንሽ ነገር አደርጋለሁ።",
            "መጨረስ አያስፈልግም፤ ለአምስት ደቂቃ ብቻ እጀምረዋለሁ።"
        ) + m(
            Kind.STEP, Lang.MIX,
            "ፋይሉን እከፍታለሁ. Open the file, nothing more.",
            "ለ two minutes ብቻ timer አዘጋጅቼ እጀምራለሁ።",
            "One sentence ብቻ እጽፋለሁ።",
            "Easiest part first. በጣም ቀላሉን ክፍል መጀመሪያ።",
            "ስልኬን ገልብጬ, one breath, then begin.",
            "መጨረስ አያስፈልግም. Just five minutes."
        ) +

        // ---------------- RESTART ----------------
        m(
            Kind.RESTART, Lang.EN,
            "It's been a while, and that's okay. Begin small.",
            "You stopped. You can start again. That is what discipline really is.",
            "Don't try to catch up. Just do today's small part.",
            "Yesterday's pause does not cancel your past discipline.",
            "Return gently. Five minutes today is a win.",
            "You owe the past no apology. Just give today a start.",
            "Restarting is a skill, and you are practicing it right now.",
            "The old rhythm is still in you. Wake it with one small act."
        ) + m(
            Kind.RESTART, Lang.AM,
            "ከቆየሁ በኋላ መመለስ ችግር የለውም፤ በትንሹ እጀምራለሁ።",
            "ቆሜ ነበር፤ እንደገና መጀመር እችላለሁ። ሥነ ሥርዓት ማለት ይኸው ነው።",
            "የቀረውን ሁሉ ለመድረስ አልሞክርም፤ የዛሬን ትንሽ ክፍል ብቻ አደርጋለሁ።",
            "ትናንት ማቆሜ ያለፈውን ብርታቴን አይሽረውም።",
            "ቀስ ብዬ እመለሳለሁ፤ ዛሬ አምስት ደቂቃ ድል ነው።",
            "ለያለፈው ይቅርታ መጠየቅ አያስፈልገኝም፤ ለዛሬ ጅምር መስጠት ይበቃኛል።",
            "እንደገና መጀመር ችሎታ ነው፤ አሁን እየተለማመድኩት ነው።",
            "የድሮው ዜማዬ አሁንም በውስጤ አለ፤ በአንድ ትንሽ ተግባር እቀሰቅሰዋለሁ።"
        ) + m(
            Kind.RESTART, Lang.MIX,
            "It's been a while. ችግር የለውም, I begin small.",
            "ቆሜ ነበር. I can start again. That's what discipline is.",
            "I won't catch up on everything. የዛሬን ትንሽ ክፍል only.",
            "ትናንት ማቆሜ doesn't erase my past discipline.",
            "Five minutes today ድል ነው።",
            "Restarting is a skill. አሁን እየተለማመድኩት ነው።"
        ) +

        // ---------------- FAITH: prayer, scripture, sayings ----------------
        m(
            Kind.FAITH, Lang.EN,
            "“I can do all things through Christ who strengthens me.” (Philippians 4:13)",
            "“Be strong and courageous. Do not be afraid.” (Joshua 1:9)",
            "“Do not fear, for I am with you.” (Isaiah 41:10)",
            "“Whatever you do, work at it with all your heart, as for the Lord.” (Colossians 3:23)",
            "“Whatever your hand finds to do, do it with all your might.” (Ecclesiastes 9:10)",
            "“Let us not grow weary in doing good, for in due time we will reap.” (Galatians 6:9)",
            "“Do not despise small beginnings.” (Zechariah 4:10)",
            "Prayer: Lord, bless this beginning and strengthen my hands.",
            "Prayer: God, give me a calm heart and a willing spirit to start.",
            "Saying: “Little by little, the egg walks on its own feet.”"
        ) + m(
            Kind.FAITH, Lang.AM,
            "“ኃይልን በሚሰጠኝ በክርስቶስ ሁሉን እችላለሁ።” (ፊልጵስዩስ 4፥13)",
            "“አይዞህ፥ በርታ፤ አትፍራ።” (ኢያሱ 1፥9)",
            "“እኔ ከአንተ ጋር ነኝና አትፍራ።” (ኢሳይያስ 41፥10)",
            "“የምታደርጉትን ሁሉ ለጌታ እንደምታደርጉ በሙሉ ልባችሁ አድርጉት።” (ቆላስይስ 3፥23)",
            "“እጅህ ለማድረግ የምታገኘውን ሁሉ በኃይልህ አድርግ።” (መክብብ 9፥10)",
            "“መልካም ስናደርግ አንታክት፤ ተስፋ ባንቆርጥ ጊዜው ሲደርስ እናጭዳለንና።” (ገላትያ 6፥9)",
            "“የትንሹን ነገር ቀን የናቀ ማን ነው?” (ዘካርያስ 4፥10)",
            "ጸሎት፦ እግዚአብሔር ሆይ፥ ይህን ጅማሬ ባርክ፤ እጆቼንም አጽና።",
            "ጸሎት፦ አምላኬ ሆይ፥ ለመጀመር የሰከነ ልብና ፈቃደኛ መንፈስ ስጠኝ።",
            "ምሳሌያዊ አነጋገር፦ “ቀስ በቀስ እንቁላል በእግሩ ይሄዳል።”"
        ) + m(
            Kind.FAITH, Lang.MIX,
            "Lord, ይህን ጅማሬ ባርክልኝ, and give me strength for the first step.",
            "“Whatever your hand finds to do, do it with all your might.” (መክብብ 9፥10)",
            "“ቀስ በቀስ እንቁላል በእግሩ ይሄዳል.” Little by little, I begin.",
            "እግዚአብሔር ሆይ, help me start. ቀሪውን አንተ ትባርከዋለህ።",
            "“Do not despise small beginnings.” ትንሽ ጅምር ታላቅ ነው።",
            "በክርስቶስ ሁሉን እችላለሁ. I can do all things through Christ. (ፊልጵስዩስ 4፥13)"
        )

    // ================= MORE MESSAGES (added in v1.2) =================
    private val extra: List<Msg> =
        // ---------------- AFFIRMATIONS ----------------
        m(
            Kind.AFFIRM, Lang.EN,
            "I can do hard things, and starting is the first one.",
            "I am becoming someone who follows through.",
            "My past effort is proof. I have done this before.",
            "Momentum begins the moment I move.",
            "I don't have to do it perfectly. I have to do it.",
            "I am capable, I am steady, and I am starting now."
        ) + m(
            Kind.AFFIRM, Lang.AM,
            "ከባድ ነገሮችን ማድረግ እችላለሁ፤ መጀመር የመጀመሪያው ነው።",
            "የምጀምረውን የምጨርስ ሰው እየሆንኩ ነው።",
            "ያለፈው ጥረቴ ማረጋገጫዬ ነው፤ ይህን ከዚህ በፊት አድርጌያለሁ።",
            "መንቀሳቀስ ስጀምር ጉልበቱ ይከተለኛል።",
            "በፍጹምነት ማድረግ አያስፈልገኝም፤ ማድረግ ብቻ በቂ ነው።",
            "ብቁ ነኝ፤ ሰላም አለኝ፤ አሁን እጀምራለሁ።"
        ) + m(
            Kind.AFFIRM, Lang.MIX,
            "I can do hard things. መጀመር የመጀመሪያው ነው።",
            "My past effort is proof. ይህን ከዚህ በፊት አድርጌያለሁ።",
            "Perfect አያስፈልገኝም. I just need to do it.",
            "Momentum begins when I move. ስንቀሳቀስ ጉልበት ይመጣል።"
        ) +

        // ---------------- WHY IT MATTERS ----------------
        m(
            Kind.WHY, Lang.EN,
            "Ten minutes now saves me an hour of worry later.",
            "This is a small step toward the life I want.",
            "Finishing even a little today makes tomorrow lighter.",
            "The people I love benefit when I follow through.",
            "I protect my own peace of mind by doing this now.",
            "Small tasks done daily become big results."
        ) + m(
            Kind.WHY, Lang.AM,
            "አሁን አሥር ደቂቃ ማድረጌ በኋላ የአንድ ሰዓት ጭንቀት ያድነኛል።",
            "ይህ እኔ ወደምፈልገው ሕይወት የሚወስድ ትንሽ እርምጃ ነው።",
            "ዛሬ ትንሽም ብጨርስ ነገ ይቀልልኛል።",
            "የምወዳቸው ሰዎች እኔ የጀመርኩትን ስቀጥል ይጠቀማሉ።",
            "አሁን ማድረጌ የራሴን የልብ ሰላም ይጠብቃል።",
            "በየቀኑ የሚደረጉ ትንንሽ ሥራዎች ትልቅ ውጤት ይሆናሉ።"
        ) + m(
            Kind.WHY, Lang.MIX,
            "Ten minutes now ከኋላ የአንድ ሰዓት ጭንቀት ያድናል።",
            "This is a small step ወደምፈልገው ሕይወት።",
            "Finishing a little today ነገን ያቀልለዋል።",
            "Small tasks done daily ትልቅ ውጤት ይሆናሉ።"
        ) +

        // ---------------- TINY FIRST STEP ----------------
        m(
            Kind.STEP, Lang.EN,
            "Open the document and read the last line you wrote.",
            "Put what you need on the table. Nothing else yet.",
            "Write the very first action on a sticky note.",
            "Promise yourself ten minutes. You can stop after.",
            "Clear one small corner of your space, then begin.",
            "Take three slow breaths, then touch the work."
        ) + m(
            Kind.STEP, Lang.AM,
            "ሰነዱን ከፍቼ የጻፍኩትን የመጨረሻ መስመር አነባለሁ።",
            "የሚያስፈልገኝን ሁሉ ጠረጴዛው ላይ አሰባስባለሁ፤ ከዚያ ሌላ ምንም።",
            "የመጀመሪያውን ተግባር በትንሽ ወረቀት ላይ እጽፋለሁ።",
            "ለራሴ አሥር ደቂቃ ቃል እገባለሁ፤ ከዚያ ማቆም እችላለሁ።",
            "የምሠራበትን ቦታ አንድ ትንሽ ጥግ አጸዳለሁ፤ ከዚያ እጀምራለሁ።",
            "ሦስት ረጅም ትንፋሽ ወስጄ ሥራውን እነካለሁ።"
        ) + m(
            Kind.STEP, Lang.MIX,
            "ሰነዱን open አድርጌ the last line አነባለሁ።",
            "Write the first action ወረቀት ላይ እጽፋለሁ።",
            "አሥር ደቂቃ promise. After that, I can stop.",
            "Three slow breaths, ከዚያ ሥራውን እነካለሁ።"
        ) +

        // ---------------- RESTART ----------------
        m(
            Kind.RESTART, Lang.EN,
            "Falling behind is not failing. Stepping back in is the win.",
            "You don't need a perfect restart. Any restart counts.",
            "Start where you are, with what you have, today.",
            "Pick up the smallest piece you left and hold it for a minute.",
            "The habit didn't die. It's waiting for one small act.",
            "Be as kind to yourself as you would be to a friend returning from a break."
        ) + m(
            Kind.RESTART, Lang.AM,
            "መዘግየት መውደቅ አይደለም፤ መመለስ ድል ነው።",
            "ፍጹም ጅምር አያስፈልገኝም፤ ማንኛውም ጅምር ይቆጠራል።",
            "ባለሁበት፣ ባለኝ ነገር፣ ዛሬ እጀምራለሁ።",
            "ትቼው የነበረውን በጣም ትንሽ ክፍል አንስቼ ለአንድ ደቂቃ እይዘዋለሁ።",
            "ልማዱ አልሞተም፤ አንድ ትንሽ ተግባር ይጠብቃል።",
            "ከእረፍት ለተመለሰ ጓደኛ እንደምራራ ለራሴም እራራለሁ።"
        ) + m(
            Kind.RESTART, Lang.MIX,
            "Falling behind is not failing. መመለስ ድል ነው።",
            "ፍጹም ጅምር አያስፈልገኝም. Any restart counts.",
            "Start where I am. ዛሬ, with what I have.",
            "The habit didn't die. አንድ ትንሽ ተግባር ይጠብቃል።"
        ) +

        // ---------------- FAITH ----------------
        m(
            Kind.FAITH, Lang.EN,
            "“Commit your work to the Lord, and your plans will be established.” (Proverbs 16:3)",
            "“For God has not given us a spirit of fear, but of power and love and self-control.” (2 Timothy 1:7)",
            "“Those who hope in the Lord will renew their strength.” (Isaiah 40:31)",
            "“He who began a good work in you will carry it on to completion.” (Philippians 1:6)",
            "“Though the righteous fall seven times, they rise again.” (Proverbs 24:16)",
            "“Establish the work of our hands for us.” (Psalm 90:17)",
            "“Trust in the Lord with all your heart, and He will make your paths straight.” (Proverbs 3:5-6)",
            "Prayer: Lord, I bring You this task. Give me wisdom for the first step.",
            "Prayer: Father, thank You for a new day. Help me use it well.",
            "Proverb: “A journey of a thousand miles begins with a single step.”"
        ) + m(
            Kind.FAITH, Lang.AM,
            "“ሥራህን ለእግዚአብሔር አደራ ስጥ፥ አሳብህም ይጸናል።” (ምሳሌ 16፥3)",
            "“እግዚአብሔር የሰጠን የኃይልና የፍቅር የራስንም የመግዛት መንፈስ ነው እንጂ የፍርሃት መንፈስ አይደለም።” (2ኛ ጢሞቴዎስ 1፥7)",
            "“እግዚአብሔርን በተስፋ የሚጠባበቁ ኃይላቸውን ያድሳሉ።” (ኢሳይያስ 40፥31)",
            "“በእናንተ መልካም ሥራን የጀመረ እርሱ ይፈጽመዋል።” (ፊልጵስዩስ 1፥6)",
            "“ጻድቅ ሰባት ጊዜ ቢወድቅ ይነሣል።” (ምሳሌ 24፥16)",
            "“የእጆቻችንንም ሥራ አጽናልን።” (መዝሙር 90፥17)",
            "“በፍጹም ልብህ በእግዚአብሔር ታመን፥ በራስህም ማስተዋል አትደገፍ።” (ምሳሌ 3፥5)",
            "ጸሎት፦ ጌታ ሆይ፥ ይህን ሥራ ወደ አንተ አመጣለሁ፤ ለመጀመሪያው እርምጃ ጥበብ ስጠኝ።",
            "ጸሎት፦ አባት ሆይ፥ ለአዲስ ቀን አመሰግንሃለሁ፤ በሚገባ እንድጠቀምበት እርዳኝ።",
            "ምሳሌ፦ “የሺህ ማይል ጉዞ በአንድ እርምጃ ይጀምራል።”"
        ) + m(
            Kind.FAITH, Lang.MIX,
            "Lord, ይህን ሥራ ወደ አንተ አመጣለሁ. Give me wisdom for the first step.",
            "“ጻድቅ ሰባት ጊዜ ቢወድቅ ይነሣል።” He rises again. (ምሳሌ 24፥16)",
            "አባት ሆይ, thank You for a new day. በሚገባ እንድጠቀምበት እርዳኝ።",
            "“Establish the work of our hands.” የእጆቻችንንም ሥራ አጽናልን።"
        )

    /** Added in v1.7: widely loved ideas (2-minute start, never miss twice, done beats perfect...) in my own words. */
    private val more: List<Msg> =
        m(
            Kind.AFFIRM, Lang.EN,
            "Today I only need to take the next small step.",
            "I am allowed to start imperfectly.",
            "My effort counts, even when it is quiet.",
            "I am becoming the person who shows up.",
            "Gentle progress is still progress.",
            "I can begin with whatever energy I have right now.",
            "I trust myself to figure out the next step.",
            "Every time I begin, beginning gets easier.",
            "I am capable of more than my doubts say.",
            "I give myself permission to do this slowly.",
            "I have handled hard days before, and I will handle this one.",
            "Consistency matters more to me than intensity."
        ) + m(
            Kind.AFFIRM, Lang.AM,
            "ዛሬ አንድ ትንሽ እርምጃ ብቻ በቂ ነው።",
            "ፍጹም ባልሆንም መጀመር እችላለሁ።",
            "ቀስ በቀስ ቢሆንም እየተራመድኩ ነው።"
        ) + m(
            Kind.WHY, Lang.EN,
            "Your future self is watching what you do in the next ten minutes.",
            "Motivation follows action. Move first, and the feeling comes after.",
            "Every delay makes the task bigger in your head than it is in real life.",
            "The discomfort of starting is shorter than the regret of avoiding.",
            "You don't need more time. You need to start the clock.",
            "What you do today is a vote for the person you are becoming.",
            "Waiting to feel ready is just another way of not starting.",
            "This task will not get lighter by being looked at. Pick it up.",
            "Done is better than perfect. Perfect never gets finished.",
            "The hardest minute is the first one. Get through it.",
            "Discipline is choosing what you want most over what you want now.",
            "Small things done daily beat big things planned forever.",
            "You already know what to do. The only question is when. Make it now.",
            "Ten minutes of honest work beats an hour of worrying."
        ) + m(
            Kind.WHY, Lang.AM,
            "መጠበቅ ሥራውን አያቀልለውም፤ አሁን መጀመር ያቀልለዋል።",
            "ዛሬ የምሠራው ነገ የምሆነውን ሰው ይሠራል።"
        ) + m(
            Kind.STEP, Lang.EN,
            "Set a timer for 2 minutes and start. You may stop when it rings.",
            "Do just the first sentence, the first line, or the first click.",
            "Put the tools you need in front of you. That is the first step.",
            "Write down the very next physical action, then do it.",
            "Close every tab except the one you need.",
            "Tell yourself: five minutes only. Then decide.",
            "Stand up, take a breath, and begin with the easiest part.",
            "Make the task so small it feels silly not to do it.",
            "Open the file. That's all. Nothing else is required right now.",
            "Do it badly first. You can improve it after.",
            "Silence your phone and put it face down for 10 minutes.",
            "Decide the when and where: after this, I will do it at my desk.",
            "Pick the smallest piece and finish only that.",
            "Drink some water, then start with step one."
        ) + m(
            Kind.STEP, Lang.AM,
            "ለሁለት ደቂቃ ብቻ ጀምሬ ከዚያ እወስናለሁ።",
            "ፋይሉን ብቻ እከፍታለሁ፤ ሌላ ምንም አይጠበቅብኝም።"
        ) + m(
            Kind.RESTART, Lang.EN,
            "Missing a few days doesn't erase your progress. Pick it up today.",
            "Don't try to catch up. Just do today's small part.",
            "A break is not a failure. Coming back is the win.",
            "Start again at half the size. Momentum will rebuild itself.",
            "Never miss twice. Restart now.",
            "You are not starting from zero. You are starting from experience.",
            "Forget the guilt. Do one small thing from where you left off.",
            "The streak is broken, the habit is not. Begin again.",
            "Return gently, but return today.",
            "Re-read where you stopped, then continue from there.",
            "Beginners restart every day. You are in good company."
        ) + m(
            Kind.RESTART, Lang.AM,
            "ጥቂት ቀን መቅረት ጉዞዬን አያጠፋውም፤ ዛሬ እንደገና እጀምራለሁ።",
            "ሁለት ጊዜ አልቀርም፤ አሁኑኑ እጀምራለሁ።"
        ) + m(
            Kind.FAITH, Lang.EN,
            "“Be strong and courageous. Do not be afraid; do not be discouraged, for the Lord your God will be with you wherever you go.” (Joshua 1:9)",
            "“I can do all things through Christ who strengthens me.” (Philippians 4:13)",
            "“Let us not become weary in doing good, for at the proper time we will reap a harvest if we do not give up.” (Galatians 6:9)",
            "“Whatever you do, work at it with all your heart.” (Colossians 3:23)",
            "“Do not despise these small beginnings.” (Zechariah 4:10)",
            "“Be still, and know that I am God.” (Psalm 46:10)",
            "Prayer: Lord, quiet my worries and steady my hands for the work ahead.",
            "Prayer: God, give me the courage to begin and the patience to continue.",
            "Prayer: Thank You for today's effort. Let me rest, and let tomorrow be a fresh start."
        ) + m(
            Kind.FAITH, Lang.AM,
            "“ቀስ በቀስ እንቁላል በእግሩ ይሄዳል።” (የአማርኛ ምሳሌ)"
        )

    val all: List<Msg> = (base + extra + more).filter { it.lang != Lang.MIX }

    /** Which kind a message belongs to (null if it is not in the list). */
    fun kindOf(text: String): Kind? = all.firstOrNull { it.text == text }?.kind

    /** Small caption shown above a message, e.g. "TINY STEP". */
    fun labelFor(text: String): String =
        when (all.firstOrNull { it.text == text }?.kind) {
            Kind.AFFIRM -> "AFFIRMATION"
            Kind.WHY -> "WHY IT MATTERS"
            Kind.STEP -> "TINY STEP"
            Kind.RESTART -> "RESTART"
            Kind.FAITH -> "FAITH"
            null -> "BEGIN"
        }

    /** Pick a random message that fits the filters, and not the same one as [avoid]. */
    fun pick(kind: Kind?, lang: Lang?, avoid: String?): String {
        val pool = all.filter { (kind == null || it.kind == kind) && (lang == null || it.lang == lang) }
        val choices = if (pool.size > 1) pool.filter { it.text != avoid } else pool
        return choices.random().text
    }
}
