package net.luck.narutoaddon.StatMenu;

public interface INinjaStats {
    float getHealth();
    void setHealth(float health);

    int getStrength();
    void setStrength(int strength);

    int getAgility();
    void setAgility(int agility);

    String getReleases();
    void setReleases(String releases);

    String getKekkeiGenkai();
    void setKekkeiGenkai(String kekkeiGenkai);

    String getDojutsu();
    void setDojutsu(String dojutsu);
}