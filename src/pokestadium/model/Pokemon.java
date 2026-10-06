package pokestadium.model;

import java.util.List;

public class Pokemon {
    private final int id;
    private final String name;
    private final List<String> types;
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private final String spriteUrl;
    private int currentHp;

    public Pokemon(int id, String name, List<String> types,
                   int maxHp, int attack, int defense, int speed, String spriteUrl) {
        this.id = id;
        this.name = name;
        this.types = types;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.spriteUrl = spriteUrl;
        this.currentHp = maxHp;
    }

    // Resta vida sin bajar de 0
    public void receiveDamage(int damage) {
        currentHp = Math.max(0, currentHp - damage);
    }

    public boolean isFainted() {
        return currentHp == 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<String> getTypes() { return types; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public String getSpriteUrl() { return spriteUrl; }

    @Override
    public String toString() {
        return name + " #" + id + " " + types + " HP " + currentHp + "/" + maxHp;
    }
}
