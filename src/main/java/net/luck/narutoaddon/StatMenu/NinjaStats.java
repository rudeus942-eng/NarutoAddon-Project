package net.luck.narutoaddon.StatMenu;

public class NinjaStats implements INinjaStats {
    // Statistiche Numeriche
    private float health = 20.0F;
    private int strength = 1;
    private int agility = 1;

    // Liste per release e stirpi
    private String releases = "Fire, Wind";
    private String kekkeiGenkai = "None";
    private String dojutsu = "Sharingan"; // Se perso, impostare a "None"

    // --- Implementazione dei Metodi dell'Interfaccia ---

    @Override
    public float getHealth() { return health; }
    @Override
    public void setHealth(float health) { this.health = health; }

    @Override
    public int getStrength() { return strength; }
    @Override
    public void setStrength(int strength) { this.strength = strength; }

    @Override
    public int getAgility() { return agility; }
    @Override
    public void setAgility(int agility) { this.agility = agility; }

    @Override
    public String getReleases() { return releases; }
    @Override
    public void setReleases(String releases) { this.releases = releases; }

    @Override
    public String getKekkeiGenkai() { return kekkeiGenkai; }
    @Override
    public void setKekkeiGenkai(String kekkeiGenkai) { this.kekkeiGenkai = kekkeiGenkai; }

    @Override
    public String getDojutsu() { return dojutsu; }
    @Override
    public void setDojutsu(String dojutsu) { this.dojutsu = dojutsu; }
}