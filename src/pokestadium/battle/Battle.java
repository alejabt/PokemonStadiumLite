package pokestadium.battle;

import pokestadium.model.Pokemon;

import java.util.Random;

// Reglas del combate por turnos.

// Daño de cada ataque:
//   base = ATK * random(0-1) - DEF * random(0-1)   (si da menos de 1, se deja en 1)
//   daño = base * efectividad * crítico            (redondeado)
//
// Crítico: 10% de probabilidad, multiplica x1.5.
// Efectividad (primer tipo): agua gana a fuego, fuego a planta y planta a agua (x1.3).
// Si es al revés x0.7, y en los demás casos x1.0.
//
// Esta clase no toca la ventana: todo lo avisa por BattleListener.
public class Battle {

    private static final double CRITICAL_CHANCE = 0.10;
    private static final double CRITICAL_MULTIPLIER = 1.5;
    private static final double SUPER_EFFECTIVE = 1.3;
    private static final double NOT_EFFECTIVE = 0.7;
    private static final double NEUTRAL = 1.0;

    private final Pokemon pokemon1;
    private final Pokemon pokemon2;
    private final String name1; // nombre que se usa en los eventos
    private final String name2;
    private final BattleListener listener;
    private final long turnDelayMs; // pausa entre turnos, para que se pueda ver
    private final Random random = new Random();

    public Battle(Pokemon pokemon1, Pokemon pokemon2,
                  BattleListener listener, long turnDelayMs) {
        this.pokemon1 = pokemon1;
        this.pokemon2 = pokemon2;
        this.listener = listener;
        this.turnDelayMs = turnDelayMs;

        // Si los dos jugadores eligen el mismo Pokémon, los diferenciamos
        if (pokemon1.getName().equals(pokemon2.getName())) {
            this.name1 = pokemon1.getName() + " (J1)";
            this.name2 = pokemon2.getName() + " (J2)";
        } else {
            this.name1 = pokemon1.getName();
            this.name2 = pokemon2.getName();
        }
    }

    //Ejecuta el combate completo, turno por turno, hasta que uno llegue a 0 HP.
    public void fight() throws InterruptedException {
        // Vida completa al empezar (permite revancha)
        pokemon1.restoreHp();
        pokemon2.restoreHp();
        listener.onHpChanged(name1, pokemon1.getCurrentHp());
        listener.onHpChanged(name2, pokemon2.getCurrentHp());

        // Orden de turnos: el más rápido empieza; si empatan, al azar
        Pokemon attacker;
        Pokemon defender;
        if (pokemon1.getSpeed() > pokemon2.getSpeed()) {
            attacker = pokemon1;
            defender = pokemon2;
        } else if (pokemon2.getSpeed() > pokemon1.getSpeed()) {
            attacker = pokemon2;
            defender = pokemon1;
        } else if (random.nextBoolean()) {
            attacker = pokemon1;
            defender = pokemon2;
        } else {
            attacker = pokemon2;
            defender = pokemon1;
        }

        while (true) {
            executeTurn(attacker, defender);

            if (defender.isFainted()) {
                break; // se acabó el combate
            }

            // Cambio de turno: el que defendía ahora ataca
            Pokemon temp = attacker;
            attacker = defender;
            defender = temp;

            if (turnDelayMs > 0) {
                Thread.sleep(turnDelayMs);
            }
        }

        listener.onBattleEnded(nameOf(attacker));
    }

    // Un ataque: calcula el daño, lo aplica y avisa.
    private void executeTurn(Pokemon attacker, Pokemon defender) {
        double base = attacker.getAttack() * random.nextDouble()
                - defender.getDefense() * random.nextDouble();
        base = Math.max(1, base);

        double modifier = typeModifier(attacker.getPrimaryType(), defender.getPrimaryType());
        boolean critical = random.nextDouble() < CRITICAL_CHANCE;

        double total = base * modifier;
        if (critical) {
            total = total * CRITICAL_MULTIPLIER;
        }
        int damage = (int) Math.round(total);

        defender.receiveDamage(damage);

        listener.onTurn(nameOf(attacker), nameOf(defender), damage, critical, modifier);
        listener.onHpChanged(nameOf(defender), defender.getCurrentHp());
    }

    // Efectividad según el primer tipo de cada Pokémon.
    private double typeModifier(String attackerType, String defenderType) {
        if (beats(attackerType, defenderType)) {
            return SUPER_EFFECTIVE;
        }
        if (beats(defenderType, attackerType)) {
            return NOT_EFFECTIVE; // la "inversa"
        }
        return NEUTRAL;
    }

    //  Agua>Fuego, Fuego>Planta, Planta>Agua.
    private boolean beats(String a, String b) {
        return (a.equals("water") && b.equals("fire"))
                || (a.equals("fire") && b.equals("grass"))
                || (a.equals("grass") && b.equals("water"));
    }

    // Devuelve el nombre que identifica a cada Pokémon en los eventos.
    private String nameOf(Pokemon pokemon) {
        return pokemon == pokemon1 ? name1 : name2;
    }

    public String getName1() { return name1; }
    public String getName2() { return name2; }
}