package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.project.game.Shooter;

public class GameOverScreen implements Screen {
    final Shooter game;

    private BitmapFont menuFont, font;
    private boolean newHighScore;
    private char[] newName;
    private int currentChar;
    SpriteBatch sb;


    private ShapeRenderer sr;

    OrthographicCamera camera;


    public GameOverScreen(Shooter game) {
        this.game = game;

        sr = new ShapeRenderer();
        sb = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 950);

        menuFont = new BitmapFont(Gdx.files.internal("Title.fnt"));
        font = new BitmapFont();
        font.getData().setScale(2, 2);

        newHighScore = Save.gd.isHighScore(Save.gd.getYourScore());

        if (newHighScore){
            newName = new char[]{'A','A','A'};
            currentChar = 0;
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        sb.setProjectionMatrix(camera.combined);

        sb.begin();

        String s = "Game Over!";
        GlyphLayout layout = new GlyphLayout(menuFont, s);
        float w = layout.width;

        menuFont.draw(sb, s, (Gdx.graphics.getWidth()-w) / 2,600);

        if(!newHighScore){
            sb.end();
            return;
        }

        s = "New High Score : "+ Save.gd.getYourScore();
        GlyphLayout layout1 = new GlyphLayout(font, s);
        w = layout1.width;

        font.draw(sb, s, (Gdx.graphics.getWidth()-w) / 2,400);

        for(int i = 0; i < newName.length; i++) {
            font.draw(
                    sb,
                    Character.toString(newName[i]),
                    340 + 50 * i,
                    250
            );
        }

        sb.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.line(340 + 50 * currentChar,
                220,
                360 + 50 * currentChar,
                220
        );
        sr.end();


        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            if(newHighScore) {
                Save.gd.addHighScore(
                        Save.gd.getYourScore(),
                        new String(newName)
                );
                Save.save();
            }
            game.setScreen(new MainMenuScreen(game));
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)){
            if (newName[currentChar] == ' '){
                newName[currentChar] = 'Z';
            }
            else {
                newName[currentChar]--;
                if (newName[currentChar] < 'A'){
                    newName[currentChar] = ' ';
                }
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            if(newName[currentChar] == ' ') {
                newName[currentChar] = 'A';
            }
            else {
                newName[currentChar]++;
                if(newName[currentChar] > 'Z') {
                    newName[currentChar] = ' ';
                }
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            if(currentChar < newName.length - 1) {
                currentChar++;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            if(currentChar > 0) {
                currentChar--;
            }
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
        sr.dispose();
        sb.dispose();
        menuFont.dispose();
        font.dispose();
    }
}
