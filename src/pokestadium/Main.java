package pokestadium;

import pokestadium.api.PokeApiClient;
import pokestadium.api.PokemonNotFoundException;
import pokestadium.model.Pokemon;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        PokeApiClient api = new PokeApiClient();

        try {
            // Prueba 1: por nombre, con mayúscula a propósito
            Pokemon pikachu = api.fetchByName("Pikachu");
            System.out.println("Por nombre: " + pikachu);
            System.out.println("Sprite: " + pikachu.getSpriteUrl());

            // Prueba 2: aleatorio
            Pokemon random = api.fetchRandom();
            System.out.println("Aleatorio: " + random);

            // Prueba 3: uno que no existe
            api.fetchByName("noexiste123");

        } catch (PokemonNotFoundException e) {
            System.out.println("Error controlado -> " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error de red -> " + e.getMessage());
        }
    }
}