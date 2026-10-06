package pokestadium;

import pokestadium.battle.BattleListener;

public class Main {
    public static void main(String[] args) {

        // Un "oyente" que muestra los avisos en consola
        BattleListener consola = new BattleListener() {
            @Override
            public void onTurn(String attacker, String defender, int damage,
                               boolean critical, double modifier) {
                System.out.println(attacker + " ataca a " + defender
                        + " y hace " + damage + " de daño"
                        + (critical ? " ¡CRÍTICO!" : "")
                        + " (x" + modifier + ")");
            }

            @Override
            public void onHpChanged(String pokemon, int hpActual) {
                System.out.println("   HP de " + pokemon + ": " + hpActual);
            }

            @Override
            public void onBattleEnded(String winner) {
                System.out.println("¡Ganó " + winner + "!");
            }
        };

        // Simulamos a mano lo que Battle hará solo en el Paso 4
        consola.onTurn("squirtle", "charmander", 15, true, 1.3);
        consola.onHpChanged("charmander", 24);
        consola.onBattleEnded("squirtle");
    }
}