import SwiftUI
import CareActivityKit

private enum BingoRules {
    static func cards(count: Int) -> [[Int]] {
        (0..<max(2, min(6, count))).map { _ in
            let columns = [(1...15), (16...30), (31...45), (46...60), (61...75)].map { Array($0).shuffled().prefix(5) }
            return (0..<25).map { index in
                if index == 12 { return 0 }
                let row = index / 5
                let column = index % 5
                let columnIndex = column == 2 && row > 2 ? row - 1 : row
                return columns[column][columnIndex]
            }
        }
    }

    static func hasLine(_ card: [Int], marks: Set<Int>) -> Bool {
        var lines = (0..<5).map { row in (0..<5).map { row * 5 + $0 } }
        lines += (0..<5).map { column in (0..<5).map { $0 * 5 + column } }
        lines += [[0, 6, 12, 18, 24], [4, 8, 12, 16, 20]]
        return lines.contains { line in line.allSatisfy { card[$0] == 0 || marks.contains(card[$0]) } }
    }

    static func label(_ number: Int) -> String {
        let letters = ["B", "I", "N", "G", "O"]
        return "\(letters[(number - 1) / 15]) \(number)"
    }
}

struct LocalBingoGameView: View {
    @State private var playerCount = 2
    @State private var cards = BingoRules.cards(count: 2)
    @State private var calls = Array(1...75).shuffled()
    @State private var called: [Int] = []
    @State private var marks = Array(repeating: Set<Int>(), count: 2)
    @State private var activePlayer = 0

    private var winner: Int? { cards.indices.first { BingoRules.hasLine(cards[$0], marks: marks[$0]) } }

    init() {
        let level = UserDefaults.standard.string(forKey: "CareSphere.gameLevel") ?? "gentle"
        let count = level == "challenge" ? 4 : level == "steady" ? 3 : 2
        _playerCount = State(initialValue: count)
        _cards = State(initialValue: BingoRules.cards(count: count))
        _marks = State(initialValue: Array(repeating: Set<Int>(), count: count))
    }

    var body: some View {
        SectionCard(title: AppLanguage.text("Bingo together"), systemImage: "square.grid.3x3.fill") {
            Text(AppLanguage.text("Pass the device between players. No account, internet, or server."))
                .foregroundStyle(.secondary)
            HStack {
                Picker(AppLanguage.text("Players"), selection: $playerCount) {
                    ForEach(2...6, id: \.self) { Text("\($0)").tag($0) }
                }
                .pickerStyle(.menu)
                .onChange(of: playerCount) { count in newGame(count: count) }
                Button(AppLanguage.text("Call next number")) { callNext() }
                    .buttonStyle(.borderedProminent).disabled(calls.isEmpty || winner != nil)
            }
            .frame(minHeight: 48)
            if let current = called.last {
                Text(AppLanguage.format("Called {0}. Mark this card if it matches.", [BingoRules.label(current)]))
                    .font(.title2.weight(.bold)).frame(maxWidth: .infinity)
                    .accessibilityAddTraits(.updatesFrequently)
            } else {
                Text(AppLanguage.text("Ready when you are")).frame(maxWidth: .infinity)
            }
            HStack {
                ForEach(cards.indices, id: \.self) { index in
                    Button(AppLanguage.format("Player {0}", ["\(index + 1)"])) { activePlayer = index }
                        .buttonStyle(.bordered).tint(activePlayer == index ? .emerald : .secondary)
                        .accessibilityAddTraits(activePlayer == index ? .isSelected : [])
                }
            }
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 5), count: 5), spacing: 5) {
                ForEach(Array("BINGO"), id: \.self) { letter in
                    Text(String(letter)).font(.headline).frame(minHeight: 28).frame(maxWidth: .infinity)
                }
                ForEach(cards[activePlayer].indices, id: \.self) { index in
                    let value = cards[activePlayer][index]
                    let isFree = value == 0
                    let isMarked = isFree || marks[activePlayer].contains(value)
                    Button {
                        guard !isFree, called.contains(value), winner == nil else { return }
                        marks[activePlayer].insert(value)
                    } label: {
                        Text(isFree ? AppLanguage.text("Free") : "\(value)")
                            .font(.subheadline.weight(.bold))
                            .frame(maxWidth: .infinity, minHeight: 48)
                            .background(isMarked ? Color.emerald.opacity(0.35) : Color.cardInner, in: RoundedRectangle(cornerRadius: 10))
                    }
                    .buttonStyle(.plain)
                    .disabled(isFree || !called.contains(value) || winner != nil)
                    .accessibilityLabel(isFree ? AppLanguage.text("Free space") : BingoRules.label(value))
                    .accessibilityValue(isMarked ? AppLanguage.text("marked") : "")
                }
            }
            if let winner {
                Text(AppLanguage.format("Bingo! Player {0} has a line.", ["\(winner + 1)"]))
                    .font(.headline).foregroundStyle(Color.emeraldText).accessibilityAddTraits(.updatesFrequently)
            }
            Text(called.suffix(12).reversed().map(BingoRules.label).joined(separator: " · "))
                .font(.footnote).foregroundStyle(.secondary)
            Button(AppLanguage.text("New game")) { newGame(count: playerCount) }
                .buttonStyle(.bordered).frame(minHeight: 44)
        }
    }

    private func callNext() {
        guard !calls.isEmpty, winner == nil else { return }
        called.append(calls.removeFirst())
    }

    private func newGame(count: Int) {
        cards = BingoRules.cards(count: count)
        calls = Array(1...75).shuffled()
        called = []
        marks = Array(repeating: Set<Int>(), count: count)
        activePlayer = 0
    }
}

