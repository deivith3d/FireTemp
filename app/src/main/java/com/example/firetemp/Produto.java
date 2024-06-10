package com.example.firetemp;

public class Produto {
    public String id;
    public String nome,categoria, idUsuario;

    public Produto() {
    }

    public Produto(String nome, String categoria) {
        this.nome = nome;
        this.categoria = categoria;
    }

    public Produto(String id,String nome, String categoria) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
    }

    public Produto(String id,String nome, String categoria, String idUsuario) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.idUsuario = idUsuario;
    }

    @Override
    public String toString(){
        return nome + " | " + categoria;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
