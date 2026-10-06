package pokestadium;

import org.json.JSONObject;
import pokestadium.model.Pokemon;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Prueba 1: la librería JSON funciona
        JSONObject json = new JSONObject("{\"name\": \"pikachu\", \"id\": 25}");
        System.out.println("JSON OK: " + json.getString("name") + " tiene id " + json.getInt("id"));

        // Prueba 2: la clase Pokemon funciona
        Pokemon p = new Pokemon(25, "pikachu", List.of("electric"),
                35, 55, 40, 90, "url");
        System.out.println(p);

        p.receiveDamage(20);   // le quitamos 20 de vida
        System.out.println(p);

        p.receiveDamage(100);  // le quitamos más de lo que tiene
        System.out.println(p + " -> fainted: " + p.isFainted());
    }
}