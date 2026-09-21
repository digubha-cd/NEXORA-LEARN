package com.example.core.repository

import com.example.core.model.Chapter
import com.example.core.model.SyllabusScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Official syllabus repository for Gujarat Board Class 12 Commerce.
 *
 * Source of truth: Official Gujarat State Board of School Textbooks (GSSTB).
 * Exact chapter names and structure preserved in original textbook language.
 *
 * School Exam Milestone: 22 October 2026:
 * - Economics: Chapters 1–7
 * - B.A.: Chapters 1–7
 * - English: Chapters 1–5 + Grammar
 * - Gujarati: Chapters 1–20 + Grammar
 * - Elements of Accounts: Part 1
 * - Statistics: Part 1 + Part 2, Chapter 1
 * - SP & CC: Part 1 + Part 2
 */
object SyllabusRepository {

    // In-memory completion state for chapter checkboxes (Step 4)
    private val _completedChapterIds = MutableStateFlow<Set<String>>(emptySet())
    val completedChapterIds: StateFlow<Set<String>> = _completedChapterIds.asStateFlow()

    fun toggleChapterCompletion(chapterId: String) {
        val current = _completedChapterIds.value.toMutableSet()
        val newState = if (current.contains(chapterId)) {
            current.remove(chapterId)
            false
        } else {
            current.add(chapterId)
            true
        }
        _completedChapterIds.value = current
        onChapterCompletionChanged?.invoke(chapterId, newState)
    }

    fun setChapterCompleted(chapterId: String, completed: Boolean) {
        val current = _completedChapterIds.value.toMutableSet()
        if (completed) {
            current.add(chapterId)
        } else {
            current.remove(chapterId)
        }
        _completedChapterIds.value = current
    }

    var onChapterCompletionChanged: ((chapterId: String, completed: Boolean) -> Unit)? = null

    fun isChapterCompleted(chapterId: String): Boolean {
        return _completedChapterIds.value.contains(chapterId)
    }

