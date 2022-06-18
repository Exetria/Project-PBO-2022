package com.project.game;
//a
import com.badlogic.gdx.math.Rectangle;

// interface

public abstract class Enemy extends Rectangle{
    private int HP;
    private int Damage;

    //konstruktor
    Enemy() { // musuh kecil
        this.HP = 10;
        Damage = 5;
    }

    public void menerimadamage(int damage) {
        this.HP = this.HP - damage;

    if (this.HP <= 0){

    }
    }

    public int getDamage(){
        return Damage;
    }
    
    public void setDamage(int damage){
        this.Damage = damage;
    }

    public int getHP(){
        return HP;
    }
    
    public void setHP(int HP){
        this.HP = HP;
    }
}

class Asteroid extends Enemy{

    Asteroid(){
        setDamage(10);
        setHP(1);
    }

    public void menerimadamage(int damage){
        super.menerimadamage(damage);
    }
}

class Boss extends Enemy {

    Boss (){
        setDamage(20);
        setHP(150);
    }

    public void menerimadamage(int damage){
        super.menerimadamage(damage);
    }
}