private struct WordPuzzle {
    let grid: [[Character]]
    let words: [String]

    static func make(words source: [String], size: Int) -> WordPuzzle {
        let size = max(10, min(12, size))
        var grid = Array(repeating: Array(repeating: Character(" "), count: size), count: size)
        var placed: [String] = []
        let words = Array(Set(source.map { $0.uppercased().filter { !$0.isWhitespace && $0 != "-" } })).sorted { $0.count > $1.count }
        for word in words where word.count <= size {
            let letters = Array(word)
            for _ in 0..<180 {
                let vertical = Bool.random()
                let row = Int.random(in: 0..<size)
                let column = Int.random(in: 0..<size)
                if (vertical ? row + letters.count : column + letters.count) > size { continue }
                let fits = letters.enumerated().allSatisfy { offset, letter in
                    let existing = grid[row + (vertical ? offset : 0)][column + (vertical ? 0 : offset)]
                    return existing == " " || existing == letter
                }
                guard fits else { continue }
                for (offset, letter) in letters.enumerated() {
                    grid[row + (vertical ? offset : 0)][column + (vertical ? 0 : offset)] = letter
                }
                placed.append(word)
                break
            }
        }
        let alphabet = Array("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        for row in 0..<size {
            for column in 0..<size where grid[row][column] == " " {
                grid[row][column] = alphabet.randomElement()!
            }
        }
        return WordPuzzle(grid: grid, words: placed)
    }
}

struct WordSearchGameView: View {
    @AppStorage("CareSphere.gameLevel") private var gameLevel = "gentle"
    @State private var preferencesApplied = false
    @State private var theme = "Nature"
    @State private var size = 10
    @State private var start: (row: Int, column: Int)?
    @State private var found: Set<String> = []
    @State private var foundCells: Set<String> = []
    @State private var message = "Choose the first and last letter of a word in the same row or column."
    @State private var puzzle = WordPuzzle.make(words: ["FLOWER", "LEAF", "TREE", "BIRD", "CLOUD", "RIVER", "FOREST", "OCEAN", "GARDEN", "RAINBOW"], size: 10)