    // --- 1. GUJARATI (Code: 001) ---
    // Standard 12 Gujarati First Language (ગુજરાતી પ્રથમ ભાષા)
    private val GUJARATI_CHAPTERS = listOf(
        Chapter(
            id = "guj_ch1",
            subjectId = "gujarati",
            chapterNumber = 1,
            title = "૧. અખિલ બ્રહ્માંડમાં (પદ)",
            englishTitle = "Akhil Brahmandma",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch2",
            subjectId = "gujarati",
            chapterNumber = 2,
            title = "૨. ખીજડિયે ટેકરે (નવલિકા)",
            englishTitle = "Khijadiye Tekare",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch3",
            subjectId = "gujarati",
            chapterNumber = 3,
            title = "૩. દમયંતી સ્વયંવર (આખ્યાનખંડ)",
            englishTitle = "Damayanti Swayamvar",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch4",
            subjectId = "gujarati",
            chapterNumber = 4,
            title = "૪. સત્યાગ્રહાશ્રમ (આત્મકથાખંડ)",
            englishTitle = "Satyagrahashram",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch5",
            subjectId = "gujarati",
            chapterNumber = 5,
            title = "૫. રામબાણ (પદ)",
            englishTitle = "Ramban",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch6",
            subjectId = "gujarati",
            chapterNumber = 6,
            title = "૬. ઉછીનું માંગનારાઓ (હાસ્યનિબંધ)",
            englishTitle = "Uchhinu Manganarao",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch7",
            subjectId = "gujarati",
            chapterNumber = 7,
            title = "૭. શ્યામ રંગ સમીપે (ગરબી)",
            englishTitle = "Shyam Rang Samipe",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch8",
            subjectId = "gujarati",
            chapterNumber = 8,
            title = "૮. અમરનાથની યાત્રાએ (પ્રવાસનિબંધ)",
            englishTitle = "Amarnathni Yatrae",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch9",
            subjectId = "gujarati",
            chapterNumber = 9,
            title = "૯. ભવના અબોલા (લોકગીત)",
            englishTitle = "Bhavna Abola",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch10",
            subjectId = "gujarati",
            chapterNumber = 10,
            title = "૧૦. યુધિષ્ઠિર યુદ્ધ-વિષાદ (નાટ્યખંડ)",
            englishTitle = "Yudhishthir Yuddha-Vishad",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch11",
            subjectId = "gujarati",
            chapterNumber = 11,
            title = "૧૧. ઉર્મિલા (ખંડકાવ્ય)",
            englishTitle = "Urmila",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch12",
            subjectId = "gujarati",
            chapterNumber = 12,
            title = "૧૨. સૌજન્યશીલ પ્રભાશંકર (ચરિત્રનિબંધ)",
            englishTitle = "Saujanyashil Prabhashankar",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch13",
            subjectId = "gujarati",
            chapterNumber = 13,
            title = "૧૩. મહાત્માના માણસ (નવલિકા)",
            englishTitle = "Mahatmana Manas",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch14",
            subjectId = "gujarati",
            chapterNumber = 14,
            title = "૧૪. છેલ્લું દર્શન (સોનેટ)",
            englishTitle = "Chhellu Darshan",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch15",
            subjectId = "gujarati",
            chapterNumber = 15,
            title = "૧૫. જુઓ (ગઝલ)",
            englishTitle = "Juo",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch16",
            subjectId = "gujarati",
            chapterNumber = 16,
            title = "૧૬. સેલ્વી પંકજમ્ (નવલિકા)",
            englishTitle = "Selvi Pankjam",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch17",
            subjectId = "gujarati",
            chapterNumber = 17,
            title = "૧૭. પથ્થર થર થર ધ્રૂજે (ગીત)",
            englishTitle = "Paththar Thar Thar Dhruje",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch18",
            subjectId = "gujarati",
            chapterNumber = 18,
            title = "૧૮. આંસુભીનો ઉજાસ (જીવનચરિત્ર)",
            englishTitle = "Aansubhino Ujas",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch19",
            subjectId = "gujarati",
            chapterNumber = 19,
            title = "૧૯. ઈશ્વર સર્વવ્યાપી (ચિંતનાત્મક નિબંધ)",
            englishTitle = "Ishwar Sarvavyapi",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch20",
            subjectId = "gujarati",
            chapterNumber = 20,
            title = "૨૦. બા એકલાં જીવે (ગીત)",
            englishTitle = "Baa Ekla Jive",
            isInSchoolExam = true
        ),
        Chapter(
            id = "guj_ch21",
            subjectId = "gujarati",
            chapterNumber = 21,
            title = "૨૧. બેટા, મને પાછી જવા દે (નાટ્યખંડ)",
            englishTitle = "Beta, Mane Pachhi Java De",
            isInSchoolExam = false
        ),
        Chapter(
            id = "guj_ch22",
            subjectId = "gujarati",
            chapterNumber = 22,
            title = "૨૨. ભાણી (ઊર્મિકાવ્ય)",
            englishTitle = "Bhani",
            isInSchoolExam = false
        ),
        Chapter(
            id = "guj_ch23",
            subjectId = "gujarati",
            chapterNumber = 23,
            title = "૨૩. સમતા અને બંધુતાના પથદર્શક (ચરિત્રલેખ)",
            englishTitle = "Samata Ane Bandhutana Pathdarshak",
            isInSchoolExam = false
        ),
        Chapter(
            id = "guj_ch24",
            subjectId = "gujarati",
            chapterNumber = 24,
            title = "૨૪. શરત (વાર્તા)",
            englishTitle = "Sharat",
            isInSchoolExam = false
        ),
        Chapter(
            id = "guj_grammar",
            subjectId = "gujarati",
            chapterNumber = 25,
            title = "વ્યાકરણ અને અર્થગ્રહણ-લેખન સજ્જતા",
            englishTitle = "Grammar, Comprehension & Composition",
            isGrammar = true,
            isInSchoolExam = true
        )
    )

    // --- 2. ENGLISH (Code: 013) ---
    // Standard 12 English Second Language
    private val ENGLISH_CHAPTERS = listOf(
        Chapter(
            id = "eng_u1",
            subjectId = "english",
            chapterNumber = 1,
            title = "Unit 1: Can You Install Love? / Sunrise on the Kangchenjunga",
            englishTitle = "Unit 1",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eng_u2",
            subjectId = "english",
            chapterNumber = 2,
            title = "Unit 2: Unforgettable Walt Disney / Shaper Shaped",
            englishTitle = "Unit 2",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eng_u3",
            subjectId = "english",
            chapterNumber = 3,
            title = "Unit 3: Manage Your Stress / Stress Control Exercises",
            englishTitle = "Unit 3",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eng_u4",
            subjectId = "english",
            chapterNumber = 4,
            title = "Unit 4: The Adjustment / Blind, Deaf Fish",
            englishTitle = "Unit 4",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eng_u5",
            subjectId = "english",
            chapterNumber = 5,
            title = "Unit 5: Ants / No Men Are Foreign",
            englishTitle = "Unit 5",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eng_u6",
            subjectId = "english",
            chapterNumber = 6,
            title = "Unit 6: Strike Against War",
            englishTitle = "Unit 6",
            isInSchoolExam = false
        ),
        Chapter(
            id = "eng_u7",
            subjectId = "english",
            chapterNumber = 7,
            title = "Unit 7: Monkey's Paw / Sojourner Truth",
            englishTitle = "Unit 7",
            isInSchoolExam = false
        ),
        Chapter(
            id = "eng_u8",
            subjectId = "english",
            chapterNumber = 8,
            title = "Unit 8: For Youth / The Heaven of Freedom",
            englishTitle = "Unit 8",
            isInSchoolExam = false
        ),
        Chapter(
            id = "eng_u9",
            subjectId = "english",
            chapterNumber = 9,
            title = "Unit 9: Headache",
            englishTitle = "Unit 9",
            isInSchoolExam = false
        ),
        Chapter(
            id = "eng_u10",
            subjectId = "english",
            chapterNumber = 10,
            title = "Unit 10: Green Charter",
            englishTitle = "Unit 10",
            isInSchoolExam = false
        ),
        Chapter(
            id = "eng_grammar",
            subjectId = "english",
            chapterNumber = 11,
            title = "Grammar & Writing Skills (Transformation, Indirect Speech, Composition)",
            englishTitle = "Grammar & Writing Skills",
            isGrammar = true,
            isInSchoolExam = true
        )
    )

