package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.assets.AssetManager;


//INI BAGIAN MAIN MENU
public class MainMenu implements Screen
{
    final Shooter game;
    final Music titleMusic;
    private AssetManager assetManager;


    OrthographicCamera camera;

    public MainMenu(final Shooter game)
    {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 950);

        titleMusic = Gdx.audio.newMusic(Gdx.files.internal("mainMenu.ogg"));

        titleMusic.setLooping(true);
        titleMusic.play();
        titleMusic.setVolume(0.2f);
    }

    @Override
    public void show()
    {

    }

    @Override
    public void render(float delta)
    {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.font.draw(game.batch, "Alien Shooter", 350, 670);
        game.font.draw(game.batch, "Click to Start", 350, 650);
        game.batch.end();

        if (Gdx.input.isTouched())
        {
            game.setScreen(new InGame(game));
            titleMusic.stop();
            dispose();
        }
    }

    @Override
    public void resize(int width, int height)
    {

    }

    @Override
    public void pause()
    {

    }

    @Override
    public void resume()
    {

    }

    @Override
    public void hide()
    {

    }

    @Override
    public void dispose()
    {

    }
}
