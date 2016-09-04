package com.ifms.tcc.marcus_bruno.tcc.Models;

/**
 * Created by marcus-bruno on 8/13/16.
 */
public class Professor {

    private String rp;
    private String nome;
    private String telefone;
    private String email;
    private String mac_address;

    public Professor(String rp, String nome, String telefone, String email, String mac_address) {
        this.rp = rp;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.mac_address = mac_address;
    }

    public String getRp() {
        return rp;
    }

    public void setRp(String rp) {
        this.rp = rp;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }
}
