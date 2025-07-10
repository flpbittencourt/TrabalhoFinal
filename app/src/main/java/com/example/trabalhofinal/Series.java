package com.example.trabalhofinal;

public class Series {
    private long id;
    private String title;
    private String genre;
    private int seasons;
    private String imagePath; // Certifique-se de que este campo está aqui

    public Series() {
        // Construtor padrão sem argumentos
    }

    // Construtor usado para criar um novo objeto Series sem um ID (usado ao adicionar)
    public Series(String title, String genre, int seasons, String imagePath) {
        this.title = title;
        this.genre = genre;
        this.seasons = seasons;
        this.imagePath = imagePath;
    }

    // Construtor usado para criar um objeto Series com um ID (usado ao editar/carregar do DB)
    public Series(long id, String title, String genre, int seasons, String imagePath) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.seasons = seasons;
        this.imagePath = imagePath;
    }

    // Certifique-se de que os getters e setters para imagePath estão presentes
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getSeasons() { return seasons; }
    public void setSeasons(int seasons) { this.seasons = seasons; }
}