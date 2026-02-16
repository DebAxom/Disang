package me.debaxom.disang

class TransliterationEngine {

    val charMap = mapOf(
        "." to "্",
        "a" to "আ",
        "i" to "ই",
        "e" to "এ",
        "u" to "উ",
        "ou" to "ঔ",
        "oi" to "ঐ",
        "o" to "ও",
        "k" to "ক",
        "g" to "গ",
        "s" to "স",
        "x" to "শ",
        "c" to "চ",
        "n" to "ন",
        "m" to "ম",
        "j" to "জ",
        "z" to "য",
        "t" to "ত",
        "d" to "দ",
        "b" to "ব",
        "w" to "ৱ",
        "v" to "ভ",
        "h" to "হ",
        "p" to "প",
        "y" to "য়",
        "r" to "ৰ",
        "f" to "ফ",
        "l" to "ল"
    )

    val kars = mapOf(
        "আ" to "া",
        "ই" to "ি",
        "এ" to "ে",
        "উ" to "ু",
        "ঔ" to "ৌ",
        "ঐ" to "ৈ",
        "ও" to "o"
    )

    val assameseWords = mapOf(
        "." to "।",
        "namaskar" to "নমস্কাৰ",
        "toi" to "তই",
        "tumar" to "তোমাৰ",
        "tumak" to "তোমাক",
        "mor" to "মোৰ",
        "tor" to "তোৰ",
        "sini" to "চিনি",
        "kun" to "কোন",
        "sun" to "চোন",
        "moi" to "মই",
        "hoi" to "হয়",
        "hoise" to "হৈছে",
        "korsa" to "কৰছা",
        "korsu" to "কৰছু",
        "axom" to "অসম",
        "asom" to "অসম",
        "asomia" to "অসমীয়া",
        "axomia" to "অসমীয়া",
        "asomiya" to "অসমীয়া",
        "axomiya" to "অসমীয়া",
        "swadhin" to "স্ৱাধীন",
        "kaziranga" to "কাজিৰঙা",
        "rashtriya" to "ৰাষ্ট্ৰীয়",
        "nagoan" to "নগাওঁ",
        "golaghat" to "গোলাঘাট",
        "bhogolik" to "ভৌগোলিক",
        "brahmaputra" to "ব্ৰহ্মপুত্ৰ",
        "prithibi" to "পৃথিৱী",
        "esingia" to "এশিঙীয়া",
        "garh" to "গঁড়",
        "gor" to "গঁড়",
        "hati" to "হাতী",
        "moh" to "ম'হ",
        "horin" to "হৰিণ",
        "uttar" to "উত্তৰ",
        "dakshin" to "দক্ষিণ",
        "bhumi" to "ভূমি",
        "bonoria" to "বনৰীয়া",
        "jetia" to "যেতিয়া",
        "kapur" to "কাপোৰ",
        "napau" to "নাপাও",
        "joi" to "জয়",
        "joy" to "জয়",
        "kot" to "ক'ত",
        "as" to "আছ",
        "pathok" to "পাঠক",
        "pathak" to "পাঠক",
        "boro" to "বড়ো",
        "bodo" to "বড়ো",
        "deka" to "ডেকা",
        "kalita" to "কলিতা",
        "kolita" to "কলিতা",
        "sutia" to "চুতীয়া",
        "chutia" to "চুতীয়া",
        "sutiya" to "চুতীয়া"
    )

    fun translate2Char(input: String): String {
        val rules = listOf(
            "oi" to "ঐ",
            "ou" to "ঔ",
            "ee" to "i",
            "th" to "থ",
            "bh" to "ভ",
            "cl" to "ক্ল",
            "kl" to "ক্ল",
            "jh" to "ঝ",
            "zh" to "ঝ",
            "sk" to "স্ক",
            "shn" to "ষ্ণ",
            "sh" to "ষ",
            "ch" to "চ",
            "khy" to "ক্ষ",
            "kh" to "খ",
            "ng" to "ং",
            "gh" to "ঘ",
            "ndh" to "n.dh",
            "dh" to "ধ",
            "ph" to "ফ",
            "nt" to "n.t",
            "tn" to "t.n",
            "sw" to "s.w",
            "hm" to "h.m",
            "mh" to "m.h",
            "jyo" to "জ্যো"
        )

        var result = input
        for ((pattern, replacement) in rules) {
            result = result.replace(pattern, replacement)
        }

        return result
    }

