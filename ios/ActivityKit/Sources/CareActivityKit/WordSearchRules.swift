import Foundation

public enum WordSearchRules {
    public static func matchedWord(_ selected: String, in words: [String]) -> String? {
        let selection = normalize(selected)
        return words.first { word in
            let candidate = normalize(word)
            return candidate == selection || String(candidate.reversed()) == selection
        }
    }

    private static func normalize(_ word: String) -> String {
        word.uppercased().filter { !$0.isWhitespace && $0 != "-" }
    }
}