    private var words: [String] {
        let language = AppLanguage.current
        switch (theme, language) {
        case ("Nature", "es"): return ["FLOR", "HOJA", "ARBOL", "AVE", "NUBE", "RIO", "BOSQUE", "MAR", "JARDIN", "ARCOIRIS"]
        case ("Nature", "zh-Hans"): return ["花朵", "树叶", "树木", "小鸟", "云朵", "河流", "森林", "海洋", "花园", "彩虹"]
        case ("Nature", "fil"): return ["BULAKLAK", "DAHON", "PUNO", "IBON", "ULAP", "ILOG", "GUBAT", "DAGAT", "ARAW", "BATO"]
        case ("Nature", "vi"): return ["HOA", "LÁ", "CÂY", "CHIM", "MÂY", "SÔNG", "RỪNG", "BIỂN", "VƯỜN", "CẦU VỒNG"]
        case ("Home", "es"): return ["TAZA", "LIBRO", "LUZ", "SILLA", "PUERTA", "MESA", "PLATO", "RELOJ", "CAMA", "VENTANA"]
        case ("Home", "zh-Hans"): return ["杯子", "书本", "台灯", "椅子", "房门", "桌子", "盘子", "时钟", "枕头", "窗户"]
        case ("Home", "fil"): return ["BASO", "AKLAT", "ILAW", "SILYA", "PINTO", "MESA", "PLATO", "ORASAN", "UNAN", "BINTANA"]
        case ("Home", "vi"): return ["CỐC", "SÁCH", "ĐÈN", "GHẾ", "CỬA", "BÀN", "ĐĨA", "ĐỒNG HỒ", "GIƯỜNG", "CỬA SỔ"]
        case ("Food", "es"): return ["MANZANA", "PAN", "PERA", "SOPA", "ARROZ", "PLATANO", "ZANAHORIA", "QUESO", "NARANJA", "UVA"]
        case ("Food", "zh-Hans"): return ["苹果", "面包", "梨子", "米饭", "蘑菇", "香蕉", "胡萝卜", "奶酪", "鸡蛋", "土豆"]
        case ("Food", "fil"): return ["MANSANAS", "TINAPAY", "PERAS", "SABAW", "BIGAS", "SAGING", "KAROT", "KESO", "DALANDAN", "ITLOG"]
        case ("Food", "vi"): return ["TÁO", "BÁNH MÌ", "LÊ", "SÚP", "CƠM", "CHUỐI", "CÀ RỐT", "PHÔ MAI", "CAM", "KHOAI"]
        case ("Home", _): return ["CUP", "BOOK", "LAMP", "CHAIR", "DOOR", "TABLE", "PLATE", "CLOCK", "PILLOW", "WINDOW"]
        case ("Food", _): return ["APPLE", "BREAD", "PEAR", "SOUP", "RICE", "BANANA", "CARROT", "CHEESE", "ORANGE", "POTATO"]
        default: return ["FLOWER", "LEAF", "TREE", "BIRD", "CLOUD", "RIVER", "FOREST", "OCEAN", "GARDEN", "RAINBOW"]
        }
    }

    var body: some View {
        SectionCard(title: AppLanguage.text("Word search"), systemImage: "text.magnifyingglass") {
            Text(AppLanguage.text("Find each word in a straight line across or down. Tap its first and last letter; reverse direction is fine."))
                .foregroundStyle(.secondary)
            Picker(AppLanguage.text("Word theme"), selection: $theme) {
                ForEach(["Nature", "Home", "Food"], id: \.self) { Text(AppLanguage.text($0)).tag($0) }
            }.pickerStyle(.menu).onChange(of: theme) { _ in reset() }
            Picker(AppLanguage.text("Grid size"), selection: $size) {
                ForEach([10, 12], id: \.self) { Text("\($0) × \($0)").tag($0) }
            }.pickerStyle(.menu).onChange(of: size) { _ in reset() }
            puzzleGrid
            wordsToFind
            ActivityFeedback(text: message)
            Button(AppLanguage.text("New puzzle"), action: reset).buttonStyle(.borderedProminent).frame(minHeight: 44)
        }
        .onAppear {
            guard !preferencesApplied else { return }
            preferencesApplied = true
            let preferredSize = gameLevel == "challenge" ? 12 : 10
            size = preferredSize
            puzzle = WordPuzzle.make(words: words, size: preferredSize)
        }
    }

    private var startKey: String? { start.map { "\($0.row):\($0.column)" } }

