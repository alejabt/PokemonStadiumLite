package pokestadium.battle;

// Avisos que Battle envía durante el combate.
// Cada clase que la use (la ventana o la consola) decide cómo mostrarlos.
public interface BattleListener {

    // Se llama en cada ataque. modifier es la efectividad del tipo (1.3, 0.7 o 1.0).
    void onTurn(String attacker, String defender, int damage, boolean critical, double modifier);

    // Se llama cada vez que cambia el HP de un Pokémon.
    void onHpChanged(String pokemon, int hpActual);

    // Se llama una sola vez, cuando un Pokémon llega a 0 HP.
    void onBattleEnded(String winner);
}