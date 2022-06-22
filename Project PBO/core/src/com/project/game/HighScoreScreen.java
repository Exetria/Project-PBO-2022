package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class HighScoreScreen implements Screen {
    final Shooter game;
    private BitmapFont font, font1, menuFont;
    private long highScores[];
    private String names[];


    OrthographicCamera camera;


    public HighScoreScreen(Shooter game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 950);

        menuFont = new BitmapFont(Gdx.files.internal("Title.fnt"));
        font = new BitmapFont();
        font.getData().setScale(2, 2);
        font1 = new BitmapFont();


        Save.load();
        highScores = Save.gd.getHighScore();
        names = Save.gd.getNames();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        String s = "High Scores";
        GlyphLayout layout = new GlyphLayout(menuFont, s);
        float w = layout.width;

        menuFont.draw(game.batch, s, (Gdx.graphics.getWidth()-w) / 2, 800);

        for (int i = 0; i < highScores.length; i++) {
            s = String.format(
                    "%2d. %7s %s",
                    i+1,
                    highScores[i],
                    names[i]
            );

            GlyphLayout layout1 = new GlyphLayout(font, s);
            w = layout1.width;

            font.draw(game.batch, s, (Gdx.graphics.getWidth()-w) / 2, 650 - (50*i));
        }

        GlyphLayout layout2 = new GlyphLayout(font1, "Press Enter or Esc to Main Menu!");
        w = layout2.width;
        font1.draw(game.batch, "Press Enter or Esc to Main Menu!", (Gdx.graphics.getWidth()-w) / 2, 50);


        game.batch.end();

        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyPressed(Input.Keys.ENTER)){
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        font.dispose();
        font1.dispose();
        menuFont.dispose();
    }
}
