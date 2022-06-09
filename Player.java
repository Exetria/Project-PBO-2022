package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.graphics.Texture;

public class Player extends Rectangle{
    private int hp;
    private int xp;
    private int score;
    private int laserDmg;

    Player(){
        this.hp = 100;
        this.xp = 0;
        this.score = 0;
        this.laserDmg = 10;
    }

    public void menerimadamage(int damage) {
        this.hp -= damage;
    }

}