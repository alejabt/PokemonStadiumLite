package pokestadium;

import pokestadium.api.PokeApiClient;
import pokestadium.api.PokemonNotFoundException;
import pokestadium.battle.Battle;
import pokestadium.battle.BattleListener;
import pokestadium.model.Pokemon;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

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

        PokeApiClient api = new PokeApiClient();

        try {
            Pokemon p1 = api.fetchByName("squirtle");
            Pokemon p2 = api.fetchByName("charmander");
            System.out.println("--- " + p1 + "\n--- " + p2 + "\n");

            Battle battle = new Battle(p1, p2, consola, 500); // medio segundo entre turnos
            battle.fight();

        } catch (PokemonNotFoundException e) {
            System.out.println("Error -> " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error de red -> " + e.getMessage());
        }
    }
}