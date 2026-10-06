package pokestadium.ui;

import pokestadium.api.PokeApiClient;
import pokestadium.api.PokemonNotFoundException;
import pokestadium.battle.Battle;
import pokestadium.battle.BattleListener;
import pokestadium.model.Pokemon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

// Ventana principal del juego.
// El diseño se hizo en BattleWindow.form, por eso los botones y etiquetas no se crean con "new".
// Los Pokémon se cargan en segundo plano (SwingWorker) para que la ventana no se congele.
// Battle le avisa a esta ventana lo que pasa en el combate (BattleListener) y aquí se muestra.

public class BattleWindow implements BattleListener {

    private static final long TURN_DELAY_MS = 700; // pausa entre turnos del combate

    //Componentes del formulario creados por GUI Designer)
    private JPanel mainPanel;

    private JTextField nameField1;
    private JButton loadButton1;
    private JButton randomButton1;
    private JLabel spriteLabel1;
    private JLabel nameLabel1;
    private JLabel typesLabel1;
    private JLabel statsLabel1;
    private JProgressBar hpBar1;

    private JTextField nameField2;
    private JButton loadButton2;
    private JButton randomButton2;
    private JLabel spriteLabel2;
    private JLabel nameLabel2;
    private JLabel typesLabel2;
    private JLabel statsLabel2;
    private JProgressBar hpBar2;

    private JButton fightButton;
    private JTextArea logArea;

    // Lógica de la ventana
    private final PokeApiClient api = new PokeApiClient();
    private final PlayerSide side1;
    private final PlayerSide side2;

    private boolean fighting = false; // true mientras corre un combate
    private String battleName1;       // nombre que usa Battle para el Jugador 1
    private String battleName2;       // y para el Jugador 2

