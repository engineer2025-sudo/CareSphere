import CareActivityKit
import SwiftUI


private let feelingOptions: [(name: String, emoji: String)] = [
    ("Happy", "😊"), ("Calm", "😌"), ("Excited", "🤩"), ("Tired", "😴"), ("Not sure", "💭"),
]
private let feelingPictures: [(emoji: String, description: String)] = [
    ("🙂", "A simplified face with a small upward-curving mouth."),
    ("😐", "A simplified face with open eyes and a straight mouth."),
    ("😴", "A simplified face with closed eyes and sleep marks above it."),
    ("😊", "A simplified face with curved eyes and an upward-curving mouth."),
]

struct FeelingsExplorerCard: View {
    @State private var pictureIndex = 0
    @State private var choice: String?

    var body: some View {
        SectionCard(title: AppLanguage.text("Feelings explorer"), systemImage: "face.smiling") {
            Text(AppLanguage.text("What might this person be feeling? Choose any word—or “Not sure.” These emoji are simplified illustrations, not rules for reading real people; there is no right answer or score."))
                .font(.subheadline)
                .foregroundStyle(.secondary)
            VStack(spacing: 10) {
                Text(feelingPictures[pictureIndex].emoji)
                    .font(.system(size: 58))
                    .accessibilityLabel(feelingPictures[pictureIndex].description)
                if let choice {
                    Text("“\(choice)” is one possibility. It is also okay not to know.")
                        .font(.footnote.weight(.medium))
                        .multilineTextAlignment(.center)
                        .accessibilityAddTraits(.updatesFrequently)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(Color.cardInner, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            VStack(spacing: 8) {
                ForEach(feelingOptions, id: \.name) { feeling in
                    Button {
                        choice = feeling.name
                    } label: {
                        HStack(spacing: 12) {
                            Text(feeling.emoji).font(.title2).accessibilityHidden(true)
                            Text(feeling.name).font(.body.weight(.semibold))
                            Spacer()
                            if choice == feeling.name { Image(systemName: "checkmark.circle.fill") }
                        }
                        .frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                        .padding(.horizontal, 12)
                        .background(Color.cardInner, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(feeling.name)
                    .accessibilityValue(choice == feeling.name ? "Selected" : "")
                }
            }
            Button {
                pictureIndex = (pictureIndex + 1) % feelingPictures.count
                choice = nil
            } label: {
                Label(AppLanguage.text("Next picture"), systemImage: "arrow.right")
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
            }
            .buttonStyle(.borderedProminent)
            .tint(.emerald)
        }
    }
}


struct PatternRecallCard: View {
    @State private var length = 2
    @State private var sequence = [1, 2]
    @State private var inputIndex = 0
    @State private var showGuide = true
    @State private var started = false
    @State private var message = "Choose a length, then start when you are ready."
    private var complete: Bool { started && inputIndex == sequence.count }

    var body: some View {
        SectionCard(title: AppLanguage.text("Remember a pattern"), systemImage: "hand.tap") {
            Text(AppLanguage.text("Tap the numbered pads in the order shown. Keep the guide visible or hide it if you want to try recalling. There is no timer or flashing playback."))
                .foregroundStyle(.secondary)
            Picker(AppLanguage.text("Pattern length"), selection: $length) {
                ForEach(1...4, id: \.self) { Text("\($0) \($0 == 1 ? "step" : "steps")").tag($0) }
            }
            .pickerStyle(.menu)
            .onChange(of: length) { _ in started = false; inputIndex = 0; message = "Start a new pattern when you are ready." }
            if started {
                Toggle(AppLanguage.text("Keep the pattern visible"), isOn: $showGuide)
                if showGuide {
                    Text(sequence.map(String.init).joined(separator: " → "))
                        .font(.title2.weight(.bold))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(Color.cardInner, in: RoundedRectangle(cornerRadius: 14))
                        .accessibilityLabel(AppLanguage.format("Pattern: {0}", [sequence.map(String.init).joined(separator: ", then ")]))
                }
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    ForEach(1...4, id: \.self) { pad in
                        Button { choose(pad) } label: {
                            Text("\(pad)").font(.title.weight(.bold))
                                .frame(maxWidth: .infinity, minHeight: 76)
                                .background(Color.cardInner, in: RoundedRectangle(cornerRadius: 16))
                                .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(Color.secondary.opacity(0.5)))
                        }
                        .buttonStyle(.plain)
                        .disabled(complete)
                        .accessibilityLabel("Pattern pad \(pad)")
                    }
                }
            }
            ActivityFeedback(text: message)
            Button {
                sequence = (0..<length).map { _ in Int.random(in: 1...4) }
                started = true; showGuide = true; inputIndex = 0
                message = "Tap step 1. You can use the visible guide."
            } label: {
                Label(started ? "New pattern" : "Start pattern", systemImage: "play")
                    .frame(maxWidth: .infinity, minHeight: 44)
            }
            .buttonStyle(.borderedProminent)
            if started {
                Button(AppLanguage.text("Try the same pattern again")) {
                    inputIndex = 0; showGuide = true
                    message = "The same pattern is ready. Start with step 1."
                }.buttonStyle(.bordered).controlSize(.large)
            }
        }
    }

    private func choose(_ pad: Int) {
        guard started, !complete else { return }
        if sequence[inputIndex] == pad {
            inputIndex += 1
            message = complete ? "Pattern completed. Repeat, try another, or leave whenever you like." : "Step added. Choose step \(inputIndex + 1)."
        } else {
            showGuide = true
            message = "The guide is visible. For step \(inputIndex + 1), choose \(sequence[inputIndex])."
        }
    }
}


private struct EverydayPicture: Identifiable {
    let id = UUID()
    let pairID: String
    let label: String
    let emoji: String
    let isFree: Bool

    init(pairID: String, label: String, emoji: String, isFree: Bool = false) {
        self.pairID = pairID
        self.label = label
        self.emoji = emoji
        self.isFree = isFree
    }
}

private let pictureThemes: [String: [(pairID: String, label: String, emoji: String)]] = [
    "Nature": [("leaf", "Leaf", "🍃"), ("flower", "Flower", "🌼"), ("bird", "Bird", "🐦"), ("sun", "Sun", "☀️"), ("cloud", "Cloud", "☁️"), ("river", "River", "🌊"), ("acorn", "Acorn", "🌰"), ("mushroom", "Mushroom", "🍄"), ("seedling", "Seedling", "🌱"), ("tree", "Tree", "🌳"), ("palm", "Palm tree", "🌴"), ("cactus", "Cactus", "🌵")],
    "Home": [("cup", "Cup", "☕"), ("book", "Book", "📘"), ("lamp", "Lamp", "💡"), ("chair", "Chair", "🪑"), ("key", "Key", "🔑"), ("clock", "Clock", "🕰️"), ("plate", "Plate", "🍽️"), ("home", "House", "🏠"), ("bed", "Bed", "🛏️"), ("broom", "Broom", "🧹"), ("soap", "Soap", "🧼"), ("toolbox", "Toolbox", "🧰")],
    "Food": [("apple", "Apple", "🍎"), ("bread", "Bread", "🍞"), ("pear", "Pear", "🍐"), ("soup", "Soup", "🥣"), ("rice", "Rice", "🍚"), ("banana", "Banana", "🍌"), ("carrot", "Carrot", "🥕"), ("cheese", "Cheese", "🧀"), ("corn", "Corn", "🌽"), ("orange", "Orange", "🍊"), ("strawberry", "Strawberry", "🍓"), ("egg", "Egg", "🥚")]
]

private func makeEverydayPictureDeck(pairCount: Int, theme: String) -> [EverydayPicture] {
    var cards = Array((pictureThemes[theme] ?? pictureThemes["Nature"]!).prefix(pairCount)).flatMap { item in
        [EverydayPicture(pairID: item.pairID, label: item.label, emoji: item.emoji),
         EverydayPicture(pairID: item.pairID, label: item.label, emoji: item.emoji)]
    }.shuffled()
    if pairCount == 12 {
        cards.insert(EverydayPicture(pairID: "free-space", label: "Free space", emoji: "✦", isFree: true), at: 12)
    }
    return cards
}

struct PicturePairsCard: View {
    @AppStorage("CareSphere.gameLevel") private var gameLevel = "gentle"
    @State private var revealAll = false
    @State private var pairCount: Int
    @State private var theme = "Nature"
    @State private var cards: [EverydayPicture]
    @State private var opened: [UUID] = []
    @State private var matched: Set<String> = []
    @State private var message = "Choose two picture cards. You can take your time."

    init() {
        let level = UserDefaults.standard.string(forKey: "CareSphere.gameLevel") ?? "gentle"
        let count = level == "challenge" ? 12 : level == "steady" ? 4 : 2
        _pairCount = State(initialValue: count)
        _cards = State(initialValue: makeEverydayPictureDeck(pairCount: count, theme: "Nature"))
    }

    var body: some View {
        SectionCard(title: AppLanguage.text("Picture pairs"), systemImage: "square.grid.2x2") {
            Text(AppLanguage.text("Match familiar picture symbols at your own pace. There is no timer or score; unmatched cards stay open until you choose to hide them. This is an optional game, not a memory test."))
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Picker(AppLanguage.text("Picture theme"), selection: $theme) {
                ForEach(["Nature", "Home", "Food"], id: \.self) { Text(AppLanguage.text($0)).tag($0) }
            }
            .pickerStyle(.menu)
            .onChange(of: theme) { _ in reset() }
            Picker(AppLanguage.text("Number of pairs"), selection: $pairCount) {
                ForEach([2, 4, 6, 8, 12], id: \.self) { count in
                    Text(AppLanguage.text(count == 12 ? "12 pairs · 5×5" : AppLanguage.format("{0} pairs", ["\(count)"]))).tag(count)
                }
            }
            .pickerStyle(.menu)
            .onChange(of: pairCount) { _ in reset() }
            if pairCount == 12 {
                Text(AppLanguage.text("The 5×5 board has twelve pairs and one open center tile."))
                    .font(.footnote).foregroundStyle(.secondary)
            }

            Toggle(AppLanguage.text("Keep all pictures visible"), isOn: $revealAll)
            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: pairCount == 12 ? 5 : pairCount <= 2 ? 2 : 4), spacing: 8) {
                ForEach(cards.indices, id: \.self) { index in
                    let card = cards[index]
                    let isMatched = card.isFree || matched.contains(card.pairID)
                    let isOpen = card.isFree || revealAll || opened.contains(card.id) || isMatched
                    Button {
                        reveal(card)
                    } label: {
                        VStack(spacing: 6) {
                            Text(isOpen ? card.emoji : "▢")
                                .font(.largeTitle)
                                .accessibilityHidden(true)
                            if card.isFree {
                                Label(AppLanguage.text("Free space"), systemImage: "sparkle").font(.caption)
                            } else if isMatched {
                                Label(AppLanguage.text("Matched"), systemImage: "checkmark.circle.fill").font(.caption)
                            } else if opened.contains(card.id) {
                                Label(AppLanguage.text("Chosen"), systemImage: "checkmark.circle").font(.caption)
                            }
                            Text(isOpen ? AppLanguage.text(card.label) : AppLanguage.format("Card {0}", ["\(index + 1)"]))
                                .font(.caption.weight(.semibold))
                                .multilineTextAlignment(.center)
                        }
                        .frame(maxWidth: .infinity, minHeight: 72)
                        .background(isOpen ? Color.emerald.opacity(0.18) : Color.cardInner,
                                    in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 14, style: .continuous)
                                .strokeBorder(isMatched || opened.contains(card.id) ? Color.emeraldText : Color.secondary.opacity(0.3), lineWidth: isMatched || opened.contains(card.id) ? 2 : 1))
                    }
                    .buttonStyle(.plain)
                    .disabled(isMatched || opened.count == 2)
                    .accessibilityLabel(isOpen ? AppLanguage.format(isMatched && !card.isFree ? "{0}, card {1}, pair found" : "{0}, card {1}", [AppLanguage.text(card.label), "\(index + 1)"]) : AppLanguage.format("Hidden picture card {0}", ["\(index + 1)"]))
                    .accessibilityValue(opened.contains(card.id) ? AppLanguage.text("Chosen") : "")
                    .accessibilityHint(AppLanguage.text(card.isFree ? "Free space" : isMatched ? "Pair found." : "Double-tap to choose this card."))
                }
            }
            Text(AppLanguage.text(message))
                .font(.footnote)
                .frame(maxWidth: .infinity, minHeight: 42, alignment: .leading)
                .accessibilityAddTraits(.updatesFrequently)

            HStack {
                if canHideCards {
                    Button(AppLanguage.text(revealAll ? "Clear selection" : "Hide cards")) {
                        opened = []
                        message = "Selection cleared. Choose two when you are ready."
                    }
                    .buttonStyle(.bordered)
                }
                Button(AppLanguage.text("Start over"), action: reset)
                    .buttonStyle(.borderedProminent)
                    .tint(.emerald)
            }
        }
    }

