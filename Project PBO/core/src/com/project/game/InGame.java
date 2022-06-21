package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.assets.AssetManager;
import com.sun.java.swing.plaf.windows.WindowsDesktopPaneUI;

import java.sql.Time;
import java.util.Iterator;

//CLASS INI ISINYA PROSES-PROSES PAS GAMENYA LAGI JALAN/IN GAME (THE LOGIC)
public class InGame implements Screen
{
    final Shooter game;

    private Texture playerImg,asteroidImg,laserImg,enemyImg,bossImg,projectileImg, backgroundImg;
    private OrthographicCamera camera;
    final Music inGameMusic;
    private Music bossMusic;
    private BitmapFont font;

    Player player;
    Enemy boss;
    Rectangle laser;
    Enemy a;                                                    //a ini rectangle sementara buat ngisi array musuh
    Array<Rectangle> lasers;
    Array<Rectangle> projectiles;
    Array<Enemy> asteroids;
    Array<Enemy> enemies;
    Array<Rectangle> enemiesTrans;

    long lastAttackTime, lastProjectileTime, lastAsteroidTime, lastSpawnTime, lastPlayerCrashTime;
    private int score;
    int enemyDestroyed, touchSide, i;
    double scoreMultiplier;
    boolean bossState;                                          //maksudnya state sekarang lagi boss fight atau tidak

    public InGame(Shooter game, AssetManager assetManager)
    {
        this.game = game;

        // assets manual
//        playerImg = new Texture("player.png");
//        enemyImg = new Texture("enemy.png");
//        bossImg = new Texture("boss.png");
//        laserImg = new Texture("laser.png");
//        asteroidImg = new Texture("asteroid.png");
//        projectileImg = new Texture("projectile.png");
//        backgroundImg = new Texture("background.png");
//        font = new BitmapFont(); // use arial
        inGameMusic = Gdx.audio.newMusic(Gdx.files.internal("inGame.mp3"));


        // assets menggunakan class Assets
        playerImg = assetManager.get(Assets.playerImg);
        enemyImg = assetManager.get(Assets.enemyImg);
        bossImg = assetManager.get(Assets.bossImg);
        laserImg = assetManager.get(Assets.laserImg);
        asteroidImg = assetManager.get(Assets.asteroidImg);
        projectileImg = assetManager.get(Assets.projectileImg);
        backgroundImg = assetManager.get(Assets.backgroundImg);
        font = assetManager.get(Assets.menuFont); // use the title font
        font.getData().setScale(0.2f);//set size for the font

        camera = new OrthographicCamera();
        camera.setToOrtho(false,800,950);

        player = new Player();
        player.x = 400-32;
        player.y = 40;
        player.width = 64;
        player.height = 64;

        lasers = new Array<Rectangle>();
        spawnLaserPulse();

        projectiles = new Array<Rectangle>();

        asteroids = new Array<Enemy>();
        spawnAsteroids();

        enemies = new Array<Enemy>();
        getEnemyBatch(1);

        bossState = false;
        i = 2;                          //i = 2 karena batch ke-1 udah masuk jadi nanti mulai dari 2

        enemyDestroyed = 0;
    }

    //fungsi show bakal insert lagu pas awal mulai
    @Override
    public void show()
    {
        inGameMusic.setVolume(0.2f);
        inGameMusic.play();
        inGameMusic.setLooping(true);
    }

