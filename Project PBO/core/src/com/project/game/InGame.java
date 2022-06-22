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

    public InGame(Shooter game)
    {
        this.game = game;

        // assets manual
        playerImg = new Texture("player.png");
        enemyImg = new Texture("enemy.png");
        bossImg = new Texture("boss.png");
        laserImg = new Texture("laser.png");
        asteroidImg = new Texture("asteroid.png");
        projectileImg = new Texture("projectile.png");
        backgroundImg = new Texture("background.png");
        font = new BitmapFont(Gdx.files.internal("Title.fnt")); // use arial
        inGameMusic = Gdx.audio.newMusic(Gdx.files.internal("inGame.mp3"));


        // assets menggunakan class Assets
//        playerImg = assetManager.get(Assets.playerImg);
//        enemyImg = assetManager.get(Assets.enemyImg);
//        bossImg = assetManager.get(Assets.bossImg);
//        laserImg = assetManager.get(Assets.laserImg);
//        asteroidImg = assetManager.get(Assets.asteroidImg);
//        projectileImg = assetManager.get(Assets.projectileImg);
//        backgroundImg = assetManager.get(Assets.backgroundImg);
//        font = assetManager.get(Assets.menuFont); // use the title font
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
        i = 1;
        getEnemyBatch(i);

        bossState = false;

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

        //cek apakah objeknya boss/small enemy, gambar sesuai classnya
        for(Enemy enemy : enemies)
        {
            if(enemy instanceof SmallEnemy)
                game.batch.draw(enemyImg, enemy.x, enemy.y);
            else if(enemy instanceof Boss)
            {
                game.batch.draw(bossImg, enemy.x, enemy.y);
                font.draw(game.batch, "Enemy HP: " + enemies.get(0).getHP(), 400 - 32, 600 - 16);
            }
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
            //spawn peluru boss / detik ketika HP > 0
            if (TimeUtils.nanoTime() - lastProjectileTime > 1000_000_000 && enemies.get(0).getHP() > 0)
            {
                spawnProjectile();
            }
            else if (enemies.get(0).getHP() <= 0){
                enemies.removeIndex(0);
                bossState = false;
                enemyDestroyed++;
            }
        }
        else
        {
            if (TimeUtils.nanoTime() - lastSpawnTime > TimeUtils.millisToNanos(2000))   //waktu untuk ganti wave
            {
                getEnemyBatch(i);
                i++;
                if (i > 11)
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

            // utk boss tp msh bekerja jg utk musuh biasa
            if (bossState) // batch 11
            {
                if(laser.overlaps(enemies.get(0)) && enemies.get(0).getHP() > 0)
                {
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                    System.out.println("boss hp: " + enemies.get(0).getHP());
                    System.out.println("player score: " + player.getScore());
                }
            } else if (i==1 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i==2 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i==3 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(4))){
                    iterLaser.remove();
                    enemies.get(4).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i==4 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(4))){
                    iterLaser.remove();
                    enemies.get(4).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i == 5 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(4))){
                    iterLaser.remove();
                    enemies.get(4).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(5))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i==6 && enemies.size > 0) {
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(4))){
                    iterLaser.remove();
                    enemies.get(4).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(5))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i==7 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(4))){
                    iterLaser.remove();
                    enemies.get(4).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(5))){
                    iterLaser.remove();
                    enemies.get(5).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i == 8 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(4))){
                    iterLaser.remove();
                    enemies.get(4).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(5))){
                    iterLaser.remove();
                    enemies.get(5).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(6))){
                    iterLaser.remove();
                    enemies.get(6).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i == 9 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(4))){
                    iterLaser.remove();
                    enemies.get(4).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(5))){
                    iterLaser.remove();
                    enemies.get(5).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(6))){
                    iterLaser.remove();
                    enemies.get(6).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(7))){
                    iterLaser.remove();
                    enemies.get(7).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(8))){
                    iterLaser.remove();
                    enemies.get(8).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else if (i == 10 && enemies.size > 0){
                if (laser.overlaps(enemies.get(0))){
                    iterLaser.remove();
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(1))){
                    iterLaser.remove();
                    enemies.get(1).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(2))){
                    iterLaser.remove();
                    enemies.get(2).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(3))){
                    iterLaser.remove();
                    enemies.get(3).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(4))){
                    iterLaser.remove();
                    enemies.get(4).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(5))){
                    iterLaser.remove();
                    enemies.get(5).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(6))){
                    iterLaser.remove();
                    enemies.get(6).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(7))){
                    iterLaser.remove();
                    enemies.get(7).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
                if (laser.overlaps(enemies.get(8))){
                    iterLaser.remove();
                    enemies.get(8).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
        }

        //peluru boss jalan ke bawah
        Iterator<Rectangle> iterBossProjectile = projectiles.iterator();
        while (iterBossProjectile.hasNext())
        {
            Rectangle projectile = iterBossProjectile.next();
            projectile.y -= 300 * Gdx.graphics.getDeltaTime();
            if(projectile.y < 0)
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
        if (bossState){
            if (player.overlaps(enemies.get(0)) && TimeUtils.nanoTime() - lastPlayerCrashTime > 3000_000_000L) {
                lastPlayerCrashTime = TimeUtils.nanoTime();
                player.menerimadamage(30);
            }
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
            getEnemyBatch(11);
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
        if(i == 0)          //selesai
        {
            /*
            FORMASI
            00000000000
            00000000000
            01000000010
            00000000000
            00000000000
            */
            a = new SmallEnemy();
            a.x = 204;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 496;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 1;
        }
        else if(i == 1)     //selesai
        {
            /*
            FORMASI
            00100000100
            00000000000
            00000000000
            00000100000
            00000000000
            */
            // 3 enemies
            a = new SmallEnemy();
            a.x = 176;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 560;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 1;
        }
        else if(i == 2)     //selesai
        {
            /*
            FORMASI
            00000000000
            00000100000
            01000000010
            00000100000
            00000000000
            */
            // 4 enemies
            a = new SmallEnemy();
            a.x = 368;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 112;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 1.5;
        }
        else if(i == 3)     //selesai
        {
            /*
            FORMASI
            00000000000
            01000100010
            00010001000
            00000000000
            00000000000
            */
            // 5 enemies
            a = new SmallEnemy();
            a.x = 112;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 240;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 496;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 2;
        }
        else if(i == 4)     //selesai
        {
            /*
            FORMASI
            00000100000
            00000000000
            00010001000
            00000000000
            01000000010
            */
            // 5 enemies
            a = new SmallEnemy();
            a.x = 240;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 496;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 112;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 2.5;
        }
        else if(i == 5)
        {
            /*
            FORMASI
            000000000
            001010100
            000101000
            000010000
            000000000
            */
            // 6 enemies

            scoreMultiplier = 3;
        }
        else if(i == 6)     //selesai
        {
            /*
            FORMASI
            00000000000
            01000000010
            00001010000
            00100000100
            00000000000
            */
            // 6 enemies
            a = new SmallEnemy();
            a.x = 112;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 304;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 432;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 176;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 560;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 3.5;
        }
        else if(i == 7)
        {
            // 6 enemies
/*
            FORMASI
            00000000001
            00000000100
            00001010000
            00100000000
            10000000000
            */

            scoreMultiplier = 4;
        }
        else if(i == 8)
        {
            // 6 enemies

            scoreMultiplier = 4.5;
        }
        else if(i == 9)
        {
            // 9 enemies

            scoreMultiplier = 5;
        }
        else if(i == 10)    //selesai
        {
            /*
            FORMASI
            01000000010
            00010001000
            00000100000
            00010001000
            01000000010
            */
            // 9 enemies
            a = new SmallEnemy();
            a.x = 112;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 240;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 496;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 240;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 496;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 112;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 5.5;
        }
        else if(i == 11)    //selesai
        {
            a = new Boss();
            a.x = 400-100;
            a.y = 750;
            a.height = 200;
            a.width = 200;
            enemies.add(a);
            bossState = true;

            scoreMultiplier = 10;
        }
        else                //selesai
        {
            getEnemyBatch(0);
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