    // --- 3. SP & CC (Code: 337) ---
    // વાણિજ્ય પત્રવ્યવહાર અને સેક્રેટરીયલ પ્રેક્ટિસ
    private val SPCC_CHAPTERS = listOf(
        // Part 1: વાણિજ્ય પત્રવ્યવહાર (Commercial Correspondence)
        Chapter(
            id = "spcc_p1_ch1",
            subjectId = "sp_cc",
            chapterNumber = 1,
            title = "પ્રકરણ ૧: બેંકને લગતો પત્રવ્યવહાર",
            englishTitle = "Bank Correspondence",
            part = 1,
            partName = "ભાગ ૧: વાણિજ્ય પત્રવ્યવહાર (Commercial Correspondence)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p1_ch2",
            subjectId = "sp_cc",
            chapterNumber = 2,
            title = "પ્રકરણ ૨: સરકારી વિભાગો, જાહેર ઉપયોગી સેવાઓ તથા સ્થાનિક સંસ્થાઓ સાથેનો પત્રવ્યવહાર",
            englishTitle = "Correspondence with Government Departments, Public Services & Local Bodies",
            part = 1,
            partName = "ભાગ ૧: વાણિજ્ય પત્રવ્યવહાર (Commercial Correspondence)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p1_ch3",
            subjectId = "sp_cc",
            chapterNumber = 3,
            title = "પ્રકરણ ૩: આંતર-વિભાગીય અને કર્મચારીવિષયક પત્રવ્યવહાર",
            englishTitle = "Inter-Departmental and Employee Related Correspondence",
            part = 1,
            partName = "ભાગ ૧: વાણિજ્ય પત્રવ્યવહાર (Commercial Correspondence)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p1_ch4",
            subjectId = "sp_cc",
            chapterNumber = 4,
            title = "પ્રકરણ ૪: વીમા સેવાઓ અંગેના પત્રો",
            englishTitle = "Insurance Services Correspondence",
            part = 1,
            partName = "ભાગ ૧: વાણિજ્ય પત્રવ્યવહાર (Commercial Correspondence)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p1_ch5",
            subjectId = "sp_cc",
            chapterNumber = 5,
            title = "પ્રકરણ ૫: ઈ-કમ્યુનિકેશન અને તેના પ્રકારો",
            englishTitle = "E-Communication and its Types",
            part = 1,
            partName = "ભાગ ૧: વાણિજ્ય પત્રવ્યવહાર (Commercial Correspondence)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p1_ch6",
            subjectId = "sp_cc",
            chapterNumber = 6,
            title = "પ્રકરણ ૬: રજૂઆત / પ્રેઝન્ટેશનના કૌશલ્યો",
            englishTitle = "Presentation Skills",
            part = 1,
            partName = "ભાગ ૧: વાણિજ્ય પત્રવ્યવહાર (Commercial Correspondence)",
            isInSchoolExam = true
        ),
        // Part 2: સેક્રેટરીયલ પ્રેક્ટિસ (Secretarial Practice)
        Chapter(
            id = "spcc_p2_ch1",
            subjectId = "sp_cc",
            chapterNumber = 1,
            title = "પ્રકરણ ૧: શેર બહાર પાડવા",
            englishTitle = "Issue of Shares",
            part = 2,
            partName = "ભાગ ૨: સેક્રેટરીયલ પ્રેક્ટિસ (Secretarial Practice)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p2_ch2",
            subjectId = "sp_cc",
            chapterNumber = 2,
            title = "પ્રકરણ ૨: શેર ફેરબદલી અને શેરનું કાયદાકીય હસ્તાંતરણ",
            englishTitle = "Transfer and Transmission of Shares",
            part = 2,
            partName = "ભાગ ૨: સેક્રેટરીયલ પ્રેક્ટિસ (Secretarial Practice)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p2_ch3",
            subjectId = "sp_cc",
            chapterNumber = 3,
            title = "પ્રકરણ ૩: ડિબેન્ચર",
            englishTitle = "Debentures",
            part = 2,
            partName = "ભાગ ૨: સેક્રેટરીયલ પ્રેક્ટિસ (Secretarial Practice)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p2_ch4",
            subjectId = "sp_cc",
            chapterNumber = 4,
            title = "પ્રકરણ ૪: સભ્યપદ",
            englishTitle = "Membership",
            part = 2,
            partName = "ભાગ ૨: સેક્રેટરીયલ પ્રેક્ટિસ (Secretarial Practice)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p2_ch5",
            subjectId = "sp_cc",
            chapterNumber = 5,
            title = "પ્રકરણ ૫: કંપનીના સંચાલકો",
            englishTitle = "Directors of Company",
            part = 2,
            partName = "ભાગ ૨: સેક્રેટરીયલ પ્રેક્ટિસ (Secretarial Practice)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p2_ch6",
            subjectId = "sp_cc",
            chapterNumber = 6,
            title = "પ્રકરણ ૬: કંપનીની સભાઓ",
            englishTitle = "Company Meetings",
            part = 2,
            partName = "ભાગ ૨: સેક્રેટરીયલ પ્રેક્ટિસ (Secretarial Practice)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "spcc_p2_ch7",
            subjectId = "sp_cc",
            chapterNumber = 7,
            title = "પ્રકરણ ૭: કંપનીનું વિસર્જન",
            englishTitle = "Dissolution of Company",
            part = 2,
            partName = "ભાગ ૨: સેક્રેટરીયલ પ્રેક્ટિસ (Secretarial Practice)",
            isInSchoolExam = true
        )
    )