    fun addWords(first: String, last: String): String {
        val charListFirst = first.map { it.toString() }.toMutableList()
        val charListLast = last.map { it.toString() }.toMutableList()

        if (charListFirst.isEmpty() || charListLast.isEmpty()) return first + last

        var lastChar = charListFirst.last()
        var firstChar = charListLast.first()

        // Handling Ra-Phala (e.g., k + r -> ক্ৰ)
        if (!isVowel(lastChar) && firstChar == "ৰ" && lastChar != "ং") {
            charListLast[0] = "্ৰ"
        }

        // Handling Reph (e.g., r + k -> ৰ্ক)
        if (lastChar == "ৰ" && !isVowel(firstChar)) {
            if (firstChar == "য়") {
                charListLast[0] = "য"
            }
            // charListFirst[charListFirst.size - 1] = "ৰ্"
            return charListFirst.joinToString("") + charListLast.joinToString("")
        }

        // Converting য় to Ya-phala (্য)
        if ((firstChar == "য়" || firstChar == "য") && !isVowel(lastChar)) {
            // Change the standalone letter "য়" into the ligature sign "্য"
            if (charListLast.size > 1 && charListLast[1] == "\u09BC") {
                charListLast.removeAt(1)
            }
            charListLast[0] = "্য"
        }

        // Converting Anusvara (ং) to Nga (ঙ)
        if (lastChar == "ং" && isVowel(firstChar)) {
            charListFirst[charListFirst.size - 1] = "ঙ"
        }

        // Update references after possible modifications above
        lastChar = charListFirst.last()
        firstChar = charListLast.first()

        // Vowel + Vowel logic (Diphthongs/Semi-vowels)
        if (isVowel(lastChar)) {
            if (firstChar == "আ" && lastChar == "ই") {
                charListLast[0] = "য়া"
            }
            if (firstChar == "আ" && lastChar == "উ") {
                charListLast[0] = "ৱা"
            }
            return charListFirst.joinToString("") + charListLast.joinToString("")
        }

        if (isVowel(firstChar)) {
            charListLast[0] = kars[firstChar] ?: firstChar
            return charListFirst.joinToString("") + charListLast.joinToString("")
        }

        return charListFirst.joinToString("") + charListLast.joinToString("")
    }

    fun isVowel(char: String): Boolean {
        val vowels = setOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "এ", "ঐ", "ও", "ঔ","া","ি","ী","ু","ূ","ে","ৈ","ো","ৌ","o")
        return char in vowels
    }

    fun handleO(word: String): MutableList<String> {
        val results = mutableListOf<String>()

        // Helper function to handle the branching logic recursively
        fun generate(current: String, index: Int) {
            // Find the next occurrence of 'o' starting from 'index'
            val oIndex = current.indexOf('o', index)

            // Base case: No more 'o's found, add the final string to results
            if (oIndex == -1) {
                results.add(current)
                return
            }

            // Branch 1: Remove the 'o'
            val removed = current.removeRange(oIndex, oIndex + 1)
            generate(removed, oIndex)

            // Branch 2: Convert the 'o' based on the preceding character
            val precedingChar = if (oIndex > 0) current[oIndex - 1].toString() else ""
            val replacement = if (precedingChar.isNotEmpty() && isVowel(precedingChar)) "ও" else "ো"
            
            val converted = current.substring(0, oIndex) + replacement + current.substring(oIndex + 1)
            
            // We update the index to oIndex + replacement.length to skip the newly added char
            generate(converted, oIndex + replacement.length)
        }

        // Start the recursion
        generate(word, 0)

        // Return unique results as a MutableList
        return results.distinct().toMutableList()
    }

    fun handleSX(word: String): MutableList<String> {
        val results = mutableListOf<String>()
        
        // Map each target to a LIST of possible variations
        val rules = mapOf(
            "শ" to listOf("ষ"),
            "স" to listOf("চ", "ছ"), // "স" now branches into two different things
            "চ" to listOf("ছ")
        )

        fun generate(current: String, index: Int) {
            // Find the next character that exists in our rules keys
            val match = rules.keys
                .map { it to current.indexOf(it, index) }
                .filter { it.second != -1 }
                .minByOrNull { it.second }

            if (match == null) {
                results.add(current)
                return
            }

            val (target, matchIndex) = match
            val replacements = rules[target] ?: emptyList()

            // Branch 1: Keep the original character
            generate(current, matchIndex + 1)

            // Branch 2+: Create a branch for every replacement in the list
            for (replacement in replacements) {
                val converted = current.substring(0, matchIndex) + replacement + current.substring(matchIndex + 1)
                generate(converted, matchIndex + 1)
            }
        }

        generate(word, 0)
        return results.distinct().toMutableList()
    }

    fun postProcess(input: String): MutableList<String> {
        var words = mutableListOf<String>()

        var charList = input.map { it.toString() }.toMutableList()

        if (charList.isEmpty()) return words

        if (charList[0] == "ং") {
            charList[0] = "ঙ"
        }

        if (input.startsWith("ৰ্") && charList.size > 1) {
            charList[1] = ""
        }

        if (input.endsWith('্')) {
            charList[charList.size - 1] = " ।"
        }

        charList.joinToString("")

        handleSX(charList.joinToString("")).forEach{ w ->
            words.addAll(handleO(w))
        }

        return words
    }

    fun getSuggestions(raw_word: String): MutableList<String> {

        var word = raw_word.lowercase()
        if (word.isEmpty()) return mutableListOf<String>()

        if(word in assameseWords){
            return mutableListOf("${assameseWords[word]}", "${word}")
        }

        word = translate(word)
        var suggestions = postProcess(word)
        suggestions.add(raw_word)
       
        return suggestions
    }

    fun translate(raw_word: String): String{

        if(charMap.containsKey(raw_word)){
            return "${charMap[raw_word]}"
        }

        var word = translate2Char(raw_word)

        val charList = word.map { it.toString() }.toMutableList()
        val strLen = charList.size

        if (strLen == 1) return word

        val half = Math.ceil(strLen.toDouble() / 2.0).toInt()
        val firstHalf = charList.subList(0, half).toList()
        repeat(half) { charList.removeAt(0) }
        val secondHalf = charList.takeLast(half)
        val firstHalfString = firstHalf.joinToString("")
        val secondHalfString = secondHalf.joinToString("")

        return addWords(translate(firstHalfString),translate(secondHalfString));
    }

}