    private var puzzleGrid: some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 2), count: size), spacing: 2) {
            ForEach(0..<(size * size), id: \.self) { index in
                puzzleCell(index)
            }
        }
    }

    private func puzzleCell(_ index: Int) -> some View {
        let row = index / size
        let column = index % size
        let key = "\(row):\(column)"
        let letter = String(puzzle.grid[row][column])
        let background = foundCells.contains(key) ? Color.emerald.opacity(0.35) : key == startKey ? Color.yellow.opacity(0.3) : Color.cardInner
        return Button { choose(row: row, column: column) } label: {
            Text(letter)
                .font(.system(size: 15, weight: .bold, design: .rounded))
                .frame(maxWidth: .infinity, minHeight: CGFloat(max(30, 280 / size)))
                .background(background, in: RoundedRectangle(cornerRadius: 5))
        }.buttonStyle(.plain)
            .accessibilityLabel(AppLanguage.format("Row {0}, column {1}, {2}", ["\(row + 1)", "\(column + 1)", letter]))
            .accessibilityAddTraits(foundCells.contains(key) || key == startKey ? .isSelected : [])
    }

    private var wordsToFind: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(AppLanguage.text("Words to find")).font(.headline)
            ForEach(puzzle.words, id: \.self) { word in
                Label(word, systemImage: found.contains(word) ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(found.contains(word) ? Color.emeraldText : Color.primary)
            }
        }
    }

    private func choose(row: Int, column: Int) {
        guard let first = start else { start = (row, column); message = "Choose the last letter of that word."; return }
        guard first.row == row || first.column == column else { start = (row, column); message = "Choose cells in the same row or column. Try again from this letter."; return }
        let step = first.row == row ? (0, column >= first.column ? 1 : -1) : (row >= first.row ? 1 : -1, 0)
        let distance = max(abs(row - first.row), abs(column - first.column))
        let selected = String((0...distance).map { puzzle.grid[first.row + step.0 * $0][first.column + step.1 * $0] })
        start = nil
        guard let word = WordSearchRules.matchedWord(selected, in: puzzle.words) else {
            message = "No listed word there. Choose another first letter."
            return
        }
        guard !found.contains(word) else { message = "That word is already found. Choose another one."; return }
        found.insert(word)
        for offset in 0...distance { foundCells.insert("\(first.row + step.0 * offset):\(first.column + step.1 * offset)") }
        message = AppLanguage.format("Found: {0}", [word])
    }

    private func reset() {
        puzzle = WordPuzzle.make(words: words, size: size)
        found = []
        foundCells = []
        start = nil
        message = "Choose the first and last letter of a word in the same row or column."
    }
}

private struct LocalSolitaireCard: Identifiable, Equatable {
    let suit: String
    let symbol: String
    let red: Bool
    let rank: Int
    var faceUp: Bool
    var id: String { "\(suit)-\(rank)" }
    var symbolName: String {
        switch suit {
        case "hearts": return "suit.heart.fill"
        case "diamonds": return "suit.diamond.fill"
        case "clubs": return "suit.club.fill"
        default: return "suit.spade.fill"
        }
    }
    var name: String {
        let value = ["", "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"][rank]
        return AppLanguage.format("{0} of {1}", [AppLanguage.text(value), AppLanguage.text(suit)])
    }
}

private struct SolitaireState: Equatable {
    var stock: [LocalSolitaireCard]
    var waste: [LocalSolitaireCard]
    var tableau: [[LocalSolitaireCard]]
    var foundations: [[LocalSolitaireCard]]

    static func deal() -> SolitaireState {
        let suits = [("clubs", "♣", false), ("diamonds", "♦", true), ("hearts", "♥", true), ("spades", "♠", false)]
        var deck = suits.flatMap { suit in (1...13).map { LocalSolitaireCard(suit: suit.0, symbol: suit.1, red: suit.2, rank: $0, faceUp: false) } }.shuffled()
        var piles: [[LocalSolitaireCard]] = []
        for index in 0..<7 {
            let cards = Array(deck.prefix(index + 1))
            deck.removeFirst(index + 1)
            piles.append(cards.enumerated().map { offset, card in var value = card; value.faceUp = offset == cards.count - 1; return value })
        }
        return SolitaireState(stock: deck, waste: [], tableau: piles, foundations: Array(repeating: [], count: 4))
    }
}

private enum SolitaireSource: Equatable { case waste, tableau(Int, Int) }

private struct CourtPortraitView: View {
    let card: LocalSolitaireCard