    @Override
    public void render(float delta)
    {
        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();

        //==================================================================GAMBAR OBJECT-OBJECT=================================================================================

        game.batch.begin();
        game.batch.draw(backgroundImg,0,0);
        game.batch.draw(playerImg, player.x, player.y);
        for (Rectangle laser : lasers)
        {
            game.batch.draw(laserImg, laser.x, laser.y);
        }
        for (Rectangle projectile : projectiles)
        {
            game.batch.draw(projectileImg, projectile.x, projectile.y);
        }
        for (Enemy asteroid : asteroids)
        {
            game.batch.draw(asteroidImg, asteroid.x, asteroid.y);
        }

        //kalo bukan boss fight, gambarnya pake enemy biasa
        if(!bossState)
        {
            for (Rectangle enemy : enemies)
            {
                game.batch.draw(enemyImg, enemy.x, enemy.y);
            }
        }

        //kalo boss fight, gambar pake img boss
        else
        {
            for (Rectangle enemy : enemies)
            {
                game.batch.draw(bossImg, enemy.x, enemy.y);
            }
            font.draw(game.batch, "Enemy HP: " + enemies.get(0).getHP(), 400 - 32, 600 - 16);
        }

        font.draw(game.batch, "Player HP: " + player.getHp(), 400-32, 32);
        font.draw(game.batch, "Score: " + player.getScore(), 10, 740);
        game.batch.end();

        //==================================================================SPAWN OBJECT-OBJECT==================================================================================

        move();

        //kalo bossnya lagi ada, nanti pake fungsi paling atas
        //yg kedua dirun kalo score 100 (transisi ke boss state)
        //yg ketiga dirun kalo keadaan biasa
        if(bossState)
        {
            bossMove();
            //spawn peluru boss / detik
            if (TimeUtils.nanoTime() - lastProjectileTime > 1000_000_000)
            {
                spawnProjectile();
            }
        }
        else if(enemyDestroyed % 100 == 0 && enemyDestroyed > 0)
        {
            spawnBoss();
            bossState = true;
        }
        else
        {
            if (TimeUtils.nanoTime() - lastSpawnTime > TimeUtils.millisToNanos(2000))
            {
                getEnemyBatch(i);
                i++;
                if (i > 10)
                    i = 1;
            }
        }

        //spawn asteroid / detik
        if (TimeUtils.nanoTime() - lastAsteroidTime > 1000_000_000)
            spawnAsteroids();

        if (TimeUtils.nanoTime() - lastAttackTime > 1000_000_000)
            spawnLaserPulse();

        //=====================================================================COLLISION DETECTION===============================================================================

        //peluru player jalan keatas
        Iterator<Rectangle> iterLaser = lasers.iterator();
        while (iterLaser.hasNext())
        {
            Rectangle laser = iterLaser.next();
            laser.y += 300 * Gdx.graphics.getDeltaTime();
            if (laser.y + 28 > 950)
            {
                iterLaser.remove();
            }

            if(laser.overlaps(enemies.get(0)) && enemies.get(0).getHP() > 0)
            {
                iterLaser.remove();
                enemies.get(0).menerimadamage(player.getLaserDmg(), player);
                System.out.println("boss hp: " + enemies.get(0).getHP());
                System.out.println("player score: " + player.getScore());
            }
        }

        //peluru boss jalan ke bawah
        Iterator<Rectangle> iterBossProjectile = projectiles.iterator();
        while (iterBossProjectile.hasNext())
        {
            Rectangle projectile = iterBossProjectile.next();
            projectile.y -= 300 * Gdx.graphics.getDeltaTime();
            if(projectile.y < -64 || projectile.y < 0)
            {
                iterBossProjectile.remove();
            }
            if (projectile.overlaps(player))
            {
                iterBossProjectile.remove();
                player.menerimadamage(enemies.get(0).getDamage()); // edit baru
            }
        }

        //asteroid jatuh ke bawah
        Iterator<Enemy> iterAsteroid = asteroids.iterator();
        while (iterAsteroid.hasNext())
        {
            Enemy asteroid = iterAsteroid.next();
            asteroid.y -= 300 * Gdx.graphics.getDeltaTime();
            if (asteroid.y < 0)
            {
                iterAsteroid.remove();
            }
            if (asteroid.overlaps(player))
            {
                iterAsteroid.remove();
                player.menerimadamage(asteroid.getDamage());
            }
        }

        // player collision with boss check
        if (player.overlaps(enemies.get(0)) && TimeUtils.nanoTime() - lastPlayerCrashTime > 3000_000_000L) {
            lastPlayerCrashTime = TimeUtils.nanoTime();
            player.menerimadamage(30);
        }
    }

    //=========================================================================FUNGSI-FUNGSI BUATAN KITA=========================================================================

    //gerakan player pake wasd
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

