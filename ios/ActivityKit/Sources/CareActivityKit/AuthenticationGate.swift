import Foundation


public enum AuthenticationGate {
    public static func accepts(canEvaluate: Bool, verified: Bool, currentAttempt: Bool = true, foreground: Bool = true) -> Bool {
        canEvaluate && verified && currentAttempt && foreground
    }
}