    private var accent: Color { card.red ? Color(red: 0.67, green: 0.14, blue: 0.19) : Color(red: 0.08, green: 0.28, blue: 0.23) }

    var body: some View {
        VStack(spacing: 0) {
            Image(systemName: "crown.fill").font(.system(size: 8, weight: .black)).foregroundStyle(Color(red: 0.75, green: 0.56, blue: 0.22))
            ZStack(alignment: .bottom) {
                Circle().fill(LinearGradient(colors: [Color(red: 0.99, green: 0.86, blue: 0.64), Color(red: 0.82, green: 0.60, blue: 0.38)], startPoint: .topLeading, endPoint: .bottomTrailing))
                Circle().stroke(Color(red: 0.75, green: 0.56, blue: 0.22), lineWidth: 1)
                Image(systemName: "person.fill").font(.system(size: 19, weight: .black)).foregroundStyle(accent).offset(y: -1)
                Image(systemName: card.symbolName).resizable().scaledToFit().frame(width: 8, height: 8).foregroundStyle(.white).offset(y: -1)
            }.frame(width: 27, height: 27)
            Rectangle().fill(Color(red: 0.75, green: 0.56, blue: 0.22)).frame(height: 1).padding(.vertical, 1)
            Text(AppLanguage.text(["", "", "", "", "", "", "", "", "", "", "", "Jack", "Queen", "King"][card.rank]))
                .font(.system(size: 5, weight: .black, design: .serif)).tracking(0.5).foregroundStyle(accent)
            Text(card.symbol).font(.system(size: 10, weight: .black, design: .serif)).foregroundStyle(accent)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(LinearGradient(colors: [Color(red: 1, green: 0.98, blue: 0.91), Color(red: 0.91, green: 0.82, blue: 0.61)], startPoint: .topLeading, endPoint: .bottomTrailing), in: RoundedRectangle(cornerRadius: 5))
        .overlay(RoundedRectangle(cornerRadius: 5).stroke(Color(red: 0.75, green: 0.56, blue: 0.22), lineWidth: 0.7))
    }
}

struct SolitaireGameView: View {
    @State private var game = SolitaireState.deal()
    @State private var history: [SolitaireState] = []
    @State private var source: SolitaireSource?
    @State private var drawCount: Int

    init() {
        let level = UserDefaults.standard.string(forKey: "CareSphere.gameLevel") ?? "gentle"
        _drawCount = State(initialValue: level == "challenge" ? 3 : 1)
    }

    var body: some View {
        SectionCard(title: AppLanguage.text("Solitaire"), systemImage: "suit.spade.fill") {
            Picker(AppLanguage.text("Draw cards"), selection: $drawCount) {
                Text(AppLanguage.text("Draw one")).tag(1)
                Text(AppLanguage.text("Draw three")).tag(3)
            }.pickerStyle(.segmented)
            HStack(spacing: 8) {
                Button { draw() } label: { pileTop(game.stock.last, label: game.stock.isEmpty ? "↻" : "✦") }
                    .accessibilityLabel(game.stock.isEmpty ? AppLanguage.text("Return waste to stock") : AppLanguage.format("Stock: {0} cards. Draw cards", ["\(game.stock.count)"]))
                Button { selectWaste() } label: { pileTop(game.waste.last, label: AppLanguage.text("Waste")) }
                    .accessibilityLabel(AppLanguage.text("Waste pile"))
                ForEach(0..<4, id: \.self) { index in
                    Button { moveToFoundation(index) } label: { pileTop(game.foundations[index].last, label: ["♣", "♦", "♥", "♠"][index]) }
                        .accessibilityLabel(AppLanguage.format("Foundation {0}", [AppLanguage.text(["clubs", "diamonds", "hearts", "spades"][index])]))
                }
            }
            ScrollView(.horizontal) {
                HStack(alignment: .top, spacing: 4) {
                    ForEach(game.tableau.indices, id: \.self) { pileIndex in
                        VStack(spacing: -35) {
                            ForEach(game.tableau[pileIndex].indices, id: \.self) { cardIndex in
                                let card = game.tableau[pileIndex][cardIndex]
                                Button { tapTableau(pile: pileIndex, index: cardIndex) } label: { cardFace(card, selected: isSelected(pile: pileIndex, index: cardIndex)) }
                                    .buttonStyle(.plain)
                                    .accessibilityLabel(card.faceUp ? card.name : AppLanguage.format("Pile {0}, face-down card", ["\(pileIndex + 1)"]))
                            }
                            if game.tableau[pileIndex].isEmpty {
                                Button("K") { moveToTableau(pileIndex) }
                                    .frame(width: 48, height: 68)
                                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(.secondary, style: StrokeStyle(lineWidth: 1, dash: [4])))
                                    .accessibilityLabel(AppLanguage.format("Empty pile {0}", ["\(pileIndex + 1)"]))
                            }
                        }.frame(width: 50, alignment: .top)
                    }
                }.padding(.vertical, 38)
            }
            HStack {
                Button(AppLanguage.text("Undo")) { undo() }.disabled(history.isEmpty)
                Button(AppLanguage.text("New game")) { restart() }
            }.buttonStyle(.bordered)
            ActivityFeedback(text: source == nil ? "Select a card, then choose a legal pile. The exposed card turns over automatically." : "Card selected. Choose a legal pile, or tap it again to cancel.")
        }
    }

