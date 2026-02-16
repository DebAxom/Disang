package me.debaxom.disang.model

object KeyboardLayouts {

    fun letters(): List<List<KeyModel>> = listOf(

        "1 2 3 4 5 6 7 8 9 0".split(" ")
            .map { KeyModel(it, it[0].code) },

        "q w e r t y u i o p".split(" ")
            .map { KeyModel(it, it[0].code) },

        "a s d f g h j k l".split(" ")
            .map { KeyModel(it, it[0].code) },

        listOf(
            KeyModel("⇧", -1, 1.5f, true),
            KeyModel("z", 'z'.code),
            KeyModel("x", 'x'.code),
            KeyModel("c", 'c'.code),
            KeyModel("v", 'v'.code),
            KeyModel("b", 'b'.code),
            KeyModel("n", 'n'.code),
            KeyModel("m", 'm'.code),
            KeyModel("⌫", -5, 1.5f, true)
        ),

        listOf(
            KeyModel("?123", -101, 1.5f, true),
            KeyModel(",", ','.code),
            KeyModel("Space", 32, 4f),
            KeyModel(".", '.'.code),
            KeyModel("↵", 10, 2f, true)
        )
    )

    // Symbols page 1
    fun symbolsPage1(): List<List<KeyModel>> = listOf(

        "1 2 3 4 5 6 7 8 9 0".split(" ")
            .map { KeyModel(it, it[0].code) },

        "! @ # $ % ^ & * ( )".split(" ")
            .map { KeyModel(it, it[0].code) },

        "- _ = + [ ] { } \\".split(" ")
            .map { KeyModel(it, it[0].code) },

        listOf(
            KeyModel("#+=", -103, 1.5f, true),   // MORE SYMBOLS
            KeyModel(";", ';'.code),
            KeyModel(":", ':'.code),
            KeyModel("'", '\''.code),
            KeyModel("\"", '"'.code),
            KeyModel("/", '/'.code),
            KeyModel("?", '?'.code),
            KeyModel("⌫", -5, 1.5f, true)
        ),

        listOf(
            KeyModel("ABC", -102, 1.5f, true),
            KeyModel(",", ','.code),
            KeyModel("Space", 32, 4f),
            KeyModel(".", '.'.code),
            KeyModel("↵", 10, 2f, true)
        )
    )

    // Symbols page 2
    fun symbolsPage2(): List<List<KeyModel>> = listOf(

        "1 2 3 4 5 6 7 8 9 0".split(" ")
            .map { KeyModel(it, it[0].code) },

        "~ ` | • √ π ÷ × ¶ ∆".split(" ")
            .map { KeyModel(it, it[0].code) },

        "£ € ¥ ¢ ° = { } []".split(" ")
            .map { KeyModel(it, it[0].code) },

        listOf(
            KeyModel("123", -104, 1.5f, true),   // BACK TO PAGE 1
            KeyModel("<", '<'.code),
            KeyModel(">", '>'.code),
            KeyModel("«", '«'.code),
            KeyModel("»", '»'.code),
            KeyModel("…", '…'.code),
            KeyModel("?", '?'.code),
            KeyModel("⌫", -5, 1.5f, true)
        ),

        listOf(
            KeyModel("ABC", -102, 1.5f, true),
            KeyModel(",", ','.code),
            KeyModel("Space", 32, 4f),
            KeyModel(".", '.'.code),
            KeyModel("↵", 10, 2f, true)
        )
    )
}