    private var canHideCards: Bool {
        guard opened.count == 2,
              let first = cards.first(where: { $0.id == opened[0] }) else { return false }
        return !matched.contains(first.pairID)
    }

    private func reveal(_ card: EverydayPicture) {
        guard !card.isFree, opened.count < 2, !matched.contains(card.pairID), !opened.contains(card.id) else { return }
        opened.append(card.id)
        guard opened.count == 2,
              let first = cards.first(where: { $0.id == opened[0] }) else { return }
        if first.pairID == card.pairID {
            matched.insert(card.pairID)
            opened = []
            message = matched.count == pairCount
                ? "All pairs are visible. Start again or choose a different number of pairs."
                : "Pair found. Take your time with the next one."
        } else {
            message = "These are different pictures. Clear the selection or hide the cards when you are ready."
        }
    }

    private func reset() {
        cards = makeEverydayPictureDeck(pairCount: pairCount, theme: theme)
        opened = []
        matched = []
        message = "Choose two picture cards to look for a pair."
    }
}


struct PicturePatternRecallCard: View {
    @State private var patternSize = 3
    @State private var patternIDs: Set<String> = []
    @State private var chosenIDs: Set<String> = []
    @State private var patternVisible = true
    @State private var started = false
    @State private var message = "Start a picture pattern whenever you are ready."