    private func pileTop(_ card: LocalSolitaireCard?, label: String) -> some View {
        Group {
            if let card { cardFace(card, selected: false) }
            else { Text(label).font(.title3.weight(.bold)).foregroundStyle(.secondary).frame(width: 48, height: 68) }
        }
        .frame(width: 48, height: 68)
        .background(Color.cardInner, in: RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(.secondary.opacity(0.5), lineWidth: 1))
    }

    private func cardFace(_ card: LocalSolitaireCard, selected: Bool) -> some View {
        let accent = card.red ? Color(red: 0.67, green: 0.14, blue: 0.19) : Color(red: 0.08, green: 0.28, blue: 0.23)
        let rank = ["", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"][card.rank]
        return ZStack {
            RoundedRectangle(cornerRadius: 8).fill(LinearGradient(colors: [Color.white, Color(red: 0.97, green: 0.94, blue: 0.85)], startPoint: .topLeading, endPoint: .bottomTrailing))
            RoundedRectangle(cornerRadius: 6).stroke(Color(red: 0.80, green: 0.69, blue: 0.43), lineWidth: 1).padding(3)
            if card.faceUp {
                if card.rank >= 11 {
                    CourtPortraitView(card: card)
                        .padding(.horizontal, 5).padding(.vertical, 7)
                } else if card.rank == 1 {
                    ZStack {
                        Circle().stroke(Color(red: 0.82, green: 0.66, blue: 0.32), lineWidth: 1.5).padding(5)
                        Image(systemName: card.symbolName).resizable().scaledToFit().frame(width: 28, height: 28).foregroundStyle(accent)
                    }.padding(8)
                } else {
                    LazyVGrid(columns: [GridItem(.flexible(), spacing: 0), GridItem(.flexible(), spacing: 0)], spacing: -2) {
                        ForEach(0..<card.rank, id: \.self) { _ in
                            Image(systemName: card.symbolName).resizable().scaledToFit().frame(width: 8, height: 8).foregroundStyle(accent)
                        }
                    }.padding(.horizontal, 7).padding(.vertical, 12)
                }
                VStack {
                    HStack(alignment: .top, spacing: 1) {
                        Text(rank).font(.system(size: 8, weight: .black, design: .serif))
                        Image(systemName: card.symbolName).resizable().scaledToFit().frame(width: 6, height: 6).padding(.top, 1)
                    }.foregroundStyle(accent).frame(maxWidth: .infinity, alignment: .leading)
                    Spacer(minLength: 0)
                    HStack(alignment: .top, spacing: 1) {
                        Text(rank).font(.system(size: 8, weight: .black, design: .serif))
                        Image(systemName: card.symbolName).resizable().scaledToFit().frame(width: 6, height: 6).padding(.top, 1)
                    }.foregroundStyle(accent).frame(maxWidth: .infinity, alignment: .leading).rotationEffect(.degrees(180))
                }.padding(5)
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 5).fill(LinearGradient(colors: [Color(red: 0.06, green: 0.25, blue: 0.22), Color(red: 0.13, green: 0.42, blue: 0.31)], startPoint: .topLeading, endPoint: .bottomTrailing)).padding(5)
                    RoundedRectangle(cornerRadius: 5).stroke(Color(red: 0.84, green: 0.69, blue: 0.39), lineWidth: 1).padding(8)
                    Image(systemName: "sparkle").font(.system(size: 19, weight: .medium)).foregroundStyle(Color(red: 0.95, green: 0.83, blue: 0.55))
                    Image(systemName: "diamond").font(.system(size: 11, weight: .medium)).foregroundStyle(Color(red: 0.95, green: 0.83, blue: 0.55))
                }
            }
        }
        .frame(width: 48, height: 68)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(selected ? Color.emeraldText : Color(red: 0.72, green: 0.64, blue: 0.45), lineWidth: selected ? 3 : 1))
        .shadow(color: .black.opacity(0.16), radius: 2, x: 0, y: 1)
    }

    private func selectedCards() -> [LocalSolitaireCard] {
        switch source {
        case .waste: return game.waste.last.map { [$0] } ?? []
        case let .tableau(pile, index): return Array(game.tableau[pile].dropFirst(index))
        case nil: return []
        }
    }

    private func isSelected(pile: Int, index: Int) -> Bool {
        if case let .tableau(sourcePile, sourceIndex) = source { return sourcePile == pile && index >= sourceIndex }
        return false
    }

    private func tapTableau(pile: Int, index: Int) {
        let card = game.tableau[pile][index]
        if !card.faceUp {
            guard index == game.tableau[pile].count - 1 else { return }
            var next = game
            next.tableau[pile][index].faceUp = true
            commit(next)
            return
        }
        if source != nil, source != .tableau(pile, index), moveToTableau(pile) { return }
        if source == .tableau(pile, index) { source = nil }
        else { source = .tableau(pile, index) }
    }

    @discardableResult private func moveToTableau(_ pile: Int) -> Bool {
        guard let source else { return false }
        if case let .tableau(sourcePile, _) = source, sourcePile == pile { return false }
        let cards = selectedCards()
        guard let moving = cards.first else { return false }
        let destination = game.tableau[pile].last
        let valid = destination.map { $0.faceUp && $0.rank == moving.rank + 1 && $0.red != moving.red } ?? (moving.rank == 13)
        guard valid else { return false }
        var next = game
        removeSource(from: &next, source: source)
        next.tableau[pile].append(contentsOf: cards)
        commit(next)
        return true
    }

    private func moveToFoundation(_ index: Int) {
        guard let source, let card = selectedCards().first, selectedCards().count == 1 else { return }
        let foundation = game.foundations[index]
        let valid = foundation.last.map { $0.suit == card.suit && $0.rank + 1 == card.rank } ?? (card.rank == 1)
        guard valid else { return }
        var next = game
        removeSource(from: &next, source: source)
        next.foundations[index].append(card)
        commit(next)
    }

    private func removeSource(from game: inout SolitaireState, source: SolitaireSource) {
        switch source {
        case .waste: game.waste.removeLast()
        case let .tableau(pile, index):
            game.tableau[pile].removeSubrange(index..<game.tableau[pile].count)
            if !game.tableau[pile].isEmpty { game.tableau[pile][game.tableau[pile].count - 1].faceUp = true }
        }
    }

    private func selectWaste() { source = source == .waste ? nil : (game.waste.isEmpty ? nil : .waste) }

    private func draw() {
        source = nil
        var next = game
        if !next.stock.isEmpty {
            let count = min(drawCount, next.stock.count)
            let drawn = Array(next.stock.suffix(count).reversed())
            next.stock.removeLast(count)
            next.waste.append(contentsOf: drawn.map { card in var value = card; value.faceUp = true; return value })
            commit(next)
        } else if !next.waste.isEmpty {
            next.stock = next.waste.reversed().map { card in var value = card; value.faceUp = false; return value }
            next.waste = []
            commit(next)
        }
    }

    private func commit(_ next: SolitaireState) { history.append(game); game = next; source = nil }
    private func undo() { guard let previous = history.popLast() else { return }; game = previous; source = nil }
    private func restart() { game = SolitaireState.deal(); history = []; source = nil }
}
