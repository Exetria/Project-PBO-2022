package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.graphics.Texture;

public class Player extends Rectangle
{
    private int hp;
    private int xp;
    private int score;
    private int laserDmg;

    public int getHp() {
        return hp;
    }

    public int getLaserDmg() {
        return laserDmg;
    }

    public int getScore() {
        return score;
    }

    Player()
    {
        this.hp = 100;
        this.xp = 0;
        this.score = 0;
        this.laserDmg = 10;
    }

    public void menerimadamage(int damage)
    {
        if (this.hp > 0)
        {
            this.hp = this.hp - damage;
            if (this.hp <= 0){
                this.hp = 0;
            }
        }
    }

    public void addScore(int score){
        this.score += score;
    }

}