    private var pictures: [(pairID: String, label: String, emoji: String)] { pictureThemes["Nature"] ?? [] }
    private var matched: Bool { started && !patternVisible && chosenIDs == patternIDs && !patternIDs.isEmpty }

    init() {
        let level = UserDefaults.standard.string(forKey: "CareSphere.gameLevel") ?? "gentle"
        _patternSize = State(initialValue: level == "challenge" ? 6 : level == "steady" ? 4 : 3)
    }

    var body: some View {
        SectionCard(title: AppLanguage.text("Picture pattern recall"), systemImage: "square.grid.3x3") {
            Text(AppLanguage.text("Look at the picture pattern, hide it when ready, then choose the same pictures. You can reveal it again at any time."))
                .foregroundStyle(.secondary)
            Picker(AppLanguage.text("Pattern size"), selection: $patternSize) {
                ForEach([3, 4, 6], id: \.self) { count in
                    Text(AppLanguage.format("{0} pictures", ["\(count)"])).tag(count)
                }
            }
            .pickerStyle(.menu)
            .onChange(of: patternSize) { _ in
                started = false
                patternIDs = []
                chosenIDs = []
                message = "Start a picture pattern whenever you are ready."
            }
            if started {
                Button(AppLanguage.text(patternVisible ? "Hide pattern" : "Show pattern")) {
                    patternVisible.toggle()
                    chosenIDs = []
                    message = patternVisible ? "The picture pattern is visible. Hide it when you are ready." : "Pattern hidden. Select the same pictures."
                }
                .buttonStyle(.bordered)
            }
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 6), count: 4), spacing: 6) {
                ForEach(pictures.indices, id: \.self) { index in
                    let picture = pictures[index]
                    let isVisible = started && patternVisible && patternIDs.contains(picture.pairID)
                    let isChosen = chosenIDs.contains(picture.pairID)
                    Button {
                        choose(picture.pairID)
                    } label: {
                        VStack(spacing: 4) {
                            Text(isVisible || isChosen ? picture.emoji : "✧")
                                .font(.title2)
                                .accessibilityHidden(true)
                            Text(isVisible || isChosen ? AppLanguage.text(picture.label) : AppLanguage.format("Card {0}", ["\(index + 1)"]))
                                .font(.caption.weight(.semibold))
                                .multilineTextAlignment(.center)
                        }
                        .frame(maxWidth: .infinity, minHeight: 72)
                        .background(isVisible || isChosen ? Color.emerald.opacity(0.2) : Color.cardInner, in: RoundedRectangle(cornerRadius: 12))
                        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(isVisible || isChosen ? Color.emeraldText : Color.secondary.opacity(0.35), lineWidth: isVisible || isChosen ? 2 : 1))
                    }
                    .buttonStyle(.plain)
                    .disabled(!started || patternVisible || matched)
                    .accessibilityLabel(isVisible || isChosen ? AppLanguage.text(picture.label) : AppLanguage.format("Picture choice {0}", ["\(index + 1)"]))
                    .accessibilityValue(isChosen ? AppLanguage.text("Selected") : "")
                    .accessibilityHint(AppLanguage.text("Double-tap to choose this card."))
                }
            }
            .accessibilityElement(children: .contain)
            .accessibilityLabel(started && patternVisible
                ? AppLanguage.format("Pattern: {0}", [pictures.filter { patternIDs.contains($0.pairID) }.map { AppLanguage.text($0.label) }.joined(separator: ", ")])
                : AppLanguage.text("Picture choices"))
            ActivityFeedback(text: message)
            HStack {
                if !patternVisible && !chosenIDs.isEmpty {
                    Button(AppLanguage.text("Clear choices")) {
                        chosenIDs = []
                        message = "Choices cleared. Select the pictures whenever you are ready."
                    }.buttonStyle(.bordered)
                }
                Button(AppLanguage.text("Start a new pattern")) {
                    patternIDs = Set(pictures.shuffled().prefix(patternSize).map { $0.pairID })
                    chosenIDs = []
                    patternVisible = true
                    started = true
                    message = "The picture pattern is visible. Hide it when you are ready."
                }
                .buttonStyle(.borderedProminent).tint(.emerald)
            }
        }
    }

    private func choose(_ id: String) {
        guard started, !patternVisible, !matched else { return }
        if chosenIDs.contains(id) {
            chosenIDs.remove(id)
        } else if chosenIDs.count < patternSize {
            chosenIDs.insert(id)
        }
        guard chosenIDs.count == patternSize else { return }
        message = chosenIDs == patternIDs
            ? "Pattern matched. You can start again whenever you like."
            : "That is different. Show the pattern or change your choices."
    }
}