    // --- 4. B.A. (Code: 154) ---
    // વાણિજ્ય વ્યવસ્થા અને સંચાલન (Business Administration)
    private val BA_CHAPTERS = listOf(
        Chapter(
            id = "ba_ch1",
            subjectId = "ba",
            chapterNumber = 1,
            title = "પ્રકરણ ૧: સંચાલનનું સ્વરૂપ અને મહત્વ",
            englishTitle = "Nature and Significance of Management",
            isInSchoolExam = true
        ),
        Chapter(
            id = "ba_ch2",
            subjectId = "ba",
            chapterNumber = 2,
            title = "પ્રકરણ ૨: સંચાલનના સિદ્ધાંતો",
            englishTitle = "Principles of Management",
            isInSchoolExam = true
        ),
        Chapter(
            id = "ba_ch3",
            subjectId = "ba",
            chapterNumber = 3,
            title = "પ્રકરણ ૩: આયોજન",
            englishTitle = "Planning",
            isInSchoolExam = true
        ),
        Chapter(
            id = "ba_ch4",
            subjectId = "ba",
            chapterNumber = 4,
            title = "પ્રકરણ ૪: વ્યવસ્થાતંત્ર",
            englishTitle = "Organizing",
            isInSchoolExam = true
        ),
        Chapter(
            id = "ba_ch5",
            subjectId = "ba",
            chapterNumber = 5,
            title = "પ્રકરણ ૫: કર્મચારી વ્યવસ્થા",
            englishTitle = "Staffing",
            isInSchoolExam = true
        ),
        Chapter(
            id = "ba_ch6",
            subjectId = "ba",
            chapterNumber = 6,
            title = "પ્રકરણ ૬: દોરવણી",
            englishTitle = "Directing",
            isInSchoolExam = true
        ),
        Chapter(
            id = "ba_ch7",
            subjectId = "ba",
            chapterNumber = 7,
            title = "પ્રકરણ ૭: અંકુશ",
            englishTitle = "Controlling",
            isInSchoolExam = true
        ),
        Chapter(
            id = "ba_ch8",
            subjectId = "ba",
            chapterNumber = 8,
            title = "પ્રકરણ ૮: નાણાકીય સંચાલન",
            englishTitle = "Financial Management",
            isInSchoolExam = false
        ),
        Chapter(
            id = "ba_ch9",
            subjectId = "ba",
            chapterNumber = 9,
            title = "પ્રકરણ ૯: નાણાકીય બજાર",
            englishTitle = "Financial Markets",
            isInSchoolExam = false
        ),
        Chapter(
            id = "ba_ch10",
            subjectId = "ba",
            chapterNumber = 10,
            title = "પ્રકરણ ૧૦: બજાર પ્રક્રિયા સંચાલન",
            englishTitle = "Marketing Management",
            isInSchoolExam = false
        ),
        Chapter(
            id = "ba_ch11",
            subjectId = "ba",
            chapterNumber = 11,
            title = "પ્રકરણ ૧૧: ગ્રાહક સુરક્ષા",
            englishTitle = "Consumer Protection",
            isInSchoolExam = false
        ),
        Chapter(
            id = "ba_ch12",
            subjectId = "ba",
            chapterNumber = 12,
            title = "પ્રકરણ ૧૨: ધંધાકીય પર્યાવરણ",
            englishTitle = "Business Environment",
            isInSchoolExam = false
        )
    )