        //PENCET SPASI BUAT TAMBAH SCORE 100 BUAT SPAWN BOSS
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE))
        {
            enemyDestroyed += 100;
        }
    }

    private void shoot()
    {
    }

    //fungsi spawn bossnya (batch musuh nomor 11 itu isinya boss tok)
    private void spawnBoss()
    {
        getEnemyBatch(11);
    }

    //gerakan kanan kiri boss
    private void bossMove()
    {
        if(enemies.get(0).x < 0)
        {
            enemies.get(0).x = 1;
            touchSide++;
        }

        else if (enemies.get(0).x > 800-200)
        {
            enemies.get(0).x = 800-199;
            touchSide++;
        }

        if(touchSide % 2 == 0)
        {
            enemies.get(0).x += 100 * Gdx.graphics.getDeltaTime();
        }
        else
        {
            enemies.get(0).x -= 100 * Gdx.graphics.getDeltaTime();
        }
    }

    //spawn laser pulse dari player
    private void spawnLaserPulse()
    {
        laser = new Rectangle();
        laser.x = player.x + 28;
        laser.y = player.y + 64;
        laser.width = 8;
        laser.height = 28;
        lasers.add(laser);
        lastAttackTime = TimeUtils.nanoTime();
    }

    //spawn asteroid random
    private void spawnAsteroids()
    {
        Enemy asteroid = new Asteroid();
        asteroid.width = 32;
        asteroid.height = 32;
        asteroid.x = MathUtils.random(0, 800-32);
        asteroid.y = 950 - 32;
        asteroids.add(asteroid);
        lastAsteroidTime = TimeUtils.nanoTime();
    }

    //spawn laser boss
    private void spawnProjectile()
    {
        Rectangle projectile = new Rectangle();
        projectile.width = 64;
        projectile.height = 64;
        projectile.x = enemies.get(0).x + projectile.width+6;
        projectile.y = enemies.get(0).y - projectile.height;
        projectiles.add(projectile);
        lastProjectileTime = TimeUtils.nanoTime();
    }

    //fungsi buat ganti batch/wave musuh
    //musuh"nya masih diisi manual untuk sekarang
    private void getEnemyBatch(int i)
    {
        enemies.clear();
        if(i == 0)
        {
            a = new SmallEnemy();
            a.x = 272;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 464;
            a.y = 750;
            enemies.add(a);
        }
        else if(i == 1)
        {
            a = new SmallEnemy();
            a.x = 272;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 432;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 0.5;
        }
        else if(i == 2)
        {
            a = new SmallEnemy();
            a.x = 240;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 464;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 1;
        }
        else if(i == 3)
        {
            a = new SmallEnemy();
            a.x = 272;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 464;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 240;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 432;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 1.5;
        }
        else if(i == 4)
        {
            a = new SmallEnemy();
            a.x = 240;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 432;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 2;
        }
        else if(i == 5)
        {
            a = new SmallEnemy();
            a.x = 300;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 600;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 2.5;
        }
        else if(i == 6)
        {
            a = new SmallEnemy();
            a.x = 654;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 734;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 3;
        }
        else if(i == 7)
        {
            a = new SmallEnemy();
            a.x = 372;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 100;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 3.5;
        }
        else if(i == 8)
        {
            a = new SmallEnemy();
            a.x = 97;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 754;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 4;
        }
        else if(i == 9)
        {
            a = new SmallEnemy();
            a.x = 675;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 213;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 4.5;
        }
        else if(i == 10)
        {
            a = new SmallEnemy();
            a.x = 272;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 464;
            a.y = 750;
            enemies.add(a);

            scoreMultiplier = 5;
        }
        else if(i == 11)
        {
            a = new Boss();
            a.x = 400-100;
            a.y = 750;
            a.height = 200;
            a.width = 200;
            enemies.add(a);

            scoreMultiplier = 10;
        }
        else
        {
            a = new SmallEnemy();
            a.x = 272;
            a.y = 750;
            enemies.add(a);
            a = new SmallEnemy();
            a.x = 464;
            a.y = 750;
            enemies.add(a);
        }
        lastSpawnTime = TimeUtils.nanoTime();
    }

    //============================================================FUNGSI BAWAAN YG TDK DIPAKE====================================================================================

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
        font.dispose();
    }
}
