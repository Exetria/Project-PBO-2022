package com.project.game;

import com.badlogic.gdx.math.Rectangle;

// interface

public abstract class Enemy extends Rectangle
{
    private int hp;
    private int damage;

    //konstruktor
    Enemy(int hp, int damage)
    {
        this.hp = hp;
        damage = damage;
    }

    public void menerimadamage(int damage)
    {
        this.hp = this.hp - damage;
        if(this.hp <= 0)
            {
                this.hp = 0;
            }
    }

    public void setHP(int HP){
        this.hp = HP;
    }
    public void setDamage(int damage){
        this.damage = damage;
    }

    public int getHP(){
        return hp;
    }
    public int getDamage(){
        return damage;
    }

}

class SmallEnemy extends Enemy
{
    SmallEnemy()
    {
        super(10, 5);
    }
}

class Boss extends Enemy
{
    Boss()
    {
        super(150, 20);
    }
}

class Asteroid extends Enemy
{
    Asteroid()
    {
        super(1, 10);
    }
}