    // --- 5. STATISTICS (Code: 135) ---
    // આંકડાશાસ્ત્ર (Statistics)
    private val STAT_CHAPTERS = listOf(
        // Part 1 (ભાગ ૧)
        Chapter(
            id = "stat_p1_ch1",
            subjectId = "stat",
            chapterNumber = 1,
            title = "પ્રકરણ ૧: સૂચક આંક",
            englishTitle = "Index Number",
            part = 1,
            partName = "ભાગ ૧ (Part 1)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "stat_p1_ch2",
            subjectId = "stat",
            chapterNumber = 2,
            title = "પ્રકરણ ૨: સુરેખ સહસંબંધ",
            englishTitle = "Linear Correlation",
            part = 1,
            partName = "ભાગ ૧ (Part 1)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "stat_p1_ch3",
            subjectId = "stat",
            chapterNumber = 3,
            title = "પ્રકરણ ૩: સુરેખ નિયતસંબંધ",
            englishTitle = "Linear Regression",
            part = 1,
            partName = "ભાગ ૧ (Part 1)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "stat_p1_ch4",
            subjectId = "stat",
            chapterNumber = 4,
            title = "પ્રકરણ ૪: સામાયિક શ્રેણી",
            englishTitle = "Time Series",
            part = 1,
            partName = "ભાગ ૧ (Part 1)",
            isInSchoolExam = true
        ),
        // Part 2 (ભાગ ૨)
        Chapter(
            id = "stat_p2_ch1",
            subjectId = "stat",
            chapterNumber = 1,
            title = "પ્રકરણ ૧: સંભાવના",
            englishTitle = "Probability",
            part = 2,
            partName = "ભાગ ૨ (Part 2)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "stat_p2_ch2",
            subjectId = "stat",
            chapterNumber = 2,
            title = "પ્રકરણ ૨: યાદચ્છિક ચલ અને અસતત સંભાવના વિતરણ",
            englishTitle = "Random Variable and Discrete Probability Distribution",
            part = 2,
            partName = "ભાગ ૨ (Part 2)",
            isInSchoolExam = false
        ),
        Chapter(
            id = "stat_p2_ch3",
            subjectId = "stat",
            chapterNumber = 3,
            title = "પ્રકરણ ૩: પ્રામાણ્ય વિતરણ",
            englishTitle = "Normal Distribution",
            part = 2,
            partName = "ભાગ ૨ (Part 2)",
            isInSchoolExam = false
        ),
        Chapter(
            id = "stat_p2_ch4",
            subjectId = "stat",
            chapterNumber = 4,
            title = "પ્રકરણ ૪: લક્ષ",
            englishTitle = "Limit",
            part = 2,
            partName = "ભાગ ૨ (Part 2)",
            isInSchoolExam = false
        ),
        Chapter(
            id = "stat_p2_ch5",
            subjectId = "stat",
            chapterNumber = 5,
            title = "પ્રકરણ ૫: વિકલન",
            englishTitle = "Differentiation",
            part = 2,
            partName = "ભાગ ૨ (Part 2)",
            isInSchoolExam = false
        )
    )

