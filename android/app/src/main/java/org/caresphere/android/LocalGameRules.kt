package org.caresphere.android

import kotlin.random.Random

object LocalGameRules {
    private val ranges = listOf(1..15, 16..30, 31..45, 46..60, 61..75)

    fun bingoCards(playerCount: Int): List<List<Int>> = (0 until playerCount.coerceIn(2, 6)).map {
        val columns = ranges.map { range -> range.shuffled().take(5) }
        (0 until 25).map { index ->
            if (index == 12) 0 else {
                val row = index / 5
                val column = index % 5
                columns[column][if (column == 2 && row > 2) row - 1 else row]
            }
        }
    }

    fun bingoCalls(): List<Int> = (1..75).shuffled()

    fun hasBingo(card: List<Int>, marks: Set<Int>): Boolean {
        val lines = mutableListOf<List<Int>>()
        for (index in 0 until 5) {
            lines += (0 until 5).map { index * 5 + it }
            lines += (0 until 5).map { it * 5 + index }
        }
        lines += listOf(listOf(0, 6, 12, 18, 24), listOf(4, 8, 12, 16, 20))
        return lines.any { line -> line.all { card[it] == 0 || card[it] in marks } }
    }

    data class Card(val suit: String, val symbol: String, val red: Boolean, val rank: Int, val faceUp: Boolean = false) {
        val id get() = "$suit-$rank"
        val rankLabel get() = when (rank) { 1 -> "A"; 11 -> "J"; 12 -> "Q"; 13 -> "K"; else -> rank.toString() }
        val name get() = "$rankLabel$symbol"
        fun flipped(up: Boolean) = copy(faceUp = up)
    }

    data class SolitaireState(
        val stock: List<Card>,
        val waste: List<Card>,
        val tableau: List<List<Card>>,
        val foundations: List<List<Card>>
    )

    private val suits = listOf("clubs" to "♣", "diamonds" to "♦", "hearts" to "♥", "spades" to "♠")

    fun newSolitaire(): SolitaireState {
        val deck = suits.flatMap { (suit, symbol) -> (1..13).map { rank -> Card(suit, symbol, suit == "hearts" || suit == "diamonds", rank) } }.shuffled().toMutableList()
        val tableau = (0 until 7).map { column ->
            val cards = deck.take(column + 1).toMutableList()
            repeat(column + 1) { deck.removeAt(0) }
            cards.mapIndexed { index, card -> card.flipped(index == cards.lastIndex) }
        }
        return SolitaireState(deck, emptyList(), tableau, List(4) { emptyList() })
    }

    fun canPlaceTableau(card: Card, target: Card?): Boolean = if (target == null) card.rank == 13
        else target.faceUp && target.rank == card.rank + 1 && target.red != card.red

    fun canPlaceFoundation(card: Card, pile: List<Card>): Boolean = if (pile.isEmpty()) card.rank == 1
        else pile.last().suit == card.suit && pile.last().rank + 1 == card.rank

    data class WordPuzzle(val grid: List<List<String>>, val words: List<String>)

    fun normalizeWord(word: String) = word.replace(Regex("[\\s-]+"), "").uppercase()

    fun wordPuzzle(words: List<String>, size: Int, random: Random = Random.Default): WordPuzzle {
        val size = size.coerceIn(6, 12)
        val grid = MutableList(size) { MutableList(size) { "" } }
        val placed = mutableListOf<String>()
        val normalized = words.map(::normalizeWord).distinct().filter { it.length in 2..size }.sortedByDescending(String::length)
        val alphabet = ('A'..'Z').map(Char::toString)
        normalized.forEach { word ->
            val letters = word.map(Char::toString)
            repeat(180) {
                if (word in placed) return@repeat
                val vertical = random.nextBoolean()
                val row = random.nextInt(size)
                val column = random.nextInt(size)
                if ((if (vertical) row else column) + letters.size > size) return@repeat
                val fits = letters.indices.all { offset ->
                    val existing = grid[row + if (vertical) offset else 0][column + if (vertical) 0 else offset]
                    existing.isEmpty() || existing == letters[offset]
                }
                if (!fits) return@repeat
                letters.forEachIndexed { offset, letter -> grid[row + if (vertical) offset else 0][column + if (vertical) 0 else offset] = letter }
                placed += word
            }
        }
        for (row in 0 until size) for (column in 0 until size) if (grid[row][column].isEmpty()) grid[row][column] = alphabet.random(random)
        return WordPuzzle(grid.map { it.toList() }, placed)
    }

    fun selectedWord(puzzle: WordPuzzle, startRow: Int, startColumn: Int, endRow: Int, endColumn: Int): String? {
        if (startRow != endRow && startColumn != endColumn) return null
        val rowStep = when { endRow > startRow -> 1; endRow < startRow -> -1; else -> 0 }
        val columnStep = when { endColumn > startColumn -> 1; endColumn < startColumn -> -1; else -> 0 }
        val length = maxOf(kotlin.math.abs(endRow - startRow), kotlin.math.abs(endColumn - startColumn)) + 1
        val word = (0 until length).joinToString("") { offset -> puzzle.grid[startRow + rowStep * offset][startColumn + columnStep * offset] }
        return word.takeIf { normalizeWord(it) in puzzle.words.map(::normalizeWord) }
    }
}
