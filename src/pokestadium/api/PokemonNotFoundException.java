package pokestadium.api;

// Error para cuando el Pokémon buscado no existe.
// Así la ventana puede mostrar "Pokémon no encontrado" en vez de "error de red".
public class PokemonNotFoundException extends Exception {

    public PokemonNotFoundException(String name) {
        super("Pokémon no encontrado: " + name);
    }
}