    public BattleWindow() {
        // Agrupamos los componentes de cada jugador para no repetir código
        side1 = new PlayerSide(nameField1, loadButton1, randomButton1,
                spriteLabel1, nameLabel1, typesLabel1, statsLabel1, hpBar1);
        side2 = new PlayerSide(nameField2, loadButton2, randomButton2,
                spriteLabel2, nameLabel2, typesLabel2, statsLabel2, hpBar2);

        // Qué pasa al hacer clic en cada botón
        loadButton1.addActionListener(e -> loadPokemon(side1, false));
        randomButton1.addActionListener(e -> loadPokemon(side1, true));
        nameField1.addActionListener(e -> loadPokemon(side1, false)); // Enter = Load

        loadButton2.addActionListener(e -> loadPokemon(side2, false));
        randomButton2.addActionListener(e -> loadPokemon(side2, true));
        nameField2.addActionListener(e -> loadPokemon(side2, false));

        fightButton.addActionListener(e -> startBattle());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    // =====================================================================
    //  Carga de Pokémon
    // =====================================================================

    /*
     * Pide un Pokémon a la API sin congelar la ventana.
     * random = true  -> Pokémon al azar
     * random = false -> el nombre escrito en la caja de texto
     */
    private void loadPokemon(PlayerSide side, boolean random) {
        String name = side.nameField.getText().trim();
        if (!random && name.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Escribe el nombre de un Pokémon.",
                    "Falta el nombre", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Mientras carga, los botones de ESTE lado (y Fight!) quedan deshabilitados
        side.loading = true;
        side.setButtonsEnabled(false);
        side.showLoading();
        updateFightButton();

        // SwingWorker: doInBackground() corre en otro hilo y done() en el hilo de Swing
        SwingWorker<Pokemon, Void> worker = new SwingWorker<Pokemon, Void>() {

            private ImageIcon sprite; // se descarga junto con los datos

            // 1) Hilo aparte: aquí va lo lento (las peticiones a internet).
            //    NO se debe tocar la interfaz desde aquí.
            @Override
            protected Pokemon doInBackground() throws Exception {
                Pokemon pokemon = random ? api.fetchRandom() : api.fetchByName(name);
                sprite = downloadSprite(pokemon.getSpriteUrl());
                return pokemon;
            }

            // 2) Hilo de Swing: se ejecuta cuando doInBackground() termina.
            //    Aquí sí se puede actualizar la interfaz.
            @Override
            protected void done() {
                try {
                    Pokemon pokemon = get(); // si doInBackground falló, get() lanza la excepción
                    side.pokemon = pokemon;
                    side.showPokemon(pokemon, sprite);
                } catch (ExecutionException e) {
                    side.showCurrentHp(); // vuelve a mostrar lo que había antes
                    showLoadError(e.getCause(), name, random);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    side.loading = false;
                    side.setButtonsEnabled(true);
                    updateFightButton();
                }
            }
        };
        worker.execute(); // arranca el hilo y regresa de inmediato
    }

    // Descarga la imagen del Pokémon. Si no tiene o falla, devuelve null (no es grave)
    private ImageIcon downloadSprite(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(URI.create(url).toURL());
            return image == null ? null : new ImageIcon(image);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    /* Muestra un mensaje distinto según el tipo de error. */
    private void showLoadError(Throwable error, String name, boolean random) {
        if (error instanceof PokemonNotFoundException) {
            String message = random
                    ? "La API no encontró el Pokémon elegido al azar.\nPulsa Random otra vez."
                    : "No existe ningún Pokémon llamado \"" + name + "\".\n"
                    + "Revisa que esté bien escrito (en inglés, por ejemplo: pikachu).";
            JOptionPane.showMessageDialog(mainPanel, message,
                    "Pokémon no encontrado", JOptionPane.WARNING_MESSAGE);

        } else if (error instanceof IOException) {
            String detail = error.getMessage() != null
                    ? error.getMessage() : error.getClass().getSimpleName();
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudo conectar con la PokeAPI.\n"
                            + "Revisa tu conexión a internet e inténtalo de nuevo.\n\n"
                            + "Detalle: " + detail,
                    "Error de red", JOptionPane.ERROR_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(mainPanel, "Ocurrió un error inesperado:\n" + error,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    //  Combate
    // =====================================================================

    // Fight! solo se puede pulsar si los dos Pokémon están cargados y no hay nada en curso.
    private void updateFightButton() {
        boolean ready = side1.pokemon != null && side2.pokemon != null
                && !side1.loading && !side2.loading
                && !fighting;
        fightButton.setEnabled(ready);
    }

    // Arranca el combate en segundo plano.
    private void startBattle() {
        fighting = true;
        side1.setButtonsEnabled(false);
        side2.setButtonsEnabled(false);
        fightButton.setEnabled(false);
        logArea.setText("");

        // "this" es el listener: Battle llamará a onTurn, onHpChanged y onBattleEnded de esta clase
        Battle battle = new Battle(side1.pokemon, side2.pokemon, this, TURN_DELAY_MS);
        battleName1 = battle.getName1();
        battleName2 = battle.getName2();
        appendLog("¡Comienza el combate! " + battleName1 + " vs " + battleName2);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            // Hilo aparte: fight() tarda varios segundos por las pausas entre turnos.
            // Si corriera en el hilo de Swing, la ventana se congelaría hasta el final.
            @Override
            protected Void doInBackground() throws Exception {
                battle.fight();
                return null;
            }

            // El final normal del combate se muestra en onBattleEnded.
            // Aquí solo se atiende el caso de que algo falle inesperadamente.
            @Override
            protected void done() {
                try {
                    get();
                } catch (ExecutionException e) {
                    appendLog("El combate se detuvo por un error: " + e.getCause());
                    JOptionPane.showMessageDialog(mainPanel, "El combate se detuvo por un error:\n" + e.getCause(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    finishBattle();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    finishBattle();
                }
            }
        };
        worker.execute();
    }

    // Deja la ventana lista para una revancha o para cambiar de Pokémon.
    private void finishBattle() {
        fighting = false;
        side1.setButtonsEnabled(true);
        side2.setButtonsEnabled(true);
        updateFightButton();
    }

    // Agrega una línea al log y baja el scroll hasta el final.
    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // ----- Eventos de BattleListener -----
    // Battle llama a estos métodos desde el hilo del SwingWorker, NO desde el hilo de Swing.
    // Swing solo se puede tocar desde su propio hilo, así que cada método envuelve su
    // trabajo en SwingUtilities.invokeLater: "ejecuta esto en el hilo de Swing apenas puedas".

    @Override
    public void onTurn(String attacker, String defender, int damage,
                       boolean critical, double modifier) {
        SwingUtilities.invokeLater(() -> {
            String line = attacker + " ataca a " + defender + " y hace " + damage + " de daño.";
            if (critical) {
                line += " ¡Golpe crítico!";
            }
            if (modifier > 1.0) {
                line += " ¡Es súper efectivo! (x" + modifier + ")";
            } else if (modifier < 1.0) {
                line += " No es muy efectivo... (x" + modifier + ")";
            }
            appendLog(line);
        });
    }

    @Override
    public void onHpChanged(String pokemon, int hpActual) {
        SwingUtilities.invokeLater(() -> {
            // Battle avisa con el nombre; con él sabemos de qué lado es
            PlayerSide side = pokemon.equals(battleName1) ? side1 : side2;
            side.showHp(hpActual);
            appendLog("    HP de " + pokemon + ": " + hpActual + " / " + side.pokemon.getMaxHp());
        });
    }

    @Override
    public void onBattleEnded(String winner) {
        SwingUtilities.invokeLater(() -> {
            String player = winner.equals(battleName1) ? "Jugador 1" : "Jugador 2";
            String message = "¡" + winner + " gana el combate! (" + player + ")";
            appendLog("");
            appendLog(message);

            finishBattle(); // se habilitan los botones para la revancha
            JOptionPane.showMessageDialog(mainPanel, message,
                    "Fin del combate", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    // =====================================================================
    //  Un lado de la pantalla (Jugador 1 o Jugador 2)
    // =====================================================================

    /*
     * Agrupa los componentes y el Pokémon de UN jugador.
     * Así el mismo código sirve para el lado izquierdo y el derecho.
     */
    private static class PlayerSide {
        final JTextField nameField;
        final JButton loadButton;
        final JButton randomButton;
        final JLabel spriteLabel;
        final JLabel nameLabel;
        final JLabel typesLabel;
        final JLabel statsLabel;
        final JProgressBar hpBar;

        Pokemon pokemon;  // null mientras no se haya cargado ninguno
        boolean loading;  // true mientras se espera la respuesta de la API

        PlayerSide(JTextField nameField, JButton loadButton, JButton randomButton,
                   JLabel spriteLabel, JLabel nameLabel, JLabel typesLabel,
                   JLabel statsLabel, JProgressBar hpBar) {
            this.nameField = nameField;
            this.loadButton = loadButton;
            this.randomButton = randomButton;
            this.spriteLabel = spriteLabel;
            this.nameLabel = nameLabel;
            this.typesLabel = typesLabel;
            this.statsLabel = statsLabel;
            this.hpBar = hpBar;
        }

        void setButtonsEnabled(boolean enabled) {
            nameField.setEnabled(enabled);
            loadButton.setEnabled(enabled);
            randomButton.setEnabled(enabled);
        }

        // Barra "en movimiento" mientras se espera a la API.
        void showLoading() {
            hpBar.setIndeterminate(true);
            hpBar.setString("Cargando...");
        }

        // Muestra todos los datos de un Pokémon recién cargado.
        void showPokemon(Pokemon p, ImageIcon sprite) {
            spriteLabel.setIcon(sprite);
            spriteLabel.setText(sprite == null ? "(sin imagen)" : "");
            nameLabel.setText("#" + p.getId() + " " + capitalize(p.getName()));
            typesLabel.setText("Tipos: " + String.join(", ", p.getTypes()));
            statsLabel.setText("HP " + p.getMaxHp() + " | ATK " + p.getAttack()
                    + " | DEF " + p.getDefense() + " | SPD " + p.getSpeed());
            showHp(p.getCurrentHp());
        }

        // Vuelve a mostrar la barra como estaba (por ejemplo, si falló una carga).
        void showCurrentHp() {
            if (pokemon != null) {
                showHp(pokemon.getCurrentHp());
            } else {
                hpBar.setIndeterminate(false);
                hpBar.setValue(0);
                hpBar.setString("Sin Pokémon");
            }
        }

        // Actualiza la barra de vida: valor, texto y color.
        void showHp(int hp) {
            int max = pokemon.getMaxHp();
            hpBar.setIndeterminate(false);
            hpBar.setMaximum(max);
            hpBar.setValue(hp);
            hpBar.setString(hp + " / " + max + " HP");

            // Verde si tiene más de la mitad, naranja si le queda poco, rojo si casi nada
            double ratio = (double) hp / max;
            if (ratio > 0.5) {
                hpBar.setForeground(new Color(76, 175, 80));
            } else if (ratio > 0.2) {
                hpBar.setForeground(new Color(255, 152, 0));
            } else {
                hpBar.setForeground(new Color(229, 57, 53));
            }
        }
    }
}
