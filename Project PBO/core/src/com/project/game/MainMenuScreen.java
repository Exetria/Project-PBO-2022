package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class MainMenuScreen extends ScreenAdapter {
    final Shooter game;

    private Stage stage;
    private Music titleMusic;
    private Sound clickSound;
    private AssetManager assetManager;
    private Skin skin;
    private BitmapFont menuFont;
    private final String title = "Alien Sky";


    private Table mainTable;

    public MainMenuScreen(Shooter game, AssetManager assetManager) {
        this.game = game;
        this.assetManager = assetManager;
        skin = assetManager.get(Assets.uiSkin);
        menuFont = assetManager.get(Assets.menuFont);
        titleMusic = assetManager.get(Assets.titleMusic);
        clickSound = assetManager.get(Assets.clickSound);
        titleMusic.setLooping(true);
        titleMusic.setVolume(0.2f);

    }

    private TextButton addButton (String name){
        TextButton button = new TextButton(name,skin);
        mainTable.add(button).width(400).height(110).padBottom(1);
        mainTable.row();
        return button;
    }

    @Override
    public void show() {
        stage = new Stage();

        mainTable = new Table();
        mainTable.setFillParent(true);

        stage.addActor(mainTable);

        titleMusic.play();

        addButton("Play").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new InGame(game, assetManager));
                titleMusic.stop();
                dispose();
            }
        });
        addButton("High Score").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
            }
        });
        addButton("Quit").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                Gdx.app.exit();
            }
        });
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();

        stage.draw();

        game.batch.begin();
        menuFont.draw(game.batch, title, Gdx.graphics.getWidth()/2-(Gdx.graphics.getWidth()/4),650);
        game.batch.end();

    }
}