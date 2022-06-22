package com.project.game;

import com.badlogic.gdx.math.Rectangle;

// interface

public abstract class Enemy extends Rectangle
{
    private int hp;
    private int damage;
    private int score;

    //konstruktor
    Enemy(int hp, int damage, int score)
    {
        this.hp = hp;
        this.damage = damage;
        this.score = score;
    }

    public void menerimadamage(int damage, Player player, double multiplier)
    {
        this.hp = this.hp - damage;
        if(this.hp <= 0)
            {
                this.hp = 0;
                player.addScore((int) (this.score*multiplier));
            }
    }

    public void dropScore(Player player){
        player.addScore(this.score);
    }

    public void setHP(int HP){
        this.hp = HP;
    }
    public void setDamage(int damage){
        this.damage = damage;
    }

    public int getHP()
    {
        return hp;
    }
    public int getDamage()
    {
        return damage;
    }

}

class SmallEnemy extends Enemy
{
    SmallEnemy()
    {
        super(20, 5, 10);
    }
}

class Boss extends Enemy
{
    Boss()
    {
        super(150, 20, 100);
    }
}

class Asteroid extends Enemy
{
    Asteroid()
    {
        super(1, 5, 1);
    }
}