private struct PictureStoryStep: Identifiable {
    let id: String
    let label: String
    let emoji: String
}

private struct PictureStorySequence: Identifiable {
    let id: String
    let title: String
    let steps: [PictureStoryStep]
}

private let pictureStories = [
    PictureStorySequence(id: "plant", title: "A seed grows", steps: [
        PictureStoryStep(id: "seed", label: "Seed", emoji: "🫘"),
        PictureStoryStep(id: "sprout", label: "Sprout", emoji: "🌱"),
        PictureStoryStep(id: "flower", label: "Flower", emoji: "🌼"),
    ]),
    PictureStorySequence(id: "hands", title: "Wash hands", steps: [
        PictureStoryStep(id: "wet", label: "Wet hands", emoji: "🚰"),
        PictureStoryStep(id: "soap", label: "Use soap", emoji: "🧼"),
        PictureStoryStep(id: "rinse", label: "Rinse", emoji: "💧"),
    ]),
]

struct PictureStoryCard: View {
    @State private var storyIndex = 0
    @State private var stepIndex = 0
    private var story: PictureStorySequence { pictureStories[storyIndex] }
    private var step: PictureStoryStep { story.steps[stepIndex] }

    var body: some View {
        SectionCard(title: AppLanguage.text("Picture story"), systemImage: "text.book.closed") {
            InfoText(AppLanguage.text("Explore a short, predictable picture sequence. These are examples, not instructions—people’s routines and preferences differ. Move forward or back whenever you like."))
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Picker(AppLanguage.text("Choose a story"), selection: $storyIndex) {
                ForEach(pictureStories.indices, id: \.self) { index in
                    Text(pictureStories[index].title).tag(index)
                }
            }
            .onChange(of: storyIndex) { _ in stepIndex = 0 }

            VStack(spacing: 10) {
                Text("Picture \(stepIndex + 1) of \(story.steps.count)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(step.emoji)
                    .font(.system(size: 48))
                    .accessibilityHidden(true)
                Text(step.label)
                    .font(.title3.weight(.bold))
            }
            .frame(maxWidth: .infinity, minHeight: 148)
            .background(Color.cardInner, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .accessibilityElement(children: .combine)

            HStack {
                Button {
                    stepIndex = max(0, stepIndex - 1)
                } label: {
                    Label(AppLanguage.text("Previous picture"), systemImage: "arrow.left")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .disabled(stepIndex == 0)

                Button {
                    stepIndex = stepIndex == story.steps.count - 1 ? 0 : stepIndex + 1
                } label: {
                    Label(stepIndex == story.steps.count - 1 ? "Start again" : "Next picture", systemImage: "arrow.right")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .tint(.emerald)
            }
        }
    }
}


struct SoundscapesCard: View {
    @Environment(\.scenePhase) private var scenePhase
    @State private var playing: SoundScapeEngine.SoundId?
    @State private var audioError: String?
    private let engine = SoundScapeEngine.shared

    var body: some View {
        SectionCard(title: AppLanguage.text("Sensory Soundscapes"), systemImage: "speaker.wave.2.fill") {
            InfoText(AppLanguage.text("Generated on-device and available offline. Start at a comfortable volume. Sound respects Silent mode; you can stop anytime."))
                .font(.caption)
                .foregroundStyle(.secondary)
            if let audioError {
                Text(audioError).font(.subheadline).foregroundStyle(.secondary)
            }
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                ForEach(SoundScapeEngine.SoundId.allCases) { sound in
                    soundscapeButton(sound)
                }
            }
        }
        .onDisappear { engine.stop(); playing = nil }
        .onChange(of: scenePhase) { phase in
            if phase != .active { engine.stop(); playing = nil }
        }
    }

    private func soundscapeButton(_ sound: SoundScapeEngine.SoundId) -> some View {
        let active = playing == sound
        return Button {
            audioError = nil
            if active {
                engine.stop()
                playing = nil
            } else if engine.play(sound) {
                playing = sound
            } else {
                playing = nil
                audioError = "Audio could not start. Check your audio output and try again. Other activities remain available."
            }
        } label: {
            HStack(spacing: 8) {
                Text(sound.emoji)
                VStack(alignment: .leading, spacing: 1) {
                    Text(sound.title).font(.subheadline.weight(.bold))
                    Text(active ? "● Playing" : "Tap to play")
                        .font(.caption2)
                        .foregroundStyle(active ? Color.emeraldText : Color.secondary)
                }
                Spacer()
            }
            .padding(12)
            .background(active ? Color.emerald.opacity(0.18) : Color.cardInner,
                        in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .strokeBorder(active ? Color.emerald.opacity(0.6) : .clear, lineWidth: 2))
        }
    }
}


struct BreathingCard: View {
    var body: some View {
        SectionCard(title: AppLanguage.text("Paced breathing"), systemImage: "wind") {
            PacedBreathingActivity()
        }
    }
}

struct PacedBreathingActivity: View {
    let targetCycles: Int?
    let onComplete: () -> Void
    let onStop: () -> Void

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @EnvironmentObject private var store: CareStore
    @Environment(\.scenePhase) private var scenePhase
    @State private var includeHold = false
    private var phases: [(name: String, seconds: Int, scale: CGFloat)] {
        includeHold
            ? [("Breathe in", 4, 1.0), ("Hold", 4, 1.0), ("Breathe out", 6, 0.55)]
            : [("Breathe in", 4, 1.0), ("Breathe out", 6, 0.55)]
    }
    private var currentPhase: (name: String, seconds: Int, scale: CGFloat) {
        phases[min(phaseIndex, phases.count - 1)]
    }
    @State private var running = false
    @State private var phaseIndex = 0
    @State private var count = 4
    @State private var cycles = 0
    @State private var breathScale: CGFloat = 1.0

    init(
        targetCycles: Int? = nil,
        onComplete: @escaping () -> Void = {},
        onStop: @escaping () -> Void = {}
    ) {
        self.targetCycles = targetCycles
        self.onComplete = onComplete
        self.onStop = onStop
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(AppLanguage.text("Breathe naturally if this pace is not comfortable. Stop if you feel dizzy or short of breath. This does not measure breathing."))
                .font(.caption)
                .foregroundStyle(.secondary)
            Toggle(AppLanguage.text("Include a 4-second hold"), isOn: $includeHold)
                .disabled(running)
                .onChange(of: includeHold) { _ in phaseIndex = 0; count = 4 }
            HStack {
                Spacer()
                ZStack {
                    Circle()
                        .fill(Color.teal.opacity(0.22))
                        .frame(width: 128, height: 128)
                        .scaleEffect((reduceMotion || store.reduceVisualMotion) ? 1.0 : breathScale)
                        .animation((reduceMotion || store.reduceVisualMotion) ? nil : .easeInOut(duration: Double(currentPhase.seconds)), value: breathScale)
                    VStack(spacing: 2) {
                        Text("\(count)").font(.title.weight(.black))
                            .accessibilityAddTraits(.updatesFrequently)
                        Text(running ? currentPhase.name.uppercased() : "PAUSED")
                            .font(.caption2.weight(.bold))
                            .foregroundStyle(.teal)
                    }
                }
                Spacer()
            }
            .padding(.vertical, 4)
            HStack {
                StatusChip(text: "\(cycles) cycles", color: .teal)
                Spacer()
                Button {
                    if running {
                        running = false
                    } else {
                        phaseIndex = 0
                        count = phases[0].seconds
                        breathScale = phases[0].scale
                        if targetCycles != nil { cycles = 0 }
                        running = true
                    }
                } label: {
                    Label(running ? "Pause" : "Begin", systemImage: running ? "pause.fill" : "play.fill")
                        .font(.subheadline.weight(.black))
                        .padding(.horizontal, 22)
                        .padding(.vertical, 10)
                }
                .buttonStyle(.borderedProminent)
            }
            if targetCycles != nil, running {
                Button {
                    running = false
                    onStop()
                } label: {
                    Label(AppLanguage.text("Stop activity"), systemImage: "stop.fill")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                }
                .buttonStyle(.bordered)
            }
        }
        .task(id: running) {
            guard running else { return }
            while running && !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard running, !Task.isCancelled else { return }
                if count > 1 {
                    count -= 1
                } else {
                    phaseIndex = (phaseIndex + 1) % phases.count
                    if phaseIndex == 0 {
                        cycles += 1
                        if let targetCycles, cycles >= targetCycles {
                            running = false
                            onComplete()
                            return
                        }
                    }
                    count = currentPhase.seconds
                    breathScale = currentPhase.scale
                }
            }
        }
        .onDisappear { running = false }
        .onChange(of: scenePhase) { phase in
            if phase != .active { running = false }
        }
    }
}
