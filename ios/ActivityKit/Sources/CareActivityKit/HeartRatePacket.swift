import Foundation


public enum HeartRatePacket {
    public static func bpm(from bytes: [UInt8]) -> Int? {
        guard bytes.count >= 2 else { return nil }
        let flags = bytes[0]
        guard flags & 4 == 0 || flags & 2 != 0 else { return nil }
        let value: Int
        if flags & 1 != 0 {
            guard bytes.count >= 3 else { return nil }
            value = Int(bytes[1]) | (Int(bytes[2]) << 8)
        } else {
            value = Int(bytes[1])
        }
        return (21...249).contains(value) ? value : nil
    }
}
