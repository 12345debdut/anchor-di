import SwiftUI
import AnchorDI

@main
struct iOSApp: App {
    init() {
        AnchorDISdk.shared.doInit()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
