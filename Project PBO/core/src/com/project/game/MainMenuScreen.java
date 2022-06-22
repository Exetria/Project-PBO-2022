package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen extends ScreenAdapter {
    final Shooter game;

    SpriteBatch sb;
    OrthographicCamera camera;
    private Stage stage;
    private Music titleMusic;
    private Sound clickSound;
    private Skin skin;
    private BitmapFont menuFont;
    private final String title = "Alien Sky";


    private Table mainTable;

    public MainMenuScreen(final Shooter game) {
        this.game = game;

        sb = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false,800, 950);


        skin = new Skin(Gdx.files.internal("star-soldier-ui.json"));
        menuFont = new BitmapFont(Gdx.files.internal("Title.fnt"));
        titleMusic = Gdx.audio.newMusic(Gdx.files.internal("title-bgm.mp3"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("click1.wav"));
        titleMusic.setLooping(true);
        Save.load();
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
                game.setScreen(new InGame(game));
                titleMusic.stop();
                dispose();
            }
        });
        addButton("High Score").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new HighScoreScreen(game));
                titleMusic.stop();
                dispose();
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
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        sb.setProjectionMatrix(camera.combined);

        stage.act();

        stage.draw();

        sb.begin();
        menuFont.draw(sb, title, Gdx.graphics.getWidth()/2-(Gdx.graphics.getWidth()/4),800);
        sb.end();

    }

    @Override
    public void dispose() {
        titleMusic.dispose();
        clickSound.dispose();
        skin.dispose();
        menuFont.dispose();
        stage.dispose();
        sb.dispose();
    }
}