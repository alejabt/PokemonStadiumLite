package pokestadium.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pokemon {

    // Datos que vienen de la API: no cambian después de crear el objeto
    private final int id;
    private final String name;
    private final List<String> types;
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private final String spriteUrl;

    // Único dato que cambia durante el combate
    private int currentHp;

    public Pokemon(int id, String name, List<String> types,
                   int maxHp, int attack, int defense, int speed,
                   String spriteUrl) {
        this.id = id;
        this.name = name;
        this.types = new ArrayList<>(types); // copia defensiva
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.spriteUrl = spriteUrl;
        this.currentHp = maxHp; // empieza con la vida completa
    }

    //Resta daño sin permitir que el HP quede negativo
    public void receiveDamage(int damage) {
        if (damage < 0) {
            damage = 0;
        }
        currentHp = Math.max(0, currentHp - damage);
    }

    public boolean isFainted() {
        return currentHp == 0;
    }

    // Devuelve el HP al máximo (útil para una revancha)
    public void restoreHp() {
        currentHp = maxHp;
    }

    public String getPrimaryType() {
        return types.isEmpty() ? "" : types.get(0);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<String> getTypes() { return Collections.unmodifiableList(types); }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public String getSpriteUrl() { return spriteUrl; }
    public int getCurrentHp() { return currentHp; }

    @Override
    public String toString() {
        return name + " " + types + " HP " + currentHp + "/" + maxHp
                + " ATK " + attack + " DEF " + defense + " SPD " + speed;
    }
}