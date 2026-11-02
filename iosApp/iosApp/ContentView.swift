import SwiftUI
import UIKit
import SduiRenderer

/// A casca do app: embute o renderer do SDUI (Compose Multiplatform) num UIViewController.
/// Tela nova não muda este código; só o BFF.
struct ComposeView: UIViewControllerRepresentable {
    // No simulador, localhost é o computador. Num aparelho, use o IP da máquina do BFF.
    let baseUrl = "http://localhost:8080"

    func makeUIViewController(context: Context) -> UIViewController {
        SduiViewControllerKt.SduiViewController(baseUrl: baseUrl, startRoute: "home")
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // O Scaffold do material3 cuida das áreas seguras e do teclado.
        ComposeView().ignoresSafeArea()
    }
}