    // --- 6. ELEMENTS OF ACCOUNTS (Code: 153) ---
    // નામાનાં મૂળતત્ત્વો
    private val ACCOUNTS_CHAPTERS = listOf(
        // Part 1 (ભાગ ૧)
        Chapter(
            id = "acc_p1_ch1",
            subjectId = "accounts",
            chapterNumber = 1,
            title = "પ્રકરણ ૧: ભાગીદારી: વિષય-પ્રવેશ",
            englishTitle = "Introduction to Partnership",
            part = 1,
            partName = "ભાગ ૧: ભાગીદારી હિસાબો (Partnership Accounts)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "acc_p1_ch2",
            subjectId = "accounts",
            chapterNumber = 2,
            title = "પ્રકરણ ૨: ભાગીદારી પેઢીના વાર્ષિક હિસાબો (નાણાકીય પત્રકો)",
            englishTitle = "Final Accounts of Partnership Firm",
            part = 1,
            partName = "ભાગ ૧: ભાગીદારી હિસાબો (Partnership Accounts)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "acc_p1_ch3",
            subjectId = "accounts",
            chapterNumber = 3,
            title = "પ્રકરણ ૩: પાઘડીનું મૂલ્યાંકન",
            englishTitle = "Valuation of Goodwill",
            part = 1,
            partName = "ભાગ ૧: ભાગીદારી હિસાબો (Partnership Accounts)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "acc_p1_ch4",
            subjectId = "accounts",
            chapterNumber = 4,
            title = "પ્રકરણ ૪: ભાગીદારીનું પુનર્ગઠન",
            englishTitle = "Reconstruction of Partnership",
            part = 1,
            partName = "ભાગ ૧: ભાગીદારી હિસાબો (Partnership Accounts)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "acc_p1_ch5",
            subjectId = "accounts",
            chapterNumber = 5,
            title = "પ્રકરણ ૫: ભાગીદારનો પ્રવેશ",
            englishTitle = "Admission of a Partner",
            part = 1,
            partName = "ભાગ ૧: ભાગીદારી હિસાબો (Partnership Accounts)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "acc_p1_ch6",
            subjectId = "accounts",
            chapterNumber = 6,
            title = "પ્રકરણ ૬: ભાગીદારની નિવૃત્તિ / મૃત્યુ",
            englishTitle = "Retirement / Death of a Partner",
            part = 1,
            partName = "ભાગ ૧: ભાગીદારી હિસાબો (Partnership Accounts)",
            isInSchoolExam = true
        ),
        Chapter(
            id = "acc_p1_ch7",
            subjectId = "accounts",
            chapterNumber = 7,
            title = "પ્રકરણ ૭: ભાગીદારી પેઢીનું વિસર્જન",
            englishTitle = "Dissolution of Partnership Firm",
            part = 1,
            partName = "ભાગ ૧: ભાગીદારી હિસાબો (Partnership Accounts)",
            isInSchoolExam = true
        ),
        // Part 2 (ભાગ ૨)
        Chapter(
            id = "acc_p2_ch1",
            subjectId = "accounts",
            chapterNumber = 1,
            title = "પ્રકરણ ૧: શેરમૂડીના હિસાબો",
            englishTitle = "Accounting for Share Capital",
            part = 2,
            partName = "ભાગ ૨: કંપની હિસાબો અને વિશ્લેષણ (Company Accounts & Analysis)",
            isInSchoolExam = false
        ),
        Chapter(
            id = "acc_p2_ch2",
            subjectId = "accounts",
            chapterNumber = 2,
            title = "પ્રકરણ ૨: ડિબેન્ચરના હિસાબો",
            englishTitle = "Accounting for Debentures",
            part = 2,
            partName = "ભાગ ૨: કંપની હિસાબો અને વિશ્લેષણ (Company Accounts & Analysis)",
            isInSchoolExam = false
        ),
        Chapter(
            id = "acc_p2_ch3",
            subjectId = "accounts",
            chapterNumber = 3,
            title = "પ્રકરણ ૩: કંપનીના વાર્ષિક હિસાબો",
            englishTitle = "Company Final Accounts",
            part = 2,
            partName = "ભાગ ૨: કંપની હિસાબો અને વિશ્લેષણ (Company Accounts & Analysis)",
            isInSchoolExam = false
        ),
        Chapter(
            id = "acc_p2_ch4",
            subjectId = "accounts",
            chapterNumber = 4,
            title = "પ્રકરણ ૪: નાણાકીય પત્રકોનું વિશ્લેષણ",
            englishTitle = "Analysis of Financial Statements",
            part = 2,
            partName = "ભાગ ૨: કંપની હિસાબો અને વિશ્લેષણ (Company Accounts & Analysis)",
            isInSchoolExam = false
        ),
        Chapter(
            id = "acc_p2_ch5",
            subjectId = "accounts",
            chapterNumber = 5,
            title = "પ્રકરણ ૫: હિસાબી ગુણોત્તરો અને વિશ્લેષણ",
            englishTitle = "Accounting Ratios and Analysis",
            part = 2,
            partName = "ભાગ ૨: કંપની હિસાબો અને વિશ્લેષણ (Company Accounts & Analysis)",
            isInSchoolExam = false
        ),
        Chapter(
            id = "acc_p2_ch6",
            subjectId = "accounts",
            chapterNumber = 6,
            title = "પ્રકરણ ૬: રોકડ પ્રવાહ પત્રક",
            englishTitle = "Cash Flow Statement",
            part = 2,
            partName = "ભાગ ૨: કંપની હિસાબો અને વિશ્લેષણ (Company Accounts & Analysis)",
            isInSchoolExam = false
        )
    )

