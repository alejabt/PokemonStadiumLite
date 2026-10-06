package pokestadium.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pokestadium.model.Pokemon;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Consulta la PokeAPI y convierte el JSON en objetos Pokemon.

public class PokeApiClient {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final int MAX_POKEMON_ID = 1025; // Pokémon disponibles en la API

    // Se crea una sola vez y se reutiliza en todas las consultas
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Random random = new Random();

    // Busca un Pokémon por su nombre
    public Pokemon fetchByName(String name)
            throws PokemonNotFoundException, IOException, InterruptedException {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre de un Pokémon");
        }

        // La API solo acepta minúsculas y guiones: "Mr Mime" -> "mr-mime"
        String cleanName = name.trim().toLowerCase().replace(" ", "-");
        return fetch(cleanName);
    }

    // Trae un Pokémon al azar usando un id entre 1 y MAX_POKEMON_ID.
    public Pokemon fetchRandom()
            throws PokemonNotFoundException, IOException, InterruptedException {

        int randomId = random.nextInt(MAX_POKEMON_ID) + 1;
        return fetch(String.valueOf(randomId));
    }

    //Hace la petición HTTP. La API acepta nombre o id en la misma URL.
    private Pokemon fetch(String nameOrId)
            throws PokemonNotFoundException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + nameOrId))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new PokemonNotFoundException(nameOrId);
        }
        if (response.statusCode() != 200) {
            throw new IOException("La API respondió con código " + response.statusCode());
        }

        return parse(response.body());
    }

    // Convierte el texto JSON en un objeto Pokemon.
    private Pokemon parse(String body) throws IOException {
        try {
            JSONObject json = new JSONObject(body);

            int id = json.getInt("id");
            String name = json.getString("name");

            // Tipos: types[i].type.name
            List<String> types = new ArrayList<>();
            JSONArray typesArray = json.getJSONArray("types");
            for (int i = 0; i < typesArray.length(); i++) {
                JSONObject typeSlot = typesArray.getJSONObject(i);
                types.add(typeSlot.getJSONObject("type").getString("name"));
            }

            // Stats: stats[i].stat.name y stats[i].base_stat
            int hp = 0, attack = 0, defense = 0, speed = 0;
            JSONArray statsArray = json.getJSONArray("stats");
            for (int i = 0; i < statsArray.length(); i++) {
                JSONObject statSlot = statsArray.getJSONObject(i);
                String statName = statSlot.getJSONObject("stat").getString("name");
                int value = statSlot.getInt("base_stat");

                switch (statName) {
                    case "hp":      hp = value;      break;
                    case "attack":  attack = value;  break;
                    case "defense": defense = value; break;
                    case "speed":   speed = value;   break;
                    default: break; // special-attack y special-defense no se usan
                }
            }

            // Sprite: si algún Pokémon no tiene imagen (viene null)
            String spriteUrl = json.getJSONObject("sprites").optString("front_default", "");

            return new Pokemon(id, name, types, hp, attack, defense, speed, spriteUrl);

        } catch (JSONException e) {
            throw new IOException("La respuesta de la API no tiene el formato esperado", e);
        }
    }
}