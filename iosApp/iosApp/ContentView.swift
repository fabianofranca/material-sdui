import SwiftUI
import UIKit
import SduiRenderer

/// A casca do app: embute o renderer do SDUI (Compose Multiplatform) num UIViewController.
/// Tela nova não muda este código; só o BFF.
struct ComposeView: UIViewControllerRepresentable {
    /// O BFF publicado (deploy/lightsail). Para o BFF local no simulador: "http://localhost:8080".
    let baseUrl = "https://bff.fabianofranca.com"

    /// A primeira tela. A variável SDUI_START_ROUTE abre outra (o CI usa para as capturas):
    /// SIMCTL_CHILD_SDUI_START_ROUTE=settings xcrun simctl launch booted com.fabianofranca.sdui.ios
    let startRoute = ProcessInfo.processInfo.environment["SDUI_START_ROUTE"] ?? "home"

    func makeUIViewController(context: Context) -> UIViewController {
        SduiViewControllerKt.SduiViewController(baseUrl: baseUrl, startRoute: startRoute)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // O Scaffold do material3 cuida das áreas seguras e do teclado.
        ComposeView().ignoresSafeArea()
    }
}