    // --- 7. ECONOMICS (Code: 022) ---
    // અર્થશાસ્ત્ર (Economics)
    private val ECONOMICS_CHAPTERS = listOf(
        Chapter(
            id = "eco_ch1",
            subjectId = "economics",
            chapterNumber = 1,
            title = "પ્રકરણ ૧: અર્થશાસ્ત્રમાં આલેખ",
            englishTitle = "Graph in Economics",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eco_ch2",
            subjectId = "economics",
            chapterNumber = 2,
            title = "પ્રકરણ ૨: વૃદ્ધિ અને વિકાસના નિર્દેશકો",
            englishTitle = "Indicators of Growth and Development",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eco_ch3",
            subjectId = "economics",
            chapterNumber = 3,
            title = "પ્રકરણ ૩: નાણું અને ફુગાવો",
            englishTitle = "Money and Inflation",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eco_ch4",
            subjectId = "economics",
            chapterNumber = 4,
            title = "પ્રકરણ ૪: બેંકિંગ અને નાણાકીય નીતિ",
            englishTitle = "Banking and Monetary Policy",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eco_ch5",
            subjectId = "economics",
            chapterNumber = 5,
            title = "પ્રકરણ ૫: ગરીબી",
            englishTitle = "Poverty",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eco_ch6",
            subjectId = "economics",
            chapterNumber = 6,
            title = "પ્રકરણ ૬: બેરોજગારી",
            englishTitle = "Unemployment",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eco_ch7",
            subjectId = "economics",
            chapterNumber = 7,
            title = "પ્રકરણ ૭: વસ્તી",
            englishTitle = "Population",
            isInSchoolExam = true
        ),
        Chapter(
            id = "eco_ch8",
            subjectId = "economics",
            chapterNumber = 8,
            title = "પ્રકરણ ૮: કૃષિ ક્ષેત્ર",
            englishTitle = "Agriculture Sector",
            isInSchoolExam = false
        ),
        Chapter(
            id = "eco_ch9",
            subjectId = "economics",
            chapterNumber = 9,
            title = "પ્રકરણ ૯: ઔદ્યોગિક ક્ષેત્ર",
            englishTitle = "Industrial Sector",
            isInSchoolExam = false
        ),
        Chapter(
            id = "eco_ch10",
            subjectId = "economics",
            chapterNumber = 10,
            title = "પ્રકરણ ૧૦: વિદેશ વેપાર",
            englishTitle = "Foreign Trade",
            isInSchoolExam = false
        ),
        Chapter(
            id = "eco_ch11",
            subjectId = "economics",
            chapterNumber = 11,
            title = "પ્રકરણ ૧૧: ભારતીય અર્થતંત્રમાં નૂતન પ્રશ્નો",
            englishTitle = "Emerging Issues in Indian Economy",
            isInSchoolExam = false
        )
    )

    private val ALL_CHAPTERS_MAP = mapOf(
        "gujarati" to GUJARATI_CHAPTERS,
        "english" to ENGLISH_CHAPTERS,
        "sp_cc" to SPCC_CHAPTERS,
        "ba" to BA_CHAPTERS,
        "stat" to STAT_CHAPTERS,
        "accounts" to ACCOUNTS_CHAPTERS,
        "economics" to ECONOMICS_CHAPTERS
    )

    /**
     * Retrieve all official chapters for a given subject.
     */
    fun getChapters(subjectId: String): List<Chapter> {
        return ALL_CHAPTERS_MAP[subjectId] ?: emptyList()
    }

    /**
     * Retrieve chapters filtered by syllabus scope:
     * - [SyllabusScope.BOARD_EXAM]: Full board syllabus
     * - [SyllabusScope.SCHOOL_EXAM]: 22 October 2026 milestone mapping
     */
    fun getChaptersByScope(subjectId: String, scope: SyllabusScope): List<Chapter> {
        val all = getChapters(subjectId)
        return when (scope) {
            SyllabusScope.BOARD_EXAM -> all
            SyllabusScope.SCHOOL_EXAM -> all.filter { it.isInSchoolExam }
        }
    }

    /**
     * Retrieve a specific chapter by its unique ID.
     */
    fun getChapter(chapterId: String): Chapter? {
        return ALL_CHAPTERS_MAP.values.flatten().firstOrNull { it.id == chapterId }
    }

    fun getChapterById(chapterId: String): Chapter? = getChapter(chapterId)

    /**
     * Get completion progress percentage (0.0 to 1.0) for a subject under a given scope.
     */
    fun getProgress(subjectId: String, scope: SyllabusScope): Float {
        val chapters = getChaptersByScope(subjectId, scope)
        if (chapters.isEmpty()) return 0.0f
        val completedCount = chapters.count { isChapterCompleted(it.id) }
        return completedCount.toFloat() / chapters.size
    }

    /**
     * Get count of completed chapters for a subject under a given scope.
     */
    fun getCompletedCount(subjectId: String, scope: SyllabusScope): Int {
        val chapters = getChaptersByScope(subjectId, scope)
        return chapters.count { isChapterCompleted(it.id) }
    }

