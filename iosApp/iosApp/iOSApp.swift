import SwiftUI
import AnchorDi

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
