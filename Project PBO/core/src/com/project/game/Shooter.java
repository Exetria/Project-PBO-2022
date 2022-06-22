package com.project.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//CLASS INI ISINYA AWALAN DARI GAMENYA (MASUK KE MAIN MENU DKK)
public class Shooter extends Game
{
	SpriteBatch batch;
	BitmapFont font;

	@Override
	public void create()
	{
		batch = new SpriteBatch();
		font = new BitmapFont();
//		this.setScreen(new MainMenuScreen(this, assets.getAssetManager()));
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render()
	{
		super.render();
	}

	@Override
	public void dispose()
	{
		font.dispose();
	}
}