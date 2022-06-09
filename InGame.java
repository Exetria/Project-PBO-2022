package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.assets.AssetManager;

import java.util.Iterator;

//CLASS INI ISINYA PROSES-PROSES PAS GAMENYA LAGI JALAN/IN GAME (THE LOGIC)
public class InGame implements Screen
{
    final Shooter game;

    private Texture playerImg,asteroidImg,laserImg,enemyImg,bossImg,projectileImg;
    private OrthographicCamera camera;
    final Music inGameMusic;
    private Music bossMusic;

    Player player;
    Enemy boss;
    Array<Enemy> asteroids;
    Rectangle laser;
    Array<Rectangle> lasers;
    Array<Rectangle> enemies;
    Array<Rectangle> projectiles;

    long lastAttackTime;
    long lastProjectileTime;
    int enemyDestroyed;
    int touchSide;
    private int score;
    long lastAsteroidTime;
    long lastPlayerCrashTime;

    //INI CONSTRUCTOR DARI GAME SCREEN, ISINYA SAMA KAYAK create()
    public InGame(Shooter game, AssetManager assetManager)
    {
        this.game = game;


        playerImg = new Texture("player.png");
        enemyImg = new Texture("enemy.png");
        bossImg = new Texture("boss.png");
        laserImg = new Texture("laser.png");
        asteroidImg = new Texture("asteroid.png");
        inGameMusic = Gdx.audio.newMusic(Gdx.files.internal("inGame.mp3"));
        projectileImg = new Texture("projectile.png");


        inGameMusic.setLooping(true);
        inGameMusic.play();
        inGameMusic.setVolume(0.2f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false,800,950);

        player = new Player();
        player.x = 400-32;
        player.y = 40;
        player.width = 64;
        player.height = 64;

        spawnBoss();

        lasers = new Array<Rectangle>();
//         spawnLaserPulse();

        projectiles = new Array<Rectangle>();
        spawnProjectile();

        asteroids = new Array<Enemy>();
        spawnAsteroids();

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

        game.batch.begin();

        game.batch.draw(playerImg, player.x, player.y);

        for (Rectangle laser : lasers) {
            game.batch.draw(laserImg, laser.x, laser.y);
        }

        for (Rectangle projectile : projectiles) {
            game.batch.draw(projectileImg, projectile.x, projectile.y);
        }

        for (Enemy asteroid : asteroids) {
            game.batch.draw(asteroidImg, asteroid.x, asteroid.y);
        }

        game.batch.draw(bossImg,boss.x,boss.y);

        game.batch.end();

        move();
        shoot();
        bossMove();

        if (TimeUtils.nanoTime() - lastAsteroidTime > 1000_000_000)
            spawnAsteroids();

        // if (TimeUtils.nanoTime() - lastAttackTime > 1000000000)
        //     spawnLaserPulse();

        if (TimeUtils.nanoTime() - lastProjectileTime > 1000_000_000){
            spawnProjectile();
        }

        Iterator<Rectangle> iterLaser = lasers.iterator();
        while (iterLaser.hasNext()) {
            Rectangle laser = iterLaser.next();
            laser.y += 300 * Gdx.graphics.getDeltaTime();
            if (laser.y + 28 > 950)
                iterLaser.remove();
        }

        Iterator<Rectangle> iterBossProjectile = projectiles.iterator();
        while (iterBossProjectile.hasNext()) {
            Rectangle projectile = iterBossProjectile.next();
            projectile.y -= 300 * Gdx.graphics.getDeltaTime();
            if (projectile.y < 0 || projectile.overlaps(player)){
                iterBossProjectile.remove();
//                System.out.println("boss projectile overlaps with player");
            }
        }

        Iterator<Enemy> iterAsteroid = asteroids.iterator();
        while (iterAsteroid.hasNext()){
            Enemy asteroid = iterAsteroid.next();
            asteroid.y -= 300 * Gdx.graphics.getDeltaTime();
            if (asteroid.y < 0 || asteroid.overlaps(player)){
                iterAsteroid.remove();
                player.menerimadamage(asteroid.getDamage());
//                System.out.println("asteroid overlaps with player");
//                System.out.println("laser breaks the asteroid");
            }
        }

        // player collision with boss check
        if (player.overlaps(boss) && TimeUtils.nanoTime() - lastPlayerCrashTime > 3000_000_000L){
            lastPlayerCrashTime = TimeUtils.nanoTime();
            player.menerimadamage(30);
            System.out.println("player ship collide with the boss ship");
        }


    }

    private void move()
    {
        if (player.x <= 0)
            player.x = 0;

        if (player.x >= 800 - 64)
            player.x = 800-64;

        if (player.y <= 0)
            player.y = 0;

        if (player.y >= 950 - 64)
            player.y = 950 - 64;

        if (Gdx.input.isKeyPressed(Input.Keys.A))
        {
            player.x -= 300 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D))
        {
            player.x += 300 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S))
        {
            player.y -= 300 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W))
        {
            player.y += 300 * Gdx.graphics.getDeltaTime();
        }
    }


    private void shoot(){
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (TimeUtils.nanoTime() - lastProjectileTime > 1000_000_000) {
                spawnLaserPulse();
            }
        }
    }

    private void spawnBoss(){
        touchSide = 1;
        boss = new Boss();
        boss.x = 400-100;
        boss.y = 750;
        boss.height = 200;
        boss.width = 200;
        System.out.println("boss spawned");
    }

    private void bossMove() {
        if (boss.x <0){
            boss.x = 1;
            touchSide++;
        } else if (boss.x > 800-200) {
            boss.x = 800-199;
            touchSide++;

        }
        if (touchSide % 2 == 0){
            boss.x += 100 * Gdx.graphics.getDeltaTime();
        }
        else {
            boss.x -= 100 * Gdx.graphics.getDeltaTime();
        }
    }

    private void spawnLaserPulse()
    {
        laser = new Rectangle();
        laser.x = player.x + 28;
        laser.y = player.y + 64;
        //GAMBAR LASERNYA UDAH AKU CUT JADI 8X28 (LASERNYA TOK TANPA PUTIH" BACKGROUND)
        laser.width = 8;
        laser.height = 28;
        lasers.add(laser);
        lastAttackTime = TimeUtils.nanoTime();
    }

    private void spawnAsteroids(){
        Enemy asteroid = new Asteroid();
        asteroid.width = 32;
        asteroid.height = 32;
        asteroid.x = MathUtils.random(0, 800-32);
        asteroid.y = 950 - 32;
        asteroids.add(asteroid);
        lastAsteroidTime = TimeUtils.nanoTime();
    }

    // boss laser
    private void spawnProjectile(){
        Rectangle projectile = new Rectangle();
        projectile.width = 64;
        projectile.height = 64;
        projectile.x = boss.x + projectile.width+6;
        projectile.y = boss.y - projectile.height;
        projectiles.add(projectile);
        lastProjectileTime = TimeUtils.nanoTime();
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
        game.batch.dispose();
        playerImg.dispose();
        enemyImg.dispose();
        bossImg.dispose();
        laserImg.dispose();
        asteroidImg.dispose();
    }
}
