// Prezzo in centesimi (int): evita gli errori di arrotondamento dei double
public record Prodotto(String codice, String nome, int prezzoCentesimi) {}
