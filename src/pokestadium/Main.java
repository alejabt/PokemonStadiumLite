package pokestadium;

import pokestadium.ui.BattleWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Toda la interfaz de Swing debe crearse en el hilo de Swing (EDT),
        // por eso se usa invokeLater en lugar de crear la ventana aquí directamente.
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pokémon Stadium Lite");
            frame.setContentPane(new BattleWindow().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();                       // tamaño según el contenido del .form
            frame.setLocationRelativeTo(null);  // centrada en la pantalla
            frame.setVisible(true);
        });
    }
}
