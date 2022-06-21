package com.project.game;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {

    public AssetManager assetManager = new AssetManager();

    public static final AssetDescriptor<Skin> uiSkin = new AssetDescriptor<Skin>("star-soldier-ui.json", Skin.class, new SkinLoader.SkinParameter("star-soldier-ui.atlas"));
    public static final String menuFont = "Title.fnt";
    public static final String titleMusic = "title-bgm.mp3";
    public static final String clickSound = "click1.wav";
    public static final String bossImg = "boss.png";
    public static final String playerImg = "player.png";
    public static final String enemyImg = "enemy.png";
    public static final String laserImg = "laser.png";
    public static final String asteroidImg = "asteroid.png";
    public static final String projectileImg = "projectile.png";
    public static final String backgroundImg = "background.png";



    public void loadAll(){
        assetManager.load(uiSkin);
        assetManager.load(menuFont, BitmapFont.class);
        assetManager.load(titleMusic, Music.class);
        assetManager.load(clickSound, Sound.class);
        assetManager.load(bossImg, Texture.class);
        assetManager.load(playerImg, Texture.class);
        assetManager.load(enemyImg, Texture.class);
        assetManager.load(laserImg, Texture.class);
        assetManager.load(asteroidImg, Texture.class);
        assetManager.load(projectileImg, Texture.class);
        assetManager.load(backgroundImg, Texture.class);
    }

    public AssetManager getAssetManager(){
        return assetManager;
    }
}