    /**
     * Get all chapters across all 7 subjects in the syllabus.
     */
    fun getAllChapters(): List<Chapter> {
        return ALL_CHAPTERS_MAP.values.flatten()
    }

    /**
     * Total chapters across the entire Class 12 Commerce syllabus.
     */
    fun getTotalChaptersCount(): Int {
        return getAllChapters().size
    }

    /**
     * Total completed chapters based on live checkbox state or custom completedSet.
     */
    fun getTotalCompletedChaptersCount(completedSet: Set<String> = _completedChapterIds.value): Int {
        return getAllChapters().count { completedSet.contains(it.id) }
    }

    /**
     * Total pending chapters remaining across all subjects.
     */
    fun getTotalPendingChaptersCount(completedSet: Set<String> = _completedChapterIds.value): Int {
        return getTotalChaptersCount() - getTotalCompletedChaptersCount(completedSet)
    }

    /**
     * Overall progress percentage (0.0 to 1.0) across all 7 subjects.
     */
    fun getOverallProgress(completedSet: Set<String> = _completedChapterIds.value): Float {
        val total = getTotalChaptersCount()
        if (total == 0) return 0f
        return getTotalCompletedChaptersCount(completedSet).toFloat() / total
    }

    /**
     * Total earned chapter points from checked chapters.
     * Completing a chapter earns its points; unchecking removes its points.
     * Derived strictly from the completed chapter set so points are never double-counted.
     */
    fun getTotalEarnedPoints(completedSet: Set<String> = _completedChapterIds.value): Int {
        return getAllChapters().filter { completedSet.contains(it.id) }.sumOf { it.points }
    }

    /**
     * Maximum possible chapter points across the syllabus.
     */
    fun getMaxPossiblePoints(): Int {
        return getAllChapters().sumOf { it.points }
    }

    /**
     * Subject-specific completed chapters count.
     */
    fun getSubjectCompletedCount(subjectId: String, completedSet: Set<String> = _completedChapterIds.value): Int {
        return getChapters(subjectId).count { completedSet.contains(it.id) }
    }

    /**
     * Subject-specific progress percentage (0.0 to 1.0).
     */
    fun getSubjectProgress(subjectId: String, completedSet: Set<String> = _completedChapterIds.value): Float {
        val total = getChapters(subjectId).size
        if (total == 0) return 0f
        return getSubjectCompletedCount(subjectId, completedSet).toFloat() / total
    }

    /**
     * Subject-specific earned chapter points.
     */
    fun getSubjectEarnedPoints(subjectId: String, completedSet: Set<String> = _completedChapterIds.value): Int {
        return getChapters(subjectId).filter { completedSet.contains(it.id) }.sumOf { it.points }
    }

    /**
     * Subject-specific total possible chapter points.
     */
    fun getSubjectMaxPoints(subjectId: String): Int {
        return getChapters(subjectId).sumOf { it.points }
    }

    /**
     * Subject-specific pending chapters count.
     */
    fun getSubjectPendingCount(subjectId: String, completedSet: Set<String> = _completedChapterIds.value): Int {
        val total = getChapters(subjectId).size
        val completed = getSubjectCompletedCount(subjectId, completedSet)
        return total - completed
    }

    /**
     * List of completed chapters for a given subject (or all subjects if null).
     */
    fun getCompletedChapters(subjectId: String? = null, completedSet: Set<String> = _completedChapterIds.value): List<Chapter> {
        val source = if (subjectId != null) getChapters(subjectId) else getAllChapters()
        return source.filter { completedSet.contains(it.id) }
    }

    /**
     * List of pending chapters for a given subject (or all subjects if null).
     */
    fun getPendingChapters(subjectId: String? = null, completedSet: Set<String> = _completedChapterIds.value): List<Chapter> {
        val source = if (subjectId != null) getChapters(subjectId) else getAllChapters()
        return source.filter { !completedSet.contains(it.id) }
    }

    /**
     * List of completed chapter titles for a subject or overall.
     */
    fun getCompletedChapterTitles(subjectId: String? = null, completedSet: Set<String> = _completedChapterIds.value): List<String> {
        return getCompletedChapters(subjectId, completedSet).map { it.title }
    }

    /**
     * List of pending chapter titles for a subject or overall.
     */
    fun getPendingChapterTitles(subjectId: String? = null, completedSet: Set<String> = _completedChapterIds.value): List<String> {
        return getPendingChapters(subjectId, completedSet).map { it.title }
    }

    /**
     * Reset all completed chapters (for testing/cleanup).
     */
    fun resetAllCompletions() {
        _completedChapterIds.value = emptySet()
    }
}
